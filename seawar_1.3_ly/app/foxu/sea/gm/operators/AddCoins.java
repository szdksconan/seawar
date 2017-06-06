package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.officer.CoinsTrack;

/***
 * Ìí¼Ó2¼¶»õ±Ò
 * 
 * @author lhj
 * 
 */
public class AddCoins extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String coins=params.get("coins");
		CreatObjectFactory factory=info.getObjectFactory();
		Player player=factory.getPlayerByName(playerName,true);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		int num=TextKit.parseInt(coins);
		if(num>0)
		{
			player.getOfficers().incrCoins(num);
			factory.createCoinsTrack(CoinsTrack.ADD_BY_GM,
				player.getId(),0,num,
				(int)player.getOfficers().getCoins(),
				CoinsTrack.ADD);
		}
		else
		{
			player.getOfficers().descCoins(-num);
			factory.createCoinsTrack(CoinsTrack.ADD_BY_GM,
				player.getId(),0,-num,
				(int)player.getOfficers().getCoins(),
				CoinsTrack.DESC);
		}
		return GMConstant.ERR_SUCCESS;
	}

}
