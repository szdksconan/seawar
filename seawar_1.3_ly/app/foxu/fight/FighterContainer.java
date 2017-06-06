package foxu.fight;

import mustang.set.Comparator;
import mustang.set.IntKeyHashMap;
import mustang.set.SetKit;

/**
 * Fighter容器类，提供需要的Fighter.
 * 
 * @author ZYT
 */
public class FighterContainer
{

	/* fields */
	/** 队伍数组 */
	IntKeyHashMap[] team=new IntKeyHashMap[2];

	/* dynamic fields */
	private Fighter[] cache;
	/**进攻方总血量*/
	private int team1HpMax;
	/**防守方总血量*/
	private int team2HpMax;
	/**进攻方现存总血量*/
	private int team1Hp;
	/**防守方现存总血量*/
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
	
	
	/**添加总血量*/
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
	/**减少总血量*/
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
	 * 获得指定队伍指定位置的Fighter
	 * 
	 * @param team 队伍号
	 * @param location 位置
	 * @return 返回Fighter
	 */
	public Fighter getFighter(int team,int location)
	{
		return (Fighter)this.team[team].get(location);
	}
	/**
	 * 获得一只队伍
	 * 
	 * @param team 队伍号
	 * @return 返回指定队伍
	 */
	public IntKeyHashMap getTeam(int team)
	{
		return this.team[team];
	}
	/** 获得所有队伍 */
	public IntKeyHashMap[] getAllTeam()
	{
		return team;
	}
	/** 清理容器 */
	public void clear()
	{
		team[0].clear();
		team[1].clear();
		cache=null;
	}
	/**
	 * 添加一队Fighter
	 * 
	 * @param teamId 队伍号
	 * @param fighters 参战队伍
	 */
	public void addTeam(int teamId,IntKeyHashMap fighters)
	{
		cache=null;
		team[teamId]=fighters;
	}
	/**
	 * 添加一个fighter
	 * 
	 * @param teamId 队伍号
	 * @param f 参战者
	 */
	public void addFighter(int teamId,Fighter f)
	{
		cache=null;
		if(team[teamId]==null) team[teamId]=new IntKeyHashMap();
		team[teamId].put(f.getLocation(),f);
	}
	/**
	 * 指定队伍添加一组fighter
	 * 
	 * @param teamId 队伍号
	 * @param f 一组参战者
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
	 * 移除一个fighter
	 * 
	 * @param f 战场中的一个fighter
	 * @return 返回true表示成功移除
	 */
	public boolean removeFighter(Fighter f)
	{
		cache=null;
		return team[f.getTeam()].remove(f.getLocation())!=null;
	}
	/**
	 * 获得战场所有Fighter
	 * 
	 * @return 返回所有Fighter数组
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
	 * 全场景fighter排序
	 * 
	 * @param com 排序条件
	 * @return 返回排序过后的fighter数组
	 */
	public Fighter[] sort(Comparator com)
	{
		Fighter[] tf=getAllFighter();
		if(com==null) return tf;
		SetKit.sort(tf,com,true);
		return tf;
	}
}