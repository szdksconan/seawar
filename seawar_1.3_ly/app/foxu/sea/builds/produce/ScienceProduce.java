package foxu.sea.builds.produce;

import mustang.io.ByteBuffer;
import foxu.sea.AttrAdjustment;
import foxu.sea.Science;

/**
 * �Ƽ��о�����
 * 
 * @author:icetiger
 */
public class ScienceProduce extends StandProduce
{
	/* fields */
	/** �ѵ����Ƽ� */
	Science[] science;

	/* properties */
	/** ������пƼ� */
	public Science[] getAllScience()
	{
		return science;
	}

	/* methods */
	/**
	 * ����sid�ҵ���Ӧ�Ƽ�
	 * 
	 * @param sid
	 * @return ����null��ʾû������Ƽ�
	 */
	public Science getScienceBySid(int sid)
	{
		for(int i=science.length-1;i>=0;i--)
		{
			if(science[i].getSid()==sid) return science[i];
		}
		return null;
	}
	public void putAdjustment(AttrAdjustment adjustment)
	{
		for(int i=science.length-1;i>=0;i--)
		{
			science[i].setChangeValue(adjustment);
		}
	}
	/** ����һ���Ƽ� */
	public void addScience(Science s)
	{
		for(int i=science.length-1;i>=0;i--)
		{
			if(science[i].getSid()==s.getSid())
			{
				science[i]=s;
				return;
			}
		}
		Science[] ss=new Science[science.length+1];
		System.arraycopy(science,0,ss,0,science.length);
		ss[science.length]=s;
		science=ss;
	}
	
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		if(science==null||science.length<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(science.length);
			for(int i=0;i<science.length;i++)
			{
				science[i].bytesWrite(data);
			}
		}
	}
	
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		int length=data.readUnsignedByte();
		science=new Science[length];
		for(int i=0;i<length;i++)
		{
			science[i]=Science.bytesReadSample(data);
		}
		return this;
	}
	public void showBytesWrite(ByteBuffer data,int current)
	{
		super.showBytesWrite(data,current);
		if(science==null||science.length<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(science.length);
			for(int i=0;i<science.length;i++)
			{
				science[i].showBytesWrite(data,current);
			}
		}
	}

	/** ���Ʒ�������Ҫ�������ε�����������������ȣ� */
	public Object copy(Object obj)
	{
		ScienceProduce p=(ScienceProduce)super.copy(obj);
		p.science=new Science[0];
		return p;
	}
}