package foxu.sea.event;

import mustang.io.ByteBuffer;


/**存在玩家身上的事件*/
public class PlayerEvent extends Event
{
	/** 将对象的域序列化到字节缓存中 */
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
