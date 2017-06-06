package foxu.cross.warclient;

import foxu.cross.war.CrossWarRoundSave;
import mustang.set.Comparator;

/**
 * Õ½±¨Î¨Ò»±àºÅ
 * @author yw
 *
 */
public class ClientWarRIdComparator implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_GRTR;
		if(o2==null) return Comparator.COMP_LESS;
		CrossWarRoundSave cp1=(CrossWarRoundSave)o1;
		CrossWarRoundSave cp2=(CrossWarRoundSave)o2;
		if(cp1.getId()<cp2.getId()) return Comparator.COMP_LESS;
		return Comparator.COMP_GRTR;
	}

}
