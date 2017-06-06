package foxu.sea.alliance;

import mustang.io.ByteBuffer;
import mustang.set.IntList;

/** boss战奖励 */
public class BossAward
{
	/** 某个npcisland的sid */
	int npcIslandSid;
	/** 领取过的玩家id */
	IntList playerIds=new IntList();
	
	/**是否领取过*/
	public boolean checkAwardPlayerId(int playerId)
	{
		for(int i=0;i<playerIds.size();i++)
		{
			if(playerIds.get(i)==playerId)return true;
		}
		return false;
	}
	
	/**添加玩家的id*/
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

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(npcIslandSid);
		bytesWritePlayerIds(data);
	}

	/** 将对象的域序列化到字节缓存中 */
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
