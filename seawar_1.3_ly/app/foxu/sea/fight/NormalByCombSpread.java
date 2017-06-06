package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.AttackBuff;
import foxu.fight.AttackTransmitter;
import foxu.fight.Consumer;
import foxu.fight.TouchOffSpread;
import foxu.fight.FightScene.FightEvent;
import foxu.fight.Fighter;
import foxu.fight.Formula;
import foxu.fight.Spread;

public class NormalByCombSpread extends Spread
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
		super.spreadStartChanged(source,ability);
		touchOff(source,target);
		setTarget(target);
		AttackTransmitter attackTransmitter=new AttackTransmitter(
			source.getScene(),source,target,ability);
		//��ӹ�������
		attackTransmitter.setAttackBuff(buff);
		Formula aFormula=source.getScene().getFormula(getFormulaType());
		//��ѡȡһ��Ŀ�� ,��������� ��ѡȡ
		setTargets(source,target,attackTransmitter,ability);
		for(int i=getCount()-1;i>=0;i--)
		{
			attackTransmitter.currentCount=i;
			//���﹥����ɺ� ����Ժ�汾Ŀ��Դ��Ϊ�����˺������ᵼ��Ŀ��ԴΪ������쳣  
			aFormula.compute(attackTransmitter);

			//������ɺ�  ���Լ����ص�����BUFF ����Ҫ�������� �����Ҫ�� �Ƴ�һ���Լ��ܷ���ǰ  ���ⱻ�Ƴ�
			attackTransmitter.spreadTouchOffAbility(attackTransmitter.fighter,null,TouchOffSpread.SPREAD_END);
			//����ͷ�Դһ�γ��ּ�ʧЧ�ļ��� usefulTime =-1 
			source.getAbilityList().removeOnceAbility();
			
			//ÿ�ι���֮�����һ�����������¼�
			if(i>0){
				//�ٴ�ѡȡĿ��
				setTargets(source,target,attackTransmitter,ability);
				//�ж�ս���Ƿ����
				if(attackTransmitter.targets!=null){
					if(attackTransmitter.targets instanceof Fighter[] ){
						Fighter[] targets = (Fighter[])attackTransmitter.targets;
							for(int j=0;j<targets.length;j++){
								if(targets[j]!=null){
									// ����ʱ�ټ���һ��  ����ļ��� useFulTime = 1 �ļ��ܶ�Ϊ-1 ��ʾһ�γ��־�ʧЧ
									buff = super.touchOffBefforSpreadForBuff(source,target,TouchOffUsedEffectSpread.BEFFOR_SPREAD_FOR_HURT,false);
									// ����BUFF��ļӳ�
									attackTransmitter.setAttackBuff(buff);
									break;
								}
							}
					}else{
						// ����ʱ�ټ���һ��  ����ļ��� useFulTime = 1 �ļ��ܶ�Ϊ-1 ��ʾһ�γ��־�ʧЧ
						buff = super.touchOffBefforSpreadForBuff(source,target,TouchOffUsedEffectSpread.BEFFOR_SPREAD_FOR_HURT,false);
						// ����BUFF��ļӳ�
						attackTransmitter.setAttackBuff(buff);
					}
				}
				source.getChangeListener().change(this,FightEvent.ATTACK_CONTINUE.ordinal(),ability.getSid());
				
			}
		}
		ability.consume(source);
		super.spreadOverChanged(source,ability);
		return true;
	}
	
	/** ѡȡ����Ŀ�� */
	public void setTargets(Fighter source,Object target,AttackTransmitter attackTransmitter,Ability ability){
		if(target instanceof Fighter)
		{
			if(((Fighter)target).isDead())
			{
				Object obj=((FleetFighter)source).findTarget((Skill)ability);
				attackTransmitter.targets=obj;
			}
		}
		else
		{
			Fighter[] obj=(Fighter[])((FleetFighter)source).findTarget((Skill)ability);
			attackTransmitter.targets=obj;
		}
	}
	

}