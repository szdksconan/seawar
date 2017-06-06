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
 * ս��������
 * 
 * @author ZYT
 */
abstract public class FightScene implements Cloneable
{

	/** ��־��¼ */
	private static final Logger log=LogFactory.getLogger(FightScene.class);

	/* static fields */
	/**
	 * �¼������� GET_SCENE=201�õ�ս������,SCENE_ADD_FIGHTER=202��Ҽ���ս��
	 * SKII_SPRING=203ÿ�غϴ�������,FIGHT_START=210ս����ʼ,ROUND_START=220�غϿ�ʼ,
	 * ROUND_OVER
	 * =229�غϽ���,ACTION_START=230�ж���ʼ,SPREAD_STRAT=231��ʼʩ��,ADD_ABILITY
	 * =232���ؼ���,
	 * EXEMPT=233����,HURT=234����˺�,IMMUNITY=235����,RESIST=236�ֿ�,DEAD=237��ɫ����,
	 * SPREAD_OVER
	 * =238ʩ�Ž���,REMOVE_FIGHTER=239�Ƴ�һ��fighter,CLEAN_ABILITY=240���Ч��,
	 * ACTION_OVER
	 * =250��������,TOUCH_SPREAD_START=241����ʩ�ſ�ʼ,TOUCH_SPREAD_OVER=242�����ͷŽ���,
	 * FIGHT_START
	 * =210ս����ʼ,FIGHTER_OVER=219ս������,ERUPT=251����,NEXT_TARGET=252��һ��Ŀ��,
	 * ATTACK_ONCE
	 * =253����һ��,ABILITY_TIME_OUT=254���ܳ�ʱ,INTERVAL_EFFECT=255���Ч����һ������
	 * NEXT_FIGHTER=256��һ��fighter�ж�,EXECUTE_AILITY=257ִ��һ������Ч��,LIMIT_HURT=258
	 * �������˺�FLUSH_BUFF=259ˢ�³�������OTHER_HURT=260���� �ֵ�ת�Ƶ��˺�,ADD_TIME=261���ʱ��
	 * FIGHT_CONTINUE  ���Ϲؿ������ݲ�
	 */
	public enum FightEvent
	{
		/**�õ�ս������*/
		GET_SCENE,
		/**��Ҽ���ս��*/
		SCENE_ADD_FIGHTER,
		/**ս����ʼ*/
		FIGHT_START,
		/**ս������*/
		FIGHT_OVER,
		/**�غϿ�ʼ*/
		ROUND_START,
		/**ÿ�غϴ�������*/
		SKII_SPRING,
		READY,
		/**�غϽ���*/
		ROUND_OVER,
		/**�ж���ʼ*/
		ACTION_START,
		/**��ʼʩ��*/
		SPREAD_START,
		/**���ؼ���*/
		ADD_ABILITY,
		/**����*/
		EXEMPT,
		/**����˺�**/
		HURT,
		/**����**/
		IMMUNITY,
		/**�ֿ�*/
		RESIST,
		/**��ɫ����*/
		DEAD,
		/**ʩ�Ž���*/
		SPREAD_OVER,
		/**��ɫ���� �Ƴ�һ��fighter*/
		REMOVE_FIGHTER,
		/**���Ч��*/
		CLEAN_ABILITY,
		/**����ʩ�ſ�ʼ*/
		TOUCH_SPREAD_START,
		/**����ʩ�Ž���*/
		TOUCH_SPREAD_OVER,
		/** ���н�ɫ��������*/
		ACTION_OVER,
		/**����*/
		ERUPT,
		/**��һ��Ŀ��*/
		NEXT_TARGET,
		/**����һ��*/
		ATTACK_ONCE,
		/**���ܳ�ʱ*/
		ABILITY_TIME_OUT,
		/**���Ч����һ������*/
		INTERVAL_EFFECT,
		/**��һ��fighter�ж�**/
		NEXT_FIGHTER,
		/**ִ��һ������Ч��*/
		EXECUTE_AILITY,
		/**�������˺�*/
		LIMIT_HURT,
		/**ˢ�³�������*/
		FLUSH_BUFF,
		/**���� �ֵ�ת�Ƶ��˺�*/
		OTHER_HURT,
		/**���ʱ��*/
		ADD_TIME,
		ATTACK_CONTINUE,
		FIGHT_CONTINUE,
		ACTIVE_STATE
	};

