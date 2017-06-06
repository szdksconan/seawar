package foxu.sea.activity;

import mustang.util.TimeKit;


/**
 * ���־
 * @author yw
 *
 */
public class ActivityLog
{
	/** �id */
	int aid;
	/** ��Ʒsid/���� */
	String sid;
	/** ���id */
	int pid;
	/** ���ı�ʯ*/
	int gems;
	/** ����ʱ�� */
	int create_at;
	
	public ActivityLog(int aid,String sid,int pid,int gems)
	{
		this.aid=aid;
		this.sid=sid;
		this.pid=pid;
		this.gems=gems;
		create_at=TimeKit.getSecondTime();
	}
	public int getAid()
	{
		return aid;
	}
	
	public void setAid(int aid)
	{
		this.aid=aid;
	}
	
	public String getSid()
	{
		return sid;
	}
	
	public void setSid(String sid)
	{
		this.sid=sid;
	}
	
	public int getPid()
	{
		return pid;
	}
	
	public void setPid(int pid)
	{
		this.pid=pid;
	}
	
	public int getGems()
	{
		return gems;
	}
	
	public void setGems(int gems)
	{
		this.gems=gems;
	}
	public int getCreate_at()
	{
		return create_at;
	}
	
	public void setCreate_at(int create_at)
	{
		this.create_at=create_at;
	}
	

}
