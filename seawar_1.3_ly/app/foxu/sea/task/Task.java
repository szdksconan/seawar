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
 * ������� author:icetiger
 */
public class Task extends Sample
{

	/** TASK_CHANGE=1����ı� TASK_FINISH=2������� ����Ҫˢ��ǰ̨ */
	public static final int TASK_CHANGE=1,TASK_FINISH=2;
	/**
	 * ��������TASK_NOMARL=1��ͨ���� TASK_DAY=2ÿ������ TASK_NEW_PLAYER=3��������
	 * TASK_SUBMIT_FOR_AWARD=4 TASK_RANDOM_CHOOSE=16ÿ����������ѡȡ
	 */
	public static final int TASK_NOMARL=1,TASK_DAY=2,TASK_NEW_GUIDE=4,
					TASK_SUBMIT_FOR_AWARD=8,TASK_RANDOM_CHOOSE=16,
					TASK_VITALITY=32;
	/** Serialization fileds */
	/** �����Ƿ��Ѿ���� */
	boolean finish;
	/** �������� */
	Condition condition[];
	/** �Ƿ��Զ����� */
	boolean autoAdd=true;

	/** �������� */
	public static SampleFactory factory=new SampleFactory();

	/** ���ֽ������з����л���ö������ */
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
	/** ����key */
	int key;
	/** ����value */
	int value;
	/** ���valueֵ������λ��(���÷�����,��1��32) */
	int showValue;
	/** �����Ǽ� */
	int taskStar;
	/** ������ȼ����� */
	int limitLevel;
	/** ǰ�������key ����˲��ܽ���������� */
	int limiteKey[];
	/** ǰ�������value ����˲��ܽ���������� */
	int limiteShowValue[];
	/** ���� */
	String name;
	/** �������� ÿ������ �������� �������� */
	int taskType;
	/** ����Ʒsid */
	int awardSid;
	/** �������� */
	String description;
	/** ÿ���������ƴ��� */
	int dayLimiteTime;
	/** ��������δ���ʱ�Ƿ񿪷� */
	boolean isOpenNew;
	/** ָ�����ĵȼ� */
	int commandCenter;

	/** valueֵ */
	public int getValue()
	{
		if(value!=0) return value;
		this.value=1<<(showValue-1);
		return value;
	}

	/** ������� 0δ�ı� 1�ı� 2��� */
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

	/** ���л����� */
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

	/** ���л����� */
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

	/** ���ֽڻ����з����л��õ�һ������ */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		finish=data.readBoolean();
		bytesReadCondition(data);
		return this;
	}

	/** ������������л����ֽڻ����� */
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

	/** ���ͽ��� */
	public void sendAward(Player player)
	{
		// ���ͽ��� ��Ҫ�ı����award
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
	 * @param awardSid Ҫ���õ� awardSid
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
	 * @param key Ҫ���õ� key
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
	 * @param taskType Ҫ���õ� taskType
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
	 * @param finish Ҫ���õ� finish
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
	 * @param dayLimiteTime Ҫ���õ� dayLimiteTime
	 */
	public void setDayLimiteTime(int dayLimiteTime)
	{
		this.dayLimiteTime=dayLimiteTime;
	}

	/** copy��������㸴�� */
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
	 * @param limitLevel Ҫ���õ� limitLevel
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
	 * @param name Ҫ���õ� name
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
	 * @param condition Ҫ���õ� condition
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
	 * @param taskStar Ҫ���õ� taskStar
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
	 * @param limiteKey Ҫ���õ� limiteKey
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
	 * @param limiteShowValue Ҫ���õ� limiteShowValue
	 */
	public void setLimiteShowValue(int[] limiteShowValue)
	{
		this.limiteShowValue=limiteShowValue;
	}

	/** ��������δ���ʱ�Ƿ񿪷� */
	public boolean isOpenNew()
	{
		return isOpenNew;
	}

	
	public void setOpenNew(boolean isOpenNew)
	{
		this.isOpenNew=isOpenNew;
	}

	/** ָ�����ĵȼ����� */
	public int getCommandCenter()
	{
		return commandCenter;
	}

	
	public void setCommandCenter(int commandCenter)
	{
		this.commandCenter=commandCenter;
	}

}
