package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.DonateRank;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/***
 * 设置联盟捐献点
 * 
 * @author lhj
 * 
 */
public class SetAlliancePlayerValue extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.loadOnly(player.getAttributes(PublicConst.ALLIANCE_ID));
		if(alliance==null)
		{
			return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
		}
		// 设置的捐献点值
		String value=params.get("value");
		 int alliance_value=TextKit.parseInt(value);
		DonateRank rank=(DonateRank)alliance.getGiveValue().get(player.getId());
		if(rank==null)
		{
			rank=new DonateRank();
			rank.setPlayerId(player.getId());
			alliance.getGiveValue().put(player.getId(),rank);
		}
		if(alliance_value<0) alliance_value=0;
		if((int)(rank.getTotleValue()+alliance_value)<0)
			rank.setTotleValue(Integer.MAX_VALUE);
		else
			rank.setTotleValue((int)(rank.getTotleValue()+alliance_value));
		return GMConstant.ERR_SUCCESS;
	}

}
