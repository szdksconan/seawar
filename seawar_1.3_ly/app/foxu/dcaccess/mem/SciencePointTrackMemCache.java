package foxu.dcaccess.mem;

import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.SciencePointTrackDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.alliance.alliancebattle.SciencePointTrack;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.uid.UidKit;


/**
 * 科技点管理器
 * @author lhj
 *
 */
public class SciencePointTrackMemCache extends LogMemCache implements TimerListener
{

	/** 1小时更新到数据库 */
	public static final int SCIENCE_POINT_DATA_DB_TIME=60*10;
	/** 触发存储记录阈值 （条） */
	public static final int MAX=5000;
	/** 数据库定时器 */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** 还未保存的操作日志 */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** 数据加载器 */
	SciencePointTrackDBAccess dbaccess;
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
	public void putTrack(SciencePointTrack track)
	{
		int aid=track.getAllianceId();
		collateAllianceTrack(aid);
		ArrayList trackList=(ArrayList)list.get(aid);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** 整理一个联盟日志 **/
	private void collateAllianceTrack(int aid)
	{
		if(aid<=0) return;
		ArrayList trackList=(ArrayList)list.get(aid);
		if(trackList==null)
		{
			trackList=new ArrayList();
			list.put(aid,trackList);
		}
	}

	/** 查找某个联盟科技点记录(全部) */
	public SciencePointTrack[] loadTracks(int aid,int sTime,int eTime,int style)
	{
		return loadTracks(aid,sTime,eTime,"  and style="+style);
	}

	/** 查找某个联盟的科技点记录(增减分离) */
	public SciencePointTrack[] loadTracks(int aid,int sTime,int eTime,
		int state,int style)
	{
		return loadTracks(aid,sTime,eTime," and state="+state+" and style="+style);
	}

	public SciencePointTrack[] loadTracks(int aid,int sTime,int eTime,
		String typeSql)
	{
		String sql="where allianceId="+aid+" and createAt>="+sTime
			+" and createAt<="+eTime+typeSql;
		return loadTracks(SciencePointTrack.class,dbaccess,sql);
	}

//	/** 查找某个玩家的科技点记录 */
//	public SciencePointTrack[] loadTracks(int aid,int year,int month,
//		int day,int type)
//	{
//		String sql="WHERE allianceId="+aid+" AND year="+year
//			+" AND month="+month;
//		String dayStr="";
//		String typeStr="";
//		if(day!=0)
//		{
//			dayStr=" AND day="+day;
//		}
//		if(type!=PublicConst.LOAD_ALL_GEM_COST)
//		{
//			typeStr=" AND type="+type;
//		}
//		sql=sql+dayStr+typeStr;
//		return loadTracks(SciencePointTrack.class,dbaccess,sql);
//	}

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
		if(now-saveTime>=SCIENCE_POINT_DATA_DB_TIME)
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

	
	public SciencePointTrackDBAccess getDbaccess()
	{
		return dbaccess;
	}

	
	public void setDbaccess(SciencePointTrackDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}
	
}
