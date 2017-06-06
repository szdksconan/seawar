package foxu.sea.config;


/**
 * 单个配置对象
 * @author yw
 *
 */
public class ConfigCell
{
	/** 平台id */
	int platid;
	/** 版本号 */
	float version;
	/** 平台名称 */
	String platName;
	/** 产品地址 */
	String address;
	/** 分享地址 */
	String share;
	
	
	public int getPlatid()
	{
		return platid;
	}
	
	public void setPlatid(int platid)
	{
		this.platid=platid;
	}
	
	public String getPlatName()
	{
		return platName;
	}
	
	public void setPlatName(String platName)
	{
		this.platName=platName;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public void setAddress(String address)
	{
		this.address=address;
	}
	
	public String getShare()
	{
		return share;
	}
	
	public void setShare(String share)
	{
		this.share=share;
	}
	
	public float getVersion()
	{
		return version;
	}
	
	public void setVersion(float version)
	{
		this.version=version;
	}
	
	

}
