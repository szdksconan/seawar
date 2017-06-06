package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import mustang.util.TimeKit;
import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.cross.goalleague.ClientLeagueManager;
import foxu.cross.goalleague.LeaguePlayer;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 编辑跨服积分赛玩家信息
 * 
 * @author Alan
 * 
 */
public class EditPlayerLeagueInfo extends GMOperator
{
	public static final int VIEW=1,ADD_GOAL=2,ADD_COIN=3;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int type=TextKit.parseInt(params.get("type"));
		String playerName=params.get("player_name");
		if(playerName==null)return GMConstant.ERR_PLAYER_NAME_NULL;
		Player player=info.getObjectFactory().getPlayerByName(playerName,false);
		if(player==null)return GMConstant.ERR_PLAYER_NOT_EXISTS;
		ClientLeagueManager clm=info.getObjectFactory().getClientLeagueManager();
		int time=TimeKit.getSecondTime();
		if(type==ADD_GOAL)
		{
			int goal=TextKit.parseInt(params.get("addtion"));
			clm.checkLeaguePlayerInNewDay(time,player,true);
			clm.addLeagueGoal(player,goal,time);
		}
		else if(type==ADD_COIN)
		{
			int coin=TextKit.parseInt(params.get("addtion"));
			clm.checkLeaguePlayerInNewDay(time,player,true);
			clm.addLeagueCoin(player,coin);
		}
		LeaguePlayer lp=clm.getLeaguePlayer(player);
		JSONObject json=new JSONObject();
		try
		{
			json.put("player_name",playerName);
			json.put("goal",lp.getGoal());
			json.put("coin",lp.getBet());
			jsonArray.put(json);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}

}
