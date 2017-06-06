package foxu.sea.bind;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONException;
import javapns.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.User;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;

/**
 * �绰�󶨹�����
 * 
 * @author Alan
 * 
 */
public class TelBindingManager
{

	public static final Logger log=LogFactory
		.getLogger(TelBindingManager.class);
	public static final int TIME_OUT=20000;

	public static SSLSocketFactory sf;

	static
	{
		TrustManager[] tm={new X509TrustManager()
		{

			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs,
				String authType)
			{
			}
			public void checkServerTrusted(X509Certificate[] certs,
				String authType)
			{
			}
		}};
		try
		{
			SSLContext sslContext=SSLContext.getInstance("SSL","SunJSSE");
			sslContext.init(null,tm,new java.security.SecureRandom());
			sf=sslContext.getSocketFactory();
		}
		catch(Exception e)
		{
		}

	}
	/**
	 * 200 ���Ͷ��ųɹ� 512 �������ܾ����ʣ����߾ܾ����� 513 ����Appkey�����ڻ򱻽��á� 514 Ȩ�޲��� 515
	 * �������ڲ����� 517 ȱ�ٱ�Ҫ��������� 518 �������û����ֻ��Ÿ�ʽ����ȷ�������ֻ������ţ� 519 ��������֤�������������
	 * 520 ��Ч��֤�롣 526 ����
	 */
	public static final int SUCCESS=200,CODE_ERROR=520,TEL_ERROR=518,
					TIMES_LIMIT=519;
	public static String certifyURL="https://webapi.sms.mob.com/sms/verify";
	public static String appKey="e3b9cbdd263e";//TODO appKey="a9903a00a108"
	public static final String APPKEY="appkey",PHONE="phone",ZONE="zone",
					CODE="code",STATUS="status",ZONE_NUM_SEPARATOR="-";
	public static final int CHANGE_BINDING_TEL=16,CERTIFY_BINDING_TEL=17;
	public static final int BINDING_TEL_ERROR=14,BINDING_TEL_RIGHT=15;
	CreatObjectFactory objectFactory;

	/** �󶨹����Ƿ���� */
	public boolean isBindingAvailable(Player player)
	{
		return !(objectFactory.getUserDBAccess()
			.load(String.valueOf(player.getUser_id())).getUserType()==User.GUEST);
	}

	/** ���������֤����־ */
	public void addRequestRecord(Player player,String zone,String phone)
	{
		User user=objectFactory.getUserDBAccess().load(String.valueOf(player.getUser_id()));
		objectFactory.createBindingTrack(BindingTrack.TELPHONE,
			BindingTrack.REQUEST_CODE,BindingTrack.PLAYER,
			player.getUser_id(),player.getId(),formatWholeTel(zone,phone),
			user.getBindingTel(),user.getBindingTel());
	}
	
	/** ��֤�󶨵绰 */
	public String certifyPlayerTel(String zone,String telphone,String code,User user)
	{
		String tel=formatWholeTel(zone,telphone);
		String msg=certifyPlayerTel(tel,code,user);
		return msg;
	}

	/** ��֤�󶨵绰 */
	public String certifyPlayerTel(String telInfo,String code,User user)
	{
		if(certifyBindingTel(CERTIFY_BINDING_TEL,user,telInfo)!=BINDING_TEL_RIGHT)
			return "binding tel error";
		String[] telInfos=TextKit.split(telInfo,ZONE_NUM_SEPARATOR);
		TelInfo info=new TelInfo(appKey,telInfos[0],telInfos[1],code);
		info=sendHttpData(info);
		String msg=getResponseCode2Msg(info.status);
		return msg;
	}

	/** ����󶨵绰 */
	public String deletePlayerTel(Player player,String code)
	{
		User user=objectFactory.getUserDBAccess().load(String.valueOf(player.getUser_id()));
		if(!isBindingAvailable(player)) return "cannot update binding tel";
		if(user.getBindingTel()==null)
			return "binding tel not exist";
		String lastRecord=user.getBindingTel();
		String msg=certifyPlayerTel(lastRecord,code,user);
		if(msg!=null)
			return msg;
		return updatePlayerTel(player,"",BindingTrack.DELETE_RECORD);
	}

	/** ���°󶨵绰 */
	public String updatePlayerTel(Player player,String newZone,String newPhone,String code)
	{
		User user=objectFactory.getUserDBAccess().load(String.valueOf(player.getUser_id()));
		if(!isBindingAvailable(player)) return "cannot update binding tel";
		if(user.getBindingTel()!=null)
			return "binding tel exist";
		String msg=certifyPlayerTel(newZone,newPhone,code,user);
		if(msg!=null) return msg;
		return updatePlayerTel(player,formatWholeTel(newZone,newPhone),BindingTrack.ADD_RECORD);
	}
	
	public String updatePlayerTel(Player player,String tel,int actionType)
	{
		User user=objectFactory.getUserDBAccess().load(String.valueOf(player.getUser_id()));
		String lastRecord=user.getBindingTel();
		if(certifyBindingTel(CHANGE_BINDING_TEL,user,tel)!=BINDING_TEL_RIGHT)
			return "binding tel error";
		if("".equalsIgnoreCase(tel))
			tel=null;
		user.setBindingTel(tel);
		objectFactory.getUserDBAccess().save(user);
		objectFactory.createBindingTrack(BindingTrack.TELPHONE,actionType,
			BindingTrack.PLAYER,player.getUser_id(),player.getId(),
			tel,lastRecord,user.getBindingTel());
		return null;
	}

	/** ��װ���� ������֤ */
	public TelInfo sendHttpData(TelInfo info)
	{
		Map<String,String> params=new HashMap<String,String>();
		params.put(APPKEY,info.appKey);
		params.put(ZONE,String.valueOf(info.zone));
		params.put(PHONE,info.number);
		params.put(CODE,info.code);
		String appStr=sendHttpsStr(certifyURL,params);
		if(appStr==null) return info;
		try
		{
			JSONObject json=new JSONObject(appStr);
			info.status=json.getInt(STATUS);
		}
		catch(JSONException e)
		{
			if(log.isErrorEnabled())
				log.error("binding tel json error, json="+appStr+" ,"
					+e.getMessage());
		}
		return info;
	}

	/** ����https���� */
	public static String sendHttpsStr(String httpUrl,
		Map<String,String> params)
	{
		// test
//		if(params.size()>0)
//			return "{status:200}";
		StringBuffer sbf=new StringBuffer();
		StringBuffer param=null;
		BufferedWriter writer=null;
		BufferedReader reader=null;
		HttpsURLConnection uc=null;
		String appStr=null;
		try
		{
			// ip host verify
			HostnameVerifier hv=new HostnameVerifier()
			{

				public boolean verify(String urlHostName,SSLSession session)
				{
					return urlHostName.equals(session.getPeerHost());
				}
			};

			// set ip host verify
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			URL url=new URL(httpUrl);
			uc=(HttpsURLConnection)url.openConnection();
			uc.setSSLSocketFactory(sf);
			uc.setConnectTimeout(TIME_OUT);
			uc.setReadTimeout(TIME_OUT);
			uc.setDoOutput(true);
			writer=new BufferedWriter(new OutputStreamWriter(
				uc.getOutputStream())); // ���������������
			param=new StringBuffer();
			for(String key:params.keySet())
			{
				param.append("&");
				param.append(key).append("=").append(params.get(key));
			}
			writer.write(param.toString());// ���͵�����
			writer.flush();
			writer.close();
			reader=new BufferedReader(new InputStreamReader(
				uc.getInputStream()));// ��ȡ��������Ӧ��Ϣ
			String line;
			while((line=reader.readLine())!=null)
			{
				sbf.append(line);
			}
			reader.close();
			uc.disconnect();
			appStr=java.net.URLDecoder.decode(sbf.toString(),"utf-8");// ��ȡ���ص�json����
			return appStr;

		}
		catch(Exception e)
		{
			if(log.isErrorEnabled())
				log.error("binding tel https error, param="+param+" ,"
					+e.getMessage());
			return appStr;
		}
		finally
		{
			// �ر���
			if(writer!=null)
			{
				try
				{
					writer.close();
					writer=null;
				}
				catch(Exception e)
				{
				}
			}
			if(reader!=null)
			{
				try
				{
					reader.close();
					reader=null;
				}
				catch(Exception e)
				{
				}
			}
		}
	}

	/** ��ȡ��ͬһ�ֻ����˺� */
	public User getUsersByTel(String zone,String telphone,String accout)
	{
		User user=getObjectFactory().getUserDBAccess().loadUser(accout);
		if(user!=null
			&&(formatWholeTel(zone,telphone).equalsIgnoreCase(user
				.getBindingTel()))) return user;
		return null;
	}

	public String getResponseCode2Msg(int code)
	{
		String msg=null;
		switch(code)
		{
			case SUCCESS:
				break;
			case CODE_ERROR:
				msg="code error";
				break;
			case TEL_ERROR:
				msg="tel number error";
				break;
			case TIMES_LIMIT:
				msg="input limit error";
				break;
			default:
				msg="inner error "+code;
				break;
		}
		return msg;
	}

	/** ��ȡ�󶨵绰��Ϣ{����,����} */
	public static String[] getPlayerBindingTel(Player player,CreatObjectFactory objectFactory)
	{
		return TextKit.split(getPlayerWholeBindingTel(player,objectFactory),ZONE_NUM_SEPARATOR);
	}
	
	public static String getPlayerWholeBindingTel(Player player,CreatObjectFactory objectFactory)
	{
		User user=objectFactory.getUserDBAccess().load(String.valueOf(player.getUser_id()));
		return user.getBindingTel();
	}
	
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public String formatWholeTel(String zone,String phone)
	{
		return zone+ZONE_NUM_SEPARATOR+phone;
	}
	
	/** ������ͨѶ */
	public int certifyBindingTel(int type,User user,String tel)
	{

		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeByte(type);
		data.writeUTF(user.getUserAccount());
		data.writeUTF(tel);
		if(type==CHANGE_BINDING_TEL)
		{
			data.writeInt(TimeKit.getSecondTime());// �һ������ʱ��
			// ����
			data.writeInt(UserToCenterPort.AREA_ID);
			// ������
			data.writeInt(UserToCenterPort.SERVER_ID);
		}
		data=sendHttpData(data);
		int typeReturn=BINDING_TEL_ERROR;
		if(data!=null) typeReturn=data.readUnsignedByte();
		return typeReturn;
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
	
	static class TelInfo
	{

		String appKey;
		String zone;
		String number;
		String code;
		int status;

		public TelInfo(String appKey,String zone,String number,String code)
		{
			super();
			this.appKey=appKey;
			this.zone=zone;
			this.number=number;
			this.code=code;
		}

		public String getAppKey()
		{
			return appKey;
		}

		public void setAppKey(String appKey)
		{
			this.appKey=appKey;
		}

		public String getZone()
		{
			return zone;
		}

		public void setZone(String zone)
		{
			this.zone=zone;
		}

		public String getNumber()
		{
			return number;
		}

		public void setNumber(String number)
		{
			this.number=number;
		}

		public String getCode()
		{
			return code;
		}

		public void setCode(String code)
		{
			this.code=code;
		}

		public int getStatus()
		{
			return status;
		}

		public void setStatus(int status)
		{
			this.status=status;
		}

	}
}
