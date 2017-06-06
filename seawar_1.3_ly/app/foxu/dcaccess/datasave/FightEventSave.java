package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.event.FightEvent;

/**
 * 战斗事件封装类
 * fightEvent
 * author:icetiger
 * */
public class FightEventSave extends ObjectSave
{
	/**战斗事件*/
    FightEvent event;
	
	@Override
	public ByteBuffer getByteBuffer()
	{
		if(event==null)return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		event.bytesWrite(bb);
		return bb;
	}

	@Override
	public FightEvent getData()
	{
		// TODO 自动生成方法存根
		return event;
	}

	@Override
	public int getId()
	{
		// TODO 自动生成方法存根
		return event.getId();
	}

	@Override
	public void setData(Object data)
	{
		// TODO 自动生成方法存根
		event = (FightEvent)data;
	}

}
