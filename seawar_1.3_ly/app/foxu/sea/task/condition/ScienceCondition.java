package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Science;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** 科技条件 */
public class ScienceCondition extends Condition
{

	/** 科技sid */
	int scienceSid;
	/** 科技等级 */
	int scienceLevel;
	/** 需要次数 */
	int needTime;

	/** 序列化 当前研发次数 */
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
