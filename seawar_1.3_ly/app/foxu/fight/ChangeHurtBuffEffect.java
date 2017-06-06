package foxu.fight;

import foxu.sea.fight.FleetFighter;

/**
 * 改变增益BUFF 单独提出来写 以前的太混乱。。
 * 
 * @author liuh
 */
public class ChangeHurtBuffEffect extends Effect implements EffectAble
{
	/** 基础技能释放前*/
	public static final int TOUCH_OFF_BEFORE_SPREAD = 1;
	/** 基础技能释放后*/
	public static final int TOUCH_OFF_AFTER_SPREAD = 2;
	/* fields */
	
	/** 触发时机 不同时机 对应的参数含义可能不一样*/
	int touchOffTime; 
	
	
	/**-------------------- 技能效果 ------------------- */
	
	/** 改变百分比的指定伤害 */
	int hurtPrecent;
	/** 改变指定数值的伤害 */
	int hurtValue;
	/** 是否必中 */
	boolean mustHit;
	/** 是否必爆 */
	boolean mustErupt;
	
	
	/**-------------------- 技能条件   方便配置 ------------------- */
	/** 目标 血线要求 这个要求和触发血线不一样 这个是技能已经触发需找符合要求的单位 这个单位血量是 单个坑位 */
	int targetHpPrecent ;

	
	
	
	public int getTouchOffTime()
	{
		return touchOffTime;
	}
	
	public void setTouchOffTime(int touchOffTime)
	{
		this.touchOffTime=touchOffTime;
	}
	/** 获得改变伤害百分比 */
	public int getHurtPrecent()
	{
		return hurtPrecent;
	}
	/** 获得改变伤害指定值 */
	public int getHurtValue()
	{
		return hurtValue;
	}

	/* methods */
	public float used(Fighter source,Object target,float data,int type)
	{
		return 0;
	}
	
	
	public Object used(Fighter source,Object target,Object data)
	{
		switch(touchOffTime)
		{
			case TOUCH_OFF_BEFORE_SPREAD:
				beforeSpread(source,target,data);
				break;

			case TOUCH_OFF_AFTER_SPREAD:
				afterSpread(source,target,data);
				break;
		}
		return data;
	}
	
	
	/** 释放普通攻击前*/
	public void beforeSpread(Fighter source,Object target,Object data){
		AttackBuff buff = (AttackBuff)data;
		if(mustHit)buff.setMustHit(true);
		if(mustErupt)buff.setMustErup(true);
		buff.addAttAlonePercent(hurtPrecent);
	}
	/** 释放普通攻击后 判断伤害前*/
	public void afterSpread(Fighter source,Object target,Object data){
		if(targetHpPrecent!=0){
		FleetFighter f = (FleetFighter)target;
		float precent = (float)f.getFleet().getHp()/f.getFighterMaxHp();
		System.out.println("敌方血线："+precent);
		if(precent*100<targetHpPrecent){
			System.out.println("触发斩杀技能：伤害加深"+hurtPrecent);
			((AttackBuff)data).addAttAlonePercent(hurtPrecent);
		}
		}
	}
	
	
	
	public void setHurtPrecent(int hurtPrecent)
	{
		this.hurtPrecent=hurtPrecent;
	}
	
}
