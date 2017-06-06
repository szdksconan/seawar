package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.task.AwardChangeTask;
import foxu.sea.task.CombinationTask;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;
import foxu.sea.task.TaskEventExecute;
import foxu.sea.task.TaskManager;

/** ����˿� 1005 */
public class TaskPort extends AccessPort
{
	/** ��ѫ����sid */
	public static int HONOR_TASK=2000;
	CreatObjectFactory objectFactory;

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		// ����sid
		int taskSid=data.readUnsignedShort();
		TaskManager manager=player.getTaskManager();
		Task task=manager.getTaskBySid(taskSid);
		if(task==null)
		{
			JBackKit.sendResetTask(s,player);
			throw new DataAccessException(0,"task is null");
		}
		// �ر�����
		if(type==PublicConst.TASK_REPORT_TYPE)
		{
			if(!task.isFinish())
			{
				JBackKit.sendResetTask(s,player);
				throw new DataAccessException(0,"task is not finish");
			}
			manager.reportTask(taskSid);
			data.clear();
			if((task.getTaskType()&Task.TASK_RANDOM_CHOOSE)!=0)
			{
				task.showBytesWrite(data,player);
				data.writeByte(player.getTaskManager().getDayTaskDateTime(
					task.getKey(),task.getValue()));
			}
			else
				data.writeShort(taskSid);
		}
		// �����һ�����
		else if(type==PublicConst.TASK_EXCHANGE_AWARD)
		{
			// ѡ����Ŀ
			int chooseType=data.readUnsignedByte();
			// TODO ��ʱ��ѫVIP�ȼ��ж�
//			if(chooseType>=1&&player.getUser_state()<1)
//			{
//				throw new DataAccessException(0,"chooseType is wrong");
//			}
//			if(chooseType>=2&&player.getUser_state()<4)
//			{
//				throw new DataAccessException(0,"chooseType is wrong");
//			}
//			if(chooseType>=3&&player.getUser_state()<6)
//			{
//				throw new DataAccessException(0,"chooseType is wrong");
//			}
			if((task.getTaskType()&Task.TASK_SUBMIT_FOR_AWARD)==0
				||(task.getTaskType()&Task.TASK_DAY)==0)
			{
				JBackKit.sendResetTask(s,player);
				throw new DataAccessException(0,"task is wrong");
			}
			// �鿴�������
			int time=manager.getDayTaskDateTime(task.getKey(),task
				.getValue());
			// ��������õ�����
			if(time>=task.getDayLimiteTime())
			{
				JBackKit.sendResetTask(s,player);
				throw new DataAccessException(0,
					"today changeTask times limite");
			}
			// TODO �ж��Ƿ���ѡ���������
			TaskEvent event=new TaskEvent();
			event.setSource(this);
			event.setEventType(chooseType);
			int checkState=task.checkCondition(event,player);
			if(checkState==0)
			{
				JBackKit.sendResetTask(s,player);
				throw new DataAccessException(0,"resource limit");
			}
			task.setFinish(true);
			manager.reportTask(taskSid);
			if(task instanceof AwardChangeTask)
			{
				AwardChangeTask act=(AwardChangeTask)task;
				int needGems=act.getResource(Resources.GEMS);
				if(needGems>0)
				{
					// ��ʯ���Ѽ�¼
					objectFactory.createGemTrack(GemsTrack.OTHER,player.getId(),
						needGems,act.getSid(),Resources.getGems(player.getResources()));
					// ����change��Ϣ
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.GEMS_ADD_SOMETHING,this,player,needGems);
				}
				//��ѫ��Ծ������
				if(task.getSid()==HONOR_TASK){
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.HONOR_UP_TASK_EVENT,null,player,null);
				}
			}
		}
		// ÿ���������ȡ
		else if(type==PublicConst.TASK_DAY_RANDOM_CHOOSE)
		{
			if(!(task instanceof CombinationTask))
			{
				throw new DataAccessException(0,"is not combinationTask");
			}
			CombinationTask taskCombin=(CombinationTask)task;
			int getTaskSid=data.readUnsignedShort();
			// ��������
			if(player.getTaskManager().getDayTaskDateTime(
				taskCombin.getKey(),taskCombin.getValue())>=CombinationTask.DAY_TASK_LIMITE)
			{
				throw new DataAccessException(0,"dayTask times limite");
			}
			if(!taskCombin.getTaskBySid(getTaskSid))
			{
				JBackKit.sendAddTaskPlayer(player,taskCombin);
				throw new DataAccessException(0,"error");
			}
			data.clear();
			data.writeShort(getTaskSid);
		}
		// // �������Ϊ��ʱ�������5������
		// else if(type==PublicConst.TASK_DAY_RANDOM)
		// {
		// if(!(task instanceof CombinationTask))
		// {
		// throw new DataAccessException(0,"is not combinationTask");
		// }
		// CombinationTask taskCombin=(CombinationTask)task;
		// if(taskCombin.getRandomTasksSid()[0]==0)
		// {
		// taskCombin.randomSids();
		// }
		// // int ramdom[]=taskCombin.getRandomTasksSid();
		// // data.writeByte(ramdom.length);
		// // for(int i=0;i<ramdom.length;i++)
		// // {
		// // data.writeShort(ramdom[i]);
		// // }
		// data.clear();
		// taskCombin.showBytesWrite(data,player);
		// }
		// ��ʯˢ���������
		else if(type==PublicConst.TASK_DAY_GEMS_RANDOM)
		{
			if(!(task instanceof CombinationTask))
			{
				throw new DataAccessException(0,"is not combinationTask");
			}
			CombinationTask taskCombin=(CombinationTask)task;
			// ��������
			if(player.getTaskManager().getDayTaskDateTime(
				taskCombin.getKey(),taskCombin.getValue())>=CombinationTask.DAY_TASK_LIMITE)
			{
				throw new DataAccessException(0,"today times limite");
			}
			if(!Resources.checkGems(PublicConst.TASK_DAY_NEED_GEMS,player
				.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			Resources.reduceGems(PublicConst.TASK_DAY_NEED_GEMS,player
				.getResources(),player);
			// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.OTHER,player.getId(),
				PublicConst.TASK_DAY_NEED_GEMS,taskCombin.getSid(),
				Resources.getGems(player.getResources()));
			taskCombin.randomSids();
			player.setAttribute(PublicConst.DAY_TASK_UPDATE,TimeKit.getSecondTime()+"");
			// ����ǰ̨������������sid
			data.clear();
			task.showBytesWrite(data,player);
			data.writeByte(player.getTaskManager().getDayTaskDateTime(
				task.getKey(),task.getValue()));
			// ����change��Ϣ
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				PublicConst.TASK_DAY_NEED_GEMS);
		}
		//���ˢ��ÿ������
		else if(type==PublicConst.FREE_RESET_TASK)
		{
			if(!(task instanceof CombinationTask))
			{
				throw new DataAccessException(0,"is not combinationTask");
			}
			CombinationTask taskCombin=(CombinationTask)task;
			data.clear();
			int cd=player.getDayUpdateCD();
			if(cd>0)
			{
				data.writeBoolean(false);
				data.writeShort(cd);
			}else
			{
				taskCombin.randomSids();
				data.writeBoolean(true);
				task.showBytesWrite(data,player);
				data.writeByte(player.getTaskManager().getDayTaskDateTime(
					task.getKey(),task.getValue()));
				player.setAttribute(PublicConst.DAY_TASK_UPDATE,TimeKit.getSecondTime()+"");
			}
			
		}
		// �������ĳ��ÿ������
		else if(type==PublicConst.GEMS_FINISH)
		{
			if(!(task instanceof CombinationTask))
			{
				throw new DataAccessException(0,"is not combinationTask");
			}
			CombinationTask taskCombin=(CombinationTask)task;
			if(taskCombin.getCurrentTask()==null)
			{
				JBackKit.sendAddTaskPlayer(player,taskCombin);
				throw new DataAccessException(0,"error");
			}
			int needGems=taskCombin.getCurrentTask().getTaskStar()*5;
			if(!Resources.checkGems(needGems,player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.OTHER,player.getId(),
				needGems,taskCombin.getCurrentTask().getSid(),
				Resources.getGems(player.getResources()));
			Resources.reduceGems(needGems,player.getResources(),player);
			taskCombin.setFinish(true);
			manager.reportTask(taskSid);
			taskCombin.randomSids();
			// ����ǰ̨������������sid
			data.clear();
			task.showBytesWrite(data,player);
			data.writeByte(player.getTaskManager().getDayTaskDateTime(
				task.getKey(),task.getValue()));
		}
		// ������ǰ�ճ�����
		else if(type==PublicConst.GIVE_UP)
		{
			if(!(task instanceof CombinationTask))
			{
				throw new DataAccessException(0,"is not combinationTask");
			}
			CombinationTask taskCombin=(CombinationTask)task;
			taskCombin.giveUpTask();
		}
		// ����ÿ������
		else if(type==PublicConst.RESET_DAY_TASK)
		{
			if(!(task instanceof CombinationTask))
			{
				throw new DataAccessException(0,"is not combinationTask");
			}
			// ��ʯ���
			if(!Resources.checkGems(PublicConst.RESET_TASK_GEMS,player
				.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			// ��ǰ���õĴ���
			int time=player.getResetTaskNum();
			// VIP�ȼ��������õĴ���
			int canTime=PublicConst.RESET_TASK_NUM[player.getUser_state()];
			if(time>=canTime)
			{
				throw new DataAccessException(0,"dayTask reset time limit");
			}
			// �۳���ʯ
			Resources.reduceGems(PublicConst.RESET_TASK_GEMS,player
				.getResources(),player);
			// ��������
			player.getTaskManager().resetDayTask(task.getKey(),
				task.getValue());
			// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.OTHER,player.getId(),
				PublicConst.RESET_TASK_GEMS,task.getSid(),
				Resources.getGems(player.getResources()));
		}
		return data;
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory Ҫ���õ� objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
}
