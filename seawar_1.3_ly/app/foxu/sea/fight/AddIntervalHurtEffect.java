/**
 * 
 */
package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.Effect;
import foxu.fight.FightScene;
import foxu.fight.Fighter;
import foxu.fight.IntervalChangeEffect;


/**
 * @author rockzyt
 * 添加一个中毒类型技能,数值根据伤害值计算
 */
public class AddIntervalHurtEffect extends AddAbilityEffect
{
	/* fields */
	int value;
	
	/* methods */
	public float used(Fighter source,Object target,float data,int type)
	{
		Ability ability=(Ability)FightScene.abilityFactory.newSample(abilitySid[0]);
		Effect[] effects=ability.getEffects();
		float v=data*value/100;
		if(value>0)
		{
			((IntervalChangeEffect)effects[0]).setEffectValue(v<1?1:v);
		}
		else
		{
			((IntervalChangeEffect)effects[0]).setEffectValue(v>-1?-1:v);
		}
		
		((Fighter)target).addAbility(ability,source.getCurrentRound());
		return data;
	}
}