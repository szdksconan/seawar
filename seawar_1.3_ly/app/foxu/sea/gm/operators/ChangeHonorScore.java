package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * ÃÌº”»Ÿ”˛÷µ
 * @author lhj
 *
 */
public class ChangeHonorScore extends GMOperator
{
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		Player player=info.getObjectFactory().getPlayerByName(playerName,false);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		String score=params.get("score");
		int honorScore=TextKit.parseInt(score);
		player.changeHonorScore(honorScore);
		return GMConstant.ERR_SUCCESS;
	}
}
