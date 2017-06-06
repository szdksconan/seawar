/**
 * 
 */
package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.FightScene;
import foxu.fight.Fighter;



/**
 * 为释放目标添加一个技能
 * @author rockzyt
 *
 */
public class AddAbilityEffect extends Effect implements EffectAble
{
	/* fields */
	int[] abilitySid;

	/* methods */
	@Override
	public float used(Fighter source,Object target,float data,int type)
	{
		return data;
	}

	@Override
	public Object used(Fighter source,Object target,Object data)
	{
		Ability ability=null;
		Fighter fighter=(Fighter)target;
		for(int i=abilitySid.length-1;i>=0;i--)
		{
			ability=(Ability)FightScene.abilityFactory.newSample(abilitySid[i]);
			fighter.addAbility(ability,source.getCurrentRound());
		}
		return null;
	}

}
