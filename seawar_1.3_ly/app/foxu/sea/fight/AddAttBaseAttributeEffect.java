package foxu.sea.fight;

import foxu.fight.AttBaseAttribute;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.Fighter;

/**
 * 攻击向的 基础属性 加成 
 * @author liuh
 *
 */
public class AddAttBaseAttributeEffect extends Effect implements EffectAble
{
	/**命中 */
	float hit;
	/**暴击率 */
	float crit;
	/**攻击百分比*/
	float hurtPercent;
	
	@Override
	public float used(Fighter source,Object target,float data,int type)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object used(Fighter source,Object target,Object data)
	{
		AttBaseAttribute aba= (AttBaseAttribute)data;
		if(hit!=0){
			aba.addHit(hit);
		}
		if(crit!=0){
			aba.addCrit(crit);
		}
		if(hurtPercent!=0){
			aba.addHurtPercent(hurtPercent);
		}
		return aba;
	}

	
	
	

}
