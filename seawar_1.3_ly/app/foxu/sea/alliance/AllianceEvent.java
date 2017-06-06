package foxu.sea.alliance;

import mustang.io.ByteBuffer;
import mustang.text.TextKit;

/** �����¼� */
public class AllianceEvent
{

	/**
	 * �¼����� ALLIANCE_EVENT_BEFIGHT���Ӷ� ALLIANCE_EVENT_NEW_PLAYER����Ҽ���
	 * ALLIANCE_EVENT_PLAYER_LEFT������뿪 ALLIANCE_EVENT_KICK_PLAYER�ߵ�ĳ�����
	 * ALLIANCE_EVENT_PLAYER_CONTRI��ҹ��׵õ����ٵ�
	 * ALLIANCE_EVENT_MASTER_CHANGE����ɸı� ALLIANCE_BOSS_FIGHT=64
	 * ALLIANCE_EVENT_MODIFY=132 �޸����� ALLIANCE_EVENT_ANNOUCE=133 ���汻�޸�
	 * ALLIANCE_EVENT_ISLAND=135 �ղ����˵��� ALLIANCE_EVENT_ICHANGE=135 ���˵����ղ��޸�
	 * ALLIANCE_BATTLE_FIGHTEND=136 ����ս���� ͬʱ����¼�    
	 * ALLIANCE_EVENT_PLAYER_MATERIAL=137 ��Ҿ������� 
	 * ALLIANCE_EVENT_BET_SUCCESS=138 ÿ�μӱ�ɹ������Ǿ���ɹ������¼� 
	 * ALLIANCE_BET_START=139 ��һ�ܵ����˾��꿪ʼ
	 */
	public static final int ALLIANCE_EVENT_BEFIGHT=1,
					ALLIANCE_EVENT_NEW_PLAYER=2,
					ALLIANCE_EVENT_PLAYER_LEFT=4,
					ALLIANCE_EVENT_KICK_PLAYER=8,
					ALLIANCE_EVENT_PLAYER_CONTRI=16,
					ALLIANCE_EVENT_MASTER_CHANGE=32,ALLIANCE_BOSS_FIGHT=64,
					ALLIANCE_EVENT_LEVEL=128,ALLIANCE_EVENT_SKILL_LEVEL=129,ALLIANCE_WORLD_BOSS_AWARD=130,
					ALLIANCE_EVENT_AUTOTRANFER=131,ALLIANCE_EVENT_MODIFY=132,ALLIANCE_EVENT_ANNOUCE=133,
					ALLIANCE_EVENT_ISLAND=134,ALLIANCE_EVENT_ICHANGE=135,ALLIANCE_BATTLE_FIGHTEND=136,
					ALLIANCE_EVENT_PLAYER_MATERIAL=137,ALLIANCE_EVENT_BET_SUCCESS=138,ALLIANCE_BET_START=139,
					ALLIANCE_EVENT_AUTO_JOIN=140;

	/** ������������� */
	String playerName="";
	/** ������������� */
	String passiveName="";
	/** ������Ϣ */
	String extraInfo="";
	/** �¼����� */
	int eventType;
	/** �¼�����ʱ�� */
	int create_at;

	public AllianceEvent()
	{

	}

	public AllianceEvent(int eventType,String playerName,String passiveName,
		String extraInfo,int time)
	{
		this.eventType=eventType;
		this.playerName=playerName;
		this.passiveName=passiveName;
		this.extraInfo=extraInfo;
		create_at=time;
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeUTF(playerName);
		data.writeUTF(passiveName);
		data.writeUTF(extraInfo);
		data.writeByte(eventType);
		data.writeInt(create_at);
	}

	/** ������������л����ֽڻ����� */
	public Object bytesRead(ByteBuffer data)
	{
		playerName=data.readUTF();
		passiveName=data.readUTF();
		extraInfo=data.readUTF();
		eventType=data.readUnsignedByte();
		create_at=data.readInt();
		return this;
	}

	public void showBytesWrite(ByteBuffer data)
	{
		checkIslandInfos();
		data.writeByte(eventType);
		data.writeInt(create_at);
		data.writeUTF(playerName);
		data.writeUTF(passiveName);
		data.writeUTF(extraInfo);
	}

	public int getCreate_at()
	{
		return create_at;
	}

	public void setCreate_at(int create_at)
	{
		this.create_at=create_at;
	}

	public int getEventType()
	{
		return eventType;
	}

	public void setEventType(int eventType)
	{
		this.eventType=eventType;
	}

	public String getExtraInfo()
	{
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo)
	{
		this.extraInfo=extraInfo;
	}

	public String getPassiveName()
	{
		return passiveName;
	}

	public void setPassiveName(String passiveName)
	{
		this.passiveName=passiveName;
	}

	public String getPlayerName()
	{
		return playerName;
	}

	public void setPlayerName(String playerName)
	{
		this.playerName=playerName;
	}
	
	/** ��ʱ���룬���ڴ����޸ĵ����ղ�ʱ����Ϊ�յ����(3.4�汾��ǰ̨����Լ���޸���bug) */
	public void checkIslandInfos()
	{
		if(eventType==ALLIANCE_EVENT_ICHANGE)
		{
			String[] infos=TextKit.split(extraInfo,",");
			if(infos.length<4||"".equals(infos[3]))
				extraInfo+=" ";
		}
	}
}
