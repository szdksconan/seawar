package foxu.fight;

import mustang.event.ChangeListener;
import mustang.math.MathKit;
import mustang.util.Sample;
import foxu.fight.FightScene.FightEvent;
import foxu.sea.fight.AddAbilityEffectForSource;
import foxu.sea.fight.AddShieldEffect;
import foxu.sea.fight.AddAttBaseAttributeEffect;
import foxu.sea.fight.FightShowEventRecord;

/**
 * ��˵���������ͷ�
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */
public abstract class Spread extends Sample
{

	/* static fields */
	/** ʩ�Ŷ�����NEAR=1����FAR=0Զ�� */
	public final static int NEAR=1,FAR=0;
	// ��Ϣ�������ֶ�:500-600Ϊ�����ͷ�ʱ������ʧ����Ϣ,600-700Ϊ��������ʱ������ʧ����Ϣ,
	// 700-800Ϊ��ʱս����Ϸ��������������ʧ����Ϣ
	/**
	 * ������֮��ͨѶ����:FAILD_CONTROL=500�ͷŽ�ɫ������,FAILD_NOT_TARGET=501Ŀ�겻��������,
	 * FAILD_TARGET_DEAD=502Ŀ��������,FAILD_SOURCE_DEAD=503�ͷ�Դ����,FAILD_TARGET_NULL=504Ŀ�겻����
	 */
	public final static int FAILD_CONTROL=500,FAILD_NOT_TARGET=501,
					FAILD_TARGET_DEAD=502,FAILD_SOURCE_DEAD=503,
					FAILD_TARGET_NULL=504,FAILD_CONSUME=505;

	/* fields */
	/** ʩ�����ͣ����������������� */
	int type;
	/** ʩ�Ŷ���������1��Զ�� 0 */
	int action;
	/** �ͷŵ�Ŀ����飺0������1���� */
	int targetTeam;
	/** ��Χ */
	int range;
	/** ���� */
	int count=1;
	/** ���㹫ʽ���� */
	int formulaType;
	/** ���ռ��ܶ��� */
	Ability ability;

	/* dynamic fields */
	/** �ͷ�Դ */
	protected int source;
	/** ����ı���Ŀ�� */
	Object target;

	/* properties */
	/** ��÷��ռ��� */
	public Ability getAbility()
	{
		return ability;
	}
	/** ��ü��㹫ʽ���� */
	public int getFormulaType()
	{
		return formulaType;
	}
	/** ���ü��㹫ʽ���� */
	public void setFormulaType(int formulaType)
	{
		this.formulaType=formulaType;
	}
	/** �����µ�Ŀ�� */
	public void setTarget(Object target)
	{
		this.target=target;
	}
	/** ���ʩ��Ŀ�� */
	public Object getTarget()
	{
		return target;
	}
	/** ���ʩ�ŵ�Ŀ�����: 0������1���� */
	public int getSpreadTeam()
	{
		return targetTeam;
	}
	/** ����ͷŴ��� */
	public int getCount()
	{
		return count;
	}
	/** �õ�ʩ�Ŷ��� ����1��Զ�� 0(�ڼ�ʱ��Ϸ�������ֻ��ʾ�ͷž���) */
	public int getAction()
	{
		return action;
	}
	/** ������� */
	public int getType()
	{
		return type;
	}
	/**
	 * �����ͷŷ�Χ
	 * 
	 * @param range ��Χ
	 */
	public void setRange(int range)
	{
		this.range=range;
	}
	/** ����ͷŷ�Χ */
	public int getRange()
	{
		return range;
	}
	/** �õ��ͷ�Դ */
	public int getSpreadSource()
	{
		return source;
	}
	/** �����ͷ�Դ */
	public void setSpreadSource(int source)
	{
		this.source=source;
	}

	/* abstract methods */
	/** ���Դ��ɫ�ܷ��Ŀ���ɫʹ�øü��� */
	public abstract int checkUsed(Fighter source,Object target,
		Ability ability);
	/** Դ��ɫ��Ŀ���ɫʹ�øü��� */
	public abstract boolean used(Fighter source,Object target,Ability ability);

