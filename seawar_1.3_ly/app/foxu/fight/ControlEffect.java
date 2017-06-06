package foxu.fight;

/**
 * �ټ�����Ƶ�ʱ�����2��N�η���ʽ����λ����
 * 
 * @author ZYT
 */
public class ControlEffect extends Effect
{

	/* fields */
	/** ��������.����Ϊ2��N�η� */
	int controlType;
	/** �¼��ֶ�:��Ч����.Ĭ��100% */
	float precent=10000;

	/* properties */
	public float getPrecent()
	{
		return precent;
	}
	/** ��ÿ������� */
	public int getControlType()
	{
		return controlType;
	}

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
		int rd=fighter.getScene().getRandom().randomValue(
			FightScene.RANDOM_MINI,FightScene.RANDOM_MAX);
		if(rd>precent) return false;
		return checkType(ability);
	}

	/** �����Ƽ������� */
	public boolean checkType(Ability ability)
	{
		return (controlType&ability.getType())!=0;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[controlType="+controlType+"]";
	}
}