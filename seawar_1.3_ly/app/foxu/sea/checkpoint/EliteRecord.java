package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;

/***
 * 
 * 精英战场记录
 * @author lhj
 *
 */
public class EliteRecord extends ArmsRecord
{
	
	/** 序列化到前台 **/
	public ByteBuffer showBytesWriteElite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeByte(starNum);
		return data;
	}
	
	/** 序列化到内存 **/
	public ByteBuffer bytesWrite(ByteBuffer data)
	{
		data.writeInt(sid);
		data.writeByte(starNum);
		return data;
	}
	/** 反序列化 **/
	public ByteBuffer bytesRead(ByteBuffer data,IntKeyHashMap EliteRecord)
	{
		this.sid=data.readInt();
		this.starNum=data.readUnsignedByte();
		EliteRecord.put(this.sid,this);
		return data;
	}
	
}
