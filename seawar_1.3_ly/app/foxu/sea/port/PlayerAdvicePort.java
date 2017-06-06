package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.Session;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;

/**
 * ��ҽ���BUG�ύ�˿� port:1001
 */
public class PlayerAdvicePort extends AccessPort
{

	/** ���ݻ�ȡ�� */
	CreatObjectFactory objectFactory;

	/***/
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session session=(Session)(connect.getSource());
		Player player=(Player)(session.getSource());
		if(player==null) return null;
		// ����
		String title=data.readUTF();
		// ����
		String content=data.readUTF();
		objectFactory.savePlayerAdvice(player.getId(),player
			.getName(),title,content);
		data.clear();
		data.writeUTF("ok");
		return data;
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
