package foxu.sea.fight;

import mustang.event.ChangeListener;
import foxu.fight.Ability;
import foxu.fight.AttackBuff;
import foxu.fight.AttackEffect;
import foxu.fight.AttackFormula;
import foxu.fight.AttackTransmitter;
import foxu.fight.ChangeAttributeEffect;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.FightScene;
import foxu.fight.FightScene.FightEvent;
import foxu.fight.Fighter;
import foxu.fight.TouchOffSpread;
import foxu.sea.PublicConst;

/**
 * 基本计算公式 基础伤害=自身攻击-对方防御
 * 
 * @author ZYT
 */
public class BaseAttackFormula extends AttackFormula
{

	/* fields */
	/** 暴击倍率 */
	float eruptFactor=2;

	/* methods */
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
		e.scene.checkOutTargetDead(e.scene.getFighterContainer()
			.getAllFighter());
		// 重置护盾效果
		resetShield(e);
		// todo 闪避或豁免时 移除武器破坏
		removeOnceEffect(e.fighter);
		return e;
	}
	
	/**重置护盾效果*/
	public void resetShield(AttackTransmitter e)
	{
		if(e.getAttackBuff().isOpenShield()&&e.getAttackBuff().getShield()>0)
		{
			if(e.fighter!=null)
			{
				((FleetFighter)e.fighter).setShield((int)e.getAttackBuff()
					.getShield());
				// 吸收护盾产生
				if(e.getAttackBuff().isOpenShield())
				{
					FightShowEventRecord OfficerChanger=(FightShowEventRecord)e.fighter
						.getChangeListener();
					if(OfficerChanger!=null)
					{
						OfficerChanger.changeForOfficer(e.fighter,
							FightShowEventRecord.OPEN_SHIELD_TYPE,
							e.getAttackBuff(),null);
					}
				}
			}
		}
	}
	
	/**
	 * 触发技能修改一个数值
	 * 
	 * @param source 施放源
	 * @param target 目标
	 * @param touchOffTime 触发时机
	 * @param ability 技能
	 * @param value 需要修改的数据
	 * @return 返回计算完成后的数据
	 */
	public float touchOffChangeValue(Fighter source,Object target,
		int touchOffTime,Ability ability,float value)
	{
		Ability[] abilitys=source.getAbilityOnSelf();
		int precent=100;
		// 判断触发技能(计算攻击力时)
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i].isEnabled())
			{
				if(abilitys[i].getSpread() instanceof TouchOffSpread)
				{
					TouchOffSpread spread=(TouchOffSpread)abilitys[i]
						.getSpread();
					spread.setCurrentTouchTime(TouchOffSpread.HARM);
					if(abilitys[i].checkUsed(source,target)==0)
					{
						Effect[] effects=abilitys[i].getEffects();
						for(int j=effects.length-1;j>=0;j--)
						{
							if(effects[j] instanceof ChangeAttributeEffect)
							{
								ChangeAttributeEffect effect=(ChangeAttributeEffect)effects[j];
								if(effect.checkType(ability.getType(),
									ability.getCid()))
								{
									if(effect.getValueType()==ChangeAttributeEffect.PRECENT)
									{
										precent+=effect.getValue(source);
									}
									else
									{
										value+=effect.getValue(source);
									}
								}
							}
						}
					}
				}
			}
		}
		return value*precent/100;
	}
	/**
	 * 获得释放源攻击力 用攻击人数来做削减 </p>
	 * 攻击力=自身攻击*{1-[系数1*(攻击人数-1)-系数2*攻击人数*(攻击人数-1)/2]}
	 * 
	 * @param e 战斗计算所需的数据
	 * @param effect 这次计算的AttackEffect
	 * @param baseAttack 从fighter身上获得的基础攻击
	 * @param target 攻击目标
	 * @return 返回计算结果
	 */
	public float getAttack(AttackTransmitter e,AttackEffect effect)
	{
		Fighter source=e.fighter;
		float baseAttack=e.fighter.getAttrValue(PublicConst.ATTACK);
		baseAttack=touchOffChangeValue(source,null,TouchOffSpread.HARM,
			e.ability,baseAttack);
		// System.out.println(">..............最终攻击:"+baseAttack);
		return baseAttack;
	}
	/**
	 * 获得目标防御力
	 * 
	 * @param target 攻击目标
	 * @param e 战斗计算所需的数据
	 * @param effect 当前正在计算的效果
	 * @return 返回防御力
	 */
	public float getDefence(AttackTransmitter e,Fighter target,
		AttackEffect effect)
	{
		// System.out.println(">................计算防御...................<");
		Fighter source=e.fighter;
		float defence;
		int sNum=(int)source.getAttrValue(PublicConst.SHIP_NUM);
		// int tNum=(int)target.getAttrValue(PublicConst.SHIP_NUM);
		defence=target.getAttrValue(PublicConst.DEFENCE)*sNum;
		// if(sNum<tNum)
		// {
		// defence=target.getAttrValue(PublicConst.DEFENCE)*sNum;
		// }
		// else
		// {
		// defence=target.getAttrValue(PublicConst.DEFENCE)*tNum;
		// }
		// System.out.println("..............目标基础防御:"+defence);
		// defence=((WkFighter)target).computeValueForAbility(Skill.DEFENSE
		// |effect.getAttackType(),ChangeAttributeEffect.ABSOLUTE_VALUE,
		// defence);
		// defence=((WkFighter)target).computeValueForAbility(Skill.DEFENSE
		// |effect.getAttackType(),ChangeAttributeEffect.PRECENT,defence);
		defence=touchOffChangeValue(target,null,TouchOffSpread.DEFENCE,
			e.ability,defence);
		// System.out.println("..............被目标自身技能改变过后的目标防御:"+defence);
		return defence;
	}
	/**
	 * 计算闪避、豁免
	 * 
	 * @param source 攻击者
	 * @param target 目标fighter
	 * @param ability 施放的技能
	 * @return 返回true表示闪避成功
	 */
	public boolean isExempt(Fighter source,Fighter target,Ability ability,AttackTransmitter e)
	{
		// System.out.println("计算闪避率");
		if(ability.isMustHit()) return false;
		double hitRate=source.getAttrValue(PublicConst.ACCURATE);
		if(e.getAttackBuff().getAttBaseAttribute().getHit()!=0){
			//System.out.println("命中加成："+e.getAttackBuff().getAttBaseAttribute().getHit());
			hitRate+=e.getAttackBuff().getAttBaseAttribute().getHit();
		}
		double exempt=target.getAttrValue(PublicConst.AVOID);
		if(e.getAttackBuff().getDefBaseAttribute().getDodge()!=0){
			//System.out.println("闪避加成："+e.getAttackBuff().getDefBaseAttribute().getDodge());
			exempt+=e.getAttackBuff().getDefBaseAttribute().getDodge();
		}
		// System.out.println(">................命中:"+hitRate);
		// System.out.println(">................闪避:"+exempt);
		double result=100+hitRate-exempt;
		// 保底命中
		if(result<=0)	result=3;
		FightScene scene=source.getScene();
		int randomValue=scene.getRandom().randomValue(
			FightScene.RANDOM_MINI,FightScene.RANDOM_MAX)/FightScene.OLD_TIMES;
		// System.out.println(".......................最终闪避率:>"+result+" 随机数:"
		// +randomValue);
		return randomValue>result;
	}
	/**
	 * fighter血量变化
	 * 
	 * @param f 血量改变的Fighter
	 * @param value 改变值
	 */
	public void fighterLoseHp(Fighter f,float value)
	{
		f.setDynamicAttr(PublicConst.FLEET_HP,f.getAttrValue(PublicConst.FLEET_HP)-value);
	}
	/**
	 * 计算是否暴击
	 * 
	 * @param source 攻击者
	 * @param target 被攻击者
	 * @return 返回是否暴击
	 */
	public boolean computErupt(Fighter source,Fighter target,AttackBuff buff)
	{
		float critialHit=source.getAttrValue(PublicConst.CRITICAL_HIT); // 攻击者爆率
		if(buff.getAttBaseAttribute().getCrit()!=0){//需要临时计算来自触发技能F的暴击
			//System.out.println("暴击加成："+buff.getAttBaseAttribute().getCrit());
			critialHit+=buff.getAttBaseAttribute().getCrit();
		}
		float critialHitResist=target
			.getAttrValue(PublicConst.CRITICAL_HIT_RESIST); // 被攻击者抵抗爆率
		if(buff.getDefBaseAttribute().getToughness()!=0){
			//System.out.println("韧性加成："+buff.getDefBaseAttribute().getToughness());
			critialHitResist+=buff.getDefBaseAttribute().getToughness();
		}
		// System.out.println(">................暴击率:"+critialHit);
		// System.out.println(">................暴击抵抗:"+critialHitResist);
		float value=critialHit-critialHitResist;
		if(value<=0) return false;
		FightScene scene=source.getScene();
		int randomValue=scene.getRandom().randomValue(
			FightScene.RANDOM_MINI,FightScene.RANDOM_MAX)/FightScene.OLD_TIMES;
		// System.out.println(">.................实际暴击率:"+value+" 随机数:"
		// +randomValue);
		return randomValue<value;
	}
	/**
	 * 计算结果
	 * 
	 * @param e 计算数据
	 * @param target 被攻击的目标
	 * @param effect 此次攻击的Effect
	 * @param attack 攻击力
	 * @param defence 防御力
	 * @param buff 这个预留给 每次击中时才算的BUFF  clone AttackTransmitter的AttackBuff对象 
	 */
	public void computeResult(AttackTransmitter e,Fighter target,
		AttackEffect effect,float attack,float defence,AttackBuff buff)
	{
		//System.out.println("初始攻击力："+attack);
//		System.out.println("攻方:"
//			+((FleetFighter)e.fighter).getFleet().getShip().getSid());
//		System.out.println("收方:"
//			+((FleetFighter)target).getFleet().getShip().getSid());
		if(buff.getAttBaseAttribute().getHurtPercent()!=0){
			attack+=attack*buff.getAttBaseAttribute().getHurtPercent()/100;
			//System.out.println("提高攻击的系数："+buff.getAttBaseAttribute().getHurtPercent()+"  提高 后的攻击力："+attack);
		}
		float hurt=(attack-defence)*effect.getValue(target);
//		System.out.println("..........基础伤害:"+hurt+" 攻击力:"+attack+" 防御力:"
//			+defence);
		float percent=target.getAttrValue(effect.getAttackType());
		//攻击者，根据被攻击者类型进行装备克制伤害系数加成计算
		percent=percent
			+e.fighter.getAttrValue(PublicConst.ATTACH_BASE
				+target.getFighterType());
//		System.out.println("_______--------_______--------attack type:"+effect.getAttackType()+",targer type:"+target.getFighterType());
//		System.out.println("_______--------_______--------fighter attach percent:"+e.fighter.getAttrValue(PublicConst.ATTACH_BASE
//			+target.getFighterType()));
		//被攻击者，根据攻击类型进行装备反克制伤害系数减免计算
		percent=percent
			+target.getAttrValue(PublicConst.RESIST_AIR_RAID
				+effect.getAttackType());
//		System.out.println("_______--------_______--------target attach percent:"+target.getAttrValue(PublicConst.RESIST_AIR_RAID
//			+effect.getAttackType()));
		hurt=hurt*(100+percent)/100.0f;// 抗性计算
//		System.out.println("抗性之后>>>>:"+hurt);
		boolean flag=false;
		if(hurt<=0) hurt=1;
//		System.out.println("--------------------伤害打印----------------------");
		if(computErupt(e.fighter,target,buff)||buff.isMustErup())//计算暴击
		{
			hurt*=eruptFactor;
			// 如果产生了暴击，标记为ture
			flag=true;
		}
//		System.out.println("..........暴击过后的:"+hurt);
		hurt=e.changeHurt(hurt,e.fighter,target,TouchOffSpread.HURT,
			EffectAble.HURT_VALUE);
//		System.out.println("..........被主动方触发技能改变过后的伤害:"+hurt);
		hurt=e.changeHurt(hurt,target,e.fighter,TouchOffSpread.BE_HURT,
			EffectAble.BE_HURT_VALUE);
		
		//攻击技能独立增伤
		if(buff.getAttAlonePercent()!=0)hurt+=hurt*buff.getAttAlonePercent()/100.0f;
		//增益buff 增伤 减伤
		if(buff.getDefBaseAttribute().getHurtResistancePercent()!=0){
			//System.out.println("减伤前的伤害:"+hurt);
			hurt-=hurt*buff.getDefBaseAttribute().getHurtResistancePercent()/100.0f;
			//System.out.println("减伤后的伤害"+hurt);
		}
		if(hurt<1){ //最少为1
			hurt = 1;
		}
		
		e.changeEndHurt(hurt,e.fighter,target,TouchOffSpread.HURT_END,
			EffectAble.HURT_VALUE);
		
		//护盾类
		if(buff.isOpenShield())buff.addShield(hurt);//是否产生护盾
		hurt = ((FleetFighter)target).reduceHurtByShiled(hurt);//计算目标身上护盾
		
//		System.out.println("..........被防御方触发技能改变过后的伤害::"+hurt);
		int currentNum=(int)target.getAttrValue(PublicConst.SHIP_NUM);
		fighterLoseHp(target,hurt);
		e.removeControlAbility(target);
//		System.out.println("..........如果没破防,打印的随机伤害:"+hurt);
		ChangeListener listener=target.getChangeListener();
		if(listener!=null)
		{
			//军官技能命中效果
			officerHitChange(target,buff);
			if(flag)
			{
				listener.change(this,FightEvent.ERUPT.ordinal(),(int)hurt,
					target,currentNum);
			}
			else
			{
				listener.change(this,FightEvent.HURT.ordinal(),(int)hurt,
					target,currentNum);
			}
		}
		// 触发反伤
		float returnHurt  = e.hurtInReturn(hurt,e.fighter,target);
		if(returnHurt>0&&returnHurt<1){//最少为1
			returnHurt = 1;
		}
		if(returnHurt>0){
		currentNum=(int)e.fighter.getAttrValue(PublicConst.SHIP_NUM);
		fighterLoseHp(e.fighter,returnHurt);
		if(listener!=null)
		{
				((FightShowEventRecord)listener).changeForOfficer(e.fighter,FightShowEventRecord.RETURN_HURT_TYPE,(int)returnHurt,currentNum);
		}
		}
		
		// 触发动作源的被动技能
		e.spreadTouchOffAbility(e.fighter,target,TouchOffSpread.HIT);
		// 触发目标的被动技能
		e.spreadTouchOffAbility(target,e.fighter,TouchOffSpread.BE_HIT);
		if(target.isDead())
		{
			e.spreadTouchOffAbility(target,e.fighter,TouchOffSpread.DEAD);
			//移除直接发送的死亡事件
			//if(listener!=null)
			//	listener.change(this,FightEvent.DEAD.ordinal(),target); 

		}
//		 System.out
//		 .println("........................一次攻击完成..............................");
//		 System.out.println();
//		 System.out.println();
	}
	
	/**
	 * 军官技能命中
	 * @param target 目标
	 * @param buff 
	 */
	public void officerHitChange(Fighter target,AttackBuff buff){
		if(buff.isUse()){
			FightShowEventRecord OfficerChanger=(FightShowEventRecord)target.getChangeListener();
			if(OfficerChanger!=null)
			{
				OfficerChanger.changeForOfficer(target,FightEvent.ADD_ABILITY.ordinal(),buff,null);
			}
			//效果触发一次就清除
			buff.setUse(false);
			buff.setSid(0);
		}
	}
	
	
	/**
	 * 计算效果作用到目标上产生的伤害
	 * 
	 * @param target 目标
	 * @param e 数据传输对象
	 * @param attAlonePercent 独立增伤
	 */
	public void computeEffect(Fighter target,AttackTransmitter e)
	{
		if(target==null||target.isDead()) return;
		
		//攻击释放后  击中前的增益BUFF 计算  （主要针对于目标身上的BUFF）
		AttackBuff buff = e.getAttackBuff()/*.clone()*/;
		e.touchOffAfterSpreadForBuff(e.fighter,target,buff);
		
		Ability ability=e.ability;
		Fighter source=e.fighter;
		
		if(!buff.isMustHit()&&isExempt(source,target,ability,e))//计算闪避
		{
			ChangeListener listener=target.getChangeListener();
			if(listener!=null)
				listener.change(this,FightEvent.EXEMPT.ordinal(),target,
					ability);
			return;
		}
		
		if(!isImmune(source,target,ability))
		{
			Effect[] effects=ability.getEffects();
			AttackEffect attackEffect;
			// 遍历技能的所有effect,计算每effect的战斗数据
			for(int i=effects.length-1;i>=0;i--)
			{
				attackEffect=(AttackEffect)effects[i];
				float defence=getDefence(e,target,attackEffect);
				float finalAttack=getAttack(e,attackEffect);
				computeResult(e,target,attackEffect,finalAttack,defence,buff);
			}
		}
	}
	
	/** 移除武器破坏 */
	public void removeOnceEffect(Fighter fighter)
	{
		if(fighter!=null){
			Ability[] abilityes=fighter.getAbilityOnSelf();
			for(int i=abilityes.length-1;i>=0;i--)
			{
				Effect[] effect=abilityes[i].getEffects();
				if(!abilityes[i].isEnabled()) continue;
				for(int j=effect.length-1;j>=0;j--)
				{
					if(effect[j] instanceof ChangeHurtOnceEffect)
					{
						ChangeHurtOnceEffect onceEffect=(ChangeHurtOnceEffect)effect[j];
						if(onceEffect.getHurtTime()==EffectAble.HURT_VALUE)
						{
							fighter.removeAbility(onceEffect.getAbility());
						}
					}
				}
			}
		}
	}

}