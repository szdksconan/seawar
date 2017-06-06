package foxu.fight;

import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.FleetFighter;


/**
 * �����˺�
 * 
 * @author ZYT
 */
public class HurtInReturnEffect extends Effect implements EffectAble
{

	/* fields */
	/** ���˰ٷֱ�*/
	int hurtPrecent;

	/* properties */
	/** ��÷��˰ٷֱ� */
	public int getHurtPrecent()
	{
		return hurtPrecent;
	}

	/* methods */
	public float used(Fighter source,Object target,float data,int type)
	{
		//�鿴���� ���㹥�����ܵ����˺�
		FightShowEventRecord listener=(FightShowEventRecord)source.getChangeListener();
		if(listener!=null)
		{
			listener.changeForOfficer(target,FightShowEventRecord.RESET_SHIEL_TYPE,null,null);//�������˶�Ч��
		}
		
		float returnHurt = ((FleetFighter)source).reduceHurtByShiled(data*hurtPrecent/100);//���㹥���������ն�
		//System.out.println("����="+returnHurt);
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
