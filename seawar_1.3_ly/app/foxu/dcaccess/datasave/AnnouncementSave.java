package foxu.dcaccess.datasave;

import foxu.sea.announcement.Announcement;
import mustang.io.ByteBuffer;

public class AnnouncementSave extends ObjectSave
{

	Announcement announce;

	@Override
	public int getId()
	{
		return announce.getId();
	}

	@Override
	public Object getData()
	{
		return announce;
	}

	@Override
	public void setData(Object data)
	{
		announce=(Announcement)data;
	}

	@Override
	public ByteBuffer getByteBuffer()
	{
		if(announce==null) return null;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		announce.bytesWrite(data);
		return data;
	}

}
