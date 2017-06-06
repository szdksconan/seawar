/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

package foxu.fight;

/**
 * 类说明：直接攻击的效果
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class AttackEffect extends Effect
{

	/* fields */
	/** 伤害类型:空袭,炮火,导弹,鱼雷,核子 */
	int attackType;
	/** 伤害值 */
	float damage;

	/* properties */
	/** 获得伤害类型：物理，法术 */
	public int getAttackType()
	{
		return attackType;
	}

	/* methods */
	/**
	 * 获得伤害值,可以根据目标的各种属性来决定伤害值的变化
	 * 
	 * @param target 伤害目标
	 * @return 可以根据目标
	 */
	public float getValue(Fighter target)
	{
		return damage;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[attackTtype="+attackType+", damage="
			+damage+"] ";
	}
}