package foxu.fight;

import foxu.sea.fight.FleetFighter;

/**
 * �ı��˺�
 * 
 * @author ZYT
 */
public class ChangeHurtEffect extends Effect implements EffectAble
{

	/* fields */
	/** �ı�ٷֱȵ�ָ���˺� */
	int hurtPrecent;
	/** �ı�ָ����ֵ���˺� */
	int hurtValue;
	/** �˺�����:�����˺�,���˺� */
	int hurtTime;
	/** �˺�����Ѫ��Ҫ�� */
	int targetHpPrecent;

	/* properties */
	/** ����˺����� */
	public int getHurtTime()
	{
		return hurtTime;
	}
	/** ��øı��˺��ٷֱ� */
	public int getHurtPrecent()
	{
		return hurtPrecent;
	}
	/** ��øı��˺�ָ��ֵ */
	public int getHurtValue()
	{
		return hurtValue;
	}

	/* methods */
	public float used(Fighter source,Object target,float data,int type)
	{
		if(type!=hurtTime) return data;
		if(targetHpPrecent!=0){//�ж�Ѫ������
			FleetFighter f = (FleetFighter)target;
			float precent = (float)f.getFleet().getHp()/f.getFighterMaxHp();
			//System.out.println("��ǰѪ����"+precent);
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
