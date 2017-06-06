package foxu.sea.recruit;


/**
 * 新兵每日任务
 * @author yw
 *
 */
public class RecruitDayTask
{

	/**
	 * 任务类型 CHECKPOINT_STARS=关卡星数,DOCK_LV=船坞等级,EXERCISE_RANK=军演排名,
	 * SCIENCE_LV=科技等级,COMMAND_LV=统御等级,RES_BUILD_LV=资源建筑等级, FIGHT_SCORE=战力
	 */
	public static int CHECKPOINT_STARS=1,DOCK_LV=2,EXERCISE_RANK=3,
					SCIENCE_LV=4,COMMAND_LV=5,RES_BUILD_LV=6,FIGHT_SCORE=7;
	/** 任务类型 */
	int type;
	/** 限值 */
	int value;
	/** 翻译id */
	String descr="asdf";
	/** 奖励内容 */
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
