package foxu.sea;

import mustang.util.TimeKit;

/** 密码重置记录 */
public class PasswordRecord
{

	/** 标题 */
	String deviceId;
	/** 内容 */
	String userAccount;
	/** 创建时间 */
	int creatTime;
	
	
	public PasswordRecord()
	{
		super();
	}

	public PasswordRecord(String deviceId,String userAccount,int creatTime)
	{
		super();
		this.deviceId=deviceId;
		this.userAccount=userAccount;
		this.creatTime=creatTime;
	}

	public String getDeviceId()
	{
		return deviceId;
	}
	
	public void setDeviceId(String deviceId)
	{
		this.deviceId=deviceId;
	}
	
	public String getUserAccount()
	{
		return userAccount;
	}
	
	public void setUserAccount(String userAccount)
	{
		this.userAccount=userAccount;
	}
	
	public int getCreatTime()
	{
		return creatTime;
	}
	
	public void setCreatTime(int creatTime)
	{
		this.creatTime=creatTime;
	}

}
