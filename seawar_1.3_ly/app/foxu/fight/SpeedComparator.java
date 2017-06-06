package foxu.fight;

import mustang.set.Comparator;

/**
 * Fighter速度比较类
 * 
 * @author ZYT
 */
public class SpeedComparator implements Comparator
{

	public int compare(Object arg0,Object arg1)
	{
		Fighter fighter0=(Fighter)arg0;
		Fighter fighter1=(Fighter)arg1;
		if(fighter0.getSpeed()<fighter1.getSpeed())
		{
			return Comparator.COMP_LESS;
		}
		else if(fighter0.getSpeed()>fighter1.getSpeed())
		{
			return Comparator.COMP_GRTR;
		}
		return Comparator.COMP_EQUAL;
	}

}