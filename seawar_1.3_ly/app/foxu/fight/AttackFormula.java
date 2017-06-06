package foxu.fight;

import mustang.event.ChangeListener;

/**
 * ������ �������ս��˫���ĸ�����ֵ
 * 
 * @author ZYT
 */
abstract public class AttackFormula extends Formula
{

	/**
	 * ����Ч�����õ�Ŀ���ϲ������˺�
	 * 
	 * @param target Ŀ��
	 * @param ability ����
	 * @param e ���ݴ������
	 * @param attAlonePercent �������� ��������ϵ��
	 * @param isMustHit �Ƿ����
	 * @param isMustErupt �Ƿ�ر�
	 */
	abstract public void computeEffect(Fighter target,AttackTransmitter e);
	
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
		return e;
	}
	/**
	 * ����Ŀ�����ܵ����˺�,���˹�������˺�
	 * 
	 * @param target Ŀ��Ϊ����fighter
	 * @param ability ����˺��ļ���
	 * @param e ���ݴ������
	 */
	public void computeTarget(Fighter target,AttackTransmitter e)
	{
		if(target==null) return;
		Ability ability=e.ability;
		computeEffect(target,e);
		ChangeListener listener=target.getChangeListener();
		if(listener!=null)
			listener.change(this,FightScene.FightEvent.ATTACK_ONCE.ordinal(),ability,
				e.fighter,target);
	}
	/**
	 * ����Ŀ�����ܵ����˺�
	 * 
	 * @param targets Ŀ��Ϊ����
	 * @param ability ����˺��ļ���
	 * @param e ���ݴ������
	 */
	public void computeTarget(Fighter[] targets,AttackTransmitter e)
	{
		if(targets==null||targets.length<=0) return;
		
		for(int j=0,i=targets.length;j<i;j++)
		{
			if(targets[j]!=null)
			{
				e.currentRange=j;
				computeEffect(targets[j],e);
			}
		}
	}
}