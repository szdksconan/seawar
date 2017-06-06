package foxu.sea.port;

import foxu.sea.Player;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;

/** 系统设置 1013 */
public class SystemPort extends AccessPort
{
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session session=(Session)(connect.getSource());
		Player player=(Player)(session.getSource());
		if(player==null)
		{
			connect.close();
			return null;
		}
		if(player.getIsland()==null)
		{
			connect.close();
			return null;
		}
		// index
		int index=data.readUnsignedByte();
		// 最高30位
		if(index<0||index>30) throw new DataAccessException(0,"fail");
		boolean yesOrNo=data.readBoolean();
		int tag=1;
		tag=tag<<index;
		int iosSystem=player.getIsland().getIosSystem();
		// 当前位为1
		if((iosSystem&tag)!=0)
		{
			if(!yesOrNo)
			{
				iosSystem^=tag;
			}
		}
		// 当前位为0
		else
		{
			if(yesOrNo)
			{
				iosSystem|=tag;
			}
		}
		player.getIsland().setIosSystem(iosSystem);
		data.clear();
		data.writeBoolean(yesOrNo);
		return data;
	}
}
