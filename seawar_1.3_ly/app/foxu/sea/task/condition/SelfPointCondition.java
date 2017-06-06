package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.checkpoint.SelfCheckPoint;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** 关卡通过那个sid了 */
public class SelfPointCondition extends Condition
{
	/** 关卡sid */
	int pointSid;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==PublicConst.POINT_SUCCESS_TASK_EVENT)
		{
			// TODO 自动生成方法存根
			CheckPoint point=(CheckPoint)event.getSource();
			int sid=point.getSid();
			if(sid>=pointSid) return Task.TASK_FINISH;
			return 0;
		}
		else
		{
			SelfCheckPoint point = player.getSelfCheckPoint();
			if(point.getCheckPointSid()>pointSid)return Task.TASK_FINISH;
			return 0;
		}
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		// TODO 自动生成方法存根
	}

}
