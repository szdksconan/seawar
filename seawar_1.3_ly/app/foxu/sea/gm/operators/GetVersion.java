package foxu.sea.gm.operators;

import java.text.DecimalFormat;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.field.Fields;
import mustang.field.LongField;
import mustang.field.StringField;
import foxu.dcaccess.UserGameDBAccess;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


public class GetVersion extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String sql="select count(*) from users";
		UserGameDBAccess access=info.getObjectFactory().getUserDBAccess();
		Fields fields=access.loadSql(sql);
		long total=((LongField)fields.get("count(*)")).value;
		sql="select version,count(*) from users group by version";
		Fields[] objs=access.loadsSql(sql);
		DecimalFormat decimalFormat=new DecimalFormat(".000");
		for(int i=0;i<objs.length;i++)
		{
			String version=((StringField)objs[i].get("version")).value;
			long amount=((LongField)objs[i].get("count(*)")).value;
			float percent=amount/(float)total*100;
			JSONObject jobj=new JSONObject();
			try
			{
				jobj.put("version",version);
				jobj.put("amount",amount);
				jobj.put("percent",decimalFormat.format(percent));
				jsonArray.put(jobj);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
			
		}
		
		
		return GMConstant.ERR_SUCCESS;
	}

}
