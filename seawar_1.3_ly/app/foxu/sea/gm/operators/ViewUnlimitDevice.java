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
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

public class ViewUnlimitDevice extends ToCenterOperator
{

	public static int VIEW_UNLIMIT=11;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeByte(VIEW_UNLIMIT);
		data=sendHttpData(data);
		if(data==null) return GMConstant.ERR_GAME_CENTER_COMUNICATION_ERROR;
		int len=data.readInt();
		try
		{
			JSONObject json=null;
			for(int i=0;i<len;i++)
			{
				json=new JSONObject();
				json.put("name",data.readUTF());
				jsonArray.put(json);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return GMConstant.ERR_SUCCESS;
	}

	public ByteBuffer sendHttpData(ByteBuffer data)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// 设置port
		map.put("port","1");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
				+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,null);
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
