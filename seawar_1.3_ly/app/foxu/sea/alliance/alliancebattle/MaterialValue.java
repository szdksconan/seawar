package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.kit.SeaBackKit;

/***
 * ���˾���������
 * 
 * @author lhj
 * 
 */
public class MaterialValue 
{

	public static final int DAY_TYPE=1,WEEK_TYPE=2,MOUTH_TYPE=3,
					TOTLE_TYPE=4;
	//ɾ����ʶ
	public static final int DELETE=-1;

	/** ���id **/
	int playerId;
	/** ����Ĵ��� **/
	int times;
	/** ����ʱ�� **/
	int valueTime;
	/** �վ������� **/
	int dayValue;
	/** �ܾ������� **/
	int weekValue;
	/** �¾������� **/
	int mouthValue;
	/** �������� **/
	int totleValue;
	/** ÿ��10���ʱ�������һ�������¼� **/
	public static int VALUE_SHOW=10;

	/** ǰ̨���л� **/
	public void showByteWrite(ByteBuffer data,CreatObjectFactory factory,
		int type,int rank)
	{
		flushGiveValue();
		data.writeInt(playerId);
		Player player=factory.getPlayerById(playerId);
		data.writeUTF(player.getName());
		data.writeByte(rank);
		data.writeByte(player.getLevel());
		data.writeInt(player.getFightScore());
		if(type==DAY_TYPE)
			data.writeInt(dayValue);
		else if(type==WEEK_TYPE)
			data.writeInt(weekValue);
		else if(type==MOUTH_TYPE)
			data.writeInt(mouthValue);
		else if(type==TOTLE_TYPE) 
			data.writeInt(totleValue);
	}

	/** ����ս�ķ����л� **/
	public Object bytesRead(ByteBuffer data)
	{
		playerId=data.readInt();
		times=data.readUnsignedByte();
		valueTime=data.readInt();
		dayValue=data.readInt();
		weekValue=data.readInt();
		mouthValue=data.readInt();
		totleValue=data.readInt();
		return this;
	}

	/** ���л� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(playerId);
		data.writeByte(times);
		data.writeInt(valueTime);
		data.writeInt(dayValue);
		data.writeInt(weekValue);
		data.writeInt(mouthValue);
		data.writeInt(totleValue);
	}

	/** ���ʾ������ **/
	public boolean addMaterialValue(int num)
	{
		if(num<0) return false;
		flushGiveValue();
		times++;
		int before=(int)totleValue/VALUE_SHOW;
		valueTime=TimeKit.getSecondTime();
		dayValue+=num;
		weekValue+=num;
		mouthValue+=num;
		totleValue+=num;
		int after=(int)totleValue/VALUE_SHOW;
		if(after>before) return true;
		return false;
	}
	/** ˢ�����ʾ��� **/
	public void flushGiveValue()
	{
		int timeNow=TimeKit.getSecondTime();
		if(valueTime==0 ||!SeaBackKit.isSameDay(valueTime,timeNow))
		{
			// �Ƿ�ˢ���¾���
			if(!SeaBackKit.isSameMouth(valueTime,timeNow))
				mouthValue=0;
			// �Ƿ�ˢ���ܾ���
			else if(!SeaBackKit.isSameWeek(valueTime,timeNow)) weekValue=0;
			dayValue=0;
			times=0;
			valueTime=timeNow;
		}
	}
	
	/**��ȡ��ҵ�ǰ�ļ�¼**/
	public String getReocrd()
	{
		flushGiveValue();
		return times+","+valueTime;
	}
	
	/** ����Ĵ��� **/
	public int getTimes()
	{
		flushGiveValue();
		return times;
	}

	public void setTimes(int times)
	{
		this.times=times;
	}

	public int getValueTime()
	{
		return valueTime;
	}

	public void setValueTime(int valueTime)
	{
		this.valueTime=valueTime;
	}

	public int getDayValue()
	{
		return dayValue;
	}

	public void setDayValue(int dayValue)
	{
		this.dayValue=dayValue;
	}

	public int getWeekValue()
	{
		return weekValue;
	}

	public void setWeekValue(int weekValue)
	{
		this.weekValue=weekValue;
	}

	public int getMouthValue()
	{
		return mouthValue;
	}

	public void setMouthValue(int mouthValue)
	{
		this.mouthValue=mouthValue;
	}

	public long getTotleValue()
	{
		return totleValue;
	}

	public void setTotleValue(int totleValue)
	{
		this.totleValue=totleValue;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

}
