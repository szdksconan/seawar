package foxu.cross.warclient;

import mustang.set.Comparator;


/**
 * Ñº×¢Á¿ ÅÅÐòÆ÷
 * @author yw
 *
 */
public class ClientWarPBComparator implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		ClientWarPlayer cp1=(ClientWarPlayer)o1;
		ClientWarPlayer cp2=(ClientWarPlayer)o2;
		if(cp1.getBet()>cp2.getBet()) return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}

}
