package foxu.dcaccess.datasave;

import foxu.sea.worldboss.WorldBoss;
import mustang.io.ByteBuffer;


public class WorldBossSave extends ObjectSave
{
	
	WorldBoss worldBoss;

	@Override
	public ByteBuffer getByteBuffer()
	{
		if(worldBoss==null)return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		worldBoss.bytesWrite(bb);
		return bb;
	}

	@Override
	public Object getData()
	{
		// TODO 自动生成方法存根
		return worldBoss;
	}

	@Override
	public int getId()
	{
		return worldBoss.getSid();
	}

	@Override
	public void setData(Object data)
	{
		worldBoss = (WorldBoss)data;
	}

}
