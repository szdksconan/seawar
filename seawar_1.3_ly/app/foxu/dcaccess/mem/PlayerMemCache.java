package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Role;
import foxu.sea.kit.SeaBackKit;

/**
 * player内存管理 每1个小时对修改的数据保存进数据库 并把redis相同id的数据删除 每5分钟 同步数据到redis
 * 服务器启动先从数据库加载所有数据 然后从redis取相同id的数据覆盖 数据库的数据 author:icetiger
 */
public class PlayerMemCache extends MemCache
{
	/** 默认改变列表和数据表大小 */
	public static final int ALL_PLAYER_SIZE=10000,CHANGE_PLAYER=300;
	/** 半小时修改过的数据更新到数据库 2者时间最好不要有相遇的时候 */
	public static final int PLAYER_DB_TIME=60*10,REDIS_TIME=60*5;
	/** 初始化的岛屿sid */
	public static final int START_SID=1;
	
	CreatObjectFactory cfactory;

	public void setFactory(CreatObjectFactory factory)
	{
		cfactory=factory;
	}
	/** 关服存储当前数据 */
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.PLAYER_REDIS);
		return changeListMap.size();
	}

	@Override
	public Player createObect()
	{
		Player player=(Player)Role.factory.newSample(START_SID);
		player.setId(uidkit.getPlusUid());
		player.setCreateTime(TimeKit.getSecondTime());
		player.setUpdateTime(TimeKit.getSecondTime());
		return player;
	}

	/** 游戏启动加载所有符合条件的玩家 从数据库加载 */
	public void init()
	{
		cacheMap=new IntKeyHashMap(ALL_PLAYER_SIZE);
		changeListMap=new IntKeyHashMap(CHANGE_PLAYER);
		// 最大可使用内存
		int time=TimeKit.getSecondTime();
		/** 查询1个月内有数据改动的玩家 */
		String sql="SELECT * FROM players";
		// 数据库加载所有玩家数据
		Player player[]=(Player[])dbaccess.loadBySql(sql);
		if(player!=null)
		{
			for(int i=0,n=player.length;i<n;i++)
			{
				initNewField(player[i]);
				PlayerSave playerSave=new PlayerSave();
				playerSave.setData(player[i]);
				// 推动下服务
				player[i].checkService(TimeKit.getSecondTime());
				//设置关卡加成等级
				player[i].setAllPointBuff();
				// 解析头像信息
				player[i].parserHead();
				//添加技能
				//player[i].resetAdjustment();
				SeaBackKit.resetPlayerSkill(player[i],cfactory);
				// 加城防技能
				SeaBackKit.addAdjustment(player[i],player[i].getIsland()
					.getMainGroup());
				// 设置保存的时间
				playerSave.setSaveTimeDB(time);
				playerSave.setSaveTimeRedis(time);
				cacheMap.put(player[i].getId(),playerSave);
			}
		}
//		// redis获取最新玩家数据进行覆盖
//		Player playerRedis[]=jedisCache.loadPlayers();
//		if(playerRedis!=null)
//		{
//			for(int i=0,n=playerRedis.length;i<n;i++)
//			{
//				PlayerSave save=(PlayerSave)cacheMap.get(playerRedis[i]
//					.getId());
//				// 数据库最新的player
//				if(save!=null)
//				{
//					save.setSaveTimeDB(time);
//					save.setSaveTimeRedis(time);
//					save.setData(playerRedis[i]);
//					// 覆盖的数据加入改变列表 下次一起存储
//					changeListMap.put(playerRedis[i].getId(),save);
//				}
//			}
//		}
		// 启动定时器 先启动redis的
//		TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);
	}
	/** 只读取数据 不加入改变列表 */
	public Player loadPlayerOnly(String key)
	{
		synchronized(this)
		{
			PlayerSave data=(PlayerSave)cacheMap.get(Integer.parseInt(key));
			if(data!=null)
			{
				return data.getData();
			}
		}
		// Player player=(Player)dbaccess.load(key);
		// save(key,player);
		return null;
	}

	/** 通过ID 获取玩家数据 并加入改变列表 */
	public Player load(String key)
	{
		synchronized(this)
		{
			PlayerSave data=(PlayerSave)cacheMap.get(Integer.parseInt(key));
			if(data!=null)
			{
				data.setSaveTimeDB(TimeKit.getSecondTime());
				if(changeListMap.get(Integer.parseInt(key))==null)
				{
					changeListMap.put(Integer.parseInt(key),data);
				}
				return data.getData();
			}
		}
		// Player player=(Player)dbaccess.load(key);
		// save(key,player);
		return null;
	}
	
	/** 通过玩家名字 获取玩家数据 bool是否加入改变列表 */
	public Player loadByName(String name,boolean bool)
	{
		// 加入改变列表
		synchronized(this)
		{
			Object object[]=cacheMap.valueArray();
			for(int i=0;i<object.length;i++)
			{
				PlayerSave data=(PlayerSave)object[i];
				Player player=data.getData();
				try
				{
					if(player.getName().equalsIgnoreCase(name))
					{
						if(bool)
						{
							if(changeListMap.get(player.getId())==null)
								changeListMap.put(player.getId(),data);
						}
						return player;
					}
				}
				catch(Exception e)
				{
					continue;
				}
			}
		}
		return null;
	}
	/** 获取总共充值宝石 */
	public int getMaxGems()
	{
		Object object[]=cacheMap.valueArray();
		int gems=0;
		for(int i=0;i<object.length;i++)
		{
			PlayerSave data=(PlayerSave)object[i];
			Player player=data.getData();
			gems+=player.getResources()[Resources.MAXGEMS];
		}
		return gems;
	}

	/** 获取多个 */
	public Player[] loads(String[] keys)
	{
		return null;
	}

	/** 储存 */
	public synchronized void save(String key,Object data)
	{
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		PlayerSave save=new PlayerSave();
		save.setData((Player)data);
		save.setSaveTimeDB(time);
		save.setSaveTimeRedis(time);
		// 内存中加入
		cacheMap.put(Integer.parseInt(key),save);
		// 改变列表里面加入
		changeListMap.put(Integer.parseInt(key),save);
	}

	public void onTimer(TimerEvent e)
	{
		// TODO 自动生成方法存根
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				PLAYER_DB_TIME,JedisMemCacheAccess.PLAYER_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
			collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
				REDIS_TIME,JedisMemCacheAccess.PLAYER_REDIS);
		}
	}

	@Override
	public void deleteCache(Object save)
	{
		// TODO 自动生成方法存根
	}
	/** 初始化新增加的玩家属性 */
	public void initNewField(Player player)
	{
		if(player.getAttributes(PublicConst.DEFAULT_PUSH_MARK)==null)
		{
			// 初始化每日折扣推送开关
			int tag=1;
			tag=tag<<PublicConst.DATE_OFF_PUSH;
			int iosSystem=player.getIsland().getIosSystem();
			iosSystem|=tag;
			tag=1;
			tag=tag<<PublicConst.ONLINE_AWARD_PUSH;
			iosSystem|=tag;
			player.getIsland().setIosSystem(iosSystem);
			player.setAttribute(PublicConst.DEFAULT_PUSH_MARK,"T");
		}
		// 兼容后增加的推送标记不再需要多个玩家属性
		String initMark=player.getAttributes(PublicConst.ADD_INIT_PUSH);
		int[] newMarks={PublicConst.PEACE_TIME_PUSH,PublicConst.MEAL_TIME_ENERGY_PUSH,PublicConst.STATIONED_PUSH};
		IntList addMarks=new IntList(newMarks);
		int iosSystem=player.getIsland().getIosSystem();
		//去除已经初始化过的标记
		if(initMark!=null)
		{
			String[] marks=TextKit.split(initMark,",");
			for(int i=0;i<marks.length;i++)
			{
				int mark=TextKit.parseInt(marks[i]);
				for(int j=0;j<addMarks.size();j++)
				{
					if(mark==addMarks.get(j))
					{
						addMarks.removeIndex(j);
						break;
					}
				}
			}
		}
		//剩下的新标记处理
		StringBuffer newMarkStr=new StringBuffer();
		for(int i=0;i<addMarks.size();i++)
		{
			newMarkStr.append(","+addMarks.get(i));
			int tag=1;
			tag=tag<<addMarks.get(i);
			iosSystem|=tag;
		}
		//去除开头的逗号
		if(initMark==null)
			player.setAttribute(PublicConst.ADD_INIT_PUSH,newMarkStr.deleteCharAt(0).toString());
		else
			player.setAttribute(PublicConst.ADD_INIT_PUSH,initMark+newMarkStr.toString());
		player.getIsland().setIosSystem(iosSystem);
	}
}
