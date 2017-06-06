package foxu.sea.task;

import mustang.io.ByteBuffer;
import mustang.util.Sample;
import foxu.sea.Player;

/**
 * 任务条件 author:icetiger
 */
public abstract class Condition extends Sample
{

	public void bytesWrite(ByteBuffer data)
	{
		
	}
	
	/** 检查任务条件 0未改变 1改变 2完成*/
	public abstract int checkCondition(Player player,TaskEvent event);
	public abstract void showBytesWrite(ByteBuffer data,Player p);
}
