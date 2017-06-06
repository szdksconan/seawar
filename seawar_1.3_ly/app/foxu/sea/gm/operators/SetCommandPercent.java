package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * …Ë÷√–“‘À÷µ100%
 * @author lanlin
 *
 */
public class SetCommandPercent extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int[] ids=info.getObjectFactory().getPlayerCache().getCacheMap()
			.keyArray();
		Player player;
		for(int i=0;i<ids.length;i++)
		{
			player=info.getObjectFactory().getPlayerCache().load(ids[i]+"");
			int value=999999;
			player.setAttribute(PublicConst.UPCOMMANDER_FAILURE,value+"");
			player.setAttribute(PublicConst.COMMAND_UP_LUCKY,999999+"");
		}
		return GMConstant.ERR_SUCCESS;
	}
}
