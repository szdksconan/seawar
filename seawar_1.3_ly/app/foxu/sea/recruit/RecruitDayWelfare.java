package foxu.sea.recruit;

import foxu.sea.Player;
import foxu.sea.award.Award;
import mustang.io.ByteBuffer;
import mustang.set.ObjectArray;

/**
 * 新兵每日福利
 * 
 * @author yw
 * 
 */
public class RecruitDayWelfare
{

	/** 第几天 */
	int day;
	/** 普通奖励 */
	RecruitAward normalAward;
	/** vip限制 */
	int viplv;
	/** vip奖励 */
	RecruitAward vipAward;
	/** 每日任务 */
	ObjectArray tasks=new ObjectArray();
	/** 半价抢购 */
	RecruitAward halfAward;

	/** 显示序列化 */
	public void showBytesWrite(ByteBuffer data,RecruitRecord record,Player player)
	{
		// 普通奖励
		data.writeByte(record.getAwardSate(normalAward,0,0));// 领取状态
		data.writeByte(0);// vip限制
		((Award)Award.factory.getSample(normalAward.getSid())).viewAward(
			data,player);
		// vip奖励
		data.writeByte(record.getAwardSate(vipAward,viplv,
			player.getUser_state()));// 领取状态
		data.writeByte(viplv);// vip限制
		((Award)Award.factory.getSample(vipAward.getSid())).viewAward(data,
			player);
		// 任务
		sendBytesWrite(data,record);
		// 半价商品
		data.writeBoolean(record.getAwardSate(halfAward,0,0)==2?true:false);// 购买状态
		data.writeShort(halfAward.getSid());
	}
	
	/** 推送序列化 */
	public void sendBytesWrite(ByteBuffer data,RecruitRecord record)
	{
		// 任务
		data.writeByte(tasks.size());
		Object[] objs=tasks.getArray();
		for(int i=0;i<objs.length;i++)
		{
			RecruitDayTask task=(RecruitDayTask)objs[i];
			data.writeInt(record.getTaskProcess(task));// 进度
			data.writeInt(task.getValue());// 条件
			data.writeUTF(task.getDescr());// 描述
			data.writeByte(record.getTaskAwardSate(task));// 领取状态
			Award award=(Award)Award.factory.getSample(task.getAward().getSid());
			award.viewAward(data,null);
			
		}
	}
	
	/** 追加任务 */
	public void addTask(RecruitDayTask task)
	{
		tasks.add(task);
	}
	
	/** 检测今天是否是目标任务类型 */
	public boolean checkTaskType(int taskType)
	{
		RecruitDayTask task=(RecruitDayTask)tasks.get();
		return task.getType()==taskType;
	}

	public int getDay()
	{
		return day;
	}

	public void setDay(int day)
	{
		this.day=day;
	}

	public RecruitAward getNormalAward()
	{
		return normalAward;
	}

	public void setNormalAward(RecruitAward normalAward)
	{
		this.normalAward=normalAward;
	}

	public int getViplv()
	{
		return viplv;
	}

	public void setViplv(int viplv)
	{
		this.viplv=viplv;
	}

	public RecruitAward getVipAward()
	{
		return vipAward;
	}

	public void setVipAward(RecruitAward vipAward)
	{
		this.vipAward=vipAward;
	}

	public ObjectArray getTasks()
	{
		return tasks;
	}

	public RecruitDayTask getTasks(int index)
	{

		return (RecruitDayTask)tasks.getArray()[index];
	}

	public void setTasks(ObjectArray tasks)
	{
		this.tasks=tasks;
	}

	public RecruitAward getHalfAward()
	{
		return halfAward;
	}

	public void setHalfAward(RecruitAward halfAward)
	{
		this.halfAward=halfAward;
	}

}
