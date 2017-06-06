package foxu.sea.alliance.alliancefight;

import mustang.io.ByteBuffer;

/**
 * 库存舰队
 * 
 * @author yw
 * 
 */
public class StockFleet
{

	/** 船SID */
	int sid;
	/** 船数量 */
	int count;

	public StockFleet()
	{
		
	}
	public StockFleet(int sid,int count)
	{
		this.sid=sid;
		this.count=count;
	}
	/** 增加库存量 */
	public int[] incrCount(int add)
	{
		int[] old_now={count,count};
		if(add<=0) return old_now;
		long addResult=(long)add+count;
		if(addResult>Integer.MAX_VALUE)
		{
			count=Integer.MAX_VALUE;
		}
		else
		{
			count=(int)addResult;
		}
		old_now[1]=count;
		return old_now;
	}
	/** 减少库存量 */
	public void decrCount(int decr)
	{
		if(decr<=0) return;
		if(count>decr)
		{
			count-=decr;
		}
		else
		{
			count=0;
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		sid=data.readUnsignedShort();
		count=data.readInt();
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeInt(count);
	}

	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		this.sid=sid;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count=count;
	}

}
