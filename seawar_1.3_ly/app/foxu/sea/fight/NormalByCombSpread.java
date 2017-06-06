package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.AttackBuff;
import foxu.fight.AttackTransmitter;
import foxu.fight.Consumer;
import foxu.fight.TouchOffSpread;
import foxu.fight.FightScene.FightEvent;
import foxu.fight.Fighter;
import foxu.fight.Formula;
import foxu.fight.Spread;

public class NormalByCombSpread extends Spread
{

	/* methods */
	public int checkUsed(Fighter source,Object target,Ability ability)
	{
		if(target==null) return FAILD_NOT_TARGET;
		if(source.checkControl(ability)) return FAILD_CONTROL;
		Consumer c=ability.getConsumer();
		if(c==null||c.checkConsume(source,ability)) return 0;
		return FAILD_CONSUME;
	}
	public boolean used(Fighter source,Object target,Ability ability)
	{
		
		//BEFFOR_SPREAD_FOR_HURT=15 释放前的 技能触发  提上来  才能做展示
		AttackBuff buff = super.touchOffBefforSpreadForBuff(source,target,TouchOffUsedEffectSpread.BEFFOR_SPREAD_FOR_HURT,true);
		super.spreadStartChanged(source,ability);
		touchOff(source,target);
		setTarget(target);
		AttackTransmitter attackTransmitter=new AttackTransmitter(
			source.getScene(),source,target,ability);
		//添加攻击收益
		attackTransmitter.setAttackBuff(buff);
		Formula aFormula=source.getScene().getFormula(getFormulaType());
		//先选取一次目标 ,如果有连击 再选取
		setTargets(source,target,attackTransmitter,ability);
		for(int i=getCount()-1;i>=0;i--)
		{
			attackTransmitter.currentCount=i;
			//这里攻击完成后 如果以后版本目标源因为反弹伤害死亡会导致目标源为空造成异常  
			aFormula.compute(attackTransmitter);

			//攻击完成后  给自己挂载的增益BUFF 不需要攻击命中 这个需要在 移除一次性技能方法前  以免被移除
			attackTransmitter.spreadTouchOffAbility(attackTransmitter.fighter,null,TouchOffSpread.SPREAD_END);
			//清楚释放源一次出手即失效的技能 usefulTime =-1 
			source.getAbilityList().removeOnceAbility();
			
			//每次攻击之间插入一个继续攻击事件
			if(i>0){
				//再次选取目标
				setTargets(source,target,attackTransmitter,ability);
				//判断战斗是否结束
				if(attackTransmitter.targets!=null){
					if(attackTransmitter.targets instanceof Fighter[] ){
						Fighter[] targets = (Fighter[])attackTransmitter.targets;
							for(int j=0;j<targets.length;j++){
								if(targets[j]!=null){
									// 连击时再计算一次  里面的技能 useFulTime = 1 的技能都为-1 表示一次出手就失效
									buff = super.touchOffBefforSpreadForBuff(source,target,TouchOffUsedEffectSpread.BEFFOR_SPREAD_FOR_HURT,false);
									// 重设BUFF类的加成
									attackTransmitter.setAttackBuff(buff);
									break;
								}
							}
					}else{
						// 连击时再计算一次  里面的技能 useFulTime = 1 的技能都为-1 表示一次出手就失效
						buff = super.touchOffBefforSpreadForBuff(source,target,TouchOffUsedEffectSpread.BEFFOR_SPREAD_FOR_HURT,false);
						// 重设BUFF类的加成
						attackTransmitter.setAttackBuff(buff);
					}
				}
				source.getChangeListener().change(this,FightEvent.ATTACK_CONTINUE.ordinal(),ability.getSid());
				
			}
		}
		ability.consume(source);
		super.spreadOverChanged(source,ability);
		return true;
	}
	
	/** 选取攻击目标 */
	public void setTargets(Fighter source,Object target,AttackTransmitter attackTransmitter,Ability ability){
		if(target instanceof Fighter)
		{
			if(((Fighter)target).isDead())
			{
				Object obj=((FleetFighter)source).findTarget((Skill)ability);
				attackTransmitter.targets=obj;
			}
		}
		else
		{
			Fighter[] obj=(Fighter[])((FleetFighter)source).findTarget((Skill)ability);
			attackTransmitter.targets=obj;
		}
	}
	

}