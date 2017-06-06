package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * 军备航线条件
 * 
 * @author Alan
 */
public class ArmsPointCondition extends Condition
{

	/** 星数 */
	int stars;
	/** 关卡sid */
	int pointSid;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
//		if(event!=null&&event.getEventType()==PublicConst.ARMS_POINT_EVENT)
//		{
			if(getDestPointStar(player)>=stars)
					return Task.TASK_FINISH;
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
		return player.getArmsroutePoint().getPointStar(pointSid);
	}

}
