package foxu.cross.server;

import mustang.io.ByteBuffer;


/**
 * ������������ ��д�ӿ�
 * @author yw
 *
 */
public interface CrossActRW
{
	/** д */
	public abstract void  writeData(ByteBuffer data);
	/** �� */
	public abstract void readData(ByteBuffer data);

}
