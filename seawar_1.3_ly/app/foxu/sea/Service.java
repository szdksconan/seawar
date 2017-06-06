package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/** 服务类 buff类 */
public class Service extends Sample implements AdjustmentsetUseable
{

	/** 服务的具体类型,配置决定,不需要通信 */
	int serviceType;
	/** 属性类型 */
	int attrType;
	/** 服务值 白分比 扩大100配置 */
	int value;
	/** 服务时长 */
	int serviceTime;

	/** 服务到期时间 */
	int endTime=0;

	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();

	/* static methods */
	/** 从字节数组中反序列化获得对象的域 */
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

	/** 服务是否已经结束 */
	public boolean isOver(int second)
	{
		return second>=endTime;
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		endTime=data.readInt();
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
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
	 * @param serviceType 要设置的 serviceType
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
	 * @param value 要设置的 value
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
	 * @param endTime 要设置的 endTime
	 */
	public void setEndTime(int endTime)
	{
		this.endTime=endTime;
	}

	/** 增加时间 服务时间增加多少秒 */
	public void addTime(int time)
	{
		endTime+=time;
	}
	
	public void reduceTime(int time)
	{
		endTime-=time;
	}

	/** 设置修正值 */
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
	 * @param serviceTime 要设置的 serviceTime
	 */
	public void setServiceTime(int serviceTime)
	{
		this.serviceTime=serviceTime;
	}
}