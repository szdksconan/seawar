package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.alliance.alliancefight.BattleGround;


public class BattleGroundSave extends ObjectSave
{
	
	/** ¾Ýµã */
	BattleGround battleGround;

	@Override
	public int getId()
	{
		return battleGround.getSid();
	}

	@Override
	public Object getData()
	{
		return battleGround;
	}

	@Override
	public void setData(Object data)
	{
		battleGround=(BattleGround)data;
	}

	@Override
	public ByteBuffer getByteBuffer()
	{
		if(battleGround==null) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		battleGround.bytesWrite(bb);
		return bb;
	}

}
