package foxu.sea.recruit;

import foxu.sea.Player;
import foxu.sea.award.Award;
import mustang.io.ByteBuffer;
import mustang.set.ObjectArray;

/**
 * �±�ÿ�ո���
 * 
 * @author yw
 * 
 */
public class RecruitDayWelfare
{

	/** �ڼ��� */
	int day;
	/** ��ͨ���� */
	RecruitAward normalAward;
	/** vip���� */
	int viplv;
	/** vip���� */
	RecruitAward vipAward;
	/** ÿ������ */
	ObjectArray tasks=new ObjectArray();
	/** ������� */
	RecruitAward halfAward;

	/** ��ʾ���л� */
	public void showBytesWrite(ByteBuffer data,RecruitRecord record,Player player)
	{
		// ��ͨ����
		data.writeByte(record.getAwardSate(normalAward,0,0));// ��ȡ״̬
		data.writeByte(0);// vip����
		((Award)Award.factory.getSample(normalAward.getSid())).viewAward(
			data,player);
		// vip����
		data.writeByte(record.getAwardSate(vipAward,viplv,
			player.getUser_state()));// ��ȡ״̬
		data.writeByte(viplv);// vip����
		((Award)Award.factory.getSample(vipAward.getSid())).viewAward(data,
			player);
		// ����
		sendBytesWrite(data,record);
		// �����Ʒ
		data.writeBoolean(record.getAwardSate(halfAward,0,0)==2?true:false);// ����״̬
		data.writeShort(halfAward.getSid());
	}
	
	/** �������л� */
	public void sendBytesWrite(ByteBuffer data,RecruitRecord record)
	{
		// ����
		data.writeByte(tasks.size());
		Object[] objs=tasks.getArray();
		for(int i=0;i<objs.length;i++)
		{
			RecruitDayTask task=(RecruitDayTask)objs[i];
			data.writeInt(record.getTaskProcess(task));// ����
			data.writeInt(task.getValue());// ����
			data.writeUTF(task.getDescr());// ����
			data.writeByte(record.getTaskAwardSate(task));// ��ȡ״̬
			Award award=(Award)Award.factory.getSample(task.getAward().getSid());
			award.viewAward(data,null);
			
		}
	}
	
	/** ׷������ */
	public void addTask(RecruitDayTask task)
	{
		tasks.add(task);
	}
	
	/** �������Ƿ���Ŀ���������� */
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
