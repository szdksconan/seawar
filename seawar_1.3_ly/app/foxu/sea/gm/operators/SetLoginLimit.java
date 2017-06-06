package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * ÉèÖÃÏÞÖÆµÇÂ¼×´Ì¬
 * 
 * @author yw
 */
public class SetLoginLimit extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String action=params.get("action");
		if("true".equals(action))
		{
			PublicConst.READY=true;
		}
		else
		{
			PublicConst.READY=false;
		}
		return GMConstant.ERR_SUCCESS;
	}

}
