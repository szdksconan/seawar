package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 天数活动
 * @author comeback
 *
 */
public class DaysActivity extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int sid=0,type=0;
		String sidStr=params.get("sid");
		String days=params.get("days");
		String hours=params.get("hours");
		String percent=params.get("percent");
		String op_typeStr=params.get("op_type");
		if(sidStr!=null) sid=Integer.parseInt(sidStr);
		if(op_typeStr!=null) type=Integer.parseInt(op_typeStr);
		String ret=null;
		if(type==1)
			ret=ActivityContainer.getInstance().startActivity(sid,days,
				hours,percent);
		else
		{
			//ret=ActivityContainer.getInstance().getActivityState(sid); //这个类目前没用上
		}
		
		if(ret!=null)
		{
			
			try
			{
				JSONObject jo;
				jo=new JSONObject(ret);
				jsonArray.put(jo);
				return GMConstant.ERR_SUCCESS;
			}
			catch(JSONException e)
			{
			}
		}
		return GMConstant.ERR_UNKNOWN;
	}

}
