package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;


/**
 * 星石等级条件
 * @author Alan
 *
 */
public class StarStoneLevelCondition extends Condition
{
	/** 技能sid */
	int skillSid;
	/** 技能等级 */
	int level;
	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==PublicConst.STAR_STONE_LEVEL_EVENT)
		{
			if(player.getShipAbilityLevel(skillSid)<level)
				return Task.TASK_CHANGE;
		}
		if(player.getShipAbilityLevel(skillSid)>=level)
				return Task.TASK_FINISH;
		return 0;
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		data.writeByte(p.getShipAbilityLevel(skillSid));
	}

}