	/** ����������޳��� */
	public static final int RANDOM_MAX=10001,RANDOM_MINI=0,OLD_TIMES=100;
	/** ս���߹��� */
	public static SampleFactory fighterFactory;
	/** ���ܹ��� */
	public static SampleFactory abilityFactory;
	public static int MAX_ROUND=41;

	/* fields */
	/** ��ʽ���� */
	Formula[] formulas;
	/** ��ɫ���� */
	private FighterContainer container;
	/** ��ǰ�غ��� */
	int currentRound;
	/** ���غ������� */
	int maxRound=MAX_ROUND;
	/** Fighter�����㷨 */
	Comparator comparator;
	/** ������ */
	ChangeListener listener;
	/** ���Ƽ��ܼ���*/
	List<LimitNumSkill> limitNumSkillList = new ArrayList<LimitNumSkill>();
	
	private Random rd=new Random1();

	/** ��ʼΪ0 ս����ɺ󷵻�ʤ���߱�� */
	int successTeam;
	/** ���������ز������� */
	boolean defend=false;

	
	
	/** ���Ƽ��ܼ���*/
	public List<LimitNumSkill> getLimitNumSkillList()
	{
		return limitNumSkillList;
	}
	/** ���Ƽ��ܼ���*/
	public void setLimitNumSkillList(List<LimitNumSkill> limitNumSkillList)
	{
		this.limitNumSkillList=limitNumSkillList;
	}
	/* properties */
	/** ���������㷨 */
	public void setComparator(Comparator comparator)
	{
		this.comparator=comparator;
	}
	/** ���ս�����غ��� */
	public int getMaxRound()
	{
		return maxRound;
	}
	/** ����ս�����غ��� */
	public void setMaxRound(int maxRound)
	{
		this.maxRound=maxRound;
	}
	/** ����fighter���� */
	public void setFighterContainer(FighterContainer team)
	{
		this.container=team;
	}
	/** ��ü����� */
	public ChangeListener getChangeListener()
	{
		return listener;
	}
	/** ���ü����� */
	public void setChangeListener(ChangeListener listener)
	{
		this.listener=listener;
	}
	/** ���ù�ʽ���� */
	public void setFormula(Formula[] formula)
	{
		formulas=formula;
	}
	/** ������������� */
	public void setSeed(int seed)
	{
		rd.setSeed(seed);
	}
	/** �������������� */
	public Random getRandom()
	{
		return rd;
	}
	/** ���Fighter���� */
	public FighterContainer getFighterContainer()
	{
		return container;
	}
	/** ���ָ�����͵Ĺ�ʽ */
	public Formula getFormula(int type)
	{
		return formulas[type];
	}
	/** �õ���ǰ�غ� */
	public int getCurrentRound()
	{
		return currentRound;
	}

	/* abstract methods */
	/**
	 * ���ս���Ƿ����
	 * 
	 * @return ����ʤ�������ţ����ս��δ��������Integer.MAX_VALUE,���ƽ�ַ���0
	 */
	abstract public int checkOver();
	/**
	 * ׼���ж����������
	 * 
	 * @param f ��Ҫ�ж���Fighter
	 */
	abstract public void fighterReady(Fighter f);

