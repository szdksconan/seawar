package foxu.sea.alliance.alliancefight;

import mustang.set.Comparator;

/**
 * ÅÅÃû±È½ÏÆ÷
 * @author yw
 *
 */
public class RankComparator implements Comparator
{

	@Override
	public int compare(Object arg0,Object arg1)
	{
		RankData rank1=(RankData)arg0;
		RankData rank2=(RankData)arg1;
		if(rank1==null)
		{
			return Comparator.COMP_LESS;
		}
		if(rank2==null)
		{
			return Comparator.COMP_GRTR;
		}
		if(rank1.pri>rank2.pri)
		{
			return Comparator.COMP_GRTR;
		}
		else if(rank1.pri<rank2.pri)
		{
			return Comparator.COMP_LESS;
		}
		else
		{
			if(rank1.getCount()>rank2.getCount())
			{
				return Comparator.COMP_GRTR;
			}else if(rank1.getCount()<rank2.getCount())
			{
				return Comparator.COMP_LESS;
			}else
			{
				return Comparator.COMP_EQUAL;
			}

		}
	}

}
