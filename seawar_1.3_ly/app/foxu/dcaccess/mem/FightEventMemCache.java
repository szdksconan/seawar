package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.event.ChangeAdapter;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.datasave.FightEventSave;
import foxu.sea.Player;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

/**
 * 内存管理 每1个小时对修改的数据保存进数据库 并把redis相同id的数据删除 每5分钟 同步数据到redis 服务器启动先从数据库加载所有数据
 * 然后从redis取相同id的数据覆盖 数据库的数据 author:icetiger
 */
public class FightEventMemCache extends MemCache
{
	/** 默认改变列表和数据表大小 */
	public static final int ALL_EVENT_SIZE=2000,CHANGE_EVENT=1000;
	/** 1小时修改过的数据更新到数据库 */
	public static final int EVENT_DB_TIME=60*10,EVENT_REDIS_TIME=60*5;
	/** 以岛屿ID为key 里面存储战斗事件集合 */
	IntKeyHashMap eventMap=new IntKeyHashMap();

	PlayerMemCache playerMemCache;

	/** 关服存储当前数据 */
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.FIGHT_EVENT_REDIS);
		return changeListMap.size();
	}

	/** 将一对多的关系组合起来 */
	public void setFightEvent()
	{
		Object[] object=cacheMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			FightEventSave save=(FightEventSave)object[i];
			FightEvent event=save.getData();
			if(event.getSourceIslandIndex()>=0)
				addFightEvent(event,event.getSourceIslandIndex());
			if(event.getAttackIslandIndex()>=0)
				addFightEvent(event,event.getAttackIslandIndex());
		}
	}

	/** 移除驻守岛屿的被攻击事件 */
	public synchronized void removeHoldOnEvent(int islandIndex,
		int returnIndex,Player player)
	{
		if(eventMap.get(islandIndex)==null) return;
		ArrayList list=((FightEventList)eventMap.get(islandIndex))
			.getFightEventList();
		Object object[]=list.toArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			FightEvent ev=(FightEvent)object[i];
			if(ev.getSourceIslandIndex()!=islandIndex
				&&ev.getAttackIslandIndex()!=islandIndex
				&&ev.getAttackIslandIndex()==returnIndex)
			{
				list.remove(ev);
				JBackKit.deleteFightEvent(player,ev);
			}
		}
	}

	/** 获取某个玩家event列表 */
	public synchronized ArrayList getFightEventListById(int islandId)
	{
		if(eventMap.get(islandId)==null) return null;
		return ((FightEventList)eventMap.get(islandId)).getFightEventList();
	}

	/** 往一对多map里面添加event */
	public synchronized void addFightEvent(FightEvent event,int sourceId)
	{
		if(event.getDelete()==FightEvent.DELETE_TYPE)
		{
			// 加入改变列表 等待删除
			load(event.getId()+"");
			return;
		}
		if(event.getFleetGroup().nowTotalNum()<=0)
		{
			event.setDelete(FightEvent.DELETE_TYPE);
			// 加入改变列表 等待删除
			load(event.getId()+"");
			return;
		}
		FightEventList list=(FightEventList)eventMap.get(sourceId);
		// 有了就添加
		if(list!=null)
		{
			list.addFightEvent(event);
			return;
		}
		// 还没有就个进去
		list=new FightEventList();
		list.addFightEvent(event);
		eventMap.put(sourceId,list);
	}
	/**
	 * 创建完对象返回进行加工后 要记得同步到一对多的map里面去 保持数据同步
	 */
	public FightEvent createObect()
	{
		// TODO 自动生成方法存根
		FightEvent event=new FightEvent();
		event.setId(uidkit.getPlusUid());
		// FightEventSave save=new FightEventSave();
		// save.setData(event);
		// // save.setSaveTimeDB(TimeKit.getSecondTime());
		// // 内存中加入
		// cacheMap.put(event.getId(),save);
		// // 改变列表里面加入
		// changeListMap.put(event.getId(),save);
		return event;
	}
	public synchronized void saveEvent(FightEvent event)
	{
		if(event==null) return;
		FightEventSave save=new FightEventSave();
		save.setData(event);
		save.setSaveTimeDB(TimeKit.getSecondTime());
		cacheMap.put(event.getId(),save);
		// 改变列表里面加入
		changeListMap.put(event.getId(),save);
	}
	public void init()
	{
		cacheMap=new IntKeyHashMap(ALL_EVENT_SIZE);
		changeListMap=new IntKeyHashMap(CHANGE_EVENT);
		int time=TimeKit.getSecondTime();
		/** 查询2周内有数据改动的玩家 */
		String sql="SELECT * FROM fight_events";
		// 数据库加载所有玩家数据
		FightEvent event[]=(FightEvent[])dbaccess.loadBySql(sql);
		if(event!=null)
		{
			for(int i=0,n=event.length;i<n;i++)
			{
				FightEventSave eventSave=new FightEventSave();
				eventSave.setData(event[i]);
				// 设置保存的时间
				eventSave.setSaveTimeDB(time);
				eventSave.setSaveTimeRedis(time);
				cacheMap.put(event[i].getId(),eventSave);
			}
		}
//		// redis获取最新玩家数据进行覆盖
//		FightEvent eventJedis[]=jedisCache.loadAllFightEvents();
//		if(eventJedis!=null)
//		{
//			for(int i=0,n=eventJedis.length;i<n;i++)
//			{
//				FightEventSave save=(FightEventSave)cacheMap
//					.get(eventJedis[i].getId());
//				// jedis最新的fightevent
//				if(save!=null)
//				{
//					save.setSaveTimeDB(time);
//					save.setSaveTimeRedis(time);
//					save.setData(eventJedis[i]);
//					// 覆盖的数据加入改变列表 下次一起存储
//					changeListMap.put(eventJedis[i].getId(),save);
//				}
//				// 数据库没有存下来 redis还有
//				else
//				{
//					FightEventSave eventSave=new FightEventSave();
//					eventSave.setData(eventJedis[i]);
//					// 设置保存的时间
//					eventSave.setSaveTimeDB(time);
//					eventSave.setSaveTimeRedis(time);
//					// 覆盖的数据加入改变列表 下次一起存储
//					changeListMap.put(eventJedis[i].getId(),eventSave);
//					cacheMap.put(eventJedis[i].getId(),eventSave);
//				}
//			}
//		}
		// 设置fleet的属性
		Object object[]=cacheMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			FightEventSave fightEventSave=(FightEventSave)object[i];
			FleetGroup fleetGroup=fightEventSave.getData().getFleetGroup();
			Player player=playerMemCache.loadPlayerOnly(fightEventSave
				.getData().getPlayerId()
				+"");
			if(player==null) continue;
			SeaBackKit.addAdjustment(player,fleetGroup);
		}
		setFightEvent();
		// 启动定时器 先启动redis的
