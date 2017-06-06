package foxu.sea.builds;

import mustang.io.ByteBuffer;

/** �˱� */
public class HurtTroop extends Troop
{
	/** 12Сʱ֮��ȫ���ָ� ֮��ÿСʱ��10% */
	public static final int HOURS=60*60,HOURS_12=12,PERCENT_10=10;
	/** �˱���ʱ�� */
	int time;

	/** ���㵱ǰ���Իָ��ı��� */
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
	

	/** ������������л����ֽڻ����� */
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
	 * @param time Ҫ���õ� time
	 */
	public void setTime(int time)
	{
		this.time=time;
	}
}
