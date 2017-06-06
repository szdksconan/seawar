package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.alliance.alliancebattle.BattleIsland;

/***
 * 联盟争夺战岛屿封装类
 * 
 * @author lhj
 * 
 */
public class BattleIslandSave extends ObjectSave
{

	/** 联盟争夺战岛屿 **/
	BattleIsland battleIsland;

	/** 获取sid **/
	@Override
	public int getId()
	{
		return battleIsland.getSid();
	}

	/** 获取对象 **/
	@Override
	public Object getData()
	{
		return battleIsland;
	}

	/** 设置 **/
	@Override
	public void setData(Object data)
	{
		battleIsland=(BattleIsland)data;
	}

	@Override
	public ByteBuffer getByteBuffer()
	{
		if(battleIsland==null) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		battleIsland.bytesWrite(bb);
		return bb;
	}

}