	/* methods */
	/** ��ʼ�� */
	public void init()
	{
		currentRound=0;
	}
	/**
	 * ���һ���ɫ
	 * 
	 * @param f һ��Fighter
	 * @return ��������Ƿ�ɹ�
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
	 * ��ӽ�ɫ
	 * 
	 * @param f Fighter
	 * @param location �Ƿ��Զ���λ��
	 * @return ������ӳɹ�
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
	 * ˢ��fighter���ϵļ���
	 * 
	 * @param time ʱ��,�����жϼ����Ƿ�ʱ
	 * @param code У����,�����жϼ����Ƿ���������
	 */
	public int flushFighterAbility(int time,ChangeListener listener)
	{
		Fighter[] fighters=container.getAllFighter();
		// Fighter���ϵ�Ч����һ������
		for(int i=fighters.length-1;i>=0;i--)
		{
			if(!fighters[i].isDead())
			{
				fighters[i].flushAbility(time,listener);
				// ˢ��Ч������,fighter�п�������
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
	 * ���н�ɫ����ս��
	 * </p>
	 * ˢ�¹⻷
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
	 * ����غ�׼���׶�
	 * </p>
	 * ��ʼ����ʱ
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
	 * ���Ŀ���Ƿ�����,�ڶ�����������������fighter�ж϶�����fighter��Ӱ��
	 * 
	 * @param fighters ��ǰ�����е�����fighter
	 */
	public void checkOutTargetDead(Fighter[] fighters)
	{
		removeHalo(fighters,fighters);
	}
	
	/** ˢ�¹⻷ */
	public void flushHalo(Fighter[] fighters)
	{
		for(int i=0;i<fighters.length;i++)
		{
			fighters[i].flushHalo(fighters);
		}
	}

	/** ��ȡ��ǰ���ֵ�fighter ����nullΪû�� teamID����� */
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
			// �鿴����Ӧ���ֵĶ���
			selectFighter=findFighterByTeamId(fighters,teamID,location);
			// �鿴��һֻ���黹û�г��ֵ�fighter
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

	/** ��ĳһֻ������ �ҳ�һ����չfighter */
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

	/** ��ʼ���� */
	public int actionStart(Comparator comparator)
	{
		int over=Integer.MAX_VALUE;
		Ability ability;
		// Fighter[] fighters=container.sort(comparator);
		Fighter[] fighters=container.getAllFighter();
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.ACTION_START.ordinal(),fighters);
		// ��������ǰ�غϳ��ֵ��±�
		int teamIndex0=0;
		// ��������ǰ�غϳ��ֵ��±�
		int teamIndex1=0;
		// ��ǰ���ֶ����
		int teamID=0;

		Fighter fighterNow=selectFighter(fighters,teamID,1,teamIndex0,
			teamIndex1);
		if(defend)
		{
			fighterNow=selectFighter(fighters,1,0,teamIndex0,teamIndex1);
		}
		while(fighterNow!=null)
		{
			// ���õ�ǰ���ֵ���Ϣ
			teamID=fighterNow.getTeam();
			int nextTeamId=0;
			if(teamID==0)
			{
				teamIndex0=fighterNow.getLocation()+1;
				nextTeamId=1;
			}
			else
				teamIndex1=fighterNow.getLocation()+1;
			// ��ʼ����
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
				// ��鼼���ܷ���ȷʩ��
				if(ability.checkUsed(fighterNow,fighterNow.getTarget())==0)
					ability.used(fighterNow,fighterNow.getTarget());
				//������� �����ڹ����� ���Ͻ���
				//checkOutTargetDead(fighters);
				// ˢ�¹⻷
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
	 * ���н�ɫ��������
	 * 
	 * @param over ս���Ƿ�����ı�ʾ��
	 * @return ���ر�ʾ��
	 */
	public int actionOver(int over)
	{
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.ACTION_OVER.ordinal());
		return over;
	}
	/**
	 * �غϽ���
	 * 
	 * @param over ս���Ƿ�����ı�ʾ��
	 * @return ���ر�ʾ��
	 */
	public void roundOver(int over)
	{
		ChangeListener listener=this.listener;
		if(listener!=null)
			listener.change(this,FightEvent.ROUND_OVER.ordinal(),over);
	}
	/**
	 * ս������
	 * 
	 * @param team ʤ�������
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
	 * �Ƴ�����fighter������һ��figher(��������)ʩ�ŵĹ⻷
	 * 
	 * @param fighter �ͷŹ⻷����fighter
	 * @param fighters ȫ��fighter
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
	 * ������������� fighterˢ��һ������fighter�Ĺ⻷����
	 * 
	 * @param fighters ��Ҫ�жϵ�fighter
	 * @param allFighters ȫ��fighter
	 */
	public void removeHalo(Fighter[] fighters,Fighter[] allFighters)
	{
		Fighter f;
		for(int i=fighters.length-1;i>=0;i--)
		{
			f=container.getFighter(fighters[i].getTeam(),fighters[i]
				.getLocation());
			if(f!=null&&fighters[i].isDead()) // ���������ˢ��һ�δ��fighter���ϵĹ⻷
			{
				fighters[i].deadClear();
				removeHalo(fighters[i],allFighters);
			}
		}
	}
	/** �Ƴ�ĳ��fighter */
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
	/** ս��������������� */
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
	 * @param successTeam Ҫ���õ� successTeam
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