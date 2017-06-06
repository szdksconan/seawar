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

/** ��ʯ׷�� */
public class ShipDataMemCaChe extends LogMemCache implements TimerListener
{

	/** ÿ15���Ӹ��µ����ݿ� */
	public static final int GEMS_DATA_DB_TIME=60*15;
	/** ������ */
	public static final ShipCheckData[] NULL_DATA={};
	/** �����洢��¼��ֵ ������ */
	public static final int MAX=5000;

	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** ��δ����Ĳ�����־ */
	IntKeyHashMap list=new IntKeyHashMap();
	/** ���ݼ����� */
	ShipDataDBAccess dbaccess;
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

	/** ��ȡһ����ҵ���־ **/
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

	/**�������ͻ�ȡ����**/
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
	
	/** ���һ����¼ */
	public void putTrack(ShipCheckData track)
	{
		int id=track.getPlayerId();
		collatePlayerTrack(id);
		ArrayList trackList=(ArrayList)list.get(id);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** ����ĳ����ҵı�ʯ���Ѽ�¼ */
	public ShipCheckData[] loadTracks(int playerId,int stime,String type)
	{
		String csql=" WHERE playerId="+playerId+" AND createAt>="+stime;
		if(type!=null&&TextKit.parseInt(type)!=-1)
			csql+=" AND TYPE="+TextKit.parseInt(type);
		return loadTracks(ShipCheckData.class,dbaccess,csql);
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
		if(now-saveTime>=GEMS_DATA_DB_TIME||cmax>=MAX)
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
	public ShipDataDBAccess getDbaccess()
	{
		return dbaccess;
	}

	/**
	 * @param dbaccess Ҫ���õ� dbaccess
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
	 * @param list Ҫ���õ� list
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
