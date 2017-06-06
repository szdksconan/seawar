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
 * 新手指引标记
 * @author comeback
 *
 */
public class GuidMark extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String stepStr=params.get("step");
		if(playerName==null||stepStr==null||playerName.length()==0||stepStr.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		int step=Integer.parseInt(stepStr);
		player.setTaskMark(step);
		player.setPlayerTaskMark(step);
		// 重置新手引导标识
		player.setAttribute(PublicConst.NEW_FOLLOW_PLAYER,"t");
		return GMConstant.ERR_SUCCESS;
	}

}
