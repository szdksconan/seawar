package foxu.sea.config;


/**
 * �������ö���
 * @author yw
 *
 */
public class ConfigCell
{
	/** ƽ̨id */
	int platid;
	/** �汾�� */
	float version;
	/** ƽ̨���� */
	String platName;
	/** ��Ʒ��ַ */
	String address;
	/** �����ַ */
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
