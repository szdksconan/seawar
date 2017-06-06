package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import mustang.io.ByteBuffer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


/**
 * 查询激活码
 * @author lhj
 *
 */
public class SerchCode extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String platform=params.get("platform_code");//平台
		String codetimesid=params.get("code_timesid");//批次
		String username=params.get("code_createname");//兑换码生成人
		String codename=params.get("code_name");//兑换码
		String playername=params.get("player_name");//兑换人
		String  pagenew=params.get("pageNow");//当前页数
		int timesid=0;
		if(codetimesid!=null && codetimesid!="") timesid=Integer.parseInt(codetimesid) ;
		int pageNow=0;
		if(pagenew!=null && pagenew!="") pageNow=Integer.parseInt(pagenew);
		ByteBuffer data=codeSendcenter(pageNow,platform,timesid,username,codename,playername);
		String array=data.readUTF();
		try
		{
				JSONArray jsonarr=new JSONArray(array);
				for(int i=0;i<jsonarr.length();i++)
				{
					jsonArray.put(jsonarr.get(i));
				}
		}
		catch(JSONException e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}
	/**
	 * 装入数据  
	 * @param pageNow  当前页
	 * @param platform 平台
	 * @param timesid 批次
	 * @param username 用户名
	 * @param codename 兑换码
	 * @param playername 玩家名称
	 * @return
	 */
	private static  ByteBuffer codeSendcenter(int pageNow,String platform,int timesid,String username,String codename,String playername)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeByte(8);
		data.writeInt(pageNow);//当前页
		data.writeInt(timesid);//批次
		data.writeUTF(username);//兑换码生成人
		data.writeUTF(codename);//兑换码
		data.writeUTF(playername);//兑换人
		//访问gamecenter
		data=CodeInfomation.sendHttpData(data,platform);
		return data;
	}


}

