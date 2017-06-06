package foxu.sea.gm.operators;

import java.util.Arrays;
import java.util.Map;

import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.sea.award.ActivityAwardManager;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.GMSetManager;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * »î¶¯ÏÞÊ±½±Àø
 * 
 * @author Alan
 * 
 */
public class ActivityLimitAward extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String type=params.get("type");
		// ÅäÖÃ½±Àø
		if("1".equals(type))
		{
			int stime=SeaBackKit.parseFormatTime(params.get("stime"));
			int etime=SeaBackKit.parseFormatTime(params.get("etime"));
			int[] awardArrs=TextKit.parseIntArray(TextKit.split(
				params.get("props"),","));
			int[] randomArrs=TextKit.parseIntArray(TextKit.split(
				params.get("randoms"),","));
			int[] awardTypes=getAwardTypes(params);
			ActivityAwardManager.getInstance().putAward(awardTypes,stime,
				etime,awardArrs,randomArrs);
		}
		// ÒÆ³ý½±Àø
		else if("2".equals(type))
		{
			int[] awardTypes=getAwardTypes(params);
			ActivityAwardManager.getInstance().removeAward(awardTypes);
		}
		// µ±Ç°ÅäÖÃ
		try
		{
			IntKeyHashMap awardMap=ActivityAwardManager.getInstance()
				.getAwardMap();
			int[] keys=awardMap.keyArray();
			ActivityAwardManager.ActivityAward award;
			JSONObject json;
			for(int i=0;i<keys.length;i++)
			{
				award=(ActivityAwardManager.ActivityAward)awardMap
					.get(keys[i]);
				if(award==null) continue;
				json=new JSONObject();
				json.put("type",keys[i]);
				json.put("props",Arrays.toString(award.getProps()));
				json.put("randoms",Arrays.toString(award.getRandoms()));
				json.put("stime",award.getStime());
				json.put("etime",award.getEtime());
				jsonArray.put(json);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return GMConstant.ERR_SUCCESS;
	}

	public int[] getAwardTypes(Map<String,String> params)
	{
		return TextKit.parseIntArray(GMSetManager.getMultiValues("awardType",params));
	}
}
