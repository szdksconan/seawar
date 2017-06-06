package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.sea.ContextVarManager;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.officer.OfficerManager;

/****
 * 设置系统碎片随机的限制
 * 
 * @author lhj
 * 
 */
public class SetOffcerShopLimit extends GMOperator
{

	public static int SERCH=2,SET=1;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String offcerScarcity=params.get("offcerScarcity");
		String limitTimes=params.get("limitTimes");
		int scarcity=TextKit.parseInt(offcerScarcity);
		int type=TextKit.parseInt(params.get("type"));
		if(type==SET)
		{
			boolean loop=false;
			int times=0;
			for(int i=0;i<PublicConst.OFFCER_SHOP_SIDS.length;i+=4)
			{
				if(scarcity==PublicConst.OFFCER_SHOP_SIDS[i+1])
				{
					times++;
					loop=true;
				}
			}
			if(!loop) return GMConstant.ERR_OFFCERSCARCITY;
			int limit=TextKit.parseInt(limitTimes);
			if(limit>OfficerManager.SHOP_LENGHT||limit<0 || times<limit)
				return GMConstant.ERR_OFFCER_LIMIT_LENGTH;
			ContextVarManager.getInstance().setVarDest(
				ContextVarManager.SAVE_OFFCER_SHOP_LIMIT,scarcity+","+limit);
		}
		String result=ContextVarManager.getInstance().getVarDest(
			ContextVarManager.SAVE_OFFCER_SHOP_LIMIT);
		JSONObject jo=new JSONObject();
		try
		{
			if(result==null)
			{
				jo.put("offcerScarcity",0);
				jo.put("limitTimes",0);
			}
			else
			{
				String[] str=TextKit.split(result,",");
				jo.put("offcerScarcity",str[0]);
				jo.put("limitTimes",str[1]);
			}
			jsonArray.put(jo);
		}
		catch(Exception e)
		{
		}
		return GMConstant.ERR_SUCCESS;
	}

}
