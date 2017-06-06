package foxu.dcaccess.datasave;

import foxu.sea.alliance.alliancefight.AllianceFightEvent;
import mustang.io.ByteBuffer;


public class AFightEventSave extends ObjectSave
{

	/** √À’Ω’Ω ¬ */
	AllianceFightEvent aFightEvent;
	@Override
	public int getId()
	{
		return aFightEvent.getUid();
	}

	@Override
	public Object getData()
	{
		return aFightEvent;
	}

	@Override
	public void setData(Object data)
	{
		aFightEvent=(AllianceFightEvent)data;
		
	}

	@Override
	public ByteBuffer getByteBuffer()
	{
		if(aFightEvent==null) return null;
		 ByteBuffer data=new ByteBuffer();
		 aFightEvent.bytesWrite(data);
		 return data;
	}

}
