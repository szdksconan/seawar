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
 * ������ִ�о���Ч�����ͷŷ�ʽ
 */
public class TouchOffUsedEffectSpread extends TouchOffSpread
{
	/* fields */
	/** ����Ч�� */
	EffectAble effect;

	/* methods */
	/** Դ��ɫ��Ŀ���ɫʹ�øü��� */
	public boolean used(Fighter source,Object target,Ability ability)
	{
		effect.used(source,target,ability);
		return false;
	}
}
