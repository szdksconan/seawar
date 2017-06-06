package foxu.sea;

import mustang.io.ByteBuffer;

/** ��ұ��浺�������б� */
public class IslandLocationSave
{
	/** �ղ�����ENEMY=1�ж� FRIENDLY=2�Ѻ� COLLECTION=3�ղ� */
	public static final int ENEMY=1,FRIENDLY=2,COLLECTION=4;
	int index;
	String name;
	int type;
	
	/** ���ֽڻ����з����л��õ�һ������ */
	public Object bytesRead(ByteBuffer data)
	{
		index = data.readInt();
		name = data.readUTF();
		type=data.readUnsignedByte();
		return this;
	}

	/** ������������л����ֽڻ����� */
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
	 * @param index Ҫ���õ� index
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
	 * @param name Ҫ���õ� name
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
	 * @param type Ҫ���õ� type
	 */
	public void setType(int type)
	{
		this.type=type;
	}
}
