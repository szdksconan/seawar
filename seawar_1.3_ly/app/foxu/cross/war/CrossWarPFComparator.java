package foxu.cross.war;

import mustang.set.Comparator;

/**
 * 战力比较
 * @author yw
 *
 */
public class CrossWarPFComparator implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		CrossWarPlayer cp1=(CrossWarPlayer)o1;
		CrossWarPlayer cp2=(CrossWarPlayer)o2;
		if(cp1.getFightscore()<cp2.getFightscore())
			return Comparator.COMP_GRTR;
		if(cp1.getFightscore()>cp2.getFightscore())
			return Comparator.COMP_LESS;
		if(cp1.getJiontime()<cp2.getJiontime())
			return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}

}
