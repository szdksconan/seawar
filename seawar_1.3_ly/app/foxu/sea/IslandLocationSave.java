package foxu.sea;

import mustang.io.ByteBuffer;

/** 玩家保存岛屿坐标列表 */
public class IslandLocationSave
{
	/** 收藏类型ENEMY=1敌对 FRIENDLY=2友好 COLLECTION=3收藏 */
	public static final int ENEMY=1,FRIENDLY=2,COLLECTION=4;
	int index;
	String name;
	int type;
	
	/** 从字节缓存中反序列化得到一个对象 */
	public Object bytesRead(ByteBuffer data)
	{
		index = data.readInt();
		name = data.readUTF();
		type=data.readUnsignedByte();
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
         data.writeInt(index);
         data.writeUTF(name);
         data.writeByte(type);
	}

	/**
	 * @return index
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @param index 要设置的 index
	 */
	public void setIndex(int index)
	{
		this.index=index;
	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name 要设置的 name
	 */
	public void setName(String name)
	{
		this.name=name;
	}

	/**
	 * @return type
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type 要设置的 type
	 */
	public void setType(int type)
	{
		this.type=type;
	}
}
