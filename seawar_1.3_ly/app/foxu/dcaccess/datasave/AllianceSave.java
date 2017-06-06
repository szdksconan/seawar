package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.alliance.Alliance;
public class AllianceSave extends ObjectSave
{

	/** ���� */
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
		// TODO �Զ����ɷ������
		return alliance;
	}

	@Override
	public int getId()
	{
		// TODO �Զ����ɷ������
		return alliance.getId();
	}

	@Override
	public void setData(Object data)
	{
		// TODO �Զ����ɷ������
		alliance=(Alliance)data;
	}
}
