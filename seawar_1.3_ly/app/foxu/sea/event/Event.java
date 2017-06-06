package foxu.sea.event;

import mustang.io.ByteBuffer;

/**�¼���*/
public class Event
{
   /**����*/
   int type;
   
   /**��ʼʱ��*/
   int startTime;
   
   /**����ʱ��*/
   int continueTime;
   
	/** ������������л����ֽڻ����� */
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
	 * @param continueTime Ҫ���õ� continueTime
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
	 * @param startTime Ҫ���õ� startTime
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
	 * @param type Ҫ���õ� type
	 */
	public void setType(int type)
	{
		this.type=type;
	}
   
}
