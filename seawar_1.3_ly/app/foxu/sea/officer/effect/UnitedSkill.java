package foxu.sea.officer.effect;

import mustang.io.ByteBuffer;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * �������-�Ӱ�켼��
 * 
 * @author Alan
 */
public class UnitedSkill extends ContainActiveEffect
{

	/** Ӱ����Ӫ (�ҷ�or�з�) */
	int effectGroup;
	/** Ӱ��λ�� (��������or��Ӧ��λ) */
	int effectField;
	/** Ӱ�켼�� */
	int effectSkillSid;
	/** Ӱ�����(������Ϊ���١���ԭ������ڸü���������ۼ�) */
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
