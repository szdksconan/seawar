package foxu.fight;

import mustang.io.BytesReader;
import mustang.io.BytesWritable;

/**
 * 消耗接口
 * </p>
 * 由于各种技能有不同的消耗方式,为了避免为实现不同的消耗方式就写一个Ability的子类,所以将消耗方式抽象为一个接口
 * 所有技能的消耗方式都实现此接口,再注册到对应的技能上去
 * 
 * @author ZYT
 */
public interface Consumer extends Cloneable,BytesWritable,BytesReader
{

	/* abstract methods */
	/**
	 * 检查技能能否正确的消耗掉所需资源
	 * 
	 * @param fighter 使用技能的fighter
	 * @param ability 被使用的技能
	 * @return 返回true表示能正确消耗
	 */
	public boolean checkConsume(Fighter fighter,Ability ability);
	/**
	 * 使用技能后消耗fighter的属性或物品
	 * 
	 * @param fighter 使用技能的fighter
	 * @param ability 被使用的技能
	 */
	public void consume(Fighter fighter,Ability ability);
	
	public Object clone();
	
}
