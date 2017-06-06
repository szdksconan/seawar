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
 * ս���¼� ��������һ�ű�� author:icetiger
 */
public class FightEvent extends ChangeListenerList
{

	/* static fields */
	/** �¼�״̬ */
	public static final int ATTACK=1,HOLD_ON=2,RETRUN_BACK=3;
	/** ������ʽATTACK_BACK=1�����󷵻�,ATTACK_HOLD=2������ռ�� */
	public static final int ATTACK_BACK=1,ATTACK_HOLD=2;
	/** change��Ϣ���� */
	public static final int CHANGE_FINISH=1;
	/** ɾ��״̬ */
	public static final int DELETE_TYPE=1;

	/* fields */
	/** �¼�id �����ݿ��Ӧ */
	int id;
	/** ���� */
	int type=ATTACK_BACK;
	/** �¼�״̬ ���� */
	int eventState=ATTACK;
	/** ���������id */
	int playerId;
	/** ��������ҵ���Index */
	int sourceIslandIndex;
	/** �������ߵ���Index */
	int attackIslandIndex;
	/** ս����Դ���� �������һλ������ */
	int resources[]=new int[Player.RESOURCES_SIZE];
	/** ����ʱ�� */
	int creatAt;
	/** ��Ҫʱ�� */
	int needTime;
	/** ɾ��״̬ */
	int delete;
	/** ���в�ս��ֻ */
	FleetGroup fleetGroup=new FleetGroup();

	/** ������������л����ֽڻ����� */
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
	/** ���ֽ������з����л���ö������ */
	public Object bytesReadShips(ByteBuffer data)
	{
		fleetGroup.bytesRead(data);
		return this;
	}
	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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
	 * @param id Ҫ���õ� id
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
	 * @param attack_id Ҫ���õ� attack_id
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
	 * @param event_state Ҫ���õ� event_state
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
	 * @param creatAt Ҫ���õ� creatAt
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
	 * @param needTime Ҫ���õ� needTime
	 */
	public void setNeedTime(int needTime,Player player,int checkTime)
	{
		// �Ƽ��ӳ�
		AdjustmentData data=((AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.ADD_SPREED_BUFF));
		int percent = 0;
		if(data!=null) percent+=data.percent;
		// ������Լӳ�
		percent+=(int)fleetGroup.getOfficerFleetAttr().getCommonAttr(
			OfficerBattleHQ.ARMY,Ship.ALL_SHIP,PublicConst.EXTRA_SPEED,
			false,0);
		needTime=needTime*100/(100+percent);
		// ����ӳ�
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
	 * @param resources Ҫ���õ� resources
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
	 * @param fleetGroup Ҫ���õ� fleetGroup
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
	 * @param delete Ҫ���õ� delete
	 */
	public void setDelete(int delete)
	{
		this.delete=delete;
		if(delete==DELETE_TYPE) change(this,CHANGE_FINISH);
	}
	
	/**
	 * @param delete ��ʱ�Ƴ�״̬��ʹ�ú�Ҫ��λ
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
	 * @param sourceIslandId Ҫ���õ� sourceIslandId
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
	 * @param type Ҫ���õ� type
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
	 * @param playerId Ҫ���õ� playerId
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	/**
	 * ��ʼ�ؿ�ս��
	 * 
	 * @param attacker ������
	 * @param bool �Ƿ���ط�����
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
	
	/**ˢ��buff**/
	public void flushBuff(NpcIsland beIsland,CreatObjectFactory factory,boolean flag)
	{
		/**ˢ��buff**/
		boolean result=beIsland.checkBuff(TimeKit.getSecondTime()-creatAt,factory);
		if(flag && result)
			fleetGroup.setBuff(beIsland.getServices());
	}
}