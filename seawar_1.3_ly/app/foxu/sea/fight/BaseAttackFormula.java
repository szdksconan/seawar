package foxu.sea.fight;

import mustang.event.ChangeListener;
import foxu.fight.Ability;
import foxu.fight.AttackBuff;
import foxu.fight.AttackEffect;
import foxu.fight.AttackFormula;
import foxu.fight.AttackTransmitter;
import foxu.fight.ChangeAttributeEffect;
import foxu.fight.Effect;
import foxu.fight.EffectAble;
import foxu.fight.FightScene;
import foxu.fight.FightScene.FightEvent;
import foxu.fight.Fighter;
import foxu.fight.TouchOffSpread;
import foxu.sea.PublicConst;

/**
 * �������㹫ʽ �����˺�=������-�Է�����
 * 
 * @author ZYT
 */
public class BaseAttackFormula extends AttackFormula
{

	/* fields */
	/** �������� */
	float eruptFactor=2;

	/* methods */
	/** ���㷽���������Ǽ������ݰ�װ���󣬷��ؼ����� */
	public Object compute(Object obj)
	{
		AttackTransmitter e=(AttackTransmitter)obj;
		if(e.targets instanceof Fighter[])
		{
			computeTarget((Fighter[])e.targets,e);
		}
		else
		{
			computeTarget((Fighter)e.targets,e);
		}
		e.scene.checkOutTargetDead(e.scene.getFighterContainer()
			.getAllFighter());
		// ���û���Ч��
		resetShield(e);
		// todo ���ܻ����ʱ �Ƴ������ƻ�
		removeOnceEffect(e.fighter);
		return e;
	}
	
	/**���û���Ч��*/
	public void resetShield(AttackTransmitter e)
	{
		if(e.getAttackBuff().isOpenShield()&&e.getAttackBuff().getShield()>0)
		{
			if(e.fighter!=null)
			{
				((FleetFighter)e.fighter).setShield((int)e.getAttackBuff()
					.getShield());
				// ���ջ��ܲ���
				if(e.getAttackBuff().isOpenShield())
				{
					FightShowEventRecord OfficerChanger=(FightShowEventRecord)e.fighter
						.getChangeListener();
					if(OfficerChanger!=null)
					{
						OfficerChanger.changeForOfficer(e.fighter,
							FightShowEventRecord.OPEN_SHIELD_TYPE,
							e.getAttackBuff(),null);
					}
				}
			}
		}
	}
	
