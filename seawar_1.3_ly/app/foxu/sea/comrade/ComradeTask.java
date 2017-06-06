package foxu.sea.comrade;

import mustang.util.Sample;
import mustang.util.SampleFactory;


/**
 * ս��ϵͳ  ����
 * @author yw
 *
 */
public class ComradeTask extends Sample
{
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	
	/**��������**/
	public static int COMRADETASK_TYPE=3;
	public static int RECHARGE=1,LEVEL=2,FIGHT_SCORE=3;
	/** ����ɴ��� */
	int complete;
	/** ��������  */
	int taskType;
	/** �������� */
	int taskCondition;
	/** ����sid */
	int awardSid;
	/** ��¼ key */
	int key;
	/** ��¼ value */
	int value;
	
	public static int getRECHARGE()
	{
		return RECHARGE;
	}
	
	public static void setRECHARGE(int rECHARGE)
	{
		RECHARGE=rECHARGE;
	}
	
	public static int getLEVEL()
	{
		return LEVEL;
	}
	
	public static void setLEVEL(int lEVEL)
	{
		LEVEL=lEVEL;
	}
	
	public static int getFIGHT_SCORE()
	{
		return FIGHT_SCORE;
	}
	
	public static void setFIGHT_SCORE(int fIGHT_SCORE)
	{
		FIGHT_SCORE=fIGHT_SCORE;
	}
	
	
	public int getAwardSid()
	{
		return awardSid;
	}
	
	public void setAwardSid(int awardSid)
	{
		this.awardSid=awardSid;
	}
	
	public int getTaskType()
	{
		return taskType;
	}
	
	public void setTaskType(int taskType)
	{
		this.taskType=taskType;
	}
	
	public int getTaskCondition()
	{
		return taskCondition;
	}
	
	public void setTaskCondition(int taskCondition)
	{
		this.taskCondition=taskCondition;
	}
	
	public int getKey()
	{
		return key;
	}
	
	public void setKey(int key)
	{
		this.key=key;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public void setValue(int value)
	{
		this.value=value;
	}

	
	public int getComplete()
	{
		return complete;
	}

	
	public void setComplete(int complete)
	{
		this.complete=complete;
	}
	
	
}
