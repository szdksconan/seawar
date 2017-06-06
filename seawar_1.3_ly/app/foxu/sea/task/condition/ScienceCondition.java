package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Science;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** �Ƽ����� */
public class ScienceCondition extends Condition
{

	/** �Ƽ�sid */
	int scienceSid;
	/** �Ƽ��ȼ� */
	int scienceLevel;
	/** ��Ҫ���� */
	int needTime;

	/** ���л� ��ǰ�з����� */
	int nowTime;

	/* methods */
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
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null
			&&event.getEventType()==PublicConst.SCIENCE_LEVEL_UP_EVENT)
		{
			if(scienceSid!=0)
			{
				Science science=(Science)event.getSource();
				if(science.getSid()==scienceSid)
				{
					if(science.getLevel()>=scienceLevel)
						return Task.TASK_FINISH;
				}
			}
			else
			{
				nowTime++;
				if(nowTime>=needTime)
				{
					nowTime = needTime;
					return Task.TASK_FINISH;
				}
				return Task.TASK_CHANGE;
			}
		}
		return 0;
	}
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		data.writeByte(nowTime);
	}
}
