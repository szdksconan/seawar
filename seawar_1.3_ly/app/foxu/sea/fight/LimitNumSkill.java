package foxu.sea.fight;

/**
 * ���ܼ�¼�� ��¼��Ҫ�������Ƶļ���
 * @author liuh
 *
 */
public class LimitNumSkill
{
	/** ��һ��*/
	int team;
	/** ����ID**/
	int sid;
	/** �Ѿ�ʹ�ô��� **/
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
