package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.proplist.PropList;


public class GetPlayerBundle extends GMOperator
{
	
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		if(playerName==null||playerName.length()==0)
		{
			return GMConstant.ERR_PLAYER_NAME_NULL;
		}
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,false);
		if(player==null)
		{
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		}
		PropList propList=player.getBundle();
		Prop[] props=propList.getProps();
		for(int i=0;i<props.length;i++)
		{
			if(props[i]==null)
				continue;
			JSONObject jo=propToJSONObject(props[i]);
			if(jo!=null)
				jsonArray.put(jo);
		}
		return GMConstant.ERR_SUCCESS;
	}

	/**
	 * 组装一个物品基本信息到一个json对象
	 * @param prop
	 * @return
	 */
	private JSONObject propToJSONObject(Prop prop)
	{
		if(prop==null)
			return null;
		try
		{
			int count=1;
			if(prop instanceof NormalProp)
				count=((NormalProp)prop).getCount();
			JSONObject jo=new JSONObject();
			// 物品sid
			jo.put(GMConstant.SID,prop.getSid());
			// 物品名称
			jo.put(GMConstant.NAME,prop.getName());
			// 物品数量
			jo.put(GMConstant.COUNT,count);
			return jo;
		}
		catch(JSONException je)
		{
			return null;
		}
	}
}
