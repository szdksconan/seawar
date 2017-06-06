package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.net.TransmitHandler;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.kit.JBackKit;

/** 接受前台需要的推送2000 */
public class ForeSendPort implements TransmitHandler
{
	/** 数据获取类 */
	CreatObjectFactory objectFactory;

	public void transmit(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return;
		}
		Session session=(Session)(connect.getSource());
		Player player=(Player)(session.getSource());
		if(player==null) return;
		int type=data.readUnsignedByte();
		if(type==PublicConst.FORE_TASK_SEND)
		{
			objectFactory.pushAll(player,TimeKit.getSecondTime());
		}
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory 要设置的 objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
}
