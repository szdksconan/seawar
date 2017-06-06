package foxu.dcaccess.mem;

import foxu.dcaccess.ShipDataDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.shipdata.ShipCheckData;
import foxu.sea.uid.UidKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/** 宝石追踪 */
public class ShipDataMemCaChe extends LogMemCache implements TimerListener
{

	/** 每15分钟更新到数据库 */
	public static final int GEMS_DATA_DB_TIME=60*15;
	/** 空数组 */
	public static final ShipCheckData[] NULL_DATA={};
	/** 触发存储记录阈值 （条） */
	public static final int MAX=5000;

	/** 数据库定时器 */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** 还未保存的操作日志 */
	IntKeyHashMap list=new IntKeyHashMap();
	/** 数据加载器 */
	ShipDataDBAccess dbaccess;
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
//		saves(dbaccess,list);
		int[] keys=list.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			ArrayList trackList=(ArrayList)list.get(keys[i]);
			if(trackList==null||trackList.size()<=0) continue;
			Object[] objs=trackList.getArray();
			for(int m=0;m<objs.length;m++)
			{
				if(objs[m]==null) continue;
				dbaccess.save(objs[m]);
			}
			trackList.clear();
		}
		cmax=0;
		return 0;
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

	/** 获取一个玩家的日志 **/
	public ShipCheckData[] getPlayerDatas(int pid,int stime,String type)
	{
		ArrayList trackList=(ArrayList)list.get(pid);
		ShipCheckData[] slqdatas=loadTracks(pid,stime,type);
		if((trackList==null||trackList.size()==0)
			&&(slqdatas==null||slqdatas.length<=0)) return NULL_DATA;
		ArrayList list=getShipDataByType(TextKit.parseInt(type),trackList);
		int len=trackList==null?0:list.size();
		int len1=slqdatas==null?0:slqdatas.length;
		ShipCheckData[] data=new ShipCheckData[len+len1];
		for(int i=0;i<len1;i++)
		{
			data[i]=slqdatas[i];
		}
		if(len>0)
		{
			Object[] objs=list.toArray();
			for(int i=len1;i<len+len1;i++)
			{
				data[i]=(ShipCheckData)objs[i-len1];
			}
		}
		return data;
	}

	/**根据类型获取长度**/
	public ArrayList getShipDataByType(int type,ArrayList trackList)
	{
		if(trackList==null) return null;
		if(type==-1) return trackList;
		ArrayList list=new ArrayList();
		Object[] objs=trackList.toArray();
		for(int i=0;i<objs.length;i++)
		{
			ShipCheckData data=(ShipCheckData)objs[i];
			if(data.getType()==type) list.add(data);
		}
		return list;
	}
	
	/** 添加一条记录 */
	public void putTrack(ShipCheckData track)
	{
		int id=track.getPlayerId();
		collatePlayerTrack(id);
		ArrayList trackList=(ArrayList)list.get(id);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** 查找某个玩家的宝石消费记录 */
	public ShipCheckData[] loadTracks(int playerId,int stime,String type)
	{
		String csql=" WHERE playerId="+playerId+" AND createAt>="+stime;
		if(type!=null&&TextKit.parseInt(type)!=-1)
			csql+=" AND TYPE="+TextKit.parseInt(type);
		return loadTracks(ShipCheckData.class,dbaccess,csql);
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
		if(now-saveTime>=GEMS_DATA_DB_TIME||cmax>=MAX)
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
	public ShipDataDBAccess getDbaccess()
	{
		return dbaccess;
	}

	/**
	 * @param dbaccess 要设置的 dbaccess
	 */
	public void setDbaccess(ShipDataDBAccess dbaccess)
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

	public int getSaveTime()
	{
		return saveTime;
	}

	public void setSaveTime(int saveTime)
	{
		this.saveTime=saveTime;
	}

}
