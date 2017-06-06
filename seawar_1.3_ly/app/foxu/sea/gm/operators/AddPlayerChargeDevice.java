package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 
 * 添加玩家的充值设备
 * 
 * @author lhj
 * 
 */
public class AddPlayerChargeDevice extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String playerName=params.get("playerName");
			String device=params.get("pdid");
			if(playerName==null||playerName.trim().length()==0)
				return GMConstant.ERR_PLAYER_NOT_EXISTS;
			if(device==null||device.length()==0)
				return GMConstant.ERR_PARAMATER_ERROR;
			Player player=info.getObjectFactory().getPlayerByName(
				playerName,true);
			if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
			SeaBackKit.setPlayerPaymentDevices(player,device);
			JSONObject js=new JSONObject();
			js.put("device",SeaBackKit.getPlayerPaymentDevices(player));
			jsonArray.put(js);
		}
		catch(Exception e)
		{

		}
		return GMConstant.ERR_SUCCESS;
	}
}
