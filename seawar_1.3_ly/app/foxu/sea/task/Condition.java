package foxu.sea.task;

import mustang.io.ByteBuffer;
import mustang.util.Sample;
import foxu.sea.Player;

/**
 * �������� author:icetiger
 */
public abstract class Condition extends Sample
{

	public void bytesWrite(ByteBuffer data)
	{
		
	}
	
	/** ����������� 0δ�ı� 1�ı� 2���*/
	public abstract int checkCondition(Player player,TaskEvent event);
	public abstract void showBytesWrite(ByteBuffer data,Player p);
}
