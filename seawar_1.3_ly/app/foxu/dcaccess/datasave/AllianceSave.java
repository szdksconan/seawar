package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.alliance.Alliance;
public class AllianceSave extends ObjectSave
{

	/** 联盟 */
	Alliance alliance;

	@Override
	public ByteBuffer getByteBuffer()
	{
		if(alliance==null) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		alliance.bytesWrite(bb);
		return bb;
	}

	@Override
	public Alliance getData()
	{
		// TODO 自动生成方法存根
		return alliance;
	}

	@Override
	public int getId()
	{
		// TODO 自动生成方法存根
		return alliance.getId();
	}

	@Override
	public void setData(Object data)
	{
		// TODO 自动生成方法存根
		alliance=(Alliance)data;
	}
}
