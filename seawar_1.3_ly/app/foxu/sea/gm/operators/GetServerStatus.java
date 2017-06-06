package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 
 * ²éÑ¯·þÎñÆ÷×´Ì¬
 * @author lhj
 *
 */
public class GetServerStatus  extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			JSONObject jo=new JSONObject();
			if(PublicConst.READY)
				jo.put("status","1");
			else
				jo.put("status","2");
			
			jsonArray.put(jo);
		}
		catch(Exception e)
		{
		}
		return GMConstant.ERR_SUCCESS;
	}

}
