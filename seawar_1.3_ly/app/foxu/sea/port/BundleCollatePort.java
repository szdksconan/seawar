package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.net.TransmitHandler;
import foxu.sea.Player;
import foxu.sea.proplist.PropComparator;
import foxu.sea.proplist.PropList;


/**
 * 包裹整理 3157
 * 
 * @author LXH
 * @version 2010-3-22 上午11:50:23
 */
public class BundleCollatePort implements TransmitHandler
{
	/** 整理功能端口及TYPE类型常量 */
	public static final int NEATEN_BUNDLE=2120,// 包裹整理类型常量
					NEATEN_GODOWN=2121;// 仓库整理类型常量

	public void transmit(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return;
		}
		Session session=(Session)(connect.getSource());
		Player player=(Player)(session.getSource());
		int type = data.readUnsignedShort();
		//包裹整理
		if(type==NEATEN_BUNDLE)
		{
			PropList bund = player.getBundle();
			bund.wrapProp();
			bund.collate(PropComparator.getInstance(),false);
		}
	}
}
