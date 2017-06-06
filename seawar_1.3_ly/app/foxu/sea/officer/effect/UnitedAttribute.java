package foxu.sea.officer.effect;

import mustang.io.ByteBuffer;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * �������-�Ӱ������
 * 
 * @author Alan
 */
public class UnitedAttribute extends ContainActiveEffect
{

	/** Ӱ����Ӫ (�ҷ�or�з�) */
	int effectGroup;
	/** Ӱ��λ�� (��������or��Ӧ��λ) */
	int effectField;
	/** Ӱ������ (��άorͳ�������١�����) */
	int effectAttr;
	/** ����ֵ��ٷֱ� */
	boolean isFix;
	/** Ӱ����ֵ(������Ϊ����) */
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
