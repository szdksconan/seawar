package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.event.ChangeAdapter;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.datasave.FightEventSave;
import foxu.sea.Player;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

/**
 * �ڴ���� ÿ1��Сʱ���޸ĵ����ݱ�������ݿ� ����redis��ͬid������ɾ�� ÿ5���� ͬ�����ݵ�redis �����������ȴ����ݿ������������
 * Ȼ���redisȡ��ͬid�����ݸ��� ���ݿ������ author:icetiger
 */
public class FightEventMemCache extends MemCache
{
	/** Ĭ�ϸı��б�����ݱ��С */
	public static final int ALL_EVENT_SIZE=2000,CHANGE_EVENT=1000;
	/** 1Сʱ�޸Ĺ������ݸ��µ����ݿ� */
	public static final int EVENT_DB_TIME=60*10,EVENT_REDIS_TIME=60*5;
	/** �Ե���IDΪkey ����洢ս���¼����� */
	IntKeyHashMap eventMap=new IntKeyHashMap();

	PlayerMemCache playerMemCache;

	/** �ط��洢��ǰ���� */
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.FIGHT_EVENT_REDIS);
		return changeListMap.size();
	}

	/** ��һ�Զ�Ĺ�ϵ������� */
	public void setFightEvent()
	{
		Object[] object=cacheMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			FightEventSave save=(FightEventSave)object[i];
			FightEvent event=save.getData();
			if(event.getSourceIslandIndex()>=0)
				addFightEvent(event,event.getSourceIslandIndex());
			if(event.getAttackIslandIndex()>=0)
				addFightEvent(event,event.getAttackIslandIndex());
		}
	}

	/** �Ƴ�פ�ص���ı������¼� */
	public synchronized void removeHoldOnEvent(int islandIndex,
		int returnIndex,Player player)
	{
		if(eventMap.get(islandIndex)==null) return;
		ArrayList list=((FightEventList)eventMap.get(islandIndex))
			.getFightEventList();
		Object object[]=list.toArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			FightEvent ev=(FightEvent)object[i];
			if(ev.getSourceIslandIndex()!=islandIndex
				&&ev.getAttackIslandIndex()!=islandIndex
				&&ev.getAttackIslandIndex()==returnIndex)
			{
				list.remove(ev);
				JBackKit.deleteFightEvent(player,ev);
			}
		}
	}

	/** ��ȡĳ�����event�б� */
	public synchronized ArrayList getFightEventListById(int islandId)
	{
		if(eventMap.get(islandId)==null) return null;
		return ((FightEventList)eventMap.get(islandId)).getFightEventList();
	}

	/** ��һ�Զ�map�������event */
	public synchronized void addFightEvent(FightEvent event,int sourceId)
	{
		if(event.getDelete()==FightEvent.DELETE_TYPE)
		{
			// ����ı��б� �ȴ�ɾ��
			load(event.getId()+"");
			return;
		}
		if(event.getFleetGroup().nowTotalNum()<=0)
		{
			event.setDelete(FightEvent.DELETE_TYPE);
			// ����ı��б� �ȴ�ɾ��
			load(event.getId()+"");
			return;
		}
		FightEventList list=(FightEventList)eventMap.get(sourceId);
		// ���˾����
		if(list!=null)
		{
			list.addFightEvent(event);
			return;
		}
		// ��û�о͸���ȥ
		list=new FightEventList();
		list.addFightEvent(event);
		eventMap.put(sourceId,list);
	}
	/**
	 * ��������󷵻ؽ��мӹ��� Ҫ�ǵ�ͬ����һ�Զ��map����ȥ ��������ͬ��
	 */
	public FightEvent createObect()
	{
		// TODO �Զ����ɷ������
		FightEvent event=new FightEvent();
		event.setId(uidkit.getPlusUid());
		// FightEventSave save=new FightEventSave();
		// save.setData(event);
		// // save.setSaveTimeDB(TimeKit.getSecondTime());
		// // �ڴ��м���
		// cacheMap.put(event.getId(),save);
		// // �ı��б��������
		// changeListMap.put(event.getId(),save);
		return event;
	}
	public synchronized void saveEvent(FightEvent event)
	{
		if(event==null) return;
		FightEventSave save=new FightEventSave();
		save.setData(event);
		save.setSaveTimeDB(TimeKit.getSecondTime());
		cacheMap.put(event.getId(),save);
		// �ı��б��������
		changeListMap.put(event.getId(),save);
	}
	public void init()
	{
		cacheMap=new IntKeyHashMap(ALL_EVENT_SIZE);
		changeListMap=new IntKeyHashMap(CHANGE_EVENT);
		int time=TimeKit.getSecondTime();
		/** ��ѯ2���������ݸĶ������ */
		String sql="SELECT * FROM fight_events";
		// ���ݿ���������������
		FightEvent event[]=(FightEvent[])dbaccess.loadBySql(sql);
		if(event!=null)
		{
			for(int i=0,n=event.length;i<n;i++)
			{
				FightEventSave eventSave=new FightEventSave();
				eventSave.setData(event[i]);
				// ���ñ����ʱ��
				eventSave.setSaveTimeDB(time);
				eventSave.setSaveTimeRedis(time);
				cacheMap.put(event[i].getId(),eventSave);
			}
		}
//		// redis��ȡ����������ݽ��и���
//		FightEvent eventJedis[]=jedisCache.loadAllFightEvents();
//		if(eventJedis!=null)
//		{
//			for(int i=0,n=eventJedis.length;i<n;i++)
//			{
//				FightEventSave save=(FightEventSave)cacheMap
//					.get(eventJedis[i].getId());
//				// jedis���µ�fightevent
//				if(save!=null)
//				{
//					save.setSaveTimeDB(time);
//					save.setSaveTimeRedis(time);
//					save.setData(eventJedis[i]);
//					// ���ǵ����ݼ���ı��б� �´�һ��洢
//					changeListMap.put(eventJedis[i].getId(),save);
//				}
//				// ���ݿ�û�д����� redis����
//				else
//				{
//					FightEventSave eventSave=new FightEventSave();
//					eventSave.setData(eventJedis[i]);
//					// ���ñ����ʱ��
//					eventSave.setSaveTimeDB(time);
//					eventSave.setSaveTimeRedis(time);
//					// ���ǵ����ݼ���ı��б� �´�һ��洢
//					changeListMap.put(eventJedis[i].getId(),eventSave);
//					cacheMap.put(eventJedis[i].getId(),eventSave);
//				}
//			}
//		}
		// ����fleet������
		Object object[]=cacheMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			FightEventSave fightEventSave=(FightEventSave)object[i];
			FleetGroup fleetGroup=fightEventSave.getData().getFleetGroup();
			Player player=playerMemCache.loadPlayerOnly(fightEventSave
				.getData().getPlayerId()
				+"");
			if(player==null) continue;
			SeaBackKit.addAdjustment(player,fleetGroup);
		}
		setFightEvent();
		// ������ʱ�� ������redis��
