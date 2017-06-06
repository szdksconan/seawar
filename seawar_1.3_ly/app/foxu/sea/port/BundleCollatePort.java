package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.net.TransmitHandler;
import foxu.sea.Player;
import foxu.sea.proplist.PropComparator;
import foxu.sea.proplist.PropList;


/**
 * �������� 3157
 * 
 * @author LXH
 * @version 2010-3-22 ����11:50:23
 */
public class BundleCollatePort implements TransmitHandler
{
	/** �����ܶ˿ڼ�TYPE���ͳ��� */
	public static final int NEATEN_BUNDLE=2120,// �����������ͳ���
					NEATEN_GODOWN=2121;// �ֿ��������ͳ���

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
		//��������
		if(type==NEATEN_BUNDLE)
		{
			PropList bund = player.getBundle();
			bund.wrapProp();
			bund.collate(PropComparator.getInstance(),false);
		}
	}
}
