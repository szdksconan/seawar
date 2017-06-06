package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.text.TextKit;
import foxu.sea.LevelAbility;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/***
 * 设定 星石（舰船等级）
 * @author lhj
 *
 */
public class AddUpShip extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String upsid=params.get("sid");
		String uplevel=params.get("level");
		String playername=params.get("player_name");
		int sid=TextKit.parseInt(upsid);
		int level=TextKit.parseInt(uplevel);
		Player player=info.getObjectFactory().getPlayerByName(playername,
			false);
		if(player==null) return GMConstant.ERR_PLAYER_NAME_NULL;
		// 人物等级
		if(level>player.getLevel())
			return GMConstant.ERR_SHIP_LEVEL_ERRO;
		PlayerBuild build=player.getIsland().getBuildByIndex(
			BuildInfo.INDEX_0,null);
		if(build==null
			||build.getBuildLevel()<PublicConst.STAR_STONE_CENTER_LVL)
			return GMConstant.ERR_BUILD_IS_LEVEL_ERRO;
		LevelAbility ability=(LevelAbility)LevelAbility.factory
			.getSample(sid);
		if(ability==null) return GMConstant.ERR_LEVELABILITY_NULL;
			player.setUpShipLevel(sid,level);
		return GMConstant.ERR_SUCCESS;
	}
}
