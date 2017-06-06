package foxu.fight;

import foxu.sea.fight.FleetFighter;

/**
 * 改变伤害
 * 
 * @author ZYT
 */
public class ChangeHurtEffect extends Effect implements EffectAble
{

	/* fields */
	/** 改变百分比的指定伤害 */
	int hurtPrecent;
	/** 改变指定数值的伤害 */
	int hurtValue;
	/** 伤害类型:主动伤害,被伤害 */
	int hurtTime;
	/** 伤害加深血线要求 */
	int targetHpPrecent;

	/* properties */
	/** 获得伤害数据 */
	public int getHurtTime()
	{
		return hurtTime;
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
		if(type!=hurtTime) return data;
		if(targetHpPrecent!=0){//判断血线条件
			FleetFighter f = (FleetFighter)target;
			float precent = (float)f.getFleet().getHp()/f.getFighterMaxHp();
			//System.out.println("当前血量："+precent);
			if(precent*100>=targetHpPrecent)return data;
		}
		int hurtPrecent=getHurtPrecent();
		if(hurtPrecent!=0)
		{
			return data+data*hurtPrecent/100;
		}
		return data+getHurtValue();
	}

	public Object used(Fighter source,Object target,Object data)
	{
		return data;
	}
	
	public void setHurtPrecent(int hurtPrecent)
	{
		this.hurtPrecent=hurtPrecent;
	}
	
}
