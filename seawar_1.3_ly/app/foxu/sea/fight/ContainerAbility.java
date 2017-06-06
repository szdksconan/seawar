/**
 * 
 */
package foxu.sea.fight;

/**
 * 联盟技能中的容器技能 包含一组舰船 sid:技能sid 的对应关系
 * 
 * @author rockzyt
 */
public class ContainerAbility extends AllianceSkill
{

	/* fields */
	/** 舰船对应的技能 同一舰船sid只能出现一次 */
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
