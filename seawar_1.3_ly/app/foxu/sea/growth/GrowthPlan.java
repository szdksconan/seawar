package foxu.sea.growth;

import mustang.io.ByteBuffer;
import mustang.set.IntList;

/**
 * �ɳ��ƻ�
 * 
 * @author Alan
 * 
 */
public class GrowthPlan
{

	/** �Ƿ�����˳ɳ��ƻ� */
	boolean isBuyPlan;
	/** ���˽�����ȡ��¼ */
	IntList privateList=new IntList();
	/** ȫ��������ȡ��¼ */
	IntList serverList=new IntList();

	public synchronized void bytesRead(ByteBuffer data)
	{
		// ���˼�¼
		isBuyPlan=data.readBoolean();
		int listLen=data.readUnsignedByte();
		for(int j=0;j<listLen;j++)
		{
			privateList.add(data.readUnsignedByte());
		}
		// ȫ����¼
		listLen=data.readUnsignedByte();
		for(int j=0;j<listLen;j++)
		{
			serverList.add(data.readInt());
		}

	}

	public synchronized void bytesWrite(ByteBuffer data)
	{
		// ���˼�¼
		data.writeBoolean(isBuyPlan);
		data.writeByte(privateList.size());
		for(int j=0;j<privateList.size();j++)
		{
			data.writeByte(privateList.get(j));
		}
		// ȫ����¼
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
