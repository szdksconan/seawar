package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.kit.SeaBackKit;

/***
 * 联盟捐献物资献
 * 
 * @author lhj
 * 
 */
public class MaterialValue 
{

	public static final int DAY_TYPE=1,WEEK_TYPE=2,MOUTH_TYPE=3,
					TOTLE_TYPE=4;
	//删除标识
	public static final int DELETE=-1;

	/** 玩家id **/
	int playerId;
	/** 当天的次数 **/
	int times;
	/** 捐献时间 **/
	int valueTime;
	/** 日捐献总数 **/
	int dayValue;
	/** 周捐献总数 **/
	int weekValue;
	/** 月捐献总数 **/
	int mouthValue;
	/** 捐献总数 **/
	int totleValue;
	/** 每隔10点的时候就推送一次联盟事件 **/
	public static int VALUE_SHOW=10;

	/** 前台序列化 **/
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

	/** 联盟战的反序列化 **/
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

	/** 序列化 */
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

	/** 物资捐献添加 **/
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
	/** 刷新物资捐献 **/
	public void flushGiveValue()
	{
		int timeNow=TimeKit.getSecondTime();
		if(valueTime==0 ||!SeaBackKit.isSameDay(valueTime,timeNow))
		{
			// 是否刷新月捐献
			if(!SeaBackKit.isSameMouth(valueTime,timeNow))
				mouthValue=0;
			// 是否刷新周捐献
			else if(!SeaBackKit.isSameWeek(valueTime,timeNow)) weekValue=0;
			dayValue=0;
			times=0;
			valueTime=timeNow;
		}
	}
	
	/**获取玩家当前的记录**/
	public String getReocrd()
	{
		flushGiveValue();
		return times+","+valueTime;
	}
	
	/** 当天的次数 **/
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
