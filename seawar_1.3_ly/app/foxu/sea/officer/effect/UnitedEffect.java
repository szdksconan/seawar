package foxu.sea.officer.effect;

import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.util.Sample;

/**
 * 军官关联效果
 * 
 * @author Alan
 */
public abstract class UnitedEffect extends Sample implements
	EffectExecutable
{

	// 动态属性
	/** 关联激活坑位 */
	IntList activyLocation;

	/** 相关联的军官 */
	int[] officers;
	/** 受用效果的军官(不需要特定位置激活的效果中可能为空) */
	int[] activeOfficers;
	/** 效果激活位置选择器(不需要特定位置激活的效果中可能为空) */
	LocationsExecutable locationSelector;
	/** 序列化类型(影响前台的解析调度) */
	int showType;
	
	public Object copy(Object obj)
	{
		UnitedEffect ue=(UnitedEffect)obj;
		ue.activyLocation=new IntList();
		return obj;
	}
	
	@Override
	public void showBytesWrite(ByteBuffer data)
	{
//		data.writeByte(showType);
		data.writeShort(getSid());
	}
}
