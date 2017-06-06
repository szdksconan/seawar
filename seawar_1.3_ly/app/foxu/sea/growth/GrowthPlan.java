package foxu.sea.growth;

import mustang.io.ByteBuffer;
import mustang.set.IntList;

/**
 * 成长计划
 * 
 * @author Alan
 * 
 */
public class GrowthPlan
{

	/** 是否购买个人成长计划 */
	boolean isBuyPlan;
	/** 个人奖励领取记录 */
	IntList privateList=new IntList();
	/** 全服奖励领取记录 */
	IntList serverList=new IntList();

	public synchronized void bytesRead(ByteBuffer data)
	{
		// 个人记录
		isBuyPlan=data.readBoolean();
		int listLen=data.readUnsignedByte();
		for(int j=0;j<listLen;j++)
		{
			privateList.add(data.readUnsignedByte());
		}
		// 全服记录
		listLen=data.readUnsignedByte();
		for(int j=0;j<listLen;j++)
		{
			serverList.add(data.readInt());
		}

	}

	public synchronized void bytesWrite(ByteBuffer data)
	{
		// 个人记录
		data.writeBoolean(isBuyPlan);
		data.writeByte(privateList.size());
		for(int j=0;j<privateList.size();j++)
		{
			data.writeByte(privateList.get(j));
		}
		// 全服记录
		data.writeByte(serverList.size());
		for(int j=0;j<serverList.size();j++)
		{
			data.writeInt(serverList.get(j));
		}
	}

	public void addPrivateRecord(int key)
	{
		privateList.add(key);
	}

	public void addServerRecord(int key)
	{
		serverList.add(key);
	}

	public boolean isBuyPlan()
	{
		return isBuyPlan;
	}

	public void setBuyPlan(boolean isBuyPlan)
	{
		this.isBuyPlan=isBuyPlan;
	}

	public IntList getPrivateList()
	{
		return privateList;
	}

	public void setPrivateList(IntList privateList)
	{
		this.privateList=privateList;
	}

	public IntList getServerList()
	{
		return serverList;
	}

	public void setServerList(IntList serverList)
	{
		this.serverList=serverList;
	}
}
