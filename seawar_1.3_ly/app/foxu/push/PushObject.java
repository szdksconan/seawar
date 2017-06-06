package foxu.push;

import foxu.sea.kit.SeaBackKit;

/**
 * push���󣬰���һ��push��Ϣ����Ҫ��������Ϣ
 * 
 * @author comeback
 */
public class PushObject implements Runnable
{

	/** ������ */
	private PushHandler handler;

	/** ���͵���Ϣ���� */
	private String message;

	/** Ҫ���͵��豸��ʶ */
	private String[] deviceTokens;

	/** �������������� */
	private String[] params;

	public PushObject(PushHandler handler,String[] deviceTokens,
		String message,String[] params)
	{
		this.handler=handler;
		this.message=message;
		this.params=params;
		this.deviceTokens=deviceTokens;
	}

	/**
	 * ��ȡ���ʹ�����
	 * 
	 * @return
	 */
	public PushHandler getHandler()
	{
		return this.handler;
	}

	/**
	 * ��ȡ��Ϣ����
	 * 
	 * @return
	 */
	public String getMessage()
	{
		return this.message;
	}

	/**
	 * ��ȡ���Ӳ���
	 * 
	 * @return
	 */
	public String[] getParams()
	{
		return this.params;
	}

	/**
	 * ��ȡ�豸��ʶ
	 * 
	 * @return
	 */
	public String[] getDeviceTokens()
	{
		return this.deviceTokens;
	}

	/**
	 * ִ������
	 */
	@Override
	public void run()
	{
		PushHandler hander=getHandler();
		SeaBackKit.log.info("---ios--do-push--");
		hander.push(getMessage(),getDeviceTokens(),getParams());
	}
}
