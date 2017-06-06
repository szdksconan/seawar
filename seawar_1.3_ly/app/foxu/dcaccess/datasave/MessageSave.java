package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.messgae.Message;

/**
 * 邮件数据封装类
 * message
 * author:icetiger
 * */
public class MessageSave extends ObjectSave
{
    Message message;
	@Override
	public ByteBuffer getByteBuffer()
	{
		if(message==null)return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		message.bytesWrite(bb);
		return bb;
	}

	@Override
	public Message getData()
	{
		// TODO 自动生成方法存根
		return message;
	}

	@Override
	public int getId()
	{
		// TODO 自动生成方法存根
		return message.getMessageId();
	}

	@Override
	public void setData(Object data)
	{
		// TODO 自动生成方法存根
		message = (Message)data;
	}
}
