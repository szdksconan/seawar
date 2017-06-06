package foxu.sea.event;

import mustang.event.ChangeListenerList;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.FightScene;
import foxu.sea.AttrAdjustment.AdjustmentData;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Service;
import foxu.sea.Ship;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.FleetGroup;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * 战斗事件 单独存入一张表的 author:icetiger
 */
public class FightEvent extends ChangeListenerList
{

	/* static fields */
	/** 事件状态 */
	public static final int ATTACK=1,HOLD_ON=2,RETRUN_BACK=3;
	/** 攻击方式ATTACK_BACK=1攻击后返回,ATTACK_HOLD=2攻击后占领 */
	public static final int ATTACK_BACK=1,ATTACK_HOLD=2;
	/** change消息常量 */
	public static final int CHANGE_FINISH=1;
	/** 删除状态 */
	public static final int DELETE_TYPE=1;

	/* fields */
	/** 事件id 和数据库对应 */
	int id;
	/** 类型 */
	int type=ATTACK_BACK;
	/** 事件状态 进攻 */
	int eventState=ATTACK;
	/** 攻击方玩家id */
	int playerId;
	/** 主动方玩家岛屿Index */
	int sourceIslandIndex;
	/** 被攻击者岛屿Index */
	int attackIslandIndex;
	/** 战斗资源奖励 这里最后一位存声望 */
	int resources[]=new int[Player.RESOURCES_SIZE];
	/** 创建时间 */
	int creatAt;
	/** 需要时间 */
	int needTime;
	/** 删除状态 */
	int delete;
	/** 所有参战船只 */
	FleetGroup fleetGroup=new FleetGroup();

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeByte(type);
		data.writeByte(eventState);
		data.writeInt(playerId);
		data.writeInt(attackIslandIndex);
		data.writeInt(creatAt);
		data.writeInt(needTime);
		data.writeInt(sourceIslandIndex);
		data.writeByte(delete);
		bytesWriteResources(data);
		bytesWriteShips(data);
	}

	public Object bytesRead(ByteBuffer data)
	{
		id=data.readInt();
		type=data.readUnsignedByte();
		eventState=data.readUnsignedByte();
		playerId=data.readInt();
		attackIslandIndex=data.readInt();
		creatAt=data.readInt();
		needTime=data.readInt();
		sourceIslandIndex=data.readInt();
		delete=data.readUnsignedByte();
		bytesReadResources(data);
		bytesReadShips(data);
		return this;
	}
	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadShips(ByteBuffer data)
	{
		fleetGroup.bytesRead(data);
		return this;
	}
	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteShips(ByteBuffer data)
	{
		fleetGroup.bytesWrite(data);
	}

	public void bytesReadResources(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		resources=new int[length];
		for(int i=0;i<length;i++)
		{
			resources[i]=data.readInt();
		}
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteResources(ByteBuffer data)
	{
		data.writeByte(resources.length);
		for(int i=0;i<resources.length;i++)
		{
			data.writeInt(resources[i]);
		}
	}
	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}
	/**
	 * @param id 要设置的 id
	 */
	public void setId(int id)
	{
		this.id=id;
	}

	/**
	 * @return attack_id
	 */
	public int getAttackIslandIndex()
	{
		return attackIslandIndex;
	}

	/**
	 * @param attack_id 要设置的 attack_id
	 */
	public void setAttackIslandIndex(int attackIndex)
	{
		this.attackIslandIndex=attackIndex;
	}
	/**
	 * @return event_state
	 */
	public int getEventState()
	{
		return eventState;
	}
	/**
	 * @param event_state 要设置的 event_state
	 */
	public void setEventState(int eventState)
	{
		this.eventState=eventState;
	}
	/**
	 * @return creatAt
	 */
	public int getCreatAt()
	{
		return creatAt;
	}
	/**
	 * @param creatAt 要设置的 creatAt
	 */
	public void setCreatAt(int creatAt)
	{
		this.creatAt=creatAt;
	}
	/**
	 * @return needTime
	 */
	public int getNeedTime()
	{
		return needTime;
	}

	public void setNeedTimeDB(int needTime)
	{
		this.needTime=needTime;
	}
	/**
	 * @param needTime 要设置的 needTime
	 */
	public void setNeedTime(int needTime,Player player,int checkTime)
	{
		// 科技加成
		AdjustmentData data=((AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.ADD_SPREED_BUFF));
		int percent = 0;
		if(data!=null) percent+=data.percent;
		// 随军属性加成
		percent+=(int)fleetGroup.getOfficerFleetAttr().getCommonAttr(
			OfficerBattleHQ.ARMY,Ship.ALL_SHIP,PublicConst.EXTRA_SPEED,
			false,0);
		needTime=needTime*100/(100+percent);
		// 服务加成
		Service service=player.getServiceByType(PublicConst.ADD_SPREED_BUFF);
		if(service!=null) needTime=needTime*100/(100+service.getValue());
		if(needTime<=5) needTime=5;
		this.needTime=needTime;
	}
	/**
	 * @return resources
	 */
	public int[] getResources()
	{
		return resources;
	}
	/**
	 * @param resources 要设置的 resources
	 */
	public void setResources(int[] resources)
	{
		this.resources=resources;
	}
	/**
	 * @return fleetGroup
	 */
	public FleetGroup getFleetGroup()
	{
		return fleetGroup;
	}
	/**
	 * @param fleetGroup 要设置的 fleetGroup
	 */
	public void setFleetGroup(FleetGroup fleetGroup)
	{
		this.fleetGroup=fleetGroup;
	}
	/**
	 * @return delete
	 */
	public int getDelete()
	{
		return delete;
	}
	/**
	 * @param delete 要设置的 delete
	 */
	public void setDelete(int delete)
	{
		this.delete=delete;
		if(delete==DELETE_TYPE) change(this,CHANGE_FINISH);
	}
	
	/**
	 * @param delete 暂时移除状态，使用后要复位
	 */
	public void setDynamicDelete(int delete)
	{
		this.delete=delete;
	}

	/**
	 * @return sourceIslandId
	 */
	public int getSourceIslandIndex()
	{
		return sourceIslandIndex;
	}
	/**
	 * @param sourceIslandId 要设置的 sourceIslandId
	 */
	public void setSourceIslandId(int sourceIslandIndex)
	{
		this.sourceIslandIndex=sourceIslandIndex;
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
	/**
	 * @return playerId
	 */
	public int getPlayerId()
	{
		return playerId;
	}
	/**
	 * @param playerId 要设置的 playerId
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	/**
	 * 开始关卡战斗
	 * 
	 * @param attacker 进攻方
	 * @param bool 是否防守方先手
	 */
	public Object[] fight(FleetGroup attacker,boolean bool)
	{
		FightScene scene=FightSceneFactory.factory.create(attacker,
			getFleetGroup());
		if(bool) scene.setDefend(true);
		FightShowEventRecord r=FightSceneFactory.factory.fight(scene,null);
		Object[] object=new Object[2];
		object[0]=scene;
		object[1]=r;
		return object;
	}
	
	/**刷新buff**/
	public void flushBuff(NpcIsland beIsland,CreatObjectFactory factory,boolean flag)
	{
		/**刷新buff**/
		boolean result=beIsland.checkBuff(TimeKit.getSecondTime()-creatAt,factory);
		if(flag && result)
			fleetGroup.setBuff(beIsland.getServices());
	}
}