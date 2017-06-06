package foxu.sea.alliance.alliancebattle;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.officer.OfficerBattleHQ;
import mustang.io.ByteBuffer;
import mustang.set.IntList;

/****
 * 参与联盟战的玩家信息
 * 
 * @author lhj
 * 
 */
public class PlayerAllianceFight
{

	/** 当前的船只数量 **/
	IntList list=new IntList();
	/** 当前的战斗力 **/
	int fightScore;
	/** 是否被淘汰 **/
	boolean out=false;
	/** 所有的船只信息 **/
	IntList wholeShips=new IntList();
	/** 玩家id **/
	int playerId;
	/** 联盟id **/
	int allianceId;
	/** 军官信息 **/
	OfficerBattleHQ officerBattle=new OfficerBattleHQ();

	/** 构造方法 **/
	public PlayerAllianceFight(int playerId,IntList list,int fightScore,
		int allianceId,OfficerBattleHQ officerBattle)
	{
		this.playerId=playerId;
		this.wholeShips=list;
		this.list=list;
		this.fightScore=fightScore;
		this.allianceId=allianceId;
		this.officerBattle=officerBattle;
	}

	public PlayerAllianceFight()
	{

	}

	public PlayerAllianceFight bytesRead(ByteBuffer data)
	{
		allianceId=data.readInt();
		fightScore=data.readInt();
		playerId=data.readInt();
		out=data.readBoolean();
		int length=data.readInt();
		// 所有的船只数量
		for(int i=0;i<length;i++)
		{
			wholeShips.add(data.readInt());
		}
		length=data.readInt();
		// 当前的船只数量
		for(int i=0;i<length;i++)
		{
			list.add(data.readInt());
		}
		officerBattle.bytesRead(data);
		return this;
	}
	/** 保存数据 **/
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(allianceId);
		data.writeInt(fightScore);
		data.writeInt(playerId);
		data.writeBoolean(out);
		bytesWriteWholeShips(data);
		bytesWriteShips(data);
		officerBattle.bytesWrite(data);

	}

	/** 序列化开始报名后的所有船只记录 **/
	public void bytesWriteWholeShips(ByteBuffer data)
	{
		data.writeInt(wholeShips.size());
		for(int i=0;i<wholeShips.size();i++)
		{
			data.writeInt(wholeShips.get(i));
		}
	}

	/** 现在拥有的船只数量 **/
	public void bytesWriteShips(ByteBuffer data)
	{
		data.writeInt(list.size());
		for(int i=0;i<list.size();i++)
		{
			data.writeInt(list.get(i));
		}
	}
	/** 前台序列化 **/
	public void showBytesWriteInfo(ByteBuffer data,CreatObjectFactory factory)
	{
		// 玩家的id
		data.writeInt(playerId);
		Player player=factory.getPlayerById(playerId);
		// 玩家的名称
		data.writeUTF(player.getName());
		data.writeInt(fightScore);
		// 玩家是否被淘汰
		if(out) 
			data.writeByte(0);
		else
			data.writeByte(1);
//		data.writeBoolean(out);
	}

	public OfficerBattleHQ getOfficerBattle()
	{
		return officerBattle;
	}

	public void setOfficerBattle(OfficerBattleHQ officerBattle)
	{
		this.officerBattle=officerBattle;
	}

	public IntList getList()
	{
		return list;
	}

	public void setList(IntList list)
	{
		this.list=list;
	}

	/** 获取当前的最大战斗力 **/
	public int getFightScore()
	{
		return fightScore;
	}

	public void setFightScore(int fightScore)
	{
		this.fightScore=fightScore;
	}

	public boolean isOut()
	{
		return out;
	}

	public void setOut(boolean out)
	{
		this.out=out;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	public IntList getWholeShips()
	{
		return wholeShips;
	}

	public void setWholeShips(IntList wholeShips)
	{
		this.wholeShips=wholeShips;
	}

	public int getAllianceId()
	{
		return allianceId;
	}

	public void setAllianceId(int allianceId)
	{
		this.allianceId=allianceId;
	}

	/**只获取当前的船只sid-数量**/
	public IntList getAllianceFightShip()
	{
		if(list==null||list.size()==0) return null;
		IntList shoplist=new IntList();
		for(int i=0;i<list.size();i+=3)
		{
			shoplist.add(list.get(i));
			shoplist.add(list.get(i+1));
		}
		return shoplist;
	}
}
