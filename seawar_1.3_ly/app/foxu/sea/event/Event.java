package foxu.sea.event;

import mustang.io.ByteBuffer;

/**事件类*/
public class Event
{
   /**类型*/
   int type;
   
   /**开始时间*/
   int startTime;
   
   /**持续时间*/
   int continueTime;
   
	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeByte(type);
		data.writeInt(startTime);
		data.writeInt(continueTime);
	}
	
	public Object bytesRead(ByteBuffer data)
	{
		type = data.readUnsignedByte(); 
		startTime = data.readInt();
		continueTime = data.readInt();
		return this;
	}

	
	/**
	 * @return continueTime
	 */
	public int getContinueTime()
	{
		return continueTime;
	}

	
	/**
	 * @param continueTime 要设置的 continueTime
	 */
	public void setContinueTime(int continueTime)
	{
		this.continueTime=continueTime;
	}

	
	/**
	 * @return startTime
	 */
	public int getStartTime()
	{
		return startTime;
	}

	
	/**
	 * @param startTime 要设置的 startTime
	 */
	public void setStartTime(int startTime)
	{
		this.startTime=startTime;
	}

	
	/**
	 * @return type
	 */
	public int getType()
	{
		return type;
	}

	
	/**
	 * @param type 要设置的 type
	 */
	public void setType(int type)
	{
		this.type=type;
	}
   
}
