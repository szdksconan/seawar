package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;


/**
 * 排行榜奖励活动
 * @author Alan
 *
 */
public class RankActivity extends GMOperator
{
	public static final int NEW_ONE=1,RESET=2,VIEW=3;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int type=TextKit.parseInt(params.get("type"));
		int sid=TextKit.parseInt(params.get("sid"));
		int id=TextKit.parseInt(params.get("id"));
		String atime=params.get("awardTime");
		String stime=params.get("stime");
		String initData=params.get("initData");
		int awardTime=SeaBackKit.parseFormatTime(atime);
		int eTime=awardTime+2*(int)(SeaBackKit.DAY_MILL_TIMES/1000);
		String etime=SeaBackKit.formatDataTime(eTime);
//		int eTime=SeaBackKit.parseFormatTime(etime);
		int result=validAward(initData);
		if(result!=GMConstant.ERR_SUCCESS)
			return result;
		String state[]=null;
		initData=atime+";"+initData;
		if(type==NEW_ONE)
		{
			state=new String[]{ActivityContainer.getInstance().startActivity(sid,stime,etime,initData)};
		}
		else if(type==RESET)
		{
			state=new String[]{ActivityContainer.getInstance().resetActivity(sid,stime,etime,initData,id)};
		}
		else
		{
			state=ActivityContainer.getInstance().getActivityState(sid);
		}
		if(state==null)
			return GMConstant.ERR_UNKNOWN;
		for(int i=0;i<state.length;i++)
		{
			JSONObject jo;
			try
			{
				jo=new JSONObject(state[i]);
				jsonArray.put(jo);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		return GMConstant.ERR_SUCCESS;
	}

	public int validAward(String initData)
	{
		String[] awardSids=TextKit.split(initData,"|");
		for(int i=0;i<awardSids.length;i++)
		{
			String[] awards=TextKit.split(awardSids[i],":");
			// 名次范围
			int[] range=TextKit.parseIntArray(TextKit.split(awards[0],"-"));
			// 奖励品信息
			int[] sids=TextKit.parseIntArray(TextKit.split(awards[1],","));
			// 取上限作为发奖判定依据
			if(range[0]>range[1])
				return GMConstant.ERR_PARAMATER_ERROR;
			//第一位为宝石
			if((sids.length-1)%2!=0||sids.length>9)
				return GMConstant.ERR_ACTIVITY_AWARD_LENGTH_ERRO;
		}
		return GMConstant.ERR_SUCCESS;
	}
	
}
