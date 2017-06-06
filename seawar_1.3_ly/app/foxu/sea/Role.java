package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;


public class Role extends Sample
{
	/**军官军衔 船类型*/
	int playerType = 1;
	/**岛屿等级 船舰等级*/
	protected int level = 1;
	protected String name;
	
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	
	/** 从字节数组中反序列化获得对象的域 */
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
	
	/** 将对象的域序列化到字节缓存中 */
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
	 * @param level 要设置的 level
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
	 * @param player_type 要设置的 player_type
	 */
	public void setPlayerType(int player_type)
	{
		this.playerType=player_type;
	}

} 
