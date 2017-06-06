package foxu.sea.fight;

import mustang.event.ChangeListener;
import foxu.fight.FightScene.FightEvent;
import foxu.fight.Fighter;
import foxu.fight.IntervalDelayChangeEffect;


/**
 * 按照百分比改变属性值的脉冲效果 暂时只能用于舰船hp修改
 * 
 * @author rockzyt
 */
public class PercentIntervalChangeEffect extends IntervalDelayChangeEffect
{

	/* methods */
	/**
	 * 获得改变以后的属性值
	 * 
	 * @param attr 属性值
	 * @param checkCode 校验码
	 * @return 改变后的fighter
	 */
	public Fighter changeAttr(Fighter fighter)
	{
		int currentValue=(int)fighter.getAttrValue(getAttrType());
		float value=currentValue*computeEffectValue(fighter)/100;
		fighter.setDynamicAttr(getAttrType(),currentValue+value);
		ChangeListener listener=fighter.getChangeListener();
		if(listener!=null)
			listener.change(this,FightEvent.SKII_SPRING.ordinal(),fighter,(int)value,currentValue);
		return fighter;
	}
}