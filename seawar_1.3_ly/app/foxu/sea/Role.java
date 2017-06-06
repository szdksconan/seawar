package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;


public class Role extends Sample
{
	/**���پ��� ������*/
	int playerType = 1;
	/**����ȼ� �����ȼ�*/
	protected int level = 1;
	protected String name;
	
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	
	/** ���ֽ������з����л���ö������ */
	public static Role bytesReadRole(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Role r=(Role)factory.newSample(sid);
		if(r==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Role.class.getName()
					+" bytesRead, invalid sid:"+sid);
		r.bytesRead(data);
		return r;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name=name;
	}
	
	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
//		data.writeByte(level);
//		data.writeInt(experience);
	}
	
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
//		playerType = data.readUnsignedByte();
//		level = data.readUnsignedByte();
//		experience = data.readInt();
		return this;
	}


	
	/**
	 * @return level
	 */
	public int getLevel()
	{
		return level;
	}


	
	/**
	 * @param level Ҫ���õ� level
	 */
	public void setLevel(int level)
	{
		this.level=level;
	}


	
	/**
	 * @return player_type
	 */
	public int getPlayerType()
	{
		return playerType;
	}


	
	/**
	 * @param player_type Ҫ���õ� player_type
	 */
	public void setPlayerType(int player_type)
	{
		this.playerType=player_type;
	}

} 
