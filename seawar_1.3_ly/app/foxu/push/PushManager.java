package foxu.push;

import java.util.HashMap;
import java.util.Map;

import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.text.TextKit;
import mustang.thread.ThreadPoolExecutor;

/**
 * push ֪ͨ������<br />
 * ������ƽ̨���߲�ͬ�����ã�ʵ�ֶԲ�ͬƽ̨�Ͳ��ò�ͬ�����ý���push
 * 
 * @author comeback
 */
public class PushManager
{

	/* log */
	private static final Logger log=LogFactory.getLogger(PushManager.class);

	/* ��̬������ */
	protected static PushManager manager;

	/* ���ַ������� */
	public static String[] NULL={};

	/** ��������������� */
	public static int GROUP_MAX_COUNT=400;

	/** ��ȡ��ǰʵ�� */
	public static PushManager getInstance()
	{
		return PushManager.manager;
	}

	/** ���е�������Ϣ������ */
	Map<String,PushHandler> handlers=new HashMap<String,PushHandler>();

	/** ִ���̳߳� */
	ThreadPoolExecutor executor;

	public PushManager()
	{
		if(PushManager.manager!=null)
		{
			throw new RuntimeException("PushManager.manager is not null.");
		}
		executor=new ThreadPoolExecutor();
		executor.setLimit(100);
		executor.init();

		PushManager.manager=this;
	}

	/**
	 * ���һ�����ʹ�����
	 * 
	 * @param bundleId
	 * @param handler
	 */
	public void addPushHandler(String bundleId,PushHandler handler)
	{
		if(bundleId==null||handler==null) return;
		bundleId=bundleId.trim().toLowerCase();
		if(bundleId.isEmpty()) return;
		handlers.put(bundleId,handler);
	}

	/**
	 * ������Ϣ���Ե����豸
	 * 
	 * @param bundleId �����������û���ƽ̨
	 * @param deviceToken
	 * @param message
	 */
	public void push(String bundleId,String deviceToken,String message)
	{
		this.push(bundleId,new String[]{deviceToken},message,NULL);
	}
	
	/**
	 * ������Ϣ,�Զ���豸
	 * 
	 * @param bundleId �����������û���ƽ̨
	 * @param deviceToken
	 * @param message
	 */
	public void push(String bundleId,String[] deviceToken,String message)
	{
		this.push(bundleId,deviceToken,message,NULL);
	}

	/**
	 * ������Ϣ
	 * 
	 * @param bundleId
	 * @param deviceToken
	 * @param message
	 * @param params
	 */
	public void push(String bundleId,String deviceToken,String message,
		String[] params)
	{
		this.push(bundleId,new String[]{deviceToken},message,params);
	}

	public void push(String bundleId,String[] deviceTokens,String message,
		String[] params)
	{
		if(deviceTokens==null||deviceTokens.length==0)
		{
			if(log.isWarnEnabled())
				log.warn("device tokens is null.deviceTokens="
					+TextKit.toString(deviceTokens));
			return;
		}
		if(bundleId==null)
		{
			if(log.isWarnEnabled())
				log.warn("bundleId is null.bundleId="
					+bundleId);
			return;
		}
		bundleId=bundleId.trim().toLowerCase();
		if(bundleId.isEmpty()) return;
		PushHandler handler=handlers.get(bundleId);
		if(handler==null)
		{
			if(log.isWarnEnabled())
				log.warn("null handler.bundleId="+bundleId+",message="
					+message+",params="+TextKit.toString(params));
			return;
		}
		if(!handler.checkMessageLength(message))
		{
			if(log.isWarnEnabled())
				log.warn("message is too long to push.key="+bundleId
					+",message="+message+",params="+TextKit.toString(params));
			return;
		}

		// ����������󣬾ͷ���
		if(deviceTokens.length>GROUP_MAX_COUNT)
		{
			int groupCount=deviceTokens.length/GROUP_MAX_COUNT;
			if(deviceTokens.length%GROUP_MAX_COUNT!=0) groupCount++;
			for(int i=0;i<groupCount;i++)
			{
				int offset=i*GROUP_MAX_COUNT;
				int count=(offset+GROUP_MAX_COUNT)>deviceTokens.length?(deviceTokens.length-offset)
					:GROUP_MAX_COUNT;
				String[] tokens=new String[count];
				System.arraycopy(deviceTokens,offset,tokens,0,count);
				PushObject po=new PushObject(handler,deviceTokens,message,params);

				executor.execute(po);
			}
		}
		else
		{
			PushObject po=new PushObject(handler,deviceTokens,message,params);

			executor.execute(po);
		}
	}

}
