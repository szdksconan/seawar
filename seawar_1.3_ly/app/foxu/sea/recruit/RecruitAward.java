package foxu.sea.recruit;

/**
 * 新兵奖励（包括半价抢购）
 * 
 * @author yw
 * 
 */
public class RecruitAward
{

	/** 奖励sid（可能是物品，可能是award） */
	int sid;
	/** 记录时 key值 */
	int key;
	/** 记录时 value值 */
	int value;

	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		this.sid=sid;
	}

	public int getKey()
	{
		return key;
	}

	public void setKey(int key)
	{
		this.key=key;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value=value;
	}

}
