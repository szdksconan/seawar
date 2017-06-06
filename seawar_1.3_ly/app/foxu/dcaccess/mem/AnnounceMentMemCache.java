package foxu.dcaccess.mem;

import jedis.JedisMemCacheAccess;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AnnouncementSave;
import foxu.sea.announcement.Announcement;
import foxu.sea.kit.JBackKit;

public class AnnounceMentMemCache extends MemCache
{

	/** 公告移除增加定时器 */
	TimerEvent eventANN=new TimerEvent(this,"ann",30000);
	CreatObjectFactory objectFactory;
	/** 1小时修改过的数据更新到数据库 */
	public static final int ANNOUNCEMENT_DB_TIME=60*15;

	@Override
	public Object load(String key)
	{
		AnnouncementSave save=(AnnouncementSave)cacheMap.get(Integer
			.parseInt(key));
		if(save==null) return null;
		return save.getData();
	}
	@Override
	public Object[] loads(String[] arg0)
	{
		return null;
	}
	@Override
	public synchronized void save(String key,Object data)
	{
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		AnnouncementSave save=new AnnouncementSave();
		save.setData((Announcement)data);
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
			JedisMemCacheAccess.AOUNNCEMENT_INFO);
		return changeListMap.size();
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				ANNOUNCEMENT_DB_TIME,JedisMemCacheAccess.AOUNNCEMENT_INFO);
		}
		else if(e.getParameter().equals("ann"))
		{
			Announcement[] ann=getAllAnnouncement();
			int timeNow=TimeKit.getSecondTime();
			for(int i=0;i<ann.length;i++)
			{
				if(timeNow>ann[i].getEndTime())
				{
					deleteCache(ann[i]);
					JBackKit.sendRemoveAnnouncement(objectFactory
						.getDsmanager().getSessionMap(),ann[i]);
				}
				if(timeNow-ann[i].getStartTime()>0
					&&timeNow-ann[i].getStartTime()<30
					&&ann[i].getPermanent()==0)
					JBackKit.sendAnnouncement(objectFactory.getDsmanager()
						.getSessionMap(),ann[i]);
			}
		}
	}

	@Override
	public void init()
	{
		cacheMap=new IntKeyHashMap();
		changeListMap=new IntKeyHashMap();
		int time=TimeKit.getSecondTime();
		int checkTimeDelete=time-60*60*24*30;
		/** 删除超过一个月的公告 */
		String deleteSql="DELETE FROM announce WHERE etime<"+checkTimeDelete;
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),deleteSql);
		String sql="SELECT * FROM announce where etime>"
			+TimeKit.getSecondTime();
		// 数据库加载所有公告数据
		Announcement announce[]=(Announcement[])dbaccess.loadBySql(sql);
		if(announce!=null)
		{
			for(int i=0,n=announce.length;i<n;i++)
			{
				AnnouncementSave announcement=new AnnouncementSave();
				announcement.setData(announce[i]);
				// 设置保存的时间
				announcement.setSaveTimeDB(time);
				cacheMap.put(announcement.getId(),announcement);
			}
		}
		TimerCenter.getMinuteTimer().add(eventDB);
		TimerCenter.getMinuteTimer().add(eventANN);
	}

	@Override
	public Object createObect()
	{
		return null;
	}

	@Override
	public void deleteCache(Object data)
	{
		if(data==null) return;
		Announcement announce=(Announcement)data;
		// 内存中移除
		cacheMap.remove(announce.getId());
	}

	/*** 得到所有的公告 */
	public Announcement[] getAllAnnouncement()
	{
		if(cacheMap==null) return null;
		Announcement[] ann=new Announcement[cacheMap.size()];
		int[] key=cacheMap.keyArray();
		for(int i=0;i<cacheMap.size();i++)
		{
			AnnouncementSave announce=(AnnouncementSave)cacheMap.get(key[i]);
			ann[i]=(Announcement)announce.getData();
		}
		return ann;
	}
	/** 通过id查找指定公告 */
	public synchronized Announcement getById(int id)
	{
		AnnouncementSave announce=(AnnouncementSave)cacheMap.get(id);
		if(announce==null) return null;
		changeListMap.put(announce.getId(),announce);
		return (Announcement)announce.getData();
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

}
