package foxu.fight;

import foxu.sea.fight.AddDefBaseAttributeEffect;
/**
 * 数据传输类
 * </p>
 * 负责战斗时数据的收集和传输
 * 
 * @author ZYT
 */

public class AttackTransmitter
{

	/* fields */
	/** 场景 */
	public FightScene scene;
	/** 攻击者 */
	public Fighter fighter;
	/** 被攻击者 */
	public Object targets;
	/** 使用的技能 */
	public Ability ability;
	/** 攻击效果 */
	public Effect[] effects;
	/** 第几个攻击目标 */
	public int currentRange;
	/** 第几次攻击 */
	public int currentCount;
	/** 出手前触发的增益BUFF */
	public AttackBuff AttackBuff;
	
	
	
	

	/** 出手前触发的增益BUFF */
	public AttackBuff getAttackBuff()
	{
		return AttackBuff;
	}

	/** 出手前触发的增益BUFF */
	public void setAttackBuff(AttackBuff attackBuff)
	{
		AttackBuff=attackBuff;
	}

	/* constractors */
	/** 构造方法 */
	public AttackTransmitter(FightScene scene,Fighter fighter,
		Object targets,Ability ability)
	{
		this.scene=scene;
		this.fighter=fighter;
		this.targets=targets;
		this.ability=ability;
		this.effects=ability.getEffects();
	}

	/* methods */
	/** 移除相应的控制技能 */
	public void removeControlAbility(Fighter fighter)
	{
		Ability[] abilitys=fighter.getAbilityOnSelf();
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i].isBreak())
			{
				fighter.removeAbility(abilitys[i]);
			}
		}
	}
	
	/**
	 *  释放攻击的 buff计算
	 * @param source
	 * @param target
	 * @param buff 临时添加的 基本属性
	 * @return
	 */
	public void touchOffAfterSpreadForBuff(Fighter source,Fighter target,AttackBuff buff){
		DefBaseAttribute dba = new DefBaseAttribute();
		Ability[] abilityes=target.getAbilityOnSelf();//检测目标身上的防御BUFF 技能
		for(int i=abilityes.length-1;i>=0;i--)
		{
			Effect[] effect=abilityes[i].getEffects();
			if(!abilityes[i].isEnabled()) continue;
			if(abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				TouchOffSpread spread=(TouchOffSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(TouchOffSpread.AFTER_SPREAD_FOR_HURT);
				if(abilityes[i].checkUsed(fighter,target)==0)
				{
					for(int j=effect.length-1;j>=0;j--)
					{
						if(effect[j] instanceof AddDefBaseAttributeEffect)
						{
							((AddDefBaseAttributeEffect)effect[j]).used(source,target,dba);
						}
					}
				}
			}
		}
		buff.setDefBaseAttribute(dba);
		}
	
	
	
	/**
	 * 触发技能改变伤害
	 * 
	 * @param hurt 伤害值
	 * @param fighter 释放源
	 * @param target 目标
	 * @param time 当前出发时机
	 * @param hurtType 伤害类型(攻击还是被攻击)
	 * @return
	 */
	public float changeHurt(float hurt,Fighter fighter,Fighter target,
		int time,int hurtType)
	{
		Ability[] abilityes=fighter.getAbilityOnSelf();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			Effect[] effect=abilityes[i].getEffects();
			if(!abilityes[i].isEnabled()) continue;
			if(abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				TouchOffSpread spread=(TouchOffSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(time);
				if(abilityes[i].checkUsed(fighter,target)==0)
				{
					for(int j=effect.length-1;j>=0;j--)
					{
						if(effect[j] instanceof EffectAble)
						{
							hurt=((EffectAble)effect[j]).used(fighter,
								target,hurt,hurtType);
						}
					}
				}
			}
			else
			{
				for(int j=effect.length-1;j>=0;j--)
				{
					if(effect[j] instanceof ChangeHurtEffect)
					{
						hurt=((ChangeHurtEffect)effect[j]).used(fighter,
							target,hurt,hurtType);
					}
				}
			}
		}
		return hurt;
	}
	
	/**
	 * 检测目标身上反伤
	 */
	public float hurtInReturn(float hurt,Fighter fighter,Fighter target){
		Ability[] abilityes=target.getAbilityOnSelf();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			Effect[] effect=abilityes[i].getEffects();
			for(int j=effect.length-1;j>=0;j--)
			{
				if(effect[j] instanceof HurtInReturnEffect)
				{
					return ((HurtInReturnEffect)effect[j]).used(fighter,
						target,hurt,0);
				}
			}
		}
		return 0;
	}
	
	
	/**
	 * 触发技能改变中毒伤害
	 * 
	 * @param hurt 伤害值
	 * @param fighter 释放源
	 * @param target 目标
	 * @param time 当前出发时机
	 * @param hurtType 伤害类型(攻击还是被攻击)
	 * @return
	 */
	public void changeEndHurt(float hurt,Fighter fighter,Fighter target,
		int time,int hurtType)
	{
		Ability[] abilityes=fighter.getAbilityOnSelf();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			Effect[] effect=abilityes[i].getEffects();
			if(!abilityes[i].isEnabled()) continue;
			if(abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				TouchOffSpread spread=(TouchOffSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(time);
				if(abilityes[i].checkUsed(fighter,target)==0)
				{
					for(int j=effect.length-1;j>=0;j--)
					{
						if(effect[j] instanceof EffectAble)
						{
							((EffectAble)effect[j]).used(fighter,target,
								hurt,hurtType);
						}
					}
				}
			}
		}
	}
	
	

	/**
	 * 释放触发式技能
	 * 
	 * @param source 源
	 * @param target 目标
	 */
	public void spreadTouchOffAbility(Fighter source,Fighter target,int time)
	{
		Ability[] abilityes=source.getAbilityOnSelf();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			if(abilityes[i].isEnabled()&&abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				((TouchOffSpread)abilityes[i].getSpread())
					.setCurrentTouchTime(time);
				int value=abilityes[i].checkUsed(source,target);
				if(value==0)abilityes[i].used(source,target);
			}
		}
	}
}