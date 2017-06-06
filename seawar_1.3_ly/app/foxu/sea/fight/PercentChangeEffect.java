package foxu.sea.fight;

import mustang.event.ChangeListener;
import foxu.fight.FightScene.FightEvent;
import foxu.fight.Fighter;
import foxu.fight.IntervalChangeEffect;


/**
 * ���հٷֱȸı�����ֵ  ��ǰ�غ���Ч
 * 
 * @author liuh
 */
public class PercentChangeEffect extends IntervalChangeEffect
{

	/* methods */
	/**
	 * ��øı��Ժ������ֵ
	 * 
	 * @param attr ����ֵ
	 * @param checkCode У����
	 * @return �ı���fighter
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