//		TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public synchronized Object loadOnly(String key)
	{
		FightEventSave data=(FightEventSave)cacheMap.get(Integer
			.parseInt(key));
		if(data!=null)
		{
			return data.getData();
		}
		return null;
	}

	public synchronized Object load(String key)
	{
		FightEventSave data=(FightEventSave)cacheMap.get(Integer
			.parseInt(key));
		if(data!=null)
		{
			data.setSaveTimeDB(TimeKit.getSecondTime());
			if(changeListMap.get(Integer.parseInt(key))==null)
			{
				changeListMap.put(Integer.parseInt(key),data);
			}
			return data.getData();
		}
		return null;
	}

	public Object[] loads(String[] keys)
	{
		// TODO 自动生成方法存根
		return null;
	}

	public void save(String key,Object data)
	{
		// TODO 自动生成方法存根

	}

	public void onTimer(TimerEvent e)
	{
		// TODO 自动生成方法存根
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				EVENT_DB_TIME,JedisMemCacheAccess.FIGHT_EVENT_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
//			collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
//				EVENT_REDIS_TIME,JedisMemCacheAccess.FIGHT_EVENT_REDIS);
		}
	}

	/** 内部类 某个玩家的邮件列表 */
	private class FightEventList extends ChangeAdapter
	{

		ArrayList fightEventList=new ArrayList();

		/** 添加邮件 */
		public void addFightEvent(FightEvent event)
		{
			event.addListener(this);
			fightEventList.add(event);
		}

		/**
		 * @return messageList
		 */
		public ArrayList getFightEventList()
		{
			return fightEventList;
		}

		/**
		 * @param messageList 要设置的 messageList
		 */
		public void setFightEventList(ArrayList fightEventList)
		{
			this.fightEventList=fightEventList;
		}

		public void change(Object source,int type)
		{
			// TODO 自动生成方法存根
			if(source instanceof FightEvent&&type==FightEvent.CHANGE_FINISH)
			{
				((FightEvent)source).removeListener(this);
				fightEventList.remove(source);
			}
		}

	}

	/**
	 * @return playerMemCache
	 */
	public PlayerMemCache getPlayerMemCache()
	{
		return playerMemCache;
	}

	/**
	 * @param playerMemCache 要设置的 playerMemCache
	 */
	public void setPlayerMemCache(PlayerMemCache playerMemCache)
	{
		this.playerMemCache=playerMemCache;
	}

	@Override
	public void deleteCache(Object event)
	{
		// TODO 自动生成方法存根
		if(event==null) return;
		FightEvent ev=(FightEvent)event;
		cacheMap.remove(ev.getId());
	}
}
