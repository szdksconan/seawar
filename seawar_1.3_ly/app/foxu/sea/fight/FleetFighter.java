/**
 * 
 */
package foxu.sea.fight;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import foxu.fight.Ability;
import foxu.fight.ChangeAttributeEffect;
import foxu.fight.Effect;
import foxu.fight.FightScene;
import foxu.fight.Fighter;
import foxu.sea.PublicConst;
import foxu.sea.Ship;

/**
 * 战舰fighter
 * 
 * @author rockzyt
 */
public class FleetFighter extends Fighter
{

	/* fields */
	/** 战舰 */
	Fleet fleet;
	/** 战斗中的出手速度 */
	int speed;
	/** 当前出手回合数 */
	int currentRound;
	/** 待选技能列表 */
	int[] abilitySelectList;
	
	
	/** 最大血量  用于战斗技能计算 每次战斗前 设置*/
	int fighterMaxHp;
	/** 坑位护盾 用于战斗吸收伤害  */
	int shield;

	/* properties */
	/**
	 * 护盾抵消伤害
	 * @param hurt 护盾吸收前造成的伤害
	 * @return 吸收后的伤害
	 */
	public float reduceHurtByShiled(float hurt){
		if(shield<=0)return hurt;
		System.out.println("攻击到护盾的伤害值："+hurt);
		System.out.println("目标身上护盾值："+shield);
		float h = (int)hurt-shield;
		FightShowEventRecord OfficerChanger = (FightShowEventRecord)getChangeListener();
		if(h>0)
		{
			//伤害大于0  护盾消失
			OfficerChanger.changeForOfficer(this,FightShowEventRecord.MISS_SHIELD_TIME_OUT_TYPE,null,null);
			shield=0;
			return h;
		}
		//护盾还在 产生吸收效果
		OfficerChanger.changeForOfficer(this,FightShowEventRecord.MISS_SHIELD_TYPE,null,null);
		shield = shield-(int)hurt;
		return 0;
	}
	
	/** 设置出手速度 */
	public void setSpeed(int speed)
	{
		this.speed=speed;
	}
	
	public int getFighterMaxHp()
	{
		return fighterMaxHp;
	}
	
	public void setFighterMaxHp(int fighterMaxHp)
	{
		this.fighterMaxHp=fighterMaxHp;
	}
	
	public int getShield()
	{
		return shield;
	}
	
	public void setShield(int shield)
	{
		this.shield=shield;
	}

