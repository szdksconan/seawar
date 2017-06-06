package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;


/**
 * 环球军演完成次数
 * @author Alan
 *
 */
public class ArenaCountCondition extends Condition
{
	int num;
	int nowNum;
	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==PublicConst.ATTACK_ARENA_TASK_EVENT)
		{
			nowNum++;
			if(nowNum>=num)
				return Task.TASK_FINISH;
			return Task.TASK_CHANGE;
		}
		return 0;
	}


	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		nowNum=data.readUnsignedByte();
		return this;
	}
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(nowNum);
	}

	public void showBytesWrite(ByteBuffer data,Player p)
	{
		super.bytesWrite(data);
		data.writeByte(nowNum);
	}

}
