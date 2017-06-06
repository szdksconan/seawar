package foxu.sea.gm.operators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.io.ByteBuffer;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/***
 * 
 * 设置中心系统公告的开启和关闭状态
 * @author lhj
 *
 */
public class SetCenterAnnState extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String plat=params.get("ann_status");
			ByteBuffer data=sendHttpData(plat);
			int state=data.readByte();
			JSONObject jo=new JSONObject();
			jo.put("state",state);
			jsonArray.put(jo);
		}
		catch(Exception e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
		
		return GMConstant.ERR_SUCCESS;
	}

	public ByteBuffer sendHttpData(String state)
	{
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		// 设置port
		map.put("port","3");
		map.put("table_type",11+"");
		map.put("ann_status",state);
		HttpRespons re=null;
		try
		{
			re=request.send(
				"http://"+GameDBCCAccess.GAME_CENTER_IP+":"+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,
				null);
		}
		catch(IOException e)
		{
			// TODO 自动生成 catch 块
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
}
