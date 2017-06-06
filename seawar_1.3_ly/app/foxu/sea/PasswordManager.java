package foxu.sea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.PasswordDBAccess;
import foxu.dcaccess.UserGameDBAccess;
import foxu.email.EmailManager;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;
import mustang.io.ByteBuffer;
import mustang.math.Random;
import mustang.math.Random1;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * @author Alan
 */
public class PasswordManager implements TimerListener
{

	/** ��֤�볬ʱʱ�� */
	public static final int DISABLE_TIME=12*60*60;
	/** ��ʱ��֤���������� */
	public static final int INTERVAL_TIME=60*60;
	/** ��֤�볤�� */
	public static final int CODE_LENGTH=6;
	/** ����������� */
	public static final int INPUT_LIMIT=5;
	/** ÿ�豸ÿ����Խ��з����ʼ��Ĵ��� */
	public static final int DEVICE_LIMIT=10;
	/** �����һ������Ӧtype */
	public static int RESET_PASSWORD=13;
	private UserGameDBAccess dbaccess;
	/** ��֤���ʼ����ͼ�¼ */
	private IntKeyHashMap emailCodeList;
	/** �豸�������� */
	private HashMap<String,ResetRecord> deviceLimitList;
	private String emailServer;
	private String smtpPort;
	private String emailUser;
	private String emailPwd;
	private PasswordDBAccess pwdDBAccess;
	/** �Ƿ�ʹ��Ĭ���ʼ���ʽ */
	private boolean isDefaultEmail=true;

	public PasswordManager()
	{
		emailCodeList=new IntKeyHashMap();
		deviceLimitList=new HashMap<String,ResetRecord>();
		TimerCenter.getMinuteTimer().add(
			new TimerEvent(this,"timeout",INTERVAL_TIME));
	}

	public UserGameDBAccess getDbaccess()
	{
		return dbaccess;
	}

	public void setDbaccess(UserGameDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}

	/** ��ȡ���豸���컹�ܲ����ʼ���֤��Ĵ��� */
	private int getDeviceLimit(String deviceId)
	{
		ResetRecord rr=deviceLimitList.get(deviceId);
		if(rr==null)
		{
			rr=new ResetRecord(TimeKit.getSecondTime(),0,DEVICE_LIMIT);
			deviceLimitList.put(deviceId,rr);
		}
		else
		{
			if((rr.time+24*60*60)<TimeKit.getSecondTime())
			{
				rr.limit=DEVICE_LIMIT;
				rr.time=TimeKit.getSecondTime();
			}
		}
		return rr.limit;
	}
	/** ��֤�û������Ƿ�Ϸ����Ҵ��ڣ�����null��δͨ����֤ */
	public User getUserByEmail(String email)
	{
		if(checkUser(email)==null)
		{
			return dbaccess.loadUser(email);
		}
		return null;
	}

	private String checkUser(String email)
	{
		/** �����ʽ */
		if(!email.matches(UserToCenterPort.EMAIL_MATCHES))
			return "account is wrong";
		// �鿴������Ϸ���ݿ��˺���
		if(!dbaccess.isExist(email,0)) return "account not exist";
		return null;
	}

	private int generateCode(int length)
	{
		int baseNum=1;
		for(int i=0;i<length-1;i++)
		{
			baseNum*=10;
		}
		Random random=new Random1();
		return random.randomValue(baseNum,10*baseNum);
	}

