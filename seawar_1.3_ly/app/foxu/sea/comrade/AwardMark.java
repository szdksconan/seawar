package foxu.sea.comrade;

import mustang.io.ByteBuffer;


/**
 * ս��ϵͳ �콱��¼
 * @author yw
 *
 */
public class AwardMark
{
	/** ����sid */
	int sid;
	/** ������ɴ��� */
	int complete;
	/** ������ȡ���� */
	int got;
	
	/** ���л�д */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeShort(complete);
		data.writeShort(got);
	}
	/** ���л��� */
	public void bytesRead(ByteBuffer data)
	{
		sid=data.readUnsignedShort();
		complete=data.readUnsignedShort();
		got=data.readUnsignedShort();
	}
	public int getComplete()
	{
		return complete;
	}
	
	public void setComplete(int complete)
	{
		this.complete=complete;
	}
	
	public int getGot()
	{
		return got;
	}
	
	public void setGot(int got)
	{
		this.got=got;
	}
	
	public int getSid()
	{
		return sid;
	}
	
	public void setSid(int sid)
	{
		this.sid=sid;
	}
	
}
