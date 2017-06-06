package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.messgae.Message;

/**
 * �ʼ����ݷ�װ��
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
		// TODO �Զ����ɷ������
		return message;
	}

	@Override
	public int getId()
	{
		// TODO �Զ����ɷ������
		return message.getMessageId();
	}

	@Override
	public void setData(Object data)
	{
		// TODO �Զ����ɷ������
		message = (Message)data;
	}
}
