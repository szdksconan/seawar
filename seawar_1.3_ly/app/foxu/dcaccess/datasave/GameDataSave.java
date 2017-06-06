package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.GameData;

/** 游戏运营数据 */
public class GameDataSave extends ObjectSave
{

	GameData gameData;
	/**强制更新数据时间*/
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
	
	/**获得当前的最高在线*/
	public int getMaxOnLine()
	{
		return gameData.getMaxOnline();
	}
	
	/**设置最高在线*/
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
	 * @param forceDbTime 要设置的 forceDbTime
	 */
	public void setForceDbTime(int forceDbTime)
	{
		this.forceDbTime=forceDbTime;
	}

}
