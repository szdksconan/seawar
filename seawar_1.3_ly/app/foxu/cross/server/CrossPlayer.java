package foxu.cross.server;


/**
 * ������
 * @author yw
 *
 */
public class CrossPlayer extends ServerIndex
{
	/* fields */
	
	/** ���������Ψһ���id */
	int crossid;
	/** ԭ������Ψһ���id */
	int id;
	/** ���� */
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
