package foxu.sea.fight;

import foxu.fight.ChangeHurtEffect;


/**
 * ���ݼ��ܵȼ�������ı��˺��İٷֱ�
 * 
 * @author rockzyt
 */
public class ChangeHurtByLevelEffect extends ChangeHurtEffect
{

	/* properties */
	/** ��øı��˺��ٷֱ� */
	public int getHurtPrecent()
	{
		return getHurtPrecent()*getAbility().getLevel();
	}
}
