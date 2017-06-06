package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** 宝石消耗条件 */
public class GemsCostConditon extends Condition
{
	/** time time次数要求 */
	int time;
	/** 额度要求 */
	int gems;

	/** 当前次数 */
	int nowTime;
	/** 当前消费的宝石 */
	int nowGems;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==PublicConst.GEMS_ADD_SOMETHING)
		{
			nowTime++;
			nowGems+=Integer.parseInt(event.getParam().toString());
			if(time>nowTime) return Task.TASK_CHANGE;
			if(gems>nowGems) return Task.TASK_CHANGE;
			if(nowGems>=gems)nowGems=gems;
			return Task.TASK_FINISH;
		}
		return 0;
	}

	
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		// TODO 自动生成方法存根
		super.bytesWrite(data);
		data.writeByte(nowTime);
		data.writeShort(nowGems);
	}

	/* methods */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		nowTime=data.readUnsignedByte();
		nowGems=data.readUnsignedShort();
		return this;
	}
	
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(nowTime);
		data.writeShort(nowGems);
	}

}
