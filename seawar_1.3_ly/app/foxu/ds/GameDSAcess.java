package foxu.ds;

import mustang.io.ByteBuffer;
import shelby.ds.DSAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;


public class GameDSAcess implements DSAccess
{

	/* fields */
	CreatObjectFactory factory;
	
	/* properties */
	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}
	
	/* methods */
	public boolean canAccess()
	{
		return true;
	}
	public void login(int id,String sid,String address)
	{
	}
	public Object load(int id)
	{
		Player player=factory.getPlayerById(id);
		return player;
	}
	public void save(int id,boolean exit,ByteBuffer data)
	{
		factory.getPlayerById(id);
	}
	public ByteBuffer update(int id,ByteBuffer args,ByteBuffer data)
	{
		return null;
	}
}