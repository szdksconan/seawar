package foxu.dcaccess.datasave;

import foxu.sea.alliance.alliancefight.AllianceFight;
import mustang.io.ByteBuffer;


/**
 * @author yw
 *
 */
public class AllianceFightSave extends ObjectSave
{
	/** √À’Ω */
	AllianceFight allianceFight;

	@Override
	public int getId()
	{
		return allianceFight.getAllianceID();
	}

	@Override
	public Object getData()
	{
		return allianceFight;
	}

	@Override
	public void setData(Object data)
	{
		allianceFight=(AllianceFight)data;
	}

	@Override
	public ByteBuffer getByteBuffer()
	{
		if(allianceFight==null) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		allianceFight.bytesWrite(bb);
		return bb;
	}

}
