package foxu.cross.server;


/**
 * 跨服玩家
 * @author yw
 *
 */
public class CrossPlayer extends ServerIndex
{
	/* fields */
	
	/** 跨服服务器唯一玩家id */
	int crossid;
	/** 原服务器唯一玩家id */
	int id;
	/** 名字 */
	String name;
	
	public int getCrossid()
	{
		return crossid;
	}
	
	public void setCrossid(int crossid)
	{
		this.crossid=crossid;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
}
