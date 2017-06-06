package foxu.sea.alliance;

import mustang.io.ByteBuffer;
import mustang.text.TextKit;

/** 联盟事件 */
public class AllianceEvent
{

	/**
	 * 事件类型 ALLIANCE_EVENT_BEFIGHT被掠夺 ALLIANCE_EVENT_NEW_PLAYER新玩家加入
	 * ALLIANCE_EVENT_PLAYER_LEFT有玩家离开 ALLIANCE_EVENT_KICK_PLAYER踢掉某个玩家
	 * ALLIANCE_EVENT_PLAYER_CONTRI玩家贡献得到多少点
	 * ALLIANCE_EVENT_MASTER_CHANGE管理成改变 ALLIANCE_BOSS_FIGHT=64
	 * ALLIANCE_EVENT_MODIFY=132 修改名称 ALLIANCE_EVENT_ANNOUCE=133 公告被修改
	 * ALLIANCE_EVENT_ISLAND=135 收藏联盟岛屿 ALLIANCE_EVENT_ICHANGE=135 联盟岛屿收藏修改
	 * ALLIANCE_BATTLE_FIGHTEND=136 联盟战结束 同时添加事件    
	 * ALLIANCE_EVENT_PLAYER_MATERIAL=137 玩家捐赠物资 
	 * ALLIANCE_EVENT_BET_SUCCESS=138 每次加标成功或者是竞标成功生成事件 
	 * ALLIANCE_BET_START=139 新一周的联盟竞标开始
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

	/** 触发方玩家名字 */
	String playerName="";
	/** 被动方玩家名字 */
	String passiveName="";
	/** 额外信息 */
	String extraInfo="";
	/** 事件类型 */
	int eventType;
	/** 事件创建时间 */
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

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeUTF(playerName);
		data.writeUTF(passiveName);
		data.writeUTF(extraInfo);
		data.writeByte(eventType);
		data.writeInt(create_at);
	}

	/** 将对象的域序列化到字节缓存中 */
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
	
	/** 临时代码，用于处理修改岛屿收藏时名字为空的情况(3.4版本与前台重新约定修复此bug) */
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
