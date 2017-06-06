package foxu.sea.messgae;

import mustang.io.ByteBuffer;

/** 玩家系统邮件读取状态 */
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
	 * @param messageId 要设置的 messageId
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
	 * @param state 要设置的 state
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

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(messageId);
		data.writeByte(state);
	}

}
