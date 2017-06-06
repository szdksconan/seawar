package foxu.sea.messgae;

import mustang.io.ByteBuffer;

/** ���ϵͳ�ʼ���ȡ״̬ */
public class SystemMessageState
{

	int messageId;

	int state;

	/**
	 * @return messageId
	 */
	public int getMessageId()
	{
		return messageId;
	}

	/**
	 * @param messageId Ҫ���õ� messageId
	 */
	public void setMessageId(int messageId)
	{
		this.messageId=messageId;
	}

	/**
	 * @return state
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * @param state Ҫ���õ� state
	 */
	public void setState(int state)
	{
		this.state=state;
	}
	
	public Object bytesRead(ByteBuffer data)
	{
		messageId = data.readInt();
		state = data.readUnsignedByte();
		return this;
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(messageId);
		data.writeByte(state);
	}

}
