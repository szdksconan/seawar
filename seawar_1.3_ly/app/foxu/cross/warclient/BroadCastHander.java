package foxu.cross.warclient;

import mustang.io.ByteBuffer;
import mustang.thread.ThreadPoolExecutor;


/**
 * 对前台广播跨服活动状态
 * @author yw
 *
 */
public class BroadCastHander implements Runnable
{
	ClientWarManager cWarManager;
	
	ThreadPoolExecutor excutor;
	
	ByteBuffer data=new ByteBuffer();
	
	public ClientWarManager getcWarManager()
	{
		return cWarManager;
	}
	
	public void setcWarManager(ClientWarManager cWarManager)
	{
		this.cWarManager=cWarManager;
	}
	
	public void start()
	{
		excutor=new ThreadPoolExecutor();
		excutor.setLimit(1);
		excutor.init();
		excutor.execute(this);
	}
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(5*1000);
				cWarManager.broadCast(data);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}

}
