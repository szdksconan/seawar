package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.alliance.alliancebattle.BattleIsland;

/***
 * ��������ս�����װ��
 * 
 * @author lhj
 * 
 */
public class BattleIslandSave extends ObjectSave
{

	/** ��������ս���� **/
	BattleIsland battleIsland;

	/** ��ȡsid **/
	@Override
	public int getId()
	{
		return battleIsland.getSid();
	}

	/** ��ȡ���� **/
	@Override
	public Object getData()
	{
		return battleIsland;
	}

	/** ���� **/
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
