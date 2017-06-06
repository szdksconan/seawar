package foxu.dcaccess.mem;

import java.util.HashMap;

import mustang.field.Fields;
import mustang.field.StringField;
import mustang.orm.ConnectionManager;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import shelby.dc.GameDBAccess;


/**
 * 封停设备缓存管理器
 * @author yw
 *
 */
public class ForbidMemCache
{

	/** 数据库操作中心 */
	GameDBAccess dbaccess;
	
	/** 黑名单 */
	HashMap<String,String> deviceMap=new HashMap<String,String>();
	/** 白名单 */
	HashMap<String,String> freeDeviceMap=new HashMap<String,String>();
	
	ConnectionManager cm;
	
	public boolean isForbid(String deviceId)
	{
		return deviceMap.get(deviceId)!=null;
	}
	
	public synchronized void add(String deviceId)
	{
		if(deviceMap.get(deviceId)!=null)return;
		String sql="insert into forbid (deviceId) values ('"+deviceId+"')";
		try
		{
			SqlKit.execute(cm,sql);
			deviceMap.put(deviceId,"");
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		
	}
	public synchronized void delete(String deviceId)
	{
		String sql="delete from forbid where deviceId='"+deviceId+"'";
		try
		{
			SqlKit.execute(cm,sql);
			deviceMap.remove(deviceId);
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
	}
	public void addFree(String did)
	{
		freeDeviceMap.put(did,"");
	}
	public void deleteFree(String did)
	{
		freeDeviceMap.remove(did);
	}
	public boolean isFree(String did)
	{
		return freeDeviceMap.get(did)!=null;
	}
	public void load()
	{
		cm=((SqlPersistence)dbaccess.getGamePersistence()).getConnectionManager();
		String sql="select deviceId from forbid";
		Fields[] fields=SqlKit.querys(cm,sql);
		if(fields==null)return;
		for(int i=0;i<fields.length;i++)
		{
			deviceMap.put(((StringField)fields[i].get("deviceId")).value,"");
		}
	}
	
	public GameDBAccess getDbaccess()
	{
		return dbaccess;
	}
	
	public void setDbaccess(GameDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}
	
	public HashMap<String,String> getDeviceMap()
	{
		return deviceMap;
	}
	
	public void setDeviceMap(HashMap<String,String> deviceMap)
	{
		this.deviceMap=deviceMap;
	}
	
	
}
