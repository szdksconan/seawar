package foxu.push;

/**
 * ���ʹ������ӿ�
 * 
 * @author comeback
 */
public interface PushHandler
{

	/**
	 * ������ݳ���
	 * 
	 * @param message
	 * @return
	 */
	public boolean checkMessageLength(String message);

	/**
	 * �����豸����
	 * 
	 * @param message ���͵���Ϣ����
	 * @param deviceToken �豸
	 * @param params
	 */
	public void push(String message,String deviceToken,String[] params);

	/**
	 * ����豸����
	 * 
	 * @param message
	 * @param deviceTokens
	 * @param params
	 */
	public void push(String message,String[] deviceTokens,String[] params);
}
