package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.sea.order.Order;

/**
 * �ڴ���� ÿ1��Сʱ���޸ĵ����ݱ�������ݿ� ����redis��ͬid������ɾ�� ÿ5���� ͬ�����ݵ�redis �����������ȴ����ݿ������������
 * Ȼ���redisȡ��ͬid�����ݸ��� ���ݿ������ 
 * author:icetiger
 */
public class OrderMemCache extends MemCache
{
	/**�ط��洢��ǰ����*/
	public int saveAndExit()
	{
		return  changeListMap.size();
	}
	
	/**��������󷵻ؽ��мӹ��� Ҫ�ǵ�ͬ����һ�Զ��map����ȥ
	 * 
	 * ��������ͬ��
	 * 
	 * */
	public Order createObect()
	{
		// TODO �Զ����ɷ������
		return null;
	}
	
	public void init()
	{
		
	}

	public Object load(String key)
	{
		// TODO �Զ����ɷ������
		return null;
	}

	public Object[] loads(String[] keys)
	{
		// TODO �Զ����ɷ������
		return null;
	}

	public void save(String key,Object data)
	{
		// TODO �Զ����ɷ������
		
	}

	public void onTimer(TimerEvent e)
	{
		// TODO �Զ����ɷ������
		
	}

	@Override
	public void deleteCache(Object save)
	{
		// TODO �Զ����ɷ������
		
	}
}
