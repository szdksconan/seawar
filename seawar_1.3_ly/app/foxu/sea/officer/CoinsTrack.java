package foxu.sea.officer;

/***
 * 
 * @author 军魂记录
 *
 */
public class CoinsTrack
{

	public static int ADD=0,DESC=1;

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

	/** 变动原因 **/
	/***
	 * BUY_OFFICER=1 军官商店消费 FORM_DIS_OFFICER=2,遣散军官 ADD_BY_GM=3 gm添加
	 */
	public static int BUY_OFFICER=1,FORM_DIS_OFFICER=2,ADD_BY_GM=3;

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
