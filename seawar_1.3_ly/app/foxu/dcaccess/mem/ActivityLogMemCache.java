package foxu.dcaccess.mem;

import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import shelby.dc.GameDBAccess;
import foxu.sea.activity.ActivityLog;


public class ActivityLogMemCache implements TimerListener
{

	public static int TIME_OUT=2*30*24*3600;
	private static ActivityLogMemCache instance;
	/** 定时检测 */
	private int intervalTime=60*1000;
//	private int intervalTime=2*1000;
	/** 存储队列元素 */
	ArrayList loglist=new ArrayList();
	/** 存储队列 */
	ArrayList quelist=new ArrayList();
	/** 单个队列元素最大长度 */
	private int max=5000;
	/** 一次入库最大长度 */
	private int one_max=500;
	/** 超时强制存储 （次数）*/
	private int cmax=15;
	/** 当前累计次数 */
	private int count;
	
	/** 数据库操作中心 */
	GameDBAccess dbaccess;

	Logger log=LogFactory.getLogger(ActivityLogMemCache.class);
	Object slock=new Object();

	public static ActivityLogMemCache getInstance()
	{
		return instance;
	}
	public void init()
	{
		instance=this;
		clear();
		TimerCenter.getMinuteTimer().add(
			new TimerEvent(this,"save",intervalTime));
	}
	public void clear()
	{
		String sql="delete from activitylog where create_at<"
			+(TimeKit.getSecondTime()-TIME_OUT);
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),sql);
	}
	public void collectAlog(int aid,String sid,int pid,int gems)
	{
		synchronized(loglist)
		{
			loglist.add(new ActivityLog(aid,sid,pid,gems));
			if(loglist.size()>=max)
			{
				quelist.add(loglist);
				loglist=new ArrayList();
			}
		}
	}

	public boolean checkSave()
	{
		synchronized(loglist)
		{
			boolean save=false;
			if(quelist.size()>0) save=true;
			if(loglist.size()<=0) return save;
			count++;
			if(count<cmax&&quelist.size()<max) return save;
			quelist.add(loglist);
			loglist=new ArrayList();
			count=0;
			return true;
		}
	}
	public void save()
	{
		synchronized(slock)
		{
//			System.out.println("---------------------0-------------");
			if(quelist.size()<=0) return;
			ArrayList savelist=(ArrayList)quelist.remove(0);
			StringBuffer sql=new StringBuffer();
			ArrayList templist=new ArrayList();

			sql.append("insert into activitylog (aid,sid,pid,gems,create_at) values ");
			int len=sql.length();
			for(int i=0;i<savelist.size();)
			{
				ActivityLog alog=(ActivityLog)savelist.get(i);
				i++;
				sql.append("("+alog.getAid()+",'"+alog.getSid()+"',"
					+alog.getPid()+","+alog.getGems()+","+alog.getCreate_at()+")");
				templist.add(alog);
				if(!(i%one_max==0||i==savelist.size()))
				{
					sql.append(",");
				}
				else
				{
//					System.out.println("------------save--------i----------:::"+i);
					// 执行sql
					SqlPersistence sp=(SqlPersistence)dbaccess
						.getGamePersistence();
					try
					{
						SqlKit.execute(sp.getConnectionManager(),
							sql.toString());
					}
					catch(Exception e)
					{
						e.printStackTrace();// test
						quelist.add(templist);
						templist=new ArrayList();
						log.warn("ActivityLogDBAccess save valid, db error:"
							+i);
						sql.delete(len,sql.length());
//						System.out.println("------------save--erro------i----------:::"+i);
						continue;
					}
					sql.delete(len,sql.length());
					templist.clear();
				}
			}

		}
	}

	public int saveAndExit()
	{
		synchronized(loglist)
		{
			if(loglist.size()>0)
			{
				quelist.add(loglist);
				loglist=new ArrayList();
			}
		}
		for(int i=0;i<quelist.size();i++)
		{
			save();
		}
		return quelist.size();
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(checkSave()) save();
	}

	public GameDBAccess getDbaccess()
	{
		return dbaccess;
	}

	public void setDbaccess(GameDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}

	public ArrayList getLoglist()
	{
		return loglist;
	}

	public void setLoglist(ArrayList loglist)
	{
		this.loglist=loglist;
	}

	public ArrayList getQuelist()
	{
		return quelist;
	}

	public void setQuelist(ArrayList quelist)
	{
		this.quelist=quelist;
	}



}
