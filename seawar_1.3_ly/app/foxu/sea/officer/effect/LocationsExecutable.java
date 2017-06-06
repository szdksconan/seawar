package foxu.sea.officer.effect;

import mustang.set.IntList;


/**
 * 军官效果影响位置执行器(仅对只激活特定位置的军官效果有实际作用)
 * @author Alan
 *
 */
public interface LocationsExecutable
{
	/** 添加军官效果影响位置
	 * @param location 军官位置
	 * @param effectLocations 影响位置容器
	 */
	public void effectLocations(int location,IntList effectLocations);
}
