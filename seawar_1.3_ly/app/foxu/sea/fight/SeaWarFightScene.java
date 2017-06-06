/**
 * 
 */
package foxu.sea.fight;

import mustang.set.IntKeyHashMap;
import foxu.fight.FightScene;
import foxu.fight.Fighter;


/**
 * 海战fighter.默认0队为进攻方,1队为防守方
 * 
 * @author rockzyt
 */
public class SeaWarFightScene extends FightScene
{

	public int checkOver()
	{
		if(getCurrentRound()>=getMaxRound()) return 1;
		IntKeyHashMap[] team=getFighterContainer().getAllTeam();
		if(team[0].size()<=0) return 1;//0队无成员,1队为胜利队伍
		if(team[1].size()<=0) return 0;//同上取反
		return Integer.MAX_VALUE;
	}
	public void fighterReady(Fighter f)
	{
		if(f.isDead()) return;
		FleetFighter ship=(FleetFighter)f;
		ship.getReady();
	}
}