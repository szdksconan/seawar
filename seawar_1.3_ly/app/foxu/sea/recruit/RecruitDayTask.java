package foxu.sea.recruit;


/**
 * �±�ÿ������
 * @author yw
 *
 */
public class RecruitDayTask
{

	/**
	 * �������� CHECKPOINT_STARS=�ؿ�����,DOCK_LV=����ȼ�,EXERCISE_RANK=��������,
	 * SCIENCE_LV=�Ƽ��ȼ�,COMMAND_LV=ͳ���ȼ�,RES_BUILD_LV=��Դ�����ȼ�, FIGHT_SCORE=ս��
	 */
	public static int CHECKPOINT_STARS=1,DOCK_LV=2,EXERCISE_RANK=3,
					SCIENCE_LV=4,COMMAND_LV=5,RES_BUILD_LV=6,FIGHT_SCORE=7;
	/** �������� */
	int type;
	/** ��ֵ */
	int value;
	/** ����id */
	String descr="asdf";
	/** �������� */
	RecruitAward award;
	
	public boolean checkTask(int value)
	{
		if(value<=0)return false;
		return this.value<=value;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type=type;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value=value;
	}

	public RecruitAward getAward()
	{
		return award;
	}

	public void setAward(RecruitAward award)
	{
		this.award=award;
	}

	
	public String getDescr()
	{
		return descr;
	}

	
	public void setDescr(String descr)
	{
		this.descr=descr;
	}

}
