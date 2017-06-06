package foxu.sea.task;

import mustang.util.Sample;
import foxu.sea.Player;

/**
 * 任务事件处理器 author
 */
public class TaskEventExecute
{

	/** 单列 */
	private static TaskEventExecute taskEventExecute=new TaskEventExecute();

	/** 单列 */
	private TaskEventExecute()
	{

	}

	/** 获取唯一实例 */
	public static TaskEventExecute getInstance()
	{
		return taskEventExecute;
	}

	/** 获取任务的samples */
	public Sample[] getTaskSamples()
	{
		return Task.factory.getSamples();
	}

	/** 执行任务事件的构造和检查 */
	public void executeEvent(int eventType,Object source,Player player,Object param)
	{
		TaskEvent event=CreatEvent(eventType,source,param);
		checkTastEvent(event,player.getTaskManager());
	}

	/** 检查条件 */
	private void checkTastEvent(TaskEvent event,TaskManager taskManager)
	{
		if(event==null) return;
		taskManager.checkTastEvent(event);
	}

	/** 根据type创建事件对象 */
	private TaskEvent CreatEvent(int eventType,Object source,Object param)
	{
		// 新建建筑
		TaskEvent event=new TaskEvent();
		event.setSource(source);
		event.setEventType(eventType);
		event.setParam(param);
		return event;
	}

}
