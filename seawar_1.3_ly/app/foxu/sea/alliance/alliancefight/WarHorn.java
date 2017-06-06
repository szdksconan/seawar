package foxu.sea.alliance.alliancefight;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;


/**
 * 战争号角
 * @author yw
 *
 */
public class WarHorn
{
	public static int MAX=20,MIN=-95,GRAD=5;
	/** 翻转时刻加成 */
	int percent;
	/** 翻转时刻 */
	int turnTime;
	/** 增益or减益 */
	int addOrDecr;
	
	public WarHorn()
	{
		turnTime=TimeKit.getSecondTime();
		addOrDecr=1;
	}
	
	/**
	 * 设置翻转时刻加成
	 * @param percent
	 */
	public void setPercent(int percent)
	{
		this.percent=percent;
	}
	
	/**
	 * 设置翻转时间
	 * @param time
	 */
	public void setTurnTime(int time)
	{
		this.turnTime=time;
	}
	
	/**
	 * 设置增减
	 * @param i
	 */
	public void setAddOrDecr(int i)
	{
		this.addOrDecr=i;
	}
	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		percent=data.readShort();
		turnTime=data.readInt();
		addOrDecr=data.readByte();
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(percent);
		data.writeInt(turnTime);
		data.writeByte(addOrDecr);
	}
	/** 获取当前加成 */
	public int getCPercent()
	{
		int cpercent=percent+(TimeKit.getSecondTime()-turnTime)/AllianceFight.AWARD_INERVAL*GRAD*addOrDecr;
		if(cpercent>MAX)cpercent=MAX;
		if(cpercent<MIN)cpercent=MIN;
		return cpercent;
	}
	/** 翻转号角 */
	public void takeBake(int aOrd,boolean retreat)
	{
		percent=getCPercent();
		if(retreat)percent=percent-GRAD<MIN?MIN:percent-GRAD;
		if(aOrd==1)percent=percent-GRAD<MIN?MIN:percent-GRAD;
		turnTime=TimeKit.getSecondTime();
		addOrDecr=aOrd;
	}
	/** 强制-5号角 */
	public void forceDecr()
	{
		percent=getCPercent();
		percent=percent-GRAD<MIN?MIN:percent-GRAD;
		turnTime=TimeKit.getSecondTime();
	}

}
