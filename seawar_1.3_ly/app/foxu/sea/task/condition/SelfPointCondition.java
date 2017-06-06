package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.checkpoint.SelfCheckPoint;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** �ؿ�ͨ���Ǹ�sid�� */
public class SelfPointCondition extends Condition
{
	/** �ؿ�sid */
	int pointSid;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==PublicConst.POINT_SUCCESS_TASK_EVENT)
		{
			// TODO �Զ����ɷ������
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
		// TODO �Զ����ɷ������
	}

}
