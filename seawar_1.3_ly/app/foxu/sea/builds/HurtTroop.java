package foxu.sea.builds;

import mustang.io.ByteBuffer;

/** 伤兵 */
public class HurtTroop extends Troop
{
	/** 12小时之内全部恢复 之后每小时减10% */
	public static final int HOURS=60*60,HOURS_12=12,PERCENT_10=10;
	/** 伤兵的时间 */
	int time;

	/** 计算当前可以恢复的兵力 */
	public int nowHurtsNum(int checkTime)
	{
//		int passTime=checkTime-time;
//		int passHours=passTime/HOURS;
//		int num=getNum();
//		num-=passHours;
//		if(num<=0) num=0;
//		if(passHours>0)
//		{
//			setTime(checkTime);
//			setNum(num);
//		}
//		return num;
		return getNum();
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		time=data.readInt();
		return this;
	}
	

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(time);
	}

	public void showBytesWrite(ByteBuffer data,int current)
	{
		super.showBytesWrite(data,0);
		data.writeInt(current);
		data.writeInt(0);
	}

	/**
	 * @return time
	 */
	public int getTime()
	{
		return time;
	}

	/**
	 * @param time 要设置的 time
	 */
	public void setTime(int time)
	{
		this.time=time;
	}
}
