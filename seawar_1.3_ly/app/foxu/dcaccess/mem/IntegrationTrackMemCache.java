package foxu.dcaccess.mem;

import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.IntegrationTrackDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.alliance.alliancebattle.IntegrationTrack;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.uid.UidKit;

/****
 * 
 * �����ڴ����
 * 
 * @author lhj
 * 
 */
public class IntegrationTrackMemCache extends LogMemCache implements
	TimerListener
{

	/** 1Сʱ���µ����ݿ� */
	public static final int INTEGRATION_DATA_DB_TIME=60*10;
	/** �����洢��¼��ֵ ������ */
	public static final int MAX=5000;
	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** ��δ����Ĳ�����־ */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** ���ݼ����� */
	IntegrationTrackDBAccess dbaccess;
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
	public void putTrack(IntegrationTrack track)
	{
		int id=track.getPlayerId();
		collatePlayerTrack(id);
		ArrayList trackList=(ArrayList)list.get(id);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** ����һ����һ�����־ **/
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

	/** ����ĳ����ҵĻ��ּ�¼(ȫ��) */
	public IntegrationTrack[] loadTracks(int playerId,int sTime,int eTime)
	{
		return loadTracks(playerId,sTime,eTime,"");
	}

	/** ����ĳ����ҵĻ��ּ�¼(��������) */
	public IntegrationTrack[] loadTracks(int playerId,int sTime,int eTime,
		int state)
	{
		return loadTracks(playerId,sTime,eTime," and state="+state);
	}

	public IntegrationTrack[] loadTracks(int playerId,int sTime,int eTime,
		String typeSql)
	{
		String sql="where playerId="+playerId+" and createAt>="+sTime
			+" and createAt<="+eTime+typeSql;
		return loadTracks(IntegrationTrack.class,dbaccess,sql);
	}

	/** ����ĳ����ҵĻ��ּ�¼ */
	public IntegrationTrack[] loadTracks(int playerId,int year,int month,
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
		return loadTracks(IntegrationTrack.class,dbaccess,sql);
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
		if(now-saveTime>=INTEGRATION_DATA_DB_TIME)
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
	public IntegrationTrackDBAccess getDbaccess()
	{
		return dbaccess;
	}

	public void setDbaccess(IntegrationTrackDBAccess dbaccess)
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
