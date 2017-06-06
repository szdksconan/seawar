package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceFlag;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;

public class AllianceMemCache extends MemCache
{
	/** 1Сʱ�޸Ĺ������ݸ��µ����ݿ� */
	public static final int ALLIAN_DB_TIME=60*15,ALLIAN_REDIS_TIME=60*5;
	
	/** �����ṩ�� */
	CreatObjectFactory factory;
	
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.ALLIANCE_REDIS);
		return changeListMap.size();
	}

	public synchronized Object createObect()
	{
		Alliance alliance=new Alliance();
		alliance.setId(uidkit.getPlusUid());
		alliance.setCreate_at(TimeKit.getSecondTime());
		AllianceSave save=new AllianceSave();
		save.setData(alliance);
		save.setSaveTimeDB(TimeKit.getSecondTime());
		// �ڴ��м���
		cacheMap.put(alliance.getId(),save);
		// �ı��б��������
		changeListMap.put(alliance.getId(),save);
		alliance.setRankNum(cacheMap.size());
		return alliance;
	}

	public void deleteCache(Object save)
	{
		if(save==null) return;
		Alliance al=(Alliance)save;
		cacheMap.remove(al.getId());
		changeListMap.remove(al.getId());
	}

	public void init()
	{
		cacheMap=new IntKeyHashMap();
		changeListMap=new IntKeyHashMap();
		int time=TimeKit.getSecondTime();
		String sql="SELECT * FROM alliances";
		/**��������**/
		AllianceFlag.setGiveTheValue();
		// ���ݿ���������������
		Alliance alliance[]=(Alliance[])dbaccess.loadBySql(sql);
		if(alliance!=null)
		{
			for(int i=0,n=alliance.length;i<n;i++)
			{
				AllianceSave allianceSave=new AllianceSave();
				allianceSave.setData(alliance[i]);
				// ���ñ����ʱ��
				allianceSave.setSaveTimeDB(time);
				allianceSave.setSaveTimeRedis(time);
				cacheMap.put(alliance[i].getId(),allianceSave);
				//�����Զ��ƽ��᳤
				alliance[i].autoChangeMaster(factory);
				alliance[i].getFlag().randomAllianceFlag();
			}
		}
//		// redis��ȡ����������ݽ��и���
//		Alliance allianJedis[]=jedisCache.loadAllAlliance();
//		if(allianJedis!=null)
//		{
//			for(int i=0,n=allianJedis.length;i<n;i++)
//			{
//				AllianceSave save=(AllianceSave)cacheMap.get(allianJedis[i]
//					.getId());
//				// jedis���µ�fightevent
//				if(save!=null)
//				{
//					save.setSaveTimeDB(time);
//					save.setSaveTimeRedis(time);
//					save.setData(allianJedis[i]);
//					// ���ǵ����ݼ���ı��б� �´�һ��洢
//					changeListMap.put(allianJedis[i].getId(),save);
//					//�����Զ��ƽ��᳤
//					allianJedis[i].autoChangeMaster(factory);
//				}
//				// ���ݿ�û�д����� redis����
//				else
//				{
//					AllianceSave allianSave=new AllianceSave();
//					allianSave.setData(allianJedis[i]);
//					// ���ñ����ʱ��
//					allianSave.setSaveTimeDB(time);
//					allianSave.setSaveTimeRedis(time);
//					// ���ǵ����ݼ���ı��б� �´�һ��洢
//					changeListMap.put(allianJedis[i].getId(),allianSave);
//					cacheMap.put(allianJedis[i].getId(),allianSave);
//				}
//			}
//		}
		// ������ʱ�� ������redis��
//		TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);

	}

	/** ͨ��������� ��ȡ������� bool�Ƿ����ı��б� */
	public Alliance loadByName(String name,boolean bool)
	{
		// ����ı��б�
		synchronized(this)
		{
			Object object[]=cacheMap.valueArray();
			for(int i=0;i<object.length;i++)
			{
				AllianceSave data=(AllianceSave)object[i];
				Alliance alliance=data.getData();
				try
				{
					if(alliance.getName().equalsIgnoreCase(name))
					{
						if(bool)
						{
							if(changeListMap.get(alliance.getId())==null)
								changeListMap.put(alliance.getId(),data);
						}
						return alliance;
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

	/** keyΪ����id ����ȫ���� */
	public synchronized Object loadOnly(String key)
	{
		if(key==null||key.equals(""))return null;
		AllianceSave data=(AllianceSave)cacheMap.get(Integer.parseInt(key));
		if(data!=null)
		{
			return data.getData();
		}
		return null;
	}

	public synchronized Object load(String key)
	{
		if(key==null||key.equals(""))return null;
		AllianceSave data=(AllianceSave)cacheMap.get(Integer.parseInt(key));
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

	/** KEYΪ����ID */
	public synchronized void save(String key,Object data)
	{
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		AllianceSave save=new AllianceSave();
		save.setData((Alliance)data);
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
				ALLIAN_DB_TIME,JedisMemCacheAccess.ALLIANCE_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
//			collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
//				ALLIAN_REDIS_TIME,JedisMemCacheAccess.ALLIANCE_REDIS);
		}

	}

	
	public CreatObjectFactory getFactory()
	{
		return factory;
	}

	
	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}
	
}
