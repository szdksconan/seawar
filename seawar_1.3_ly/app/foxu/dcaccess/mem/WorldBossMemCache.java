package foxu.dcaccess.mem;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.WorldBossSave;
import foxu.ds.SWDSManager;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.PublicConst;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.worldboss.WorldBoss;
import jedis.JedisMemCacheAccess;
import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.Sample;
import mustang.util.TimeKit;

public class WorldBossMemCache extends MemCache
{

	/** 1Сʱ�޸Ĺ������ݸ��µ����ݿ� */
	public static final int WORLD_BOSS_DB_TIME=60*10;
	
	public static final int VIRTUAL_BOSS_SID=1;
	
	public static final int UP_LEVEL=2;
	
	/** boss����������<=FLUSH_COUNT ��ˢ�¸ߵ�boss*/
	public static final int FLUSH_COUNT=1;
	
	/** ��߻�ɱ�ȼ����¶��ټ�BOSS����ˢ�� */
	public static final int RANGE_LEVEL=4;
	/** ��ʱ��δ����ɱ(����) */
	public static final int HOUERS=12*3600;
	/** ��ʱ��δ����ɱ(��λ) */
	public static final int HOUERS_DIRECTION=6*3600;
	
	/** ���ݿⶨʱ�� */
	TimerEvent eventBoss=new TimerEvent(this,"bossflush",60*1000);
	/** boss��sids */
	public static final int[] BOSS_SIDS={13001,13002,13003,13004,13005,
		13006,13007,13008,13009,13010,13011,13012,13013,13014,13015,13016,
		13017,13018,13019,13020,13021,13022,13023,13024,13025,13026,13027,
		13028,13029,13030};

	NpcIsLandMemCache islandCache;

	SWDSManager manager;
	
	CreatObjectFactory objectFactory;
	
	public void setCreatObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	@Override
	public Object createObect()
	{
		return null;
	}
	
	public Object createObject(int sid)
	{
		return null;
	}

	@Override
	public void deleteCache(Object save)
	{
		// TODO �Զ����ɷ������

	}
	
