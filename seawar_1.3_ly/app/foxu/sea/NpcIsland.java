package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.math.Random;
import mustang.math.Random1;
import mustang.net.DataAccessException;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.sea.alliance.Flag;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.SeaBackKit;

/** NPC���� */
public class NpcIsland extends Sample
{

	/** �Ӷ���Դ��ֵ */
	public static final int PLUDER_BI=30;
	/* static fields */
	/** �������� */
	public static final int ISLAND_METAL=1,ISLAND_OIL=2,ISLAND_SILION=3,
					ISLAND_URANIUM=4,ISLAND_MONEY=5,ISLAND_WARTER=6,
					CHECK_POINT_NPC=7,WORLD_BOSS=8,AFFAIR_ACTIVITY=9,
					NIAN_BOSS=10,ISLAND_GEMS=11;
	/** ��Ϸ����ĳߴ� */
	public static final int WORLD_WIDTH=600,WORLD_HEIGHT=600;
	/***/
	public static final int NULL_INT[]=new int[0];
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	/** �������ݳ��� */
	public static int dataLength=4;
	
	/**��ʶΪ��ҵ���(����gm���߲�ѯ)**/
	public static int NPC_PLAYER=1000;

	/* static methods */
	/** ���ֽ������з����л���ö������ */
	public static NpcIsland bytesReadNpcIsland(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		NpcIsland r=(NpcIsland)factory.newSample(sid);
		if(r==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,NpcIsland.class
					.getName()
					+" bytesRead, invalid sid:"+sid);
		r.bytesRead(data);
		return r;
	}

	/* configure fileds */
	/** ����������������ǹ̶� false����� */
	boolean fix=false;
	/** {shipSid,num,index,range}4λһ�� ���ģʽ,num��index�����.�̶�ģʽ,���Ƿ�Ҳ���õ�������� */
	int[] fleetGroupCfg;
	/** �Ƿ�����,���ݸ�ʽ��fleetGroupCfgһ��,����index�̶� */
	int[] cityDefence=NULL_INT;
	/** ����Ʒsid */
	int awardSid;
	/** npc���� */
	int islandType;
	/** ����ȼ� */
	int islandLevel;
	/** ռ���ÿ���Ӳ�������Դ�� */
	int resource;
	/** �������� */
	String name;