	/* methods */
	/**
	 * ����Ŀ��
	 * 
	 * @param source �����ͷ�Դ
	 * @param target Ŀ�����
	 * @return �����ҵ���Ŀ������
	 */
	public Fighter[] findTarget(Fighter source,Object target,Ability ability)
	{
		return null;
	}
	/**
	 * �����ͷ������ϵļ���
	 * 
	 * @param source �ͷ���
	 * @param target Ŀ��
	 */
	public void touchOff(Fighter source,Object target)
	{
		Ability[] abilityes=source.getAbilityList().getAllAbility();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			if(abilityes[i]!=null
				&&abilityes[i].getSpread() instanceof TouchOffSpread)
			{
				TouchOffSpread spread=(TouchOffSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(TouchOffSpread.BEFFOR_SPREAD);
				if(abilityes[i].checkUsed(source,target)==0)
				{
					abilityes[i].used(source,target);
				}
			}
		}
	}
	
	/**
	 * �ͷ�ǰ �����ͷ������ϵļ��� ��ɹ�������BUFF ���� �������
	 * 
	 * @param source �ͷ���
	 * @param target Ŀ��
	 * @param useTouchOffSkill �Ƿ�ʹ�ô���ʽ�ļ��ܣ���������ֻ���Ǿ��ټ��ܣ�
	 */
	public AttackBuff touchOffBefforSpreadForBuff(Fighter source,Object target,int touchTime,boolean useTouchOffSkill){
	AttackBuff buff = new AttackBuff();
	AttBaseAttribute aba = new AttBaseAttribute();
	Ability[] abilityes=source.getAbilityOnSelf();
	for(int i=abilityes.length-1;i>=0;i--)
	{
		Effect[] effect=abilityes[i].getEffects();
		if(!abilityes[i].isEnabled()) continue;
		if(abilityes[i].getSpread() instanceof TouchOffSpread)
		{
			TouchOffSpread spread=(TouchOffSpread)abilityes[i]
				.getSpread();
			spread.setCurrentTouchTime(touchTime);
			if(abilityes[i].checkUsed(source,target)==0)
			{
				for(int j=effect.length-1;j>=0;j--)
				{
					//��д���߼� �ÿ��� 
					if(useTouchOffSkill){
					if(effect[j] instanceof ChangeHurtBuffEffect){//�Ƿ��ж�������
						if(((ChangeHurtBuffEffect)effect[j]).getTouchOffTime()==ChangeHurtBuffEffect.TOUCH_OFF_BEFORE_SPREAD){
							showChangeByte(source,target,abilityes[i],buff);
							((ChangeHurtBuffEffect)effect[j]).used(source,target,buff);
						}
					}
					else if(effect[j] instanceof AddAbilityEffectForSource){//Ϊ�ͷ�Դ���һ������
						showChangeByte(source,target,abilityes[i],buff);
						((AddAbilityEffectForSource)effect[j]).used(source,target,null);
					}else if(effect[j] instanceof AddShieldEffect){//��������
						showChangeByte(source,target,abilityes[i],buff);
						((AddShieldEffect)effect[j]).used(source,abilityes[i],buff);
					}
					}
					
					if(effect[j] instanceof AddAttBaseAttributeEffect){//����Ϊ����Ч�� �Ǵ�������
						//showChangeByte(source,target,abilityes[i],buff);
						((AddAttBaseAttributeEffect)effect[j]).used(source,target,aba);
					}
					
				}
			}
		}
	}
	buff.setAttBaseAttribute(aba);
	return buff;
	}
	
	public void showChangeByte(Fighter source,Object target,Ability ability,AttackBuff buff){
		FightShowEventRecord listener=(FightShowEventRecord)source.getChangeListener();
		if(listener!=null)
		{
				listener.changeForOfficer(source,FightShowEventRecord.LAG_TYPE,ability,null);//�����ӳ��¼�
				listener.changeForOfficer(source,FightShowEventRecord.OFFICER_SKILL_START_TYPE,ability,null);
		}
		//��¼���ټ���ID ʹ�����
		buff.setSid(ability.getSid());
		buff.setUse(true);
	}
	
	
	/**
	 * ��ʼ�ͷ�������change�¼�
	 * 
	 * @param source �ͷ�Դ
	 * @param ability �ͷŵļ���
	 */
	public void spreadStartChanged(Fighter source,Ability ability)
	{
		ChangeListener listener=source.getChangeListener();
		if(listener!=null)
			listener.change(this,FightEvent.SPREAD_START.ordinal(),source,ability);
	}
	/**
	 * �����ͷ�������change�¼�
	 * 
	 * @param source �ͷ�Դ
	 * @param ability �ͷŵļ���
	 */
	public void spreadOverChanged(Fighter source,Ability ability)
	{
		ChangeListener listener=source.getChangeListener();
		if(listener!=null)
			listener.change(this,FightEvent.SPREAD_OVER.ordinal(),source,ability);
	}
}