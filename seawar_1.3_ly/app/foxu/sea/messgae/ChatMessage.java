package foxu.sea.messgae;

import mustang.io.ByteBuffer;
import mustang.text.TextKit;

/** 玩家聊天类 */
public class ChatMessage
{

	/** 目的地默认为空DEFAULT */
	public static final String DEFAULT="";
	/** 屏蔽字幕集 */
	public static String SHIELD[];
	/** 格式化消息时需要用到的符号 1=换行 ,2=制表符*/
	public static final int FORMAT_NEW_LINE=1,FORMAT_TAB=2;
	/**系统分割符号**/
	public static String SEPARATORS="[@#$]";
	/**
	 * 聊天类型常量 WORLD_CHAT世界聊天，ALLIANCE_CHAT=联盟聊天，CHAT_SELF=4私聊
	 * SYSTEM_CHAT=8系统消息CHAT_SELF_FIGHT_DATA=5私聊战报分享   9，10，11 成就分享
	 * EQUIPMENT_WORLD=13世界配件消息 EQUIPMENT_ALLIANCE=14联盟配件消息 
	 * EQUIPMENT_FRIEND=15好友配件消息 FORMAT_SYSTEM_MESSAGE=17格式化的系统消息
	 * SHIP_INFO_WORLD=18 世界消息舰船信息分享 SHIP_INFO_ALLIANCE=19 联盟消息舰船信息分享
     * SHIP_INFO_FRIEND=20 好友消息舰船信息分享 ALLIANCE_FIGHT_TYPE=21 联盟系统分享战报
     * EQUIPMENT_WORLD=22世界军官消息 EQUIPMENT_ALLIANCE=23联盟军官消息 
	 * EQUIPMENT_FRIEND=24好友军官消息
	 */
	public static final int WORLD_CHAT=1,ALLIANCE_CHAT=2,CHAT_SELF=4,
					SYSTEM_CHAT=8,GET_FORE_MESSAGE=16,
					GET_ALLIANCE_MESSAGE=17,GM_WORLD_CHAT=32,FIGHT_DATA=64,
					ALLIANCE_FIGHT_DATA=128,CHAT_SELF_FIGHT_DATA=5,
					ACHIEVE_DATA=9,ACHIEVE_ALLIANCE_DATA=10,ACHIEVE_SELF_DATA=11,
					REPORT_MSG=12,EQUIPMENT_WORLD=13,EQUIPMENT_ALLIANCE=14,
					EQUIPMENT_FRIEND=15,FORMAT_SYSTEM_MESSAGE=17,SHIP_INFO_WORLD=18,
					SHIP_INFO_ALLIANCE=19,SHIP_INFO_FRIEND=20,ALLIANCE_FIGHT_TYPE=21,
					OFFICER_WORLD=22,OFFICER_ALLIANCE=23,OFFICER_FRIEND=24;
	/** 聊天类型 */
	int type;
	/** 消息源地址 (发送者名字) */
	String src;
	/** 军衔等级 */
	int playerType;
	/** 消息目的地信息 (帮会消息=帮会名,私聊为玩家名字) */
	String dest=DEFAULT;
	/** 消息正文 */
	String text;
	/** 消息时间 */
	int time;

	/** 战报id */
	int messageId;
	/**成就 sid */
	int sid;
	/**配件(或其他)等级 */
	int level;
	/** 消息发送者的语言环境 */
	int locale;
	/** 需要格式化的消息 */
	String[] formatText;
	/** 格式化符号 */
	int[] formatSign;

	/** 屏蔽字过滤 */
	public static String filerText(String text)
	{
		if((text==null)||(text.equals(""))) return "";
		String vallid=null;
		while((vallid=getValid(text))!=null)
		{
			text=TextKit.replaceAll(text,vallid,"*");
		}
		return text;
	}

	public static String getValid(String text)
	{
		if((text==null)||(text.equals(""))) return null;
		String t=text.toLowerCase();
		for(int i=0;i<SHIELD.length;i++)
		{
			int index=t.indexOf(SHIELD[i]);
			if(index>=0)
			{
				return text.substring(index,index+SHIELD[i].length());
			}
		}

		return null;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeByte(type);
		data.writeUTF(src);
		data.writeUTF(dest);
		data.writeUTF(text);
		data.writeInt(time);
	}

