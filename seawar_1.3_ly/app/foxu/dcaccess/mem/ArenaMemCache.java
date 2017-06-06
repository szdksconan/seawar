package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.ArenaSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.arena.SeawarGladiator;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;

/**
 * 竞技场数据缓存
 * 
 * @author comeback
 * 
 */
public class ArenaMemCache extends MemCache
{
	private static Logger log=LogFactory.getLogger(ArenaMemCache.class);
	CreatObjectFactory objectFactory;
	/** 1小时修改过的数据更新到数据库 */
	public static final int ARENA_DB_TIME=60*15,ARENA_REDIS_TIME=60*5;
	
	public synchronized SeawarGladiator load(String key)
	{
		if(key==null||key.equals(""))return null;
		ArenaSave data=(ArenaSave)cacheMap.get(Integer.parseInt(key));
		if(data!=null)
		{
			if(changeListMap.get(Integer.parseInt(key))==null)
			{
				changeListMap.put(Integer.parseInt(key),data);
			}
			return (SeawarGladiator)data.getData();
		}
		return null;
	}
	
	public synchronized SeawarGladiator loadOnly(String key)
	{
		if(key==null||key.equals(""))return null;
		ArenaSave data=(ArenaSave)cacheMap.get(Integer.parseInt(key));
		if(data!=null)
		{
			return (SeawarGladiator)data.getData();
		}
		return null;
	}

	public Object[] loads(String[] keys)
	{
		return null;
	}

	public synchronized void save(String key,Object data)
	{
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		ArenaSave save=new ArenaSave();
		save.setData(data);
		save.setSaveTimeDB(time);
		save.setSaveTimeRedis(time);
		// 内存中加入
		cacheMap.put(Integer.parseInt(key),save);
		// 改变列表里面加入
		changeListMap.put(Integer.parseInt(key),save);
	}

	public synchronized int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.ARENA_REDIS);
		return changeListMap.size();
	}

	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				ARENA_DB_TIME,JedisMemCacheAccess.ARENA_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
//					collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
//						ARENA_REDIS_TIME,JedisMemCacheAccess.ALLIANCE_REDIS);
		}
	}

	public void init()
	{
		cacheMap=new IntKeyHashMap(PlayerMemCache.ALL_PLAYER_SIZE/5);
		changeListMap=new IntKeyHashMap(PlayerMemCache.CHANGE_PLAYER*20);
		int time=TimeKit.getSecondTime();
		String sql="select * from arena";
		
		SeawarGladiator[] datas=(SeawarGladiator[])dbaccess.loadBySql(sql);
		if(datas!=null)
		{
			for(int i=0;i<datas.length;i++)
			{
				ArenaSave arenaSave=new ArenaSave();
				arenaSave.setData(datas[i]);
				arenaSave.setSaveTimeDB(time);
				arenaSave.setSaveTimeRedis(time);
				SeawarGladiator sg = datas[i];
				cacheMap.put(sg.getPlayerId(),arenaSave);
				//第一次更新 加载军团名次成就
				Player p = objectFactory.getPlayerById(sg.getPlayerId());
				if(p!=null){
				if(!PublicConst.HEAD_SIGN.equals(p.getAttributes(PublicConst.HEAD_TO_ACHIEVEMENT))){//特殊处理一些 还未出现的成就
					AchieveCollect.arenaRank(sg.getRanking(),p);
				}
				}
			}
		}
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public synchronized SeawarGladiator createObect(int playerId)
	{
		SeawarGladiator gladiator=new SeawarGladiator();
		gladiator.setPlayerId(playerId);
		gladiator.setRanking(cacheMap.size()+1);
		ArenaSave save=new ArenaSave();
		save.setData(gladiator);
		save.setSaveTimeDB(TimeKit.getSecondTime());
		
		//第一次更新 加载环球军演名次成就
		Player p = objectFactory.getPlayerById(gladiator.getPlayerId());
		if(p!=null)
			AchieveCollect.arenaRank(gladiator.getRanking(),p);
		
		cacheMap.put(playerId,save);
		
		changeListMap.put(playerId,save);
		
		return gladiator;
	}

	public synchronized void deleteCache(Object save)
	{
		if(save==null) return;
		SeawarGladiator sg=(SeawarGladiator)save;
		cacheMap.remove(sg.getPlayerId());
		changeListMap.remove(sg.getPlayerId());
	}

	public Object createObect()
	{
		throw new RuntimeException("must call createObject(int)");
	}

	
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
}
