package foxu.cross.goalleague;

/**
 * 联赛商店代币物品
 * 
 * @author Alan
 * 
 */
public class LeagueShopProp
{

	/** 物品sid */
	int propSid;
	/** 代币价格 */
	int costCoin;
	/** 购买限制 */
	int limit;
	/** 稀有等级 */
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
