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
 * �ڴ������ �´�������Ҫ�ӵ��ı��б���ڴ��б�����ȥ
 */
public abstract class MemCache implements MemCacheAccess,TimerListener
{

	/** ��־��¼ */
	private static final Logger log=LogFactory.getLogger(MemCache.class);
	/** ���ݿⶨʱ�� */
	TimerEvent eventDB=new TimerEvent(this,"db",60*1000);
	/** redis���¶�ʱ�� */
	TimerEvent eventRedis=new TimerEvent(this,"redis",60*1000);
	/** �ȴ��ڴ��� û���ҵ�ȥ���ݿ��� �����ص��ڴ��� */
	IntKeyHashMap cacheMap;
	/** �ı��б� ��Ҫ�������ݿ� */
	IntKeyHashMap changeListMap;
	/** redis�ӿ� */
	JedisMemCacheAccess jedisCache;
	/** ���ݼ����� */
	GameDBAccess dbaccess;
	/** UID�ṩ�� */
	UidKit uidkit;
	/** ���ݿ��Ƿ����ڴ洢 */
	boolean dbSave=false;

	/** ��ʼ�� */
	public abstract void init();
	/** �����µĶ��� ��Ҫ���������ڴ�͸ı��б��� */
	public abstract Object createObect();
	/** ɾ�����ڴ��� */
	public abstract void deleteCache(Object save);

	/** ������ DB */
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
				// �����Ҫɾ��
				if(dbaccess.isDelete(data.getData()))
				{
					dbaccess.delete(data.getData());
					data.setSaveTimeDB(TimeKit.getSecondTime());
					// needDelete.put(data.getId(),data.getId());
					// ɾ���ı��б����
					synchronized(this)
					{
						deleteCache(data.getData());
						changeListMap.remove(data.getId());
					}
				}
				else
				{
					boolean save=false;
					// �洢
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
						// ɾ���ı��б����
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
			// // ɾ��redis�����
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
	/** ������ redis */
	void collateRedis(int nowTime,IntKeyHashMap changeListMap,
		int saveRedisTime,String key)
	{
		// // ���ݿ����ڴ洢
		// if(dbSave) return;
		// Object object[]=changeListMap.valueArray();
		// boolean bool=false;
		// // �ܵ��洢
		// Pipeline p=jedisCache.pipelined();
		// for(int i=0;i<object.length;i++)
		// {
		// ObjectSave data=(ObjectSave)object[i];
		// int saveTimeRedis=data.getSaveTimeRedis();
		// if((saveTimeRedis+saveRedisTime)>nowTime) continue;
		// bool=true;
		// // �ܵ�����
		// p.set(key+data.getId(),jedisCache.getBase64After(data
		// .getByteBuffer()));
		// data.setSaveTimeRedis(TimeKit.getSecondTime());
		// }
		// if(bool)
		// {
		// p.sync();
		// }
	}

	/** ���ݿ�洢�� ɾ��redis�����key */
	public void deleteRedisKeys(IntKeyHashMap needDelete,String redisKey)
	{
		// // �ܵ��洢
		// Pipeline p=jedisCache.pipelined();
		// Object object[]=needDelete.valueArray();
		// for(int i=0;i<object.length;i++)
		// {
		// int id=Integer.parseInt(object[i].toString());
		// // �ܵ�����
		// p.del(redisKey+id);
		// }
		// // ɾ���ı��б������
		// if(needDelete.size()>0)
		// {
		// p.sync();
		// }
	}

	/**
	 * ɾ��redis�϶�ӦID�ı��� ����0ʧ�� 1�ɹ�
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
	 * @param jedisCache Ҫ���õ� jedisCache
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
	 * @param dbaccess Ҫ���õ� dbaccess
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
	 * @param uidkit Ҫ���õ� uidkit
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
	 * @param cacheMap Ҫ���õ� cacheMap
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
	 * @param changeListMap Ҫ���õ� changeListMap
	 */
	public void setChangeListMap(IntKeyHashMap changeListMap)
	{
		this.changeListMap=changeListMap;
	}
}
