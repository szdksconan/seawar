package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * 遗忘都市条件
 * 
 * @author Alan
 */
public class HCityPointCondition extends Condition
{

	/** 关卡sid */
	int pointSid;
	/** 关卡星数 */
	int stars;
	/** 起始关卡 */
	int startSid;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==PublicConst.HCITY_POINT_EVENT)
		{
			if(getDestPointStart(player)<stars) return Task.TASK_CHANGE;
		}
		if(getDestPointStart(player)>=stars) return Task.TASK_FINISH;
		return 0;
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		int currentSid=p.getHeritagePoint().getCheckPointSid();
		// 当前关卡与起始关卡的差数(进度)
		int count=currentSid-startSid;
		// 如果打过此关卡，则进行星数判定
		if(currentSid>=pointSid)
		{
			// 星数未达标
			count=pointSid-startSid-1;
			// 星数已达标
			if(getDestPointStart(p)>=stars)
				count++;
		}
		data.writeByte(count);
	}
	
	public int getDestPointStart(Player player)
	{
		CheckPoint point=(CheckPoint)CheckPoint.factory.getSample(pointSid);
		return player.getHeritagePoint().getStar(point.getChapter()-1,
			point.getIndex());
	}

}
