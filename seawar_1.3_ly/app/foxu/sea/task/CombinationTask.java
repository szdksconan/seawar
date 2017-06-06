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
 * 组合任务 用于每日任务 author:icetiger
 */
public class CombinationTask extends Task
{
	/** 每个星级任务的概率 */
	public static final int TASK_FIVE_STARS[]={45,70,85,95,100};
	/** 从7个类型的任务中选 */
	public static final int TEN_TASK_TYPE[]={0,1,2,3,4,5,6};
	/** 从5个星级的该类型任务中选 */
	public static final int FIVE_TASK_STAR[]={0,1,2,3,4};
	/** 随机的任务数量 */
	public static final int RANDOM_SIDS=5,TASK_MAX_STAR=5,DAY_TASK_LIMITE=5;

	/** Serialization fileds */
	/** 当前选中的任务 玩家选择 */
	Task currentTask;
	/** 当前随机出来的5个任务sid */
	int randomTasksSid[]=new int[RANDOM_SIDS];

	/** configure fileds 冒号分隔 */
	String taskCombinSids[];

	/** 动态 */
	int taskSids[][];

	/** 玩家从5个任务中选取一个任务接受 */
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

	/** 放弃当前任务 */
	public void giveUpTask()
	{
		setFinish(false);
		currentTask=null;
	}

	/** 随机5个任务 给玩家选 */
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
		// 随机选取一个类型不能重复
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

	/** 发送奖励 */
	public void sendAward(Player player)
	{
		if(currentTask==null) return;
		// 发送奖励 不要改变这个award
		Award award=(Award)Award.factory
			.getSample(currentTask.getAwardSid());
		if(award!=null)
			award.awardSelf(player,TimeKit.getSecondTime(),null,null,null,
				new int[]{EquipmentTrack.FROM_TASK});
		currentTask=null;
		// 重新随机5个任务
		randomSids();
	}

	/** 检查条件 */
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

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteRandomTasks(ByteBuffer data)
	{
		data.writeByte(randomTasksSid.length);
		for(int i=0;i<randomTasksSid.length;i++)
		{
			data.writeShort(randomTasksSid[i]);
		}
	}

	/** 从字节缓存中反序列化得到一个对象 */
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

	/** 将对象的域序列化到字节缓存中 */
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
	 * @param randomTasksSid 要设置的 randomTasksSid
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
	 * @param currentTask 要设置的 currentTask
	 */
	public void setCurrentTask(Task currentTask)
	{
		this.currentTask=currentTask;
	}

	/** copy方法，深层复制 */
	public Object copy(Object obj)
	{
		CombinationTask t=(CombinationTask)super.copy(obj);
		if(currentTask!=null) t.currentTask=(Task)currentTask.clone();
		t.randomTasksSid=new int[RANDOM_SIDS];
		return t;
	}
}
