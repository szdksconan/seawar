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
import foxu.sea.kit.SeaBackKit;

/**
 * 手动设置奖品提示
 * @author Alan
 *
 */
public class ManualLuckySid extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int type=TextKit.parseInt(params.get("type"));
		if(type==1)
		{
			getCurrentSids(jsonArray);
		}
		else if(type==2)
		{
			String initData=params.get("initData");
			String[] sids=TextKit.split(initData,",");
			if(sids==null||sids.length<=0)
				return GMConstant.ERR_PARAMATER_ERROR;
			for(int i=0;i<sids.length;i++)
			{
				if(!sids[i].matches("\\d+"))
					return GMConstant.ERR_PARAMATER_ERROR;
			}
			ContextVarManager.getInstance().putVar(
				ContextVarManager.AWARD_LUCKY_SIDS,1,initData);
			PublicConst.manualLuckySids=TextKit.parseIntArray(sids);
			getCurrentSids(jsonArray);
		}
		return GMConstant.ERR_SUCCESS;
	}

	public int getCurrentSids(JSONArray jsonArray)
	{
		try
		{
			int[] sids=SeaBackKit.getLuckySids();
			for(int i=0;i<sids.length;i++)
			{
				JSONObject json=new JSONObject();
				json.put("sid",sids[i]);
				jsonArray.put(json);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}
}
