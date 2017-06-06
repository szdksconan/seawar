package foxu.sea.gm;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javapns.json.JSONException;
import javapns.json.JSONObject;
import shelby.cc.CCManager;
import shelby.ds.DSManager;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.orm.ConnectionManager;
import mustang.orm.SqlKit;
import mustang.util.TimeKit;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;

/**
 * GM���ܹ�����
 * @author comeback
 *
 */
public class GMManager implements ServerInfo
{
	private static Logger log=LogFactory.getLogger(GMManager.class);
	
	public static boolean IS_TEST=false;
	
	public final String RUN_COMMAND_ERROR="{\"success\":%d}";
	
	/** ����GM�˺� */
	Map<String,String> gmAccountsCache=new HashMap<String,String>();
	
	/** GM�������� */
	Map<String,GMOperator> gmOperators=new HashMap<String,GMOperator>();
	
	/** ���󹤳� */
	CreatObjectFactory objectFactory;
	
	/** ���ݿ����Ӷ��� */
	ConnectionManager connectionManager;
	
	/** CCManager */
	CCManager ccmanager;
	
	DSManager dsmanager;
	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
	public void setConnectionManager(ConnectionManager connectionManager)
	{
		this.connectionManager=connectionManager;
	}
	
	public void setCCManager(CCManager ccmanager)
	{
		this.ccmanager=ccmanager;
	}
	
	public void setDSManager(DSManager dsmanager)
	{
		this.dsmanager=dsmanager;
	}
	
	/**
	 * 
	 * @param command
	 * @param operator
	 */
	public void addOperator(String command,GMOperator operator)
	{
		addOperator(command,operator.getPrivilegeLevel(),operator);
	}
	
	/**
	 * 
	 * @param command
	 * @param operator
	 */
	public void addOperator(String command,int privilege,GMOperator operator)
	{
		operator.setPrivilege(privilege);
		gmOperators.put(command,operator);
	}

