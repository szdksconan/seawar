package foxu.fight;

/**
 * ���߼��㷽ʽ����2��N�η�����λ����
 * 
 * @author ZYT
 */
public class ImmuneEffect extends Effect
{

	/* fields */
	/** �������� */
	int immuneType;

	/* methods */
	/**
	 * ����Ƿ�����ָ������
	 * 
	 * @param ability ���ļ���
	 * @param fighter ����fighter
	 * @return ����true�����������
	 */
	public boolean isImmune(Ability ability,Fighter attacker,Fighter fighter)
	{
		// ���Ч��������ʱ��,��ô�ж�Ч����û�й���
		if(usefulTime>0
			&&fighter.getScene().getCurrentRound()-ability.getStartTime()>usefulTime)
		{
			enable=false;
			return false;
		}
		int type=ability.getType();
		return (immuneType&type)!=0;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[immuneType="+immuneType+"] ";
	}
}