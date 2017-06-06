package foxu.dcaccess.mem;

import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.CoinsTrackDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.CoinsTrack;
import foxu.sea.uid.UidKit;

/**
 * 
 * 军魂日志记录
 * @author lihon
 *
 */
public class CoinsTrackMemCache extends LogMemCache implements TimerListener
{
	/** 1小时更新到数据库 */
	public static final int COINS_DATA_DB_TIME=60*10;
	/** 触发存储记录阈值 （条） */
	public static final int MAX=5000;
	/** 数据库定时器 */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** 还未保存的操作日志 */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** 数据加载器 */
	CoinsTrackDBAccess dbaccess;
	/** UID提供器 */
	UidKit uidkit;
	/** 上次存库时间 */
	int saveTime;
	/** 上次清理时间 */
	int clearTime;
	/** 当前记录数量 */
	int cmax=0;

	/** 关服存储当前数据 */
	public int saveAndExit()
	{
		saves(dbaccess,list);
		cmax=0;
		return 0;
	}

	/** 添加一条记录 */
	public void putTrack(CoinsTrack track)
	{
		int id=track.getPlayerId();
		collatePlayerTrack(id);
		ArrayList trackList=(ArrayList)list.get(id);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** 整理一个玩家军魂日志 **/
	private void collatePlayerTrack(int pid)
	{
		if(pid<=0) return;
		ArrayList trackList=(ArrayList)list.get(pid);
		if(trackList==null)
		{
			trackList=new ArrayList();
			list.put(pid,trackList);
		}
	}

	/** 查找某个玩家的军魂记录(全部) */
	public CoinsTrack[] loadTracks(int playerId,int sTime,int eTime)
	{
		return loadTracks(playerId,sTime,eTime,"");
	}

	/** 查找某个玩家的军魂记录(增减分离) */
	public CoinsTrack[] loadTracks(int playerId,int sTime,int eTime,
		int state)
	{
		return loadTracks(playerId,sTime,eTime," and state="+state);
	}

	public CoinsTrack[] loadTracks(int playerId,int sTime,int eTime,
		String typeSql)
	{
		String sql="where playerId="+playerId+" and createAt>="+sTime
			+" and createAt<="+eTime+typeSql;
		return loadTracks(CoinsTrack.class,dbaccess,sql);
	}

	/** 查找某个玩家的军魂记录 */
	public CoinsTrack[] loadTracks(int playerId,int year,int month,
		int day,int type)
	{
		String sql="WHERE playerId="+playerId+" AND year="+year
			+" AND month="+month;
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
		return loadTracks(CoinsTrack.class,dbaccess,sql);
	}

	public void init()
	{
		// 启动定时器
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public void onTimer(TimerEvent e)
	{
		// 整理表
		SeaBackKit.collateLogTable(this,
			(SqlPersistence)dbaccess.getGamePersistence());
		int now=TimeKit.getSecondTime();
		if(now-saveTime>=COINS_DATA_DB_TIME)
		{
			saveAndExit();
			saveTime=now;
		}
		// 清理 04:00
		clearMap(now);
	}
	/** 清理空数据 */
	public void clearMap(int now)
	{
		if(now-clearTime>PublicConst.DAY_SEC||cmax>=MAX)
		{
			int[] keys=list.keyArray();
			for(int i=0;i<keys.length;i++)
			{
				ArrayList trackList=(ArrayList)list.get(keys[i]);
				if(trackList==null||trackList.size()<=0)
					list.remove(keys[i]);
			}
			clearTime=SeaBackKit.getSomedayBegin(0)+4*3600;
		}
	}

	/**
	 * @return list
	 */
	public IntKeyHashMap getList()
	{
		return list;
	}

	/**
	 * @return dbaccess
	 */
	public CoinsTrackDBAccess getDbaccess()
	{
		return dbaccess;
	}

	public void setDbaccess(CoinsTrackDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}

	/**
	 * @param list 要设置的 list
	 */
	public void setList(IntKeyHashMap list)
	{
		this.list=list;
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



}
