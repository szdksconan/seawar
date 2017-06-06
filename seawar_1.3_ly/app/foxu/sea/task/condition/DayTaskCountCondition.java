package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import mustang.set.ObjectArray;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * �ճ�����ۼ�
 * @author Alan
 */
public class DayTaskCountCondition extends Condition
{

	/** ��Ҫ��ɵĴ��� */
	int time;
	/** ��ǰ��ɵĴ��� */
	int nowTime;
	/** ��ǰϵͳ���ճ�����(��̬) */
	Object[] dayTasks;
	
	/** ������� */
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getSource() instanceof Task
			&&(((Task)event.getSource()).getTaskType()&Task.TASK_DAY)!=0
			&&event.getEventType()==PublicConst.DAY_TASK_COUNT_EVENT)
		{
			nowTime++;
			if(nowTime>=time)
			{
				nowTime=time;
				return Task.TASK_FINISH;
			}
			return Task.TASK_CHANGE;
		}
		int count=0;
		Object[] ts=getDayTasks();
		for(int i=0;i<ts.length;i++)
		{
			Task task=(Task)ts[i];
			count+=player.getTaskManager().getDayTaskDateTime(task.getKey(),
				task.getValue());
		}
		nowTime=count;
		if(nowTime>=time)
		{
			nowTime=time;
			return Task.TASK_FINISH;
		}
		return 0;
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		nowTime=data.readUnsignedByte();
		return this;
	}
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(nowTime);
	}

	public void showBytesWrite(ByteBuffer data,Player p)
	{
		super.bytesWrite(data);
		data.writeByte(nowTime);
	}
	
	public Object[] getDayTasks()
	{
		if(dayTasks==null)
		{
			ObjectArray tasks_temp=new ObjectArray();
			Object[] tasks=Task.factory.getSamples();
			Task task;
			for(int i=0;i<tasks.length;i++)
			{
				if(!(tasks[i] instanceof Task))	continue;
				task=(Task)tasks[i];
				if(task!=null&&((task.getTaskType()&Task.TASK_DAY)!=0))
					tasks_temp.add(task);
			}
			dayTasks=tasks_temp.getArray();
		}
		return dayTasks;
	}
}
