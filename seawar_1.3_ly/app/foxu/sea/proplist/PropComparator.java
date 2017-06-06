package foxu.sea.proplist;

import mustang.set.Comparator;


/**
 * ��Ʒ����������PropList�����Ʒ������Ʒ����IDֵ�Ĵ�С��С�����������ע��ͬ������
 */
public class PropComparator implements Comparator
{

	/* static fields */
	/** Ψһ��ʵ�� */
	private static final PropComparator propComparator=new PropComparator();

	/* static methods */
	/** ��õ�ǰ��ʵ�� */
	public static PropComparator getInstance()
	{
		return propComparator;
	}
	/** �ȽϷ��� */
	public int compare(Object arg0,Object arg1)
	{
		if(!(arg0 instanceof Prop)||!(arg1 instanceof Prop))
			return COMP_EQUAL;
		Prop prop0=(Prop)arg0;
		Prop prop1=(Prop)arg1;
		if(prop0.getSortId()==0&&prop1.getSortId()!=0)// ����sortIdΪ0�����⴦�����ڰ����б�����
			return COMP_GRTR;
		if(prop0.getSortId()!=0&&prop1.getSortId()==0)// ����sortIdΪ0�����⴦�����ڰ����б�����
			return COMP_LESS;
		if(prop0.getSortId()>prop1.getSortId()) return COMP_GRTR;
		if(prop0.getSortId()<prop1.getSortId()) return COMP_LESS;// sortIdС�ķ���COMP_LESS,���ڿ�ǰ��λ��
		// sortId��ͬ����Ϊͬһ��SID����Ʒ
		if(prop0 instanceof NormalProp&&prop1 instanceof NormalProp)// ����ǿɵ�����Ʒ�������ж�����
		{
			if(((NormalProp)prop0).getCount()>((NormalProp)prop1).getCount())
				return COMP_LESS;// ������ķ���COMP_LESS,���ڿ�ǰ��λ��
			if(((NormalProp)prop0).getCount()<((NormalProp)prop1).getCount())
				return COMP_GRTR;
		}
		return COMP_EQUAL;
	}
}
