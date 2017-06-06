package foxu.sea.gm.operators;

import java.util.ArrayList;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.port.ChatMessagePort;


/**
 * @author yw
 * 另类禁言,玩家不察觉
 */
public class ForbidChat extends GMOperator
{

	final int NAME=1,ID=2,ACCOUNT=3;
	final int FORBID=1,REMOVE=2;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int ptype=TextKit.parseInt(params.get("ptype"));
		String param=params.get("param");
		int action=TextKit.parseInt(params.get("action"));
		CreatObjectFactory objfactory=info.getObjectFactory();
		Player player=null;
		if(ptype==NAME)
		{
			player=objfactory.getPlayerByName(param,true);
		}
		else if(ptype==ID)
		{
			player=objfactory.getPlayerById(TextKit.parseInt(param));
		}
		else if(ptype==ACCOUNT)
		{
			String sql="SELECT * FROM players WHERE players.user_id=(SELECT id FROM users WHERE users.user_account='"
				+param+"')";
			Player[] objs=(Player[])objfactory.getPlayerCache()
				.getDbaccess().loadBySql(sql);
			if(objs!=null&&objs.length>=1) player=objs[0];
		}
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		if(action==FORBID)
		{
			player.setAttribute(PublicConst.FORBID_CHAT,"1");
			ChatMessagePort chatPort=(ChatMessagePort)BackKit.getContext()
				.get("chatMessagePort");
			// 清除被禁言玩家的世界聊天
			ArrayList<ChatMessage> list=chatPort.getChatMessages();
			synchronized(list)
			{
				for(int i=0;i<list.size();i++)
				{
					ChatMessage m=list.get(i);
					if(m==null) continue;
					if(m.getSrc().equals(player.getName()))
					{
						list.remove(i);
						i--;
					}
				}
			}

		}
		else
		{
			player.setAttribute(PublicConst.FORBID_CHAT,null);
		}
		return GMConstant.ERR_SUCCESS;
	}

}
