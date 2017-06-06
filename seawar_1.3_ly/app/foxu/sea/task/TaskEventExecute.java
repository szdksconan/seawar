package foxu.sea.task;

import mustang.util.Sample;
import foxu.sea.Player;

/**
 * �����¼������� author
 */
public class TaskEventExecute
{

	/** ���� */
	private static TaskEventExecute taskEventExecute=new TaskEventExecute();

	/** ���� */
	private TaskEventExecute()
	{

	}

	/** ��ȡΨһʵ�� */
	public static TaskEventExecute getInstance()
	{
		return taskEventExecute;
	}

	/** ��ȡ�����samples */
	public Sample[] getTaskSamples()
	{
		return Task.factory.getSamples();
	}

	/** ִ�������¼��Ĺ���ͼ�� */
	public void executeEvent(int eventType,Object source,Player player,Object param)
	{
		TaskEvent event=CreatEvent(eventType,source,param);
		checkTastEvent(event,player.getTaskManager());
	}

	/** ������� */
	private void checkTastEvent(TaskEvent event,TaskManager taskManager)
	{
		if(event==null) return;
		taskManager.checkTastEvent(event);
	}

	/** ����type�����¼����� */
	private TaskEvent CreatEvent(int eventType,Object source,Object param)
	{
		// �½�����
		TaskEvent event=new TaskEvent();
		event.setSource(source);
		event.setEventType(eventType);
		event.setParam(param);
		return event;
	}

}
