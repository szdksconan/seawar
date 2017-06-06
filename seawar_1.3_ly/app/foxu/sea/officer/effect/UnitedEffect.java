package foxu.sea.officer.effect;

import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.util.Sample;

/**
 * ���ٹ���Ч��
 * 
 * @author Alan
 */
public abstract class UnitedEffect extends Sample implements
	EffectExecutable
{

	// ��̬����
	/** ���������λ */
	IntList activyLocation;

	/** ������ľ��� */
	int[] officers;
	/** ����Ч���ľ���(����Ҫ�ض�λ�ü����Ч���п���Ϊ��) */
	int[] activeOfficers;
	/** Ч������λ��ѡ����(����Ҫ�ض�λ�ü����Ч���п���Ϊ��) */
	LocationsExecutable locationSelector;
	/** ���л�����(Ӱ��ǰ̨�Ľ�������) */
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
