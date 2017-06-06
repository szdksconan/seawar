package foxu.backup;

import java.util.Calendar;

import foxu.dcaccess.CreatObjectFactory;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.text.TextKit;
import mustang.thread.ThreadPoolExecutor;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * 数据库备份
 * 
 * @author comeback
 * 
 */
public class DatabaseBackup implements TimerListener,Runnable
{
	// 每分钟检查一次是否需要备份
	public static final int CHECK_TIME=60*1000;
	
	private static Logger log=LogFactory.getLogger(DatabaseBackup.class);
	
	/** 是否启动备份功能  */
	boolean isEnabled=true;
	
	CreatObjectFactory objectFactory;
	
	/** 下次备份的时间 */
	int nextBackupTime=-1;
	
	String backupScriptPath;
	
	/** 备份时间字符串,暂时只能整点备份 */
	String backupTimeString;
	
	/** 正在进行备份的标志 */
	volatile boolean backuping=false;
	
	/***/
	ThreadPoolExecutor excutor;
	
	public void start()
	{
		excutor=new ThreadPoolExecutor();
		excutor.setLimit(2);
		excutor.init();
		TimerEvent timer=new TimerEvent(this,"backup",CHECK_TIME);
		TimerCenter.getMinuteTimer().add(timer);
	}
	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
	/**
	 * 设置备份时间，并重置下次备份时间
	 * @param str
	 */
	public void setBackupTime(String str)
	{
		this.backupTimeString=str;
		resetNextBackupTime();
	}
	
	public void setBackupScriptPath(String fileName)
	{
		this.backupScriptPath=fileName;
	}
	
	/** 备份方法，先保存数据 */
	private void backupAllDatabase()
	{
		int save=0;
		int c=0;
		do
		{
			c=0;
			save++;
			int[] res=objectFactory.saveAndExit(false);
			for(int i=0;i<res.length;i++)
				c+=res[i];
			// 如果返回的数值有不为0的，重复存储5次，等待30秒
			try
			{
				log.info("some data not save=============try:"+(save-1));
				Thread.sleep(30*1000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}while(c>0&&save<5);
//		try
//		{
//			Runtime.getRuntime().exec(backupScriptPath);
//		}
//		catch(IOException e)
//		{
//			log.error("database backup failed."+e.getLocalizedMessage());
//			e.printStackTrace();
//		}
	}
	
	/**
	 * 重设下次备份时间,如果设定的规则不能再执行备份，则将备份时间设置为-1
	 */
	private void resetNextBackupTime()
	{
		// 获取到今天的备份时间
		Calendar cal=Calendar.getInstance();
		long nowTime=TimeKit.getMillisTime();
		cal.setTimeInMillis(nowTime);
		String[] time=TextKit.split(backupTimeString,",");
		int hour=0;
		int minute=0;
		int second=0;
		if(time.length>0&&!time[0].equals("*"))
			hour=Integer.parseInt(time[0]);
		if(time.length>1&&!time[1].equals("*"))
			minute=Integer.parseInt(time[1]);
		if(time.length>2&&!time[2].equals("*"))
			second=Integer.parseInt(time[2]);
		cal.set(Calendar.HOUR_OF_DAY,hour);
		cal.set(Calendar.MINUTE,minute);
		cal.set(Calendar.SECOND,second);
		cal.set(Calendar.MILLISECOND,0);
		long todayBackupTime=cal.getTimeInMillis();
		// 如果今天的备份时间大于当前时间，说明今天还没有备份，需要再备份，否则明天备份
		if(todayBackupTime>nowTime)
			this.nextBackupTime=(int)(todayBackupTime/1000L);
		else
			this.nextBackupTime=((int)(todayBackupTime/1000L))+24*3600;
	}
	
	/**
	 * 是否启动备份功能
	 * @return
	 */
	public boolean isEnabled()
	{
		return this.isEnabled;
	}
	
	public void setEnabled(boolean bool)
	{
		this.isEnabled=bool;
	}
	
	public void onTimer(TimerEvent e)
	{
		int nowTime=TimeKit.getSecondTime();
		if(!backuping&&nextBackupTime>=0&&nowTime>nextBackupTime)
		{
			resetNextBackupTime();
			excutor.execute(this);
		}
	}
	
	/** 执行备份，在单独的线程中 */
	public void run()
	{
		if(backuping)
			return;
		backuping=true;
		backupAllDatabase();
		backuping=false;
	}

}
