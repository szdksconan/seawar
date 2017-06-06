package foxu.dcaccess.mem;


import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.User;
import foxu.sea.kit.SeaBackKit;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.util.TimeKit;
import shelby.dc.GameDBAccess;

/**
 * 登陆日志
 * 
 * @author yw
 * 
 */
public class LoginLogMemCache
{

	public static LoginLogMemCache loginLogMem;

	/** 超时删除时间  */
	public static int DAYS=30;
	/** 数据库操作中心 */
	GameDBAccess dbaccess;

	public void init()
	{
		loginLogMem=this;
		clear();
	}

	public void save(Player player,String deviceId,String pdid,boolean force)
	{
		if(!force
			&&SeaBackKit.isSameDay(player.getLoginLogTime(),TimeKit.getSecondTime())) return;
		String sql="insert into loginlog (id,loginTime,deviceId,ip,pdid) values ("
			+player.getId()
			+","
			+TimeKit.getSecondTime()
			+",'"
			+deviceId
			+"','"
			+player.getLoginIp()
			+"','"
			+pdid+"')";
		SqlPersistence sp=(SqlPersistence)dbaccess.getGamePersistence();
		try
		{
			SqlKit.execute(sp.getConnectionManager(),sql);
			player.setLoginLogTime(TimeKit.getSecondTime());
		}
		catch(Exception e)
		{
		}

	}

	public void query(int id,int timeStart,JSONArray array)
	{
		String sql="select * from loginlog where id="+id+" and loginTime>="
			+timeStart+" and loginTime<"+(timeStart+PublicConst.DAY_SEC*31);
		SqlPersistence sp=(SqlPersistence)dbaccess.getGamePersistence();
		try
		{
			Fields[] fields=SqlKit.querys(sp.getConnectionManager(),sql);
			if(fields!=null)
			{
				for(int i=0;i<fields.length;i++)
				{
					JSONObject json=new JSONObject();
					int loginTime=((IntField)fields[i].get("loginTime")).value;
					json.put("loginTime",
						SeaBackKit.formatDataTime(loginTime));
					json.put("deviceId",
						((StringField)fields[i].get("deviceId")).value);
					json.put("ip",((StringField)fields[i].get("ip")).value);
					json.put("pdid",((StringField)fields[i].get("pdid")).value);
					array.put(json);
				}
			}
		}
		catch(Exception e)
		{
		}

	}

	public void clear()
	{
		String sql="delete from loginlog where loginTime<"
			+(TimeKit.getSecondTime()-DAYS*PublicConst.DAY_SEC);
		SqlPersistence sp=(SqlPersistence)dbaccess.getGamePersistence();
		try
		{
			SqlKit.execute(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
		}
	}
	
	public void flushLoginLog(Player player,CreatObjectFactory objfactory)
	{
		if(SeaBackKit.isSameDay(player.getLoginLogTime(),
			TimeKit.getSecondTime())) return;
		User user=objfactory.getUserDBAccess().load(player.getUser_id()+"");
		save(player,user.getLoginUdid(),"flushLoginLog",false);

	}

	public GameDBAccess getDbaccess()
	{
		return dbaccess;
	}

	public void setDbaccess(GameDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}

}
