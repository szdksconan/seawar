package foxu.sea.comparator;

import foxu.sea.worldboss.BossHurt;
import mustang.set.Comparator;

public class AllianceBossHurtComparator implements Comparator
{

	/* static fields */
	/** 唯一的实例 */
	private static final AllianceBossHurtComparator allianceBossHurtComparator=new AllianceBossHurtComparator();

	/* static methods */
	/** 获得当前的实例 */
	public static AllianceBossHurtComparator getInstance()
	{
		return allianceBossHurtComparator;
	}

	public int compare(Object o1,Object o2)
	{
		if(!(o1 instanceof BossHurt)
			||!(o2 instanceof BossHurt)) return COMP_EQUAL;
		BossHurt event0=(BossHurt)o1;
		BossHurt event1=(BossHurt)o2;
		if(event0.getHurtNum()<event1.getHurtNum()) return COMP_GRTR;
		if(event0.getHurtNum()>=event1.getHurtNum()) return COMP_LESS;
		return COMP_EQUAL;
	}
}
