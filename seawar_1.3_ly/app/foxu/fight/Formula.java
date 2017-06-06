/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

package foxu.fight;

import mustang.event.ChangeListener;
import foxu.fight.FightScene.FightEvent;

/**
 * 类说明：计算公式类，函数结构，公式系数应设置使用，公式的变量应通过参数传入。
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public abstract class Formula
{

	/* static fields */
	/**
	 * 容错值
	 */
	public static final double STANDARD_ERROR=1.0000000000000001E-005D;

	/* methods */
	/**
	 * 检查身上是否有指定类型的免疫效果
	 * 
	 * @param immuneType 免疫类型
	 * @return 返回是否免疫
	 */
	/**
	 * 检查fighter身上是否有指定类型的免疫效果
	 * 
	 * @param fighter 被检测的fighter
	 * @param ability 对此fighter释放的技能
	 * @return 返回true表示被免疫
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
								// 引发免疫事件
								ChangeListener listener=fighter
									.getChangeListener();
								if(listener!=null)
									listener
										.change(this,
											FightEvent.IMMUNITY
												.ordinal(),fighter
												.getLocation());// 免疫
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	/** 计算方法，参数是计算变量，返回计算结果 */
	public abstract Object compute(Object obj);

}