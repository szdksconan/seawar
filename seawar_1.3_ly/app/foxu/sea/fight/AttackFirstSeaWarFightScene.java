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
 * ��սǰ,��˫�����������ͷ�һ��ǰ�ü���
 * 
 * @author rockzyt
 */
public class AttackFirstSeaWarFightScene extends FightScene
{

	/* static fields */
	public static final Ability[] NULL_ABILITY=new Ability[0];

	/* fields */
	/** �Խ������ͷŵļ��� */
	Ability[] ability0=NULL_ABILITY;
	/** �Է��ط��ͷŵļ��� */
	Ability[] ability1=NULL_ABILITY;

	/* properties */
	/**
	 * ���ý�������ǰ�ü���
	 * 
	 * @param ability0
	 */
	public void setAbility0(Ability[] ability0)
	{
		this.ability0=ability0;
	}
	/**
	 * ���÷��ط���ǰ�ü���
	 * 
	 * @param ability1
	 */
	public void setAbility1(Ability[] ability1)
	{
		this.ability1=ability1;
	}

	/* methods */
	// /**
	// * ���н�ɫ����ս��
	// * </p>
	// * ˢ�¹⻷
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
	 * ����غ�׼���׶� </p> ��ʼ����ʱ
	 */
	public int roundStart()
	{
		// �����һ�غϻ�û��ʼ,�ͷŶ�Ӧ��ǰ�ü���
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
		if(team[0].size()<=0) return 1;// 0���޳�Ա,1��Ϊʤ������
		if(team[1].size()<=0) return 0;// ͬ��ȡ��
		return Integer.MAX_VALUE;
	}
	public void fighterReady(Fighter f)
	{
		if(f.isDead()) return;
		FleetFighter ship=(FleetFighter)f;
		ship.getReady();
	}
}