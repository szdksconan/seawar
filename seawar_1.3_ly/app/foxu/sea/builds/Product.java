package foxu.sea.builds;

import mustang.io.ByteBuffer;

/**
 * 正在生产中的产品
 * 
 * @author rockzyt
 */
public class Product implements Cloneable
{
	/* fields */
	/** 产品sid */
	public int sid;
	/** 产品数量 */
	public int num;
	/** 完成时间点 */
	int finishTime;
	/** 此队列产品需要的时间 */
	int produceTime;

	public void showBytesWrite(ByteBuffer data,int current)
	{
		data.writeShort(sid);
		data.writeShort(num);
		data.writeInt(produceTime);
		data.writeInt(finishTime-current);
	}
	
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeShort(num);
		data.writeInt(produceTime);
		data.writeInt(finishTime);
	}
	public void bytesRead(ByteBuffer data)
	{
		sid=data.readUnsignedShort();
		num=data.readUnsignedShort();
		produceTime=data.readInt();
		finishTime=data.readInt();
	}

	/**
	 * @return produceTime
	 */
	public int getProduceTime()
	{
		return produceTime;
	}

	/**
	 * @param produceTime 要设置的 produceTime
	 */
	public void setProduceTime(int produceTime)
	{
		this.produceTime=produceTime;
	}

	/**
	 * @return finlishTime
	 */
	public int getFinishTime()
	{
		return finishTime;
	}

	/**
	 * @param finlishTime 要设置的 finlishTime
	 */
	public void setFinishTime(int finlishTime)
	{
		this.finishTime=finlishTime;
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
	 * @return sid
	 */
	public int getSid()
	{
		return sid;
	}

	/**
	 * @param sid 要设置的 sid
	 */
	public void setSid(int sid)
	{
		this.sid=sid;
	}
	
	public Object clone()
	{
		try
		{
			return copy(super.clone());
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException(getClass().getName()+" clone, "+e);
		}
	}

	public Object copy(Object obj)
	{
		return obj;
	}
}
