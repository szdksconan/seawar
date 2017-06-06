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
 * 接收前端数据
 * 
 * @author yw
 * 
 */
public class ClientServerPort extends AccessPort
{

	public static int FORE_PORT=2006;
	public static final int GET_FLEET=1,// 获取舰队信息
					JION=2,// 设置舰队信息 || 报名
					GET_REP=3,// 获取战报详细内容
					BET=4,// 发送押注请求
					GET_RANK=5,//获取人气排行
					GET_SN=6,//获取64强玩家信息
					GET_AWAD=7,//获取奖励
					PRE_STATE=8;//预赛战报显示标记
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
