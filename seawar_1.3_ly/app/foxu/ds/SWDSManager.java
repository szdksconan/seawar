/**
 * 
 */
package foxu.ds;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import shelby.ds.DSManager;
import foxu.sea.Player;


/**
 * @author rockzyt
 *
 */
public class SWDSManager extends DSManager
{

	public SWDSManager()
	{
		super();
	}
	/**
	 * @param sm
	 */
	public SWDSManager(SessionMap sm)
	{
		super(sm);
	}
	
	/* methods */
	public Object createObj(Object bb)
	{
		return bb;
	}
	/** ������෽�� */
	public void setSession(Session s,Object obj,Object data)
	{
		Player p=(Player)obj;
		s.setAttribute(Player.KEY_ID,new Integer(p.getId()));
		p.setSource(s);
	}
	/** ������෽�� */
	public void saves(ArrayList list,long time,ByteBuffer data)
	{
	}
	/** ������෽�� */
	public DataAccessException saveSession(Session s,boolean exit,
		ByteBuffer data)
	{
		return null;
	}
	
	public ByteBuffer load(Session session,ByteBuffer data)
	{
		data=super.load(session,data);
		
//		Player player=(Player)session.getSource();
//		if(player==null)
//			return data;
//		if(player.isTakeOver())
//		{
//			throw new DataAccessException(0,"user is banned");
//		}
		return data;
	}
}