	/**
	 * ���һ��׼����������ļ�¼ -2��ϵͳ�쳣��-1�����ͳɹ���0���豸������������
	 */
	public int addResetRecord(User user,String deviceId)
	{
		if(getDeviceLimit(deviceId)<=0) return 0;
		int result=-1;
		int codes=generateCode(CODE_LENGTH);
		ResetRecord record=new ResetRecord(TimeKit.getSecondTime(),codes,
			INPUT_LIMIT);
		emailCodeList.put(user.getId(),record);
		try
		{
			EmailManager.emailServer=emailServer;
			EmailManager.user=emailUser;
			EmailManager.pwd=emailPwd;
			EmailManager.smtpPort=smtpPort;
			if(isDefaultEmail)
				emailFooter=emailFooter
					.replace("%{%","<")
					.replace("%}%",">")
					.replace("%YEAR%",
						Calendar.getInstance().get(Calendar.YEAR)+"");
			EmailManager.getInstance().sendMail(
				user.getUserAccount(),
				getEmailTitle(),
				emailHeader
					+InterTransltor.getInstance()
						.getTransByKey(PublicConst.SERVER_LOCALE,
							"password_reset_mail_code")
					+"��"
					+codes
					+"<br />"
					+InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,
						"password_reset_mail_device")+"��"+deviceId
					+emailFooter);
			PasswordRecord pwdRecord=new PasswordRecord(deviceId,
				user.getUserAccount(),TimeKit.getSecondTime());
			pwdDBAccess.save(pwdRecord);
			ResetRecord lastRecord=deviceLimitList.get(deviceId);
			lastRecord.limit-=1;
			// -----------�����ʼ�
		}
		catch(Exception e)
		{
			e.printStackTrace();
			result=-2;
		}
		return result;
	}

	private String validateCode(int userId,int codes)
	{
		ResetRecord record=(ResetRecord)emailCodeList.get(userId);
		// û��������¼
		if(record==null)
		{
			return "record not exist";
		}
		// ʣ�������������
		else if(record.limit<1)
		{
			return "input limit error";
		}
		// ��¼��ʱ
		else if(record.time+DISABLE_TIME<TimeKit.getSecondTime())
		{
			record.limit=0;
			return "code time out";
		}
		// ��֤�����
		else if(codes!=record.code)
		{
			record.limit-=1;
			return "code error";
		}
		return null;
	}

	/** ͨ����֤������˻�id����֤���������������֤������-1��ͨ����֤�޸ĳɹ� */
	public int resetPassword(User user,String pwd,int codes)
	{
		String result=validateCode(user.getId(),codes);
		ResetRecord record=(ResetRecord)emailCodeList.get(user.getId());
		if(result!=null)
		{
			if(record==null)
			{
				return 0;
			}
			return record.limit;
		}
		else
		{
			emailCodeList.remove(user.getId());
			return resetCustomPassword(user,pwd);
		}
	}
	
	/** ��������������֤������-1���޸ĳɹ� */
	public int resetCustomPassword(User user,String pwd)
	{

		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeByte(RESET_PASSWORD);
		data.writeUTF(user.getUserAccount());
		data.writeUTF(pwd);
		data.writeInt(TimeKit.getSecondTime());//�һ������ʱ��
		//����
		data.writeInt(UserToCenterPort.AREA_ID);
		data=sendHttpData(data);
		int typeReturn=-2;
		if(data!=null)
			typeReturn=data.readUnsignedByte();
		if(typeReturn!=0)
		{
			return -2;
		}
		user.setPassword(pwd);
		dbaccess.save(user);
		return -1;
	}

	public ByteBuffer sendHttpData(ByteBuffer data)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// ����port
		map.put("port","1");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
				+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,null);
		}
		catch(IOException e)
		{
			// TODO �Զ����� catch ��
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		synchronized(emailCodeList)
		{
			int[] userRecords=emailCodeList.keyArray();
			for(int i=0;i<userRecords.length;i++)
			{
				ResetRecord record=(ResetRecord)emailCodeList
					.get(userRecords[i]);
				if((record.time+DISABLE_TIME)<(int)(e.getCurrentTime()/1000)
					||record.limit<1)
				{
					emailCodeList.remove(userRecords[i]);// ������������ϼ�¼
				}
			}
		}
		synchronized(deviceLimitList)
		{
			Set<String> deviceRecord=deviceLimitList.keySet();
			List<String> removeKey=new ArrayList<String>();
			for(String key:deviceRecord)
			{
				ResetRecord record=deviceLimitList.get(key);
				if((record.time+24*60*60)<(int)(e.getCurrentTime()/1000))
				{
					removeKey.add(key);
				}
			}
			for(int i=0;i<removeKey.size();i++)
			{
				String key=removeKey.get(i);
				deviceLimitList.remove(key);
			}
		}
	}

	/**�����ʼ�**/
	public  int  sendMail(User user,int length,String deviceId)
	{
		int codes=generateCode(length);
		try
		{
			EmailManager.emailServer=emailServer;
			EmailManager.user=emailUser;
			EmailManager.pwd=emailPwd;
			EmailManager.smtpPort=smtpPort;
			if(isDefaultEmail)
				emailFoot=emailFoot
					.replace("%{%","<")
					.replace("%}%",">")
					.replace("%YEAR%",
						Calendar.getInstance().get(Calendar.YEAR)+"");
			EmailManager.getInstance().sendMail(
				user.getUserAccount(),
				getEmailTitle(),
					emailHead
					+InterTransltor.getInstance()
						.getTransByKey(PublicConst.SERVER_LOCALE,
							"password_reset_mail_code")
					+"��"
					+codes
					+"<br />"
					+InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,
						"password_reset_mail_device")+"��"+deviceId
					+emailFoot);
		}
		catch(Exception e)
		{
			return 0;
		}
		return codes;
	}
	
	public String getEmailTitle()
	{
		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"password_reset_mail_title");
		title=TextKit.replace(title,"%",InterTransltor.getInstance()
			.getTransByKey(PublicConst.SERVER_LOCALE,"game_name"));
		return title;
	}
	
	public String getEmailServer()
	{
		return emailServer;
	}

	public void setEmailServer(String emailServer)
	{
		this.emailServer=emailServer;
	}
	
	public String getSmtpPort()
	{
		return smtpPort;
	}
	
	public void setSmtpPort(String smtpPort)
	{
		this.smtpPort=smtpPort;
	}

	public String getEmailUser()
	{
		return emailUser;
	}

	public void setEmailUser(String emailUser)
	{
		this.emailUser=emailUser;
	}

	public String getEmailPwd()
	{
		return emailPwd;
	}

	public void setEmailPwd(String emailPwd)
	{
		this.emailPwd=emailPwd;
	}

	public String getEmailHeader()
	{
		return emailHeader;
	}

	public void setEmailHeader(String emailHeader)
	{
		this.emailHeader=emailHeader;
		isDefaultEmail=false;
	}

	public String getEmailFooter()
	{
		return emailFooter;
	}

	public void setEmailFooter(String emailFooter)
	{
		this.emailFooter=emailFooter;
		isDefaultEmail=false;
	}

	class ResetRecord
	{

		int time;
		int code;
		int limit;

		public ResetRecord(int time,int code,int limit)
		{
			this.time=time;
			this.code=code;
			this.limit=limit;
		}

	}

	public void setPwdDBAccess(PasswordDBAccess pwdDBAccess)
	{
		this.pwdDBAccess=pwdDBAccess;
	}

	private String emailHeader="<table border='0' cellpadding='0' cellspacing='0' width='100%'><tbody>"
		+"<tr><td bgcolor='#f7f9fa' align='center' style='padding:22px 0 20px 0'><table border='0' cellpadding='0'"
		+" cellspacing='0' style='background-color:f7f9fa; border-radius:3px;border:1px solid #dedede;margin:0 auto; "
		+"background-color:#ffffff;min-width:300px;max-width:650px;'><tbody><tr><td align='center' "
		+"style='border-top-left-radius:3px;border-top-right-radius:3px;'><table border='0' cellpadding='0' cellspacing='0'"
		+" width='100%'><tbody><tr align='center'><td style='border-bottom:solid 2px #2B7ACD;'>"
		+"<img width='15%' src='http://bbs.foxugame.com/zh-cn/data/attachment/forum/201404/11/152356zrrrdwr1w663w1dm.jpg' style='vertical-align:middle;'>"
		+"<img width='70%' src='http://bbs.foxugame.com/zh-cn/data/attachment/forum/201404/11/152406in0mttx07r0v904r.png' style='vertical-align:middle;'></td></tr></t"
		+"body></table></td></tr><tr><td bgcolor='#ffffff' align='center' style='padding: 0 15px 0px 15px;'><table border='0' cellpadding='0'"
		+" cellspacing='0' width='100%'><tbody><tr><td><table width='100%' border='0' cellpadding='0' cellspacing='0'><tbody><tr><td><table"
		+" cellpadding='0' cellspacing='0' border='0' align='left'><tbody><tr><td align='left' valign='top'><table width='100%' border='0' "
		+"cellpadding='0' cellspacing='0'><tbody><tr><td bgcolor='#ffffff' align='left' style='background-color:#ffffff; font-size: 17px; "
		+"color:#7b7b7b; padding:28px 0 0 0;line-height:25px;'><b>"
		+InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"password_reset_mail_command")
		+"</b></td></tr></tr><td bgcolor='#ffffff' align='left' style='backg"
		+"round-color:#ffffff; font-size: 14px; color:#7b7b7b; padding:28px 0 0 0;line-height:25px;'><b>";
	private String emailFooter="</b><tr><tr><td align='left' valign='top' style='font-size:15px; color:#7b7b7b; font-size:14px; line-height: 25px; font-family:Hiragino Sans GB; padding: 20px 0px 20px 0px'>"
		+InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"password_reset_mail_tip")
		+"</td></tr><tr><td style='border-top:1px #f1f4f6 solid; padding: 26px 0 32px 0;' align='center'><table border='0' cellspacing='0' "
		+"cellpadding='0'><tbody><tr><td><span style='font-family:Hiragino Sans GB;font-size:17px;color:#0a82e4'></span></td></tr></tbody>"
		+"</table></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody>"
		+"</table></td></tr></tbody></table><table cellpadding='0' cellspacing='0' border='0' width='100%'><tbody><tr><td bgcolor='#f7f9fa' "
		+"align='center'><table border='0' cellpadding='0' cellspacing='0' align='center' style='min-width:300px;max-width:650px;'><tbody><tr>"
		+"<td align='center' valign='top' bgcolor='#f7f9fa' style='font-family:Hiragino Sans GB; font-size:12px; color:#b6c2cc; line-height:17px;"
		+" padding:0 0 25px 0;'>"
		+InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"password_reset_mail_foottip")
		+"<br />&reg;"
		+InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"password_reset_mail_reg")
		+"</td></tr></tbody></table></td></tr></tbody></table>";
	
	private String emailHead="<table border='0' cellpadding='0' cellspacing='0' width='100%'><tbody>"
					+"<tr><td bgcolor='#f7f9fa' align='center' style='padding:22px 0 20px 0'><table border='0' cellpadding='0'"
					+" cellspacing='0' style='background-color:f7f9fa; border-radius:3px;border:1px solid #dedede;margin:0 auto; "
					+"background-color:#ffffff;min-width:300px;max-width:650px;'><tbody><tr><td align='center' "
					+"style='border-top-left-radius:3px;border-top-right-radius:3px;'><table border='0' cellpadding='0' cellspacing='0'"
					+" width='100%'><tbody><tr align='center'><td style='border-bottom:solid 2px #2B7ACD;'>"
					+"<img width='15%' src='http://bbs.foxugame.com/zh-cn/data/attachment/forum/201404/11/152356zrrrdwr1w663w1dm.jpg' style='vertical-align:middle;'>"
					+"<img width='70%' src='http://bbs.foxugame.com/zh-cn/data/attachment/forum/201404/11/152406in0mttx07r0v904r.png' style='vertical-align:middle;'></td></tr></t"
					+"body></table></td></tr><tr><td bgcolor='#ffffff' align='center' style='padding: 0 15px 0px 15px;'><table border='0' cellpadding='0'"
					+" cellspacing='0' width='100%'><tbody><tr><td><table width='100%' border='0' cellpadding='0' cellspacing='0'><tbody><tr><td><table"
					+" cellpadding='0' cellspacing='0' border='0' align='left'><tbody><tr><td align='left' valign='top'><table width='100%' border='0' "
					+"cellpadding='0' cellspacing='0'><tbody><tr><td bgcolor='#ffffff' align='left' style='background-color:#ffffff; font-size: 17px; "
					+"color:#7b7b7b; padding:28px 0 0 0;line-height:25px;'><b>"
					+InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"account_reset_mail_command")
					+"</b></td></tr></tr><td bgcolor='#ffffff' align='left' style='backg"
					+"round-color:#ffffff; font-size: 14px; color:#7b7b7b; padding:28px 0 0 0;line-height:25px;'><b>";
	
	
	private String emailFoot="</b><tr><tr><td align='left' valign='top' style='font-size:15px; color:#7b7b7b; font-size:14px; line-height: 25px; font-family:Hiragino Sans GB; padding: 20px 0px 20px 0px'>"
					+InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"account_reset_mail_tip")
					+"</td></tr><tr><td style='border-top:1px #f1f4f6 solid; padding: 26px 0 32px 0;' align='center'><table border='0' cellspacing='0' "
					+"cellpadding='0'><tbody><tr><td><span style='font-family:Hiragino Sans GB;font-size:17px;color:#0a82e4'></span></td></tr></tbody>"
					+"</table></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody>"
					+"</table></td></tr></tbody></table><table cellpadding='0' cellspacing='0' border='0' width='100%'><tbody><tr><td bgcolor='#f7f9fa' "
					+"align='center'><table border='0' cellpadding='0' cellspacing='0' align='center' style='min-width:300px;max-width:650px;'><tbody><tr>"
					+"<td align='center' valign='top' bgcolor='#f7f9fa' style='font-family:Hiragino Sans GB; font-size:12px; color:#b6c2cc; line-height:17px;"
					+" padding:0 0 25px 0;'>"
					+InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"password_reset_mail_foottip")
					+"<br />&reg;"
					+InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"password_reset_mail_reg")
					+"</td></tr></tbody></table></td></tr></tbody></table>";
}