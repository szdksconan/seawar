package foxu.sea.proplist;

import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import foxu.email.EmailManager;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.port.UserToCenterPort;

/**
 * 实物奖励管理
 * 
 * @author Alan
 * 
 */
public class PhysicalPropManager implements TimerListener
{

	public static int SEND_TIME=10*60*1000;
	public static final String REPORT_TITLE="physical_prop_email_title";
	private String emailServer;
	private String smtpPort;
	private String emailUser;
	private String emailPwd;
	private String toEmail;
	IntKeyHashMap infos=new IntKeyHashMap();

	public void init()
	{
		TimerEvent event=new TimerEvent(this,"physical_prop_email",SEND_TIME);
		TimerCenter.getMinuteTimer().add(event);
	}

	public void addInfo(Player player,String rName,String rAddress,
		String rPhone,int sid)
	{
		ArrayList playerRecord=(ArrayList)infos.get(player.getId());
		if(playerRecord==null)
		{
			playerRecord=new ArrayList();
			infos.put(player.getId(),playerRecord);
		}
		Info info=new Info(player.getName(),rName,rAddress,rPhone,sid);
		playerRecord.add(info);
	}

	public void sendInfos()
	{
		StringBuffer reportsStr=new StringBuffer();
		String head="<table border='1'><tr><td colspan='7'>"
			+UserToCenterPort.SERVER_ID+"："+UserToCenterPort.SERVER_NAME
			+"</td></tr>";
		head+="<tr><td>player_id</td><td>player_name</td><td>post_name</td><td>post_address</td><td>post_phone</td><td>prop_sid</td><td>prop_name</td></tr>";
		reportsStr.append(head);
		if(infos.size()>0)
		{
			synchronized(infos)
			{
				int[] keys=infos.keyArray();
				for(int id:keys)
				{
					ArrayList playerRecord=(ArrayList)infos.get(id);
					// 组装html
					String pname=playerRecord.size()>0?((Info)playerRecord
						.get()).pname:"";
					reportsStr.append("<tr><td rowspan='"
						+playerRecord.size()+"'>"+id+"</td><td rowspan='"
						+playerRecord.size()+"'>"+pname+"</td>");
					boolean firstRow=true;
					for(int i=0;i<playerRecord.size();i++)
					{
						Info info=(Info)playerRecord.get(i);
						if(firstRow)
							firstRow=false;
						else
							reportsStr.append("<tr>");
						reportsStr.append("<td>"+info.name+"</td>");
						reportsStr.append("<td>"+info.address+"</td>");
						reportsStr.append("<td>"+info.phone+"</td>");
						reportsStr.append("<td>"+info.sid+"</td>");
						reportsStr.append("<td>"
							+InterTransltor.getInstance().getTransByKey(
								PublicConst.SERVER_LOCALE,
								"prop_sid_"+info.sid)+"</td>");
						reportsStr.append("</tr>");
					}
				}
				infos.clear();
			}
			reportsStr.append("</table>");
		}
		else
			return;

		String html=reportsStr.toString();
		EmailManager.emailServer=emailServer;
		EmailManager.smtpPort=smtpPort;
		EmailManager.user=emailUser;
		EmailManager.pwd=emailPwd;
		EmailManager.getInstance().sendMail(
			toEmail,
			InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,REPORT_TITLE),html);
	}

	class Info
	{

		String pname;
		String name;
		String address;
		String phone;
		int sid;

		public Info(String pname,String name,String address,String phone,
			int sid)
		{
			super();
			this.pname=pname;
			this.name=name;
			this.address=address;
			this.phone=phone;
			this.sid=sid;
		}
	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		sendInfos();
	}

	public String getEmailServer()
	{
		return emailServer;
	}

	public void setEmailServer(String emailServer)
	{
		this.emailServer=emailServer;
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

	public String getToEmail()
	{
		return toEmail;
	}

	public void setToEmail(String toEmail)
	{
		this.toEmail=toEmail;
	}

	public IntKeyHashMap getInfos()
	{
		return infos;
	}

	public void setInfos(IntKeyHashMap infos)
	{
		this.infos=infos;
	}

	public String getSmtpPort()
	{
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort)
	{
		this.smtpPort=smtpPort;
	}

}
