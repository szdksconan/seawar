package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * 活跃度任务条件
 * 
 * @author Alan
 * 
 */
public class VitalityCondition extends Condition
{

	int eventType;

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		return this;
	}

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==eventType)
		{
			return Task.TASK_FINISH;
		}
		else
			return 0;
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{

	}

}
