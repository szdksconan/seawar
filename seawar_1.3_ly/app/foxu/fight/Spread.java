package foxu.fight;

import mustang.event.ChangeListener;
import mustang.math.MathKit;
import mustang.util.Sample;
import foxu.fight.FightScene.FightEvent;
import foxu.sea.fight.AddAbilityEffectForSource;
import foxu.sea.fight.AddShieldEffect;
import foxu.sea.fight.AddAttBaseAttributeEffect;
import foxu.sea.fight.FightShowEventRecord;

/**
 * 类说明：技能释放
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */
public abstract class Spread extends Sample
{

	/* static fields */
	/** 施放动作：NEAR=1近身，FAR=0远程 */
	public final static int NEAR=1,FAR=0;
	// 消息常量划分段:500-600为技能释放时产生的失败信息,600-700为触发技能时产生的失败信息,
	// 700-800为即时战斗游戏特有条件产生的失败信息
	/**
	 * 类与类之间通讯常量:FAILD_CONTROL=500释放角色被控制,FAILD_NOT_TARGET=501目标不符合条件,
	 * FAILD_TARGET_DEAD=502目标已死亡,FAILD_SOURCE_DEAD=503释放源死亡,FAILD_TARGET_NULL=504目标不存在
	 */
	public final static int FAILD_CONTROL=500,FAILD_NOT_TARGET=501,
					FAILD_TARGET_DEAD=502,FAILD_SOURCE_DEAD=503,
					FAILD_TARGET_NULL=504,FAILD_CONSUME=505;

	/* fields */
	/** 施放类型：主动、被动、触发 */
	int type;
	/** 施放动作：近身1、远程 0 */
	int action;
	/** 释放的目标队伍：0其他，1己方 */
	int targetTeam;
	/** 范围 */
	int range;
	/** 次数 */
	int count=1;
	/** 计算公式类型 */
	int formulaType;
	/** 反握技能对象 */
	Ability ability;

	/* dynamic fields */
	/** 释放源 */
	protected int source;
	/** 缓存改变后的目标 */
	Object target;

	/* properties */
	/** 获得反握技能 */
	public Ability getAbility()
	{
		return ability;
	}
	/** 获得计算公式类型 */
	public int getFormulaType()
	{
		return formulaType;
	}
	/** 设置计算公式类型 */
	public void setFormulaType(int formulaType)
	{
		this.formulaType=formulaType;
	}
	/** 设置新的目标 */
	public void setTarget(Object target)
	{
		this.target=target;
	}
	/** 获得施放目标 */
	public Object getTarget()
	{
		return target;
	}
	/** 获得施放的目标队伍: 0其他，1己方 */
	public int getSpreadTeam()
	{
		return targetTeam;
	}
	/** 获得释放次数 */
	public int getCount()
	{
		return count;
	}
	/** 得到施放动作 近身1、远程 0(在及时游戏里这个域只表示释放距离) */
	public int getAction()
	{
		return action;
	}
	/** 获得类型 */
	public int getType()
	{
		return type;
	}
	/**
	 * 设置释放范围
	 * 
	 * @param range 范围
	 */
	public void setRange(int range)
	{
		this.range=range;
	}
	/** 获得释放范围 */
	public int getRange()
	{
		return range;
	}
	/** 得到释放源 */
	public int getSpreadSource()
	{
		return source;
	}
	/** 设置释放源 */
	public void setSpreadSource(int source)
	{
		this.source=source;
	}

	/* abstract methods */
	/** 检查源角色能否对目标角色使用该技能 */
	public abstract int checkUsed(Fighter source,Object target,
		Ability ability);
	/** 源角色对目标角色使用该技能 */
	public abstract boolean used(Fighter source,Object target,Ability ability);

	/* methods */
	/**
	 * 查找目标
	 * 
	 * @param source 技能释放源
	 * @param target 目标对象
	 * @return 返回找到的目标数组
	 */
	public Fighter[] findTarget(Fighter source,Object target,Ability ability)
	{
		return null;
	}
	/**
	 * 触发释放者身上的技能
	 * 
	 * @param source 释放者
	 * @param target 目标
	 */
	public void touchOff(Fighter source,Object target)
	{
		Ability[] abilityes=source.getAbilityList().getAllAbility();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			if(abilityes[i]!=null
				&&abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				TouchOffSpread spread=(TouchOffSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(TouchOffSpread.BEFFOR_SPREAD);
				if(abilityes[i].checkUsed(source,target)==0)
				{
					abilityes[i].used(source,target);
				}
			}
		}
	}
	
