package foxu.sea.builds;

import mustang.io.ByteBuffer;

/** 兵力 */
public class Troop
{

	/** Serialization fileds 伤兵的话就是总的伤兵数量 */
	/** 兵力数量 */
	int num;
	/** 兵力Sid */
	int shipSid;

	/* methods */
	/** 增加数量 */
	public void addNums(int num)
	{
		if(num<0) return;
		this.num+=num;
	}

	/** 减少数量 */
	public int reduceNum(int num)
	{
		int reduceNum=0;
		if(num<0) return 0;
		reduceNum=num;
		if(this.num<=num)
		{
			reduceNum=this.num;
		}
		this.num-=num;
		if(this.num<0) this.num=0;
		return reduceNum;
	}
	public Object bytesRead(ByteBuffer data)
	{
		num=data.readInt();
		shipSid=data.readUnsignedShort();
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(num);
		data.writeShort(shipSid);
	}
	public void showBytesWrite(ByteBuffer data,int current)
	{
		data.writeInt(num);
		data.writeShort(shipSid);
	}

	/**
	 * @return num
	 */
	public int getNum()
	{
		return num;
	}

	/**
	 * @param num 要设置的 num
	 */
	public void setNum(int num)
	{
		this.num=num;
	}

	/**
	 * @return shipSid
	 */
	public int getShipSid()
	{
		return shipSid;
	}

	/**
	 * @param shipSid 要设置的 shipSid
	 */
	public void setShipSid(int shipSid)
	{
		this.shipSid=shipSid;
	}
}
