package foxu.sea.builds;

import mustang.io.ByteBuffer;

/** ���� */
public class Troop
{

	/** Serialization fileds �˱��Ļ������ܵ��˱����� */
	/** �������� */
	int num;
	/** ����Sid */
	int shipSid;

	/* methods */
	/** �������� */
	public void addNums(int num)
	{
		if(num<0) return;
		this.num+=num;
	}

	/** �������� */
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

	/** ������������л����ֽڻ����� */
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
	 * @param num Ҫ���õ� num
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
	 * @param shipSid Ҫ���õ� shipSid
	 */
	public void setShipSid(int shipSid)
	{
		this.shipSid=shipSid;
	}
}