	public Object bytesRead(ByteBuffer data)
	{
		type=data.readUnsignedByte();
		src=data.readUTF();
		dest=data.readUTF();
		text=data.readUTF();
		time=data.readInt();
		return this;
	}

	public void showBytesWrite(ByteBuffer data,boolean wirteTime)
	{
		data.writeByte(type);
		if(wirteTime) data.writeInt(time);
		data.writeUTF(text);
		if(type!=SYSTEM_CHAT&&type!=FORMAT_SYSTEM_MESSAGE) data.writeUTF(src);
		if(type==FIGHT_DATA||type==ALLIANCE_FIGHT_DATA
			||type==CHAT_SELF_FIGHT_DATA || type==ALLIANCE_FIGHT_TYPE)
		{
			data.writeInt(messageId);
		}
		else if(type==ACHIEVE_DATA||type==ACHIEVE_ALLIANCE_DATA
			||type==ACHIEVE_SELF_DATA||type==EQUIPMENT_ALLIANCE
			||type==EQUIPMENT_FRIEND||type==EQUIPMENT_WORLD
			||type==OFFICER_ALLIANCE||type==OFFICER_FRIEND
			||type==OFFICER_WORLD)
		{
			data.writeShort(sid);
		}
		if(type==WORLD_CHAT||type==FIGHT_DATA||type==ACHIEVE_DATA
			||type==OFFICER_ALLIANCE||type==OFFICER_FRIEND
			||type==OFFICER_WORLD)
		{
			data.writeByte(playerType);
		}
		else if(type==EQUIPMENT_ALLIANCE||type==EQUIPMENT_FRIEND
			||type==EQUIPMENT_WORLD)
		{
			data.writeShort(level);
		}
		else if(type==FORMAT_SYSTEM_MESSAGE)
		{
			data.writeByte(formatText.length);
			for(int i=0;i<formatText.length;i++)
			{
				data.writeUTF(formatText[i]);
				data.writeByte(formatSign[i]);
			}
		}
		if(type==OFFICER_ALLIANCE||type==OFFICER_FRIEND||type==OFFICER_WORLD)
		{
			data.writeShort(level);
		}
	}

	/**
	 * @return dest
	 */
	public String getDest()
	{
		return dest;
	}

	/**
	 * @param dest 要设置的 dest
	 */
	public void setDest(String dest)
	{
		this.dest=dest;
	}

	/**
	 * @return src
	 */
	public String getSrc()
	{
		return src;
	}

	/**
	 * @param src 要设置的 src
	 */
	public void setSrc(String src)
	{
		this.src=src;
	}

	/**
	 * @return text
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @param text 要设置的 text
	 */
	public void setText(String text)
	{
		this.text=text;
	}

	/**
	 * @return time
	 */
	public int getTime()
	{
		return time;
	}

	/**
	 * @param time 要设置的 time
	 */
	public void setTime(int time)
	{
		this.time=time;
	}

	/**
	 * @return type
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type 要设置的 type
	 */
	public void setType(int type)
	{
		this.type=type;
	}

	public int getMessageId()
	{
		return messageId;
	}

	public void setMessageId(int messageId)
	{
		this.messageId=messageId;
	}

	
	public int getSid()
	{
		return sid;
	}

	
	public void setSid(int sid)
	{
		this.sid=sid;
	}

	
	public int getPlayerType()
	{
		return playerType;
	}

	
	public void setPlayerType(int playerType)
	{
		this.playerType=playerType;
	}

	public int getLocale()
	{
		return this.locale;
	}
	
	public int getLevel()
	{
		return level;
	}

	
	public void setLevel(int level)
	{
		this.level=level;
	}

	
	public void setLocale(int locale)
	{
		this.locale=locale;
	}

	
	public String[] getFormatText()
	{
		return formatText;
	}

	
	public void setFormatText(String[] formatText)
	{
		this.formatText=formatText;
	}

	
	public int[] getFormatSign()
	{
		return formatSign;
	}

	
	public void setFormatSign(int[] formatSign)
	{
		this.formatSign=formatSign;
	}
	
}
