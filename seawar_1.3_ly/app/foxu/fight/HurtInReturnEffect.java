package foxu.fight;

import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.FleetFighter;


/**
 * 反弹伤害
 * 
 * @author ZYT
 */
public class HurtInReturnEffect extends Effect implements EffectAble
{

	/* fields */
	/** 反伤百分比*/
	int hurtPrecent;

	/* properties */
	/** 获得反伤百分比 */
	public int getHurtPrecent()
	{
		return hurtPrecent;
	}

	/* methods */
	public float used(Fighter source,Object target,float data,int type)
	{
		//查看护盾 计算攻击方受到的伤害
		FightShowEventRecord listener=(FightShowEventRecord)source.getChangeListener();
		if(listener!=null)
		{
			listener.changeForOfficer(target,FightShowEventRecord.RESET_SHIEL_TYPE,null,null);//触发反伤盾效果
		}
		
		float returnHurt = ((FleetFighter)source).reduceHurtByShiled(data*hurtPrecent/100);//计算攻击方的吸收盾
		//System.out.println("反伤="+returnHurt);
		/*	source.setDynamicAttr(PublicConst.FLEET_HP,source.getAttrValue(PublicConst.FLEET_HP)-returnHurt);
		int currentNum=(int)source.getAttrValue(PublicConst.SHIP_NUM);
		
		if(listener!=null)
		{
				listener.change(this,FightEvent.HURT.ordinal(),(int)returnHurt,
					source,currentNum);
		}*/
		return returnHurt;
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
