package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.sea.order.Order;

/**
 * 内存管理 每1个小时对修改的数据保存进数据库 并把redis相同id的数据删除 每5分钟 同步数据到redis 服务器启动先从数据库加载所有数据
 * 然后从redis取相同id的数据覆盖 数据库的数据 
 * author:icetiger
 */
public class OrderMemCache extends MemCache
{
	/**关服存储当前数据*/
	public int saveAndExit()
	{
		return  changeListMap.size();
	}
	
	/**创建完对象返回进行加工后 要记得同步到一对多的map里面去
	 * 
	 * 保持数据同步
	 * 
	 * */
	public Order createObect()
	{
		// TODO 自动生成方法存根
		return null;
	}
	
	public void init()
	{
		
	}

	public Object load(String key)
	{
		// TODO 自动生成方法存根
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
		
	}

	@Override
	public void deleteCache(Object save)
	{
		// TODO 自动生成方法存根
		
	}
}
