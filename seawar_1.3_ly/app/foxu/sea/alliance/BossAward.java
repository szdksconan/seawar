package foxu.sea.alliance;

import mustang.io.ByteBuffer;
import mustang.set.IntList;

/** bossս���� */
public class BossAward
{
	/** ĳ��npcisland��sid */
	int npcIslandSid;
	/** ��ȡ�������id */
	IntList playerIds=new IntList();
	
	/**�Ƿ���ȡ��*/
	public boolean checkAwardPlayerId(int playerId)
	{
		for(int i=0;i<playerIds.size();i++)
		{
			if(playerIds.get(i)==playerId)return true;
		}
		return false;
	}
	
	/**�����ҵ�id*/
	public void addAwardPlayerId(int playerId)
	{
		playerIds.add(playerId);
	}
	
	public void bytesWritePlayerIds(ByteBuffer data)
	{
		if(playerIds==null||playerIds.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(playerIds.size());
			for(int i=0;i<playerIds.size();i++)
			{
				data.writeInt(playerIds.get(i));
			}
		}
	}
	
	public void bytesReadPlayerIds(ByteBuffer data)
	{
		int size = data.readUnsignedByte();
		playerIds.clear();
		if(size>0)
		{
			for(int i=0;i<size;i++)
			{
				playerIds.add(data.readInt());
			}
		}
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(npcIslandSid);
		bytesWritePlayerIds(data);
	}

	/** ������������л����ֽڻ����� */
	public Object bytesRead(ByteBuffer data)
	{
		npcIslandSid = data.readUnsignedShort();
		bytesReadPlayerIds(data);
		return this;
	}

	
	public int getNpcIslandSid()
	{
		return npcIslandSid;
	}

	
	public void setNpcIslandSid(int npcIslandSid)
	{
		this.npcIslandSid=npcIslandSid;
	}

	
	public IntList getPlayerIds()
	{
		return playerIds;
	}

	
	public void setPlayerIds(IntList playerIds)
	{
		this.playerIds=playerIds;
	}

}
