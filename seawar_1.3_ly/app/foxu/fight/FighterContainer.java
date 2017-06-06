package foxu.fight;

import mustang.set.Comparator;
import mustang.set.IntKeyHashMap;
import mustang.set.SetKit;

/**
 * Fighter�����࣬�ṩ��Ҫ��Fighter.
 * 
 * @author ZYT
 */
public class FighterContainer
{

	/* fields */
	/** �������� */
	IntKeyHashMap[] team=new IntKeyHashMap[2];

	/* dynamic fields */
	private Fighter[] cache;
	/**��������Ѫ��*/
	private int team1HpMax;
	/**���ط���Ѫ��*/
	private int team2HpMax;
	/**�������ִ���Ѫ��*/
	private int team1Hp;
	/**���ط��ִ���Ѫ��*/
	private int team2Hp;
	
	public int getTeamHp(int team){
		switch(team){
			case 0:
				return team1Hp;
			case 1:
				return team2Hp;
		}
		return 0;
	}
	
	public int getTeamHpMax(int team){
		switch(team){
			case 0:
				return team1HpMax;
			case 1:
				return team2HpMax;
		}
		return 0;
	}
	
	
	/**�����Ѫ��*/
	public void addHp(int team,int hp){
		switch(team)
		{
			case 0:
				team1Hp+=hp;
				break;

			case 1:
				team2Hp+=hp;
				break;
		}
	}
	/**������Ѫ��*/
	public void reduceHp(int team,int hp){
		switch(team)
		{
			case 0:
				team1Hp-=hp;
				break;

			case 1:
				team2Hp-=hp;
				break;
		}
	}
	
	
	
	
	
	public int getTeam1HpMax()
	{
		return team1HpMax;
	}


	
	public void setTeam1HpMax(int team1HpMax)
	{
		this.team1HpMax=team1HpMax;
	}


	
	public int getTeam2HpMax()
	{
		return team2HpMax;
	}


	
	public void setTeam2HpMax(int team2HpMax)
	{
		this.team2HpMax=team2HpMax;
	}


	public int getTeam2Hp()
	{
		return team2Hp;
	}
	
	public void setTeam2Hp(int team2Hp)
	{
		this.team2Hp=team2Hp;
	}
	
	public int getTeam1Hp()
	{
		return team1Hp;
	}
	public void setTeam1Hp(int team1Hp)
	{
		this.team1Hp=team1Hp;
	}
	/* methods */
	/**
	 * ���ָ������ָ��λ�õ�Fighter
	 * 
	 * @param team �����
	 * @param location λ��
	 * @return ����Fighter
	 */
	public Fighter getFighter(int team,int location)
	{
		return (Fighter)this.team[team].get(location);
	}
	/**
	 * ���һֻ����
	 * 
	 * @param team �����
	 * @return ����ָ������
	 */
	public IntKeyHashMap getTeam(int team)
	{
		return this.team[team];
	}
	/** ������ж��� */
	public IntKeyHashMap[] getAllTeam()
	{
		return team;
	}
	/** �������� */
	public void clear()
	{
		team[0].clear();
		team[1].clear();
		cache=null;
	}
	/**
	 * ���һ��Fighter
	 * 
	 * @param teamId �����
	 * @param fighters ��ս����
	 */
	public void addTeam(int teamId,IntKeyHashMap fighters)
	{
		cache=null;
		team[teamId]=fighters;
	}
	/**
	 * ���һ��fighter
	 * 
	 * @param teamId �����
	 * @param f ��ս��
	 */
	public void addFighter(int teamId,Fighter f)
	{
		cache=null;
		if(team[teamId]==null) team[teamId]=new IntKeyHashMap();
		team[teamId].put(f.getLocation(),f);
	}
	/**
	 * ָ���������һ��fighter
	 * 
	 * @param teamId �����
	 * @param f һ���ս��
	 */
	public void addFighter(int teamId,Fighter[] f)
	{
		cache=null;
		if(team[teamId]==null) team[teamId]=new IntKeyHashMap();
		for(int i=0;i<f.length;i++)
		{
			team[teamId].put(f[i].getLocation(),f);
		}
	}
	/**
	 * �Ƴ�һ��fighter
	 * 
	 * @param f ս���е�һ��fighter
	 * @return ����true��ʾ�ɹ��Ƴ�
	 */
	public boolean removeFighter(Fighter f)
	{
		cache=null;
		return team[f.getTeam()].remove(f.getLocation())!=null;
	}
	/**
	 * ���ս������Fighter
	 * 
	 * @return ��������Fighter����
	 */
	public Fighter[] getAllFighter()
	{
		if(cache==null)
		{
			Fighter[] fighters=new Fighter[team[0].size()+team[1].size()];
			Object[] team0=team[0].valueArray();
			Object[] team1=team[1].valueArray();
			int i=0,j=0;
			for(;i<fighters.length;i++)
			{
				if(i<team0.length)
				{
					fighters[i]=(Fighter)team0[j++];
					if(j>=team0.length) j=0;
				}
				else
				{
					fighters[i]=(Fighter)team1[j++];
				}
			}
			cache=fighters;
		}
		return cache;
	}
	/**
	 * ȫ����fighter����
	 * 
	 * @param com ��������
	 * @return ������������fighter����
	 */
	public Fighter[] sort(Comparator com)
	{
		Fighter[] tf=getAllFighter();
		if(com==null) return tf;
		SetKit.sort(tf,com,true);
		return tf;
	}
}