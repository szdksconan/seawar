package foxu.sea.alliance.alliancebattle;


/***
 * 科技点和物资记录的日志
 * @author lhj
 *
 */
public class SciencePointTrack
{
	/** 日志类型 */
	public static final int ADD=0,REDUCE=1;
	/**
	 * 尖端科技 增加 FROM_ALLIANCE_FIGHT=1 联盟战战斗结束以后获取 FROM_DAY_SNED=2 联盟战中占领的岛屿
	 * FROM_GIVE_VALUES=3 捐献获取 * 减少 USER_ALLIANCE_SKILL=4 升级技能
	 */
	public static final int FROM_ALLIANCE_FIGHT=1,FROM_DAY_SNED=2,
					FROM_GIVE_VALUES=3,USE_ALLIANCE_SKILL=4;

	/**
	 * 物资增加：FROM_GIVE_VALUE=1 联盟捐献 REBACK_BY_COMBINED=2 系统返回物资(合服)
	 * REBACK_BY_BEYOND=4 竞标失败(被超过了) BET_ISLAND=3 竞标联盟战岛屿
	 **/
	public static final int FROM_GIVE_VALUE=1,REBACK_BY_COMBINED=2,
					REBACK_BY_BEYOND=4,BET_ISLAND=3;
	
	/**MATERIAL=1 物资 SCIENCE_POINT=2 科技点 **/
	public static final int MATERIAL=1,SCIENCE_POINT=2;
	/** 记录ID */
	int id;
	/** 日志类型 */
	int type;
	/** 玩家ID */
	int playerId;
	/**联盟id**/
	int allianceId;
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
	long nowLeft;
	/** 增加还是减少 **/
	int state;
	/**物资是科技点**/
	int  style;
	/**额外信息**/
	String extra;
	
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
	
	
	public int getAllianceId()
	{
		return allianceId;
	}

	
	public void setAllianceId(int allianceId)
	{
		this.allianceId=allianceId;
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

	
	public int getState()
	{
		return state;
	}
	
	public void setState(int state)
	{
		this.state=state;
	}

	
	public int getPlayerId()
	{
		return playerId;
	}

	
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	
	public long getNowLeft()
	{
		return nowLeft;
	}

	
	public void setNowLeft(long nowLeft)
	{
		this.nowLeft=nowLeft;
	}

	
	public int getStyle()
	{
		return style;
	}

	
	public void setStyle(int style)
	{
		this.style=style;
	}

	
	public String getExtra()
	{
		return extra;
	}

	
	public void setExtra(String extra)
	{
		this.extra=extra;
	}
	
	
	
}
