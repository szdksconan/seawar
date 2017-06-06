package foxu.cross.server;

import mustang.util.SampleFactory;


/**
 * 跨服服务器信息
 * @author yw
 *
 */
public class CrossServer extends ServerIndex
{
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	
	/** 平台名 */
	String platName;
	/** 大区名 */
	String areaName;
	
	/** ip */
	String ip;
	/** 通讯端口（http）*/
	String port;
	
	/* methods */
	
	public String getIp()
	{
		return ip;
	}
	
	public void setIp(String ip)
	{
		this.ip=ip;
	}
	
	public String getPort()
	{
		return port;
	}

	
	public void setPort(String port)
	{
		this.port=port;
	}

	public String getAreaName()
	{
		return areaName;
	}

	
	public void setAreaName(String areaName)
	{
		this.areaName=areaName;
	}

	public String getPlatName()
	{
		return platName;
	}

	
	public void setPlatName(String platName)
	{
		this.platName=platName;
	}

	
	public static SampleFactory getFactory()
	{
		return factory;
	}

	
	public static void setFactory(SampleFactory factory)
	{
		CrossServer.factory=factory;
	}
	
	
}
