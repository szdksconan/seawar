package foxu.sea.worldboss;

import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.sea.Ship;
import foxu.sea.comparator.AllianceBossHurtComparator;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.math.Random;
import mustang.math.Random1;
import mustang.net.DataAccessException;
import mustang.set.IntKeyHashMap;
import mustang.set.SetKit;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;

/** 世界boss类 存放联盟排名 船只数量 刷新时间 保护时间 */
public class WorldBoss extends Sample
{
	/** 配置数据长度 */
	public static int dataLength=4;
	/***/
	public static final int NULL_INT[]=new int[0];

	/** 根据配置随机或者是固定 false是随机 */
	boolean fix=false;
	/** {shipSid,num,index,range}4位一组 随机模式,num和index被随机.固定模式,将城防也配置到这个数组 */
	int[] fleetGroupCfg;
	/** 城防配置,数据格式和fleetGroupCfg一样,但是index固定 */
	int[] cityDefence=NULL_INT;
	/** 奖励品sid 这里配置的是服务的sids */
	int awardSids[];
	/** 击杀奖励品sid */
	int killAwardSid;
	/** boss刷新时间 */
	int flushTime;
	/** 保护时间 */
	int protectTimeConfig;
	/** boss等级用于系统公告 */
	int bossLevel=10;
	/** BOSS出生时间 */
	int createTime;
	/** 被攻击次数 */
	int beAttack;
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();

	/* static methods */
	/** 从字节数组中反序列化获得对象的域 */
	public static WorldBoss bytesReadWorldBoss(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		WorldBoss r=(WorldBoss)factory.newSample(sid);
		if(r==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,WorldBoss.class
					.getName()
					+" bytesRead, invalid sid:"+sid);
		r.bytesRead(data);
		return r;
	}

	/** 上一次刷新时间 */
	int lastTime;
	/** 持续到的保护时间 */
	int protectTime;
	/** 联盟伤害排名 联盟id，伤害船只数量 */
	IntKeyHashMap hurtList=new IntKeyHashMap();
	/** boss船只 */
	FleetGroup fleetGroup=new FleetGroup();
	/** 当前服务sid */
	int serviceSid;
	/** 当前随机的岛屿index */
	int index;
	
	
	public int getCreateTime()
	{
		return createTime;
	}


	
	public void setCreateTime(int createTime)
	{
		this.createTime=createTime;
	}
	/** 获得初始最大船只数量 */
	public int getFleetMaxNum()
	{
		return fleetGroup.getMaxNum();
	}

	/** 获得当前的船只数量 */
	public int getFleetNowNum()
	{
		return fleetGroup.nowTotalNum();
	}

	/** 重置当前击毁数量 */
	public void resetLostNum()
	{
		fleetGroup.resetLastNum();
	}

	/** 本次攻击销毁船只数量 */
	public int lostNum()
	{
		return fleetGroup.hurtListNum();
	}

	/** 获得某个联盟的击毁船只数量 */
	public int allianceAttackNum(int allianceId)
	{
		int attackNum=0;
		if(hurtList.get(allianceId)!=null
			&&!hurtList.get(allianceId).equals(""))
		{
			BossHurt bosshurt=(BossHurt)hurtList
				.get(allianceId);
			attackNum=bosshurt.getHurtNum();
		}
		return attackNum;
	}

	/** 为某个联盟添加伤害船只数量 */
	public void addLostNum(int allianceId,int num)
	{
		if(hurtList.get(allianceId)!=null
			&&!hurtList.get(allianceId).equals(""))
		{
			BossHurt bosshurt=(BossHurt)hurtList
				.get(allianceId);
			int attackNum=bosshurt.getHurtNum();
			bosshurt.setHurtNum(attackNum+num);
			return;
		}
		BossHurt bosshurt=new BossHurt();
		bosshurt.setId(allianceId);
		bosshurt.setHurtNum(num);
		hurtList.put(allianceId,bosshurt);
	}

	/** 联盟不存在时移除联盟的伤害数据 */
	public void removeAllianceHurtInfo(CreatObjectFactory objectFactory)
	{
		int[] ids=hurtList.keyArray();
		for(int i=0;i<ids.length;i++)
		{
			if(objectFactory.getAlliance(ids[i],false)==null)
				hurtList.remove(ids[i]);
		}
	}
	
	/** 找出销毁船只数最多的联盟id,击毁船只数量,获取之前先移除不存在的联盟 */
	public Object[] getHurtMostAllianceId(CreatObjectFactory objectFactory)
	{
		removeAllianceHurtInfo(objectFactory);
		Object objects[]=hurtList.valueArray();
		SetKit.sort(objects,AllianceBossHurtComparator.getInstance());
		return objects;
	}

	/** 找到某个联盟的排名 */
	public int sortNum(int allianceId)
	{
		Object objects[]=hurtList.valueArray();
		SetKit.sort(objects,AllianceBossHurtComparator.getInstance());
		for(int i=0;i<objects.length;i++)
		{
			BossHurt bosshurt=(BossHurt)objects[i];
			if(bosshurt.getId()==allianceId)
			{
				return i+1;
			}
		}
		return 0;
	}

