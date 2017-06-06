package foxu.combine;

import mustang.orm.ConnectionManager;
import mustang.orm.SqlPersistence;

/**
 * 服务器信息
 * @author comeback
 *
 */
public class Server
{
	ConnectionManager cm;
	
	String nameSuffix;
	
	/** 添加给玩家的道具 */
	int[] addProps;
	
	/** 添加给玩家的宝石 */
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
