/**
 * 
 */
package foxu.sea.service;

import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.sea.AttrAdjustment;
import foxu.sea.Service;


/**
 * սǰ�Ҽ��ܵķ���
 * 
 * @author rockzyt
 */
public class ServiceAbility extends Service
{
	/* methods */
	public void setChangeValue(AttrAdjustment adjustment)
	{
		//�޸ĵ�ս������,��������޸�ֵ����
	}
	/**
	 * ��ü���(�Ѹ���)
	 * 
	 * @return ���ؼ���
	 */
	public Ability getAbility()
	{
		return (Ability)FightScene.abilityFactory.newSample(getValue());
	}
}