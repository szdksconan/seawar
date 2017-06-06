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

/** ����ǰ̨��Ҫ������2000 */
public class ForeSendPort implements TransmitHandler
{
	/** ���ݻ�ȡ�� */
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
	 * @param objectFactory Ҫ���õ� objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
}
