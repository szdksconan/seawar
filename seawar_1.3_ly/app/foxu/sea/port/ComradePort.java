package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.text.TextKit;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.comrade.ComradeHandler;


/**
 * 战友系统 端口
 * @author yw
 *
 */
public class ComradePort extends AccessPort
{

	/** GET_COMRADE获取战友信息 COMRADEPORT_AWARD=2 获取奖励 */
	public static int ADD_NEWCOMRADE=1,COMRADEPORT_AWARD=2;

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
		/** 获取奖励 **/
		 if(type==COMRADEPORT_AWARD)
		{
			ComradeHandler.getInstance().getAward(data,player);
		}
		return data;
	}

}
