/**
 * 
 */
package foxu.sea.fight;

import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.Comparator;
import mustang.set.IntList;
import mustang.set.SetKit;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.fight.Fighter;
import foxu.sea.AttrAdjustment;
import foxu.sea.BuffService;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Role;
import foxu.sea.Service;
import foxu.sea.Ship;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.FleetAttrAdjustment;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerInfo;
import foxu.sea.officer.OfficerManager;
import foxu.sea.service.ServiceAbility;

/**
 * 舰队
 * 
 * @author rockzyt
 */
public class Fleet implements Cloneable,Comparator
{

	/* static fields */
	/** 影响舰船的buff */
	public static int[] BUFF_TYPE={PublicConst.ADD_HURT_BUFF,
		PublicConst.REDUCE_HURT_BUFF,PublicConst.ADD_ACCURATE_BUFF,
		PublicConst.ADD_AVOID_BUFF,PublicConst.ADD_CRITICAL_BUFF,
		PublicConst.ADD_CRITICAL_RESIST};

	/* static fields */
	public static int compteShipNum(Fleet f,float hp)
	{
		if(hp<=0) return 0;
		return (int)Math.ceil(hp/f.getShipLife());// 有余数,说明还有一只船未被破坏
	}

	/* fields */
	/** 当前数量 */
	int num;
	/** 当前生命值 */
	int hp;
	/** 位置 */
	int location;
	/** 用于战斗后计算伤兵 伤兵计算完成后 此值设为当前数量 */
	int lastNum;
	/** 舰队初始数量 事件舰队或者主力舰队的最初始值 用于城防舰队恢复 */
	int startNum;

	/* dynmaic fields */
	/** 参战者 */
	FleetFighter fighter;
	/** 船只 */
	Ship ship;
	/** 舰队关联的player,为了避免数据回档,只能对这个player做读操作,不能修改其中的任何数据 */
	private Player player;
	/** 随军属性 */
	FleetAttrAdjustment fleetAdjust=new FleetAttrAdjustment();
	/** 无吃药buff 进入战斗*/
	boolean buffEffect=true;
	/**可以改变的 buff(值也可以改变)**/
	/**sid-buff属性**/
	IntList buff=new IntList();

	/* constructor */
	public Fleet()
	{

	}

	public Fleet(Ship ship,int num)
	{
		if(ship==null||num<=0)
			throw new IllegalArgumentException("change fleet err, num="+num
				+",ship="+ship);
		this.ship=ship;
		this.num=num;
		lastNum=num;
		startNum=num;
	}
	/** 计算伤兵经验值 */
	public int hurtExp(Player player,int time)
	{
		int hurtNum=lostNum();
		int exp=hurtNum*ship.getExp();
		return exp;
	}

	/** 伤兵计算 */
	public void hurtTroops(Player player,int time)
	{
		int hurtNum=lostNum();
		/** 减去真正丢失的 */
		int lost=hurtNum
			*(100-PublicConst.PLAYER_RESET_HURT_SHIPS[player.getLevel()-1])
			/100;
		hurtNum=hurtNum-lost;
		if(hurtNum<=0) return;
		// 先push下伤兵
		resetLastNum();
		player.getIsland().addHurtTroop(ship.getSid(),hurtNum,time);
	}

	/** 添加兵力 不改变最初设置的兵力最大值 */
	public void addNum(int num)
	{
		this.num+=num;
	}

	/** boss战回复兵力 */
	public void resetBossShips()
	{
		num=lastNum;
	}

	/** 减少兵力 返回减少的兵力 */
	public int reduceNum(int num)
	{
		if(num<=this.num)
		{
			this.num-=num;
			return num;
		}
		else
		{
			num=this.num;
			this.num=0;
			return num;
		}
	}

	public void resetLastNum()
	{
		lastNum=num;
	}

	/** 本只船队本次战斗损失的兵力 */
	public int lostNum()
	{
		return (lastNum-num);
	}

	/** 本次应该自动补充的兵力 */
	public int lostAutoNum()
	{
		if(startNum<num)
		{
			num=startNum;
			return 0;
		}
		return (startNum-num);
	}
	
