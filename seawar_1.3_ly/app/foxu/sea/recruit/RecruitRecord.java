package foxu.sea.recruit;

import foxu.sea.PublicConst;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;

/**
 * 新兵福利记录
 * 
 * @author yw
 * 
 */
public class RecruitRecord
{

	/** 福利超时时间(包含领奖时间) */
	int timeout;
	/** 角色创建了多少天 */
	int days;
	/** 奖励记录 */
	int[] awardMark={};
	/** 任务记录(taskType---value) (进度值) */
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
	
	/** 获取任务进度 */
	public int getTaskProcess(RecruitDayTask task)
	{
		Integer value=(Integer)taskMarks.get(task.getType());
		return value==null?0:value;
	}
	/** 获取任务奖励状态 0不可领，1可领，2已领 */
	public int getTaskAwardSate(RecruitDayTask task)
	{
		int awardSate=getAwardSate(task.getAward(),0,0);
		if(awardSate==2)return awardSate;
		int value=getTaskProcess(task);
		return task.checkTask(value)?1:0;
	}
	/** 获取奖励状态  0不可领，1可领，2已领*/
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
