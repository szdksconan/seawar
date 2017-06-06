package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Role;
import foxu.sea.kit.SeaBackKit;

/**
 * player�ڴ���� ÿ1��Сʱ���޸ĵ����ݱ�������ݿ� ����redis��ͬid������ɾ�� ÿ5���� ͬ�����ݵ�redis
 * �����������ȴ����ݿ������������ Ȼ���redisȡ��ͬid�����ݸ��� ���ݿ������ author:icetiger
 */
public class PlayerMemCache extends MemCache
{
	/** Ĭ�ϸı��б�����ݱ��С */
	public static final int ALL_PLAYER_SIZE=10000,CHANGE_PLAYER=300;
	/** ��Сʱ�޸Ĺ������ݸ��µ����ݿ� 2��ʱ����ò�Ҫ��������ʱ�� */
	public static final int PLAYER_DB_TIME=60*10,REDIS_TIME=60*5;
	/** ��ʼ���ĵ���sid */
	public static final int START_SID=1;
	
	CreatObjectFactory cfactory;

	public void setFactory(CreatObjectFactory factory)
	{
		cfactory=factory;
	}
	/** �ط��洢��ǰ���� */
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.PLAYER_REDIS);
		return changeListMap.size();
	}

	@Override
	public Player createObect()
	{
		Player player=(Player)Role.factory.newSample(START_SID);
		player.setId(uidkit.getPlusUid());
		player.setCreateTime(TimeKit.getSecondTime());
		player.setUpdateTime(TimeKit.getSecondTime());
		return player;
	}

	/** ��Ϸ�����������з������������ �����ݿ���� */
	public void init()
	{
		cacheMap=new IntKeyHashMap(ALL_PLAYER_SIZE);
		changeListMap=new IntKeyHashMap(CHANGE_PLAYER);
		// ����ʹ���ڴ�
		int time=TimeKit.getSecondTime();
		/** ��ѯ1�����������ݸĶ������ */
		String sql="SELECT * FROM players";
		// ���ݿ���������������
		Player player[]=(Player[])dbaccess.loadBySql(sql);
		if(player!=null)
		{
			for(int i=0,n=player.length;i<n;i++)
			{
				initNewField(player[i]);
				PlayerSave playerSave=new PlayerSave();
				playerSave.setData(player[i]);
				// �ƶ��·���
				player[i].checkService(TimeKit.getSecondTime());
				//���ùؿ��ӳɵȼ�
				player[i].setAllPointBuff();
				// ����ͷ����Ϣ
				player[i].parserHead();
				//��Ӽ���
				//player[i].resetAdjustment();
				SeaBackKit.resetPlayerSkill(player[i],cfactory);
				// �ӳǷ�����
				SeaBackKit.addAdjustment(player[i],player[i].getIsland()
					.getMainGroup());
				// ���ñ����ʱ��
				playerSave.setSaveTimeDB(time);
				playerSave.setSaveTimeRedis(time);
				cacheMap.put(player[i].getId(),playerSave);
			}
		}
//		// redis��ȡ����������ݽ��и���
//		Player playerRedis[]=jedisCache.loadPlayers();
//		if(playerRedis!=null)
//		{
//			for(int i=0,n=playerRedis.length;i<n;i++)
//			{
//				PlayerSave save=(PlayerSave)cacheMap.get(playerRedis[i]
//					.getId());
//				// ���ݿ����µ�player
//				if(save!=null)
//				{
//					save.setSaveTimeDB(time);
//					save.setSaveTimeRedis(time);
//					save.setData(playerRedis[i]);
//					// ���ǵ����ݼ���ı��б� �´�һ��洢
//					changeListMap.put(playerRedis[i].getId(),save);
//				}
//			}
//		}
		// ������ʱ�� ������redis��
//		TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);
	}
	/** ֻ��ȡ���� ������ı��б� */
	public Player loadPlayerOnly(String key)
	{
		synchronized(this)
		{
			PlayerSave data=(PlayerSave)cacheMap.get(Integer.parseInt(key));
			if(data!=null)
			{
				return data.getData();
			}
		}
		// Player player=(Player)dbaccess.load(key);
		// save(key,player);
		return null;
	}

	/** ͨ��ID ��ȡ������� ������ı��б� */
	public Player load(String key)
	{
		synchronized(this)
		{
			PlayerSave data=(PlayerSave)cacheMap.get(Integer.parseInt(key));
			if(data!=null)
			{
				data.setSaveTimeDB(TimeKit.getSecondTime());
				if(changeListMap.get(Integer.parseInt(key))==null)
				{
					changeListMap.put(Integer.parseInt(key),data);
				}
				return data.getData();
			}
		}
		// Player player=(Player)dbaccess.load(key);
		// save(key,player);
		return null;
	}
	
	/** ͨ��������� ��ȡ������� bool�Ƿ����ı��б� */
	public Player loadByName(String name,boolean bool)
	{
		// ����ı��б�
		synchronized(this)
		{
			Object object[]=cacheMap.valueArray();
			for(int i=0;i<object.length;i++)
			{
				PlayerSave data=(PlayerSave)object[i];
				Player player=data.getData();
				try
				{
					if(player.getName().equalsIgnoreCase(name))
					{
						if(bool)
						{
							if(changeListMap.get(player.getId())==null)
								changeListMap.put(player.getId(),data);
						}
						return player;
					}
				}
				catch(Exception e)
				{
					continue;
				}
			}
		}
		return null;
	}
	/** ��ȡ�ܹ���ֵ��ʯ */
	public int getMaxGems()
	{
		Object object[]=cacheMap.valueArray();
		int gems=0;
		for(int i=0;i<object.length;i++)
		{
			PlayerSave data=(PlayerSave)object[i];
			Player player=data.getData();
			gems+=player.getResources()[Resources.MAXGEMS];
		}
		return gems;
	}

	/** ��ȡ��� */
	public Player[] loads(String[] keys)
	{
		return null;
	}

	/** ���� */
	public synchronized void save(String key,Object data)
	{
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		PlayerSave save=new PlayerSave();
		save.setData((Player)data);
		save.setSaveTimeDB(time);
		save.setSaveTimeRedis(time);
		// �ڴ��м���
		cacheMap.put(Integer.parseInt(key),save);
		// �ı��б��������
		changeListMap.put(Integer.parseInt(key),save);
	}

	public void onTimer(TimerEvent e)
	{
		// TODO �Զ����ɷ������
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				PLAYER_DB_TIME,JedisMemCacheAccess.PLAYER_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
			collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
				REDIS_TIME,JedisMemCacheAccess.PLAYER_REDIS);
		}
	}

	@Override
	public void deleteCache(Object save)
	{
		// TODO �Զ����ɷ������
	}
	/** ��ʼ�������ӵ�������� */
	public void initNewField(Player player)
	{
		if(player.getAttributes(PublicConst.DEFAULT_PUSH_MARK)==null)
		{
			// ��ʼ��ÿ���ۿ����Ϳ���
			int tag=1;
			tag=tag<<PublicConst.DATE_OFF_PUSH;
			int iosSystem=player.getIsland().getIosSystem();
			iosSystem|=tag;
			tag=1;
			tag=tag<<PublicConst.ONLINE_AWARD_PUSH;
			iosSystem|=tag;
			player.getIsland().setIosSystem(iosSystem);
			player.setAttribute(PublicConst.DEFAULT_PUSH_MARK,"T");
		}
		// ���ݺ����ӵ����ͱ�ǲ�����Ҫ����������
		String initMark=player.getAttributes(PublicConst.ADD_INIT_PUSH);
		int[] newMarks={PublicConst.PEACE_TIME_PUSH,PublicConst.MEAL_TIME_ENERGY_PUSH,PublicConst.STATIONED_PUSH};
		IntList addMarks=new IntList(newMarks);
		int iosSystem=player.getIsland().getIosSystem();
		//ȥ���Ѿ���ʼ�����ı��
		if(initMark!=null)
		{
			String[] marks=TextKit.split(initMark,",");
			for(int i=0;i<marks.length;i++)
			{
				int mark=TextKit.parseInt(marks[i]);
				for(int j=0;j<addMarks.size();j++)
				{
					if(mark==addMarks.get(j))
					{
						addMarks.removeIndex(j);
						break;
					}
				}
			}
		}
		//ʣ�µ��±�Ǵ���
		StringBuffer newMarkStr=new StringBuffer();
		for(int i=0;i<addMarks.size();i++)
		{
			newMarkStr.append(","+addMarks.get(i));
			int tag=1;
			tag=tag<<addMarks.get(i);
			iosSystem|=tag;
		}
		//ȥ����ͷ�Ķ���
		if(initMark==null)
			player.setAttribute(PublicConst.ADD_INIT_PUSH,newMarkStr.deleteCharAt(0).toString());
		else
			player.setAttribute(PublicConst.ADD_INIT_PUSH,initMark+newMarkStr.toString());
		player.getIsland().setIosSystem(iosSystem);
	}
}
