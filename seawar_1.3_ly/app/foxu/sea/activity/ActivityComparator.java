package foxu.sea.activity;

import mustang.set.Comparator;


public class ActivityComparator implements Comparator
{

	@Override
	public int compare(Object o1,Object o2)
	{
		Activity act1=(Activity)o1;
		Activity act2=(Activity)o2;
		if(act1.getStartTime()>act2.getStartTime())
		{
			return Comparator.COMP_LESS;
		}
		return Comparator.COMP_GRTR;
	}

}
