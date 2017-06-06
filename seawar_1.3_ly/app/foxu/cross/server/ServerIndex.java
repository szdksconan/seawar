package foxu.cross.server;

import mustang.util.Sample;


/**
 * 服务器信息 索引
 * @author yw
 *
 */
public class ServerIndex extends Sample
{
	/* fields */
	
	/** 平台id */
	int platid;
	/** 大区id */
	int areaid;
	/** 服务器id */
	int severid;
	/** 服务器名 */
	String severName;
	/** 国籍 */
	String national;
	
	/* methods */
	public int getPlatid()
	{
		return platid;
	}
	
	public void setPlatid(int platid)
	{
		this.platid=platid;
	}
	
	public int getAreaid()
	{
		return areaid;
	}
	
	public void setAreaid(int areaid)
	{
		this.areaid=areaid;
	}
	
	public int getSeverid()
	{
		return severid;
	}
	
	public void setSeverid(int severid)
	{
		this.severid=severid;
	}
	
	public String getSeverName()
	{
		return severName;
	}
	
	public void setSeverName(String severName)
	{
		this.severName=severName;
	}

	
	public String getNational()
	{
		return national;
	}

	
	public void setNational(String national)
	{
		this.national=national;
	}
	
}
