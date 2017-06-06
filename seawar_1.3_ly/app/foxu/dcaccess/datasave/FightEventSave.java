package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.event.FightEvent;

/**
 * ս���¼���װ��
 * fightEvent
 * author:icetiger
 * */
public class FightEventSave extends ObjectSave
{
	/**ս���¼�*/
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
		// TODO �Զ����ɷ������
		return event;
	}

	@Override
	public int getId()
	{
		// TODO �Զ����ɷ������
		return event.getId();
	}

	@Override
	public void setData(Object data)
	{
		// TODO �Զ����ɷ������
		event = (FightEvent)data;
	}

}
