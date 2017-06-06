package foxu.cross.war;

import mustang.set.Comparator;


/**
 * ±àºÅ±È½ÏÆ÷
 * @author yw
 *
 */
public class CrossWarPNComparator implements Comparator
{
	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		CrossWarPlayer cp1=(CrossWarPlayer)o1;
		CrossWarPlayer cp2=(CrossWarPlayer)o2;
		if(cp1.getNum()<cp2.getNum()) return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}

}
