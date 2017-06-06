package foxu.sea.officer.effect;

import mustang.io.ByteBuffer;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * 军官组合-羁绊影响属性
 * 
 * @author Alan
 */
public class UnitedAttribute extends ContainActiveEffect
{

	/** 影响阵营 (我方or敌方) */
	int effectGroup;
	/** 影响位置 (舰船类型or相应坑位) */
	int effectField;
	/** 影响属性 (四维or统御、航速、载重) */
	int effectAttr;
	/** 修正值或百分比 */
	boolean isFix;
	/** 影响数值(负数则为减少) */
	int value;

	@Override
	public void effectExecute(OfficerBattleHQ battleHQ)
	{
		battleHQ.addAttr(effectGroup,effectField,effectAttr,isFix,value,
			activyLocation);
	}

	@Override
	public void showBytesWrite(ByteBuffer data)
	{
		super.showBytesWrite(data);
//		data.writeByte(effectGroup);
//		data.writeShort(effectField);
//		data.writeShort(effectAttr);
//		data.writeBoolean(isFix);
//		data.writeShort(value);
//		int len=0;
//		if(activyLocation!=null)
//			len=activyLocation.size();
//		data.writeByte(len);
//		for(int i=0;i<len;i++)
//		{
//			data.writeByte(activyLocation.get(i));
//		}
	}
}