	public FleetGroup getFleetGroup()
	{
		if(fleetGroup==null) return createFleetGroup();
		return fleetGroup;
	}

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
			int randomSeed=MathKit.randomInt();
			Random r=new Random1(randomSeed);
			int length=fleetGroupCfg.length/dataLength;
			Fleet[] fleets=new Fleet[FleetGroup.MAX_FLEET];
			int i=0,rd=0,num;
//			float rdRange=0;
			for(i=fleets.length-1;i>=0;i--)
			{
				rd=r.randomValue(0,length)*dataLength;
				num=fleetGroupCfg[rd+1];
//				rdRange=fleetGroupCfg[rd+3]*1.0f;
				// num=(int)(Math.ceil(num
				// *(100+r.randomValue(-rdRange,rdRange))/100));

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
				// num=(int)(Math.ceil(num
				// *(100+r.randomValue(0.0f,cityDefence[i+3]))/100));
				fleets[j]=new Fleet((Ship)Ship.factory
					.newSample(cityDefence[i]),num);
				fleets[j++].setLocation(cityDefence[i+2]);
			}

			for(i=0;i<fleets.length;i++)
			{
				fleetGroup.setFleet(fleets[i].getLocation(),fleets[i]);
			}
		}
		// 初始化奖励品sid
		serviceSid=awardSids[MathKit.randomValue(0,awardSids.length)];
		return fleetGroup;
	}

	/**
	 * 开始关卡战斗
	 * 
	 * @param attacker 进攻方
	 */
	public Object[] fight(FleetGroup attacker)
	{
		Ability[] ab=new Ability[1];
		ab[0]=(Ability)FightScene.abilityFactory.newSample(601);
		FightScene scene=FightSceneFactory.factory.create(attacker,null,
			getFleetGroup(),ab);
		FightShowEventRecord r=FightSceneFactory.factory.fight(scene,null);
		Object[] object=new Object[2];
		object[0]=scene;
		object[1]=r;
		beAttack++;
		return object;
	}

	public void bytesWirteFleetGroup(ByteBuffer data)
	{
		fleetGroup.bytesWrite(data);
	}

	public void bytesReadFleetGroup(ByteBuffer data)
	{
		fleetGroup.bytesRead(data);
	}

	public void bytesWriteHurtList(ByteBuffer data)
	{
		Object object[]=hurtList.valueArray();
		if(object.length<=0)
		{
			data.writeShort(0);
		}
		else
		{
			data.writeShort(object.length);
			for(int i=0;i<object.length;i++)
			{
				BossHurt hurtValue=(BossHurt)object[i];
				data.writeInt(hurtValue.getId());
				data.writeShort(hurtValue.getHurtNum());
			}
		}
	}

	public void bytesReadHurtList(ByteBuffer data)
	{
		int size=data.readUnsignedShort();
		for(int i=0;i<size;i++)
		{
			int allianceId=data.readInt();
			int hurtNum=data.readUnsignedShort();
			BossHurt hurtValue=new BossHurt();
			hurtValue.setId(allianceId);
			hurtValue.setHurtNum(hurtNum);
			hurtList.put(allianceId,hurtValue);
		}
	}

	/** 被击杀 */
	public void bekilled()
	{
		index=0;
		lastTime=TimeKit.getSecondTime();
		getHurtList().clear();
//		fleetGroup=null;
	}

	public Object copy(Object obj)
	{
		WorldBoss boss=(WorldBoss)super.copy(obj);
		boss.fleetGroup=new FleetGroup();
		boss.hurtList=new IntKeyHashMap();
		// island.lock=new ReentrantLock();
		return boss;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(lastTime);
		data.writeInt(protectTime);
		data.writeInt(createTime);
		data.writeShort(serviceSid);
		data.writeInt(index);
		bytesWirteFleetGroup(data);
		bytesWriteHurtList(data);
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		lastTime=data.readInt();
		protectTime=data.readInt();
		createTime=data.readInt();
		serviceSid=data.readUnsignedShort();
		index=data.readInt();
		bytesReadFleetGroup(data);
		bytesReadHurtList(data);
		return this;
	}

	public int getFlushTime()
	{
		return flushTime;
	}

	public void setFlushTime(int flushTime)
	{
		this.flushTime=flushTime;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index=index;
	}

	public int getLastTime()
	{
		return lastTime;
	}

	public void setLastTime(int lastTime)
	{
		this.lastTime=lastTime;
	}

	public int getProtectTime()
	{
		return protectTime;
	}

	public void setProtectTime(int protectTime)
	{
		this.protectTime=protectTime;
	}

	public int getProtectTimeConfig()
	{
		return protectTimeConfig;
	}

	public void setProtectTimeConfig(int protectTimeConfig)
	{
		this.protectTimeConfig=protectTimeConfig;
	}

	public int getServiceSid()
	{
		return serviceSid;
	}

	public void setServiceSid(int awardSid)
	{
		this.serviceSid=awardSid;
	}

	public IntKeyHashMap getHurtList()
	{
		return hurtList;
	}

	public void setHurtList(IntKeyHashMap hurtList)
	{
		this.hurtList=hurtList;
	}

	public int getBossLevel()
	{
		return bossLevel;
	}

	public void setBossLevel(int bossLevel)
	{
		this.bossLevel=bossLevel;
	}

	public int getKillAwardSid()
	{
		return killAwardSid;
	}

	public void setKillAwardSid(int killAwardSid)
	{
		this.killAwardSid=killAwardSid;
	}

	public int getBeAttack()
	{
		return beAttack;
	}

	public void setBeAttack(int beAttack)
	{
		this.beAttack=beAttack;
	}
}
