package foxu.sea.alliance.alliancefight;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;


/**
 * ս���Ž�
 * @author yw
 *
 */
public class WarHorn
{
	public static int MAX=20,MIN=-95,GRAD=5;
	/** ��תʱ�̼ӳ� */
	int percent;
	/** ��תʱ�� */
	int turnTime;
	/** ����or���� */
	int addOrDecr;
	
	public WarHorn()
	{
		turnTime=TimeKit.getSecondTime();
		addOrDecr=1;
	}
	
	/**
	 * ���÷�תʱ�̼ӳ�
	 * @param percent
	 */
	public void setPercent(int percent)
	{
		this.percent=percent;
	}
	
	/**
	 * ���÷�תʱ��
	 * @param time
	 */
	public void setTurnTime(int time)
	{
		this.turnTime=time;
	}
	
	/**
	 * ��������
	 * @param i
	 */
	public void setAddOrDecr(int i)
	{
		this.addOrDecr=i;
	}
	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		percent=data.readShort();
		turnTime=data.readInt();
		addOrDecr=data.readByte();
		return this;
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(percent);
		data.writeInt(turnTime);
		data.writeByte(addOrDecr);
	}
	/** ��ȡ��ǰ�ӳ� */
	public int getCPercent()
	{
		int cpercent=percent+(TimeKit.getSecondTime()-turnTime)/AllianceFight.AWARD_INERVAL*GRAD*addOrDecr;
		if(cpercent>MAX)cpercent=MAX;
		if(cpercent<MIN)cpercent=MIN;
		return cpercent;
	}
	/** ��ת�Ž� */
	public void takeBake(int aOrd,boolean retreat)
	{
		percent=getCPercent();
		if(retreat)percent=percent-GRAD<MIN?MIN:percent-GRAD;
		if(aOrd==1)percent=percent-GRAD<MIN?MIN:percent-GRAD;
		turnTime=TimeKit.getSecondTime();
		addOrDecr=aOrd;
	}
	/** ǿ��-5�Ž� */
	public void forceDecr()
	{
		percent=getCPercent();
		percent=percent-GRAD<MIN?MIN:percent-GRAD;
		turnTime=TimeKit.getSecondTime();
	}

}
