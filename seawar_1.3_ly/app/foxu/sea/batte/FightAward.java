package foxu.sea.batte;

import mustang.io.ByteBuffer;
import mustang.util.Sample;

/**
 * ������ȡ�Ľ��� ��Ʒsid author:icetiger
 */
public class FightAward extends Sample
{
	/** ��ƷSID */
	int propSid[] = {};
	/** ��Դ */
	int resources[]={};
	
	public void bytesReadPropSid(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		propSid=new int[length];
		for(int i=length-1;i>=0;i--)
		{
			propSid[i]=data.readInt();
		}
	}
	
	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWritePropSid(ByteBuffer data)
	{
		data.writeByte(propSid.length);
		for(int i=propSid.length-1;i>=0;i--)
		{
			data.writeInt(propSid[i]);
		}
	}
	
	
	public void bytesReadResources(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		resources=new int[length];
		for(int i=length-1;i>=0;i--)
		{
			resources[i]=data.readInt();
		}
	}
	
	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteResources(ByteBuffer data)
	{
		data.writeByte(resources.length);
		for(int i=resources.length-1;i>=0;i--)
		{
			data.writeInt(resources[i]);
		}
	}
	
	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		bytesWritePropSid(data);
		bytesWriteResources(data);
	}
	
	public Object bytesRead(ByteBuffer data)
	{
		bytesReadPropSid(data);
		bytesReadResources(data);
		return this;
	}
	
	/**
	 * @return propSid
	 */
	public int[] getPropSid()
	{
		return propSid;
	}

	/**
	 * @param propSid Ҫ���õ� propSid
	 */
	public void setPropSid(int[] propSid)
	{
		this.propSid=propSid;
	}
}
