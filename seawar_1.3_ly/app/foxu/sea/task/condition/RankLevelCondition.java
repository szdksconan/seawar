package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** ���������ȼ����� */
public class RankLevelCondition extends Condition
{

	/** ���εȼ� */
	int randLevel;
	/** �����ȼ� */
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
		// TODO �Զ����ɷ������
	}
}
