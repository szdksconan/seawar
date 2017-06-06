package foxu.sea.achieve;

import foxu.sea.NpcIsland;
import foxu.sea.Player;

public class WildAchievement extends Achievement
{

	/** 野地等级 （对应进度） */
	int[] wildlevel={2,10,26,46};

	public boolean canAddValue(NpcIsland beIsland,Player player)
	{
		int progress=player.getAchieveProgress(getSid());
		long cvalue=player.getAchieveValue(atrKey);
		if(!fullCollect)
		{
			if(progress>=needValue.length||cvalue>=needValue[progress]
				||beIsland.getIslandLevel()<wildlevel[progress])
				return false;
		}
		return true;
	}

}
