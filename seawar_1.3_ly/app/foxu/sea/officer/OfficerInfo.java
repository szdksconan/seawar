package foxu.sea.officer;

import mustang.io.ByteBuffer;

public class OfficerInfo
{
	public int id;
	public int sid;
	public int militaryRank;
	public int level;
	public int exp;

	public OfficerInfo()
	{
	}

	public OfficerInfo(Officer officer)
	{
		id=officer.getId();
		sid=officer.getSid();
		militaryRank=officer.getMilitaryRank();
		level=officer.getLevel();
		exp=officer.getExp();
	}
	public void showBytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(sid);
		data.writeInt(militaryRank);
		data.writeInt(level);
		data.writeInt(exp);
	}

	public void showBytesRead(ByteBuffer data)
	{
		id=data.readInt();
		sid=data.readInt();
		militaryRank=data.readInt();
		level=data.readInt();
		exp=data.readInt();
	}

	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeShort(sid);
		data.writeByte(militaryRank);
		data.writeByte(level);
		data.writeInt(exp);
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id=id;
	}
}
