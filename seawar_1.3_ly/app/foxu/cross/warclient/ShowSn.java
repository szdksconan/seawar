package foxu.cross.warclient;


/**
 * nǿ����
 * @author yw
 *
 */
public class ShowSn
{
	/** �������� */
	String servername;
	/** ����� */
	String name;
	/** nǿ */
	int rank;
	
	public ShowSn(String servername,String name,int rank)
	{
		this.servername=servername;
		this.name=name;
		this.rank=rank;
	}
	public String getServername()
	{
		return servername;
	}
	
	public void setServername(String servername)
	{
		this.servername=servername;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public int getRank()
	{
		return rank;
	}
	
	public void setRank(int rank)
	{
		this.rank=rank;
	}
	
}
