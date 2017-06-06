package jedis;

import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * jedis
 * 
 * @author icetiger
 */
public class SeawarJedis
{
	/** jedis实例 */
	// private Jedis jedis=new Jedis("localhost",6379);
	private Jedis jedis;

	/** dataBase redis库选择 */
	public void init(String host,int port,int dataBase)
	{
//		jedis=new Jedis(host,port);
//		jedis.connect();
//		jedis.select(dataBase);
	}

	/** 清空所有库 */
	public String flushAll()
	{
		return jedis.flushAll();
	}

	/** 连接是否连上 */
	public boolean isConnected()
	{
		return jedis.isConnected();
	}

	/** 删除某个key */
	public void delKey(String key)
	{
		jedis.del(key);
	}

	/** 设置key和value 已经存在的key会被覆盖 返回ok表示成功 */
	public String set(final String key,String value)
	{
		return jedis.set(key,value);
	}

	/** 设置key和value 已经存在的key则放弃操作 返回1表示成功 返回0表示失败 */
	public Long setnx(final String key,String value)
	{
		return jedis.setnx(key,value);
	}

	/** 获取指定key的value */
	public String get(final String key)
	{
		return jedis.get(key);
	}

	/**
	 * 获取key并且改变原有的value 如果key不存在 则创建 GETSET is an atomic set this value and
	 * return the old value command.
	 */
	public String getSet(final String key,String value)
	{
		return jedis.getSet(key,value);
	}

	/** 查看某个key是否已经存在 */
	public boolean exists(final String key)
	{
		return jedis.exists(key);
	}

	/**
	 * 获取指定范围的keys examples:
	 * <li>h?llo will match hello hallo hhllo
	 * <li>h*llo will match hllo heeeello
	 * <li>h[ae]llo will match hello and hallo, but not hillo
	 */
	public Set<String> keys(final String pattern)
	{
		return jedis.keys(pattern);
	}

	public Pipeline pipelined()
	{
		return jedis.pipelined();
	}
}
