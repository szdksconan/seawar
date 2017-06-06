package foxu.sea.alliance.alliancebattle.compare;

import foxu.sea.alliance.alliancebattle.DonateRank;
import mustang.set.Comparator;


/***
 * 
 * ÷‹≈≈––
 * @author lhj
 *
 */
public class WeekRankCompare implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		DonateRank cp1=(DonateRank)o1;
		DonateRank cp2=(DonateRank)o2;
		if(cp1.getWeekValue()<cp2.getWeekValue())
			return Comparator.COMP_GRTR;
		if(cp1.getWeekValue()>cp2.getWeekValue())
			return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}
}
