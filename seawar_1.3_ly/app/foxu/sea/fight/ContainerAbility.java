/**
 * 
 */
package foxu.sea.fight;

/**
 * ���˼����е��������� ����һ�齢�� sid:����sid �Ķ�Ӧ��ϵ
 * 
 * @author rockzyt
 */
public class ContainerAbility extends AllianceSkill
{

	/* fields */
	/** ������Ӧ�ļ��� ͬһ����sidֻ�ܳ���һ�� */
	int[] shipAbilityList;

	/* methods */
	public int getAbilitySidByShipSid(int shipSid)
	{
		for(int i=0;i<shipAbilityList.length;i+=2)
		{
			if(shipSid==shipAbilityList[i]) return shipAbilityList[i+1];
		}
		return 0;
	}
}
