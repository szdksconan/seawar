package foxu.cross.warclient;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;

/**
 * ����ǰ������
 * 
 * @author yw
 * 
 */
public class ClientServerPort extends AccessPort
{

	public static int FORE_PORT=2006;
	public static final int GET_FLEET=1,// ��ȡ������Ϣ
					JION=2,// ���ý�����Ϣ || ����
					GET_REP=3,// ��ȡս����ϸ����
					BET=4,// ����Ѻע����
					GET_RANK=5,//��ȡ��������
					GET_SN=6,//��ȡ64ǿ�����Ϣ
					GET_AWAD=7,//��ȡ����
					PRE_STATE=8;//Ԥ��ս����ʾ���
	CreatObjectFactory objectFactory;

	ClientWarManager cWarManager;

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		//System.out.println("-------ClientServerPort----------type---:"+type);
		if(type==JION)
		{
			cWarManager.jionSet(player,data);
		}
		else if(type==GET_FLEET)
		{
			cWarManager.getFleet(player,data);
		}
		else if(type==GET_REP)
		{
			int rid=data.readInt();
			boolean isattack=data.readBoolean();
			//System.out.println(rid+":----rid-------:"+isattack);
			cWarManager.getRep(player,rid,isattack,data);
		}
		else if(type==BET)
		{
			cWarManager.bet(player,data);
		}
		else if(type==GET_RANK)
		{
			cWarManager.getRank(player,data);
		}
		else if(type==GET_SN)
		{
			cWarManager.getSn64(player,data);
		}
		else if(type==GET_AWAD)
		{
			int stype=data.readUnsignedByte();
			cWarManager.getAward(player,data,stype);
			data.clear();
			data.writeByte(stype);
		}
		else if(type==PRE_STATE)
		{
			player.setCrossAwardState(5,0);
		}
		else
		{
			throw new DataAccessException(0,"have no such command");
		}
		return data;
	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public ClientWarManager getcWarManager()
	{
		return cWarManager;
	}

	public void setcWarManager(ClientWarManager cWarManager)
	{
		this.cWarManager=cWarManager;
	}

}
