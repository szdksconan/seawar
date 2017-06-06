package foxu.sea.gm.operators;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import mustang.codec.CodecKit;
import mustang.codec.MD5;
import mustang.text.TextKit;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 通过pdid获取充值信息
 * @author Alan
 *
 */
public class ViewOrdersByPdid extends GMOperator
{
	private static final String PDID_QUERY_KEY="nkyntCuBgsNIEGujwH2fHruYUx3YrnHqk0HS1GQbJJqltMlBMggWHymfiurt6kud";
	public static final int MAX_SIZE=20;
	String purchaseUrl="http://127.0.0.1:8880/query";
	MD5 md5=new MD5();

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		Map<String,String> params_temp=new HashMap<String,String>();
		// 设置port
		params_temp.put("action","querybypdid");
		params_temp.put("pdid",params.get("pdid"));
		params_temp.put("start_time",params.get("stime"));
		params_temp.put("end_time",params.get("etime"));
		params_temp.put("offset",((TextKit.parseInt(params.get("page"))-1)*MAX_SIZE)+"");
		params_temp.put("count",MAX_SIZE+"");
		params_temp.put("md5",MD5(PDID_QUERY_KEY+params.get("pdid")));
		HttpRespons re=null;
		String content=null;
		try
		{
			re=request.send(purchaseUrl,"POST",params_temp,null);
			if(re!=null)
			{
				content=re.getContent();
				JSONObject json=new JSONObject(content);
				json.put("page",params.get("page"));
				int max=TextKit.parseInt(json.getString("max"));
				json.put("maxpage",(max/MAX_SIZE+max%MAX_SIZE==0?0:1));
				json.put("stime",params.get("stime"));
				json.put("etime",params.get("etime"));
				json.put("pdid",params.get("pdid"));
				jsonArray.put(json);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	/** 计算字符串的md5，库里面的MD5类的计算方法在处理多字节字符时有bug，所以重新写一个 */
	private String MD5(String str)
	{
		if(str==null) return "";
		try
		{
			byte[] data=str.getBytes("utf-8");
			byte[] digest=md5.encode(data);
			String csign=new String(CodecKit.byteHex(digest));

			return csign;
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return "";
		}
	}

	
	public String getPurchaseUrl()
	{
		return purchaseUrl;
	}

	
	public void setPurchaseUrl(String purchaseUrl)
	{
		this.purchaseUrl=purchaseUrl;
	}

}
