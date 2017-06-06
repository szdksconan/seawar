package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.Fighter;
import foxu.fight.TouchOffSpread;

/**
 * 某回合某时机 激活效果
 * 
 * @author yw
 * 
 */
public class RoundActiveSpread extends TouchOffSpread
{

	/** 是否必然命中 */
	boolean mustHit;// =true;
	/** 转变为其他 触发时机 */
	int transTime;// =TouchOffSpread.HURT;
	/** 触发回合 */
	int targetRound;
	/** 当前回合 */
	int currentRound;

	/* methods */
	public int checkUsed(Fighter source,Object target,Ability ability)
	{
		int touch=super.checkUsed(source,target,ability);
		if(touch!=0) return touch;
		if(getTouchOffTime()==transTime) return touch;
		if(currentRound!=targetRound)
		{
			return FAILD_TIME_ERROR;
		}
		return touch;
	}

	/** 源角色对目标角色使用该技能 */
	public boolean used(Fighter source,Object target,Ability ability)
	{
		if(getTouchOffTime()!=transTime)
		{
			setTouchOffTime(transTime);
		}
		return false;
	}
	
	/** 检测是否必然命中 */
	public boolean isMustHit()
	{
		if(getTouchOffTime()==transTime&&mustHit) return true;
		return false;
	}

	public int getTargetRound()
	{
		return targetRound;
	}

	public void setTargetRound(int targetRound)
	{
		this.targetRound=targetRound;
	}

	public int getCurrentRound()
	{
		return currentRound;
	}

	public void setCurrentRound(int currentRound)
	{
		this.currentRound=currentRound;
	}

	public int getTransTime()
	{
		return transTime;
	}

	public void setTransTime(int transTime)
	{
		this.transTime=transTime;
	}

}
