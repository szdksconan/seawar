/**
 * 
 */
package foxu.sea.fight;

import mustang.event.ChangeListener;
import mustang.set.IntKeyHashMap;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.fight.Fighter;
import foxu.fight.Spread;

/**
 * 开战前,对双方都会事先释放一组前置技能
 * 
 * @author rockzyt
 */
public class AttackFirstSeaWarFightScene extends FightScene
{

	/* static fields */
	public static final Ability[] NULL_ABILITY=new Ability[0];

	/* fields */
	/** 对进攻方释放的技能 */
	Ability[] ability0=NULL_ABILITY;
	/** 对防守方释放的技能 */
	Ability[] ability1=NULL_ABILITY;

	/* properties */
	/**
	 * 设置进攻方的前置技能
	 * 
	 * @param ability0
	 */
	public void setAbility0(Ability[] ability0)
	{
		this.ability0=ability0;
	}
	/**
	 * 设置防守方的前置技能
	 * 
	 * @param ability1
	 */
	public void setAbility1(Ability[] ability1)
	{
		this.ability1=ability1;
	}

	/* methods */
	// /**
	// * 所有角色进入战斗
	// * </p>
	// * 刷新光环
	// */
	// public void fightStart()
	// {
	// IntKeyHashMap team0=getFighterContainer().getTeam(0);
	// IntKeyHashMap team1=getFighterContainer().getTeam(1);
	// Fighter fighter=null;
	// Ability ability=null;
	// for(int i=0,j=0;i<FleetGroup.MAX_FLEET;i++)
	// {
	// fighter=(Fighter)team0.get(i);
	// if(fighter!=null&&ability0!=null)
	// {
	// for(j=ability0.length-1;j>=0;j--)
	// {
	// ability=(Ability)ability0[j];
	// fighter.addAbility(ability,1);
	// }
	// }
	// fighter=(Fighter)team1.get(i);
	// if(fighter!=null&&ability1!=null)
	// {
	// for(j=ability1.length-1;j>=0;j--)
	// {
	// ability=(Ability)ability1[j];
	// fighter.addAbility(ability,1);
	// }
	// }
	// }
	// super.fightStart();
	// }
	/**
	 * 进入回合准备阶段 </p> 开始到计时
	 */
	public int roundStart()
	{
		// 如果第一回合还没开始,释放对应的前置技能
		if(getCurrentRound()<1)
		{
			IntKeyHashMap team0=getFighterContainer().getTeam(0);
			IntKeyHashMap team1=getFighterContainer().getTeam(1);
			Fighter fighter=null;
			ChangeListener listener=getChangeListener();
			if(ability0!=null)
			{
				for(int i=ability0.length-1,j=0;i>=0;i--)
				{
					if(ability0[i]==null) continue;
					for(j=0;j<FleetGroup.MAX_FLEET;j++)
					{
						fighter=(Fighter)team0.get(j);
						if(fighter==null) continue;
						fighter.addAbility((Ability)ability0[i].clone(),0);
					}
					if(listener!=null)
						listener.change(this,
							FightEvent.SPREAD_OVER.ordinal());
				}
			}
			if(ability1!=null)
			{
				for(int i=ability1.length-1,j=0;i>=0;i--)
				{
					if(ability1[i]==null) continue;
					for(j=0;j<FleetGroup.MAX_FLEET;j++)
					{
						fighter=(Fighter)team1.get(j);
						if(fighter==null) continue;
						fighter.addAbility((Ability)ability1[i].clone(),0);
					}
					if(listener!=null)
						listener.change(this,
							FightEvent.SPREAD_OVER.ordinal());
				}
			}
		}
		return super.roundStart();
	}
	public int checkOver()
	{
		if(getCurrentRound()>=getMaxRound()) return 1;
		IntKeyHashMap[] team=getFighterContainer().getAllTeam();
		if(team[0].size()<=0) return 1;// 0队无成员,1队为胜利队伍
		if(team[1].size()<=0) return 0;// 同上取反
		return Integer.MAX_VALUE;
	}
	public void fighterReady(Fighter f)
	{
		if(f.isDead()) return;
		FleetFighter ship=(FleetFighter)f;
		ship.getReady();
	}
}