package foxu.sea.alliance.alliancebattle.compare;

import foxu.sea.alliance.alliancebattle.PlayerAllianceFight;
import mustang.set.Comparator;

/***
 * 
 * @author lhj 战斗力比较器
 * 
 */
public class FightScoreCompare implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		PlayerAllianceFight cp1=(PlayerAllianceFight)o1;
		PlayerAllianceFight cp2=(PlayerAllianceFight)o2;
		if(cp1.getFightScore()<cp2.getFightScore())
			return Comparator.COMP_GRTR;
		if(cp1.getFightScore()>cp2.getFightScore())
			return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}
}
