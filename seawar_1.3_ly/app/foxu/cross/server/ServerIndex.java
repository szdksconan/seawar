package foxu.cross.server;

import mustang.util.Sample;


/**
 * ��������Ϣ ����
 * @author yw
 *
 */
public class ServerIndex extends Sample
{
	/* fields */
	
	/** ƽ̨id */
	int platid;
	/** ����id */
	int areaid;
	/** ������id */
	int severid;
	/** �������� */
	String severName;
	/** ���� */
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
