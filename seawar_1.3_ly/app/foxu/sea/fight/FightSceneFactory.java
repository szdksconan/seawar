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
 * ս������,����ս��
 * 
 * @author rockzyt
 */
public class FightSceneFactory
{
	/* static fields */
	/** ս������ */
	public static FightSceneFactory factory=new FightSceneFactory();

	/* fields */
	/** λ�����ֵ */
	public static final int MAX_INDEX=8;
	// /** �ٶ�ֵ���� */
	// int[] speed=new
	// int[]{100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71};
	/** ս�����㹫ʽ */
	Formula[] formula=new Formula[]{new BaseAttackFormula()};
	/** ս�������㷨 */
	Comparator comparator=new SpeedComparator();
	/** 1�Ӷ���� */
	int team1=0;
	/** 2�Ӷ���� */
	int team2=1;

	/* methods */
	/**
	 * ������Fighter����һ��ս�� ��սfighter�������ú�location
	 * 
	 * @param f1 ����1,������,Ĭ���ж����ֵĶ���
	 * @param f2 ����2,���ط�
	 * @return ���ش����õ�FightScene
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
				// ������Ϣ(����������������Ч���ĳ�ʼ��)
				fleets1[i].clearFleetAdjust();
				f1.initArmyFleet(fleets1[i],list);
				f2.initEnemyFleet(fleets1[i],list);
				fleets1[i].intoFightScene(list);
				fleets1[i].intoOfficerSkill(f1);//���뽫�촥���Լ���
				
				//fleets1[i].getFighter().addAbility((Ability)FightScene.abilityFactory
				//	.newSample(459),0);
				scene.addFighter(team1,fleets1[i].getFighter());
				scene.getFighterContainer().addHp(team1,fleets1[i].getHp());//��ʼ����Ѫ��
				fleets1[i].getFighter().setFighterMaxHp(fleets1[i].getHp());;//����һ����ʼ���ֵ ���ڼ����λ�ٷֱ�
			}
			if(fleets2[i]!=null&&fleets2[i].getNum()>0)
			{
				IntList list=new IntList();
				// ������Ϣ(����������������Ч���ĳ�ʼ��)
				fleets2[i].clearFleetAdjust();
				f2.initArmyFleet(fleets2[i],list);
				f1.initEnemyFleet(fleets2[i],list);
				fleets2[i].intoFightScene(list);
				fleets2[i].intoOfficerSkill(f2);//���뽫�촥���Լ���
				//���Լ���Ч��
				//fleets2[i].getFighter().addAbility((Ability)FightScene.abilityFactory
				//	.newSample(20036),0);
				//fleets2[i].getFighter().addAbility((Ability)FightScene.abilityFactory
				//	.newSample(438),0);
				//fleets2[i].getFighter().addAbility((Ability)FightScene.abilityFactory
				//	.newSample(459),0);
				scene.addFighter(team2,fleets2[i].getFighter());
				scene.getFighterContainer().addHp(team2,fleets2[i].getHp());//��ʼ����Ѫ��
				fleets2[i].getFighter().setFighterMaxHp(fleets2[i].getHp());;//����һ����ʼ���ֵ ���ڼ����λ�ٷֱ�
			}
		}
		//System.out.println(scene.getFighterContainer().getTeam1Hp()+"   "+scene.getFighterContainer().getTeam2Hp());
		//��¼ ���Ѫ��  ���ڼ��� ��ǰѪ�ߵİٷֱ�
		scene.getFighterContainer().setTeam1HpMax(scene.getFighterContainer().getTeam1Hp());
		scene.getFighterContainer().setTeam2HpMax(scene.getFighterContainer().getTeam2Hp());
		
		
//		 initFighterSpeed(f1,f2);
		return scene;
	}
	/**
	 * ������Fighter������ǰ�ü��ܴ���һ��ս�� ��սfighter�������ú�location
	 * 
	 * @param f1 ����1,������,Ĭ���ж����ֵĶ���
	 * @param abilitys1 ����1,������,Ĭ���ж����ֵĶ���
	 * @param f2 ����2,���ط�
	 * @param abilitys2 ����1,������,Ĭ���ж����ֵĶ���
	 * @return ���ش����õ�FightScene
	 */
	public FightScene create(FleetGroup f1, Ability[] abilitys1,FleetGroup f2,Ability[] abilitys2)
	{
		AttackFirstSeaWarFightScene scene=(AttackFirstSeaWarFightScene)create(f1,f2);
		scene.setAbility0(abilitys1);
		scene.setAbility1(abilitys2);
		return scene;
	}
	/**
	 * ��ʼһ��ս��
	 * 
	 * @param scene ս������
	 * @param listeners �¼�������
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