package foxu.sea.port;

import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.achieve.AchieveManager;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;

/**
 * ³É¾Í¶Ë¿Ú
 * @author yw
 *
 */
public class AchievementPort extends AccessPort
{

	final static int GET_AWARD=1,SHARE=2;
	@Override
	public ByteBuffer access(Connect c,ByteBuffer data)
	{
		if(c.getSource()==null)
		{
			c.close();
			return null;
		}
		Session s=(Session)(c.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			c.close();
			return null;
		}
		int type=data.readUnsignedByte();
		if(type==GET_AWARD)
		{
			String res=AchieveManager.instance.getAchieveAward(data,player);
			if(res!=null)
			{
				throw new DataAccessException(0,res);			
			}
		}else if(type==SHARE)
		{
			AchieveCollect.shareGame(player);
		}
		return data;
	}

}
