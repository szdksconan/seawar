package foxu.fight;

import foxu.sea.fight.FleetFighter;

/**
 * �ı�����BUFF ���������д ��ǰ��̫���ҡ���
 * 
 * @author liuh
 */
public class ChangeHurtBuffEffect extends Effect implements EffectAble
{
	/** ���������ͷ�ǰ*/
	public static final int TOUCH_OFF_BEFORE_SPREAD = 1;
	/** ���������ͷź�*/
	public static final int TOUCH_OFF_AFTER_SPREAD = 2;
	/* fields */
	
	/** ����ʱ�� ��ͬʱ�� ��Ӧ�Ĳ���������ܲ�һ��*/
	int touchOffTime; 
	
	
	/**-------------------- ����Ч�� ------------------- */
	
	/** �ı�ٷֱȵ�ָ���˺� */
	int hurtPrecent;
	/** �ı�ָ����ֵ���˺� */
	int hurtValue;
	/** �Ƿ���� */
	boolean mustHit;
	/** �Ƿ�ر� */
	boolean mustErupt;
	
	
	/**-------------------- ��������   �������� ------------------- */
	/** Ŀ�� Ѫ��Ҫ�� ���Ҫ��ʹ���Ѫ�߲�һ�� ����Ǽ����Ѿ��������ҷ���Ҫ��ĵ�λ �����λѪ���� ������λ */
	int targetHpPrecent ;

	
	
	
	public int getTouchOffTime()
	{
		return touchOffTime;
	}
	
	public void setTouchOffTime(int touchOffTime)
	{
		this.touchOffTime=touchOffTime;
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
	
	
	/** �ͷ���ͨ����ǰ*/
	public void beforeSpread(Fighter source,Object target,Object data){
		AttackBuff buff = (AttackBuff)data;
		if(mustHit)buff.setMustHit(true);
		if(mustErupt)buff.setMustErup(true);
		buff.addAttAlonePercent(hurtPrecent);
	}
	/** �ͷ���ͨ������ �ж��˺�ǰ*/
	public void afterSpread(Fighter source,Object target,Object data){
		if(targetHpPrecent!=0){
		FleetFighter f = (FleetFighter)target;
		float precent = (float)f.getFleet().getHp()/f.getFighterMaxHp();
		System.out.println("�з�Ѫ�ߣ�"+precent);
		if(precent*100<targetHpPrecent){
			System.out.println("����նɱ���ܣ��˺�����"+hurtPrecent);
			((AttackBuff)data).addAttAlonePercent(hurtPrecent);
		}
		}
	}
	
	
	
	public void setHurtPrecent(int hurtPrecent)
	{
		this.hurtPrecent=hurtPrecent;
	}
	
}
