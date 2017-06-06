package foxu.cross.warclient;


/**
 * n强播报
 * @author yw
 *
 */
public class ShowSn
{
	/** 服务器名 */
	String servername;
	/** 玩家名 */
	String name;
	/** n强 */
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
