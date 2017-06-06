package foxu.sea.activity;

/***
 * 
 * 活动记录器
 * @author lhj
 *
 */
public class ActivityRecord
{
	/**购买时间**/
	int createAt;
	/**购买次数**/
	int times;
	/**玩家id**/
	int playerId;
	
	
	/**增加**/
	public void add()
	{
		times++;
	}
	
	
	public int getCreateAt()
	{
		return createAt;
	}
	
	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}
	
	public int getTimes()
	{
		return times;
	}
	
	public void setTimes(int times)
	{
		this.times=times;
	}
	
	
	public int getPlayerId()
	{
		return playerId;
	}

	
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	
}
