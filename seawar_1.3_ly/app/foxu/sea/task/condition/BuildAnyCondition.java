package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** 建筑条件 操作事件 建筑不一定完成 */
public class BuildAnyCondition extends BuildCondition
{

	/** 需要建筑的次数 */
	int time;
	/** 当前建筑的次数 */
	int nowTime;

	/** 检查条件 */
	public int checkCondition(Player player,TaskEvent event)
	{
		boolean change=false;
		// 建筑建造和升级事件 还没有完成
		if(event!=null&&event.getSource() instanceof PlayerBuild
			&&event.getEventType()==PublicConst.BUILD_FINISH_TASK_EVENT)
		{
			nowTime++;
			change=true;
			if(nowTime>=time)
			{
				nowTime=time;
				return Task.TASK_FINISH;
			}
		}
		if(change) return Task.TASK_CHANGE;
		return 0;
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		nowTime=data.readUnsignedByte();
		return this;
	}
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(nowTime);
	}

	public void showBytesWrite(ByteBuffer data,Player p)
	{
		super.bytesWrite(data);
		data.writeByte(nowTime);
	}
}