	/**
	 * ���һ��BOSS
	 * @param boss
	 * @param show �Ƿ������ڵ�ͼ����ʾ
	 */
	private void addBoss(WorldBoss boss,boolean show)
	{
		int nowTime=TimeKit.getSecondTime();
		// ϵͳ����
		String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"boss_came");
		WorldBossSave save=new WorldBossSave();
		save.setData(boss);
		save.setSaveTimeRedis(TimeKit.getSecondTime());
		save.setSaveTimeDB(TimeKit.getSecondTime());
		boss.setLastTime(nowTime);
		// �ڴ��м���
		cacheMap.put(boss.getSid(),save);
		// �ı��б��������
		changeListMap.put(boss.getSid(),save);
		if(show)
		{
			NpcIsland island=islandCache.islandContainer.randomIsLand();
			if(island==null)
				throw new DataAccessException(0,"the world is full");
			island.updateSid(boss.getSid());
			islandCache.load(island.getIndex()+"");
			boss.setIndex(island.getIndex());
			boss.setProtectTime(nowTime+boss.getProtectTimeConfig());
			boss.setCreateTime(nowTime);
			boss.createFleetGroup();
			message=TextKit.replace(message,"%",boss.getBossLevel()+"");
			// ϵͳ����
			SeaBackKit.sendSystemMsg(manager,message);
			// ˢ��ǰ̨
			JBackKit.flushIsland(manager,island,objectFactory);
		}
	}
	
	/**
	 * ��ȡ�����BOSS�������洢��ߵȼ�������BOSS����ʾ��������������λ��index���洢
	 * @return
	 */
	private WorldBoss getVirtualBoss()
	{
		WorldBoss virtualBoss=(WorldBoss)load(String.valueOf(VIRTUAL_BOSS_SID));
		if(virtualBoss==null)
		{
			int nowTime=TimeKit.getSecondTime();
			virtualBoss=(WorldBoss)WorldBoss.factory.newSample(VIRTUAL_BOSS_SID);
			virtualBoss.setIndex(UP_LEVEL);
			WorldBossSave virtualBossSave=new WorldBossSave();
			virtualBossSave.setData(virtualBoss);
			virtualBossSave.setSaveTimeDB(nowTime);
			virtualBossSave.setSaveTimeRedis(nowTime);
			// �ڴ��м���
			cacheMap.put(virtualBoss.getSid(),virtualBossSave);
			// �ı��б��������
			changeListMap.put(virtualBoss.getSid(),virtualBossSave);
		}
		return virtualBoss;
	}
	
	/**
	 * ��ȡ��ˢ������߼�BOSS�ĵȼ�
	 * @return
	 */
	private int getLastBossLevel()
	{
		WorldBoss boss=getVirtualBoss();
		if(boss==null)
			return UP_LEVEL;
		return boss.getIndex();
	}
	
	/**
	 * ��ɱʱ����Ƿ�����BOSS
	 * @param killLevel
	 */
	public void addBossOnKill(WorldBoss killboss)
	{
		WorldBoss virtualBoss=getVirtualBoss();
		// ��ˢ��������ߵȼ���BOSS
		int lastLevel=virtualBoss.getIndex();
		// �����ɱ�Ĳ�����ߵȼ�����С1������ɱ���ͷ�����ˢ�µ���ߵȼ���������ݻ�ɱ�ȼ�����
		if(killboss.getBossLevel()<lastLevel-UP_LEVEL
			||killboss.getBeAttack()>FLUSH_COUNT) return;
		// �ӹ����в��ҵȼ���Ӧ��BOSS
		Sample[] samples=WorldBoss.factory.getSamples();
		for(int lv=lastLevel+1;lv<=killboss.getBossLevel()+UP_LEVEL;lv++)
		{
			for(int j=0;j<samples.length;j++)
			{
				if(samples[j]==null||samples[j].getSid()==VIRTUAL_BOSS_SID)
					continue;
				if(!(samples[j] instanceof WorldBoss))continue;
				WorldBoss boss=(WorldBoss)samples[j];
				// ����ȼ����ϣ��һ�û��ˢ
				if(boss.getBossLevel()==lv&&cacheMap.get(boss.getSid())==null)
				{
					addBoss(boss,false);
					// ��������BOSS��indexΪ�µĵȼ�
					virtualBoss.setIndex(lv);
				}
			}
		}
	}

	/** ���boss�Ƿ���Ҫˢ�� */
	public void checkFlushBoss()
	{
		int nowTime=TimeKit.getSecondTime();
		// ��ߵȼ�BOSS
		int topLevel=getLastBossLevel();
		for(int i=0;i<BOSS_SIDS.length;i++)
		{
			WorldBossSave bossSave=(WorldBossSave)cacheMap.get(BOSS_SIDS[i]);
			if(bossSave!=null)
			{
				WorldBoss boss=(WorldBoss)bossSave.getData();
				int nowNum=boss.getFleetNowNum();
				if(boss.getIndex()==0||nowNum==0)
				{
					if(boss.getBossLevel()<topLevel-RANGE_LEVEL)continue;
					if(boss.getLastTime()==0)
						boss.setLastTime(TimeKit.getSecondTime());
					// ���ˢ��ʱ�� ����һ�α�����ʼ����
					int flushTime=boss.getFlushTime();
					// ���ݻ����״̬���¼���ˢ��ʱ��
					flushTime=ActivityContainer.getInstance().resetActivityBossFlush(boss.getLastTime(),flushTime);
					if((boss.getLastTime()+flushTime)>nowTime)
						continue;
					NpcIsland island=islandCache.islandContainer
						.randomIsLand();
					if(island==null)
						throw new DataAccessException(0,"the world is full");
					if(island.getPlayerId()>0) island.setPlayerId(0);
					island.updateSid(boss.getSid());
					boss.setIndex(island.getIndex());
					islandCache.load(island.getIndex()+"");
					boss.setProtectTime(nowTime+boss.getProtectTimeConfig());
					boss.setCreateTime(nowTime);
					boss.getHurtList().clear();
					boss.createFleetGroup();
					boss.setBeAttack(0);
					// ϵͳ����
					String message=InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"boss_came");
					message=TextKit.replace(message,"%",boss.getBossLevel()
						+"");
					SeaBackKit.sendSystemMsg(manager,message);
					// ˢ��ǰ̨
					JBackKit.flushIsland(manager,island,objectFactory);
				}
				else
				{
					// ��鱣��ʱ���Ƿ����
					if(boss.getProtectTime()>0)
					{
						if(boss.getProtectTime()<=TimeKit.getSecondTime())
						{
							JBackKit.sendPlayerIslandState(manager
								.getSessionMap(),0,boss.getIndex());
							boss.setProtectTime(0);
						}
					}
				}
			}
			// ���BOSS�����ڣ��ж�����ȼ���ˢ������ߵȼ��ͣ��ͼӽ�ȥ������ˢ������ߵȼ����ڻ�ɱ��ˢ
			else
			{
				WorldBoss boss=(WorldBoss)WorldBoss.factory
					.newSample(BOSS_SIDS[i]);
				if(boss.getBossLevel()<=topLevel&&boss.getBossLevel()>=topLevel-RANGE_LEVEL)
					addBoss(boss,true);
			}
		}
	}
	/** ��ʱ�䲻ɱBOSS���� */
	public void bossTaunt()
	{
		for(int i=0;i<BOSS_SIDS.length;i++)
		{
			WorldBossSave bossSave=(WorldBossSave)cacheMap.get(BOSS_SIDS[i]);
			if(bossSave!=null)
			{
				WorldBoss boss=(WorldBoss)bossSave.getData();
				int nowNum=boss.getFleetNowNum();
				if(boss.getIndex()==0||nowNum==0) continue;
				int createTime=boss.getCreateTime();
				int nowTime=TimeKit.getSecondTime();

				if(nowTime-createTime>=HOUERS
					&&(nowTime-createTime)%HOUERS<60)
				{
					String message=InterTransltor.getInstance()
						.getTransByKey(PublicConst.SERVER_LOCALE,
							"boss_taunt");
					message=TextKit.replace(message,"%",String.valueOf(boss.getBossLevel()));
					message=TextKit.replace(message,"%",SeaBackKit.getIslandLocation(boss.getIndex()));
					SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
						message);
				}

			}
		}

	}
	/** ��ȡBOSS��Ϣ */
	public void getBossInfo(ByteBuffer data)
	{
		data.clear();
		int oldTop=data.top();
		data.writeByte(0);
		int len=0;
		for(int i=0;i<BOSS_SIDS.length;i++)
		{
			WorldBossSave bossSave=(WorldBossSave)cacheMap.get(BOSS_SIDS[i]);
			if(bossSave!=null)
			{
				WorldBoss boss=(WorldBoss)bossSave.getData();
				int nowNum=boss.getFleetNowNum();
				if(boss.getIndex()==0||nowNum==0) continue;
				len++;
				data.writeShort(boss.getSid());
				if(TimeKit.getSecondTime()-boss.getCreateTime()>=HOUERS)
				{
					data.writeByte(2);
				}else if(TimeKit.getSecondTime()-boss.getCreateTime()>=HOUERS_DIRECTION)
				{
					data.writeByte(1);
				}else{
					data.writeByte(0);
				}
				data.writeShort(boss.getIndex()%600);
				data.writeShort(boss.getIndex()/600);

			}
		}
		int nowTop=data.top();
		data.setTop(oldTop);
		data.writeByte(len);
		data.setTop(nowTop);
		
	}
	
	public void init()
	{
		cacheMap=new IntKeyHashMap();
		changeListMap=new IntKeyHashMap();
		int time=TimeKit.getSecondTime();
		String sql="SELECT * FROM world_boss";
		// ���ݿ���������ʼ�����
		WorldBoss worldBoss[]=(WorldBoss[])dbaccess.loadBySql(sql);
		if(worldBoss!=null)
		{
			for(int i=0,n=worldBoss.length;i<n;i++)
			{
				WorldBossSave worldBossSave=new WorldBossSave();
				worldBossSave.setData(worldBoss[i]);
				// ���ñ����ʱ��
				worldBossSave.setSaveTimeDB(time);
				cacheMap.put(worldBoss[i].getSid(),worldBossSave);
			}
		}
		checkBoss_Island();
		TimerCenter.getMinuteTimer().add(eventDB);
		TimerCenter.getMinuteTimer().add(eventBoss);
	}
	
	/** ���isLand-boss�Ķ�Ӧ��ϵ */
	public void checkBoss_Island()
	{
		for(int i=0;i<BOSS_SIDS.length;i++)
		{
			WorldBossSave bossSave=(WorldBossSave)cacheMap.get(BOSS_SIDS[i]);
			if(bossSave!=null)
			{
				WorldBoss boss=(WorldBoss)bossSave.getData();
				int nowNum=boss.getFleetNowNum();
				if(boss.getIndex()==0||nowNum==0) continue;
				NpcIsland boss_island=islandCache.load(boss.getIndex()+"");
				if(boss_island.getSid()!=boss.getSid())
				{
					NpcIsland island=islandCache.islandContainer.randomIsLand();
					if(island==null)
					{
						cacheMap.remove(BOSS_SIDS[i]);
					}
					else
					{
						if(island.getPlayerId()>0) island.setPlayerId(0);
						island.updateSid(boss.getSid());
						boss.setIndex(island.getIndex());
						islandCache.load(island.getIndex()+"");
						boss.setProtectTime(boss.getProtectTime());
						boss.setCreateTime(boss.getCreateTime());
					}
				}
			}
		}
	}	
	/** keyΪsid npc_islands��Ӧboss��sid */
	public Object load(String key)
	{
		WorldBossSave data=(WorldBossSave)cacheMap
			.get(Integer.parseInt(key));
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

	}

	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.WORLD_BOSS);
		return changeListMap.size();
	}

	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				WORLD_BOSS_DB_TIME,JedisMemCacheAccess.WORLD_BOSS);
		}
		else if(e.getParameter().equals("bossflush"))
		{
			checkFlushBoss();
			checkBoss_Island();
			bossTaunt();
		}

	}

	public NpcIsLandMemCache getIslandCache()
	{
		return islandCache;
	}

	public void setIslandCache(NpcIsLandMemCache islandCache)
	{
		this.islandCache=islandCache;
	}

	public SWDSManager getManager()
	{
		return manager;
	}

	public void setManager(SWDSManager manager)
	{
		this.manager=manager;
	}

}
