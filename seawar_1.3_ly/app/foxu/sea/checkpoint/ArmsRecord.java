package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;

/**
 * ������¼����
 * 
 * @author lhj
 * 
 */
public class ArmsRecord
{

	/** �ؿ���sid **/
	int sid;
	/** ������ **/
	int starNum;
	/** ��ս���� **/
	int challengTimes;
	/** ��սʱ�� **/
	int attackTime;
	/** ����ˢ�´��� **/
	int payCount;

	/** �ؿ���sid **/
	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		this.sid=sid;
	}
	/** ������ **/
	public int getStarNum()
	{
		return starNum;
	}

	public void setStarNum(int starNum)
	{
		this.starNum=starNum;
	}
	/** ��ս���� **/
	public int getChallengTimes()
	{
		return challengTimes;
	}

	public void setChallengTimes(int challengTimes)
	{
		this.challengTimes=challengTimes;
	}
	/** ��սʱ�� **/
	public int getAttackTime()
	{
		return attackTime;
	}

	public void setAttackTime(int attackTime)
	{
		this.attackTime=attackTime;
	}

	/** ����ˢ�´��� **/
	public int getPayCount()
	{
		return payCount;
	}

	public void setPayCount(int payCount)
	{
		this.payCount=payCount;
	}

	/** ���л���ǰ̨ **/
	public ByteBuffer showBytesWrite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeByte(starNum);
		data.writeByte(challengTimes);
		data.writeByte(payCount);
		return data;
	}
	/** ���л����ڴ� **/
	public ByteBuffer bytesWrite(ByteBuffer data)
	{
		data.writeInt(sid);
		data.writeByte(starNum);
		data.writeByte(challengTimes);
		data.writeByte(payCount);
		data.writeInt(attackTime);
		return data;
	}
	/** �����л� **/
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
