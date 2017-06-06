package foxu.sea.activity;

import foxu.sea.InterTransltor;
import foxu.sea.Player;


public class HonorActivity extends ServerRanklistActivity
{
	String actName="honor_score_act";
	@Override
	public long getScore(Player player)
	{
		return player.getHonorScore();
	}

	@Override
	public int getRank(Player player)
	{
		return player.getHonorScoreRank();
	}

	@Override
	public void setRank(Player player,int rank)
	{
		player.setHonorScoreRank(rank);
	}

	@Override
	public String getActivityName(Player player)
	{
		return InterTransltor.getInstance().getTransByKey(
			player.getLocale(),actName);
	}
}
