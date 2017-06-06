package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;

/**
 * 军备记录对象
 * 
 * @author lhj
 * 
 */
public class ArmsRecord
{

	/** 关卡的sid **/
	int sid;
	/** 星星数 **/
	int starNum;
	/** 挑战次数 **/
	int challengTimes;
	/** 挑战时间 **/
	int attackTime;
	/** 付款刷新次数 **/
	int payCount;

	/** 关卡的sid **/
	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		this.sid=sid;
	}
	/** 星星数 **/
	public int getStarNum()
	{
		return starNum;
	}

	public void setStarNum(int starNum)
	{
		this.starNum=starNum;
	}
	/** 挑战次数 **/
	public int getChallengTimes()
	{
		return challengTimes;
	}

	public void setChallengTimes(int challengTimes)
	{
		this.challengTimes=challengTimes;
	}
	/** 挑战时间 **/
	public int getAttackTime()
	{
		return attackTime;
	}

	public void setAttackTime(int attackTime)
	{
		this.attackTime=attackTime;
	}

	/** 付款刷新次数 **/
	public int getPayCount()
	{
		return payCount;
	}

	public void setPayCount(int payCount)
	{
		this.payCount=payCount;
	}

	/** 序列化到前台 **/
	public ByteBuffer showBytesWrite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeByte(starNum);
		data.writeByte(challengTimes);
		data.writeByte(payCount);
		return data;
	}
	/** 序列化到内存 **/
	public ByteBuffer bytesWrite(ByteBuffer data)
	{
		data.writeInt(sid);
		data.writeByte(starNum);
		data.writeByte(challengTimes);
		data.writeByte(payCount);
		data.writeInt(attackTime);
		return data;
	}
	/** 反序列化 **/
	public ByteBuffer bytesRead(ByteBuffer data,IntKeyHashMap routeRecord)
	{
		this.sid=data.readInt();
		this.starNum=data.readUnsignedByte();
		this.challengTimes=data.readUnsignedByte();
		this.payCount=data.readUnsignedByte();
		this.attackTime=data.readInt();
		routeRecord.put(this.sid,this);
		return data;
	}
}