	/* fields */
	/** ����id ���ݿ���� ����index */
	int id;
	/** ������±� index */
	int index;
	/** �󶨵����id û����0 */
	int playerId;
	/** ��ʱ��ռ����¼�ID */
	int tempAttackEventId;
	/**��ʯ����Ľ���ʱ��**/
	int endTime;
	/** ������ */
	// private ReentrantLock lock=new ReentrantLock();
	/** ���в�ս��ֻ */
	FleetGroup fleetGroup;
	/** ���ʱ������ */
	int randomSeed;
	/**buffӰ��**/
	IntList services=new IntList();
	/* properties */
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
	 * @return index
	 */
	public int getIndex()
	{
		return index;
	}
	/**
	 * @param index Ҫ���õ� index
	 */
	public void setIndex(int index)
	{
		this.index=index;
	}
	/**
	 * @return islandLevel
	 */
	public int getIslandLevel()
	{
		return islandLevel;
	}
	/**
	 * @param islandLevel Ҫ���õ� islandLevel
	 */
	public void setIslandLevel(int islandLevel)
	{
		this.islandLevel=islandLevel;
	}
	/**
	 * @return islandType
	 */
	public int getIslandType()
	{
		return islandType;
	}
	/**
	 * @param islandType Ҫ���õ� islandType
	 */
	public void setIslandType(int islandType)
	{
		this.islandType=islandType;
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

	/** ��ǰ���ӵ���ʧ */
	public FleetGroup getNowFightFleetGroup()
	{
		return fleetGroup;
	}

	/** ��ý���Ⱥ */
	public FleetGroup getFleetGroup()
	{
		if(fleetGroup==null) createFleetGroup();
		// else
		// {
		// Fleet fleets[]=fleetGroup.getArray();
		// boolean bool=true;
		// for(int i=0;i<fleets.length;i++)
		// {
		// if(fleets[i]!=null&&fleets[i].getNum()>0)
		// {
		// bool=false;
		// break;
		// }
		// }
		// if(bool) createFleetGroup();
		// }
		return fleetGroup;
	}

	/** ��øõ�����Ӷ���Դ ֻ��һ����Դ */
	public int getPluderResource()
	{
		return this.resource*PLUDER_BI;
	}

	/** �������е���Դ ����פ��ʱ�� */
	public int pushTime(int resources[])
	{
		int time=0;
		//������Ϊ���������������ɼ��������������ʾ��һ��
		if(islandType!=ISLAND_GEMS)
		{
			float resource=resources[islandType-1];
			if(resource!=0) time=(int)(resource/this.resource*60);
			return time;
		}
		float resource=resources[Resources.GEMS];
		if(resource!=0) time=(int)(resource/this.resource*60);
		return time;
	}

	/** �����¼���ʼ��Դת�Ƶ����¼� */
	public void addResource(int oldResources[],int newResources[]){
		newResources[Resources.METAL]+=oldResources[Resources.METAL];
		newResources[Resources.OIL]+=oldResources[Resources.OIL];
		newResources[Resources.SILICON]+=oldResources[Resources.SILICON];
		newResources[Resources.URANIUM]+=oldResources[Resources.URANIUM];
		newResources[Resources.MONEY]+=oldResources[Resources.MONEY];
		newResources[Resources.GEMS]+=oldResources[Resources.GEMS];
	}
	/** �����������Դ time���� ����ʤ��������õ�30������Դ ��ʯ���м��� */
	public void setResource(Player player,int resources[],int time,FleetGroup group,
		int maxResource)
	{
		// ��Դ��
		if(islandType>=ISLAND_METAL&&islandType<=ISLAND_MONEY)
		{
			int addtion=SeaBackKit.groupResourceAddition(player,group,islandType);
			long addResource=(long)resource*(time)*addtion/PublicConst.AWARD_TOTAL_LENGTH;
			if(addResource>=maxResource&&maxResource!=0)
				addResource=maxResource;
			resources[islandType-1]+=addResource;
		}
		else if(islandType==ISLAND_GEMS)
		{
			long addResource=(long)resource*(time);
			if(addResource>=maxResource&&maxResource!=0)
				addResource=maxResource;
			resources[Resources.GEMS]+=addResource;
		}
		SeaBackKit.attackResource(group,resources);
	}
	/** ǿ��������Դ(��������������) */
	public void forceSetResource(int resources[],int addResource)
	{
		// ��Դ��
		if(islandType>=ISLAND_METAL&&islandType<=ISLAND_MONEY)
		{
			resources[islandType-1]+=addResource;
		}
	}
	
	/* methods */
	// /** ��סisland */
	// public void lock()
	// {
	// lock.lock();
	// }
	// /** ����island */
	// public void unlock()
	// {
	// lock.unlock();
	// }
	public FleetGroup createFleetGroup()
	{
		fleetGroup=new FleetGroup();
		if(fix)
		{
			Fleet fleet;
			for(int i=0;i<fleetGroupCfg.length;i+=dataLength)
			{
				fleet=new Fleet((Ship)Ship.factory
					.newSample(fleetGroupCfg[i]),fleetGroupCfg[i+1]);
				fleetGroup.setFleet(fleetGroupCfg[i+2],fleet);
			}
			// {shipSid,num,index,range}
		}
		else
		{
			randomSeed=MathKit.randomInt();
			Random r=new Random1(randomSeed);
			int length=fleetGroupCfg.length/dataLength;
			Fleet[] fleets=new Fleet[FleetGroup.MAX_FLEET];
			int i=0,rd=0,num;
			float rdRange;
			for(i=fleets.length-1;i>=0;i--)
			{
				rd=r.randomValue(0,length)*dataLength;
				num=fleetGroupCfg[rd+1];
				rdRange=fleetGroupCfg[rd+3]*1.0f;
				num=(int)(Math.ceil(num
					*(100+r.randomValue(-rdRange,rdRange))/100));

				fleets[i]=new Fleet((Ship)Ship.factory
					.newSample(fleetGroupCfg[rd]),num);
			}
			/** ����������б� */
			int[] rdList=new int[FleetGroup.MAX_FLEET];
			for(i=rdList.length-1;i>=0;i--)
			{
				rdList[i]=-1;
			}
			int j=0,nullIndex=0;
			i=0;
			a1:while(i<FleetGroup.MAX_FLEET)
			{
				rd=r.randomValue(0,FleetGroup.MAX_FLEET);
				for(j=rdList.length-1;j>=0;j--) // �жϻ����б�����û����������
				{
					if(rdList[j]<0) nullIndex=j; // ��¼һ����λ��index,�±��������������
					if(rdList[j]==rd) continue a1; // ����ظ�,������ǰѭ��,�������ѭ��,�������������
				}
				rdList[nullIndex]=rd;
				fleets[i].setLocation(rd);
				i++;
			}
			for(i=0,j=FleetGroup.MAX_FLEET;i<cityDefence.length;i+=dataLength)
			{
				num=cityDefence[i+1];
				num=(int)(Math.ceil(num
					*(100+r.randomValue(0.0f,cityDefence[i+3]))/100));
				fleets[j]=new Fleet((Ship)Ship.factory
					.newSample(cityDefence[i]),num);
				fleets[j++].setLocation(cityDefence[i+2]);
			}

			for(i=0;i<fleets.length;i++)
			{
				fleetGroup.setFleet(fleets[i].getLocation(),fleets[i]);
			}
		}
		return fleetGroup;
	}
	public Object copy(Object obj)
	{
		NpcIsland island=(NpcIsland)obj;
		island.fleetGroup=null;
		// island.lock=new ReentrantLock();
		return obj;
	}
	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(id);
		data.writeByte(islandType);
		data.writeByte(islandLevel);
		data.writeInt(index);
		data.writeInt(playerId);
		data.writeInt(tempAttackEventId);
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		id=data.readInt();
		islandType=data.readUnsignedByte();
		islandLevel=data.readUnsignedByte();
		index=data.readInt();
		playerId=data.readInt();
		tempAttackEventId=data.readInt();
		return this;
	}

