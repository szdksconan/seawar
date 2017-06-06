/**
 * 
 */
package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.AttackEffect;
import foxu.fight.ChangeHurtEffect;
import foxu.fight.Fighter;


/**
 * 根据攻击者的舰船类型改变伤害
 * 
 * @author rockzyt
 */
public class ChangeHurtByShipEffect extends ChangeHurtEffect
{
	
	/* fields */
	/** 舰船类型 */
	int attackType;
	
	/* methods */
	public float used(Fighter source,Object target,float data,int type)
	{
		if(type!=getHurtTime()) return data;
		if(target instanceof Fighter)
		{
			Fighter fTargter=(Fighter)target;
			Ability ability=fTargter.getAbility();
			if(((AttackEffect)ability.getEffects()[0]).getAttackType()==attackType)
				return super.used(source,target,data,type);
		}
		return data;
	}
}
