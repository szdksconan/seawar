package foxu.cross.server;

import mustang.io.ByteBuffer;


public interface DataAction
{
	public abstract void readAction(ByteBuffer data);
}
