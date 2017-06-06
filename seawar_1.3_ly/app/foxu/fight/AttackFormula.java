package foxu.fight;

import mustang.event.ChangeListener;

/**
 * 计算类 负责计算战斗双方的各种数值
 * 
 * @author ZYT
 */
abstract public class AttackFormula extends Formula
{

	/**
	 * 计算效果作用到目标上产生的伤害
	 * 
	 * @param target 目标
	 * @param ability 技能
	 * @param e 数据传输对象
	 * @param attAlonePercent 触发技能 独立增伤系数
	 * @param isMustHit 是否必中
	 * @param isMustErupt 是否必爆
	 */
	abstract public void computeEffect(Fighter target,AttackTransmitter e);
	
	/** 计算方法，参数是计算数据包装对象，返回计算结果 */
	public Object compute(Object obj)
	{
		AttackTransmitter e=(AttackTransmitter)obj;
		if(e.targets instanceof Fighter[])
		{
			computeTarget((Fighter[])e.targets,e);
		}
		else
		{
			computeTarget((Fighter)e.targets,e);
		}
		return e;
	}
	/**
	 * 计算目标所受到的伤害,单人攻击多次伤害
	 * 
	 * @param target 目标为单个fighter
	 * @param ability 造成伤害的技能
	 * @param e 数据传输对象
	 */
	public void computeTarget(Fighter target,AttackTransmitter e)
	{
		if(target==null) return;
		Ability ability=e.ability;
		computeEffect(target,e);
		ChangeListener listener=target.getChangeListener();
		if(listener!=null)
			listener.change(this,FightScene.FightEvent.ATTACK_ONCE.ordinal(),ability,
				e.fighter,target);
	}
	/**
	 * 计算目标所受到的伤害
	 * 
	 * @param targets 目标为数组
	 * @param ability 造成伤害的技能
	 * @param e 数据传输对象
	 */
	public void computeTarget(Fighter[] targets,AttackTransmitter e)
	{
		if(targets==null||targets.length<=0) return;
		
		for(int j=0,i=targets.length;j<i;j++)
		{
			if(targets[j]!=null)
			{
				e.currentRange=j;
				computeEffect(targets[j],e);
			}
		}
	}
}