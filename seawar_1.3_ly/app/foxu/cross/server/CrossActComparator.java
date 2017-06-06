package foxu.cross.server;

import mustang.set.Comparator;


/**
 * 活动时间排序器
 * @author yw
 *
 */
public class CrossActComparator implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		CrossAct a1=(CrossAct)o1;
		CrossAct a2=(CrossAct)o2;
		if(a1.getStime()<a2.getStime()) return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}

}
