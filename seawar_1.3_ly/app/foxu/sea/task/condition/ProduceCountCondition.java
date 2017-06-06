package foxu.sea.task.condition;

import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.Product;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * 生产任务累计
 * @author Alan
 *
 */
public class ProduceCountCondition extends BuildShipCondition
{

	/** 对应的建筑sid */
	int buildSid;
	/** 产品sid */
	int productSid;
	public int checkCondition(Player player,TaskEvent event)
	{
		int checkStat=0;
		if(event!=null
			&&event.getEventType()==PublicConst.PRODUCE_COUNT_TASK_EVENT)
		{
			if(event.getSource() instanceof Product
				&&(Integer)event.getParam()==buildSid)
			{
				Product product=(Product)event.getSource();
				if(product.getSid()==productSid||productSid==0)
					nowNum+=product.getNum();
				if(nowNum>=num)
				{
					nowNum=num;
					return Task.TASK_FINISH;
				}
				checkStat=Task.TASK_CHANGE;
			}

		}
		return checkStat;
	}
}
