package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.Player;

/**player����
 * ��Ҫ���ڱ�������ݴ������ݿ��ʱ��
 */
public class PlayerSave extends ObjectSave
{
	/** player���� */
	Player playerData;

	/**
	 * @return data
	 */
	public ByteBuffer getByteBuffer()
	{
		if(playerData==null)return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		playerData.bytesWrite(bb);
		return bb;
	}

	@Override
	public int getId()
	{
		return playerData.getId();
	}

	@Override
	public Player getData()
	{
		return playerData;
	}

	@Override
	public void setData(Object data)
	{
		playerData = (Player)data;		
	}
}