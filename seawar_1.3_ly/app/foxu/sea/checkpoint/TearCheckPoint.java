package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;

/**
 * 撕裂虚空关卡状态
 * 
 * @author yw
 * 
 */
public class TearCheckPoint extends SelfCheckPoint
{

	/** 付费刷新时间 */
	int[] payTime=new int[1];
	/** 付费刷新次数 */
	int[] payCount=new int[1];
	/** 攻打时间 */
	int[] attackTime=new int[1];
	/** 今日已通过关卡 */
	int[] attackList=new int[1];
	/** 当前攻打的最高关卡 */
	int[] checkSid;

	public TearCheckPoint()
	{
		checkSid=PublicConst.TEAR_CHECK_SID;// 初始关卡
	}
	/** 数组扩充 */
	public int[] extraArray(int[] array,int len)
	{
		if(array==null) return new int[len+1];
		if(len>=array.length)
		{
			int[] temp=new int[len+1];
			System.arraycopy(array,0,temp,0,array.length);
			return temp;
		}
		return array;
	}
	/** 获取检测SID */
	public int getCheckPointSid(int chapter)
	{
		checkSid=extraArray(checkSid,chapter+1);
		return checkSid[chapter];
	}
	/** 攻击完一个关卡 星大于0就通过了 */
	public void addPoint(int star,int nextSid,int chapter,int index)
	{
		if(star<=0) return;
		super.addPoint(star,nextSid,chapter,index);
		CheckPoint point=(CheckPoint)CheckPoint.factory.getSample(nextSid);
		if(point!=null)
		{
			if(point.getChapter()-1>=checkSid.length)
			{
				checkSid=extraArray(checkSid,point.getChapter()-1);
			}
			if(point.getChapter()-1!=chapter
							&&nextSid>checkSid[point.getChapter()-1])
			{
				checkSid[point.getChapter()-1]=nextSid;
			}
			else if(point.getChapter()-1==chapter
							&&nextSid>checkSid[chapter])
			{
				checkSid[chapter]=nextSid;
			}
		}
		attackTime=extraArray(attackTime,chapter);
		attackList=extraArray(attackList,chapter);
		int time=TimeKit.getSecondTime();
		if(!SeaBackKit.isSameDay(attackTime[chapter],time))
		{
			attackTime[chapter]=time;
			clear(chapter);
		}
		attackList[chapter]=attackList[chapter]|(1<<index/2);
	}
	/** 清除关卡记录 */
	public void clear(int chapter)
	{
		if(chapter>=attackList.length) return;
		attackList[chapter]=0;
	}
	/** 判断某个关卡是否被打过 */
	public boolean isAttacked(int chapter,int index)
	{
		attackTime=extraArray(attackTime,chapter);
		attackList=extraArray(attackList,chapter);
		if(SeaBackKit.isSameDay(attackTime[chapter],TimeKit.getSecondTime())
			&&(attackList[chapter]&(1<<index))>0) return true;
		return false;
	}
	/** 判断某个章节是否被打过 */
	public boolean isAttackChapter(int chapter)
	{
		attackTime=extraArray(attackTime,chapter);
		attackList=extraArray(attackList,chapter);
		if(!SeaBackKit.isSameDay(attackTime[chapter],TimeKit.getSecondTime()))return false;
		if(attackList[chapter]<=0)return false;
		return true;
	}
	/** 获取某章关卡攻打记录值 */
	public int getAttackValueByChapter(int chapter)
	{
		attackTime=extraArray(attackTime,chapter);
		attackList=extraArray(attackList,chapter);
		if(!SeaBackKit.isSameDay(attackTime[chapter],TimeKit.getSecondTime()))return 0;
		return attackList[chapter];
	}
	/** 判断是否可付费刷新 */
	public boolean canPayReset(int chapter)
	{
		payTime=extraArray(payTime,chapter);
		payCount=extraArray(payCount,chapter);
		
		if(SeaBackKit.isSameDay(payTime[chapter],TimeKit.getSecondTime())
			&&payCount[chapter]>=PublicConst.TEAR_PAYCOUNT_MAX)
		{
			return false;
		}
		return true;
	}
	/** 获取今日刷新次数 */
	public int getPayCount(int chapter)
	{
		payTime=extraArray(payTime,chapter);
		payCount=extraArray(payCount,chapter);
		if(!SeaBackKit.isSameDay(payTime[chapter],TimeKit.getSecondTime()))
		{
			return 0;
		}
		return 	payCount[chapter];
		
	}
	/** 增加刷新次数 */
	public void addPayCount(int chapter)
	{
		payTime=extraArray(payTime,chapter);
		payCount=extraArray(payCount,chapter);
		int time=TimeKit.getSecondTime();
		if(!SeaBackKit.isSameDay(payTime[chapter],time))
		{
			payTime[chapter]=time;
			payCount[chapter]=1;
		}
		else
		{
			payCount[chapter]++;
		}
		clear(chapter);
	}
	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(payTime.length);
		for(int i=0;i<payTime.length;i++)
		{
			data.writeInt(payTime[i]);
		}
		data.writeByte(payCount.length);
		for(int i=0;i<payCount.length;i++)
		{
			data.writeByte(payCount[i]);
		}
		data.writeByte(attackTime.length);
		for(int i=0;i<attackTime.length;i++)
		{
			data.writeInt(attackTime[i]);
		}
		data.writeByte(attackList.length);
		for(int i=0;i<attackList.length;i++)
		{
			data.writeInt(attackList[i]);
		}
		data.writeByte(checkSid.length);
		for(int i=0;i<checkSid.length;i++)
		{
			data.writeShort(checkSid[i]);
		}
	}
	/** 反序列化 */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		int len=data.readUnsignedByte();
		payTime=new int[len];
		for(int i=0;i<len;i++)
		{
			payTime[i]=data.readInt();
		}
		len=data.readUnsignedByte();
		payCount=new int[len];
		for(int i=0;i<len;i++)
		{
			payCount[i]=data.readUnsignedByte();
		}
		len=data.readUnsignedByte();
		attackTime=new int[len];
		for(int i=0;i<len;i++)
		{
			attackTime[i]=data.readInt();
		}
		len=data.readUnsignedByte();
		attackList=new int[len];
		for(int i=0;i<len;i++)
		{
			attackList[i]=data.readInt();
		}
		len=data.readUnsignedByte();
		if(len>checkSid.length)
		{

			checkSid=extraArray(checkSid,len-1);
		}
		for(int i=0;i<len;i++)
		{
			checkSid[i]=data.readUnsignedShort();
		}
		return this;
	}
	/** 序列化给前台 */
	public void showBytesWrite(ByteBuffer data)
	{
		data.writeByte(checkSid.length);
		for(int i=0;i<checkSid.length;i++)
		{
			data.writeShort(checkSid[i]);
			if(i<list.length)
			{
				data.writeInt(list[i]);
			}
			else
			{
				data.writeInt(0);
			}
		}
		// 付费次数
		int len=payCount==null?0:payCount.length;
		data.writeByte(len);
		for(int i=0;i<len;i++)
		{
			data.writeShort(getPayCount(i));
		}
	}
}
