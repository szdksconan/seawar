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

/** ��ʯ׷�� */
public class GemsTrackMemCache implements TimerListener
{

	/** 1Сʱ���µ����ݿ� */
	public static final int GEMS_DATA_DB_TIME=60*10;
	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** ��δ����Ĳ�����־ */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** ���ݼ����� */
	GemsTrackDBAccess dbaccess;
	/** UID�ṩ�� */
	UidKit uidkit;

	/** �ط��洢��ǰ���� */
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

	/** ���һ����¼ */
	public void putTrack(GemsTrack track)
	{
		synchronized(list)
		{
			list.put(track.getId(),track);
		}
	}

	/** ����ĳ����ҵı�ʯ���Ѽ�¼ */
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
		/**ɾ������һ���µ�����ʼ�*/
		String deleteSql="DELETE FROM gem_tracks WHERE createAt<"
			+checkTimeDelete;
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),deleteSql);
		// ������ʱ��
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
	 * @param uidkit Ҫ���õ� uidkit
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
	 * @param dbaccess Ҫ���õ� dbaccess
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
	 * @param list Ҫ���õ� list
	 */
	public void setList(IntKeyHashMap list)
	{
		this.list=list;
	}
}