	/** 获取死亡积分 */
	public int getDeadPoint()
	{
		return lostNum()
			*(100-PublicConst.PLAYER_RESET_HURT_SHIPS[player.getLevel()-1])
			/100*ship.getDeadPoint();
	}

	/* properties */
	public void setPlayter(Player player)
	{
		this.player=player;
	}
	public int getLocation()
	{
		return location;
	}
	/** 设置位置 */
	public void setLocation(int loc)
	{
		location=loc;
		getFighter().setLocation(loc);
	}
	public FleetFighter getFighter()
	{
		if(fighter==null)
		{
			fighter=new FleetFighter();
			fighter.init(-1);
			fighter.setFleet(this);
			fighter.setLocation(getLocation());
		}
		return fighter;
	}
	public int getDecritical()
	{
		if(player==null) return ship.getDecritical();
		AttrAdjustment adjustment=player.getAdjstment();
		AttrAdjustment.AdjustmentData data=null;
		if(adjustment!=null)
			data=adjustment.getAdjustmentValue(ship.getPlayerType(),
				PublicConst.CRITICAL_HIT_RESIST);
		int percent=0;
		if(data!=null) percent+=data.percent;
		int v=ship.getDecritical();
		return (int)((v
			+percent
			+(int)SeaBackKit.getEquipAttribute(
				PublicConst.CRITICAL_HIT_RESIST,false,ship.getSid(),player)
			+(int)SeaBackKit.getOfficerAttribute(
				PublicConst.CRITICAL_HIT_RESIST,this))*getDeBuff(PublicConst.CRITICAL_HIT_RESIST));
	}
	public int getCritical()
	{
		if(player==null) return ship.getCritical();
		AttrAdjustment adjustment=player.getAdjstment();
		AttrAdjustment.AdjustmentData data=null;
		if(adjustment!=null)
			data=adjustment.getAdjustmentValue(ship.getPlayerType(),
				PublicConst.CRITICAL_HIT);
		int percent=0;
		if(data!=null) percent+=data.percent;
		int v=ship.getCritical();
		return (int)((v
			+percent
			+(int)SeaBackKit.getEquipAttribute(PublicConst.CRITICAL_HIT,
				false,ship.getSid(),player)
			+(int)SeaBackKit.getOfficerAttribute(PublicConst.CRITICAL_HIT,
				this))*getDeBuff(PublicConst.CRITICAL_HIT));
	}
	public int getType()
	{
		return ship.getPlayerType();
	}
	public int getLevel()
	{
		return ship.getLevel();
	}
	public int getNumber()
	{
		return num;
	}
	public void setNumber(int num)
	{
		this.num=num;
	}
	public int getHp()
	{
		return (int)(hp*getDeBuff(PublicConst.SHIP_HP));
	}
	public void setHp(int hp)
	{
		if(ship==null) this.hp=0;
		this.hp=hp;
		num=Fleet.compteShipNum(this,hp);
	}
	public void setShip(Ship ship)
	{
		this.ship=ship;
	}
	/** 获得船只范本 */
	public Ship getShip()
	{
		return ship;
	}
	/** 获得单支舰船生命值 */
	public float getShipLife()
	{
		if(player==null) return ship.getLife();
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return ship.getLife();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.SHIP_HP);
		AttrAdjustment.AdjustmentData data1=adjustment.getAdjustmentValue(
			Ship.ALL_SHIP,PublicConst.SHIP_HP);
		int percent=(data==null?0:data.percent)
			+(data1==null?0:data1.percent);
		float v=ship.getLife();
		return (float)((v+v*percent/100)
			+SeaBackKit.getEquipAttribute(PublicConst.SHIP_HP,false,
				ship.getSid(),player)+SeaBackKit.getOfficerAttribute(PublicConst.SHIP_HP,this));
	}

	/* methods */
	/** 战斗初始化 */
	public void fightInit()
	{
		hp=maxHp();
	}
	/** 获得舰队攻击力 */
	public int attack()
	{
		return attack(num);
	}
	/** 获得指定数量船只的攻击力 */
	public int attack(int num)
	{
		if(player==null) return (int)ship.getAttack()*num;
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return (int)ship.getAttack()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.ATTACK);
		AttrAdjustment.AdjustmentData data1=adjustment.getAdjustmentValue(
			Ship.ALL_SHIP,PublicConst.ATTACK);
		int percent=(data==null?0:data.percent)
						+(data1==null?0:data1.percent);
		float v=ship.getAttack();
		return (int)((num*v+((double)num)*(v*percent/100+v*SeaBackKit.getProsperityAttBuff(player)+SeaBackKit.getEquipAttribute(PublicConst.ATTACK,false,
			ship.getSid(),player)+SeaBackKit.getOfficerAttribute(PublicConst.ATTACK,this)))*getDeBuff(PublicConst.ATTACK));
	}
	/** 获得舰队防御力 */
	public int defence()
	{
		return defence(num);
	}
	/** 获得指定数量船只的防御力 */
	public int defence(int num)
	{
		if(player==null) return ship.getDefence()*num;
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return ship.getDefence()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.DEFENCE);
		int percent=(data==null?0:data.percent);
		int v=ship.getDefence();
		return (int)((num*v+((double)num)
			*(v*percent/100+SeaBackKit.getEquipAttribute(
				PublicConst.DEFENCE,false,ship.getSid(),player)
				+SeaBackKit.getOfficerAttribute(PublicConst.DEFENCE,this)))*getDeBuff(PublicConst.DEFENCE));
	}
	/** 获得回避 */
	public int getAvoid()
	{
		if(player==null) return ship.getAvoid();
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return ship.getAvoid();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.AVOID);
		int percent=(data==null?0:data.percent);
		int v=ship.getAvoid();
		return (int)((v
			+percent
			+(int)SeaBackKit.getEquipAttribute(PublicConst.AVOID,false,
				ship.getSid(),player)
			+(int)+SeaBackKit.getOfficerAttribute(PublicConst.AVOID,this))*getDeBuff(PublicConst.AVOID));
	}

	/** 获得精准 */
	public int getAccurate()
	{
		if(player==null) return ship.getAccurate();
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return ship.getAccurate();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.ACCURATE);
		int percent=(data==null?0:data.percent);
		int v=ship.getAccurate();
		return (int)((v
			+percent
			+(int)SeaBackKit.getEquipAttribute(PublicConst.ACCURATE,false,
				ship.getSid(),player)
			+(int)SeaBackKit.getOfficerAttribute(PublicConst.ACCURATE,this))*getDeBuff(PublicConst.ACCURATE));
	}
	/**
	 * 改变舰队
	 * 
	 * @param ship 船只
	 * @param num 数量
	 */
	public void change(int shipSid,int num)
	{
		if(num<=0||shipSid<=0)
			throw new IllegalArgumentException("change fleet err, num="+num
				+",shipSid="+shipSid);
		ship=(Ship)Role.factory.newSample(shipSid);
		if(ship==null)
			throw new IllegalArgumentException("change fleet err shipSid="
				+shipSid);
		this.num=num;
	}
	/** 获得舰队最大HP */
	public int maxHp()
	{
		if(player==null) return (int)ship.getLife()*num;
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return (int)ship.getLife()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.SHIP_HP);
		AttrAdjustment.AdjustmentData data1=adjustment.getAdjustmentValue(
			Ship.ALL_SHIP,PublicConst.SHIP_HP);
		int percent=(data==null?0:data.percent)
			+(data1==null?0:data1.percent);
		float v=ship.getLife();
		return (int)(num*v+((double)num)
			*(v*percent/100+SeaBackKit.getEquipAttribute(
				PublicConst.SHIP_HP,false,ship.getSid(),player)
				+SeaBackKit.getOfficerAttribute(PublicConst.SHIP_HP,this)));
	}
	public void changeHp(int hp)
	{
		if(hp>0)
			incrHp(hp);
		else
			decrHp(hp);
	}
	/**
	 * 增加血量,不会复活船只,只会设置为当前数量计算出来的max
	 * 
	 * @param hp 增加的hp
	 */
	public void incrHp(int hp)
	{
		int max=maxHp();
		this.hp=hp>max?max:hp;
	}
	/**
	 * 减少hp,根据减少后的hp,重新计算了剩余的船只数量
	 * 
	 * @param hp 减少的hp
	 */
	public void decrHp(int hp)
	{
		this.hp=this.hp+hp;
		if(this.hp<=0)
		{
			this.hp=0;
			num=0;
		}
		else
		{
			num=compteShipNum(this,this.hp);// 有余数,说明还有一只船未被破坏
		}
	}
	/** 舰队进入战场,初始化战斗数据,传入IntList技能影响[技能sid,影响概率] */
	public void intoFightScene(IntList probList)
	{
		Fighter f=getFighter();
		f.init(-1);// 重新分配id,将就前台....
		f.clear();
		if(player!=null)
		{
			if(buffEffect)
			{
				for(int i=BUFF_TYPE.length-1;i>=0;i--)
				{
					ServiceAbility sa=(ServiceAbility)player
						.getServiceByType(BUFF_TYPE[i]);
					if(sa==null) continue;
					Ability ability=sa.getAbility();
					fighter.addAbility(ability,0);
				}
			}
			// 增加 舰船等级技能影响
			float[] add=player.getLevelAbilityValue(ship.getSid());
			if(add!=null)
			{
				ship.resetShip();
				ship.addLife(add[0]);
				ship.addAttack(add[1]);
			}
		}
		// 增加光环技能,初始化主动技能使用几率
		Ability[] abilitys=ship.getAilityList();
		if(abilitys!=null&&abilitys.length>0)
		{
			Ability ability=null;
			ArrayList list=new ArrayList();
			int probability=0;
			for(int i=abilitys.length-1;i>=0;i--)
			{
				if(abilitys[i].getType()==Ability.ETERNAL
					||abilitys[i].getType()==Ability.PASSIVE)
				{
					ability=(Ability)abilitys[i].clone();
					ability.getSpread().setSpreadSource(fighter.getUid());
					fighter.addAbility(ability,0);
				}
				else if(abilitys[i].getType()==Ability.ATTACK)
				{
					list.add(abilitys[i]);
					probability+=((Skill)abilitys[i]).getProbability();
				}
			}
			if(player!=null)
			{
				IntList skillList=player.getAllianceList();
				for(int i=0;i<skillList.size();i+=2)
				{
					int sid=skillList.get(i);
					AllianceSkill aAbility=(AllianceSkill)FightScene.abilityFactory
						.getSample(sid);
					// 仅对作用友方的技能进行处理
					if(aAbility.getShipType()==ship.getPlayerType()
						&&!aAbility.isEffectEnemySkill())
					{
						aAbility=(AllianceSkill)aAbility.clone();
						aAbility.setLevel(skillList.get(i+1));
						probability+=aAbility.getProbability();
						list.add(aAbility);
					}
				}
			}
			probability=initFleetSkill(probList,list,probability);
			Object[] objs=list.toArray();
			// TODO 技能选择逻辑需要重写,多技能时触发概率不正确
			SetKit.sort(objs,this);
			int[] probabilityList=new int[objs.length*2];
			Skill skill=null;
			Ability[] tempAbilitys=new Ability[objs.length];
			for(int i=probabilityList.length-1,j=0;i>=0;i-=2,j++)
			{
				skill=(Skill)objs[j];
				tempAbilitys[j]=skill;
				int prob=skill.getProbability();
				for(int k=0;k<probList.size();k++)
				{
					if(probList.get(k)==skill.getSid())
					{
						prob+=probList.get(k+1);
						break;
					}
				}
				if(probability>FightScene.RANDOM_MAX-1)
				{
					probabilityList[i]=prob*(FightScene.RANDOM_MAX-1)
						/probability;
				}
				else
				{
					probabilityList[i]=prob;
				}
				probabilityList[i-1]=skill.getSid();
			}
			fighter.setSelectAbility(probabilityList);
			ship.setAbilityListInFight(tempAbilitys);
		}
	}
	
	/**
	 * 装载军官技能
	 */
	public void intoOfficerSkill(FleetGroup fg){
			OfficerInfo[] officerInfos = fg.getOfficerFleetAttr().getUsedOfficers();
			if(officerInfos!=null&&officerInfos.length>0){
				OfficerInfo of = officerInfos[fighter.getLocation()];//获取对应坑位的军官
				if(of!=null){
					Officer officer = (Officer)OfficerManager.factory.getSample(of.sid);
					if(officer!=null){
						fighter.setOfficerLv(of.militaryRank);
						int[] skillsSid = officer.getOfficerSkill();
						if(skillsSid!=null){
							for(int i=0;i<skillsSid.length;i++){
								//System.out.println("加载了军官技能："+skillsSid[i]);
								fighter.addAbility((Ability)FightScene.abilityFactory.newSample(skillsSid[i]),0);//添加军官触发技能
							}
						}
					}
				}
			}
	}
	
	public int compare(Object o1,Object o2)
	{
		Skill skill1=(Skill)o1;
		Skill skill2=(Skill)o2;
		if(skill1.getProbability()<skill2.getProbability())
			return Comparator.COMP_LESS;
		if(skill1.getProbability()>skill2.getProbability())
			return Comparator.COMP_GRTR;
		return Comparator.COMP_EQUAL;
	}
	/**
	 * 获得指定的抗性
	 * 
	 * @param index 类型
	 * @return 返回抗性
	 */
	public int getResist(int index)
	{
		return ship.getResist(index);
	}
	
	/** 获取盟战残余兵力 */
	public void getResidualShip(IntList list,int losePecr,IntList loss)
	{
		list.add(location);
		list.add(ship.getSid());
		int residual=startNum-(int)Math.ceil((startNum-num)*losePecr/(double)100);
		list.add(residual);
		if(startNum-residual>0)
		{
			loss.add(ship.getSid());
			loss.add(startNum-residual);
		}
	}

	public Object copy(Object obj)
	{
		Fleet f=(Fleet)obj;
		if(ship!=null) f.ship=(Ship)ship.clone();
		f.player=player;
		f.fighter=null;
		return obj;
	}

	public Object clone()
	{
		try
		{
			return copy(super.clone());
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException(getClass().getName()+" clone, "+e);
		}
	}

	public void bytesWrite(ByteBuffer data)
	{
		ship.bytesWrite(data);
		data.writeShort(num);
		data.writeByte(location);
		data.writeShort(lastNum);
		data.writeShort(startNum);
	}
	public Object bytesRead(ByteBuffer data)
	{
		ship=(Ship)Ship.bytesReadRole(data);
		num=data.readUnsignedShort();
		location=data.readUnsignedByte();
		lastNum=data.readUnsignedShort();
		startNum=data.readUnsignedShort();
		return this;
	}

	public void showBytesWrite(ByteBuffer data)
	{
		ship.bytesWrite(data);
		data.writeShort(num);
		data.writeByte(location);
	}

	public void showFightBytesWirte(ByteBuffer data)
	{
		ship.bytesWrite(data);
		data.writeShort(num);
		data.writeInt(hp);
	}

	/**
	 * @return maxNum
	 */
	public int getLastNum()
	{
		return lastNum;
	}

	/**
	 * @param maxNum 要设置的 maxNum
	 */
	public void setLastNum(int lastNum)
	{
		this.lastNum=lastNum;
	}

	/**
	 * @return num
	 */
	public int getNum()
	{
		return num;
	}

	public void initNum(int num)
	{
		this.num=num;
		lastNum=num;
		startNum=num;
	}

	/**
	 * @param num 要设置的 num
	 */
	public void setNum(int num)
	{
		this.num=num;
	}

	public int getStartNum()
	{
		return startNum;
	}

	public void setStartNum(int startNum)
	{
		this.startNum=startNum;
	}
	
	/** 获取玩家所有已装备的物件提升的抗性 */
	public int getEquipResist(int attr){
		if(player==null) return 0;
		return player.getEquips().getEquipedResist(ship.getPlayerType())[attr];
	}
	
	/** 获取玩家所有已装备的物件提升的加成 */
	public int getEquipAttach(int attr){
		if(player==null||attr<=0) return 0;
		int shipTypeIndex=0;
		while(attr!=1){
			attr=attr>>1;
			shipTypeIndex++;
		}
		return player.getEquips().getEquipedAttach(ship.getPlayerType())[shipTypeIndex];
	}
	
	/** 重置概率 */
	public void resetAbilityProbability(int effectSid,int probability)
	{
		int[] skills=fighter.getSelectAbility();
		for(int i=0;i<skills.length;i+=2)
		{
			if(effectSid==skills[i])
			{
				skills[i+1]+=probability;
				break;
			}
		}
	}
	
	public IntList getSkillList()
	{
		if(player!=null)
			return player.getAllianceList();
		return null;
	}
	
	public AttrAdjustment getAttrAdjustment()
	{
		if(player!=null)
			return player.getAdjstment();
		return null;
	}

	/** 重置随军属性 */
	public void resetFleetAdjust(int attrType,boolean isFix,float value)
	{
		fleetAdjust.add(attrType,value,isFix);
	}
	
	/** 清空随军属性 */
	public void clearFleetAdjust()
	{
		fleetAdjust.clear();
	}

	/** 初始化随军技能,移除不可用的技能并返回重置后的总概率 */
	public int initFleetSkill(IntList list,ArrayList skillList,int prob)
	{
		int resetProb=0;
		for(int i=0;i<list.size();i+=2)
		{
			Ability updateAbility=(Ability)FightScene.abilityFactory
				.newSample(list.get(i));
			int updateProbability=list.get(i+1);
			// 被动技能
			if(updateAbility.getType()==Ability.ETERNAL
				||updateAbility.getType()==Ability.PASSIVE)
			{
				updateAbility.getSpread().setSpreadSource(fighter.getUid());
				fighter.addAbility(updateAbility,0);
			}
			else
			{
				Ability ability;
				int j=0;
				for(;j<skillList.size();j++)
				{
					ability=(Ability)skillList.get(j);
					// 如果需要重置的概率使当前技能不可用,则移除
					if(ability.getSid()==updateAbility.getSid())
					{
						Skill skill=(Skill)ability;
						if(skill.getProbability()
							+updateProbability<=0)
						{
							resetProb+=skill.getProbability();
							skillList.remove(j);
						}
						break;
					}
				}
				// 如果没有这个技能就添加
				if(j>=skillList.size()&&updateProbability>0)
				{
					skillList.add(updateAbility);
					prob+=updateProbability;
				}
			}
		}
		return prob-resetProb;
	}
	
	/** 获取随军属性 */
	public double getFleetAttrAdjustment(int type,boolean isFix)
	{
		FleetAttrAdjustment.AdjustmentData fd=fleetAdjust
			.getAdjustmentValue(type);
		if(fd==null) return 0;
		if(isFix) return fd.fix;
		return fd.percent;
	}

	public int getCarryResource()
	{
		if(player==null) return ship.getCarryResource()*num;
		AttrAdjustment adjustment=player.getAdjstment();
		if(adjustment==null) return (int)ship.getCarryResource()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.EXTRA_CARRY);
		AttrAdjustment.AdjustmentData data1=adjustment.getAdjustmentValue(
			Ship.ALL_SHIP,PublicConst.EXTRA_CARRY);
		int percent=(data==null?0:data.percent)
						+(data1==null?0:data1.percent);
		int v=ship.getCarryResource();
		return (int)(num*v+(double)v*percent*num/100);
	}
	
	/** 是否可以主动攻击 */
	public boolean isAttack()
	{
		return ship.isAttack();
	}
	
	public boolean isBuffEffect()
	{
		return buffEffect;
	}

	
	public void setBuffEffect(boolean buffEffect)
	{
		this.buffEffect=buffEffect;
	}
	
	/**根据类型获取计算所有的各种属性buff**/
	public float getDeBuff(int type)
	{
		if(buff==null || buff.size()==0)
			return 1.0f;
		int percent=100;
		for(int i=0;i<buff.size();i++)
		{
			BuffService service=(BuffService)Service.factory.getSample(buff.get(i));
			if(service==null || !SeaBackKit.isContainValue(service.getFullAttribute(),type)) continue;
			percent+=service.getValue();
		}
		if(percent<0) percent=0;
		return percent/100f;
	}

	
	public IntList getBuff()
	{
		return buff;
	}

	
	public void setBuff(IntList buff)
	{
		this.buff=buff;
	}

	
}