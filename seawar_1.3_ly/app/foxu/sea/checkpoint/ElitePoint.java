package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;

/**
 * ��Ӣս��
 * 
 * @author lhj
 * 
 */
public class ElitePoint
{

	/** �������ߵ����� */
	IntKeyHashMap shipFleets=new IntKeyHashMap();
	/** ��¼ **/
	IntKeyHashMap eliteRecord=new IntKeyHashMap();
	/** ������� **/
	int attackNums;
	/** ����ʱ�� **/
	int attackTime;

	/** ������������ **/
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
	/** �Ƿ����ɨ�� **/
	public boolean canattact(int sid)
	{
		EliteRecord record=(EliteRecord)eliteRecord.get(sid);
		if(record==null) return false;
		return record.getStarNum()>=SelfCheckPoint.THREE_STAR;
	}
	/** �ж��Ƿ����ս */
	public String canChalleng(int sid,int times)
	{
		if(attackNums>=PublicConst.FIGHT_ATTACK_LENGTH) return "times  out";
		int effectiveTimes=PublicConst.FIGHT_ATTACK_LENGTH-attackNums;
		if(effectiveTimes<times) return "times limit";
		int timeNow=TimeKit.getSecondTime();
		// ������ͬһ����ˢ��
		if(!SeaBackKit.isSameDay(timeNow,attackTime)) clear();
		return null;
	}
	/** �����ս���� **/
	public void clear()
	{
		attackNums=0;
	}
	/** ��ȡ�������� **/
	public int getPointStar(int sid)
	{
		EliteRecord record=(EliteRecord)eliteRecord.get(sid);
		if(record==null) return 0;
		return record.getStarNum();
	}
	/** ���л���ǰ̨ **/
	public void showBytesWrite(ByteBuffer data)
	{
		if(attackTime!=0
			&&!SeaBackKit.isSameDay(TimeKit.getSecondTime(),attackTime))
			clear();
		// �������
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

	/** ������������л����ֽڻ����� */
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

	/** �����л� */
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
