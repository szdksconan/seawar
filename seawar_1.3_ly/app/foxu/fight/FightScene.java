package foxu.fight;

import java.util.ArrayList;
import java.util.List;

import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.LimitNumSkill;
import mustang.event.ChangeListener;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.math.Random;
import mustang.math.Random1;
import mustang.set.Comparator;
import mustang.util.SampleFactory;

/**
 * 战斗场景类
 * 
 * @author ZYT
 */
abstract public class FightScene implements Cloneable
{

	/** 日志记录 */
	private static final Logger log=LogFactory.getLogger(FightScene.class);

	/* static fields */
	/**
	 * 事件常量： GET_SCENE=201得到战斗场景,SCENE_ADD_FIGHTER=202玩家加入战斗
	 * SKII_SPRING=203每回合触发技能,FIGHT_START=210战斗开始,ROUND_START=220回合开始,
	 * ROUND_OVER
	 * =229回合结束,ACTION_START=230行动开始,SPREAD_STRAT=231开始施放,ADD_ABILITY
	 * =232挂载技能,
	 * EXEMPT=233闪避,HURT=234造成伤害,IMMUNITY=235免疫,RESIST=236抵抗,DEAD=237角色死亡,
	 * SPREAD_OVER
	 * =238施放结束,REMOVE_FIGHTER=239移除一个fighter,CLEAN_ABILITY=240清除效果,
	 * ACTION_OVER
	 * =250动作结束,TOUCH_SPREAD_START=241触发施放开始,TOUCH_SPREAD_OVER=242触发释放结束,
	 * FIGHT_START
	 * =210战斗开始,FIGHTER_OVER=219战斗结束,ERUPT=251暴击,NEXT_TARGET=252下一个目标,
	 * ATTACK_ONCE
	 * =253攻击一次,ABILITY_TIME_OUT=254技能超时,INTERVAL_EFFECT=255间隔效果起一次作用
	 * NEXT_FIGHTER=256下一个fighter行动,EXECUTE_AILITY=257执行一个技能效果,LIMIT_HURT=258
	 * 打上限伤害FLUSH_BUFF=259刷新持续技能OTHER_HURT=260额外 分担转移的伤害,ADD_TIME=261添加时间
	 * FIGHT_CONTINUE  联合关卡继续演播
	 */
	public enum FightEvent
	{
		/**得到战斗场景*/
		GET_SCENE,
		/**玩家加入战斗*/
		SCENE_ADD_FIGHTER,
		/**战斗开始*/
		FIGHT_START,
		/**战斗结束*/
		FIGHT_OVER,
		/**回合开始*/
		ROUND_START,
		/**每回合触发技能*/
		SKII_SPRING,
		READY,
		/**回合结束*/
		ROUND_OVER,
		/**行动开始*/
		ACTION_START,
		/**开始施放*/
		SPREAD_START,
		/**挂载技能*/
		ADD_ABILITY,
		/**闪避*/
		EXEMPT,
		/**造成伤害**/
		HURT,
		/**免疫**/
		IMMUNITY,
		/**抵抗*/
		RESIST,
		/**角色死亡*/
		DEAD,
		/**施放结束*/
		SPREAD_OVER,
		/**角色死亡 移除一个fighter*/
		REMOVE_FIGHTER,
		/**清除效果*/
		CLEAN_ABILITY,
		/**触发施放开始*/
		TOUCH_SPREAD_START,
		/**触发施放结束*/
		TOUCH_SPREAD_OVER,
		/** 所有角色动作结束*/
		ACTION_OVER,
		/**暴击*/
		ERUPT,
		/**下一个目标*/
		NEXT_TARGET,
		/**攻击一次*/
		ATTACK_ONCE,
		/**技能超时*/
		ABILITY_TIME_OUT,
		/**间隔效果起一次作用*/
		INTERVAL_EFFECT,
		/**下一个fighter行动**/
		NEXT_FIGHTER,
		/**执行一个技能效果*/
		EXECUTE_AILITY,
		/**打上限伤害*/
		LIMIT_HURT,
		/**刷新持续技能*/
		FLUSH_BUFF,
		/**额外 分担转移的伤害*/
		OTHER_HURT,
		/**添加时间*/
		ADD_TIME,
		ATTACK_CONTINUE,
		FIGHT_CONTINUE,
		ACTIVE_STATE
	};

