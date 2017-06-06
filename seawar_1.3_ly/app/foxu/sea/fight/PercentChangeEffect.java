package foxu.sea.fight;

import mustang.event.ChangeListener;
import foxu.fight.FightScene.FightEvent;
import foxu.fight.Fighter;
import foxu.fight.IntervalChangeEffect;


/**
 * 按照百分比改变属性值  当前回合生效
 * 
 * @author liuh
 */
public class PercentChangeEffect extends IntervalChangeEffect
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