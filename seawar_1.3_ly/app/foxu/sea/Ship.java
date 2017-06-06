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

/** 船 */
public class Ship extends Role implements Produceable
{
	/* static fields */
	private static Logger log=LogFactory.getLogger(Ship.class);
	
	/**
	 * ATTACK=100攻击,DEFENCE=101防御,ACCURATE=102精准,AVOID=103回避,SHIP_NUM=104舰船数量
	 * ,HP=105生命,CRITICAL_HIT=106暴击, CRITICAL_HIT_RESIST=107暴击抵抗
	 */
	public static final int ATTACK=100,DEFENCE=101,ACCURATE=102,AVOID=103,
					SHIP_NUM=104,HP=105,CRITICAL_HIT=106,
					CRITICAL_HIT_RESIST=107;
	/**
	 * 船类型 SHIPS_1战列舰 SHIPS_2潜艇 SHIPS_3巡洋舰 SHIPS_4航母 POSITION_AIR=空军基地
	 * POSITION_MISSILE=导弹基地 POSITION_FIRE=火炮阵地 放的位置火炮 空军 导弹 6,7,8
	 */
	public static final int BATTLE_SHIP=1,SUBMARINE_SHIP=2,CRUISER_SHIP=4,
					AIRCRAFT_SHIP=8,POSITION_AIR=16,POSITION_MISSILE=32,
					POSITION_FIRE=64;
	/** 通用类型 （该类型代表所有类型船只）*/
	public static final int ALL_SHIP=15;
	
	/** AIR_RAID空袭,ARTILLERY炮火,MISSILE导弹,TORPEDO鱼雷,NUCLEAR核 */
	public static final int AIR_RAID=0,ARTILLERY=1,MISSILE=2,TORPEDO=3,
					NUCLEAR=4;

	/* configure fileds */
	/** 生命值 */
	float life;
	/** 攻击类型 */
	int attackType;
	/** 每只船建筑时间 */
	int buildTime;
	/** 建筑获取的经验 */
	int buildExperience;
	/** 普通攻击技能 */
	int normalAbility;
	/** 技能sid列表 */
	int[] abilitySid;
	/** 宝石修复价钱 */
	float gemRepair;
	/** 金币修复 */
	int goldCost;
	/** 资源携带量 */
	int carryResource;
	/** 主动方攻击成功后获得的经验值 */
	int exp;

	/** 消耗配置 */
	long costResources[];
	/** 需要消耗的物品sid,num,sid,num */
	int costPropSid[];
	/** 技能列表 */
	Ability[] abilityList;
	/** 战斗技能列表 */
	Ability[] fightAbility;
	/** 防御 */
	int defence;
	/** 攻击力 */
	float attack;
	/** 精准 */
	int accurate;
	/** 回避 */
	int avoid;
	/** 暴击率 */
	int critical;
	/** 暴击抗性 */
	int decritical;
	/** 抗性列表 */
	int[] resist;
	/** 是否可以主动攻击 */
	boolean isAttack=true;

	/** 升级相关 升级消耗的船只sid，num */
	int upgradeShipConsume[];
	/** 升级时间 */
	int upgradeTime;
	/** 升级资源 */
	long upgradeResources[];
	
	/**战力计算因子*/
	int attackFactor;
	/** 死亡积分 */
	int deadPoint;
	/** 建造积分 */
	int createPoint;
	
	/** 获得指定数量船只的攻击力 */
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
	
	/** 获得指定数量船只的防御力 */
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
	
	/** 获得单支舰船生命值 */
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
	/** 获得回避 */
	public int getAvoid()
	{
		return avoid;
	}
	/** 获得精准 */
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
	/** 获得技能列表 */
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
	 * @param life 要设置的 life
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
	 * @param buildTime 要设置的 buildTime
	 */
	public void setBuildTime(int buildTime)
	{
		this.buildTime=buildTime;
	}
	/** 获得攻击力 */
	public float getAttack()
	{
		return attack;
	}
	/** 获得防御力 */
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
	 * @param resources 要设置的 resources
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
	 * 获得指定的抗性
	 * 
	 * @param index 类型
	 * @return 返回抗性
	 */
	public int getResist(int index)
	{
		return resist[index];
	}
	/** 是否是船只 不是城防 */
	public boolean isMoveShips()
	{
		return playerType==BATTLE_SHIP||playerType==SUBMARINE_SHIP
			||playerType==CRUISER_SHIP||playerType==AIRCRAFT_SHIP;
	}
	/** 将对象的域序列化到字节缓存中 */
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
		// 自动补充兵力
		player.autoAddMainGroup();
		// 事件通知任务
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.SHIP_PRODUCE_TASK_EVENT,product,player,objectFactory);
		//船只日志
		IntList list = new IntList();
		list.add(getSid());
		list.add(product.getNum());
		objectFactory.addShipTrack(0,ShipCheckData.FINLISH_SHIP_PRODUCE,player,list,null,false);
		//战争狂人
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
		// TODO 自动生成方法存根
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
		// 返回道具
		// sid,num的形式
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
		/** 返回资源 */
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
		// sid,num的形式
		if(upgradeShipConsume!=null&&upgradeShipConsume.length>0)
		{
			for(int i=0;i<upgradeShipConsume.length;i+=2)
			{
				player.getIsland().addTroop(upgradeShipConsume[i],
					upgradeShipConsume[i+1]*product.getNum(),null);
			}
		}
		// 返回道具
		// sid,num的形式
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
		// 返回资源
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
	 * @param propSid 要设置的 propSid
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
	 * @param attackType 要设置的 attackType
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
	 * @param carryResource 要设置的 carryResource
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
	 * @param glodCost 要设置的 glodCost
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
	 * @param gemRepair 要设置的 gemRepair
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

	/** 增加血量 */
	public void addLife(float n)
	{
		if(n<=0)return;
		life+=n;
	}
	/** 增加攻击力 */
	public void addAttack(float n)
	{
		if(n<=0)return;
		attack+=n;
	}
	/** 重置舰船攻击力 血量 */
	public void resetShip()
	{
		Ship ship=(Ship)factory.getSample(getSid());
		attack=ship.getAttack();
		life=ship.getLife();
	}
}