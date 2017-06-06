package foxu.dcaccess.mem;

import foxu.dcaccess.ProducePropTrackDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.builds.produce.ProducePropTrack;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.uid.UidKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * ��Ʒ������־�ڴ����
 * 
 * @author Alan
 */
public class ProducePropTrackMemCache extends LogMemCache implements
	TimerListener
{

	/** 1Сʱ���µ����ݿ� */
	public static final int PRODUCEPROP_DATA_DB_TIME=60*10;
	/** �����洢��¼��ֵ ������ */
	public static final int MAX=10000;
	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** ��δ����Ĳ�����־ */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** ���ݼ����� */
	ProducePropTrackDBAccess dbaccess;
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
	public void putTrack(ProducePropTrack track)
	{
		int id=track.getPlayerId();
		collatePlayerTrack(id);
		ArrayList trackList=(ArrayList)list.get(id);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** ����һ����ҵ���־ **/
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

	/** ����ĳ����ҵ���Ʒ������¼ */
	public ProducePropTrack[] loadTracks(int playerId,int buildSid,
		int startTime,int endTime)
	{
		String sql="WHERE playerId="+playerId+" AND buildSid="+buildSid
			+" AND createAt>="+startTime+" AND createAt<="+endTime
			+" ORDER BY createAt";
		return loadTracks(ProducePropTrack.class,dbaccess,sql);
	}

	public void init()
	{
		// ������ʱ��
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public ProducePropTrack load(int trackId)
	{
		ProducePropTrack track=(ProducePropTrack)list.get(trackId);
		if(track==null)
		{
			track=dbaccess.load(trackId+"");
		}
		return track;
	}

	public void onTimer(TimerEvent e)
	{
		// �����
		SeaBackKit.collateLogTable(this,
			(SqlPersistence)dbaccess.getGamePersistence());
		int now=TimeKit.getSecondTime();
		if(now-saveTime>=PRODUCEPROP_DATA_DB_TIME||cmax>=MAX)
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
	public ProducePropTrackDBAccess getDbaccess()
	{
		return dbaccess;
	}

	/**
	 * @param dbaccess Ҫ���õ� dbaccess
	 */
	public void setDbaccess(ProducePropTrackDBAccess dbaccess)
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
	 * @param list Ҫ���õ� list
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