	public void showBytesWrite(ByteBuffer data,String playerName,int level,
		int state,int playerSid,boolean bool,boolean bossBool,boolean hostile,int [] flag)
	{
		super.bytesWrite(data);
		if(playerId!=0)
		{
			data.writeBoolean(true);
			data.writeInt(index);
			data.writeByte(level);
			data.writeUTF(playerName);
			data.writeByte(state);
			data.writeInt(playerSid);
			// �Ƿ�ͬ��
			data.writeBoolean(bool);
			data.writeBoolean(hostile);
			if(flag!=null && flag.length==Flag.ALLIANCEFLAG)
			{
				data.writeShort(flag[0]);
				data.writeShort(flag[1]);
				data.writeShort(flag[2]);
			}
			else
			{
				data.writeShort(0);
				data.writeShort(0);
				data.writeShort(0);
			}
		}
		else
		{
			data.writeBoolean(false);
			data.writeInt(index);
			if(islandType==WORLD_BOSS)
			{
				data.writeBoolean(bossBool);
			}
		}
	}

	/**
	 * @return awardSid
	 */
	public int getAwardSid()
	{
		return awardSid;
	}

	/**
	 * @param awardSid Ҫ���õ� awardSid
	 */
	public void setAwardSid(int awardSid)
	{
		this.awardSid=awardSid;
	}

	/**
	 * @return tempAttackId
	 */
	public int getTempAttackEventId()
	{
		return tempAttackEventId;
	}

	/**
	 * @param tempAttackId Ҫ���õ� tempAttackId
	 */
	public void setTempAttackEventId(int tempAttackEventId)
	{
		this.tempAttackEventId=tempAttackEventId;
	}

	// /** bossս�� */
	public Object[] bossFight(FleetGroup attacker,boolean redeceProp,
		boolean stopProp)
	{
		Ability reduce=null;
		Ability stop=null;
		if(redeceProp)
			reduce=(Ability)FightScene.abilityFactory.newSample(412);
		if(stopProp) stop=(Ability)FightScene.abilityFactory.newSample(411);
		Ability[] roundAblility=new Ability[]{reduce,stop};
		FightScene scene=FightSceneFactory.factory.create(attacker,null,
			getFleetGroup(),roundAblility);
		FightShowEventRecord r=FightSceneFactory.factory.fight(scene,null);
		Object[] object=new Object[2];
		object[0]=scene;
		object[1]=r;
		return object;
	}