//		TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public synchronized Object loadOnly(String key)
	{
		FightEventSave data=(FightEventSave)cacheMap.get(Integer
			.parseInt(key));
		if(data!=null)
		{
			return data.getData();
		}
		return null;
	}

	public synchronized Object load(String key)
	{
		FightEventSave data=(FightEventSave)cacheMap.get(Integer
			.parseInt(key));
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
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				EVENT_DB_TIME,JedisMemCacheAccess.FIGHT_EVENT_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
//			collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
//				EVENT_REDIS_TIME,JedisMemCacheAccess.FIGHT_EVENT_REDIS);
		}
	}

	/** �ڲ��� ĳ����ҵ��ʼ��б� */
	private class FightEventList extends ChangeAdapter
	{

		ArrayList fightEventList=new ArrayList();

		/** ����ʼ� */
		public void addFightEvent(FightEvent event)
		{
			event.addListener(this);
			fightEventList.add(event);
		}

		/**
		 * @return messageList
		 */
		public ArrayList getFightEventList()
		{
			return fightEventList;
		}

		/**
		 * @param messageList Ҫ���õ� messageList
		 */
		public void setFightEventList(ArrayList fightEventList)
		{
			this.fightEventList=fightEventList;
		}

		public void change(Object source,int type)
		{
			// TODO �Զ����ɷ������
			if(source instanceof FightEvent&&type==FightEvent.CHANGE_FINISH)
			{
				((FightEvent)source).removeListener(this);
				fightEventList.remove(source);
			}
		}

	}

	/**
	 * @return playerMemCache
	 */
	public PlayerMemCache getPlayerMemCache()
	{
		return playerMemCache;
	}

	/**
	 * @param playerMemCache Ҫ���õ� playerMemCache
	 */
	public void setPlayerMemCache(PlayerMemCache playerMemCache)
	{
		this.playerMemCache=playerMemCache;
	}

	@Override
	public void deleteCache(Object event)
	{
		// TODO �Զ����ɷ������
		if(event==null) return;
		FightEvent ev=(FightEvent)event;
		cacheMap.remove(ev.getId());
	}
}
