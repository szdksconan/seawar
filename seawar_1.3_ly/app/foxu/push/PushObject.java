package foxu.push;

import foxu.sea.kit.SeaBackKit;

/**
 * push对象，包含一个push消息所需要的所有信息
 * 
 * @author comeback
 */
public class PushObject implements Runnable
{

	/** 处理器 */
	private PushHandler handler;

	/** 推送的消息内容 */
	private String message;

	/** 要推送的设备标识 */
	private String[] deviceTokens;

	/** 附带的其他参数 */
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
	 * 获取推送处理器
	 * 
	 * @return
	 */
	public PushHandler getHandler()
	{
		return this.handler;
	}

	/**
	 * 获取消息内容
	 * 
	 * @return
	 */
	public String getMessage()
	{
		return this.message;
	}

	/**
	 * 获取附加参数
	 * 
	 * @return
	 */
	public String[] getParams()
	{
		return this.params;
	}

	/**
	 * 获取设备标识
	 * 
	 * @return
	 */
	public String[] getDeviceTokens()
	{
		return this.deviceTokens;
	}

	/**
	 * 执行推送
	 */
	@Override
	public void run()
	{
		PushHandler hander=getHandler();
		SeaBackKit.log.info("---ios--do-push--");
		hander.push(getMessage(),getDeviceTokens(),getParams());
	}
}
