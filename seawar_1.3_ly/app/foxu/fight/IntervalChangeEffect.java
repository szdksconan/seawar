package foxu.fight;

import mustang.event.ChangeListener;
import foxu.fight.FightScene.FightEvent;

/**
 * ���һ��ʱ��ı�һ�ν�ɫ����ֵ��Ч��
 * 
 * @author ZYT
 */
public class IntervalChangeEffect extends Effect
{

	/**
	 * ʱ������:BEFORE_ACTION=1 fighter�ж�֮ǰ,ROUND_START=2 �غϿ�ʼʱ,ADD_SUCCESS=4
	 * ���ܼ��سɹ�ʱ
	 */
	public static final int BEFORE_ACTION=1,ROUND_START=2,ADD_SUCCESS=4;

	/* fields */
	/** Ч�����õ����� */
	int attrType;
	/** Ч����ֵ */
	float effectValue;

	/* properties */
	/** ����Ч����ֵ */
	public void setEffectValue(float effectValue)
	{
		this.effectValue=effectValue;
	}
	/** ���Ч���������� */
	public int getAttrType()
	{
		return attrType;
	}
	/* methods */
	/**
	 * ����Ч��ֵ
	 * 
	 * @param fighter �д˼��ܵ�fighter
	 * @return ���ؼ�������Ч��ֵ
	 */
	public float computeEffectValue(Fighter fighter)
	{
		return effectValue;
	}
	/**
	 * ��øı��Ժ������ֵ
	 * 
	 * @param attr ����ֵ
	 * @param checkCode У����
	 * @return �ı���fighter
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