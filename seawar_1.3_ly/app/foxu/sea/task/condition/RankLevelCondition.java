package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** 军衔声望等级条件 */
public class RankLevelCondition extends Condition
{

	/** 军衔等级 */
	int randLevel;
	/** 声望等级 */
	int honorLevel;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(randLevel>player.getPlayerType()) return 0;
		if(honorLevel>player.getHonor()[1]) return 0;
		return Task.TASK_FINISH;
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		// TODO 自动生成方法存根
	}
}
