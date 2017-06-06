package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.fight.ControlEffect;
import foxu.fight.Fighter;


/**
 * @author rockzyt
 * �ɹ����ƽ�ɫһ��,���Ƴ��ü���
 */
public class ControlOnceEffect extends ControlEffect
{

	/* methods */
	/**
	 * �ж��Ƿ��иĽ�ɫ�ͷż��ܵĶ�Ӧ�Ŀ�������
	 * 
	 * @param fighter �ͷż��ܵĽ�ɫ
	 * @param ability ��ǰfighterʹ�õļ���
	 * @return ����true��ʾ������
	 */
	public boolean checkControl(Fighter fighter,Ability ability)
	{
		//���ε� ���ڰ��������غϼ��ܴ���  ������������� ����Ϊ���� �غϽ������ж��Ƴ�
		if(super.checkControl(fighter,ability))
		{
			fighter.clearClientAbility();
			fighter.removeAbility(getAbility());
			return true;
		}
		return true;
	}
}