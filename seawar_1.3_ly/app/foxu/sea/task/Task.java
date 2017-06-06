package foxu.sea.task;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;

/**
 * 玩家任务 author:icetiger
 */
public class Task extends Sample
{

	/** TASK_CHANGE=1任务改变 TASK_FINISH=2任务完成 都需要刷新前台 */
	public static final int TASK_CHANGE=1,TASK_FINISH=2;
	/**
	 * 任务类型TASK_NOMARL=1普通任务 TASK_DAY=2每日任务 TASK_NEW_PLAYER=3引导任务
	 * TASK_SUBMIT_FOR_AWARD=4 TASK_RANDOM_CHOOSE=16每日任务的随机选取
	 */
	public static final int TASK_NOMARL=1,TASK_DAY=2,TASK_NEW_GUIDE=4,
					TASK_SUBMIT_FOR_AWARD=8,TASK_RANDOM_CHOOSE=16,
					TASK_VITALITY=32;
	/** Serialization fileds */
	/** 任务是否已经完成 */
	boolean finish;
	/** 任务条件 */
	Condition condition[];
	/** 是否自动加载 */
	boolean autoAdd=true;

	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();

	/** 从字节数组中反序列化获得对象的域 */
	public static Task bytesReadTask(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Task r=(Task)factory.newSample(sid);
		if(r==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Task.class.getName()
					+" bytesRead, invalid sid:"+sid);
		r.bytesRead(data);
		return r;
	}

	/** configure fileds */
	/** 任务key */
	int key;
	/** 任务value */
	int value;
	/** 获得value值的左移位量(配置方便用,从1到32) */
	int showValue;
	/** 任务星级 */
	int taskStar;
	/** 接任务等级限制 */
	int limitLevel;
	/** 前置任务的key 完成了才能接受这个任务 */
	int limiteKey[];
	/** 前置任务的value 完成了才能接受这个任务 */
	int limiteShowValue[];
	/** 名字 */
	String name;
	/** 任务类型 每日任务 常规任务 引导任务 */
	int taskType;
	/** 奖励品sid */
	int awardSid;
	/** 任务描述 */
	String description;
	/** 每日任务限制次数 */
	int dayLimiteTime;
	/** 新手引导未完成时是否开放 */
	boolean isOpenNew;
	/** 指挥中心等级 */
	int commandCenter;

	/** value值 */
	public int getValue()
	{
		if(value!=0) return value;
		this.value=1<<(showValue-1);
		return value;
	}

	/** 检查条件 0未改变 1改变 2完成 */
	public int checkCondition(TaskEvent event,Player player)
	{
		int checkState=Task.TASK_FINISH;
		if(condition==null) return checkState;
		for(int i=0;i<condition.length;i++)
		{
			checkState=condition[i].checkCondition(player,event);
			if(checkState!=Task.TASK_FINISH) return checkState;
		}
		return checkState;
	}

	/** 序列化条件 */
	public void bytesReadCondition(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		if(length>0)
		{
			for(int i=0;i<length;i++)
			{
				condition[i].bytesRead(data);
			}
		}
	}

