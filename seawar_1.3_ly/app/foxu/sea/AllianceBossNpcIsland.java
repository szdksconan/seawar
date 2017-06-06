package foxu.sea;

import foxu.sea.fight.FleetGroup;

/** 联盟boss */
public class AllianceBossNpcIsland extends NpcIsland
{
	/** 获取的联盟升级点数 */
	int allianceExp;
	/** 联盟等级限制 */
	int limitLevel;
	
	/**获取boss战斗群*/
	public FleetGroup getFleetGroup()
	{
		if(fleetGroup==null)
			createFleetGroup();
		return fleetGroup;
	}
	

	public int getAllianceExp()
	{
		return allianceExp;
	}

	public void setAllianceExp(int allianceExp)
	{
		this.allianceExp=allianceExp;
	}

	public int getLimitLevel()
	{
		return limitLevel;
	}

	public void setLimitLevel(int limitLevel)
	{
		this.limitLevel=limitLevel;
	}
	
}
