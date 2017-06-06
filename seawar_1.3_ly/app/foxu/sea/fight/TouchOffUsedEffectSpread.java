/**
 * 
 */
package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.EffectAble;
import foxu.fight.Fighter;
import foxu.fight.TouchOffSpread;


/**
 * @author rockzyt
 * 触发后执行具体效果的释放方式
 */
public class TouchOffUsedEffectSpread extends TouchOffSpread
{
	/* fields */
	/** 技能效果 */
	EffectAble effect;

	/* methods */
	/** 源角色对目标角色使用该技能 */
	public boolean used(Fighter source,Object target,Ability ability)
	{
		effect.used(source,target,ability);
		return false;
	}
}
