package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.officer.OfficerManager;


/**
 * 控制玩家军官总功勋
 * @author Alan
 */
public class ControlOfficerFeats extends GMOperator
{

	/** VIEW=1 查询,INCR=2 增加,DECR=3 减少 */
	public final int VIEW=1,INCR=2,DECR=3;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playername=params.get("pname");
		String types=params.get("typefun");
		if(playername==null||playername.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playername,true);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		int type=TextKit.parseInt(types);
		int offset=0;
		if(type!=VIEW)
		{
			offset=TextKit.parseInt(params.get("feats"));
		}
		if(type==INCR)
		{
			player.getOfficers().incrFeats(offset);
		}
		else if(type==DECR)
		{
			player.getOfficers().decrFeats(offset);
		}
		long feats=OfficerManager.getInstance().getTotalFeats(player);
		try
		{
			JSONObject json=new JSONObject();
			json.put("pname",playername);
			json.put("feats",feats);
			jsonArray.put(json);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}

}
