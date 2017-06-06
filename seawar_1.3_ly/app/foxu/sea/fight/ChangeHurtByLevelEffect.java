package foxu.sea.fight;

import foxu.fight.ChangeHurtEffect;


/**
 * 根据技能等级来计算改变伤害的百分比
 * 
 * @author rockzyt
 */
public class ChangeHurtByLevelEffect extends ChangeHurtEffect
{

	/* properties */
	/** 获得改变伤害百分比 */
	public int getHurtPrecent()
	{
		return getHurtPrecent()*getAbility().getLevel();
	}
}
