package foxu.sea.activity;

import foxu.sea.InterTransltor;
import foxu.sea.Player;


/**
 * 关卡星数活动
 * @author Alan
 *
 */
public class PointStarActivity extends ServerRanklistActivity
{
	String actName="point_star_act";
	@Override
	public long getScore(Player player)
	{	
		return player.getPlunderResource();
	}

	@Override
	public int getRank(Player player)
	{
		return player.getPlunderRank();
	}

	@Override
	public void setRank(Player player,int rank)
	{
		player.setPlunderRank(rank);
	}

	@Override
	public String getActivityName(Player player)
	{
		return InterTransltor.getInstance().getTransByKey(
			player.getLocale(),actName);
	}
}
