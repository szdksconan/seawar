package foxu.sea.alliance.alliancebattle;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.officer.OfficerBattleHQ;
import mustang.io.ByteBuffer;
import mustang.set.IntList;

/****
 * ��������ս�������Ϣ
 * 
 * @author lhj
 * 
 */
public class PlayerAllianceFight
{

	/** ��ǰ�Ĵ�ֻ���� **/
	IntList list=new IntList();
	/** ��ǰ��ս���� **/
	int fightScore;
	/** �Ƿ���̭ **/
	boolean out=false;
	/** ���еĴ�ֻ��Ϣ **/
	IntList wholeShips=new IntList();
	/** ���id **/
	int playerId;
	/** ����id **/
	int allianceId;
	/** ������Ϣ **/
	OfficerBattleHQ officerBattle=new OfficerBattleHQ();

	/** ���췽�� **/
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
		// ���еĴ�ֻ����
		for(int i=0;i<length;i++)
		{
			wholeShips.add(data.readInt());
		}
		length=data.readInt();
		// ��ǰ�Ĵ�ֻ����
		for(int i=0;i<length;i++)
		{
			list.add(data.readInt());
		}
		officerBattle.bytesRead(data);
		return this;
	}
	/** �������� **/
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

	/** ���л���ʼ����������д�ֻ��¼ **/
	public void bytesWriteWholeShips(ByteBuffer data)
	{
		data.writeInt(wholeShips.size());
		for(int i=0;i<wholeShips.size();i++)
		{
			data.writeInt(wholeShips.get(i));
		}
	}

	/** ����ӵ�еĴ�ֻ���� **/
	public void bytesWriteShips(ByteBuffer data)
	{
		data.writeInt(list.size());
		for(int i=0;i<list.size();i++)
		{
			data.writeInt(list.get(i));
		}
	}
	/** ǰ̨���л� **/
	public void showBytesWriteInfo(ByteBuffer data,CreatObjectFactory factory)
	{
		// ��ҵ�id
		data.writeInt(playerId);
		Player player=factory.getPlayerById(playerId);
		// ��ҵ�����
		data.writeUTF(player.getName());
		data.writeInt(fightScore);
		// ����Ƿ���̭
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

	/** ��ȡ��ǰ�����ս���� **/
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

	/**ֻ��ȡ��ǰ�Ĵ�ֻsid-����**/
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
