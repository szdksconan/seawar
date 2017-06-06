package foxu.fight;

import mustang.event.ChangeListener;
import foxu.fight.FightScene.FightEvent;

/**
 * 间隔一定时间改变一次角色属性值的效果
 * 
 * @author ZYT
 */
public class IntervalChangeEffect extends Effect
{

	/**
	 * 时机常量:BEFORE_ACTION=1 fighter行动之前,ROUND_START=2 回合开始时,ADD_SUCCESS=4
	 * 技能加载成功时
	 */
	public static final int BEFORE_ACTION=1,ROUND_START=2,ADD_SUCCESS=4;

	/* fields */
	/** 效果作用的属性 */
	int attrType;
	/** 效果数值 */
	float effectValue;

	/* properties */
	/** 设置效果数值 */
	public void setEffectValue(float effectValue)
	{
		this.effectValue=effectValue;
	}
	/** 获得效果作用属性 */
	public int getAttrType()
	{
		return attrType;
	}
	/* methods */
	/**
	 * 计算效果值
	 * 
	 * @param fighter 中此技能的fighter
	 * @return 返回计算过后的效果值
	 */
	public float computeEffectValue(Fighter fighter)
	{
		return effectValue;
	}
	/**
	 * 获得改变以后的属性值
	 * 
	 * @param attr 属性值
	 * @param checkCode 校验码
	 * @return 改变后的fighter
	 */
	public Fighter changeAttr(Fighter fighter)
	{
		float attrValue=fighter.getAttrValue(attrType);
		int currentValue=(int)attrValue;
		float value=computeEffectValue(fighter);
		attrValue+=value;
		if(attrValue<0) attrValue=0;
		fighter.setDynamicAttr(attrType,attrValue);
		ChangeListener listener=fighter.getChangeListener();
		if(listener!=null)
			listener.change(this,FightEvent.SKII_SPRING.ordinal(),fighter,
				(int)value,currentValue);
		return fighter;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[attrType="+attrType+", effectValue="
			+effectValue+"] ";
	}
}