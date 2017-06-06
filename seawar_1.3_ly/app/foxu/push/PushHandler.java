package foxu.push;

/**
 * 推送处理器接口
 * 
 * @author comeback
 */
public interface PushHandler
{

	/**
	 * 检查内容长度
	 * 
	 * @param message
	 * @return
	 */
	public boolean checkMessageLength(String message);

	/**
	 * 单个设备推送
	 * 
	 * @param message 推送的消息内容
	 * @param deviceToken 设备
	 * @param params
	 */
	public void push(String message,String deviceToken,String[] params);

	/**
	 * 多个设备推送
	 * 
	 * @param message
	 * @param deviceTokens
	 * @param params
	 */
	public void push(String message,String[] deviceTokens,String[] params);
}
