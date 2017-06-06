package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import redis.clients.jedis.Pipeline;
import shelby.dc.GameDBAccess;
import shelby.dc.MemCacheAccess;
import foxu.dcaccess.datasave.ObjectSave;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.messgae.Message;
import foxu.sea.uid.UidKit;

/**
 * 内存管理器 新创建的类要加到改变列表和内存列表里面去
 */
public abstract class MemCache implements MemCacheAccess,TimerListener
{

	/** 日志记录 */
	private static final Logger log=LogFactory.getLogger(MemCache.class);
	/** 数据库定时器 */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** redis更新定时器 */
	TimerEvent eventRedis=new TimerEvent(this,"redis",60*1000);
	/** 先从内存找 没有找到去数据库找 并加载到内存中 */
	IntKeyHashMap cacheMap;
	/** 改变列表 需要存入数据库 */
	IntKeyHashMap changeListMap;
	/** redis接口 */
	JedisMemCacheAccess jedisCache;
	/** 数据加载器 */
	GameDBAccess dbaccess;
	/** UID提供器 */
	UidKit uidkit;
	/** 数据库是否正在存储 */
	boolean dbSave=false;

	/** 初始化 */
	public abstract void init();
	/** 创建新的对象 需要立即加入内存和改变列表中 */
	public abstract Object createObect();
	/** 删除在内存中 */
	public abstract void deleteCache(Object save);

	/** 整理方法 DB */
	void collateDB(int nowTime,IntKeyHashMap changeListMap,int everyDBTime,
		String redisKey)
	{
		if(dbSave)
		{
			log.error("db save redisKey==="+redisKey+",dbSave=true");
			return;
		}
		dbSave=true;
		// IntKeyHashMap needDelete=new IntKeyHashMap();
		try
		{
			Object object[];
			synchronized(this)
			{
				object=changeListMap.valueArray();
			}
			//log.error("db save redisKey=object"+redisKey+",object="+object.length);
			for(int i=0;i<object.length;i++)
			{
				ObjectSave data=(ObjectSave)object[i];
				int saveTimeDB=data.getSaveTimeDB();
				if((saveTimeDB+everyDBTime)>nowTime) continue;
				// 如果需要删除
				if(dbaccess.isDelete(data.getData()))
				{
					dbaccess.delete(data.getData());
					data.setSaveTimeDB(TimeKit.getSecondTime());
					// needDelete.put(data.getId(),data.getId());
					// 删除改变列表保存的
					synchronized(this)
					{
						deleteCache(data.getData());
						changeListMap.remove(data.getId());
					}
				}
				else
				{
					boolean save=false;
					// 存储
					save=dbaccess.save(data.getData());
					if(save||data.getData() instanceof Message)
					{
						if(data.getData() instanceof Message)
						{
							if(((Message)data.getData()).getDelete()!=0)
							{
								deleteCache(data.getData());
							}
						}
						data.setSaveTimeDB(TimeKit.getSecondTime());
						// needDelete.put(data.getId(),data.getId());
						// 删除改变列表保存的
						synchronized(this)
						{
							changeListMap.remove(data.getId());
						}
					}
					if(!save&&data.getData() instanceof Alliance)
					{
						((Alliance)data.getData()).setDescription("");
						((Alliance)data.getData()).setAnnouncement("");
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			// try
			// {
			// // 删除redis里面的
			// // deleteRedisKeys(needDelete,redisKey);
			// }
			// catch(Exception e)
			// {
			// // TODO: handle exception
			// e.printStackTrace();
			// dbSave=false;
			// }
			dbSave=false;
		}
		if(log.isInfoEnabled())
		{
			Runtime r=Runtime.getRuntime();
			long memory=r.totalMemory();
			long used=memory-r.freeMemory();
			log.info("collate ok, dbCollate ok  redisKey="+redisKey
				+", memory="+used+"/"+memory+", maxMemory="+r.maxMemory());
		}
	}
	/** 整理方法 redis */
	void collateRedis(int nowTime,IntKeyHashMap changeListMap,
		int saveRedisTime,String key)
	{
		// // 数据库正在存储
		// if(dbSave) return;
		// Object object[]=changeListMap.valueArray();
		// boolean bool=false;
		// // 管道存储
		// Pipeline p=jedisCache.pipelined();
		// for(int i=0;i<object.length;i++)
		// {
		// ObjectSave data=(ObjectSave)object[i];
		// int saveTimeRedis=data.getSaveTimeRedis();
		// if((saveTimeRedis+saveRedisTime)>nowTime) continue;
		// bool=true;
		// // 管道储存
		// p.set(key+data.getId(),jedisCache.getBase64After(data
		// .getByteBuffer()));
		// data.setSaveTimeRedis(TimeKit.getSecondTime());
		// }
		// if(bool)
		// {
		// p.sync();
		// }
	}

	/** 数据库存储后 删除redis里面的key */
	public void deleteRedisKeys(IntKeyHashMap needDelete,String redisKey)
	{
		// // 管道存储
		// Pipeline p=jedisCache.pipelined();
		// Object object[]=needDelete.valueArray();
		// for(int i=0;i<object.length;i++)
		// {
		// int id=Integer.parseInt(object[i].toString());
		// // 管道储存
		// p.del(redisKey+id);
		// }
		// // 删除改变列表里面的
		// if(needDelete.size()>0)
		// {
		// p.sync();
		// }
	}

	/**
	 * 删除redis上对应ID的备份 返回0失败 1成功
	 */
	void deleteRedisById(String id)
	{
		jedisCache.delKey(id);
	}

	/**
	 * @return jedisCache
	 */
	public JedisMemCacheAccess getJedisCache()
	{
		return jedisCache;
	}

	/**
	 * @param jedisCache 要设置的 jedisCache
	 */
	public void setJedisCache(JedisMemCacheAccess jedisCache)
	{
		this.jedisCache=jedisCache;
	}

	/**
	 * @return dbaccess
	 */
	public GameDBAccess getDbaccess()
	{
		return dbaccess;
	}

	/**
	 * @param dbaccess 要设置的 dbaccess
	 */
	public void setDbaccess(GameDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}

	/**
	 * @return uidkit
	 */
	public UidKit getUidkit()
	{
		return uidkit;
	}

	/**
	 * @param uidkit 要设置的 uidkit
	 */
	public void setUidkit(UidKit uidkit)
	{
		this.uidkit=uidkit;
	}

	/**
	 * @return cacheMap
	 */
	public IntKeyHashMap getCacheMap()
	{
		return cacheMap;
	}

	/**
	 * @param cacheMap 要设置的 cacheMap
	 */
	public void setCacheMap(IntKeyHashMap cacheMap)
	{
		this.cacheMap=cacheMap;
	}

	/**
	 * @return changeListMap
	 */
	public IntKeyHashMap getChangeListMap()
	{
		return changeListMap;
	}

	/**
	 * @param changeListMap 要设置的 changeListMap
	 */
	public void setChangeListMap(IntKeyHashMap changeListMap)
	{
		this.changeListMap=changeListMap;
	}
}
