package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;


public class TearPointCondition extends Condition
{
	/** 关卡sid */
	int pointSid;
	/** 关卡星数 */
	int stars=1;
	
	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
//		if(event!=null&&event.getEventType()==PublicConst.TEARPOINT_SUCCESS_TASK_EVENT)
//		{
//			// TODO 自动生成方法存根
//			CheckPoint point=(CheckPoint)event.getSource();
//			int sid=point.getSid();
//			if(sid>=pointSid) return Task.TASK_FINISH;
//		}else
//		{
			if(getDestPointStar(player)>=stars) return Task.TASK_FINISH;
//		}
		return 0;
		
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		int count=0;
		if(getDestPointStar(p)>=stars)
			count=1;
		data.writeByte(count);
	}

	public int getDestPointStar(Player player)
	{
		CheckPoint point=(CheckPoint)CheckPoint.factory.getSample(pointSid);
		return player.getTearCheckPoint().getStar(point.getChapter()-1,
			point.getIndex());
	}
	
}
