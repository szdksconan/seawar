package foxu.sea.activity;

/***
 * 
 * ���¼��
 * @author lhj
 *
 */
public class ActivityRecord
{
	/**����ʱ��**/
	int createAt;
	/**�������**/
	int times;
	/**���id**/
	int playerId;
	
	
	/**����**/
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
