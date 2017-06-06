package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import foxu.dcaccess.datasave.AllianceFightSave;
import foxu.sea.alliance.alliancefight.AllianceFight;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;


public class AllianceFightMemCache extends MemCache
{
	/** 1Сʱ�޸Ĺ������ݸ��µ����ݿ� */
	public static final int ALLIANFIGHT_DB_TIME=60*15;
	@Override
	public Object load(String key)
	{
		AllianceFightSave save=(AllianceFightSave)cacheMap.get(Integer.parseInt(key));
		if(save==null)return null;
		return save.getData();
	}

	@Override
	public Object[] loads(String[] arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(String key,Object data)
	{
		if(data==null) return;
//		System.out.println("------------save-------afight-----");
		int time=TimeKit.getSecondTime();
		AllianceFightSave save=new AllianceFightSave();
		save.setData((AllianceFight)data);
		save.setSaveTimeDB(time);
		save.setSaveTimeRedis(time);
		// �ڴ��м���
		cacheMap.put(Integer.parseInt(key),save);
		// �ı��б��������
		changeListMap.put(Integer.parseInt(key),save);
		
	}

	@Override
	public int saveAndExit()
	{
//		System.out.println("--------saveAndExit-------:"+changeListMap.size());
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.ALLIANCE_FIGHT);
		return changeListMap.size();
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				ALLIANFIGHT_DB_TIME,JedisMemCacheAccess.ALLIANCE_FIGHT);
		}
		
	}

	@Override
	public void init()
	{
//		System.out.println("------allianceFight----init-----");
		cacheMap=new IntKeyHashMap();
		changeListMap=new IntKeyHashMap();
		int time=TimeKit.getSecondTime();
		String sql="SELECT * FROM alliancefight";
		// ���ݿ���������������
		AllianceFight allianceFight[]=(AllianceFight[])dbaccess.loadBySql(sql);
		if(allianceFight!=null)
		{
//			System.out.println("----------allianceFight-----------:"+allianceFight.length);
			for(int i=0,n=allianceFight.length;i<n;i++)
			{
				AllianceFightSave fightSave=new AllianceFightSave();
				fightSave.setData(allianceFight[i]);
				// ���ñ����ʱ��
				fightSave.setSaveTimeDB(time);
				fightSave.setSaveTimeRedis(time);
				cacheMap.put(fightSave.getId(),fightSave);
			}
		}
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	@Override
	public Object createObect()
	{
		return null;
	}

	@Override
	public void deleteCache(Object save)
	{
		if(save==null) return;
		AllianceFight al=(AllianceFight)save;
		cacheMap.remove(al.getAllianceID());
		changeListMap.remove(al.getAllianceID());
		
	}
	
}
