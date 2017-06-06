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
 * ������־��¼
 * @author lihon
 *
 */
public class CoinsTrackMemCache extends LogMemCache implements TimerListener
{
	/** 1Сʱ���µ����ݿ� */
	public static final int COINS_DATA_DB_TIME=60*10;
	/** �����洢��¼��ֵ ������ */
	public static final int MAX=5000;
	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** ��δ����Ĳ�����־ */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** ���ݼ����� */
	CoinsTrackDBAccess dbaccess;
	/** UID�ṩ�� */
	UidKit uidkit;
	/** �ϴδ��ʱ�� */
	int saveTime;
	/** �ϴ�����ʱ�� */
	int clearTime;
	/** ��ǰ��¼���� */
	int cmax=0;

	/** �ط��洢��ǰ���� */
	public int saveAndExit()
	{
		saves(dbaccess,list);
		cmax=0;
		return 0;
	}

	/** ���һ����¼ */
	public void putTrack(CoinsTrack track)
	{
		int id=track.getPlayerId();
		collatePlayerTrack(id);
		ArrayList trackList=(ArrayList)list.get(id);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** ����һ����Ҿ�����־ **/
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

	/** ����ĳ����ҵľ����¼(ȫ��) */
	public CoinsTrack[] loadTracks(int playerId,int sTime,int eTime)
	{
		return loadTracks(playerId,sTime,eTime,"");
	}

	/** ����ĳ����ҵľ����¼(��������) */
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

	/** ����ĳ����ҵľ����¼ */
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
		// ������ʱ��
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public void onTimer(TimerEvent e)
	{
		// �����
		SeaBackKit.collateLogTable(this,
			(SqlPersistence)dbaccess.getGamePersistence());
		int now=TimeKit.getSecondTime();
		if(now-saveTime>=COINS_DATA_DB_TIME)
		{
			saveAndExit();
			saveTime=now;
		}
		// ���� 04:00
		clearMap(now);
	}
	/** ��������� */
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
	 * @param list Ҫ���õ� list
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
	 * @param uidkit Ҫ���õ� uidkit
	 */
	public void setUidkit(UidKit uidkit)
	{
		this.uidkit=uidkit;
	}



}
