package foxu.sea;

import foxu.sea.fight.FleetGroup;

/** ����boss */
public class AllianceBossNpcIsland extends NpcIsland
{
	/** ��ȡ�������������� */
	int allianceExp;
	/** ���˵ȼ����� */
	int limitLevel;
	
	/**��ȡbossս��Ⱥ*/
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
