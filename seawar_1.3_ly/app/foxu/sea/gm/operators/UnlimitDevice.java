package foxu.sea.gm.operators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.io.ByteBuffer;
import shelby.httpclient.HttpRequester;
import foxu.cc.GameDBCCAccess;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;


public class UnlimitDevice extends ToCenterOperator
{

	public static int SET_DEVICE_UNLIMIT=9;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeByte(SET_DEVICE_UNLIMIT);
		data.writeUTF(params.get("udid"));
		String content=sendHttpData(data);
		if(content==null)
			return GMConstant.ERR_SUCCESS;
		return GMConstant.ERR_GAME_CENTER_COMUNICATION_ERROR;
	}
	
	public static int getSET_DEVICE_UNLIMIT()
	{
		return SET_DEVICE_UNLIMIT;
	}
	
	public static void setSET_DEVICE_UNLIMIT(int sET_DEVICE_UNLIMIT)
	{
		SET_DEVICE_UNLIMIT=sET_DEVICE_UNLIMIT;
	}
	
	public String sendHttpData(ByteBuffer data)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// …Ë÷√port
		map.put("port","1");
//		HttpRespons re=null;
		try
		{
			request.send(
				"http://"+GameDBCCAccess.GAME_CENTER_IP+":"+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,
				null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return "Err";
		}
		return null;
	}

}
