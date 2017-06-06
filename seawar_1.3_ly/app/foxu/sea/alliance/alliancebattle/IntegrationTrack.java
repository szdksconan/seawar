package foxu.sea.alliance.alliancebattle;

/***
 * 积分
 * 
 * @author lhj
 * 
 */
public class IntegrationTrack
{

	/** 日志类型 */
	public static final int ADD=0,REDUCE=1;
	/**
	 * 增加 FROM_GIVE_VALUE=1 从捐献中获取
	 */
	public static final int   FROM_GIVE_VALUE=1;
	/**
	 * 减少 FROM_ALLIANCE_SHOP=2 从商店中购买东西
	 */
	public static final int FROM_ALLIANCE_SHOP=2;
	/** 记录ID */
	int id;
	/** 日志类型 */
	int type;
	/** 玩家ID */
	int playerId;
	/** 购买物品的sid **/
	int propSid;
	/** 消耗积分的数量 */
	int num;
	/** 创建时间 */
	int createAt;
	/** 年 */
	int year;
	/** 月 */
	int month;
	/** 日 */
	int day;
	/** 当前剩余 */
	int nowLeft;
	/** 增加还是减少 **/
	int state;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id=id;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type=type;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	public int getPropSid()
	{
		return propSid;
	}

	public void setPropSid(int propSid)
	{
		this.propSid=propSid;
	}

	public int getNum()
	{
		return num;
	}

	public void setNum(int num)
	{
		this.num=num;
	}

	public int getCreateAt()
	{
		return createAt;
	}

	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}

	public int getYear()
	{
		return year;
	}

	public void setYear(int year)
	{
		this.year=year;
	}

	public int getMonth()
	{
		return month;
	}

	public void setMonth(int month)
	{
		this.month=month;
	}

	public int getDay()
	{
		return day;
	}

	public void setDay(int day)
	{
		this.day=day;
	}

	public int getNowLeft()
	{
		return nowLeft;
	}

	public void setNowLeft(int nowLeft)
	{
		this.nowLeft=nowLeft;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state=state;
	}

}
