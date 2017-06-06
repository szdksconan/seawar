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
 * �Ƽ��������
 * @author lhj
 *
 */
public class SciencePointTrackMemCache extends LogMemCache implements TimerListener
{

	/** 1Сʱ���µ����ݿ� */
	public static final int SCIENCE_POINT_DATA_DB_TIME=60*10;
	/** �����洢��¼��ֵ ������ */
	public static final int MAX=5000;
	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** ��δ����Ĳ�����־ */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** ���ݼ����� */
	SciencePointTrackDBAccess dbaccess;
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
	public void putTrack(SciencePointTrack track)
	{
		int aid=track.getAllianceId();
		collateAllianceTrack(aid);
		ArrayList trackList=(ArrayList)list.get(aid);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** ����һ��������־ **/
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

	/** ����ĳ�����˿Ƽ����¼(ȫ��) */
	public SciencePointTrack[] loadTracks(int aid,int sTime,int eTime,int style)
	{
		return loadTracks(aid,sTime,eTime,"  and style="+style);
	}

	/** ����ĳ�����˵ĿƼ����¼(��������) */
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

//	/** ����ĳ����ҵĿƼ����¼ */
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
		// ������ʱ��
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public void onTimer(TimerEvent e)
	{
		// �����
		SeaBackKit.collateLogTable(this,
			(SqlPersistence)dbaccess.getGamePersistence());
		int now=TimeKit.getSecondTime();
		if(now-saveTime>=SCIENCE_POINT_DATA_DB_TIME)
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

	
	public SciencePointTrackDBAccess getDbaccess()
	{
		return dbaccess;
	}

	
	public void setDbaccess(SciencePointTrackDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}
	
}
