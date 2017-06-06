package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** ��ʯ�������� */
public class GemsCostConditon extends Condition
{
	/** time time����Ҫ�� */
	int time;
	/** ���Ҫ�� */
	int gems;

	/** ��ǰ���� */
	int nowTime;
	/** ��ǰ���ѵı�ʯ */
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
		// TODO �Զ����ɷ������
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
