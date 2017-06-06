package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * ������������
 * 
 * @author Alan
 */
public class HCityPointCondition extends Condition
{

	/** �ؿ�sid */
	int pointSid;
	/** �ؿ����� */
	int stars;
	/** ��ʼ�ؿ� */
	int startSid;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==PublicConst.HCITY_POINT_EVENT)
		{
			if(getDestPointStart(player)<stars) return Task.TASK_CHANGE;
		}
		if(getDestPointStart(player)>=stars) return Task.TASK_FINISH;
		return 0;
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		int currentSid=p.getHeritagePoint().getCheckPointSid();
		// ��ǰ�ؿ�����ʼ�ؿ��Ĳ���(����)
		int count=currentSid-startSid;
		// �������˹ؿ�������������ж�
		if(currentSid>=pointSid)
		{
			// ����δ���
			count=pointSid-startSid-1;
			// �����Ѵ��
			if(getDestPointStart(p)>=stars)
				count++;
		}
		data.writeByte(count);
	}
	
	public int getDestPointStart(Player player)
	{
		CheckPoint point=(CheckPoint)CheckPoint.factory.getSample(pointSid);
		return player.getHeritagePoint().getStar(point.getChapter()-1,
			point.getIndex());
	}

}
