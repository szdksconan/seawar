package foxu.sea.recruit;

import foxu.sea.PublicConst;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;

/**
 * �±�������¼
 * 
 * @author yw
 * 
 */
public class RecruitRecord
{

	/** ������ʱʱ��(�����콱ʱ��) */
	int timeout;
	/** ��ɫ�����˶����� */
	int days;
	/** ������¼ */
	int[] awardMark={};
	/** �����¼(taskType---value) (����ֵ) */
	IntKeyHashMap taskMarks=new IntKeyHashMap();

	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(timeout);
		data.writeInt(days);
		data.writeByte(awardMark.length);
		for(int i=0;i<awardMark.length;i++)
		{
			data.writeInt(awardMark[i]);
		}
		data.writeByte(taskMarks.size());
		int[] keys=taskMarks.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			data.writeByte(keys[i]);
			data.writeInt((Integer)taskMarks.get(keys[i]));
		}
	}

	public void bytesRead(ByteBuffer data)
	{
		timeout=data.readInt();
		// System.out.println("----------timeout:"+SeaBackKit.formatDataTime(timeout));
		days=data.readInt();
		// System.out.println("----------days:"+days);
		int len=data.readUnsignedByte();
		awardMark=new int[len];
		for(int i=0;i<len;i++)
		{
			awardMark[i]=data.readInt();
		}
		len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			taskMarks.put(data.readUnsignedByte(),data.readInt());
		}

	}

	public boolean showBytesWrite(ByteBuffer data)
	{
		int awardTime=timeout-TimeKit.getSecondTime();
		data.writeInt(awardTime>0?awardTime:0);
		if(awardTime<=0) return true;
		int actTime=awardTime-RecruitKit.AWARD_DAYS*PublicConst.DAY_SEC;
		data.writeInt(actTime>0?actTime:0);
		return false;

	}
	
	/** ��ȡ������� */
	public int getTaskProcess(RecruitDayTask task)
	{
		Integer value=(Integer)taskMarks.get(task.getType());
		return value==null?0:value;
	}
	/** ��ȡ������״̬ 0�����죬1���죬2���� */
	public int getTaskAwardSate(RecruitDayTask task)
	{
		int awardSate=getAwardSate(task.getAward(),0,0);
		if(awardSate==2)return awardSate;
		int value=getTaskProcess(task);
		return task.checkTask(value)?1:0;
	}
	/** ��ȡ����״̬  0�����죬1���죬2����*/
	public int getAwardSate(RecruitAward award,int vipLv,int nowVip)
	{
		int key=award.getKey();
		int value=award.getValue();
		int state=0;
		if(key>=awardMark.length)
		{
			state=vipLv>=nowVip?1:0;
		}
		else
		{
			state=(awardMark[key]&(1<<value))==0?1:2;
		}
		return state;
	}

	public int getDays()
	{
		return days;
	}

	public void setDays(int days)
	{
		this.days=days;
	}

	public int[] getAwardMark()
	{
		return awardMark;
	}

	public void setAwardMark(int[] awardMark)
	{
		this.awardMark=awardMark;
	}
	public IntKeyHashMap getTaskMarks()
	{
		return taskMarks;
	}

	public void setTaskMarks(IntKeyHashMap taskMarks)
	{
		this.taskMarks=taskMarks;
	}

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout=timeout;
	}
}
