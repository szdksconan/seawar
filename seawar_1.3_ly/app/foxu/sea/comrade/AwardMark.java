package foxu.sea.comrade;

import mustang.io.ByteBuffer;


/**
 * 战友系统 领奖记录
 * @author yw
 *
 */
public class AwardMark
{
	/** 任务sid */
	int sid;
	/** 任务完成次数 */
	int complete;
	/** 奖励领取次数 */
	int got;
	
	/** 序列化写 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeShort(complete);
		data.writeShort(got);
	}
	/** 序列化读 */
	public void bytesRead(ByteBuffer data)
	{
		sid=data.readUnsignedShort();
		complete=data.readUnsignedShort();
		got=data.readUnsignedShort();
	}
	public int getComplete()
	{
		return complete;
	}
	
	public void setComplete(int complete)
	{
		this.complete=complete;
	}
	
	public int getGot()
	{
		return got;
	}
	
	public void setGot(int got)
	{
		this.got=got;
	}
	
	public int getSid()
	{
		return sid;
	}
	
	public void setSid(int sid)
	{
		this.sid=sid;
	}
	
}
