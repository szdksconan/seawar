package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.Effect;
import foxu.fight.FightScene;
import foxu.fight.Fighter;
import foxu.fight.IntervalChangeEffect;

/**
 * @author liuh
 * �������˺� ����Ѫ���ٷֱȵ�Ѫ
 */
public class AddPercentIntervalHurtEffect extends AddAbilityEffect
{
	
	int value;
	
	public float used(Fighter source,Object target,float data,int type)
	{
		Ability ability=(Ability)FightScene.abilityFactory.newSample(abilitySid[0]);
		Effect[] effects=ability.getEffects();
		if(value>0)
		{
			((IntervalChangeEffect)effects[0]).setEffectValue(value<1?1:value);
		}
		else
		{
			((IntervalChangeEffect)effects[0]).setEffectValue(value>-1?-1:value);
		}
		((Fighter)target).addAbility(ability,source.getCurrentRound());
		return data;
	}
}
