package foxu.cross.war;

import mustang.set.Comparator;


/**
 * 时间比较
 * @author Alan
 *
 */
public class CrossWarPTComparator implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		CrossWarPlayer cp1=(CrossWarPlayer)o1;
		CrossWarPlayer cp2=(CrossWarPlayer)o2;
		if(cp1.getJiontime()>cp2.getJiontime())
			return Comparator.COMP_GRTR;
		if(cp1.getJiontime()<cp2.getJiontime())
			return Comparator.COMP_LESS;
		if(cp1.getSeverid()<cp2.getSeverid())
			return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}

}
