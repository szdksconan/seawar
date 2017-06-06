package foxu.dcaccess.mem;

import mustang.field.Fields;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.AllianceFightRecordDBAccess;
import foxu.sea.PublicConst;
import foxu.sea.alliance.alliancebattle.AllianceFightRecordTrack;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.uid.UidKit;

/***
 * ����ս�ļ�¼��
 * 
 * @author lhj
 * 
 */
public class AllianceFightRecordMemCache extends LogMemCache implements TimerListener
{

	/** 1Сʱ���µ����ݿ� */
	public static final int FIGHT_RECORD_DATA_DB_TIME=60*10;
	/**FIND_ALL=0 ȫ����ѯ  FIND_BY_ALLIANCE=1 ���˲�ѯ
	 * FINAD_BY_PLAYER=2 ��Ҳ�ѯ
	 * **/
	public static final int FIND_ALL=0,FIND_BY_ALLIANCE=1,FINAD_BY_PLAYER=2;
	/** �����洢��¼��ֵ ������ */
	public static final int MAX=5000;
	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** ��δ����Ĳ�����־ */
	IntKeyHashMap list=new IntKeyHashMap(30000);
	/** ���ݼ����� */
	AllianceFightRecordDBAccess dbaccess;
	/** UID�ṩ�� */
	UidKit uidkit;
	/** �ϴδ��ʱ�� */
	int saveTime;
	/** �ϴ�����ʱ�� */
	int clearTime;
	/** ��ǰ��¼���� */
	int cmax=0;
	/**��ǰ�ļ�¼�ı�ʶ**/
	int state=0;
	/**ʱ��**/
	int  time;
	/** �ط��洢��ǰ���� */
	public int saveAndExit()
	{
		saves(dbaccess,list);
		cmax=0;
		return 0;
	}

	/** ���һ����¼ */
	public void putTrack(AllianceFightRecordTrack track)
	{
		int aid=track.getBattleIsland();
		collateAllianceFightRecordTrack(aid);
		ArrayList trackList=(ArrayList)list.get(aid);
		if(trackList!=null) trackList.add(track);
		cmax++;
	}

	/** ����һ��������־ **/
	private void collateAllianceFightRecordTrack(int islandSid)
	{
		ArrayList trackList=(ArrayList)list.get(islandSid);
		if(trackList==null)
		{
			trackList=new ArrayList();
			list.put(islandSid,trackList);
		}
	}

	/** ����ĳ�����˼�¼(ȫ��) */
	public AllianceFightRecordTrack[] loadTracks(int sTime,int eTime,int type,String typeSql)
	{
		if(type==FIND_ALL) return loadTracks(sTime,eTime,typeSql);
		return null;
	}
	/** ����ȫ����¼*/
	public AllianceFightRecordTrack[] loadTracks(int sTime,int eTime,
		String typeSql)
	{
		String sql="where  createAt>="+sTime+" and createAt<="+eTime+typeSql;
		String sql1="SELECT * FROM a_fight_record_track "+sql;
		return dbaccess.loadBySql(sql1);
	}
	
	/** ���ձ�ʶ��ѯ*/
	public AllianceFightRecordTrack[] loadTracks(int state,String typeSql)
	{
		String sql="where  state="+state+typeSql;
		String sql1="SELECT * FROM a_fight_record_track "+sql;
		return dbaccess.loadBySql(sql1);
	}
	
	public void init()
	{
		createTable();
		int timeNow=TimeKit.getSecondTime();
		String deleteSql ="delete  from a_fight_record_track  where createAt<="+(timeNow-2*30*60*60*24);
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),deleteSql);
		// ������ʱ��
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public void onTimer(TimerEvent e)
	{
		int now=TimeKit.getSecondTime();
		if(now-saveTime>=FIGHT_RECORD_DATA_DB_TIME)
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
	
	public void createTable()
	{
		String tableName="a_fight_record_track";
		SqlPersistence sp=(SqlPersistence)dbaccess.getGamePersistence();
		String sql="show TABLES like ";
		Fields fields=null;
		fields=SqlKit.query(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),sql+"'"+tableName+"'");
		if(fields==null)
		{
			sql="CREATE TABLE `"+tableName+"` "+this.getCreateSql();
			SqlKit.execute(sp.getConnectionManager(),sql);
			this.setTableTime(TimeKit.getSecondTime());
		}
		sp.setTable(tableName);
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

	
	public void setDbaccess(AllianceFightRecordDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}
}
