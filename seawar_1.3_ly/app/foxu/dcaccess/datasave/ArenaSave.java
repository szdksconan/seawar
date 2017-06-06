package foxu.dcaccess.datasave;

import foxu.sea.arena.Gladiator;
import mustang.io.ByteBuffer;


public class ArenaSave extends ObjectSave
{
	Gladiator gladator;
	
	public int getId()
	{
		return gladator.getPlayerId();
	}

	public Object getData()
	{
		return gladator;
	}

	public void setData(Object data)
	{
		gladator=(Gladiator)data;
	}

	public ByteBuffer getByteBuffer()
	{
		if(gladator==null)
			return null;
		return null;
	}

}
