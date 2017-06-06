package foxu.sea.fight;

import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.set.SetKit;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.fight.Fighter;
import foxu.sea.AttrAdjustment;
import foxu.sea.PublicConst;
import foxu.sea.Ship;


/**
 * 联盟舰队
 * @author yw
 *
 */
public class AllianceFleet extends Fleet
{
	/** 联盟技能修正值集合  */
	private AttrAdjustment adjustment;
	/** 联盟主动技能（连击） */
	private IntList skillList;

	/** 获得单支舰船生命值 */
	public float getShipLife()
	{
		if(adjustment==null) return ship.getLife();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.SHIP_HP);
		if(data==null) return ship.getLife();
		float v=ship.getLife();
		return (v+v*data.percent/100);
	}

	/* methods */
	/** 获得指定数量船只的攻击力 */
	public int attack(int num)
	{
		if(adjustment==null) return (int)ship.getAttack()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.ATTACK);
		if(data==null) return (int)ship.getAttack()*num;
		float v=ship.getAttack();
		return (int)(num*v+((double)num)*v*data.percent/100);
	}
	/** 获得指定数量船只的防御力 */
	public int defence(int num)
	{
		if(adjustment==null) return ship.getDefence()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.DEFENCE);
		if(data==null) return ship.getDefence()*num;
		int v=ship.getDefence();
		return (int)(num*v+((double)num)*v*data.percent/100);
	}
	/** 获得舰队最大HP */
	public int maxHp()
	{
		if(adjustment==null) return (int)ship.getLife()*num;
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.SHIP_HP);
		if(data==null) return (int)ship.getLife()*num;
		float v=ship.getLife();
		return (int)(num*v+((double)num)*v*data.percent/100);   
	}
	/** 获得回避 */
	public int getAvoid()
	{
		if(adjustment==null) return ship.getAvoid();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.AVOID);
		if(data==null) return ship.getAvoid();
		int v=ship.getAvoid();
		return v+data.percent;
	}

	/** 获得精准 */
	public int getAccurate()
	{
		if(adjustment==null) return ship.getAccurate();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.ACCURATE);
		if(data==null) return ship.getAccurate();
		int v=ship.getAccurate();
		return v+data.percent;
	}

	/** 暴击 */
	public int getDecritical()
	{
		if(adjustment==null) return ship.getDecritical();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.CRITICAL_HIT_RESIST);
		if(data==null) return ship.getDecritical();
		int v=ship.getDecritical();
		return v+data.percent;
	}
	/** 暴击抵抗 */
	public int getCritical()
	{
		if(adjustment==null) return ship.getCritical();
		AttrAdjustment.AdjustmentData data=adjustment.getAdjustmentValue(
			ship.getPlayerType(),PublicConst.CRITICAL_HIT);
		if(data==null) return ship.getCritical();
		int v=ship.getCritical();
		return v+data.percent;
	}
	/** 舰队进入战场,初始化战斗数据,传入IntList技能影响[技能sid,影响概率] */
	public void intoFightScene(IntList probList)
	{
		Fighter f=getFighter();
		f.init(-1);// 重新分配id,将就前台....
		f.clear();
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
			for(int i=0;i<skillList.size();i+=2)
			{
				int sid=skillList.get(i);
				AllianceSkill aAbility=(AllianceSkill)FightScene.abilityFactory
					.getSample(sid);
				// 仅对影响友方的技能做处理
				if(aAbility.getShipType()==ship.getPlayerType()
					&&!aAbility.isEffectEnemySkill())
				{
					aAbility=(AllianceSkill)aAbility.clone();
					aAbility.setLevel(skillList.get(i+1));
					probability+=aAbility.getProbability();
					list.add(aAbility);
				}
			}
			probability=initFleetSkill(probList,list,probability);
			Object[] objs=list.toArray();
			SetKit.sort(objs,this);
			int[] probabilityList=new int[objs.length*2];
			Skill skill=null;
			Ability[] tempAbilitys=new Ability[objs.length];
			for(int i=probabilityList.length-1,j=0;i>=0;i-=2,j++)
			{
				skill=(Skill)objs[j];
				tempAbilitys[j]=skill;
				if(probability>FightScene.RANDOM_MAX-1)
				{
					probabilityList[i]=skill.getProbability()*(FightScene.RANDOM_MAX-1)
						/probability;
				}
				else
				{
					probabilityList[i]=skill.getProbability();
				}
				probabilityList[i-1]=skill.getSid();
			}
			fighter.setSelectAbility(probabilityList);
			ship.setAbilityListInFight(tempAbilitys);
		}
	}

	public Object copy(Object obj)
	{
		AllianceFleet f=(AllianceFleet)obj;
		if(ship!=null) f.ship=(Ship)ship.clone();
		f.setAdjustment(adjustment);
		f.setSkillList(skillList);
		f.fighter=null;
		return obj;
	}

	
	public AttrAdjustment getAdjustment()
	{
		return adjustment;
	}

	
	public void setAdjustment(AttrAdjustment adjustment)
	{
		this.adjustment=adjustment;
	}

	@Override
	public IntList getSkillList()
	{
		return skillList;
	}

	
	public void setSkillList(IntList skillList)
	{
		this.skillList=skillList;
	}
	
	@Override
	public AttrAdjustment getAttrAdjustment()
	{
		return adjustment;
	}
}
