package foxu.sea.vertify;

/**
 * 验证器类
 */
public class Vertify
{

	/** 验证操作次数 */
	int operateCount = 0;
	/** 规定时间内首次操作的时间 */
	long operateTime = 0L;
	/** 验证失败次数 */
	int vertifyWrongCount=1;

	public int getOperateCount()
	{
		return operateCount;
	}

	public void setOperateCount(int operateCount)
	{
		this.operateCount=operateCount;
	}

	public long getOperateTime()
	{
		return operateTime;
	}

	public void setOperateTime(long operateTime)
	{
		this.operateTime=operateTime;
	}

	public int getVertifyWrongCount()
	{
		return vertifyWrongCount;
	}

	public void setVertifyWrongCount(int vertifyWrongCount)
	{
		this.vertifyWrongCount=vertifyWrongCount;
	}

}
