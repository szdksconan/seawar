package foxu.dcaccess.mem;

import foxu.dcaccess.GemsTrackDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.uid.UidKit;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/** 宝石追踪 */
public class GemsTrackMemCache implements TimerListener
{

	/** 1小时更新到数据库 */
	public static final int GEMS_DATA_DB_TIME=60*10;
	/** 数据库定时器 */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** 还未保存的操作日志 */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** 数据加载器 */
	GemsTrackDBAccess dbaccess;
	/** UID提供器 */
	UidKit uidkit;

	/** 关服存储当前数据 */
	public int saveAndExit()
	{
		Object[] object=list.valueArray();
		for(int i=0;i<object.length;i++)
		{
			GemsTrack track=(GemsTrack)object[i];
			dbaccess.save(track);
			list.remove(track.getId());
		}
		return list.size();
	}

	/** 添加一条记录 */
	public void putTrack(GemsTrack track)
	{
		synchronized(list)
		{
			list.put(track.getId(),track);
		}
	}

	/** 查找某个玩家的宝石消费记录 */
	public GemsTrack[] loadTracks(int playerId,int year,int month,int day,int type)
	{
		String sql="SELECT * FROM gem_tracks WHERE playerId="+playerId
			+" AND year="+year+" AND month="+month;
		String dayStr="";
		String typeStr="";
		if(day!=0)
		{
			dayStr=" AND day="+day;
		}
		if(type!=PublicConst.LOAD_ALL_GEM_COST)
		{
			typeStr=" AND type="+type;
		}
		sql=sql+dayStr+typeStr;
		return dbaccess.loadBySql(sql);
	}

	public void init()
	{
		int checkTimeDelete=TimeKit.getSecondTime()-60*60*24*30;
		/**删除超过一个月的玩家邮件*/
		String deleteSql="DELETE FROM gem_tracks WHERE createAt<"
			+checkTimeDelete;
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),deleteSql);
		// 启动定时器
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public void onTimer(TimerEvent e)
	{
		synchronized(list)
		{
			Object[] object=list.valueArray();
			for(int i=0;i<object.length;i++)
			{
				GemsTrack track=(GemsTrack)object[i];
				if((track.getCreateAt()+GEMS_DATA_DB_TIME)>TimeKit
					.getSecondTime()) continue;
				dbaccess.save(track);
				list.remove(track.getId());
			}
		}
	}

	/**
	 * @return uidkit
	 */
	public UidKit getUidkit()
	{
		return uidkit;
	}

	/**
	 * @param uidkit 要设置的 uidkit
	 */
	public void setUidkit(UidKit uidkit)
	{
		this.uidkit=uidkit;
	}

	/**
	 * @return dbaccess
	 */
	public GemsTrackDBAccess getDbaccess()
	{
		return dbaccess;
	}

	/**
	 * @param dbaccess 要设置的 dbaccess
	 */
	public void setDbaccess(GemsTrackDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}

	/**
	 * @return list
	 */
	public IntKeyHashMap getList()
	{
		return list;
	}

	/**
	 * @param list 要设置的 list
	 */
	public void setList(IntKeyHashMap list)
	{
		this.list=list;
	}
}
