package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


/**
 * 查看虚空实验室 舰船强化等级
 * @author alan
 *
 */
public class ShipLevelViewer extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String name=params.get("name");
		if(name==null||name.length()==0)
		{
			return GMConstant.ERR_PLAYER_NAME_NULL;
		}
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(name,false);
		if(player==null)
		{
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		}
		ByteBuffer data=new ByteBuffer();
		data.clear();
		player.showBytesWriteShipLevel(data);
		int length=data.readByte();
		for(int i=0;i<length;i++){
			try
			{
				JSONObject json=new JSONObject();
				json.put("ship_level_id",data.readShort());
				json.put("ship_level",data.readByte());
				jsonArray.put(json);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				return GMConstant.ERR_UNKNOWN;
			}
			
		}
		return GMConstant.ERR_SUCCESS;
	}

}
