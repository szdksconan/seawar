package foxu.sea.fight;

import foxu.fight.AttBaseAttribute;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.Fighter;

/**
 * ������� �������� �ӳ� 
 * @author liuh
 *
 */
public class AddAttBaseAttributeEffect extends Effect implements EffectAble
{
	/**���� */
	float hit;
	/**������ */
	float crit;
	/**�����ٷֱ�*/
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
