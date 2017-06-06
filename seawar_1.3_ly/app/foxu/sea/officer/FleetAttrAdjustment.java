/**
 * 
 */
package foxu.sea.officer;

import mustang.set.IntKeyHashMap;

/**
 * 舰队(坑位)属性修正值集合
 * 参照{@linkplain foxu.sea.AttrAdjustment AttrAdjustment}
 * @author Alan
 */
public class FleetAttrAdjustment
{

	/* fields */
	/** 修正值容器,k为属性的type,v为修正值对象 */
	IntKeyHashMap dataMap=new IntKeyHashMap();

	/* methods */
	/** 清除数据 */
	public void clear()
	{
		dataMap.clear();
	}
	/** 获得指定的修改值 */
	public AdjustmentData getAdjustmentValue(int k)
	{
		return (AdjustmentData)dataMap.get(k);
	}
	/**
	 * 加入一个修正值,固定量
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