	/**
	 * 释放前 触发释放者身上的技能 完成攻击增益BUFF 或者 技能添加
	 * 
	 * @param source 释放者
	 * @param target 目标
	 * @param useTouchOffSkill 是否使用触发式的技能（现在这里只会是军官技能）
	 */
	public AttackBuff touchOffBefforSpreadForBuff(Fighter source,Object target,int touchTime,boolean useTouchOffSkill){
	AttackBuff buff = new AttackBuff();
	AttBaseAttribute aba = new AttBaseAttribute();
	Ability[] abilityes=source.getAbilityOnSelf();
	for(int i=abilityes.length-1;i>=0;i--)
	{
		Effect[] effect=abilityes[i].getEffects();
		if(!abilityes[i].isEnabled()) continue;
		if(abilityes[i].getSpread() instanceof TouchOffSpread)
		{
			TouchOffSpread spread=(TouchOffSpread)abilityes[i]
				.getSpread();
			spread.setCurrentTouchTime(touchTime);
			if(abilityes[i].checkUsed(source,target)==0)
			{
				for(int j=effect.length-1;j>=0;j--)
				{
					//先写个逻辑 好看点 
					if(useTouchOffSkill){
					if(effect[j] instanceof ChangeHurtBuffEffect){//是否有独立增伤
						if(((ChangeHurtBuffEffect)effect[j]).getTouchOffTime()==ChangeHurtBuffEffect.TOUCH_OFF_BEFORE_SPREAD){
							showChangeByte(source,target,abilityes[i],buff);
							((ChangeHurtBuffEffect)effect[j]).used(source,target,buff);
						}
					}
					else if(effect[j] instanceof AddAbilityEffectForSource){//为释放源添加一个技能
						showChangeByte(source,target,abilityes[i],buff);
						((AddAbilityEffectForSource)effect[j]).used(source,target,null);
					}else if(effect[j] instanceof AddShieldEffect){//触发护盾
						showChangeByte(source,target,abilityes[i],buff);
						((AddShieldEffect)effect[j]).used(source,abilityes[i],buff);
					}
					}
					
					if(effect[j] instanceof AddAttBaseAttributeEffect){//这里为技能效果 非触发技能
						//showChangeByte(source,target,abilityes[i],buff);
						((AddAttBaseAttributeEffect)effect[j]).used(source,target,aba);
					}
					
				}
			}
		}
	}
	buff.setAttBaseAttribute(aba);
	return buff;
	}
	
	public void showChangeByte(Fighter source,Object target,Ability ability,AttackBuff buff){
		FightShowEventRecord listener=(FightShowEventRecord)source.getChangeListener();
		if(listener!=null)
		{
				listener.changeForOfficer(source,FightShowEventRecord.LAG_TYPE,ability,null);//设置延迟事件
				listener.changeForOfficer(source,FightShowEventRecord.OFFICER_SKILL_START_TYPE,ability,null);
		}
		//记录军官技能ID 使用情况
		buff.setSid(ability.getSid());
		buff.setUse(true);
	}
	
	
	/**
	 * 开始释放引发的change事件
	 * 
	 * @param source 释放源
	 * @param ability 释放的技能
	 */
	public void spreadStartChanged(Fighter source,Ability ability)
	{
		ChangeListener listener=source.getChangeListener();
		if(listener!=null)
			listener.change(this,FightEvent.SPREAD_START.ordinal(),source,ability);
	}
	/**
	 * 结束释放引发的change事件
	 * 
	 * @param source 释放源
	 * @param ability 释放的技能
	 */
	public void spreadOverChanged(Fighter source,Ability ability)
	{
		ChangeListener listener=source.getChangeListener();
		if(listener!=null)
			listener.change(this,FightEvent.SPREAD_OVER.ordinal(),source,ability);
	}
}