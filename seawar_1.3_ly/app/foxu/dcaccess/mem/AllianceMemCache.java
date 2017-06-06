package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceFlag;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;

public class AllianceMemCache extends MemCache
{
	/** 1小时修改过的数据更新到数据库 */
	public static final int ALLIAN_DB_TIME=60*15,ALLIAN_REDIS_TIME=60*5;
	
	/** 数据提供器 */
	CreatObjectFactory factory;
	
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.ALLIANCE_REDIS);
		return changeListMap.size();
	}

	public synchronized Object createObect()
	{
		Alliance alliance=new Alliance();
		alliance.setId(uidkit.getPlusUid());
		alliance.setCreate_at(TimeKit.getSecondTime());
		AllianceSave save=new AllianceSave();
		save.setData(alliance);
		save.setSaveTimeDB(TimeKit.getSecondTime());
		// 内存中加入
		cacheMap.put(alliance.getId(),save);
		// 改变列表里面加入
		changeListMap.put(alliance.getId(),save);
		alliance.setRankNum(cacheMap.size());
		return alliance;
	}

	public void deleteCache(Object save)
	{
		if(save==null) return;
		Alliance al=(Alliance)save;
		cacheMap.remove(al.getId());
		changeListMap.remove(al.getId());
	}

	public void init()
	{
		cacheMap=new IntKeyHashMap();
		changeListMap=new IntKeyHashMap();
		int time=TimeKit.getSecondTime();
		String sql="SELECT * FROM alliances";
		/**加载旗帜**/
		AllianceFlag.setGiveTheValue();
		// 数据库加载所有玩家数据
		Alliance alliance[]=(Alliance[])dbaccess.loadBySql(sql);
		if(alliance!=null)
		{
			for(int i=0,n=alliance.length;i<n;i++)
			{
				AllianceSave allianceSave=new AllianceSave();
				allianceSave.setData(alliance[i]);
				// 设置保存的时间
				allianceSave.setSaveTimeDB(time);
				allianceSave.setSaveTimeRedis(time);
				cacheMap.put(alliance[i].getId(),allianceSave);
				//计算自动移交会长
				alliance[i].autoChangeMaster(factory);
				alliance[i].getFlag().randomAllianceFlag();
			}
		}
//		// redis获取最新玩家数据进行覆盖
//		Alliance allianJedis[]=jedisCache.loadAllAlliance();
//		if(allianJedis!=null)
//		{
//			for(int i=0,n=allianJedis.length;i<n;i++)
//			{
//				AllianceSave save=(AllianceSave)cacheMap.get(allianJedis[i]
//					.getId());
//				// jedis最新的fightevent
//				if(save!=null)
//				{
//					save.setSaveTimeDB(time);
//					save.setSaveTimeRedis(time);
//					save.setData(allianJedis[i]);
//					// 覆盖的数据加入改变列表 下次一起存储
//					changeListMap.put(allianJedis[i].getId(),save);
//					//计算自动移交会长
//					allianJedis[i].autoChangeMaster(factory);
//				}
//				// 数据库没有存下来 redis还有
//				else
//				{
//					AllianceSave allianSave=new AllianceSave();
//					allianSave.setData(allianJedis[i]);
//					// 设置保存的时间
//					allianSave.setSaveTimeDB(time);
//					allianSave.setSaveTimeRedis(time);
//					// 覆盖的数据加入改变列表 下次一起存储
//					changeListMap.put(allianJedis[i].getId(),allianSave);
//					cacheMap.put(allianJedis[i].getId(),allianSave);
//				}
//			}
//		}
		// 启动定时器 先启动redis的
//		TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);

	}

	/** 通过玩家名字 获取玩家数据 bool是否加入改变列表 */
	public Alliance loadByName(String name,boolean bool)
	{
		// 加入改变列表
		synchronized(this)
		{
			Object object[]=cacheMap.valueArray();
			for(int i=0;i<object.length;i++)
			{
				AllianceSave data=(AllianceSave)object[i];
				Alliance alliance=data.getData();
				try
				{
					if(alliance.getName().equalsIgnoreCase(name))
					{
						if(bool)
						{
							if(changeListMap.get(alliance.getId())==null)
								changeListMap.put(alliance.getId(),data);
						}
						return alliance;
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

	/** key为联盟id 联盟全加载 */
	public synchronized Object loadOnly(String key)
	{
		if(key==null||key.equals(""))return null;
		AllianceSave data=(AllianceSave)cacheMap.get(Integer.parseInt(key));
		if(data!=null)
		{
			return data.getData();
		}
		return null;
	}

	public synchronized Object load(String key)
	{
		if(key==null||key.equals(""))return null;
		AllianceSave data=(AllianceSave)cacheMap.get(Integer.parseInt(key));
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

	/** KEY为联盟ID */
	public synchronized void save(String key,Object data)
	{
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		AllianceSave save=new AllianceSave();
		save.setData((Alliance)data);
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
				ALLIAN_DB_TIME,JedisMemCacheAccess.ALLIANCE_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
//			collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
//				ALLIAN_REDIS_TIME,JedisMemCacheAccess.ALLIANCE_REDIS);
		}

	}

	
	public CreatObjectFactory getFactory()
	{
		return factory;
	}

	
	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}
	
}
