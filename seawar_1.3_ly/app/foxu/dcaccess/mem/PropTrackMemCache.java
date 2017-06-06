package foxu.dcaccess.mem;

import foxu.dcaccess.PropTrackDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.PropTrack;
import foxu.sea.uid.UidKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * 物品日志内存管理
 * 
 * @author Alan
 */
public class PropTrackMemCache extends LogMemCache implements TimerListener
{

	/** 1小时更新到数据库 */
	public static final int PROP_DATA_DB_TIME=60*10;
	/** 数据库保留时间一周 */
	public static final int DB_HOLD_TIME=60*60*24*7;
	/** 触发存储记录阈值 （条） */
	public static final int MAX=10000;
	/** 数据库定时器 */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** 还未保存的操作日志 */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** 数据加载器 */
	PropTrackDBAccess dbaccess;
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
	public void putTrack(PropTrack track)
	{
		int id=track.getPlayerId();
		collatePlayerTrack(id);
		ArrayList trackList=(ArrayList)list.get(id);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** 整理一个玩家的日志 **/
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
	
	/** 查找某个玩家的单一物品记录 */
	public PropTrack[] loadTracks(int playerId,int propSid,int startTime,
		int endTime)
	{
		String sql="WHERE playerId="+playerId
			+" AND propSid="+propSid+" AND createAt>="+startTime
			+" AND createAt<="+endTime+" ORDER BY createAt";
		return loadTracks(PropTrack.class,dbaccess,sql);
	}

	/** 查找某个玩家的全部物品记录 */
	public PropTrack[] loadTracks(int playerId,int startTime,int endTime)
	{
		String sql="WHERE playerId="+playerId
			+" AND createAt>="+startTime+" AND createAt<="+endTime
			+" ORDER BY createAt";
		return loadTracks(PropTrack.class,dbaccess,sql);
	}

	public void init()
	{
		// 启动定时器
		TimerCenter.getMinuteTimer().add(eventDB);
	}
	
	public PropTrack load(int trackId)
	{
		PropTrack track=(PropTrack)list.get(trackId);
		if(track==null)
		{
			track=dbaccess.load(trackId+"");
		}
		return track;
	}

	public void onTimer(TimerEvent e)
	{
		// 整理表
		SeaBackKit.collateLogTable(this,
			(SqlPersistence)dbaccess.getGamePersistence());
		int now=TimeKit.getSecondTime();
		if(now-saveTime>=PROP_DATA_DB_TIME||cmax>=MAX)
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
		if(now-clearTime>PublicConst.DAY_SEC)
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
	 * @return dbaccess
	 */
	public PropTrackDBAccess getDbaccess()
	{
		return dbaccess;
	}

	/**
	 * @param dbaccess 要设置的 dbaccess
	 */
	public void setDbaccess(PropTrackDBAccess dbaccess)
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

	public UidKit getUidkit()
	{
		return uidkit;
	}

	public void setUidkit(UidKit uidkit)
	{
		this.uidkit=uidkit;
	}

}
