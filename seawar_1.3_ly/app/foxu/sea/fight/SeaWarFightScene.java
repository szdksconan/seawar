/**
 * 
 */
package foxu.sea.fight;

import mustang.set.IntKeyHashMap;
import foxu.fight.FightScene;
import foxu.fight.Fighter;


/**
 * ��սfighter.Ĭ��0��Ϊ������,1��Ϊ���ط�
 * 
 * @author rockzyt
 */
public class SeaWarFightScene extends FightScene
{

	public int checkOver()
	{
		if(getCurrentRound()>=getMaxRound()) return 1;
		IntKeyHashMap[] team=getFighterContainer().getAllTeam();
		if(team[0].size()<=0) return 1;//0���޳�Ա,1��Ϊʤ������
		if(team[1].size()<=0) return 0;//ͬ��ȡ��
		return Integer.MAX_VALUE;
	}
	public void fighterReady(Fighter f)
	{
		if(f.isDead()) return;
		FleetFighter ship=(FleetFighter)f;
		ship.getReady();
	}
}