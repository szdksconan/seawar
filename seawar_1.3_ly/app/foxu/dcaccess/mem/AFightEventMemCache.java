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
	/** 1Сʱ�޸Ĺ������ݸ��µ����ݿ� */
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
		// �ڴ��м���
		cacheMap.put(Integer.parseInt(key),save);
		// �ı��б��������
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
		// ���ݿ���������������
		AllianceFightEvent event[]=(AllianceFightEvent[])dbaccess.loadBySql(sql);
		if(event!=null)
		{
			for(int i=0,n=event.length;i<n;i++)
			{
				AFightEventSave save=new AFightEventSave();
				save.setData(event[i]);
				// ���ñ����ʱ��
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
