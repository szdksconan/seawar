package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;

/***
 * 
 * ��Ӣս����¼
 * @author lhj
 *
 */
public class EliteRecord extends ArmsRecord
{
	
	/** ���л���ǰ̨ **/
	public ByteBuffer showBytesWriteElite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeByte(starNum);
		return data;
	}
	
	/** ���л����ڴ� **/
	public ByteBuffer bytesWrite(ByteBuffer data)
	{
		data.writeInt(sid);
		data.writeByte(starNum);
		return data;
	}
	/** �����л� **/
	public ByteBuffer bytesRead(ByteBuffer data,IntKeyHashMap EliteRecord)
	{
		this.sid=data.readInt();
		this.starNum=data.readUnsignedByte();
		EliteRecord.put(this.sid,this);
		return data;
	}
	
}
