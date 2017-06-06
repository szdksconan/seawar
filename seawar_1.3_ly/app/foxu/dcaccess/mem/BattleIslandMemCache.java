package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.datasave.BattleIslandSave;
import foxu.sea.alliance.alliancebattle.BattleIsland;

public class BattleIslandMemCache extends MemCache
{

	/** 1Сʱ�޸Ĺ������ݸ��µ����ݿ� */
	public static final int BLSLAND_DB_TIME=60*15;

	@Override
	public Object load(String key)
	{
		BattleIslandSave save=(BattleIslandSave)cacheMap.get(Integer
			.parseInt(key));
		if(save==null) return null;
		return save.getData();
	}

	@Override
	public Object[] loads(String[] keys)
	{
		return null;
	}

	@Override
	public void save(String key,Object data)
	{
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		BattleIslandSave save=new BattleIslandSave();
		save.setData((BattleIsland)data);
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
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.BATTLE_ISLAND);
		return changeListMap.size();
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				BLSLAND_DB_TIME,JedisMemCacheAccess.BATTLE_ISLAND);
		}
	}
	
	/**�Ƿ�����׃�б�**/
	public Object load(int key,boolean bool)
	{
		synchronized(this)
		{
			BattleIslandSave data=(BattleIslandSave)cacheMap.get(key);
			if(data!=null)
			{
				if(bool && changeListMap.get(key)==null)
				{
					data.setSaveTimeDB(TimeKit.getSecondTime());
					changeListMap.put(key,data);
				}
				return data.getData();
			}
		}
		return null;
	}

	/**�Ƿ�����׃�б�**/
	public Object[] loadBattleIslands(boolean bool)
	{
		synchronized(this)
		{
			if(bool)
			{
				Object[] objects=cacheMap.valueArray();
				for(int i=0;i<objects.length;i++)
				{
					BattleIslandSave save=(BattleIslandSave)objects[i];
					save.setSaveTimeDB(TimeKit.getSecondTime());
					changeListMap.put(save.getId(),save);
				}
			}
		}
		if(cacheMap.size()==0) return null;
		return cacheMap.valueArray();
	}
	
	@Override
	public void init()
	{
		cacheMap=new IntKeyHashMap();
		changeListMap=new IntKeyHashMap();
		int time=TimeKit.getSecondTime();
		String sql="SELECT * FROM battleisland";
		BattleIsland battleisland[]=(BattleIsland[])dbaccess.loadBySql(sql);
		if(battleisland!=null)
		{
			for(int i=0,n=battleisland.length;i<n;i++)
			{
				BattleIslandSave save=new BattleIslandSave();
				save.setData(battleisland[i]);
				// ���ñ����ʱ��
				save.setSaveTimeDB(time);
				save.setSaveTimeRedis(time);
				cacheMap.put(save.getId(),save);
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
		BattleIslandSave bg=(BattleIslandSave)save;
		cacheMap.remove(bg.getId());
		changeListMap.remove(bg.getId());
	}

}
