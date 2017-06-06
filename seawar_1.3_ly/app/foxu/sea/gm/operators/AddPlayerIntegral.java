package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;

/***
 * 
 * 添加玩家积分
 * 
 * @author lhj
 * 
 */
public class AddPlayerIntegral extends GMOperator
{
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String player_name=params.get("player_name");
		Player player=info.getObjectFactory().getPlayerByName(player_name,true);
		if(player==null) return GMConstant.ERR_PLAYER_IS_EXSITS;
		String point=params.get("integral");
		int integral=TextKit.parseInt(point);
		if(integral<0)
			player.reduceIntegral(-integral);
		else 
			player.addIntegral(integral);
		JBackKit.sendPlayerIntegral(player);
		return GMConstant.ERR_SUCCESS;
	}
}
