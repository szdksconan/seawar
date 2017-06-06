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
 * ���ݿⱸ��
 * 
 * @author comeback
 * 
 */
public class DatabaseBackup implements TimerListener,Runnable
{
	// ÿ���Ӽ��һ���Ƿ���Ҫ����
	public static final int CHECK_TIME=60*1000;
	
	private static Logger log=LogFactory.getLogger(DatabaseBackup.class);
	
	/** �Ƿ��������ݹ���  */
	boolean isEnabled=true;
	
	CreatObjectFactory objectFactory;
	
	/** �´α��ݵ�ʱ�� */
	int nextBackupTime=-1;
	
	String backupScriptPath;
	
	/** ����ʱ���ַ���,��ʱֻ�����㱸�� */
	String backupTimeString;
	
	/** ���ڽ��б��ݵı�־ */
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
	 * ���ñ���ʱ�䣬�������´α���ʱ��
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
	
	/** ���ݷ������ȱ������� */
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
			// ������ص���ֵ�в�Ϊ0�ģ��ظ��洢5�Σ��ȴ�30��
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
	 * �����´α���ʱ��,����趨�Ĺ�������ִ�б��ݣ��򽫱���ʱ������Ϊ-1
	 */
	private void resetNextBackupTime()
	{
		// ��ȡ������ı���ʱ��
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
		// �������ı���ʱ����ڵ�ǰʱ�䣬˵�����컹û�б��ݣ���Ҫ�ٱ��ݣ��������챸��
		if(todayBackupTime>nowTime)
			this.nextBackupTime=(int)(todayBackupTime/1000L);
		else
			this.nextBackupTime=((int)(todayBackupTime/1000L))+24*3600;
	}
	
	/**
	 * �Ƿ��������ݹ���
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
	
	/** ִ�б��ݣ��ڵ������߳��� */
	public void run()
	{
		if(backuping)
			return;
		backuping=true;
		backupAllDatabase();
		backuping=false;
	}

}
