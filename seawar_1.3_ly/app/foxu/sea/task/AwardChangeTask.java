package foxu.sea.task;

import foxu.sea.Player;
import foxu.sea.Resources;

/** ����Ʒ�һ����� ���Ӳ� */
public class AwardChangeTask extends Task
{

	/** �һ���Դ���� ��ŵ�����Դtypeֵ */
	int resourceType[];
	/** �һ���Դ���� */
	int resourceNum[];
	/** ����Ʒsid */
	int awardSid[];

	/** ��̬ */
	int resource[]=new int[Player.RESOURCES_SIZE];
	
	/**
	 * ��ȡָ�����͵���Դ����
	 * @param type
	 * @return
	 */
	public int getResource(int type)
	{
		if(type<0||type>=resource.length)
			return 0;
		return resource[type];
	}

	/** ������� */
	public int checkCondition(TaskEvent event,Player player)
	{
		if(event==null) return 0;
		for(int i=0;i<resource.length;i++)
		{
			resource[i]=0;
		}
		if(event.getEventType()<resourceType.length)
		{
			resource[resourceType[event.getEventType()]]=resourceNum[event
				.getEventType()];
			boolean bool=Resources.checkResources(resource,player
				.getResources());
			if(bool)
			{
				setAwardSid(awardSid[event.getEventType()]);
				return Task.TASK_FINISH;
			}
		}
		return 0;
	}

	/** ���ͽ��� */
	public void sendAward(Player player)
	{
		// �۳��һ���Դ
		Resources.reduceResources(player.getResources(),resource,player);
		// �۳���ʯ
		Resources.reduceGems(resource[Resources.GEMS],player.getResources(),
			player);
		// ���ͽ��� ��Ҫ�ı����award
		super.sendAward(player);
	}

	/** copy��������㸴�� */
	public Object copy(Object obj)
	{
		AwardChangeTask t=(AwardChangeTask)super.copy(obj);
		t.resource=new int[Player.RESOURCES_SIZE];
		return t;
	}

}
