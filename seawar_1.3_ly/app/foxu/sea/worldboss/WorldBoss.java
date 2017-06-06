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

/** ����boss�� ����������� ��ֻ���� ˢ��ʱ�� ����ʱ�� */
public class WorldBoss extends Sample
{
	/** �������ݳ��� */
	public static int dataLength=4;
	/***/
	public static final int NULL_INT[]=new int[0];

	/** ����������������ǹ̶� false����� */
	boolean fix=false;
	/** {shipSid,num,index,range}4λһ�� ���ģʽ,num��index�����.�̶�ģʽ,���Ƿ�Ҳ���õ�������� */
	int[] fleetGroupCfg;
	/** �Ƿ�����,���ݸ�ʽ��fleetGroupCfgһ��,����index�̶� */
	int[] cityDefence=NULL_INT;
	/** ����Ʒsid �������õ��Ƿ����sids */
	int awardSids[];
	/** ��ɱ����Ʒsid */
	int killAwardSid;
	/** bossˢ��ʱ�� */
	int flushTime;
	/** ����ʱ�� */
	int protectTimeConfig;
	/** boss�ȼ�����ϵͳ���� */
	int bossLevel=10;
	/** BOSS����ʱ�� */
	int createTime;
	/** ���������� */
	int beAttack;
	/** �������� */
	public static SampleFactory factory=new SampleFactory();

	/* static methods */
	/** ���ֽ������з����л���ö������ */
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

	/** ��һ��ˢ��ʱ�� */
	int lastTime;
	/** �������ı���ʱ�� */
	int protectTime;
	/** �����˺����� ����id���˺���ֻ���� */
	IntKeyHashMap hurtList=new IntKeyHashMap();
	/** boss��ֻ */
	FleetGroup fleetGroup=new FleetGroup();
	/** ��ǰ����sid */
	int serviceSid;
	/** ��ǰ����ĵ���index */
	int index;
	
	
	public int getCreateTime()
	{
		return createTime;
	}


	
	public void setCreateTime(int createTime)
	{
		this.createTime=createTime;
	}
	/** ��ó�ʼ���ֻ���� */
	public int getFleetMaxNum()
	{
		return fleetGroup.getMaxNum();
	}

	/** ��õ�ǰ�Ĵ�ֻ���� */
	public int getFleetNowNum()
	{
		return fleetGroup.nowTotalNum();
	}

	/** ���õ�ǰ�������� */
	public void resetLostNum()
	{
		fleetGroup.resetLastNum();
	}

	/** ���ι������ٴ�ֻ���� */
	public int lostNum()
	{
		return fleetGroup.hurtListNum();
	}

	/** ���ĳ�����˵Ļ��ٴ�ֻ���� */
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

	/** Ϊĳ����������˺���ֻ���� */
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

	/** ���˲�����ʱ�Ƴ����˵��˺����� */
	public void removeAllianceHurtInfo(CreatObjectFactory objectFactory)
	{
		int[] ids=hurtList.keyArray();
		for(int i=0;i<ids.length;i++)
		{
			if(objectFactory.getAlliance(ids[i],false)==null)
				hurtList.remove(ids[i]);
		}
	}
	
	/** �ҳ����ٴ�ֻ����������id,���ٴ�ֻ����,��ȡ֮ǰ���Ƴ������ڵ����� */
	public Object[] getHurtMostAllianceId(CreatObjectFactory objectFactory)
	{
		removeAllianceHurtInfo(objectFactory);
		Object objects[]=hurtList.valueArray();
		SetKit.sort(objects,AllianceBossHurtComparator.getInstance());
		return objects;
	}

	/** �ҵ�ĳ�����˵����� */
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
		// ��ʼ������Ʒsid
		serviceSid=awardSids[MathKit.randomValue(0,awardSids.length)];
		return fleetGroup;
	}

	/**
	 * ��ʼ�ؿ�ս��
	 * 
	 * @param attacker ������
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

	/** ����ɱ */
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

	/** ������������л����ֽڻ����� */
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
