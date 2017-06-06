package foxu.fight;

/**
 * 武器装备触发效果动作接口
 * 
 * @author ZYT
 */
public interface EffectAble
{

	/* static fields */
	/**
	 * HARM_VALUE=1攻击力,DEFENS_VALUE=2防御力,HURT_VALUE=4攻击别人的伤害数据,EXPEMT_VALUE=8闪避数据
	 * BE_HURT_VALUE=16被攻击的伤害数据
	 */
	public static final int HARM_VALUE=1,DEFENS_VALUE=2,HURT_VALUE=4,
					EXPEMT_VALUE=8,BE_HURT_VALUE=16;

	/* methdos */
	/**
	 * 使效果生效:主要改变某个数据
	 * 
	 * @param source 施放源
	 * @param target 目标
	 * @param data 需要操作的数据
	 * @param type 数据类型
	 */
	public float used(Fighter source,Object target,float data,int type);
	/**
	 * 使效果生效:主要改变某个对象
	 * 
	 * @param source 施放源
	 * @param target 目标
	 * @param data 需要操作的对象
	 * @return
	 */
	public Object used(Fighter source,Object target,Object data);
}
