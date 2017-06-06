package foxu.sea.fight;

/**
 * 技能记录类 记录需要次数限制的技能
 * @author liuh
 *
 */
public class LimitNumSkill
{
	/** 哪一方*/
	int team;
	/** 技能ID**/
	int sid;
	/** 已经使用次数 **/
	int count;
	
	
	public LimitNumSkill(int team,int sid,int count)
	{
		super();
		this.team=team;
		this.sid=sid;
		this.count=count;
	}

	public int getCount()
	{
		return count;
	}
	
	public void setCount(int count)
	{
		this.count=count;
	}

	
	public int getTeam()
	{
		return team;
	}

	
	public void setTeam(int team)
	{
		this.team=team;
	}

	
	public int getSid()
	{
		return sid;
	}

	
	public void setSid(int sid)
	{
		this.sid=sid;
	}

	
	
}
