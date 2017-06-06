package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.ControlEffect;
import foxu.fight.Fighter;


/**
 * @author rockzyt
 * 成功控制角色一次,就移除该技能
 */
public class ControlOnceEffect extends ControlEffect
{

	/* methods */
	/**
	 * 判断是否有改角色释放技能的对应的控制类型
	 * 
	 * @param fighter 释放技能的角色
	 * @param ability 当前fighter使用的技能
	 * @return 返回true表示被控制
	 */
	public boolean checkControl(Fighter fighter,Ability ability)
	{
		//屏蔽掉 现在把他当做回合技能处理  挂载了这个技能 则认为控制 回合结束再判断移除
		if(super.checkControl(fighter,ability))
		{
			fighter.clearClientAbility();
			fighter.removeAbility(getAbility());
			return true;
		}
		return true;
	}
}