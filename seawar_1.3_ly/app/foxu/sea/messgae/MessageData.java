package foxu.sea.messgae;

import mustang.io.ByteBuffer;
import foxu.sea.Player;

/**
 * 邮件附件类 author:icetiger
 */
public class MessageData
{
	/** 物品sid,num*/
	int propsid[] = {};
	/** 资源 包括宝石 */
	int resources[] = new int[Player.RESOURCES_SIZE];
	/**船只sid,num*/
	int shipSids[] = {};
	/**声望*/
	int honor;

	public Object bytesRead(ByteBuffer data)
	{
		bytesReadPropsid(data);
		bytesReadResources(data);
		bytesReadShipSids(data);
		honor = data.readInt();
		return this;
	}

	public void bytesWrite(ByteBuffer data)
	{
		bytesWritePropSid(data);
		bytesWriteResources(data);
		bytesWriteShipSids(data);
		data.writeInt(honor);
	}
	
	public Object bytesReadPropsid(ByteBuffer data)
	{
		int length=data.readUnsignedShort();
		propsid=new int[length];
		for(int i=length-1;i>=0;i--)
		{
			propsid[i]=data.readInt();
		}
		return this;
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
	
	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteShipSids(ByteBuffer data)
	{
		data.writeShort(shipSids.length);
		for(int i=shipSids.length-1;i>=0;i--)
		{
			data.writeInt(shipSids[i]);
		}
	}
	
	public Object bytesReadShipSids(ByteBuffer data)
	{
		int length=data.readUnsignedShort();
		shipSids=new int[length];
		for(int i=length-1;i>=0;i--)
		{
			shipSids[i]=data.readInt();
		}
		return this;
	}
	
	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWritePropSid(ByteBuffer data)
	{
		data.writeShort(propsid.length);
		for(int i=propsid.length-1;i>=0;i--)
		{
			data.writeInt(propsid[i]);
		}
	}

	/**
	 * @return propsid
	 */
	public int[] getPropsid()
	{
		return propsid;
	}

	/**
	 * @param propsid 要设置的 propsid
	 */
	public void setPropsid(int[] propsid)
	{
		this.propsid=propsid;
	}

	
	/**
	 * @return resources
	 */
	public int[] getResources()
	{
		return resources;
	}

	
	/**
	 * @param resources 要设置的 resources
	 */
	public void setResources(int[] resources)
	{
		this.resources=resources;
	}

	
	/**
	 * @return shipSids
	 */
	public int[] getShipSids()
	{
		return shipSids;
	}

	
	/**
	 * @param shipSids 要设置的 shipSids
	 */
	public void setShipSids(int[] shipSids)
	{
		this.shipSids=shipSids;
	}

	
	/**
	 * @return honor
	 */
	public int getHonor()
	{
		return honor;
	}

	
	/**
	 * @param honor 要设置的 honor
	 */
	public void setHonor(int honor)
	{
		this.honor=honor;
	}

}
