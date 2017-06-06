package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/** ������ buff�� */
public class Service extends Sample implements AdjustmentsetUseable
{

	/** ����ľ�������,���þ���,����Ҫͨ�� */
	int serviceType;
	/** �������� */
	int attrType;
	/** ����ֵ �׷ֱ� ����100���� */
	int value;
	/** ����ʱ�� */
	int serviceTime;

	/** ������ʱ�� */
	int endTime=0;

	/** �������� */
	public static SampleFactory factory=new SampleFactory();

	/* static methods */
	/** ���ֽ������з����л���ö������ */
	public static Service bytesReadService(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Service p=(Service)factory.newSample(sid);
		if(p==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Service.class
					.getName()
					+" bytesRead, invalid sid:"+sid);
		p.bytesRead(data);
		return p;
	}

	/** �����Ƿ��Ѿ����� */
	public boolean isOver(int second)
	{
		return second>=endTime;
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		endTime=data.readInt();
		return this;
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(endTime);
	}
	public void showBytesWrite(ByteBuffer data,int time)
	{
		super.bytesWrite(data);
		data.writeInt(endTime-time);
	}
	/**
	 * @return serviceType
	 */
	public int getServiceType()
	{
		return serviceType;
	}
	/**
	 * @param serviceType Ҫ���õ� serviceType
	 */
	public void setServiceType(int serviceType)
	{
		this.serviceType=serviceType;
	}
	/**
	 * @return value
	 */
	public int getValue()
	{
		return value;
	}
	/**
	 * @param value Ҫ���õ� value
	 */
	public void setValue(int value)
	{
		this.value=value;
	}
	/**
	 * @return endTime
	 */
	public int getEndTime()
	{
		return endTime;
	}

	/**
	 * @param endTime Ҫ���õ� endTime
	 */
	public void setEndTime(int endTime)
	{
		this.endTime=endTime;
	}

	/** ����ʱ�� ����ʱ�����Ӷ����� */
	public void addTime(int time)
	{
		endTime+=time;
	}
	
	public void reduceTime(int time)
	{
		endTime-=time;
	}

	/** ��������ֵ */
	public void setChangeValue(AttrAdjustment adjustment)
	{
		if(serviceType==PublicConst.ADD_SPREED_BUFF) return;
		if(attrType==0)
			adjustment.add(serviceType,value,false);
		else
			adjustment.add(serviceType,attrType,value,false);
	}

	/**
	 * @return serviceTime
	 */
	public int getServiceTime()
	{
		return serviceTime;
	}

	/**
	 * @param serviceTime Ҫ���õ� serviceTime
	 */
	public void setServiceTime(int serviceTime)
	{
		this.serviceTime=serviceTime;
	}
}