package foxu.sea.fight;

import foxu.fight.DefBaseAttribute;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.Fighter;

/**
 * ������� �������� �ӳ�   ����͹����ֿ�д ��Ϊ �������Ի���ÿ��Ŀ��ǰ����һ�ε�
 * @author liuh
 *
 */
public class AddDefBaseAttributeEffect extends Effect implements EffectAble
{
	/**���� */
	float toughness;
	/**���� */
	float dodge;
	/**���˰ٷֱ�*/
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
