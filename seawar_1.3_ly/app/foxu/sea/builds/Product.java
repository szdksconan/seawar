package foxu.sea.builds;

import mustang.io.ByteBuffer;

/**
 * ���������еĲ�Ʒ
 * 
 * @author rockzyt
 */
public class Product implements Cloneable
{
	/* fields */
	/** ��Ʒsid */
	public int sid;
	/** ��Ʒ���� */
	public int num;
	/** ���ʱ��� */
	int finishTime;
	/** �˶��в�Ʒ��Ҫ��ʱ�� */
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
	 * @param produceTime Ҫ���õ� produceTime
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
	 * @param finlishTime Ҫ���õ� finlishTime
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
	 * @param num Ҫ���õ� num
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
	 * @param sid Ҫ���õ� sid
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
