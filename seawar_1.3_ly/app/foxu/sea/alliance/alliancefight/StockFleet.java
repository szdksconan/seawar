package foxu.sea.alliance.alliancefight;

import mustang.io.ByteBuffer;

/**
 * ��潢��
 * 
 * @author yw
 * 
 */
public class StockFleet
{

	/** ��SID */
	int sid;
	/** ������ */
	int count;

	public StockFleet()
	{
		
	}
	public StockFleet(int sid,int count)
	{
		this.sid=sid;
		this.count=count;
	}
	/** ���ӿ���� */
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
	/** ���ٿ���� */
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

	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		sid=data.readUnsignedShort();
		count=data.readInt();
		return this;
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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
