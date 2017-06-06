package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.NpcIsland;

/**NPC����洢*/
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
		// TODO �Զ����ɷ������
		return island;
	}

	@Override
	public int getId()
	{
		// TODO �Զ����ɷ������
		return island.getIndex();
	}

	@Override
	public void setData(Object data)
	{
		// TODO �Զ����ɷ������
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
	 * @param island Ҫ���õ� island
	 */
	public void setIsland(NpcIsland island)
	{
		this.island=island;
	}

}
