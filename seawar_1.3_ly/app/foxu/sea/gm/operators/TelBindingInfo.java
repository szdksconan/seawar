package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.bind.BindingTrack;
import foxu.sea.bind.TelBindingManager;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * µç»°°ó¶¨
 * 
 * @author Alan
 * 
 */
public class TelBindingInfo extends GMOperator
{

	public final static int CHECK=1,EDIT=2,DELETE=3;
	TelBindingManager telManager;

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
		String typeStr=params.get("type");
		String zone=params.get("zone");
		String phone=params.get("phone");
		if(zone==null||!zone.matches("\\d+"))
			return GMConstant.ERR_PARAMATER_ERROR;
		int type=TextKit.parseInt(typeStr);
		if(type==EDIT)
		{
			if(telManager.updatePlayerTel(player,
				telManager.formatWholeTel(zone,phone),BindingTrack.GM)!=null)
				return GMConstant.ERR_UNKNOWN;
		}
		else if(type==DELETE)
		{
			if(telManager.updatePlayerTel(player,"",BindingTrack.GM)!=null)
				return GMConstant.ERR_UNKNOWN;
		}
		try
		{
			JSONObject json=new JSONObject();
			json.put("name",playerName);
			json.put("tel",TelBindingManager.getPlayerWholeBindingTel(
				player,objectFactory));
			jsonArray.put(json);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}

	public TelBindingManager getTelManager()
	{
		return telManager;
	}

	public void setTelManager(TelBindingManager telManager)
	{
		this.telManager=telManager;
	}

}
