package foxu.sea.task;

import foxu.sea.Player;
import foxu.sea.Resources;

/** 奖励品兑换任务 如视察 */
public class AwardChangeTask extends Task
{

	/** 兑换资源类型 存放的是资源type值 */
	int resourceType[];
	/** 兑换资源数量 */
	int resourceNum[];
	/** 奖励品sid */
	int awardSid[];

	/** 动态 */
	int resource[]=new int[Player.RESOURCES_SIZE];
	
	/**
	 * 获取指定类型的资源需求
	 * @param type
	 * @return
	 */
	public int getResource(int type)
	{
		if(type<0||type>=resource.length)
			return 0;
		return resource[type];
	}

	/** 检查条件 */
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

	/** 发送奖励 */
	public void sendAward(Player player)
	{
		// 扣除兑换资源
		Resources.reduceResources(player.getResources(),resource,player);
		// 扣除宝石
		Resources.reduceGems(resource[Resources.GEMS],player.getResources(),
			player);
		// 发送奖励 不要改变这个award
		super.sendAward(player);
	}

	/** copy方法，深层复制 */
	public Object copy(Object obj)
	{
		AwardChangeTask t=(AwardChangeTask)super.copy(obj);
		t.resource=new int[Player.RESOURCES_SIZE];
		return t;
	}

}
