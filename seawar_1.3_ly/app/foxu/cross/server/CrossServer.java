package foxu.cross.server;

import mustang.util.SampleFactory;


/**
 * �����������Ϣ
 * @author yw
 *
 */
public class CrossServer extends ServerIndex
{
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	
	/** ƽ̨�� */
	String platName;
	/** ������ */
	String areaName;
	
	/** ip */
	String ip;
	/** ͨѶ�˿ڣ�http��*/
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
