/**
 * 
 */
package foxu.sea.service;

import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.sea.AttrAdjustment;
import foxu.sea.Service;


/**
 * 战前挂技能的服务
 * 
 * @author rockzyt
 */
public class ServiceAbility extends Service
{
	/* methods */
	public void setChangeValue(AttrAdjustment adjustment)
	{
		//修改的战斗属性,不再添加修改值集合
	}
	/**
	 * 获得技能(已复制)
	 * 
	 * @return 返回技能
	 */
	public Ability getAbility()
	{
		return (Ability)FightScene.abilityFactory.newSample(getValue());
	}
}