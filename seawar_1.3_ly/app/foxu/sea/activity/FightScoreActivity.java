package foxu.sea.activity;

import foxu.sea.InterTransltor;
import foxu.sea.Player;


/**
 * 战力排行活动
 * @author Alan
 *
 */
public class FightScoreActivity extends ServerRanklistActivity
{
	String actName="fight_score_act";
	@Override
	public long getScore(Player player)
	{
		return player.getFightScore();
	}

	@Override
	public int getRank(Player player)
	{
		return player.getFightScoreRank();
	}

	@Override
	public void setRank(Player player,int rank)
	{
		player.setFightScoreRank(rank);
	}

	@Override
	public String getActivityName(Player player)
	{
		return InterTransltor.getInstance().getTransByKey(
			player.getLocale(),actName);
	}

}
