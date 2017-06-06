package foxu.sea.alliance.alliancebattle.compare;

import mustang.set.Comparator;
import foxu.sea.alliance.alliancebattle.DonateRank;

/**¾èÏ×ÅÅÐÐ±È½ÏÆ÷**/
public class DayRankCompare   implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		DonateRank cp1=(DonateRank)o1;
		DonateRank cp2=(DonateRank)o2;
		if(cp1.getDayValue()<cp2.getDayValue())
			return Comparator.COMP_GRTR;
		if(cp1.getDayValue()>cp2.getDayValue())
			return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}

}
