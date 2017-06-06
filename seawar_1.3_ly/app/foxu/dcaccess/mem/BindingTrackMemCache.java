package foxu.dcaccess.mem;

import foxu.dcaccess.BindingTrackDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.bind.BindingTrack;
import foxu.sea.kit.SeaBackKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * ����־�ڴ����
 * 
 * @author Alan
 */
public class BindingTrackMemCache extends LogMemCache implements
	TimerListener
{

	/** 1Сʱ���µ����ݿ� */
	public static final int Binding_DATA_DB_TIME=60*10;
	/** �����洢��¼��ֵ ������ */
	public static final int MAX=5000;
	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** ��δ����Ĳ�����־ */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** ���ݼ����� */
	BindingTrackDBAccess dbaccess;
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
	public void putTrack(BindingTrack track)
	{
		int id=track.getPid();
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

	/** ����ĳ����ҵ�װ����¼(ȫ��) */
	public BindingTrack[] loadTracks(int uid,int playerId,int sTime,int eTime)
	{
		return loadTracks(BindingTrack.ALL,BindingTrack.ALL,
			BindingTrack.ALL,uid,playerId,sTime,eTime);
	}

	public BindingTrack[] loadTracks(int bindType,int trackType,
		int actionType,int uid,int playerId,int sTime,int eTime)
	{
		String sql="";
		if(bindType!=BindingTrack.ALL) sql+=" and bindType="+bindType;
		if(trackType!=BindingTrack.ALL) sql+=" and trackType="+trackType;
		if(actionType!=BindingTrack.ALL) sql+=" and actionType="+actionType;
		if(uid!=BindingTrack.ALL) sql+=" and uid="+uid;
		if(playerId!=BindingTrack.ALL) sql+=" and pid="+playerId;
		sql="where time>="+sTime+" and time<="+eTime+sql;
		return loadTracks(BindingTrack.class,dbaccess,sql);

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
		if(now-saveTime>=Binding_DATA_DB_TIME)
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
	 * @return dbaccess
	 */
	public BindingTrackDBAccess getDbaccess()
	{
		return dbaccess;
	}

	/**
	 * @param dbaccess Ҫ���õ� dbaccess
	 */
	public void setDbaccess(BindingTrackDBAccess dbaccess)
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

}
