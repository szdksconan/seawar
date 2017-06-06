package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.kit.SeaBackKit;

/**
 * �������а�
 * 
 * @author lhj
 * 
 */
public class DonateRank extends MaterialValue
{

	/** ÿ����Դÿ�������״��� */
	public static final int MAX_VALUE=6;
	/**����50�� ���������¼�**/
	public static final int GIVE_SHOW=50;
	/** ��Ҿ��׵����� **/
	int[] donaterecord=new int[6];
	
	/** ����ս�ķ����л� **/
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		int le=data.readUnsignedByte();
		for(int i=0;i<le;i++)
		{
			donaterecord[i]=data.readUnsignedByte();
		}
		return this;
	}

	/** ���л� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(donaterecord.length);
		for(int i=0;i<donaterecord.length;i++)
		{
			data.writeByte(donaterecord[i]);
		}
	}
	
	/** ������� **/
	public boolean addGiveValue(int num,int type,Player player)
	{
		if(num<0) return false;
		flushValue();
		times++;
		int before=(int)totleValue/GIVE_SHOW;
		valueTime=TimeKit.getSecondTime();
		dayValue+=num;
		AchieveCollect.allianceOfferForOneDay(dayValue,player);//���ճɾͽ���ͷ��
		weekValue+=num;
		mouthValue+=num;
		totleValue+=num;
		donaterecord[type]++;
		int after=(int)totleValue/GIVE_SHOW;
		if(after>before) return true;
		return false;
	}
	
	
	/** ˢ�¾��� **/
	public void flushValue()
	{
		int timeNow=TimeKit.getSecondTime();
		if(valueTime==0 || !SeaBackKit.isSameDay(valueTime,timeNow))
		{
			// �Ƿ�ˢ���¾���
			if(!SeaBackKit.isSameMouth(valueTime,timeNow))
				mouthValue=0;
			// �Ƿ�ˢ���ܾ���
			else if(!SeaBackKit.isSameWeek(valueTime,timeNow)) weekValue=0;
			dayValue=0;
			times=0;
			donaterecord=new int[6];
			valueTime=timeNow;
		}
	}
	/**��ȡ��ǰ���׼�¼**/
	public String getGiveRecord()
	{
		flushValue();
		StringBuffer buffer=new StringBuffer();
		for(int i=0;i<donaterecord.length;i++)
		{
			buffer.append(","+donaterecord[i]);
		}
		buffer.append(","+valueTime);
		return buffer.toString();
	}

	
	public int[] getDonaterecord()
	{
		flushValue();
		return donaterecord;
	}

	
	public void setDonaterecord(int[] donaterecord)
	{
		this.donaterecord=donaterecord;
	}
	
	
}
