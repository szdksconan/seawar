package foxu.sea.activity;

import mustang.io.ByteBuffer;


/**
 * 累计消费活动记录
 * @author Alan
 *
 */
public class ConsumeGemsRecord
{
	int aid;
	int startTime;
	int endTime;
	int percent;
	int count;
	
	private ConsumeGemsRecord()
	{
		
	}
	
	public ConsumeGemsRecord(int aid,int startTime,int endTime,int percent)
	{
		super();
		this.aid=aid;
		this.startTime=startTime;
		this.endTime=endTime;
		this.percent=percent;
	}

	public int getAid()
	{
		return aid;
	}
	
	public void setAid(int aid)
	{
		this.aid=aid;
	}
	
	public int getStartTime()
	{
		return startTime;
	}
	
	public void setStartTime(int startTime)
	{
		this.startTime=startTime;
	}
	
	public int getEndTime()
	{
		return endTime;
	}
	
	public void setEndTime(int endTime)
	{
		this.endTime=endTime;
	}
	
	public int getPercent()
	{
		return percent;
	}
	
	public void setPercent(int percent)
	{
		this.percent=percent;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public void setCount(int count)
	{
		this.count=count;
	}
	
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(aid);
		data.writeInt(startTime);
		data.writeInt(endTime);
		data.writeByte(percent);
		data.writeInt(count);
	}
	
	public static ConsumeGemsRecord bytesRead(ByteBuffer data)
	{
		ConsumeGemsRecord record=new ConsumeGemsRecord();
		record.setAid(data.readInt());
		record.setStartTime(data.readInt());
		record.setEndTime(data.readInt());
		record.setPercent(data.readUnsignedByte());
		record.setCount(data.readInt());
		return record;
	}
}
