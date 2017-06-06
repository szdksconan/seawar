package foxu.sea.gm.operators;

import java.io.IOException;
import java.util.Map;

import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.sea.gm.GMOperator;


public abstract class ToCenterOperator extends GMOperator
{

	public String sendHttpDataToCenter(int type,Map<String,String> params)
	{
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		// …Ë÷√port
		params.put("port","3");
		params.put("table_type",String.valueOf(type));
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
				+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",params,null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		if(re!=null)
			return re.getContent();
		return null;
	}
	
}