	/** 随机数上下限常量 */
	public static final int RANDOM_MAX=10001,RANDOM_MINI=0,OLD_TIMES=100;
	/** 战斗者工厂 */
	public static SampleFactory fighterFactory;
	/** 技能工厂 */
	public static SampleFactory abilityFactory;
	public static int MAX_ROUND=41;

	/* fields */
	/** 公式数组 */
	Formula[] formulas;
	/** 角色容器 */
	private FighterContainer container;
	/** 当前回合数 */
	int currentRound;
	/** 最大回合数限制 */
	int maxRound=MAX_ROUND;
	/** Fighter排序算法 */
	Comparator comparator;
	/** 监听器 */
	ChangeListener listener;
	/** 限制技能计数*/
	List<LimitNumSkill> limitNumSkillList = new ArrayList<LimitNumSkill>();
	
	private Random rd=new Random1();

	/** 初始为0 战斗完成后返回胜利者编号 */
	int successTeam;
	/** 被攻击防守部队先手 */
	boolean defend=false;

	
	
	/** 限制技能计数*/
	public List<LimitNumSkill> getLimitNumSkillList()
	{
		return limitNumSkillList;
	}
	/** 限制技能计数*/
	public void setLimitNumSkillList(List<LimitNumSkill> limitNumSkillList)
	{
		this.limitNumSkillList=limitNumSkillList;
	}
	/* properties */
	/** 设置排序算法 */
	public void setComparator(Comparator comparator)
	{
		this.comparator=comparator;
	}
	/** 获得战斗最大回合数 */
	public int getMaxRound()
	{
		return maxRound;
	}
	/** 设置战斗最大回合数 */
	public void setMaxRound(int maxRound)
	{
		this.maxRound=maxRound;
	}
	/** 设置fighter容器 */
	public void setFighterContainer(FighterContainer team)
	{
		this.container=team;
	}
	/** 获得监听器 */
	public ChangeListener getChangeListener()
	{
		return listener;
	}
	/** 设置监听器 */
	public void setChangeListener(ChangeListener listener)
	{
		this.listener=listener;
	}
	/** 设置公式对象 */
	public void setFormula(Formula[] formula)
	{
		formulas=formula;
	}
	/** 设置随机数种子 */
	public void setSeed(int seed)
	{
		rd.setSeed(seed);
	}
	/** 获得随机数生成器 */
	public Random getRandom()
	{
		return rd;
	}
	/** 获得Fighter容器 */
	public FighterContainer getFighterContainer()
	{
		return container;
	}
	/** 获得指定类型的公式 */
	public Formula getFormula(int type)
	{
		return formulas[type];
	}
	/** 得到当前回合 */
	public int getCurrentRound()
	{
		return currentRound;
	}

	/* abstract methods */
	/**
	 * 检查战斗是否结束
	 * 
	 * @return 返回胜利队伍编号，如果战斗未结束返回Integer.MAX_VALUE,如果平手返回0
	 */
	abstract public int checkOver();
	/**
	 * 准备行动所需的数据
	 * 
	 * @param f 需要行动的Fighter
	 */
	abstract public void fighterReady(Fighter f);

