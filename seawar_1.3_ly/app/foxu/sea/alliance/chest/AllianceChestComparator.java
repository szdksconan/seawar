package foxu.sea.alliance.chest;

import mustang.set.Comparator;

public class AllianceChestComparator implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_LESS;
		if(o2==null) return Comparator.COMP_GRTR;
		AllianceChestManager.RankInfo a1=(AllianceChestManager.RankInfo)o1;
		AllianceChestManager.RankInfo a2=(AllianceChestManager.RankInfo)o2;
		if(a1.getLuckyPoint()>a2.getLuckyPoint())
			return Comparator.COMP_GRTR;
		if(a1.getLuckyPoint()<a2.getLuckyPoint())
			return Comparator.COMP_LESS;
		if(a1.getLuckyCreateAt()<a2.getLuckyCreateAt())
			return Comparator.COMP_GRTR;
		if(a1.getLuckyCreateAt()>a2.getLuckyCreateAt())
			return Comparator.COMP_LESS;
		return Comparator.COMP_EQUAL;
	}

}