	/**
	 * ���������޸�һ����ֵ
	 * 
	 * @param source ʩ��Դ
	 * @param target Ŀ��
	 * @param touchOffTime ����ʱ��
	 * @param ability ����
	 * @param value ��Ҫ�޸ĵ�����
	 * @return ���ؼ�����ɺ������
	 */
	public float touchOffChangeValue(Fighter source,Object target,
		int touchOffTime,Ability ability,float value)
	{
		Ability[] abilitys=source.getAbilityOnSelf();
		int precent=100;
		// �жϴ�������(���㹥����ʱ)
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i].isEnabled())
			{
				if(abilitys[i].getSpread() instanceof TouchOffSpread)
				{
					TouchOffSpread spread=(TouchOffSpread)abilitys[i]
						.getSpread();
					spread.setCurrentTouchTime(TouchOffSpread.HARM);
					if(abilitys[i].checkUsed(source,target)==0)
					{
						Effect[] effects=abilitys[i].getEffects();
						for(int j=effects.length-1;j>=0;j--)
						{
							if(effects[j] instanceof ChangeAttributeEffect)
							{
								ChangeAttributeEffect effect=(ChangeAttributeEffect)effects[j];
								if(effect.checkType(ability.getType(),
									ability.getCid()))
								{
									if(effect.getValueType()==ChangeAttributeEffect.PRECENT)
									{
										precent+=effect.getValue(source);
									}
									else
									{
										value+=effect.getValue(source);
									}
								}
							}
						}
					}
				}
			}
		}
		return value*precent/100;
	}
	/**
	 * ����ͷ�Դ������ �ù��������������� </p>
	 * ������=������*{1-[ϵ��1*(��������-1)-ϵ��2*��������*(��������-1)/2]}
	 * 
	 * @param e ս���������������
	 * @param effect ��μ����AttackEffect
	 * @param baseAttack ��fighter���ϻ�õĻ�������
	 * @param target ����Ŀ��
	 * @return ���ؼ�����
	 */
	public float getAttack(AttackTransmitter e,AttackEffect effect)
	{
		Fighter source=e.fighter;
		float baseAttack=e.fighter.getAttrValue(PublicConst.ATTACK);
		baseAttack=touchOffChangeValue(source,null,TouchOffSpread.HARM,
			e.ability,baseAttack);
		// System.out.println(">..............���չ���:"+baseAttack);
		return baseAttack;
	}
	/**
	 * ���Ŀ�������
	 * 
	 * @param target ����Ŀ��
	 * @param e ս���������������
	 * @param effect ��ǰ���ڼ����Ч��
	 * @return ���ط�����
	 */
	public float getDefence(AttackTransmitter e,Fighter target,
		AttackEffect effect)
	{
		// System.out.println(">................�������...................<");
		Fighter source=e.fighter;
		float defence;
		int sNum=(int)source.getAttrValue(PublicConst.SHIP_NUM);
		// int tNum=(int)target.getAttrValue(PublicConst.SHIP_NUM);
		defence=target.getAttrValue(PublicConst.DEFENCE)*sNum;
		// if(sNum<tNum)
		// {
		// defence=target.getAttrValue(PublicConst.DEFENCE)*sNum;
		// }
		// else
		// {
		// defence=target.getAttrValue(PublicConst.DEFENCE)*tNum;
		// }
		// System.out.println("..............Ŀ���������:"+defence);
		// defence=((WkFighter)target).computeValueForAbility(Skill.DEFENSE
		// |effect.getAttackType(),ChangeAttributeEffect.ABSOLUTE_VALUE,
		// defence);
		// defence=((WkFighter)target).computeValueForAbility(Skill.DEFENSE
		// |effect.getAttackType(),ChangeAttributeEffect.PRECENT,defence);
		defence=touchOffChangeValue(target,null,TouchOffSpread.DEFENCE,
			e.ability,defence);
		// System.out.println("..............��Ŀ�������ܸı�����Ŀ�����:"+defence);
		return defence;
	}
	/**
	 * �������ܡ�����
	 * 
	 * @param source ������
	 * @param target Ŀ��fighter
	 * @param ability ʩ�ŵļ���
	 * @return ����true��ʾ���ܳɹ�
	 */
	public boolean isExempt(Fighter source,Fighter target,Ability ability,AttackTransmitter e)
	{
		// System.out.println("����������");
		if(ability.isMustHit()) return false;
		double hitRate=source.getAttrValue(PublicConst.ACCURATE);
		if(e.getAttackBuff().getAttBaseAttribute().getHit()!=0){
			//System.out.println("���мӳɣ�"+e.getAttackBuff().getAttBaseAttribute().getHit());
			hitRate+=e.getAttackBuff().getAttBaseAttribute().getHit();
		}
		double exempt=target.getAttrValue(PublicConst.AVOID);
		if(e.getAttackBuff().getDefBaseAttribute().getDodge()!=0){
			//System.out.println("���ܼӳɣ�"+e.getAttackBuff().getDefBaseAttribute().getDodge());
			exempt+=e.getAttackBuff().getDefBaseAttribute().getDodge();
		}
		// System.out.println(">................����:"+hitRate);
		// System.out.println(">................����:"+exempt);
		double result=100+hitRate-exempt;
		// ��������
		if(result<=0)	result=3;
		FightScene scene=source.getScene();
		int randomValue=scene.getRandom().randomValue(
			FightScene.RANDOM_MINI,FightScene.RANDOM_MAX)/FightScene.OLD_TIMES;
		// System.out.println(".......................����������:>"+result+" �����:"
		// +randomValue);
		return randomValue>result;
	}
	/**
	 * fighterѪ���仯
	 * 
	 * @param f Ѫ���ı��Fighter
	 * @param value �ı�ֵ
	 */
	public void fighterLoseHp(Fighter f,float value)
	{
		f.setDynamicAttr(PublicConst.FLEET_HP,f.getAttrValue(PublicConst.FLEET_HP)-value);
	}
	/**
	 * �����Ƿ񱩻�
	 * 
	 * @param source ������
	 * @param target ��������
	 * @return �����Ƿ񱩻�
	 */
	public boolean computErupt(Fighter source,Fighter target,AttackBuff buff)
	{
		float critialHit=source.getAttrValue(PublicConst.CRITICAL_HIT); // �����߱���
		if(buff.getAttBaseAttribute().getCrit()!=0){//��Ҫ��ʱ�������Դ�������F�ı���
			//System.out.println("�����ӳɣ�"+buff.getAttBaseAttribute().getCrit());
			critialHit+=buff.getAttBaseAttribute().getCrit();
		}
		float critialHitResist=target
			.getAttrValue(PublicConst.CRITICAL_HIT_RESIST); // �������ߵֿ�����
		if(buff.getDefBaseAttribute().getToughness()!=0){
			//System.out.println("���Լӳɣ�"+buff.getDefBaseAttribute().getToughness());
			critialHitResist+=buff.getDefBaseAttribute().getToughness();
		}
		// System.out.println(">................������:"+critialHit);
		// System.out.println(">................�����ֿ�:"+critialHitResist);
		float value=critialHit-critialHitResist;
		if(value<=0) return false;
		FightScene scene=source.getScene();
		int randomValue=scene.getRandom().randomValue(
			FightScene.RANDOM_MINI,FightScene.RANDOM_MAX)/FightScene.OLD_TIMES;
		// System.out.println(">.................ʵ�ʱ�����:"+value+" �����:"
		// +randomValue);
		return randomValue<value;
	}
	/**
	 * ������
	 * 
	 * @param e ��������
	 * @param target ��������Ŀ��
	 * @param effect �˴ι�����Effect
	 * @param attack ������
	 * @param defence ������
	 * @param buff ���Ԥ���� ÿ�λ���ʱ�����BUFF  clone AttackTransmitter��AttackBuff���� 
	 */
	public void computeResult(AttackTransmitter e,Fighter target,
		AttackEffect effect,float attack,float defence,AttackBuff buff)
	{
		//System.out.println("��ʼ��������"+attack);
//		System.out.println("����:"
//			+((FleetFighter)e.fighter).getFleet().getShip().getSid());
//		System.out.println("�շ�:"
//			+((FleetFighter)target).getFleet().getShip().getSid());
		if(buff.getAttBaseAttribute().getHurtPercent()!=0){
			attack+=attack*buff.getAttBaseAttribute().getHurtPercent()/100;
			//System.out.println("��߹�����ϵ����"+buff.getAttBaseAttribute().getHurtPercent()+"  ��� ��Ĺ�������"+attack);
		}
		float hurt=(attack-defence)*effect.getValue(target);
//		System.out.println("..........�����˺�:"+hurt+" ������:"+attack+" ������:"
//			+defence);
		float percent=target.getAttrValue(effect.getAttackType());
		//�����ߣ����ݱ����������ͽ���װ�������˺�ϵ���ӳɼ���
		percent=percent
			+e.fighter.getAttrValue(PublicConst.ATTACH_BASE
				+target.getFighterType());
//		System.out.println("_______--------_______--------attack type:"+effect.getAttackType()+",targer type:"+target.getFighterType());
//		System.out.println("_______--------_______--------fighter attach percent:"+e.fighter.getAttrValue(PublicConst.ATTACH_BASE
//			+target.getFighterType()));
		//�������ߣ����ݹ������ͽ���װ���������˺�ϵ���������
		percent=percent
			+target.getAttrValue(PublicConst.RESIST_AIR_RAID
				+effect.getAttackType());
//		System.out.println("_______--------_______--------target attach percent:"+target.getAttrValue(PublicConst.RESIST_AIR_RAID
//			+effect.getAttackType()));
		hurt=hurt*(100+percent)/100.0f;// ���Լ���
//		System.out.println("����֮��>>>>:"+hurt);
		boolean flag=false;
		if(hurt<=0) hurt=1;
//		System.out.println("--------------------�˺���ӡ----------------------");
		if(computErupt(e.fighter,target,buff)||buff.isMustErup())//���㱩��
		{
			hurt*=eruptFactor;
			// ��������˱��������Ϊture
			flag=true;
		}
//		System.out.println("..........���������:"+hurt);
		hurt=e.changeHurt(hurt,e.fighter,target,TouchOffSpread.HURT,
			EffectAble.HURT_VALUE);
//		System.out.println("..........���������������ܸı������˺�:"+hurt);
		hurt=e.changeHurt(hurt,target,e.fighter,TouchOffSpread.BE_HURT,
			EffectAble.BE_HURT_VALUE);
		
		//�������ܶ�������
		if(buff.getAttAlonePercent()!=0)hurt+=hurt*buff.getAttAlonePercent()/100.0f;
		//����buff ���� ����
		if(buff.getDefBaseAttribute().getHurtResistancePercent()!=0){
			//System.out.println("����ǰ���˺�:"+hurt);
			hurt-=hurt*buff.getDefBaseAttribute().getHurtResistancePercent()/100.0f;
			//System.out.println("���˺���˺�"+hurt);
		}
		if(hurt<1){ //����Ϊ1
			hurt = 1;
		}
		
		e.changeEndHurt(hurt,e.fighter,target,TouchOffSpread.HURT_END,
			EffectAble.HURT_VALUE);
		
		//������
		if(buff.isOpenShield())buff.addShield(hurt);//�Ƿ��������
		hurt = ((FleetFighter)target).reduceHurtByShiled(hurt);//����Ŀ�����ϻ���
		
//		System.out.println("..........���������������ܸı������˺�::"+hurt);
		int currentNum=(int)target.getAttrValue(PublicConst.SHIP_NUM);
		fighterLoseHp(target,hurt);
		e.removeControlAbility(target);
//		System.out.println("..........���û�Ʒ�,��ӡ������˺�:"+hurt);
		ChangeListener listener=target.getChangeListener();
		if(listener!=null)
		{
			//���ټ�������Ч��
			officerHitChange(target,buff);
			if(flag)
			{
				listener.change(this,FightEvent.ERUPT.ordinal(),(int)hurt,
					target,currentNum);
			}
			else
			{
				listener.change(this,FightEvent.HURT.ordinal(),(int)hurt,
					target,currentNum);
			}
		}
		// ��������
		float returnHurt  = e.hurtInReturn(hurt,e.fighter,target);
		if(returnHurt>0&&returnHurt<1){//����Ϊ1
			returnHurt = 1;
		}
		if(returnHurt>0){
		currentNum=(int)e.fighter.getAttrValue(PublicConst.SHIP_NUM);
		fighterLoseHp(e.fighter,returnHurt);
		if(listener!=null)
		{
				((FightShowEventRecord)listener).changeForOfficer(e.fighter,FightShowEventRecord.RETURN_HURT_TYPE,(int)returnHurt,currentNum);
		}
		}
		
		// ��������Դ�ı�������
		e.spreadTouchOffAbility(e.fighter,target,TouchOffSpread.HIT);
		// ����Ŀ��ı�������
		e.spreadTouchOffAbility(target,e.fighter,TouchOffSpread.BE_HIT);
		if(target.isDead())
		{
			e.spreadTouchOffAbility(target,e.fighter,TouchOffSpread.DEAD);
			//�Ƴ�ֱ�ӷ��͵������¼�
			//if(listener!=null)
			//	listener.change(this,FightEvent.DEAD.ordinal(),target); 

		}
//		 System.out
//		 .println("........................һ�ι������..............................");
//		 System.out.println();
//		 System.out.println();
	}
	
	/**
	 * ���ټ�������
	 * @param target Ŀ��
	 * @param buff 
	 */
	public void officerHitChange(Fighter target,AttackBuff buff){
		if(buff.isUse()){
			FightShowEventRecord OfficerChanger=(FightShowEventRecord)target.getChangeListener();
			if(OfficerChanger!=null)
			{
				OfficerChanger.changeForOfficer(target,FightEvent.ADD_ABILITY.ordinal(),buff,null);
			}
			//Ч������һ�ξ����
			buff.setUse(false);
			buff.setSid(0);
		}
	}
	
	
	/**
	 * ����Ч�����õ�Ŀ���ϲ������˺�
	 * 
	 * @param target Ŀ��
	 * @param e ���ݴ������
	 * @param attAlonePercent ��������
	 */
	public void computeEffect(Fighter target,AttackTransmitter e)
	{
		if(target==null||target.isDead()) return;
		
		//�����ͷź�  ����ǰ������BUFF ����  ����Ҫ�����Ŀ�����ϵ�BUFF��
		AttackBuff buff = e.getAttackBuff()/*.clone()*/;
		e.touchOffAfterSpreadForBuff(e.fighter,target,buff);
		
		Ability ability=e.ability;
		Fighter source=e.fighter;
		
		if(!buff.isMustHit()&&isExempt(source,target,ability,e))//��������
		{
			ChangeListener listener=target.getChangeListener();
			if(listener!=null)
				listener.change(this,FightEvent.EXEMPT.ordinal(),target,
					ability);
			return;
		}
		
		if(!isImmune(source,target,ability))
		{
			Effect[] effects=ability.getEffects();
			AttackEffect attackEffect;
			// �������ܵ�����effect,����ÿeffect��ս������
			for(int i=effects.length-1;i>=0;i--)
			{
				attackEffect=(AttackEffect)effects[i];
				float defence=getDefence(e,target,attackEffect);
				float finalAttack=getAttack(e,attackEffect);
				computeResult(e,target,attackEffect,finalAttack,defence,buff);
			}
		}
	}
	
	/** �Ƴ������ƻ� */
	public void removeOnceEffect(Fighter fighter)
	{
		if(fighter!=null){
			Ability[] abilityes=fighter.getAbilityOnSelf();
			for(int i=abilityes.length-1;i>=0;i--)
			{
				Effect[] effect=abilityes[i].getEffects();
				if(!abilityes[i].isEnabled()) continue;
				for(int j=effect.length-1;j>=0;j--)
				{
					if(effect[j] instanceof ChangeHurtOnceEffect)
					{
						ChangeHurtOnceEffect onceEffect=(ChangeHurtOnceEffect)effect[j];
						if(onceEffect.getHurtTime()==EffectAble.HURT_VALUE)
						{
							fighter.removeAbility(onceEffect.getAbility());
						}
					}
				}
			}
		}
	}

}