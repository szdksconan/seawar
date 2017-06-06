package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.AttackBuff;
import foxu.fight.AttackTransmitter;
import foxu.fight.Consumer;
import foxu.fight.Fighter;
import foxu.fight.Formula;
import foxu.fight.Spread;
import foxu.fight.TouchOffSpread;

public class NormalSpread extends Spread
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
		//开始释放普通攻击技能 如：空袭之类的
		super.spreadStartChanged(source,ability);
		touchOff(source,target);
		setTarget(target);
		
		AttackTransmitter attackTransmitter=new AttackTransmitter(
			source.getScene(),source,target,ability);
		//添加攻击收益
		attackTransmitter.setAttackBuff(buff);
		Formula aFormula=source.getScene().getFormula(getFormulaType());
		for(int i=getCount()-1;i>=0;i--)
		{
			attackTransmitter.currentCount=i;
			//这里攻击完成后 如果以后版本目标源因为反弹伤害死亡会导致目标源为空造成异常  
			aFormula.compute(attackTransmitter);
			//攻击完成后  给自己挂载的增益BUFF 不需要攻击命中 这个需要在 移除一次性技能方法前  以免被移除
			attackTransmitter.spreadTouchOffAbility(attackTransmitter.fighter,null,TouchOffSpread.SPREAD_END);
			//清楚释放源一次出手即失效的技能 usefulTime =-1 
			source.getAbilityList().removeOnceAbility();
		}
		ability.consume(source);
		super.spreadOverChanged(source,ability);
		return true;
	}
}