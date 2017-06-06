package foxu.sea.achieve;

import mustang.set.Comparator;


public class AchieveComparator implements Comparator
{

	@Override
	public int compare(Object arg0,Object arg1)
	{
		Achievement achieve0=(Achievement)arg0;
		Achievement achieve1=(Achievement)arg1;
		if(achieve0.sort>=achieve1.sort)return Comparator.COMP_GRTR;
		return Comparator.COMP_LESS;
	}

}
