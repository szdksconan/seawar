package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.kit.SeaBackKit;

/**
 * 捐献排行榜
 * 
 * @author lhj
 * 
 */
public class DonateRank extends MaterialValue
{

	/** 每种资源每天最大捐献次数 */
	public static final int MAX_VALUE=6;
	/**消耗50点 发出联盟事件**/
	public static final int GIVE_SHOW=50;
	/** 玩家捐献的数据 **/
	int[] donaterecord=new int[6];
	
	/** 联盟战的反序列化 **/
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

	/** 序列化 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(donaterecord.length);
		for(int i=0;i<donaterecord.length;i++)
		{
			data.writeByte(donaterecord[i]);
		}
	}
	
	/** 捐献添加 **/
	public boolean addGiveValue(int num,int type,Player player)
	{
		if(num<0) return false;
		flushValue();
		times++;
		int before=(int)totleValue/GIVE_SHOW;
		valueTime=TimeKit.getSecondTime();
		dayValue+=num;
		AchieveCollect.allianceOfferForOneDay(dayValue,player);//单日成就解锁头像
		weekValue+=num;
		mouthValue+=num;
		totleValue+=num;
		donaterecord[type]++;
		int after=(int)totleValue/GIVE_SHOW;
		if(after>before) return true;
		return false;
	}
	
	
	/** 刷新捐献 **/
	public void flushValue()
	{
		int timeNow=TimeKit.getSecondTime();
		if(valueTime==0 || !SeaBackKit.isSameDay(valueTime,timeNow))
		{
			// 是否刷新月捐献
			if(!SeaBackKit.isSameMouth(valueTime,timeNow))
				mouthValue=0;
			// 是否刷新周捐献
			else if(!SeaBackKit.isSameWeek(valueTime,timeNow)) weekValue=0;
			dayValue=0;
			times=0;
			donaterecord=new int[6];
			valueTime=timeNow;
		}
	}
	/**获取当前捐献记录**/
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
