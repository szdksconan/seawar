package foxu.sea.task;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.SeaBackKit;

/**
 * ������� ����ÿ������ author:icetiger
 */
public class CombinationTask extends Task
{
	/** ÿ���Ǽ�����ĸ��� */
	public static final int TASK_FIVE_STARS[]={45,70,85,95,100};
	/** ��7�����͵�������ѡ */
	public static final int TEN_TASK_TYPE[]={0,1,2,3,4,5,6};
	/** ��5���Ǽ��ĸ�����������ѡ */
	public static final int FIVE_TASK_STAR[]={0,1,2,3,4};
	/** ������������� */
	public static final int RANDOM_SIDS=5,TASK_MAX_STAR=5,DAY_TASK_LIMITE=5;

	/** Serialization fileds */
	/** ��ǰѡ�е����� ���ѡ�� */
	Task currentTask;
	/** ��ǰ���������5������sid */
	int randomTasksSid[]=new int[RANDOM_SIDS];

	/** configure fileds ð�ŷָ� */
	String taskCombinSids[];

	/** ��̬ */
	int taskSids[][];

	/** ��Ҵ�5��������ѡȡһ��������� */
	public boolean getTaskBySid(int sid)
	{
		if(randomTasksSid==null||randomTasksSid.length<RANDOM_SIDS)
			return false;
		for(int i=0;i<randomTasksSid.length;i++)
		{
			if(randomTasksSid[i]==sid)
			{
				currentTask=(Task)Task.factory.newSample(sid);
				return true;
			}
		}
		return false;
	}

	/** ������ǰ���� */
	public void giveUpTask()
	{
		setFinish(false);
		currentTask=null;
	}

	/** ���5������ �����ѡ */
	public int[] randomSids()
	{
		if(taskSids==null)
		{
			taskSids=new int[taskCombinSids.length][TASK_MAX_STAR];
			for(int i=0;i<taskCombinSids.length;i++)
			{
				String sids[]=TextKit.split(taskCombinSids[i],":");
				for(int j=0;j<TASK_MAX_STAR;j++)
				{
					taskSids[i][j]=Integer.parseInt(sids[j]);
				}
			}
		}
		// ���ѡȡһ�����Ͳ����ظ�
		int randomTaskType[]=SeaBackKit.getRandomNums(TEN_TASK_TYPE,
			RANDOM_SIDS);
		for(int i=0;i<randomTaskType.length;i++)
		{
			int random=MathKit.randomValue(0,101);
			int index=0;
			for(int x=0;x<TASK_FIVE_STARS.length;x++)
			{
				if(random<=TASK_FIVE_STARS[0])
				{
					break;
				}
				if(random>TASK_FIVE_STARS[x]&&random<=TASK_FIVE_STARS[x+1])
				{
					index=x+1;
					break;
				}
			}
			randomTasksSid[i]=taskSids[randomTaskType[i]][index];
		}
		setFinish(false);
		currentTask=null;
		return randomTasksSid;
	}

	/** ���ͽ��� */
	public void sendAward(Player player)
	{
		if(currentTask==null) return;
		// ���ͽ��� ��Ҫ�ı����award
		Award award=(Award)Award.factory
			.getSample(currentTask.getAwardSid());
		if(award!=null)
			award.awardSelf(player,TimeKit.getSecondTime(),null,null,null,
				new int[]{EquipmentTrack.FROM_TASK});
		currentTask=null;
		// �������5������
		randomSids();
	}

	/** ������� */
	public int checkCondition(TaskEvent event,Player player)
	{
		if(currentTask==null) return 0;
		int checkState=0;
		Condition condition[]=currentTask.getCondition();
		for(int i=0;i<condition.length;i++)
		{
			checkState=condition[i].checkCondition(player,event);
		}
		return checkState;
	}

	public void bytesReadRandomTasks(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		randomTasksSid=new int[length];
		for(int i=0;i<length;i++)
		{
			randomTasksSid[i]=data.readUnsignedShort();
		}
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteRandomTasks(ByteBuffer data)
	{
		data.writeByte(randomTasksSid.length);
		for(int i=0;i<randomTasksSid.length;i++)
		{
			data.writeShort(randomTasksSid[i]);
		}
	}

	/** ���ֽڻ����з����л��õ�һ������ */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		bytesReadRandomTasks(data);
		boolean bool=data.readBoolean();
		if(bool)
		{
			currentTask=Task.bytesReadTask(data);
		}
		return this;
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		bytesWriteRandomTasks(data);
		if(currentTask!=null)
		{
			data.writeBoolean(true);
			currentTask.bytesWrite(data);
		}
		else
		{
			data.writeBoolean(false);
		}
	}

	public void showBytesWrite(ByteBuffer data,Player p)
	{
		super.showBytesWrite(data,p);
		if(currentTask!=null)
			data.writeShort(currentTask.getSid());
		else
			data.writeShort(0);
		data.writeByte(randomTasksSid.length);
		for(int i=0;i<randomTasksSid.length;i++)
		{
			Task t=(Task)Task.factory.newSample(randomTasksSid[i]);
			if(currentTask!=null&&currentTask.getSid()==t.getSid())
			{
				currentTask.showBytesWrite(data,p);
			}
			else
			{
				t.showBytesWrite(data,p);
			}
		}
	}

	/**
	 * @return randomTasksSid
	 */
	public int[] getRandomTasksSid()
	{
		return randomTasksSid;
	}

	/**
	 * @param randomTasksSid Ҫ���õ� randomTasksSid
	 */
	public void setRandomTasksSid(int[] randomTasksSid)
	{
		this.randomTasksSid=randomTasksSid;
	}

	/**
	 * @return currentTask
	 */
	public Task getCurrentTask()
	{
		return currentTask;
	}

	/**
	 * @param currentTask Ҫ���õ� currentTask
	 */
	public void setCurrentTask(Task currentTask)
	{
		this.currentTask=currentTask;
	}

	/** copy��������㸴�� */
	public Object copy(Object obj)
	{
		CombinationTask t=(CombinationTask)super.copy(obj);
		if(currentTask!=null) t.currentTask=(Task)currentTask.clone();
		t.randomTasksSid=new int[RANDOM_SIDS];
		return t;
	}
}
