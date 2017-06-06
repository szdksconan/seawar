package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.dcaccess.mem.PlayerMemCache;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 模糊查询玩家名
 * @author alan
 *
 */
public class IndistinctName extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String name=params.get("name");
		if(name==null||name.equals(""))
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		PlayerMemCache playerMem=objectFactory.getPlayerCache();
		Object[] players=playerMem.getCacheMap().valueArray();
		int playerNum=players.length;
		for(int i=0;i<playerNum;i++)
		{
			if(players[i]!=null)
			{
				String playName=((PlayerSave)players[i]).getData().getName();
				if(playName!=null&&playName.contains(name))
				{
					try
					{
						JSONObject json=new JSONObject();
						json.put("player_name",playName);
						jsonArray.put(json);
					}
					catch(JSONException e)
					{
						e.printStackTrace();
						return GMConstant.ERR_UNKNOWN;
					}
				}
			}
		}
		return GMConstant.ERR_SUCCESS;
	}

}