	/** 序列化条件 */
	public void bytesWriteCondition(ByteBuffer data)
	{
		if(condition==null)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(condition.length);
			for(int i=0;i<condition.length;i++)
			{
				condition[i].bytesWrite(data);
			}
		}
	}

	/** 从字节缓存中反序列化得到一个对象 */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		finish=data.readBoolean();
		bytesReadCondition(data);
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeBoolean(finish);
		bytesWriteCondition(data);
	}
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		super.bytesWrite(data);
		data.writeBoolean(finish);
		if(condition==null) return;
		for(int i=0;i<condition.length;i++)
		{
			condition[i].showBytesWrite(data,p);
		}
	}

	/** 发送奖励 */
	public void sendAward(Player player)
	{
		// 发送奖励 不要改变这个award
		Award award=(Award)Award.factory.getSample(getAwardSid());
		if(award!=null)
			award.awardSelf(player,TimeKit.getSecondTime(),null,null,null,
				new int[]{EquipmentTrack.FROM_TASK});
	}

	/**
	 * @return awardSid
	 */
	public int getAwardSid()
	{
		return awardSid;
	}

	/**
	 * @param awardSid 要设置的 awardSid
	 */
	public void setAwardSid(int awardSid)
	{
		this.awardSid=awardSid;
	}

	/**
	 * @return key
	 */
	public int getKey()
	{
		return key;
	}

	/**
	 * @param key 要设置的 key
	 */
	public void setKey(int key)
	{
		this.key=key;
	}

	/**
	 * @return taskType
	 */
	public int getTaskType()
	{
		return taskType;
	}

	/**
	 * @param taskType 要设置的 taskType
	 */
	public void setTaskType(int taskType)
	{
		this.taskType=taskType;
	}

	/**
	 * @return finish
	 */
	public boolean isFinish()
	{
		return finish;
	}

	/**
	 * @param finish 要设置的 finish
	 */
	public void setFinish(boolean finish)
	{
		this.finish=finish;
	}

	/**
	 * @return dayLimiteTime
	 */
	public int getDayLimiteTime()
	{
		return dayLimiteTime;
	}

	/**
	 * @param dayLimiteTime 要设置的 dayLimiteTime
	 */
	public void setDayLimiteTime(int dayLimiteTime)
	{
		this.dayLimiteTime=dayLimiteTime;
	}

	/** copy方法，深层复制 */
	public Object copy(Object obj)
	{
		Task t=(Task)super.copy(obj);
		if(condition!=null)
		{
			t.condition=new Condition[condition.length];
			for(int i=0;i<condition.length;i++)
			{
				t.condition[i]=(Condition)condition[i].clone();
			}
		}
		return t;
	}

	/**
	 * @return limitLevel
	 */
	public int getLimitLevel()
	{
		return limitLevel;
	}

	/**
	 * @param limitLevel 要设置的 limitLevel
	 */
	public void setLimitLevel(int limitLevel)
	{
		this.limitLevel=limitLevel;
	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name 要设置的 name
	 */
	public void setName(String name)
	{
		this.name=name;
	}

	/**
	 * @return condition
	 */
	public Condition[] getCondition()
	{
		return condition;
	}

	/**
	 * @param condition 要设置的 condition
	 */
	public void setCondition(Condition[] condition)
	{
		this.condition=condition;
	}

	/**
	 * @return taskStar
	 */
	public int getTaskStar()
	{
		return taskStar;
	}

	/**
	 * @param taskStar 要设置的 taskStar
	 */
	public void setTaskStar(int taskStar)
	{
		this.taskStar=taskStar;
	}

	/**
	 * @return limiteKey
	 */
	public int[] getLimiteKey()
	{
		return limiteKey;
	}

	/**
	 * @param limiteKey 要设置的 limiteKey
	 */
	public void setLimiteKey(int[] limiteKey)
	{
		this.limiteKey=limiteKey;
	}

	/**
	 * @return limiteShowValue
	 */
	public int[] getLimiteShowValue()
	{
		return limiteShowValue;
	}

	/**
	 * @param limiteShowValue 要设置的 limiteShowValue
	 */
	public void setLimiteShowValue(int[] limiteShowValue)
	{
		this.limiteShowValue=limiteShowValue;
	}

	/** 新手引导未完成时是否开放 */
	public boolean isOpenNew()
	{
		return isOpenNew;
	}

	
	public void setOpenNew(boolean isOpenNew)
	{
		this.isOpenNew=isOpenNew;
	}

	/** 指挥中心等级限制 */
	public int getCommandCenter()
	{
		return commandCenter;
	}

	
	public void setCommandCenter(int commandCenter)
	{
		this.commandCenter=commandCenter;
	}

}
