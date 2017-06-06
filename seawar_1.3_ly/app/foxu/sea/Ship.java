package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.WarManicActivity;
import foxu.sea.builds.Produceable;
import foxu.sea.builds.Product;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.shipdata.ShipCheckData;
import foxu.sea.task.TaskEventExecute;

/** �� */
public class Ship extends Role implements Produceable
{
	/* static fields */
	private static Logger log=LogFactory.getLogger(Ship.class);
	
	/**
	 * ATTACK=100����,DEFENCE=101����,ACCURATE=102��׼,AVOID=103�ر�,SHIP_NUM=104��������
	 * ,HP=105����,CRITICAL_HIT=106����, CRITICAL_HIT_RESIST=107�����ֿ�
	 */
	public static final int ATTACK=100,DEFENCE=101,ACCURATE=102,AVOID=103,
					SHIP_NUM=104,HP=105,CRITICAL_HIT=106,
					CRITICAL_HIT_RESIST=107;
	/**
	 * ������ SHIPS_1ս�н� SHIPS_2Ǳͧ SHIPS_3Ѳ�� SHIPS_4��ĸ POSITION_AIR=�վ�����
	 * POSITION_MISSILE=�������� POSITION_FIRE=������� �ŵ�λ�û��� �վ� ���� 6,7,8
	 */
	public static final int BATTLE_SHIP=1,SUBMARINE_SHIP=2,CRUISER_SHIP=4,
					AIRCRAFT_SHIP=8,POSITION_AIR=16,POSITION_MISSILE=32,
					POSITION_FIRE=64;
	/** ͨ������ �������ʹ����������ʹ�ֻ��*/
	public static final int ALL_SHIP=15;
	
	/** AIR_RAID��Ϯ,ARTILLERY�ڻ�,MISSILE����,TORPEDO����,NUCLEAR�� */
	public static final int AIR_RAID=0,ARTILLERY=1,MISSILE=2,TORPEDO=3,
					NUCLEAR=4;

	/* configure fileds */
	/** ����ֵ */
	float life;
	/** �������� */
	int attackType;
	/** ÿֻ������ʱ�� */
	int buildTime;
	/** ������ȡ�ľ��� */
	int buildExperience;
	/** ��ͨ�������� */
	int normalAbility;
	/** ����sid�б� */
	int[] abilitySid;
	/** ��ʯ�޸���Ǯ */
	float gemRepair;
	/** ����޸� */
	int goldCost;
	/** ��ԴЯ���� */
	int carryResource;
	/** �����������ɹ����õľ���ֵ */
	int exp;

	/** �������� */
	long costResources[];
	/** ��Ҫ���ĵ���Ʒsid,num,sid,num */
	int costPropSid[];
	/** �����б� */
	Ability[] abilityList;
	/** ս�������б� */
	Ability[] fightAbility;
	/** ���� */
	int defence;
	/** ������ */
	float attack;
	/** ��׼ */
	int accurate;
	/** �ر� */
	int avoid;
	/** ������ */
	int critical;
	/** �������� */
	int decritical;
	/** �����б� */
	int[] resist;
	/** �Ƿ������������ */
	boolean isAttack=true;

	/** ������� �������ĵĴ�ֻsid��num */
	int upgradeShipConsume[];
	/** ����ʱ�� */
	int upgradeTime;
	/** ������Դ */
	long upgradeResources[];
	
	/**ս����������*/
	int attackFactor;
	/** �������� */
	int deadPoint;
	/** ������� */
	int createPoint;
	
