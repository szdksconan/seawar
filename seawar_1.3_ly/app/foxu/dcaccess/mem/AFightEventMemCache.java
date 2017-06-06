package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import foxu.dcaccess.datasave.AFightEventSave;
import foxu.sea.alliance.alliancefight.AllianceFightEvent;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;


public class AFightEventMemCache extends MemCache
{
	/** 1小时修改过的数据更新到数据库 */
	public static final int AFIGHT_DB_TIME=60*15;
	@Override
	public Object load(String key)
	{
		AFightEventSave save=(AFightEventSave)cacheMap.get(Integer.parseInt(key));
		if(save==null)return null;
		return save.getData();
	}

	@Override
	public Object[] loads(String[] arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized void save(String key,Object data)
	{
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		AFightEventSave save=new AFightEventSave();
		save.setData((AllianceFightEvent)data);
		save.setSaveTimeDB(time);
		save.setSaveTimeRedis(time);
		// 内存中加入
		cacheMap.put(Integer.parseInt(key),save);
		// 改变列表里面加入
		changeListMap.put(Integer.parseInt(key),save);
	}

	@Override
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.ALLIANCE_FIGHT_EVENT);
		return changeListMap.size();
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				AFIGHT_DB_TIME,JedisMemCacheAccess.ALLIANCE_FIGHT_EVENT);
		}
		
	}

	@Override
	public void init()
	{
		cacheMap=new IntKeyHashMap();
		changeListMap=new IntKeyHashMap();
		int time=TimeKit.getSecondTime();
		String sql="SELECT * FROM afightevent";
		// 数据库加载所有玩家数据
		AllianceFightEvent event[]=(AllianceFightEvent[])dbaccess.loadBySql(sql);
		if(event!=null)
		{
			for(int i=0,n=event.length;i<n;i++)
			{
				AFightEventSave save=new AFightEventSave();
				save.setData(event[i]);
				// 设置保存的时间
				save.setSaveTimeDB(time);
				save.setSaveTimeRedis(time);
				cacheMap.put(save.getId(),save);
			}
		}
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	@Override
	public Object createObect()
	{
		AllianceFightEvent event=null;
		synchronized(this)
		{
			event=new AllianceFightEvent();
			event.setUid(getUidkit().getPlusUid());
		}
		return event;
	}

	@Override
	public void deleteCache(Object save)
	{
		if(save==null) return;
		AllianceFightEvent al=(AllianceFightEvent)save;
		cacheMap.remove(al.getUid());
		changeListMap.remove(al.getUid());
		
	}

}
