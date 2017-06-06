package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.FightScene;
import foxu.fight.Fighter;



/**
 * 为释放源添加一个技能
 * @author liuh
 *
 */
public class AddAbilityEffectForSource extends Effect implements EffectAble
{
	int[] abilitySid;

	@Override
	public float used(Fighter source,Object target,float data,int type)
	{
		return data;
	}

	@Override
	public Object used(Fighter source,Object target,Object data)
	{
		Ability ability=null;
		for(int i=abilitySid.length-1;i>=0;i--)
		{
			ability=(Ability)FightScene.abilityFactory.newSample(abilitySid[i]);
			source.addAbility(ability,source.getCurrentRound());
		}
		return null;
	}

}
