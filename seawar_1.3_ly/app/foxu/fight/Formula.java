/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

package foxu.fight;

import mustang.event.ChangeListener;
import foxu.fight.FightScene.FightEvent;

/**
 * ��˵�������㹫ʽ�࣬�����ṹ����ʽϵ��Ӧ����ʹ�ã���ʽ�ı���Ӧͨ���������롣
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public abstract class Formula
{

	/* static fields */
	/**
	 * �ݴ�ֵ
	 */
	public static final double STANDARD_ERROR=1.0000000000000001E-005D;

	/* methods */
	/**
	 * ��������Ƿ���ָ�����͵�����Ч��
	 * 
	 * @param immuneType ��������
	 * @return �����Ƿ�����
	 */
	/**
	 * ���fighter�����Ƿ���ָ�����͵�����Ч��
	 * 
	 * @param fighter ������fighter
	 * @param ability �Դ�fighter�ͷŵļ���
	 * @return ����true��ʾ������
	 */
	public boolean isImmune(Fighter attacker,Fighter fighter,Ability ability)
	{
		Ability[] abilitys=fighter.getAbilityOnSelf();
		Effect[] effects=null;
		// int type=ability.getType();
		for(int i=abilitys.length-1,j=0;i>=0;i--)
		{
			if(abilitys[i].isEnabled())
			{
				effects=abilitys[i].getEffects();
				for(j=effects.length-1;j>=0;j--)
				{
					if(effects[j].isEnable())
					{
						if(effects[j] instanceof ImmuneEffect)
						{
							ImmuneEffect effect=(ImmuneEffect)effects[j];
							if(effect.isImmune(ability,attacker,fighter))
							{
								// ���������¼�
								ChangeListener listener=fighter
									.getChangeListener();
								if(listener!=null)
									listener
										.change(this,
											FightEvent.IMMUNITY
												.ordinal(),fighter
												.getLocation());// ����
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	/** ���㷽���������Ǽ�����������ؼ����� */
	public abstract Object compute(Object obj);

}