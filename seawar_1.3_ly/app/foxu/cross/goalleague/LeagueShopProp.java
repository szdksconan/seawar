package foxu.cross.goalleague;

/**
 * �����̵������Ʒ
 * 
 * @author Alan
 * 
 */
public class LeagueShopProp
{

	/** ��Ʒsid */
	int propSid;
	/** ���Ҽ۸� */
	int costCoin;
	/** �������� */
	int limit;
	/** ϡ�еȼ� */
	int level;

	public LeagueShopProp()
	{
		super();
	}

	public LeagueShopProp(int propSid,int costCoin,int limit,int level)
	{
		super();
		this.propSid=propSid;
		this.costCoin=costCoin;
		this.limit=limit;
		this.level=level;
	}

	public int getPropSid()
	{
		return propSid;
	}

	public void setPropSid(int propSid)
	{
		this.propSid=propSid;
	}

	public int getCostCoin()
	{
		return costCoin;
	}

	public void setCostCoin(int costCoin)
	{
		this.costCoin=costCoin;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit=limit;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level=level;
	}

}
