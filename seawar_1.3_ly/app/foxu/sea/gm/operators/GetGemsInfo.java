package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.orm.ConnectionManager;
import mustang.orm.SqlKit;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 查看各类宝石数量
 * 
 * @author yw
 */
public class GetGemsInfo extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String start=params.get("start");
		String end=params.get("end");
		String free="select SUM(gems) from gem_tracks where (type=8 or type=9 or type=11 or type=12 or type=20 or type=25) and createAt>=UNIX_TIMESTAMP('"
			+start+"') and createAt<=UNIX_TIMESTAMP('"+end+"');";
		String charge="select SUM(gems) from orders where create_at>=UNIX_TIMESTAMP('"
			+start+"') and create_at<=UNIX_TIMESTAMP('"+end+"');";
		String pay="select SUM(gems) from gem_tracks where type!=7 and type!=8 and type!=9 and type!=11 and type!=12 and type!=20 and type!=23 and type!=24 and type!=25 and type!=18 and type!=19 and createAt>=UNIX_TIMESTAMP('"
			+start+"') and createAt<=UNIX_TIMESTAMP('"+end+"');";
		ConnectionManager c=info.getConnectionManager();
		JSONObject json=new JSONObject();
		try
		{
			Fields fields=SqlKit.query(c,free);
			json.put("free",((IntField)fields.toArray()[0]).value);
			fields=SqlKit.query(c,charge);
			json.put("charge",((IntField)fields.toArray()[0]).value);
			fields=SqlKit.query(c,pay);
			json.put("pay",((IntField)fields.toArray()[0]).value);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		jsonArray.put(json);
		return GMConstant.ERR_SUCCESS;
	}

}
