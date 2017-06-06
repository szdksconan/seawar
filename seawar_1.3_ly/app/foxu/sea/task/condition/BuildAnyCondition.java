package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** �������� �����¼� ������һ����� */
public class BuildAnyCondition extends BuildCondition
{

	/** ��Ҫ�����Ĵ��� */
	int time;
	/** ��ǰ�����Ĵ��� */
	int nowTime;

	/** ������� */
	public int checkCondition(Player player,TaskEvent event)
	{
		boolean change=false;
		// ��������������¼� ��û�����
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
