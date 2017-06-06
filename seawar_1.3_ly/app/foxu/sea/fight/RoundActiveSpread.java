package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.Fighter;
import foxu.fight.TouchOffSpread;

/**
 * ĳ�غ�ĳʱ�� ����Ч��
 * 
 * @author yw
 * 
 */
public class RoundActiveSpread extends TouchOffSpread
{

	/** �Ƿ��Ȼ���� */
	boolean mustHit;// =true;
	/** ת��Ϊ���� ����ʱ�� */
	int transTime;// =TouchOffSpread.HURT;
	/** �����غ� */
	int targetRound;
	/** ��ǰ�غ� */
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

	/** Դ��ɫ��Ŀ���ɫʹ�øü��� */
	public boolean used(Fighter source,Object target,Ability ability)
	{
		if(getTouchOffTime()!=transTime)
		{
			setTouchOffTime(transTime);
		}
		return false;
	}
	
	/** ����Ƿ��Ȼ���� */
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
