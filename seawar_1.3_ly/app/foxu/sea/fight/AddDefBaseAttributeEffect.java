package foxu.sea.fight;

import foxu.fight.DefBaseAttribute;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.Fighter;

/**
 * 防御向的 基础属性 加成   这个和攻击分开写 因为 这个是针对击中每个目标前计算一次的
 * @author liuh
 *
 */
public class AddDefBaseAttributeEffect extends Effect implements EffectAble
{
	/**韧性 */
	float toughness;
	/**闪避 */
	float dodge;
	/**减伤百分比*/
	float hurtResistancePercent;
	
	@Override
	public float used(Fighter source,Object target,float data,int type)
	{
		return 0;
	}

	@Override
	public Object used(Fighter source,Object target,Object data)
	{
		DefBaseAttribute dba= (DefBaseAttribute)data;
		if(toughness!=0){
			dba.addToughness(toughness);
		}
		if(dodge!=0){
			dba.addDodge(dodge);
		}
		if(hurtResistancePercent!=0){
			dba.addHurtResistancePercent(hurtResistancePercent);
		}
		return dba;
	}

	
	
	

}
