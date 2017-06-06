package foxu.sea.officer.effect;

import mustang.io.ByteBuffer;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerBattleHQ;


/**
 * 军官效果
 * @author Alan
 *
 */
public interface EffectExecutable
{
	/** 效果是否可用 */
	public boolean isEffectAvailable(Officer[] officers);
	/** 执行军官效果 */
	public void effectExecute(OfficerBattleHQ battleHQ);
	/** 军官效果序列化 */
	public void showBytesWrite(ByteBuffer data);
}