	public String process(String user,String password,String command,Map<String,String> params)
	{
		//TODO ��ʱ����,Ϊ����ԭ�����߼�
		// ���Ȩ��
		int privilege=selectPrivilege(user,password);
		GMOperator operator=gmOperators.get(command);
		if(operator!=null) 
		{
			try
			{
				if(operator.checkPrivilegeLevel(privilege))
				{
					String jsonStr=operator.operate(user,params,this);
					if(operator.isNeedLog())
						recordLog(user,command,params,jsonStr);
					return jsonStr;
				}
				else
					return String.format(RUN_COMMAND_ERROR,GMConstant.ERR_PRIVILEGE_ERROR);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		if(privilege>=0)
			this.recordLog(user,command,params,"");
		if(privilege<0)
			return null;
		else
			return "";
		/* ��������ʽ����		
		GMOperator operator=gmOperators.get(command);
		if(operator==null)
			return String.format(RUN_COMMAND_ERROR,GMConstant.ERR_COMMAND_NOT_EXISTS);
		try
		{
			// ���Ȩ��
			int privilege=selectPrivilege(user,password);
			// �������
			if(privilege<0)
			{
				return String.format(RUN_COMMAND_ERROR,privilege==GMConstant.ERR_UNKNOWN?privilege:-privilege);
			}
			if(!operator.checkPrivilegeLevel(privilege))
			{
				return String.format(RUN_COMMAND_ERROR,GMConstant.ERR_PRIVILEGE_ERROR);
			}
			String jsonStr=operator.operate(params,this);
			// ��¼������־
			if(operator.isNeedLog())
				recordLog(user,params,jsonStr);
			return jsonStr;
		}
		catch(Throwable t)
		{
			return String.format(RUN_COMMAND_ERROR,GMConstant.ERR_UNKNOWN);
		}
		*/
	}
	
	public void updatePassword(String user,String password)
	{
		String info=this.gmAccountsCache.get(user);
		if(info!=null)
		{
			try
			{
				JSONObject jo=new JSONObject(info);
				jo.put("password",password);
				gmAccountsCache.put(user,jo.toString());
			}
			catch(JSONException e)
			{
				
			}
		}
	}
	
	public boolean checkPassword(String user,String password)
	{
		String info=gmAccountsCache.get(user);
		if(info==null||password==null)
			return false;
		JSONObject jo;
		try
		{
			jo=new JSONObject(info);
			return password.equalsIgnoreCase(jo.getString("password"));
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public String paramsToString(Map<String,String> params)
	{
		Iterator<Entry<String,String>> iter=params.entrySet().iterator();
		StringBuilder sb=new StringBuilder();
		while(iter.hasNext())
		{
			Entry<String,String> entry=iter.next();
			if(sb.length()>0)
				sb.append('&');
			sb.append(entry.getKey()).append('=').append(entry.getValue());
		}
		return URLEncoder.encode(sb.toString());
	}
	
	/**
	 * ��ѯȨ��
	 * @param user
	 * @param password
	 * @return ����Ȩ�ޣ�����˺Ų����ڣ�����-1�����������󷵻�0
	 */
	public int selectPrivilege(String user,String password)
	{
		if(IS_TEST)
			return 500;
		if(user==null||user.length()==0)
			return -GMConstant.ERR_ACCOUNT_NOT_EXISTS;
		if(password==null||password.length()==0)
			return -GMConstant.ERR_PASSWORD_ERROR;
		String info=gmAccountsCache.get(user);
		boolean updated=false;
		if(info==null)
		{
			int ret=updateUserInfo(user);
			if(ret!=GMConstant.ERR_SUCCESS)
			{
				return ret>0?-ret:ret;
			}
			info=gmAccountsCache.get(user);
			updated=true;
		}
		JSONObject jo=null;
		try
		{
			jo=new JSONObject(info);
			String oldpw=jo.getString("password");
			if(!password.equalsIgnoreCase(oldpw))
			{
				// ����Ѿ����¹�������������󣬷������һ��
				if(updated)
					return -GMConstant.ERR_PASSWORD_ERROR;
				int ret=updateUserInfo(user);
				if(ret!=GMConstant.ERR_SUCCESS)
					return ret>0?-ret:ret;
				info=gmAccountsCache.get(user);
				jo=new JSONObject(info);
				// ����֮������֤�����Ƿ���ȷ
				if(!password.equalsIgnoreCase(jo.getString("password")))
				{
					return -GMConstant.ERR_PASSWORD_ERROR;
				}
			}
			return jo.getInt("privilege");
		}
		catch(JSONException e)
		{
			log.info(info);
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}
	}
	
	public int updateUserInfo(String user)
	{
		Map<String,String> params=new HashMap<String,String>();
		params.put("type","1");
		params.put("user_account",user);
		String info= sendHttpDataToCenter(7,params);
		if(info==null)
			return GMConstant.ERR_GAME_CENTER_COMUNICATION_ERROR;
		try
		{
			JSONObject jo=new JSONObject(info);
			boolean b=jo.getBoolean("success");
			if(b)
			{
				jo.remove("success");
				gmAccountsCache.put(user,jo.toString());
				return GMConstant.ERR_SUCCESS;
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return GMConstant.ERR_UNKNOWN;
	}

	public CreatObjectFactory getObjectFactory()
	{
		return this.objectFactory;
	}

	public ConnectionManager getConnectionManager()
	{
		return this.connectionManager;
	}
	
	public CCManager getCCManager()
	{
		return this.ccmanager;
	}
	
	public DSManager getDSManager()
	{
		return this.dsmanager;
	}
	
	public String getGameCenterIP()
	{
		return GameDBCCAccess.GAME_CENTER_IP;
	}
	
	public int getGameCenterPort()
	{
		return GameDBCCAccess.GAME_CENTER_HTTP_PORT;
	}
	
	private String sendHttpDataToCenter(int type,Map<String,String> params)
	{
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		// ����port
		params.put("port","3");
		params.put("table_type",String.valueOf(type));
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
				+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",params,null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		if(re!=null)
			return re.getContent();
		return null;
	}
	
	/**
	 * ��GM������¼����־
	 * @param user
	 * @param player
	 * @param parameters
	 * @param success
	 */
	private void recordLog(String user,String command,Map<String,String> params,String resultStr)
	{
		String parameters=paramsToString(params);
		if(parameters.length()>500)
			parameters=parameters.substring(0,500);
		String player=params.get("player_name");
		String success=String.valueOf(GMConstant.ERR_UNKNOWN);
		try
		{
			JSONObject json=new JSONObject(resultStr);
			success=json.getString("success");
		}
		catch(JSONException e1)
		{
			e1.printStackTrace();
		}
//		int s=resultStr.indexOf("\"success\":");
//		if(s>=0)
//		{
//			int e=resultStr.indexOf(",",s);
//			if(e<0)
//				e=resultStr.indexOf('}',s);
//			if(e>s+10)
//			{
//				success=resultStr.substring(s+10,e);
//			}
//				
//		}
		String sql="insert gm_tracks (user_account,player_name,command,parameters,result,created_at)" +
				"values" +
				"('"+user+"','"
				+(player==null?"":player)+"','"
				+command+"','"
				+parameters+"',"
				+success+","
				+TimeKit.getSecondTime()+")";
		SqlKit.execute(getConnectionManager(),sql);
	}

	
	public Map<String,GMOperator> getGmOperators()
	{
		return gmOperators;
	}
	
	public String webprocess(String user,String ip,String command,Map<String,String> params)
	{
		//TODO ��ʱ����,Ϊ����ԭ�����߼�
		// ���Ȩ��
//		int privilege=selectPrivilege(user,password);
		GMOperator operator=gmOperators.get(command);
		boolean level=operator.checkPrivilegeLevel(ip);
		if(operator!=null) 
		{
			try
			{
//				if(operator.checkPrivilegeLevel(privilege))
				if(level)
				{
					String jsonStr=operator.operate(user,params,this);
					if(operator.isNeedLog())
						recordLog(user,command,params,jsonStr);
					return jsonStr;
				}
				else
					return String.format(RUN_COMMAND_ERROR,GMConstant.ERR_PRIVILEGE_ERROR);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		if(level)
			this.recordLog(user,command,params,"");
		else return null;
			     return "";
	}
	
	
}
