package foxu.fight;

/**
 * ����һ��״̬���ܵĸ�������
 * 
 * @author ZYT
 */
public class StateSpead extends Spread
{

	/* methods */
	public int checkUsed(Fighter source,Object target,Ability ability)
	{
		return 0;
	}
	public boolean used(Fighter source,Object target,Ability ability)
	{
		return false;
	}
}