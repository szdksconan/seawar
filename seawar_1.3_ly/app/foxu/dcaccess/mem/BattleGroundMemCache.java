package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import foxu.dcaccess.datasave.BattleGroundSave;
import foxu.sea.alliance.alliancefight.BattleGround;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;


public class BattleGroundMemCache extends MemCache
{
	/** 1小时修改过的数据更新到数据库 */
	public static final int BGROUND_DB_TIME=60*15;

	@Override
	public Object load(String key)
	{
		BattleGroundSave save=(BattleGroundSave)cacheMap.get(Integer.parseInt(key));
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
		int time=TimeKit.getSecondTime();
		BattleGroundSave save=new BattleGroundSave();
		save.setData((BattleGround)data);
		save.setSaveTimeDB(time);
		save.setSaveTimeRedis(time);
		// 内存中加入
		cacheMap.put(Integer.parseInt(key),save);
		// 改变列表里面加入
		changeListMap.put(Integer.parseInt(key),save);
		
	}

	@Override
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.BATTLE_GROUND);
		return changeListMap.size();
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				BGROUND_DB_TIME,JedisMemCacheAccess.BATTLE_GROUND);
		}
		
	}

	@Override
	public void init()
	{
		cacheMap=new IntKeyHashMap();
		changeListMap=new IntKeyHashMap();
		int time=TimeKit.getSecondTime();
		String sql="SELECT * FROM battleground";
		// 数据库加载所有玩家数据
		BattleGround battleground[]=(BattleGround[])dbaccess.loadBySql(sql);
//		System.out.println("----------BattleGround-------init00000-------------------");
		if(battleground!=null)
		{
//			System.out.println("---------BattleGround----------init-------------:::"+battleground.length);
			for(int i=0,n=battleground.length;i<n;i++)
			{
				BattleGroundSave save=new BattleGroundSave();
				save.setData(battleground[i]);
				// 设置保存的时间
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCache(Object save)
	{
		if(save==null) return;
		BattleGround bg=(BattleGround)save;
		cacheMap.remove(bg.getSid());
		changeListMap.remove(bg.getSid());
		
	}

}
