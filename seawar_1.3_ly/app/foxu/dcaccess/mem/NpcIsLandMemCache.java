package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.datasave.NpcIsLandSave;
import foxu.sea.NpcIsland;
import foxu.sea.island.SpaceIslandContainer;

/** �����ڴ���� */
public class NpcIsLandMemCache extends MemCache
{

	/** Ĭ�ϸı��б�����ݱ��С */
	public static final int ALL_ISLAND_SIZE=360000,CHANGE_ISLAND=500;
	/** 1Сʱ�޸Ĺ������ݸ��µ����ݿ� */
	public static final int ISLAND_DB_TIME=60*20,ISLAND_REDIS_TIME=60*5;
	/** playerid��Ӧ�ĵ��� */
	IntKeyHashMap islandMap=new IntKeyHashMap();
	/** �յ������� */
	SpaceIslandContainer islandContainer;

	/** �ط��洢��ǰ���� */
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.ISLAND_REDIS);

		return changeListMap.size();
	}

	public Object createObect()
	{
		return null;
	}

	/**
	 * ������ҵ�id���һ���յ�����
	 * 
	 * @return �����ҵ��Ŀյ���,����null��ʾû�пյ���
	 */
	public synchronized NpcIsland getRandomSpace()
	{
		NpcIsland island=islandContainer.getRandomSpace();
		if(island!=null)
		{
			NpcIsLandSave data=(NpcIsLandSave)cacheMap
				.get(island.getIndex());
			// changeListMap.put(island.getIndex(),data);
		}
		return island;
	}
	/** �Ƴ�һ������,���¼��������������������ܶ� */
	public void removeSpaceIsland(NpcIsland island)
	{
		islandContainer.removeSpaceIsland(island);
	}
	/** ���һ���յ���,���¼��������������������ܶ� */
	public void putSpaceIsland(NpcIsland island)
	{
		islandContainer.putSpaceIsland(island);
	}
	/** �����Ҷ�Ӧ�ĵ���index */
	public void setIslandMap()
	{
		Object[] object=cacheMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			NpcIsLandSave isLandSave=(NpcIsLandSave)object[i];
			NpcIsland island=isLandSave.getData();
			if(island.getPlayerId()!=0)
			{
				// �����ж�
				if(islandMap.get(island.getPlayerId())!=null)
				{
					island.setPlayerId(0);
					load(island.getIndex()+"");
				}
				else
					islandMap.put(island.getPlayerId(),island.getIndex());
			}
			islandContainer.addIsLand(island);
		}
		islandContainer.initDensity();
	}

	/** ��ȡ��ҵ�ǰ�ĵ���index */
	public synchronized int getPlayerIsLandId(int playerId)
	{
		if(islandMap.get(playerId)==null) return -1;
		return Integer.parseInt(islandMap.get(playerId).toString());
	}

	/** ����һ����ҵ��� */
	public synchronized void addPlayerIsLandMap(NpcIsland island)
	{
		if(island.getPlayerId()!=0)
			islandMap.put(island.getPlayerId(),island.getIndex());
	}

	/** �Ƴ�һ����ҵ��� Ǩ�� */
	public synchronized void removePlayerIslandMap(int playerId)
	{
		islandMap.remove(playerId);
	}

	/** ��ȡ��ҵ��첢����ı��б� */
	public NpcIsland getPlayerIsLandAndChange(int playerId)
	{
		return load(getPlayerIsLandId(playerId)+"");
	}

	/** ��ȡ��ҵ��� */
	public NpcIsland getPlayerIsland(int playerId)
	{
		return loadOnly(getPlayerIsLandId(playerId)+"");
	}

	public void init()
	{
		cacheMap=new IntKeyHashMap(ALL_ISLAND_SIZE);
		changeListMap=new IntKeyHashMap(CHANGE_ISLAND);
		// ʣ���ڴ�
		int time=TimeKit.getSecondTime();
		/** ��ѯ2���������ݸĶ������ */
		String sql="SELECT * FROM npc_islands";
		// ���ݿ���������������
		NpcIsland island[]=(NpcIsland[])dbaccess.loadBySql(sql);
		// String str="\r\n";
		if(island!=null)
		{
			for(int i=0,n=island.length;i<n;i++)
			{
				NpcIsLandSave isLandSave=new NpcIsLandSave();
				isLandSave.setData(island[i]);
				// ���ñ����ʱ��
				isLandSave.setSaveTimeDB(time);
				isLandSave.setSaveTimeRedis(time);
				cacheMap.put(island[i].getIndex(),isLandSave);
				// if(island[i].getPlayerId()!=0)
				// {
				// str+=island[i].getIndex()+":";
				// }
			}
			// // ��ӡ��������
			// File f=new File("filefan-2.txt");
			// try
			// {
			// f.createNewFile();
			// FileOutputStream fos=new FileOutputStream(f);
			// ObjectOutputStream oos=new ObjectOutputStream(fos);
			//
			// oos.writeObject(str);
			// oos.close();
			// }
			// catch(IOException e)
			// {
			// // TODO �Զ����� catch ��
			// e.printStackTrace();
			// }
		}
		// // redis��ȡ����������ݽ��и���
		// NpcIsland islandRedis[]=jedisCache.loadNpcIsLand();
		// if(islandRedis!=null)
		// {
		// for(int i=0,n=islandRedis.length;i<n;i++)
		// {
		// NpcIsLandSave save=(NpcIsLandSave)cacheMap
		// .get(islandRedis[i].getIndex());
		// // ���ݿ����µ�
		// if(save!=null)
		// {
		// save.setSaveTimeDB(time);
		// save.setSaveTimeRedis(time);
		// save.setData(islandRedis[i]);
		// // ���ǵ����ݼ���ı��б� �´�һ��洢
		// changeListMap.put(islandRedis[i].getIndex(),save);
		// }
		// }
		// }
		setIslandMap();
		// ������ʱ�� ������redis��
		// TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	/** keyΪ�����index */
	public synchronized NpcIsland load(String index)
	{
		NpcIsLandSave data=(NpcIsLandSave)cacheMap.get(Integer
			.parseInt(index));
		if(data!=null)
		{
			data.setSaveTimeDB(TimeKit.getSecondTime());
			if(changeListMap.get(Integer.parseInt(index))==null)
			{
				changeListMap.put(Integer.parseInt(index),data);
			}
			return data.getData();
		}
		return null;
	}

	/** keyΪ�����index */
	public synchronized NpcIsland loadOnly(String index)
	{
		NpcIsLandSave data=(NpcIsLandSave)cacheMap.get(Integer
			.parseInt(index));
		if(data!=null)
		{
			return data.getData();
		}
		return null;
	}

	public Object[] loads(String[] keys)
	{
		return null;
	}

	public void save(String key,Object data)
	{

	}

	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				ISLAND_DB_TIME,JedisMemCacheAccess.ISLAND_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
			// collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
			// ISLAND_REDIS_TIME,JedisMemCacheAccess.ISLAND_REDIS);
		}
	}

	@Override
	public void deleteCache(Object save)
	{
		// TODO �Զ����ɷ������

	}

}
