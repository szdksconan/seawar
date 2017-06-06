package foxu.sea.builds.produce;

import mustang.io.ByteBuffer;
import foxu.sea.AttrAdjustment;
import foxu.sea.Science;

/**
 * 科技研究生产
 * 
 * @author:icetiger
 */
public class ScienceProduce extends StandProduce
{
	/* fields */
	/** 已点亮科技 */
	Science[] science;

	/* properties */
	/** 获得所有科技 */
	public Science[] getAllScience()
	{
		return science;
	}

	/* methods */
	/**
	 * 根据sid找到对应科技
	 * 
	 * @param sid
	 * @return 返回null表示没有这个科技
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
	/** 增加一个科技 */
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

	/** 复制方法（主要复制深层次的域变量，如对象、数组等） */
	public Object copy(Object obj)
	{
		ScienceProduce p=(ScienceProduce)super.copy(obj);
		p.science=new Science[0];
		return p;
	}
}