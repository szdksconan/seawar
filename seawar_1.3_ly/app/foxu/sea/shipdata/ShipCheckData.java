package foxu.sea.shipdata;

import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.set.ObjectArray;

/** 船只日志 */
public class ShipCheckData
{

	/** 日志类型 */
	public static final int FINLISH_SHIP_PRODUCE=0,CANCEL_SHIP_PRODUCE=1,
					FIGHT_SEND_SHIPS=2,REAPRIE_SHIP=3,FIGHT_GUAN_KA=4,
					FIGHT_NPC_ISLAND=5,FIGHT_PLAYER_ISLAND=6,
					SHIP_BACK_HOME=7,UP_SHIP_PRODUCE=8,BE_FIGHT_ISLAND=9,
					BE_FIGHT_YEDI=10,GM_SEND=11,ALLIANCE_DEFEND=12,
					RANDOM_AWARD=13,DONATE_SHIP=14,LOGIN_AWARD=15,ANN_SEND=16,
					SET_MAIN=17,BE_ALLIANCE_HELP=18,ALLIANCE_BATTLE_FIGHT=19,
					ALLIANCE_BACK_SHIP=20,FIGHT_REST_SHIP=21,WORLDBOSS_FIGHT=22,
					NIAN_FIGHT=23;
	/** 自增ID */
	int id;
	/** 类型 */
	int type;
	/** 创建时间 秒 */
	int createAt;
	/** 玩家ID */
	int playerId;
	/** 其他方玩家名字 */
	String attackPlayerName="";
	/** 额外信息 */
	String extra="";
	/** 本次操作后港口的船只数量 */
	IntList leftList=new IntList();
	/** 本次操作后事件上的船只数量 */
	ObjectArray eventList=new ObjectArray();
	/** 本次操作后伤兵船只数量 */
	IntList hurtList=new IntList();
	/** 本次操作船只sid和数量 */
	IntList list=new IntList();

	public void addShipDataEvent(ShipDataEvent event)
	{
		eventList.add(event);
	}

	public void bytesWriteList(ByteBuffer data)
	{
		data.writeByte(list.size());
		for(int i=0;i<list.size();i++)
		{
			data.writeShort(list.get(i));
		}
	}

	public void bytesReadList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		list.clear();
		for(int i=0;i<length;i++)
		{
			list.add(data.readUnsignedShort());
		}
	}

	public void bytesWritehurtList(ByteBuffer data)
	{
		data.writeByte(hurtList.size());
		for(int i=0;i<hurtList.size();i++)
		{
			data.writeShort(hurtList.get(i));
		}
	}

	public void bytesReadhurtList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		hurtList.clear();
		for(int i=0;i<length;i++)
		{
			hurtList.add(data.readUnsignedShort());
		}
	}

	public void bytesWriteEventList(ByteBuffer data)
	{
		if(eventList!=null&&eventList.size()>0)
		{
			Object[] array=eventList.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((ShipDataEvent)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	public void bytesReadEventList(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			ShipDataEvent troop=new ShipDataEvent();
			temp[i]=troop.bytesRead(data);
		}
		eventList=new ObjectArray(temp);
	}

	public void bytesWriteLeftList(ByteBuffer data)
	{
		data.writeByte(leftList.size());
		for(int i=0;i<leftList.size();i++)
		{
			data.writeShort(leftList.get(i));
		}
	}

	public void bytesReadLeftList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		leftList.clear();
		for(int i=0;i<length;i++)
		{
			leftList.add(data.readUnsignedShort());
		}
	}

	public String getAttackPlayerName()
	{
		return attackPlayerName;
	}

	public void setAttackPlayerName(String attackPlayerName)
	{
		this.attackPlayerName=attackPlayerName;
	}

	public int getCreateAt()
	{
		return createAt;
	}

	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}

	public String getExtra()
	{
		return extra;
	}

	public void setExtra(String extra)
	{
		this.extra=extra;
	}

	public IntList getHurtList()
	{
		return hurtList;
	}

	public void setHurtList(IntList hurtList)
	{
		this.hurtList=hurtList;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id=id;
	}

	public IntList getLeftList()
	{
		return leftList;
	}

	public void setLeftList(IntList leftList)
	{
		this.leftList=leftList;
	}

	public IntList getList()
	{
		return list;
	}

	public void setList(IntList list)
	{
		this.list=list;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type=type;
	}

	public ObjectArray getEventList()
	{
		return eventList;
	}

	public void setEventList(ObjectArray eventList)
	{
		this.eventList=eventList;
	}
}