	/** ���ָ��������ֻ�Ĺ����� */
	public int getAttack(int num,Player player)
	{
		if(player==null) return (int)getAttack()*num;
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return (int)getAttack()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			getPlayerType(),PublicConst.ATTACK);
		AttrAdjustment.AdjustmentData data1=adjustment.getAdjustmentValue(
			Ship.ALL_SHIP,PublicConst.ATTACK);
		int percent=(data==null?0:data.percent)+(data1==null?0:data1.percent);
		float v=getAttack();
		return (int)(num*v+((double)num)
			*(v*percent/100+SeaBackKit.getEquipAttribute(PublicConst.ATTACK,
				false,getSid(),player)));
	}
	
	/** ���ָ��������ֻ�ķ����� */
	public int getDefence(int num,Player player)
	{
		if(player==null) return getDefence()*num;
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return getDefence()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			getPlayerType(),PublicConst.DEFENCE);
		if(data==null) return getDefence()*num;
		int v=getDefence();
		return (int)(num*v+((double)num)
			*(v*data.percent/100+SeaBackKit.getEquipAttribute(
				PublicConst.DEFENCE,false,getSid(),player)));
	}
	
	/** ��õ�֧��������ֵ */
	public int getShipLife(Player player)
	{
		if(player==null) return (int)getLife();
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return (int)getLife();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			getPlayerType(),PublicConst.SHIP_HP);
		AttrAdjustment.AdjustmentData data1=adjustment.getAdjustmentValue(
			Ship.ALL_SHIP,PublicConst.SHIP_HP);
		int percent=(data==null?0:data.percent)+(data1==null?0:data1.percent);
		float v=getLife();
		return (int)(v+v*percent/100+SeaBackKit.getEquipAttribute(
			PublicConst.SHIP_HP,false,getSid(),player));
	}

	/* properties */
	public int getDecritical()
	{
		return decritical;
	}
	public int getCritical()
	{
		return critical;
	}
	/** ��ûر� */
	public int getAvoid()
	{
		return avoid;
	}
	/** ��þ�׼ */
	public int getAccurate()
	{
		return accurate;
	}
	public int getNormalAbility()
	{
		return normalAbility;
	}
	public void setAbilityListInFight(Ability[] list)
	{
		fightAbility=list;
	}
	public Ability[] getAbilityListInFight()
	{
		return fightAbility;
	}
	/** ��ü����б� */
	public Ability[] getAilityList()
	{
		return abilityList;
	}
	/**
	 * @return life
	 */
	public float getLife()
	{
		return life;
	}
	/**
	 * @param life Ҫ���õ� life
	 */
	public void setLife(int life)
	{
		this.life=life;
	}
	/**
	 * @return buildTime
	 */
	public int getBuildTime()
	{
		return buildTime;
	}
	/**
	 * @param buildTime Ҫ���õ� buildTime
	 */
	public void setBuildTime(int buildTime)
	{
		this.buildTime=buildTime;
	}
	/** ��ù����� */
	public float getAttack()
	{
		return attack;
	}
	/** ��÷����� */
	public int getDefence()
	{
		return defence;
	}
	/**
	 * @return resources
	 */
	public long[] getCostResources()
	{
		return costResources;
	}
	/**
	 * @param resources Ҫ���õ� resources
	 */
	public void setCostResources(long[] resources)
	{
		this.costResources=resources;
	}

	/* methods */
	public Object copy(Object o)
	{
		Ship ship=(Ship)o;
		if(abilitySid!=null&&abilitySid.length>0)
		{
			ship.abilityList=new Ability[abilitySid.length];
			for(int i=0;i<abilitySid.length;i++)
			{
				ship.abilityList[i]=(Ability)FightScene.abilityFactory
					.newSample(abilitySid[i]);
			}
		}
		return o;
	}
	/**
	 * ���ָ���Ŀ���
	 * 
	 * @param index ����
	 * @return ���ؿ���
	 */
	public int getResist(int index)
	{
		return resist[index];
	}
	/** �Ƿ��Ǵ�ֻ ���ǳǷ� */
	public boolean isMoveShips()
	{
		return playerType==BATTLE_SHIP||playerType==SUBMARINE_SHIP
			||playerType==CRUISER_SHIP||playerType==AIRCRAFT_SHIP;
	}
	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		return this;
	}

	public void finish(Player player,Product product,CreatObjectFactory objectFactory)
	{
		player.getIsland().addTroop(getSid(),product.getNum(),
			player.getIsland().getTroops());
		// �Զ��������
		player.autoAddMainGroup();
		// �¼�֪ͨ����
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.SHIP_PRODUCE_TASK_EVENT,product,player,objectFactory);
		//��ֻ��־
		IntList list = new IntList();
		list.add(getSid());
		list.add(product.getNum());
		objectFactory.addShipTrack(0,ShipCheckData.FINLISH_SHIP_PRODUCE,player,list,null,false);
		//ս������
		WarManicActivity activity=(WarManicActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
		if(activity!=null&&activity.isActive(TimeKit.getSecondTime()))
		{
			activity.addPScore(WarManicActivity.ARMS,
				getCreatePoint()*product.getNum(),player);
		}
	}

	public void cancel(Player player,Product product)
	{
		// TODO �Զ����ɷ������
		float scroe=(float)(product.getFinishTime()-TimeKit.getSecondTime())
			/(float)(buildTime*product.getNum());
		if(product.getFinishTime()==0) scroe=1;
		if(scroe<0) 
		{
			if(player!=null&&product!=null)
				log.error("cancelUp(),player="+player.getName()
					+",sid="+getSid()
					+",num="+product.getNum()
					+",finishTime="+product.getFinishTime()
					+",scroe="+scroe);
			else
				log.error("player="+player+",product="+product);
			return;
		}
		if(scroe>1) scroe=1;
		// ���ص���
		// sid,num����ʽ
		NormalProp prop;
		if(costPropSid!=null&&costPropSid.length>0)
		{
			for(int i=0;i<costPropSid.length;i+=2)
			{
				prop=(NormalProp)Prop.factory.newSample(costPropSid[i]);
				prop.setCount(costPropSid[i+1]*product.getNum());
				player.getBundle().incrProp(prop,true);
			}
		}
		/** ������Դ */
		Resources.addResources(player.getResources(),
			(long)(costResources[Resources.METAL]*scroe*product.getNum()),
			(long)(costResources[Resources.OIL]*scroe*product.getNum()),
			(long)(costResources[Resources.SILICON]*scroe*product.getNum()),
			(long)(costResources[Resources.URANIUM]*scroe*product.getNum()),
			(long)(costResources[Resources.MONEY]*scroe*product.getNum()),
			player);
	}

	public void cancelUp(Player player,Product product,CreatObjectFactory objectFactory)
	{
		float scroe=(float)(product.getFinishTime()-TimeKit.getSecondTime())
			/(float)(buildTime*product.getNum());
		if(product.getFinishTime()==0) scroe=1;
		if(scroe<0) 
		{
			if(player!=null&&product!=null)
				log.error("cancelUp(),player="+player.getName()
					+",sid="+getSid()
					+",num="+product.getNum()
					+",finishTime="+product.getFinishTime()
					+",scroe="+scroe);
			else
				log.error("player="+player+",product="+product);
			return;
		}
		if(scroe>1) scroe=1;
		// sid,num����ʽ
		if(upgradeShipConsume!=null&&upgradeShipConsume.length>0)
		{
			for(int i=0;i<upgradeShipConsume.length;i+=2)
			{
				player.getIsland().addTroop(upgradeShipConsume[i],
					upgradeShipConsume[i+1]*product.getNum(),null);
			}
		}
		// ���ص���
		// sid,num����ʽ
		NormalProp prop;
		if(costPropSid!=null&&costPropSid.length>0)
		{
			for(int i=0;i<costPropSid.length;i+=2)
			{
				prop=(NormalProp)Prop.factory.newSample(costPropSid[i]);
				prop.setCount(costPropSid[i+1]*product.getNum());
				player.getBundle().incrProp(prop,true);
			}
		}
		// ������Դ
		Resources.addResources(player.getResources(),
			(long)(upgradeResources[Resources.METAL]*scroe*product.getNum()),
			(long)(upgradeResources[Resources.OIL]*scroe*product.getNum()),
			(long)(upgradeResources[Resources.SILICON]*scroe*product.getNum()),
			(long)(upgradeResources[Resources.URANIUM]*scroe*product.getNum()),
			(long)(upgradeResources[Resources.MONEY]*scroe*product.getNum()),
			player);
		JBackKit.sendResetTroops(player);
		player.autoAddMainGroup();
		JBackKit.resetMainGroup(player);
		
		IntList list = new IntList();
		list.add(getSid());
		list.add(product.getNum());
		objectFactory.addShipTrack(0,ShipCheckData.CANCEL_SHIP_PRODUCE,player,list,null,false);
	}

	/**
	 * @return propSid
	 */
	public int[] getCostPropSid()
	{
		return costPropSid;
	}

	/**
	 * @param propSid Ҫ���õ� propSid
	 */
	public void setCostPropSid(int[] propSid)
	{
		this.costPropSid=propSid;
	}

	/**
	 * @return attackType
	 */
	public int getAttackType()
	{
		return attackType;
	}

	/**
	 * @param attackType Ҫ���õ� attackType
	 */
	public void setAttackType(int attackType)
	{
		this.attackType=attackType;
	}

	/**
	 * @return carryResource
	 */
	public int getCarryResource()
	{
		return carryResource;
	}

	/**
	 * @param carryResource Ҫ���õ� carryResource
	 */
	public void setCarryResource(int carryResource)
	{
		this.carryResource=carryResource;
	}

	/**
	 * @return glodCost
	 */
	public int getGlodCost()
	{
		return goldCost;
	}

	/**
	 * @param glodCost Ҫ���õ� glodCost
	 */
	public void setGlodCost(int goldCost)
	{
		this.goldCost=goldCost;
	}

	/**
	 * @return gemRepair
	 */
	public float getGemRepair()
	{
		return gemRepair;
	}

	/**
	 * @param gemRepair Ҫ���õ� gemRepair
	 */
	public void setGemRepair(float gemRepair)
	{
		this.gemRepair=gemRepair;
	}

	public int getExp()
	{
		return exp;
	}

	public void setExp(int exp)
	{
		this.exp=exp;
	}

	public long[] getUpgradeResources()
	{
		return upgradeResources;
	}

	public void setUpgradeResources(long[] upgradeResources)
	{
		this.upgradeResources=upgradeResources;
	}

	public int getUpgradeTime()
	{
		return upgradeTime;
	}

	public void setUpgradeTime(int upgradeTime)
	{
		this.upgradeTime=upgradeTime;
	}

	public int[] getUpgradeShipConsume()
	{
		return upgradeShipConsume;
	}

	public void setUpgradeShipConsume(int[] upgradeShipConsume)
	{
		this.upgradeShipConsume=upgradeShipConsume;
	}

	
	public int getAttackFactor()
	{
		return attackFactor;
	}

	
	public void setAttackFactor(int attackFactor)
	{
		this.attackFactor=attackFactor;
	}
	
	public int getDeadPoint()
	{
		return deadPoint;
	}
	
	public void setDeadPoint(int deadPoint)
	{
		this.deadPoint=deadPoint;
	}

	public int getCreatePoint()
	{
		return createPoint;
	}

	public void setCreatePoint(int createPoint)
	{
		this.createPoint=createPoint;
	}

	
	public boolean isAttack()
	{
		return isAttack;
	}

	
	public void setAttack(boolean isAttack)
	{
		this.isAttack=isAttack;
	}

	/** ����Ѫ�� */
	public void addLife(float n)
	{
		if(n<=0)return;
		life+=n;
	}
	/** ���ӹ����� */
	public void addAttack(float n)
	{
		if(n<=0)return;
		attack+=n;
	}
	/** ���ý��������� Ѫ�� */
	public void resetShip()
	{
		Ship ship=(Ship)factory.getSample(getSid());
		attack=ship.getAttack();
		life=ship.getLife();
	}
}