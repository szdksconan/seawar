package foxu.sea.comparator;

import foxu.sea.event.FightEvent;
import mustang.set.Comparator;

public class FightEventComparator implements Comparator
{

	/* static fields */
	/** 唯一的实例 */
	private static final FightEventComparator fightEventComparator=new FightEventComparator();

	/* static methods */
	/** 获得当前的实例 */
	public static FightEventComparator getInstance()
	{
		return fightEventComparator;
	}

	public int compare(Object arg0,Object arg1)
	{
		if(!(arg0 instanceof FightEvent)||!(arg1 instanceof FightEvent))
			return COMP_EQUAL;
		FightEvent event0=(FightEvent)arg0;
		FightEvent event1=(FightEvent)arg1;
		int time0=event0.getCreatAt()+event0.getNeedTime();
		int time1=event1.getCreatAt()+event1.getNeedTime();
		if(time0<time1) return COMP_LESS;
		if(time0>=time1) return COMP_GRTR;
		return COMP_EQUAL;
	}

}
