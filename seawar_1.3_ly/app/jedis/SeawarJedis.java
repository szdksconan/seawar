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
	/** jedisʵ�� */
	// private Jedis jedis=new Jedis("localhost",6379);
	private Jedis jedis;

	/** dataBase redis��ѡ�� */
	public void init(String host,int port,int dataBase)
	{
//		jedis=new Jedis(host,port);
//		jedis.connect();
//		jedis.select(dataBase);
	}

	/** ������п� */
	public String flushAll()
	{
		return jedis.flushAll();
	}

	/** �����Ƿ����� */
	public boolean isConnected()
	{
		return jedis.isConnected();
	}

	/** ɾ��ĳ��key */
	public void delKey(String key)
	{
		jedis.del(key);
	}

	/** ����key��value �Ѿ����ڵ�key�ᱻ���� ����ok��ʾ�ɹ� */
	public String set(final String key,String value)
	{
		return jedis.set(key,value);
	}

	/** ����key��value �Ѿ����ڵ�key��������� ����1��ʾ�ɹ� ����0��ʾʧ�� */
	public Long setnx(final String key,String value)
	{
		return jedis.setnx(key,value);
	}

	/** ��ȡָ��key��value */
	public String get(final String key)
	{
		return jedis.get(key);
	}

	/**
	 * ��ȡkey���Ҹı�ԭ�е�value ���key������ �򴴽� GETSET is an atomic set this value and
	 * return the old value command.
	 */
	public String getSet(final String key,String value)
	{
		return jedis.getSet(key,value);
	}

	/** �鿴ĳ��key�Ƿ��Ѿ����� */
	public boolean exists(final String key)
	{
		return jedis.exists(key);
	}

	/**
	 * ��ȡָ����Χ��keys examples:
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
