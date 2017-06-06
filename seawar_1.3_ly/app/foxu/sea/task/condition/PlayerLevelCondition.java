package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** ��ҵȼ� */
public class PlayerLevelCondition extends Condition
{
	/** ��ҵȼ� */
	int playerLevel;
	/** ����ȼ� */
	int islandLevel;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
//		if(event!=null&&event.getEventType()==PublicConst.PLAYER_LEVEL_ISLAND_EVENT)
//		{
			if(playerLevel>player.getLevel()) return 0;
			if(islandLevel>player.getIsland().getIslandLevel())
				return 0;
			return Task.TASK_FINISH;
//		}
//		return 0;
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		data.writeByte(p.getLevel());
	}

}
