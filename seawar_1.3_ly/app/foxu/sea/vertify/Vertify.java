package foxu.sea.vertify;

/**
 * ��֤����
 */
public class Vertify
{

	/** ��֤�������� */
	int operateCount = 0;
	/** �涨ʱ�����״β�����ʱ�� */
	long operateTime = 0L;
	/** ��֤ʧ�ܴ��� */
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
