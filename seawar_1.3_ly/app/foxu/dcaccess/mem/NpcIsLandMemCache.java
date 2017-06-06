package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.datasave.NpcIsLandSave;
import foxu.sea.NpcIsland;
import foxu.sea.island.SpaceIslandContainer;

/** 岛屿内存管理 */
public class NpcIsLandMemCache extends MemCache
{

	/** 默认改变列表和数据表大小 */
	public static final int ALL_ISLAND_SIZE=360000,CHANGE_ISLAND=500;
	/** 1小时修改过的数据更新到数据库 */
	public static final int ISLAND_DB_TIME=60*20,ISLAND_REDIS_TIME=60*5;
	/** playerid对应的岛屿 */
	IntKeyHashMap islandMap=new IntKeyHashMap();
	/** 空岛屿容器 */
	SpaceIslandContainer islandContainer;

	/** 关服存储当前数据 */
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.ISLAND_REDIS);

		return changeListMap.size();
	}

	public Object createObect()
	{
		return null;
	}

	/**
	 * 根据玩家的id随机一个空岛出来
	 * 
	 * @return 返回找到的空岛屿,返回null表示没有空岛屿
	 */
	public synchronized NpcIsland getRandomSpace()
	{
		NpcIsland island=islandContainer.getRandomSpace();
		if(island!=null)
		{
			NpcIsLandSave data=(NpcIsLandSave)cacheMap
				.get(island.getIndex());
			// changeListMap.put(island.getIndex(),data);
		}
		return island;
	}
	/** 移除一个岛屿,重新计算这个岛屿所在区域的密度 */
	public void removeSpaceIsland(NpcIsland island)
	{
		islandContainer.removeSpaceIsland(island);
	}
	/** 添加一个空岛屿,重新计算这个岛屿所在区域的密度 */
	public void putSpaceIsland(NpcIsland island)
	{
		islandContainer.putSpaceIsland(island);
	}
	/** 设计玩家对应的岛屿index */
	public void setIslandMap()
	{
		Object[] object=cacheMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			NpcIsLandSave isLandSave=(NpcIsLandSave)object[i];
			NpcIsland island=isLandSave.getData();
			if(island.getPlayerId()!=0)
			{
				// 错误判定
				if(islandMap.get(island.getPlayerId())!=null)
				{
					island.setPlayerId(0);
					load(island.getIndex()+"");
				}
				else
					islandMap.put(island.getPlayerId(),island.getIndex());
			}
			islandContainer.addIsLand(island);
		}
		islandContainer.initDensity();
	}

	/** 获取玩家当前的岛屿index */
	public synchronized int getPlayerIsLandId(int playerId)
	{
		if(islandMap.get(playerId)==null) return -1;
		return Integer.parseInt(islandMap.get(playerId).toString());
	}

	/** 分配一个玩家岛屿 */
	public synchronized void addPlayerIsLandMap(NpcIsland island)
	{
		if(island.getPlayerId()!=0)
			islandMap.put(island.getPlayerId(),island.getIndex());
	}

	/** 移除一个玩家岛屿 迁走 */
	public synchronized void removePlayerIslandMap(int playerId)
	{
		islandMap.remove(playerId);
	}

	/** 获取玩家岛屿并加入改变列表 */
	public NpcIsland getPlayerIsLandAndChange(int playerId)
	{
		return load(getPlayerIsLandId(playerId)+"");
	}

	/** 获取玩家岛屿 */
	public NpcIsland getPlayerIsland(int playerId)
	{
		return loadOnly(getPlayerIsLandId(playerId)+"");
	}

	public void init()
	{
		cacheMap=new IntKeyHashMap(ALL_ISLAND_SIZE);
		changeListMap=new IntKeyHashMap(CHANGE_ISLAND);
		// 剩余内存
		int time=TimeKit.getSecondTime();
		/** 查询2周内有数据改动的玩家 */
		String sql="SELECT * FROM npc_islands";
		// 数据库加载所有玩家数据
		NpcIsland island[]=(NpcIsland[])dbaccess.loadBySql(sql);
		// String str="\r\n";
		if(island!=null)
		{
			for(int i=0,n=island.length;i<n;i++)
			{
				NpcIsLandSave isLandSave=new NpcIsLandSave();
				isLandSave.setData(island[i]);
				// 设置保存的时间
				isLandSave.setSaveTimeDB(time);
				isLandSave.setSaveTimeRedis(time);
				cacheMap.put(island[i].getIndex(),isLandSave);
				// if(island[i].getPlayerId()!=0)
				// {
				// str+=island[i].getIndex()+":";
				// }
			}
			// // 打印岛屿数据
			// File f=new File("filefan-2.txt");
			// try
			// {
			// f.createNewFile();
			// FileOutputStream fos=new FileOutputStream(f);
			// ObjectOutputStream oos=new ObjectOutputStream(fos);
			//
			// oos.writeObject(str);
			// oos.close();
			// }
			// catch(IOException e)
			// {
			// // TODO 自动生成 catch 块
			// e.printStackTrace();
			// }
		}
		// // redis获取最新玩家数据进行覆盖
		// NpcIsland islandRedis[]=jedisCache.loadNpcIsLand();
		// if(islandRedis!=null)
		// {
		// for(int i=0,n=islandRedis.length;i<n;i++)
		// {
		// NpcIsLandSave save=(NpcIsLandSave)cacheMap
		// .get(islandRedis[i].getIndex());
		// // 数据库最新的
		// if(save!=null)
		// {
		// save.setSaveTimeDB(time);
		// save.setSaveTimeRedis(time);
		// save.setData(islandRedis[i]);
		// // 覆盖的数据加入改变列表 下次一起存储
		// changeListMap.put(islandRedis[i].getIndex(),save);
		// }
		// }
		// }
		setIslandMap();
		// 启动定时器 先启动redis的
		// TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	/** key为岛屿的index */
	public synchronized NpcIsland load(String index)
	{
		NpcIsLandSave data=(NpcIsLandSave)cacheMap.get(Integer
			.parseInt(index));
		if(data!=null)
		{
			data.setSaveTimeDB(TimeKit.getSecondTime());
			if(changeListMap.get(Integer.parseInt(index))==null)
			{
				changeListMap.put(Integer.parseInt(index),data);
			}
			return data.getData();
		}
		return null;
	}

	/** key为岛屿的index */
	public synchronized NpcIsland loadOnly(String index)
	{
		NpcIsLandSave data=(NpcIsLandSave)cacheMap.get(Integer
			.parseInt(index));
		if(data!=null)
		{
			return data.getData();
		}
		return null;
	}

	public Object[] loads(String[] keys)
	{
		return null;
	}

	public void save(String key,Object data)
	{

	}

	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				ISLAND_DB_TIME,JedisMemCacheAccess.ISLAND_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
			// collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
			// ISLAND_REDIS_TIME,JedisMemCacheAccess.ISLAND_REDIS);
		}
	}

	@Override
	public void deleteCache(Object save)
	{
		// TODO 自动生成方法存根

	}

}
