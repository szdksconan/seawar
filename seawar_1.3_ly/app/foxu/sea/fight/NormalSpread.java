package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.AttackBuff;
import foxu.fight.AttackTransmitter;
import foxu.fight.Consumer;
import foxu.fight.Fighter;
import foxu.fight.Formula;
import foxu.fight.Spread;
import foxu.fight.TouchOffSpread;

public class NormalSpread extends Spread
{

	/* methods */
	public int checkUsed(Fighter source,Object target,Ability ability)
	{
		if(target==null) return FAILD_NOT_TARGET;
		if(source.checkControl(ability)) return FAILD_CONTROL;
		Consumer c=ability.getConsumer();
		if(c==null||c.checkConsume(source,ability)) return 0;
		return FAILD_CONSUME;
	}
	public boolean used(Fighter source,Object target,Ability ability)
	{
		//BEFFOR_SPREAD_FOR_HURT=15 �ͷ�ǰ�� ���ܴ���  ������  ������չʾ
		AttackBuff buff = super.touchOffBefforSpreadForBuff(source,target,TouchOffUsedEffectSpread.BEFFOR_SPREAD_FOR_HURT,true);
		//��ʼ�ͷ���ͨ�������� �磺��Ϯ֮���
		super.spreadStartChanged(source,ability);
		touchOff(source,target);
		setTarget(target);
		
		AttackTransmitter attackTransmitter=new AttackTransmitter(
			source.getScene(),source,target,ability);
		//��ӹ�������
		attackTransmitter.setAttackBuff(buff);
		Formula aFormula=source.getScene().getFormula(getFormulaType());
		for(int i=getCount()-1;i>=0;i--)
		{
			attackTransmitter.currentCount=i;
			//���﹥����ɺ� ����Ժ�汾Ŀ��Դ��Ϊ�����˺������ᵼ��Ŀ��ԴΪ������쳣  
			aFormula.compute(attackTransmitter);
			//������ɺ�  ���Լ����ص�����BUFF ����Ҫ�������� �����Ҫ�� �Ƴ�һ���Լ��ܷ���ǰ  ���ⱻ�Ƴ�
			attackTransmitter.spreadTouchOffAbility(attackTransmitter.fighter,null,TouchOffSpread.SPREAD_END);
			//����ͷ�Դһ�γ��ּ�ʧЧ�ļ��� usefulTime =-1 
			source.getAbilityList().removeOnceAbility();
		}
		ability.consume(source);
		super.spreadOverChanged(source,ability);
		return true;
	}
}