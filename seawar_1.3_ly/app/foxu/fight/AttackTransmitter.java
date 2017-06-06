package foxu.fight;

import foxu.sea.fight.AddDefBaseAttributeEffect;
/**
 * ���ݴ�����
 * </p>
 * ����ս��ʱ���ݵ��ռ��ʹ���
 * 
 * @author ZYT
 */

public class AttackTransmitter
{

	/* fields */
	/** ���� */
	public FightScene scene;
	/** ������ */
	public Fighter fighter;
	/** �������� */
	public Object targets;
	/** ʹ�õļ��� */
	public Ability ability;
	/** ����Ч�� */
	public Effect[] effects;
	/** �ڼ�������Ŀ�� */
	public int currentRange;
	/** �ڼ��ι��� */
	public int currentCount;
	/** ����ǰ����������BUFF */
	public AttackBuff AttackBuff;
	
	
	
	

	/** ����ǰ����������BUFF */
	public AttackBuff getAttackBuff()
	{
		return AttackBuff;
	}

	/** ����ǰ����������BUFF */
	public void setAttackBuff(AttackBuff attackBuff)
	{
		AttackBuff=attackBuff;
	}

	/* constractors */
	/** ���췽�� */
	public AttackTransmitter(FightScene scene,Fighter fighter,
		Object targets,Ability ability)
	{
		this.scene=scene;
		this.fighter=fighter;
		this.targets=targets;
		this.ability=ability;
		this.effects=ability.getEffects();
	}

	/* methods */
	/** �Ƴ���Ӧ�Ŀ��Ƽ��� */
	public void removeControlAbility(Fighter fighter)
	{
		Ability[] abilitys=fighter.getAbilityOnSelf();
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i].isBreak())
			{
				fighter.removeAbility(abilitys[i]);
			}
		}
	}
	
	/**
	 *  �ͷŹ����� buff����
	 * @param source
	 * @param target
	 * @param buff ��ʱ��ӵ� ��������
	 * @return
	 */
	public void touchOffAfterSpreadForBuff(Fighter source,Fighter target,AttackBuff buff){
		DefBaseAttribute dba = new DefBaseAttribute();
		Ability[] abilityes=target.getAbilityOnSelf();//���Ŀ�����ϵķ���BUFF ����
		for(int i=abilityes.length-1;i>=0;i--)
		{
			Effect[] effect=abilityes[i].getEffects();
			if(!abilityes[i].isEnabled()) continue;
			if(abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				TouchOffSpread spread=(TouchOffSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(TouchOffSpread.AFTER_SPREAD_FOR_HURT);
				if(abilityes[i].checkUsed(fighter,target)==0)
				{
					for(int j=effect.length-1;j>=0;j--)
					{
						if(effect[j] instanceof AddDefBaseAttributeEffect)
						{
							((AddDefBaseAttributeEffect)effect[j]).used(source,target,dba);
						}
					}
				}
			}
		}
		buff.setDefBaseAttribute(dba);
		}
	
	
	
	/**
	 * �������ܸı��˺�
	 * 
	 * @param hurt �˺�ֵ
	 * @param fighter �ͷ�Դ
	 * @param target Ŀ��
	 * @param time ��ǰ����ʱ��
	 * @param hurtType �˺�����(�������Ǳ�����)
	 * @return
	 */
	public float changeHurt(float hurt,Fighter fighter,Fighter target,
		int time,int hurtType)
	{
		Ability[] abilityes=fighter.getAbilityOnSelf();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			Effect[] effect=abilityes[i].getEffects();
			if(!abilityes[i].isEnabled()) continue;
			if(abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				TouchOffSpread spread=(TouchOffSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(time);
				if(abilityes[i].checkUsed(fighter,target)==0)
				{
					for(int j=effect.length-1;j>=0;j--)
					{
						if(effect[j] instanceof EffectAble)
						{
							hurt=((EffectAble)effect[j]).used(fighter,
								target,hurt,hurtType);
						}
					}
				}
			}
			else
			{
				for(int j=effect.length-1;j>=0;j--)
				{
					if(effect[j] instanceof ChangeHurtEffect)
					{
						hurt=((ChangeHurtEffect)effect[j]).used(fighter,
							target,hurt,hurtType);
					}
				}
			}
		}
		return hurt;
	}
	
	/**
	 * ���Ŀ�����Ϸ���
	 */
	public float hurtInReturn(float hurt,Fighter fighter,Fighter target){
		Ability[] abilityes=target.getAbilityOnSelf();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			Effect[] effect=abilityes[i].getEffects();
			for(int j=effect.length-1;j>=0;j--)
			{
				if(effect[j] instanceof HurtInReturnEffect)
				{
					return ((HurtInReturnEffect)effect[j]).used(fighter,
						target,hurt,0);
				}
			}
		}
		return 0;
	}
	
	
	/**
	 * �������ܸı��ж��˺�
	 * 
	 * @param hurt �˺�ֵ
	 * @param fighter �ͷ�Դ
	 * @param target Ŀ��
	 * @param time ��ǰ����ʱ��
	 * @param hurtType �˺�����(�������Ǳ�����)
	 * @return
	 */
	public void changeEndHurt(float hurt,Fighter fighter,Fighter target,
		int time,int hurtType)
	{
		Ability[] abilityes=fighter.getAbilityOnSelf();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			Effect[] effect=abilityes[i].getEffects();
			if(!abilityes[i].isEnabled()) continue;
			if(abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				TouchOffSpread spread=(TouchOffSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(time);
				if(abilityes[i].checkUsed(fighter,target)==0)
				{
					for(int j=effect.length-1;j>=0;j--)
					{
						if(effect[j] instanceof EffectAble)
						{
							((EffectAble)effect[j]).used(fighter,target,
								hurt,hurtType);
						}
					}
				}
			}
		}
	}
	
	

	/**
	 * �ͷŴ���ʽ����
	 * 
	 * @param source Դ
	 * @param target Ŀ��
	 */
	public void spreadTouchOffAbility(Fighter source,Fighter target,int time)
	{
		Ability[] abilityes=source.getAbilityOnSelf();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			if(abilityes[i].isEnabled()&&abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				((TouchOffSpread)abilityes[i].getSpread())
					.setCurrentTouchTime(time);
				int value=abilityes[i].checkUsed(source,target);
				if(value==0)abilityes[i].used(source,target);
			}
		}
	}
}