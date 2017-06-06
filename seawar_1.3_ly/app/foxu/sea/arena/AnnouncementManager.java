//package foxu.sea.arena;
//
//import mustang.set.IntKeyHashMap;
//import mustang.timer.TimerCenter;
//import mustang.timer.TimerEvent;
//import mustang.timer.TimerListener;
//import mustang.util.TimeKit;
//import foxu.dcaccess.AnnouncementDBAccess;
//import foxu.dcaccess.CreatObjectFactory;
//import foxu.sea.announcement.Announcement;
//import foxu.sea.kit.JBackKit;
//
///***
// * ���������
// * @author lihongji
// *
// */
//public class AnnouncementManager implements TimerListener
//{
//	TimerEvent te;
//	public final static int ALL_ANNOUNCE=100;
//	/** �ȴ��ڴ��� û���ҵ�ȥ���ݿ��� �����ص��ڴ��� */
//	IntKeyHashMap cacheMap;
//	AnnouncementDBAccess dbaccess;
//	CreatObjectFactory objectFactory;
//	public void init()
//	{
//		cacheMap=new IntKeyHashMap(ALL_ANNOUNCE);
//		String sql="SELECT * FROM announce where etime>"+TimeKit.getSecondTime();
//		System.out.println(sql+"=======================");
//		// ���ݿ���������ʼ�����
//		Announcement announce[]=(Announcement[])dbaccess.loadBySql(sql);
//		if(announce!=null)
//		{
//			System.out.println(announce.length+"=========��ʼ��length========");
//			for(int i=0,n=announce.length;i<n;i++)
//			{
//				Announcement announcement=announce[i];
//				cacheMap.put(announcement.getId(),announcement);
//			}
//		}
//		System.out.println(cacheMap.size()+"======cacheMap=======");
//		te=new TimerEvent(this,"announcement",60*1000);
//		TimerCenter.getMinuteTimer().add(te);
//	}
//	/**�ж��Ƿ����������*/
//	public Announcement  getById(int  key)
//	{
//		Announcement announce=(Announcement)cacheMap.get(key);
//		if(announce==null) return null;
//		return announce;
//	}
//	public void  save(Announcement announcement)
//	{
//		cacheMap.put(announcement.getId(),announcement);
//		dbaccess.save(announcement);
//	}
//	public void delete(Announcement announcement)
//	{
//			dbaccess.delete(announcement);
//	}
//	/***
//	 * �õ����еĹ���
//	 * @return
//	 */
//	public Announcement[] getAllannounce()
//	{
//		if(cacheMap==null) return null;
//		Announcement[] ann=new Announcement[cacheMap.size()];
//		int[] key=cacheMap.keyArray();
//		for(int i=0;i<cacheMap.size();i++)
//		{
//			System.out.println(key[i]+":+++++++++++++++key[i]");
//			Announcement announce=(Announcement)cacheMap.get(key[i]);
//			ann[i]=announce;
//		}
//		return 	ann;
//	}
//	@Override
//	public void onTimer(TimerEvent timer)
//	{
//		if("announcement".equals(timer.getParameter()))
//		{
//			int timeNow=TimeKit.getSecondTime();
//			int[] key=cacheMap.keyArray();
//			for(int i=0;i<cacheMap.size();i++)
//			{
//				Announcement announce=(Announcement)cacheMap.get(key[i]);
//				if(timeNow>announce.getEndTime())
//				{
////					dbaccess.delete(announce);
//					cacheMap.remove(key[i]);
//				}
//				if(announce.getPermanent()!=1 && timeNow>=announce.getStartTime())
//				{
//					announce.setPermanent(1);
//					System.out.println("===============��ʱ=");
//					JBackKit.sendAnnouncement(objectFactory.getDsmanager().getSessionMap(),announce);
//				}
//					
//			}
//		}
//	}
//	
//	public void setDbaccess(AnnouncementDBAccess dbaccess)
//	{
//		this.dbaccess=dbaccess;
//	}
//	
//	public void setObjectFactory(CreatObjectFactory objectFactory)
//	{
//		this.objectFactory=objectFactory;
//	}
//	
//	
//}
