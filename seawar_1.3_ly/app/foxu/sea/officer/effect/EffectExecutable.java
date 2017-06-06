package foxu.sea.officer.effect;

import mustang.io.ByteBuffer;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerBattleHQ;


/**
 * ����Ч��
 * @author Alan
 *
 */
public interface EffectExecutable
{
	/** Ч���Ƿ���� */
	public boolean isEffectAvailable(Officer[] officers);
	/** ִ�о���Ч�� */
	public void effectExecute(OfficerBattleHQ battleHQ);
	/** ����Ч�����л� */
	public void showBytesWrite(ByteBuffer data);
}
