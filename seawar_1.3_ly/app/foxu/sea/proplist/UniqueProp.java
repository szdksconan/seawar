package foxu.sea.proplist;

import mustang.io.ByteBuffer;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */


/**
 * ��˵����Ψһ��Ʒ��
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
	
	/** ���������Ĺ��췽�� */
	protected UniqueProp()
	{
	}
	/* properties */
	/**��ü򻯰汾����Ʒ*/
	public Prop toShowProp()
	{
		UniqueProp up=new UniqueProp();
		up.bindUid(getId());
		up.style=getStyle();
		return up;
	}
	/* methods */
	/** ���ֽ������з����л���ö������ */
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
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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