	/**
	 * ��ʼ�ؿ�ս��
	 * 
	 * @param attacker ������
	 */
	public Object[] fight(FleetGroup attacker)
	{
		// testcode
		// Ability[] test=new Ability[]{
		// (Ability)FightScene.abilityFactory.newSample(411),
		// (Ability)FightScene.abilityFactory.newSample(412)};
		// FightScene scene=FightSceneFactory.factory.create(attacker,null,
		// getFleetGroup(),test);
		FightScene scene=FightSceneFactory.factory.create(attacker,
			getFleetGroup());
		FightShowEventRecord r=FightSceneFactory.factory.fight(scene,null);
		Object[] object=new Object[2];
		object[0]=scene;
		object[1]=r;
		return object;
	}

	/**
	 * @return resource
	 */
	public int getResource()
	{
		return resource;
	}

	/**
	 * @param resource Ҫ���õ� resource
	 */
	public void setResource(int resource)
	{
		this.resource=resource;
	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name Ҫ���õ� name
	 */
	public void setName(String name)
	{
		this.name=name;
	}

	public int getEndTime()
	{
		return endTime;
	}
	
	public void setEndTime(int endTime)
	{
		this.endTime=endTime;
	}
	
	public IntList getServices()
	{
		return services;
	}
	
	public void setServices(IntList services)
	{
		this.services=services;
	}
	/** ����boss�� ͬʱ��Ҫ�޸�type�͵ȼ� */
	public void updateSid(int sid)
	{
		setSid(sid);
		NpcIsland land=(NpcIsland)NpcIsland.factory.newSample(sid);
		setIslandLevel(land.getIslandLevel());
		setIslandType(land.getIslandType());
		setName(land.getName());
	}
	
	public void setFleetGroup(FleetGroup fleetGroup)
	{
		this.fleetGroup=fleetGroup;
	}

	/**�жϵ������Ƿ���Ч**/
	public boolean checkDismiss()
	{
		if(islandType!=ISLAND_GEMS) return false;
		int timeNow=TimeKit.getSecondTime();
		if(timeNow>=endTime)
			return true;
		return false;
	}
	/**��ȡbuff**/
	public String getBuff()
	{
		if(services.size()==0) return "";
		StringBuffer buff=new StringBuffer();
		for(int i=0;i<services.size();i++)
		{
			if(i==0)
				buff.append(services.get(i));
			else
				buff.append(","+services.get(i));
		}
		return buff.toString();
	}
	/**����buff**/
	public void setBuff(String buff)
	{
		if(buff==null||buff.length()==0) return;
		String[] buffs=TextKit.split(buff,",");
		if(buffs.length==0) return;
		for(int i=0;i<buffs.length;i++)
		{
			int serviceId=TextKit.parseInt(buffs[i]);
			if(services.contain(serviceId)) continue;
				services.add(serviceId);
		}
	}
	/** ��鵺��buff **/
	public boolean checkBuff(int time,CreatObjectFactory factory)
	{
		if(islandType==NpcIsland.ISLAND_GEMS) 
		{
			checkGemsBuff(time,factory);
			return true;
		}
		return false;
	}

	/** ����buff״̬ time ռ��ʱ�� **/
	public boolean checkGemsBuff(int time,CreatObjectFactory factory)
	{
		time=time/3600;
		if(time<PublicConst.ISLAND_BUFF_CHANGE)
		{
			removeGemsBuff();
			return false;
		}
		int buffSid=0;
		for(int i=0;i<PublicConst.GEMS_ISLAND_BUFF.length;i+=2)
		{
			if(time>=PublicConst.GEMS_ISLAND_BUFF[i])
				buffSid=PublicConst.GEMS_ISLAND_BUFF[i+1];
		}
		if(buffSid==0) return false;
		if(services.contain(buffSid)) return false;
		removeGemsBuff();
		services.add(buffSid);
		factory.getIslandCache().load(index+"");
		return true;
	}

	/** ����buff **/
	public void addBuff(int buffSid)
	{
		if(services.contain(buffSid)) return;
		services.add(buffSid);
	}
	/** �Ƴ�buff **/
	public void removeBuff(int buffSid)
	{
		if(!services.contain(buffSid)) return;
		services.remove(buffSid);
	}
	/** �Ƴ�����buff **/
	public void removeGemsBuff()
	{
		if(services==null || services.size()==0) return ;
		/** �Ƴ�buff **/
		for(int i=0;i<PublicConst.GEMS_ISLAND_BUFF.length;i+=2)
		{
			removeBuff(PublicConst.GEMS_ISLAND_BUFF[i+1]);
		}
	}
}
