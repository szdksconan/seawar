package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;

/**
 * 精英战场
 * 
 * @author lhj
 * 
 */
public class ElitePoint
{

	/** 军备航线的设置 */
	IntKeyHashMap shipFleets=new IntKeyHashMap();
	/** 记录 **/
	IntKeyHashMap eliteRecord=new IntKeyHashMap();
	/** 攻打次数 **/
	int attackNums;
	/** 攻打时间 **/
	int attackTime;

	/** 增加星星数量 **/
	public void addPoint(int star,int sid,int nextSid)
	{
		EliteRecord record=(EliteRecord)eliteRecord.get(sid);
		if(record==null) record=new EliteRecord();
		record.setSid(sid);
		attackTime=TimeKit.getSecondTime();
		if(record.getStarNum()<star) record.setStarNum(star);
		attackNums+=1;
		eliteRecord.put(sid,record);
	}
	/** 是否可以扫荡 **/
	public boolean canattact(int sid)
	{
		EliteRecord record=(EliteRecord)eliteRecord.get(sid);
		if(record==null) return false;
		return record.getStarNum()>=SelfCheckPoint.THREE_STAR;
	}
	/** 判断是否可挑战 */
	public String canChalleng(int sid,int times)
	{
		if(attackNums>=PublicConst.FIGHT_ATTACK_LENGTH) return "times  out";
		int effectiveTimes=PublicConst.FIGHT_ATTACK_LENGTH-attackNums;
		if(effectiveTimes<times) return "times limit";
		int timeNow=TimeKit.getSecondTime();
		// 不是在同一天则刷新
		if(!SeaBackKit.isSameDay(timeNow,attackTime)) clear();
		return null;
	}
	/** 清除挑战次数 **/
	public void clear()
	{
		attackNums=0;
	}
	/** 获取星星数量 **/
	public int getPointStar(int sid)
	{
		EliteRecord record=(EliteRecord)eliteRecord.get(sid);
		if(record==null) return 0;
		return record.getStarNum();
	}
	/** 序列化给前台 **/
	public void showBytesWrite(ByteBuffer data)
	{
		if(attackTime!=0
			&&!SeaBackKit.isSameDay(TimeKit.getSecondTime(),attackTime))
			clear();
		// 攻打次数
		data.writeByte(attackNums);
		Object[] recordValue=eliteRecord.valueArray();
		if(recordValue==null)
			data.writeByte(0);
		else
		{
			int len=0;
			int top=data.top();
			data.writeByte(eliteRecord.size());
			for(int j=0;j<recordValue.length;j++)
			{
				if(recordValue[j]==null) continue;
				len++;
				EliteRecord record=(EliteRecord)recordValue[j];
				record.showBytesWriteElite(data);
			}
			if(len!=eliteRecord.size())
			{
				int nowTop=data.top();
				data.setTop(top);
				data.writeByte(len);
				data.setTop(nowTop);
			}
		}
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeByte(attackNums);
		data.writeInt(attackTime);
		Object[] recordValue=eliteRecord.valueArray();
		if(recordValue==null)
		{
			data.writeInt(0);
		}
		else
		{
			int len=0;
			int top=data.top();
			data.writeInt(eliteRecord.size());
			for(int i=0;i<recordValue.length;i++)
			{
				if(recordValue[i]==null) continue;
				len++;
				EliteRecord record=(EliteRecord)recordValue[i];
				record.bytesWrite(data);
			}
			if(len!=eliteRecord.size())
			{
				int nowTop=data.top();
				data.setTop(top);
				data.writeInt(len);
				data.setTop(nowTop);
			}
		}
	}

	/** 反序列化 */
	public Object bytesRead(ByteBuffer data)
	{
		attackNums=data.readUnsignedByte();
		attackTime=data.readInt();
		int length=data.readInt();
		if(length!=0)
		{
			for(int j=0;j<length;j++)
			{
				EliteRecord record=new EliteRecord();
				record.bytesRead(data,eliteRecord);
			}
		}
		return this;
	}

	public int getAttackNums()
	{
		return attackNums;
	}

	
	public void setAttackNums(int attackNums)
	{
		this.attackNums=attackNums;
	}

	
	
}
