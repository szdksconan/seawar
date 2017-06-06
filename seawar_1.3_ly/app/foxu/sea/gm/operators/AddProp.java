package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMManager;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;

/**
 * Ìí¼ÓµÀ¾ß
 * @author comeback
 *
 */
public class AddProp extends GMOperator
{
	public static int PROP_LIMIT=30;
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String playerName=params.get("player_name");
			String sidStr=params.get("sid");
			String countStr=params.get("count");
			int sid=Integer.parseInt(sidStr);
			int count=Integer.parseInt(countStr);
			if(!GMManager.IS_TEST&&(count>PROP_LIMIT||count<0))
				return GMConstant.ERR_PRO_NUM_IS_ERRO;
			if(playerName==null||playerName.length()==0)
				return GMConstant.ERR_PLAYER_NAME_NULL;
			CreatObjectFactory objectFactory=info.getObjectFactory();
			Player player=objectFactory.getPlayerByName(playerName,true);
			if(player==null)
				return GMConstant.ERR_PLAYER_NOT_EXISTS;
			Prop prop=(Prop)Prop.factory.newSample(sid);
			if(prop==null) 
				return GMConstant.ERR_PROP_IS_NULL;
			if(prop instanceof NormalProp) 
				((NormalProp)prop).setCount(count);
			if(player.getBundle().incrProp(prop,true))
				return GMConstant.ERR_SUCCESS;
			else
				return GMConstant.ERR_BUNDLE_IS_FULL;
		}
		catch(Exception e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
	}

}
