package foxu.fight;

/**
 * 再计算控制的时候采用2的N次方方式进行位运算
 * 
 * @author ZYT
 */
public class ControlEffect extends Effect
{

	/* fields */
	/** 控制类型.定义为2的N次方 */
	int controlType;
	/** 新加字段:生效几率.默认100% */
	float precent=10000;

	/* properties */
	public float getPrecent()
	{
		return precent;
	}
	/** 获得控制类型 */
	public int getControlType()
	{
		return controlType;
	}

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
		int rd=fighter.getScene().getRandom().randomValue(
			FightScene.RANDOM_MINI,FightScene.RANDOM_MAX);
		if(rd>precent) return false;
		return checkType(ability);
	}

	/** 检测控制技能类型 */
	public boolean checkType(Ability ability)
	{
		return (controlType&ability.getType())!=0;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[controlType="+controlType+"]";
	}
}