	public void setScene(FightScene s)
	{
		super.setScene(s);
		fleet.fightInit();
	}
	/** 设置舰队 */
	public void setFleet(Fleet f)
	{
		fleet=f;
	}
	public Fleet getFleet()
	{
		return fleet;
	}
	/** 设置待用技能列表 */
	public void setSelectAbility(int[] list)
	{
		abilitySelectList=list;
	}
	/** 获取待用技能列表 */
	public int[] getSelectAbility()
	{
		return abilitySelectList;
	}
	/* methods */
	public Skill getNomalAttack()
	{
		return (Skill)FightScene.abilityFactory.newSample(fleet.getShip()
			.getNormalAbility());
	}
	public float getSpeed()
	{
		return speed;
	}
	/**
	 * 计算属性数据
	 * 
	 * @param attr 属性常量
	 * @param v 当前值
	 * @return 返回加成过后的数据
	 */
	public float computAttrValue(int attr,int v)
	{
		Ability[] abilitys=getAbilityOnSelf();
		Effect[] effects=null;
		float value=0;
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(!abilitys[i].isEnabled()) continue;
			effects=abilitys[i].getEffects();
			for(int j=effects.length-1;j>=0;j--)
			{
				if(effects[j] instanceof ChangeAttributeEffect)
				{
					ChangeAttributeEffect effect=(ChangeAttributeEffect)effects[j];
					if(effect.checkType(attr,0))
					{
						if(effect.getValueType()==ChangeAttributeEffect.PRECENT)
						{
							value+=effect.getValue(this);
						}
						else
						{
							v+=effect.getValue(this);
						}
					}
				}
			}
		}
		return v+v*value/100;
	}
	public float getAttrValue(int attr)
	{
		if(attr==PublicConst.ATTACK)
		{
			return fleet.attack();
		}
		else if(attr==PublicConst.DEFENCE)
		{
			return fleet.defence(1);// 由于防御力的算法特殊,这个地方无法获得攻击方数量,只有返回一艘船的防御在外部计算整体防御
		}
		else if(attr==PublicConst.ACCURATE)
		{
			return computAttrValue(PublicConst.ACCURATE,fleet.getAccurate());
		}
		else if(attr==PublicConst.AVOID)
		{
			return computAttrValue(PublicConst.AVOID,fleet.getAvoid());
		}
		else if(attr==PublicConst.FLEET_HP)
		{
			return fleet.getHp();
		}
		else if(attr==PublicConst.SHIP_NUM)
		{
			return fleet.getNumber();
		}
		else if(attr==PublicConst.CRITICAL_HIT)
		{
			return computAttrValue(PublicConst.CRITICAL_HIT,
				fleet.getCritical());
		}
		else if(attr==PublicConst.CRITICAL_HIT_RESIST)
		{
			return computAttrValue(PublicConst.CRITICAL_HIT_RESIST,
				fleet.getDecritical());
		}
		else if(attr>=PublicConst.AIR_RAID&&attr<=PublicConst.NUCLEAR)
		{
			return computAttrValue(attr,fleet.getResist(attr));
		}
		else if(attr>=PublicConst.RESIST_AIR_RAID&&attr<=PublicConst.RESIST_NUCLEAR)
		{
			return fleet.getEquipResist(attr-PublicConst.RESIST_AIR_RAID);
		}
		else if(attr>=PublicConst.ATTACH_BATTLE&&attr<=PublicConst.ATTACH_AIRCRAFT)
		{
			return fleet.getEquipAttach(attr-PublicConst.ATTACH_BASE);
		}
		return 0;
	}
	public int getLevel()
	{
		return fleet.getLevel();
	}
	public float setDynamicAttr(int type,float value)
	{
		if(type==PublicConst.FLEET_HP)
		{
			//计算一下总血量 现在血量 减去 变化后的血量
			if(value<0)value=0;//再做一次 检测 有些地方没有检测
			this.getScene().getFighterContainer().reduceHp(this.getTeam(),fleet.getHp()-(int)value);
			fleet.setHp((int)value);
		}
		return value;
	}
	public int getFighterType()
	{
		return fleet.getType();
	}
	public boolean isDead()
	{
		return fleet.getNumber()==0;
	}
	public boolean isAttackFighter()
	{
		return fleet.isAttack();
	}
	/** 角色死亡，清理挂载的技能 */
	public void deadClear()
	{
		super.deadClear();
		getScene().removeFighter(this);
	}
	/** 准备行动,选择技能,选择目标 */
	public void getReady()
	{
		Ship ship=fleet.getShip();
		if(ship.getAbilityListInFight()==null
			||ship.getAbilityListInFight().length<=0)
		{
			useNormalSkill();
		}
		else
		{
			Skill skill=findSkill(ship.getAbilityListInFight());
			if(skill==null)
			{
				useNormalSkill();
			}
			else
			{
				Object target=findTarget(skill);
				if(target==null)
				{
					useNormalSkill();
				}
				else
				{
					setAct(skill,target);
				}
			}
		}
	}
	/** 使用普通攻击行动 */
	public void useNormalSkill()
	{
		Skill skill=getNomalAttack();
		Object target=findTarget(skill);
		setAct(skill,target);
	}
	/**
	 * 选择一个技能
	 * 
	 * @return 返回选择技能sid,如果返回0表示没有找到技能
	 */
	public int selectAbility()
	{
		if(abilitySelectList==null||abilitySelectList.length<=0) return 0;
		int value=getScene().getRandom().randomValue(FightScene.RANDOM_MINI,
			FightScene.RANDOM_MAX);
		int[] temp=abilitySelectList;
		for(int i=0;i<temp.length;i+=2)
		{
			if(value<temp[i+1]) return temp[i];
		}
		return 0;
	}
	/** 在技能列表中找到一个技能 */
	public Skill findSkill(Ability[] list)
	{
		int sid=selectAbility();
		if(sid!=0)
		{
			Skill skill=null;
			for(int i=list.length-1;i>=0;i--)
			{
				skill=(Skill)list[i];
				if(skill.getSid()==sid)
				{
					if(skill instanceof ContainerAbility)
					{
						ContainerAbility temp=(ContainerAbility)skill;
						sid=temp.getAbilitySidByShipSid(fleet.getShip()
							.getSid());
						skill=(Skill)FightScene.abilityFactory
							.newSample(sid);
						skill.setLevel(temp.getLevel());
						return skill;
					}
					else
					{
						return (Skill)skill.clone();
					}
				}
			}
		}
		sid=fleet.getShip().getNormalAbility();
		return (Skill)FightScene.abilityFactory.newSample(sid);
	}
	/** 实用指定技能配置找到对应的目标 */
	public Object findTarget(Skill skill)
	{
		TargetSelector s=skill.getSelector();
		IntKeyHashMap team=null;
		if(skill.getSpread().getSpreadTeam()==0)// 0表示目标是对方队伍
		{
			if(getTeam()==0)
			{
				team=getScene().getFighterContainer().getTeam(1);
			}
			else
			{
				team=getScene().getFighterContainer().getTeam(0);
			}
		}
		else
		{
			team=getScene().getFighterContainer().getTeam(getTeam());
		}
		Object target=TargetSelector.getTarget(this,s,team);
		return target;
	}

	public void showBytesWrite(ByteBuffer data)
	{
		super.showBytesWrite(data);
		fleet.showFightBytesWirte(data);
	}

	public int getCurrentRound()
	{
		return getScene().getCurrentRound();
	}

	public void setCurrentRound(int currentRound)
	{
		this.currentRound=currentRound;
	}
}