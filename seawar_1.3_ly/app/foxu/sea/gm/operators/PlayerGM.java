package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 提升玩家为GM
 * @author comeback
 *
 */
public class PlayerGM extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		// 玩家名字
		String playerName=params.get("player_name");
		if(playerName==null||playerName.equals(""))
			return GMConstant.ERR_PLAYER_NAME_NULL;
		String bool=params.get("bool");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory
			.getPlayerByName(playerName,false);
		if(player==null) 
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		if(bool.equals("true"))
			player.setAttribute(PublicConst.PLAYER_GM,"1");
		else
			player.setAttribute(PublicConst.PLAYER_GM,null);
		return GMConstant.ERR_SUCCESS;
	}

}
