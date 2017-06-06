/**
 * 
 */
package foxu.sea.fight;

import mustang.event.ChangeListenerList;
import mustang.set.Comparator;
import mustang.set.IntList;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.fight.FighterContainer;
import foxu.fight.Formula;
import foxu.fight.SpeedComparator;

/**
 * 战场工厂,创建战斗
 * 
 * @author rockzyt
 */
public class FightSceneFactory
{
	/* static fields */
	/** 战斗工厂 */
	public static FightSceneFactory factory=new FightSceneFactory();

	/* fields */
	/** 位置最大值 */
	public static final int MAX_INDEX=8;
	// /** 速度值数组 */
	// int[] speed=new
	// int[]{100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71};
	/** 战斗计算公式 */
	Formula[] formula=new Formula[]{new BaseAttackFormula()};
	/** 战斗排序算法 */
	Comparator comparator=new SpeedComparator();
	/** 1队队伍号 */
	int team1=0;
	/** 2队队伍号 */
	int team2=1;

	/* methods */
	/**
	 * 用两组Fighter创建一场战斗 参战fighter必须设置好location
	 * 
	 * @param f1 队伍1,进攻方,默认判断先手的队伍
	 * @param f2 队伍2,防守方
	 * @return 返回创建好的FightScene
	 */
	public FightScene create(FleetGroup f1,FleetGroup f2)
	{
		FightScene scene=new AttackFirstSeaWarFightScene();
		scene.setFormula(formula);
		scene.setComparator(comparator);
		scene.setFighterContainer(new FighterContainer());
		Fleet[] fleets1=f1.getArray();
		Fleet[] fleets2=f2.getArray();
		for(int i=fleets1.length-1;i>=0;i--)
		{
			if(fleets1[i]!=null&&fleets1[i].getNum()>0)
			{
				IntList list=new IntList();
				// 出阵信息(包括反连击、军官效果的初始化)
				fleets1[i].clearFleetAdjust();
				f1.initArmyFleet(fleets1[i],list);
				f2.initEnemyFleet(fleets1[i],list);
				fleets1[i].intoFightScene(list);
				fleets1[i].intoOfficerSkill(f1);//加入将领触发性技能
				
				//fleets1[i].getFighter().addAbility((Ability)FightScene.abilityFactory
				//	.newSample(459),0);
				scene.addFighter(team1,fleets1[i].getFighter());
				scene.getFighterContainer().addHp(team1,fleets1[i].getHp());//初始化总血量
				fleets1[i].getFighter().setFighterMaxHp(fleets1[i].getHp());;//设置一个初始最大值 用于计算坑位百分比
			}
			if(fleets2[i]!=null&&fleets2[i].getNum()>0)
			{
				IntList list=new IntList();
				// 出阵信息(包括反连击、军官效果的初始化)
				fleets2[i].clearFleetAdjust();
				f2.initArmyFleet(fleets2[i],list);
				f1.initEnemyFleet(fleets2[i],list);
				fleets2[i].intoFightScene(list);
				fleets2[i].intoOfficerSkill(f2);//加入将领触发性技能
				//测试技能效果
				//fleets2[i].getFighter().addAbility((Ability)FightScene.abilityFactory
				//	.newSample(20036),0);
				//fleets2[i].getFighter().addAbility((Ability)FightScene.abilityFactory
				//	.newSample(438),0);
				//fleets2[i].getFighter().addAbility((Ability)FightScene.abilityFactory
				//	.newSample(459),0);
				scene.addFighter(team2,fleets2[i].getFighter());
				scene.getFighterContainer().addHp(team2,fleets2[i].getHp());//初始化总血量
				fleets2[i].getFighter().setFighterMaxHp(fleets2[i].getHp());;//设置一个初始最大值 用于计算坑位百分比
			}
		}
		//System.out.println(scene.getFighterContainer().getTeam1Hp()+"   "+scene.getFighterContainer().getTeam2Hp());
		//记录 最大血量  用于计算 当前血线的百分比
		scene.getFighterContainer().setTeam1HpMax(scene.getFighterContainer().getTeam1Hp());
		scene.getFighterContainer().setTeam2HpMax(scene.getFighterContainer().getTeam2Hp());
		
		
//		 initFighterSpeed(f1,f2);
		return scene;
	}
	/**
	 * 用两组Fighter和两组前置技能创建一场战斗 参战fighter必须设置好location
	 * 
	 * @param f1 队伍1,进攻方,默认判断先手的队伍
	 * @param abilitys1 队伍1,进攻方,默认判断先手的队伍
	 * @param f2 队伍2,防守方
	 * @param abilitys2 队伍1,进攻方,默认判断先手的队伍
	 * @return 返回创建好的FightScene
	 */
	public FightScene create(FleetGroup f1, Ability[] abilitys1,FleetGroup f2,Ability[] abilitys2)
	{
		AttackFirstSeaWarFightScene scene=(AttackFirstSeaWarFightScene)create(f1,f2);
		scene.setAbility0(abilitys1);
		scene.setAbility1(abilitys2);
		return scene;
	}
	/**
	 * 开始一场战斗
	 * 
	 * @param scene 战斗场景
	 * @param listeners 事件监听器
	 * @return
	 */
	public FightShowEventRecord fight(FightScene scene,
		ChangeListenerList listeners)
	{
		FightShowEventRecord r=new FightShowEventRecord();
		if(listeners==null)
		{
			scene.setChangeListener(r);
		}
		else
		{
			listeners.addListener(r);
			scene.setChangeListener(listeners);
		}
		scene.fightStart();
		return r;
	}
}