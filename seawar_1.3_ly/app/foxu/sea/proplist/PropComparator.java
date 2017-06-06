package foxu.sea.proplist;

import mustang.set.Comparator;


/**
 * 物品排序器，对PropList里的物品根据物品排序ID值的大小由小到大进行排序，注意同步问题
 */
public class PropComparator implements Comparator
{

	/* static fields */
	/** 唯一的实例 */
	private static final PropComparator propComparator=new PropComparator();

	/* static methods */
	/** 获得当前的实例 */
	public static PropComparator getInstance()
	{
		return propComparator;
	}
	/** 比较方法 */
	public int compare(Object arg0,Object arg1)
	{
		if(!(arg0 instanceof Prop)||!(arg1 instanceof Prop))
			return COMP_EQUAL;
		Prop prop0=(Prop)arg0;
		Prop prop1=(Prop)arg1;
		if(prop0.getSortId()==0&&prop1.getSortId()!=0)// 对于sortId为0的特殊处理，放在包裹列表的最后
			return COMP_GRTR;
		if(prop0.getSortId()!=0&&prop1.getSortId()==0)// 对于sortId为0的特殊处理，放在包裹列表的最后
			return COMP_LESS;
		if(prop0.getSortId()>prop1.getSortId()) return COMP_GRTR;
		if(prop0.getSortId()<prop1.getSortId()) return COMP_LESS;// sortId小的返回COMP_LESS,排在靠前的位置
		// sortId相同，即为同一种SID的物品
		if(prop0 instanceof NormalProp&&prop1 instanceof NormalProp)// 如果是可叠加物品，则再判断数量
		{
			if(((NormalProp)prop0).getCount()>((NormalProp)prop1).getCount())
				return COMP_LESS;// 数量多的返回COMP_LESS,排在靠前的位置
			if(((NormalProp)prop0).getCount()<((NormalProp)prop1).getCount())
				return COMP_GRTR;
		}
		return COMP_EQUAL;
	}
}
