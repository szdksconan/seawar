package foxu.sea.event;

import mustang.io.ByteBuffer;


/**����������ϵ��¼�*/
public class PlayerEvent extends Event
{
	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
	}
	
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		return this;
	}
}
