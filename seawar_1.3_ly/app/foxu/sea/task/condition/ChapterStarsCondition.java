package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.checkpoint.SelfCheckPoint;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** ĳ���½��Ƿ�õ�ָ������ */
public class ChapterStarsCondition extends Condition
{

	/** �½� */
	int chapter;
	/** ���� */
	int stars;

	/** ǰ̨��Ҫ */
	int nowStar;

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null
			&&event.getEventType()==PublicConst.CHAPTER_STARTS_TASK_EVENT)
		{
			SelfCheckPoint point=player.getSelfCheckPoint();
			int starList[]=point.getList();
			nowStar=0;
			// �������»�ȡ������
			for(int i=(chapter-1)*SelfCheckPoint.CHAPTER_NUM;i<starList.length;i++)
			{
				if(i>=chapter*SelfCheckPoint.CHAPTER_NUM) break;
				nowStar+=starList[i];
			}
			if(nowStar>=stars)
			{
				nowStar= stars;
				return Task.TASK_FINISH;
			}
			return Task.TASK_CHANGE;
		}
		return 0;
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		// TODO �Զ����ɷ������
		data.writeByte(nowStar);
	}

}
