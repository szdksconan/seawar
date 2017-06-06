/**
 * 
 */
package foxu.sea.officer;

import mustang.set.IntKeyHashMap;

/**
 * ����(��λ)��������ֵ����
 * ����{@linkplain foxu.sea.AttrAdjustment AttrAdjustment}
 * @author Alan
 */
public class FleetAttrAdjustment
{

	/* fields */
	/** ����ֵ����,kΪ���Ե�type,vΪ����ֵ���� */
	IntKeyHashMap dataMap=new IntKeyHashMap();

	/* methods */
	/** ������� */
	public void clear()
	{
		dataMap.clear();
	}
	/** ���ָ�����޸�ֵ */
	public AdjustmentData getAdjustmentValue(int k)
	{
		return (AdjustmentData)dataMap.get(k);
	}
	/**
	 * ����һ������ֵ,�̶���
	 * 
	 * @param k
	 * @param v
	 */
	public void add(int k,float v,boolean isFix)
	{
		add(k,v,isFix,dataMap);
	}

	private void add(int k,float v,boolean isFix,IntKeyHashMap map)
	{
		AdjustmentData data=(AdjustmentData)map.get(k);
		if(data==null)
		{
			data=new AdjustmentData();
			map.put(k,data);
		}
		if(isFix)
		{
			data.fix+=v;
		}
		else
		{
			data.percent+=v;
		}
	}

	/* inner class */
	public class AdjustmentData
	{

		/* fields */
		public float fix;
		public float percent;
	}
}