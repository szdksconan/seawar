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

/** NPC岛屿 */
public class NpcIsland extends Sample
{

	/** 掠夺资源比值 */
	public static final int PLUDER_BI=30;
	/* static fields */
	/** 岛屿类型 */
	public static final int ISLAND_METAL=1,ISLAND_OIL=2,ISLAND_SILION=3,
					ISLAND_URANIUM=4,ISLAND_MONEY=5,ISLAND_WARTER=6,
					CHECK_POINT_NPC=7,WORLD_BOSS=8,AFFAIR_ACTIVITY=9,
					NIAN_BOSS=10,ISLAND_GEMS=11;
	/** 游戏世界的尺寸 */
	public static final int WORLD_WIDTH=600,WORLD_HEIGHT=600;
	/***/
	public static final int NULL_INT[]=new int[0];
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	/** 配置数据长度 */
	public static int dataLength=4;
	
	/**标识为玩家岛屿(用于gm工具查询)**/
	public static int NPC_PLAYER=1000;

	/* static methods */
	/** 从字节数组中反序列化获得对象的域 */
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
	/** 根据配置随机或者是固定 false是随机 */
	boolean fix=false;
	/** {shipSid,num,index,range}4位一组 随机模式,num和index被随机.固定模式,将城防也配置到这个数组 */
	int[] fleetGroupCfg;
	/** 城防配置,数据格式和fleetGroupCfg一样,但是index固定 */
	int[] cityDefence=NULL_INT;
	/** 奖励品sid */
	int awardSid;
	/** npc类型 */
	int islandType;
	/** 岛屿等级 */
	int islandLevel;
	/** 占领后每分钟产出的资源量 */
	int resource;
	/** 岛屿名字 */
	String name;

	/* fields */
	/** 岛屿id 数据库递增 用于index */
	int id;
	/** 岛屿的下标 index */
	int index;
	/** 绑定的玩家id 没有是0 */
	int playerId;
	/** 临时被占领的事件ID */
	int tempAttackEventId;
	/**宝石岛屿的结束时间**/
	int endTime;
	/** 锁对象 */
	// private ReentrantLock lock=new ReentrantLock();
	/** 所有参战船只 */
	FleetGroup fleetGroup;
	/** 随机时的种子 */
	int randomSeed;
	/**buff影响**/
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
	 * @param id 要设置的 id
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
	 * @param index 要设置的 index
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
	 * @param islandLevel 要设置的 islandLevel
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
	 * @param islandType 要设置的 islandType
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
	 * @param playerId 要设置的 playerId
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	/** 当前舰队的损失 */
	public FleetGroup getNowFightFleetGroup()
	{
		return fleetGroup;
	}

	/** 获得舰队群 */
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

	/** 获得该岛屿的掠夺资源 只有一种资源 */
	public int getPluderResource()
	{
		return this.resource*PLUDER_BI;
	}

	/** 根据已有的资源 反推驻守时间 */
	public int pushTime(int resources[])
	{
		int time=0;
		//避免因为多次整数计算带来采集玩家与侦查玩家显示不一致
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

	/** 将旧事件初始资源转移到新事件 */
	public void addResource(int oldResources[],int newResources[]){
		newResources[Resources.METAL]+=oldResources[Resources.METAL];
		newResources[Resources.OIL]+=oldResources[Resources.OIL];
		newResources[Resources.SILICON]+=oldResources[Resources.SILICON];
		newResources[Resources.URANIUM]+=oldResources[Resources.URANIUM];
		newResources[Resources.MONEY]+=oldResources[Resources.MONEY];
		newResources[Resources.GEMS]+=oldResources[Resources.GEMS];
	}
	/** 计算产生的资源 time分钟 加上胜利立即获得的30分钟资源 宝石另行计算 */
	public void setResource(Player player,int resources[],int time,FleetGroup group,
		int maxResource)
	{
		// 资源岛
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
	/** 强制设置资源(适用于新手引导) */
	public void forceSetResource(int resources[],int addResource)
	{
		// 资源岛
		if(islandType>=ISLAND_METAL&&islandType<=ISLAND_MONEY)
		{
			resources[islandType-1]+=addResource;
		}
	}
	
	/* methods */
	// /** 锁住island */
	// public void lock()
	// {
	// lock.lock();
	// }
	// /** 解锁island */
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
			/** 已用随机数列表 */
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
				for(j=rdList.length-1;j>=0;j--) // 判断缓存列表中有没有这个随机数
				{
					if(rdList[j]<0) nullIndex=j; // 记录一个空位的index,下边用来保存随机数
					if(rdList[j]==rd) continue a1; // 如果重复,结束当前循环,继续外层循环,重新生成随机数
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
	/** 将对象的域序列化到字节缓存中 */
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
			// 是否同盟
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
	 * @param awardSid 要设置的 awardSid
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
	 * @param tempAttackId 要设置的 tempAttackId
	 */
	public void setTempAttackEventId(int tempAttackEventId)
	{
		this.tempAttackEventId=tempAttackEventId;
	}

	// /** boss战斗 */
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
	 * 开始关卡战斗
	 * 
	 * @param attacker 进攻方
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
	 * @param resource 要设置的 resource
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
	 * @param name 要设置的 name
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
	/** 世界boss用 同时需要修改type和等级 */
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

	/**判断岛屿是是否生效**/
	public boolean checkDismiss()
	{
		if(islandType!=ISLAND_GEMS) return false;
		int timeNow=TimeKit.getSecondTime();
		if(timeNow>=endTime)
			return true;
		return false;
	}
	/**获取buff**/
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
	/**设置buff**/
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
	/** 检查岛屿buff **/
	public boolean checkBuff(int time,CreatObjectFactory factory)
	{
		if(islandType==NpcIsland.ISLAND_GEMS) 
		{
			checkGemsBuff(time,factory);
			return true;
		}
		return false;
	}

	/** 调整buff状态 time 占领时间 **/
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

	/** 增加buff **/
	public void addBuff(int buffSid)
	{
		if(services.contain(buffSid)) return;
		services.add(buffSid);
	}
	/** 移除buff **/
	public void removeBuff(int buffSid)
	{
		if(!services.contain(buffSid)) return;
		services.remove(buffSid);
	}
	/** 移除关联buff **/
	public void removeGemsBuff()
	{
		if(services==null || services.size()==0) return ;
		/** 移除buff **/
		for(int i=0;i<PublicConst.GEMS_ISLAND_BUFF.length;i+=2)
		{
			removeBuff(PublicConst.GEMS_ISLAND_BUFF[i+1]);
		}
	}
}
