package foxu.sea.batte;

import mustang.io.ByteBuffer;
import mustang.util.Sample;

/**
 * 奖励获取的奖励 物品sid author:icetiger
 */
public class FightAward extends Sample
{
	/** 物品SID */
	int propSid[] = {};
	/** 资源 */
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
	
	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
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
	
	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteResources(ByteBuffer data)
	{
		data.writeByte(resources.length);
		for(int i=resources.length-1;i>=0;i--)
		{
			data.writeInt(resources[i]);
		}
	}
	
	/** 将对象的域序列化到字节缓存中 */
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
	 * @param propSid 要设置的 propSid
	 */
	public void setPropSid(int[] propSid)
	{
		this.propSid=propSid;
	}
}
