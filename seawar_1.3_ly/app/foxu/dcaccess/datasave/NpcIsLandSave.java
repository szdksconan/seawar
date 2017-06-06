package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.NpcIsland;

/**NPC岛屿存储*/
public class NpcIsLandSave extends ObjectSave
{
    NpcIsland island;
	
	@Override
	public ByteBuffer getByteBuffer()
	{
		if(island==null)return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		island.bytesWrite(bb);
		return bb;
	}

	@Override
	public NpcIsland getData()
	{
		// TODO 自动生成方法存根
		return island;
	}

	@Override
	public int getId()
	{
		// TODO 自动生成方法存根
		return island.getIndex();
	}

	@Override
	public void setData(Object data)
	{
		// TODO 自动生成方法存根
		island = (NpcIsland)data;
	}
	
	/**
	 * @return island
	 */
	public NpcIsland getIsland()
	{
		return island;
	}

	
	/**
	 * @param island 要设置的 island
	 */
	public void setIsland(NpcIsland island)
	{
		this.island=island;
	}

}
