package foxu.combine;

import mustang.orm.ConnectionManager;
import mustang.orm.SqlPersistence;

/**
 * ��������Ϣ
 * @author comeback
 *
 */
public class Server
{
	ConnectionManager cm;
	
	String nameSuffix;
	
	/** ��Ӹ���ҵĵ��� */
	int[] addProps;
	
	/** ��Ӹ���ҵı�ʯ */
	int addGems;
	
	public void setAddProps(int[] props)
	{
		this.addProps=props;
	}
	
	public int[] getAddProps()
	{
		return this.addProps;
	}
	
	public void setAddGems(int gems)
	{
		this.addGems=gems;
	}
	
	public int getAddGems()
	{
		return this.addGems;
	}
	
	public void setConnectionManager(ConnectionManager cm)
	{
		this.cm=cm;
	}
	
	public ConnectionManager getConnectionManager()
	{
		return this.cm;
	}
	
	public void setNameSuffix(String suffix)
	{
		this.nameSuffix=suffix;
	}
	
	public String getNameSuffix()
	{
		return this.nameSuffix;
	}
	
	public SqlPersistence getPersistence(String table)
	{
		SqlPersistence p=new SqlPersistence();
		p.setConnectionManager(cm);
		p.setTable(table);
		return p;
	}
}
