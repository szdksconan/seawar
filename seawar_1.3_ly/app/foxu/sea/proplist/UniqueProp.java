package foxu.sea.proplist;

import mustang.io.ByteBuffer;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */


/**
 * 类说明：唯一物品类
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class UniqueProp extends Prop
{
	/***/
	int arg1=0;
	/***/
	int[] arg2=null;
	
	/** 保护样本的构造方法 */
	protected UniqueProp()
	{
	}
	/* properties */
	/**获得简化版本的物品*/
	public Prop toShowProp()
	{
		UniqueProp up=new UniqueProp();
		up.bindUid(getId());
		up.style=getStyle();
		return up;
	}
	/* methods */
	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		bytesReadUid(data);
		arg1=data.readInt();
		int n=data.readUnsignedByte();
		if(n==0)return this;
		arg2=new int[n];
		for(int i=0;i<n;i++)
		{
			arg2[i]=data.readInt();
		}
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		bytesWriteUid(data);
		data.writeInt(arg1);
		if(arg2!=null)
		{
			int n=arg2.length;
			data.writeByte(n);
			for(int i=0;i<n;i++)
			{
				data.writeInt(arg2[i]);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}
	/* common methods */
	public String toString()
	{
		return super.toString();
	}
}