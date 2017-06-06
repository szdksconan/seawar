package foxu.fight;

import mustang.io.ByteBuffer;

/**
 * 改变角色属性效果类
 * 
 * @author ZYT
 */
public class ChangeAttributeEffect extends Effect
{

	/* static fields */
	/** 数值类型常量:PRECENT=1百分比,ABSOLUTE_VALUE=2固定值 */
	public static final int PRECENT=1,ABSOLUTE_VALUE=2;

	/* fields */
	/** 提升指定负面技能的抗性 */
	int abilityCid;
	/** 效果作用的数据 */
	float effectValue;
	/** 效果起作用的属性 */
	int changeAttr;
	/** 数值类型:百分比,绝对值 */
	int valueType=1;

	/* properties */
	/** 获得效果数据类型 */
	public int getDataType()
	{
		return changeAttr;
	}
	/** 设置效果作用数据 */
	public void setValue(float effectValue)
	{
		this.effectValue=effectValue;
	}
	/** 获得效果数据 */
	public float getValue(Fighter fighter)
	{
		return effectValue;
	}
	/** 获得数据类型 */
	public int getValueType()
	{
		return valueType;
	}

	/* methods */
	/**
	 * 检查是否是这个效果能起作用技能
	 * 
	 * @param type 属性类型
	 * @param cid 技能的cid
	 * @return true表示符合条件
	 */
	public boolean checkType(int type,int cid)
	{
		if(abilityCid!=0) return cid==abilityCid&&(type==changeAttr);
		return changeAttr==type;
	}
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeFloat(effectValue);
	}
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		effectValue=data.readFloat();
		return this;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[changeAttr="+changeAttr+", effectValue="
			+effectValue+"] ";
	}
}