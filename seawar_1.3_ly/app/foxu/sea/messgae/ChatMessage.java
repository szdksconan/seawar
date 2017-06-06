package foxu.sea.messgae;

import mustang.io.ByteBuffer;
import mustang.text.TextKit;

/** ��������� */
public class ChatMessage
{

	/** Ŀ�ĵ�Ĭ��Ϊ��DEFAULT */
	public static final String DEFAULT="";
	/** ������Ļ�� */
	public static String SHIELD[];
	/** ��ʽ����Ϣʱ��Ҫ�õ��ķ��� 1=���� ,2=�Ʊ��*/
	public static final int FORMAT_NEW_LINE=1,FORMAT_TAB=2;
	/**ϵͳ�ָ����**/
	public static String SEPARATORS="[@#$]";
	/**
	 * �������ͳ��� WORLD_CHAT�������죬ALLIANCE_CHAT=�������죬CHAT_SELF=4˽��
	 * SYSTEM_CHAT=8ϵͳ��ϢCHAT_SELF_FIGHT_DATA=5˽��ս������   9��10��11 �ɾͷ���
	 * EQUIPMENT_WORLD=13���������Ϣ EQUIPMENT_ALLIANCE=14���������Ϣ 
	 * EQUIPMENT_FRIEND=15���������Ϣ FORMAT_SYSTEM_MESSAGE=17��ʽ����ϵͳ��Ϣ
	 * SHIP_INFO_WORLD=18 ������Ϣ������Ϣ���� SHIP_INFO_ALLIANCE=19 ������Ϣ������Ϣ����
     * SHIP_INFO_FRIEND=20 ������Ϣ������Ϣ���� ALLIANCE_FIGHT_TYPE=21 ����ϵͳ����ս��
     * EQUIPMENT_WORLD=22���������Ϣ EQUIPMENT_ALLIANCE=23���˾�����Ϣ 
	 * EQUIPMENT_FRIEND=24���Ѿ�����Ϣ
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
	/** �������� */
	int type;
	/** ��ϢԴ��ַ (����������) */
	String src;
	/** ���εȼ� */
	int playerType;
	/** ��ϢĿ�ĵ���Ϣ (�����Ϣ=�����,˽��Ϊ�������) */
	String dest=DEFAULT;
	/** ��Ϣ���� */
	String text;
	/** ��Ϣʱ�� */
	int time;

	/** ս��id */
	int messageId;
	/**�ɾ� sid */
	int sid;
	/**���(������)�ȼ� */
	int level;
	/** ��Ϣ�����ߵ����Ի��� */
	int locale;
	/** ��Ҫ��ʽ������Ϣ */
	String[] formatText;
	/** ��ʽ������ */
	int[] formatSign;

	/** �����ֹ��� */
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

	/** ������������л����ֽڻ����� */
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
	 * @param dest Ҫ���õ� dest
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
	 * @param src Ҫ���õ� src
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
	 * @param text Ҫ���õ� text
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
	 * @param time Ҫ���õ� time
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
	 * @param type Ҫ���õ� type
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
