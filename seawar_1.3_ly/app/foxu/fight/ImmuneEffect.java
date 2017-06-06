package foxu.fight;

/**
 * 免疫计算方式采用2的N次方进行位运算
 * 
 * @author ZYT
 */
public class ImmuneEffect extends Effect
{

	/* fields */
	/** 免疫类型 */
	int immuneType;

	/* methods */
	/**
	 * 检查是否免疫指定类型
	 * 
	 * @param ability 检查的技能
	 * @param fighter 检查的fighter
	 * @return 返回true代表可以免疫
	 */
	public boolean isImmune(Ability ability,Fighter attacker,Fighter fighter)
	{
		// 如果效果有作用时间,那么判断效果有没有过期
		if(usefulTime>0
			&&fighter.getScene().getCurrentRound()-ability.getStartTime()>usefulTime)
		{
			enable=false;
			return false;
		}
		int type=ability.getType();
		return (immuneType&type)!=0;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[immuneType="+immuneType+"] ";
	}
}