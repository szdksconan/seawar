package foxu.sea.achieve;
import foxu.sea.Player;


/**
 */
public class ArenaRankAchievement extends Achievement
{
	int[] needRank;
	public boolean canAddValue(int rank,Player player)
	{
		int progress=player.getAchieveProgress(getSid());
		//long cvalue=player.getAchieveValue(atrKey);
		if(progress>=needValue.length||rank>needRank[progress])
				return false;
		return true;
	}

}