	/* methods */
	/** 初始化 */
	public void init()
	{
		currentRound=0;
	}
	/**
	 * 添加一组角色
	 * 
	 * @param f 一组Fighter
	 * @return 返回添加是否成功
	 */
	public boolean addFighter(int team,Fighter[] f)
	{
		if(f==null) return false;
		for(int i=f.length-1;i>=0;i--)
		{
			if(f[i]!=null)
			{
				f[i].setScene(this);
				f[i].setTeam(team);
				container.addFighter(team,f[i]);
			}
		}
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.SCENE_ADD_FIGHTER.ordinal(),
				new Integer(team),f);
		return true;
	}
	/**
	 * 添加角色
	 * 
	 * @param f Fighter
	 * @param location 是否自定义位置
	 * @return 返回添加成功
	 */
	public boolean addFighter(int teamId,Fighter f)
	{
		if(f==null) return false;
		container.addFighter(teamId,f);
		f.setTeam(teamId);
		f.setScene(this);
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.SCENE_ADD_FIGHTER.ordinal(),
				new Integer(teamId),f);
		return true;
	}
	/**
	 * 刷新fighter身上的技能
	 * 
	 * @param time 时间,用于判断技能是否超时
	 * @param code 校验码,用于判断技能是否能起作用
	 */
	public int flushFighterAbility(int time,ChangeListener listener)
	{
		Fighter[] fighters=container.getAllFighter();
		// Fighter身上的效果起一次作用
		for(int i=fighters.length-1;i>=0;i--)
		{
			if(!fighters[i].isDead())
			{
				fighters[i].flushAbility(time,listener);
				// 刷新效果过后,fighter有可能死亡
				if(fighters[i].isDead())
				{
					fighters[i].deadClear();
					removeHalo(fighters[i],fighters);
				}
				else
				{
					fighters[i].flushHalo(fighters);
				}
			}
		}
		return checkOver();
	}
	/**
	 * 所有角色进入战斗
	 * </p>
	 * 刷新光环
	 */
	public void fightStart()
	{
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.FIGHT_START.ordinal());
		int team;
		while(currentRound<maxRound)
		{
			team=roundStart();
			roundOver(team);
			if(team!=Integer.MAX_VALUE)
			{
				fightOver(team);
				break;
			}
		}
	}
	/**
	 * 进入回合准备阶段
	 * </p>
	 * 开始到计时
	 */
	public int roundStart()
	{
		currentRound++;
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.ROUND_START.ordinal());
		int team=flushFighterAbility(currentRound,listener);
		if(team==Integer.MAX_VALUE) return actionStart(comparator);
		return team;
	}
	/**
	 * 检查目标是否死亡,第二个参数的用于死亡fighter判断对其他fighter的影响
	 * 
	 * @param fighters 当前场景中的所有fighter
	 */
	public void checkOutTargetDead(Fighter[] fighters)
	{
		removeHalo(fighters,fighters);
	}
	
	/** 刷新光环 */
	public void flushHalo(Fighter[] fighters)
	{
		for(int i=0;i<fighters.length;i++)
		{
			fighters[i].flushHalo(fighters);
		}
	}

	/** 获取当前出手的fighter 返回null为没有 teamID队伍号 */
	public Fighter selectFighter(Fighter[] fighters,int teamID,
		int otherTeamId,int teamIndex0,int teamIndex1)
	{
		Fighter selectFighter=null;
		if(teamIndex0>FightSceneFactory.MAX_INDEX
			&&teamIndex1>FightSceneFactory.MAX_INDEX) return null;
		int location=teamIndex0;
		if(teamID==1) location=teamIndex1;
		for(int i=0;i<fighters.length;i++)
		{
			if(fighters[i]==null) continue;
			// 查看本次应出手的队伍
			selectFighter=findFighterByTeamId(fighters,teamID,location);
			// 查看另一只队伍还没有出手的fighter
			if(selectFighter==null)
			{
				int otherLocation=teamIndex0;
				if(otherTeamId==1) otherLocation=teamIndex1;
				selectFighter=findFighterByTeamId(fighters,otherTeamId,
					otherLocation);
			}
		}
		return selectFighter;
	}

	/** 从某一只队伍中 找出一个参展fighter */
	public Fighter findFighterByTeamId(Fighter[] fighters,int teamId,
		int location)
	{
		if(location>FightSceneFactory.MAX_INDEX) return null;
		for(int i=0;i<fighters.length;i++)
		{
			if(fighters[i]==null) continue;
			if(fighters[i].getTeam()==teamId
				&&fighters[i].getLocation()==location)
			{
				if(fighters[i].isDead())
					return findFighterByTeamId(fighters,teamId,location+1);
				return fighters[i];
			}
		}
		return findFighterByTeamId(fighters,teamId,location+1);
	}

	/** 开始动作 */
	public int actionStart(Comparator comparator)
	{
		int over=Integer.MAX_VALUE;
		Ability ability;
		// Fighter[] fighters=container.sort(comparator);
		Fighter[] fighters=container.getAllFighter();
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.ACTION_START.ordinal(),fighters);
		// 主动方当前回合出手的下标
		int teamIndex0=0;
		// 被动方当前回合出手的下标
		int teamIndex1=0;
		// 当前出手队伍号
		int teamID=0;

		Fighter fighterNow=selectFighter(fighters,teamID,1,teamIndex0,
			teamIndex1);
		if(defend)
		{
			fighterNow=selectFighter(fighters,1,0,teamIndex0,teamIndex1);
		}
		while(fighterNow!=null)
		{
			// 设置当前出手的信息
			teamID=fighterNow.getTeam();
			int nextTeamId=0;
			if(teamID==0)
			{
				teamIndex0=fighterNow.getLocation()+1;
				nextTeamId=1;
			}
			else
				teamIndex1=fighterNow.getLocation()+1;
			// 开始计算
			if(listener!=null)
				listener.change(this,FightEvent.NEXT_FIGHTER.ordinal(),
					fighterNow);
			if(fighterNow.isAttackFighter())
			{
				fighterReady(fighterNow);
				ability=fighterNow.getAbility();
				if(ability==null)
				{
					fighterNow=selectFighter(fighters,nextTeamId,teamID,
						teamIndex0,teamIndex1);
					continue;
				}
				// 检查技能能否正确施放
				if(ability.checkUsed(fighterNow,fighterNow.getTarget())==0)
					ability.used(fighterNow,fighterNow.getTarget());
				//检测死亡 现在在攻击后 马上结算
				//checkOutTargetDead(fighters);
				// 刷新光环
				flushHalo(fighters);
				fighterNow.clearTarget();
			}
			over=checkOver();
			if(over!=Integer.MAX_VALUE) break;
			fighterNow=selectFighter(fighters,nextTeamId,teamID,teamIndex0,
				teamIndex1);
		}
		return actionOver(over);
	}
	/**
	 * 所有角色动作结束
	 * 
	 * @param over 战斗是否结束的标示符
	 * @return 返回标示符
	 */
	public int actionOver(int over)
	{
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.ACTION_OVER.ordinal());
		return over;
	}
	/**
	 * 回合结束
	 * 
	 * @param over 战斗是否结束的标示符
	 * @return 返回标示符
	 */
	public void roundOver(int over)
	{
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.ROUND_OVER.ordinal(),over);
	}
	/**
	 * 战斗结束
	 * 
	 * @param team 胜利队伍号
	 */
	public void fightOver(int team)
	{
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.FIGHT_OVER.ordinal(),team);
		clear();
		successTeam=team;
	}
	/**
	 * 移除所有fighter身上另一个figher(此人已死)施放的光环
	 * 
	 * @param fighter 释放光环技的fighter
	 * @param fighters 全部fighter
	 */
	public void removeHalo(Fighter fighter,Fighter[] fighters)
	{
		if(fighter==null) return;
		for(int i=fighters.length-1;i>=0;i--)
		{
			if(fighter==fighters[i]) continue;
			fighters[i].removePerennityAbility(fighter);
		}
	}
	/**
	 * 如果有已死亡的 fighter刷新一次所有fighter的光环技能
	 * 
	 * @param fighters 需要判断的fighter
	 * @param allFighters 全部fighter
	 */
	public void removeHalo(Fighter[] fighters,Fighter[] allFighters)
	{
		Fighter f;
		for(int i=fighters.length-1;i>=0;i--)
		{
			f=container.getFighter(fighters[i].getTeam(),fighters[i]
				.getLocation());
			if(f!=null&&fighters[i].isDead()) // 如果死亡，刷新一次存活fighter身上的光环
			{
				fighters[i].deadClear();
				removeHalo(fighters[i],allFighters);
			}
		}
	}
	/** 移除某个fighter */
	public void removeFighter(Fighter fighter)
	{
		if(fighter==null) return;
		// if(fighter.isDead()) fighter.deadClear();
		fighter.clear();
		Fighter[] fighters=container.getAllFighter();
		for(int i=fighters.length-1;i>=0;i--)
		{
			if(fighters[i]!=fighter)
				fighters[i].removePerennityAbility(fighter);
		}
		container.removeFighter(fighter);
		fighter.setScene(null);
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener
				.change(this,FightEvent.REMOVE_FIGHTER.ordinal(),fighter);
	}
	/** 战斗结束后的清理工作 */
	public void clear()
	{
		Fighter[] fighters=container.getAllFighter();
		for(int i=fighters.length-1;i>=0;i--)
		{
			fighters[i].clear();
		}
		container.clear();
	}

	/**
	 * @return successTeam
	 */
	public int getSuccessTeam()
	{
		return successTeam;
	}

	/**
	 * @param successTeam 要设置的 successTeam
	 */
	public void setSuccessTeam(int successTeam)
	{
		this.successTeam=successTeam;
	}

	public boolean isDefend()
	{
		return defend;
	}

	public void setDefend(boolean defend)
	{
		this.defend=defend;
	}
}