package foxu.sea.officer.effect;

import mustang.io.ByteBuffer;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * 军官组合-羁绊影响技能
 * 
 * @author Alan
 */
public class UnitedSkill extends ContainActiveEffect
{

	/** 影响阵营 (我方or敌方) */
	int effectGroup;
	/** 影响位置 (舰船类型or相应坑位) */
	int effectField;
	/** 影响技能 */
	int effectSkillSid;
	/** 影响概率(负数则为减少。若原对象存在该技能则概率累加) */
	int effectPercent;

	@Override
	public void effectExecute(OfficerBattleHQ battleHQ)
	{
		battleHQ.addSkill(effectGroup,effectField,effectSkillSid,
			effectPercent,activyLocation);
	}

	@Override
	public void showBytesWrite(ByteBuffer data)
	{
		super.showBytesWrite(data);
//		data.writeByte(effectGroup);
//		data.writeShort(effectField);
//		data.writeShort(effectSkillSid);
//		data.writeShort(effectPercent);
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
