package foxu.sea.fight;

import foxu.fight.AttackBuff;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.Fighter;
import foxu.fight.FightScene.FightEvent;

/**
 * �����BUFF ������ֵ Ҫ���˺���� ����Ͳ�������
 * @author liuh
 *
 */
public class AddShieldEffect extends Effect implements EffectAble
{
	
	int shieldPrecent;
	
	@Override
	public float used(Fighter source,Object target,float data,int type)
	{
		return 0;
	}

	@Override
	public Object used(Fighter source,Object target,Object data)
	{
		AttackBuff buff = (AttackBuff)data;
		buff.setOpenShield(true);
		buff.setShieldPrecent(shieldPrecent);
		return buff;
	}

}
