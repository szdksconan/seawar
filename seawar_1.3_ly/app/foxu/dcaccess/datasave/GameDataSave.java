package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.GameData;

/** ��Ϸ��Ӫ���� */
public class GameDataSave extends ObjectSave
{

	GameData gameData;
	/**ǿ�Ƹ�������ʱ��*/
	int forceDbTime;

	public ByteBuffer getByteBuffer()
	{
		if(gameData==null) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
//		gameData.bytesWrite(bb);
		return bb;
	}

	public GameData getData()
	{
		return gameData;
	}

	public int getId()
	{
		return gameData.getId();
	}

	public void setData(Object data)
	{
		gameData=(GameData)data;
	}
	
	/**��õ�ǰ���������*/
	public int getMaxOnLine()
	{
		return gameData.getMaxOnline();
	}
	
	/**�����������*/
	public void setMaxOnLine(int maxOnline)
	{
		gameData.setMaxOnline(maxOnline);
	}

	
	/**
	 * @return forceDbTime
	 */
	public int getForceDbTime()
	{
		return forceDbTime;
	}

	
	/**
	 * @param forceDbTime Ҫ���õ� forceDbTime
	 */
	public void setForceDbTime(int forceDbTime)
	{
		this.forceDbTime=forceDbTime;
	}

}
