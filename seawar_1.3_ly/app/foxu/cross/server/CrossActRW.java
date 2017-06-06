package foxu.cross.server;

import mustang.io.ByteBuffer;


/**
 * 跨服活动其他数据 读写接口
 * @author yw
 *
 */
public interface CrossActRW
{
	/** 写 */
	public abstract void  writeData(ByteBuffer data);
	/** 读 */
	public abstract void readData(ByteBuffer data);

}
