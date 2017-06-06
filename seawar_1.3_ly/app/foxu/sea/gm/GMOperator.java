package foxu.sea.gm;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.back.BackKit;
import foxu.sea.port.UserToCenterPort;

/**
 * GM操作，返回JSON串
 * @author comeback
 *
 */
public abstract class GMOperator
{
	public static int MIN_PRIVILEGE=0,MAX_PRIVILEGE=500;
	
	/**WEB服务器IP*/
	public static String SERVER_IP_LIST[];
	
	/** 需要的权限等级 */
	private int privilege=Integer.MAX_VALUE;
	
	/** 操作是否需要记录日志 */
	private boolean needLog=false;
	
	private static int timeZone=0;
	
	static
	{
		TimeZone tz=TimeZone.getDefault();
		timeZone=tz.getRawOffset()+Calendar.getInstance().get(Calendar.DST_OFFSET);
		timeZone/=3600*1000;
	}
	
	/**
	 * 设置GM操作所需要的权限等级
	 * @param privilege
	 */
	public void setPrivilege(int privilege)
	{
		this.privilege=privilege;
	}
	
	/**
	 * 执行操作
	 * @param params
	 * @return
	 * @throws JSONException 
	 */
	public final String operate(String user,Map<String,String> params,ServerInfo info) throws JSONException
	{
		JSONArray array=new JSONArray();
		JSONObject successObj=new JSONObject();
		int ret=operate(user,params,array,info);
		String ser=String.valueOf(BackKit.getContext().get("sea_ServerID"));
		int server_id=0;
		if(!ser.equals("null"))
			server_id=Integer.parseInt(ser);
		else
			server_id=UserToCenterPort.SERVER_ID;
		successObj.put("success",ret);
		successObj.put("data",array);
		successObj.put("server_id",server_id);
		successObj.put("time_zone",timeZone);
		return successObj.toString();
	}
	
	/**
	 * 检查操作所需要的权限等级
	 * @return
	 */
	public boolean checkPrivilegeLevel(int privilege)
	{
		return privilege>=this.privilege;
	}
	
	/**
	 * 检查操作所需要的权限等级
	 * @return
	 */
	public boolean checkPrivilegeLevel(String ip)
	{
		for(int i=0;i<SERVER_IP_LIST.length;i++)
		{
			if(ip.equals(SERVER_IP_LIST[i]))
				return true;
		}
		return false;
	}
	/**
	 * 获取权限等级
	 * @return
	 */
	public int getPrivilegeLevel()
	{
		return this.privilege;
	}
	
	/**
	 * 设置操作是否需要记录日志
	 * @param needlog
	 */
	public void setNeedLog(boolean needlog)
	{
		this.needLog=needlog;
	}
	
	/**
	 * 操作是否需要记录日志
	 * @return
	 */
	public boolean isNeedLog()
	{
		return this.needLog;
	}
	
	/**
	 * 操作执行方法，返回错误代码
	 * @param params
	 * @param array
	 * @return 成功时返回GMError.ERR_SUCCESS
	 */
	public abstract int operate(String user,Map<String,String> params,JSONArray jsonArray,ServerInfo info);
}
