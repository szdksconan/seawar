package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.OfficerManager;
import foxu.sea.proplist.Prop;


public class LoginRewardActivity extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String sidStr=params.get("sid");
		String op_typeStr=params.get("op_type");
		String stime=params.get("stime");
		String etime=params.get("etime");
		String initData=params.get("initData");
		String idStr=params.get("id");
		//领奖时间 小时
		int rewardTime=Integer.parseInt(params.get("reward_time"));

		if(sidStr==null||sidStr.length()==0
			||TextKit.valid(sidStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(idStr==null||idStr.length()==0
			||TextKit.valid(idStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(op_typeStr==null||op_typeStr.length()==0
			||TextKit.valid(op_typeStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(SeaBackKit.checkBlank(initData))
			return GMConstant.ERR_PRO_IS_SPACE;
		if(rewardTime<0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(!checkInit(initData))
			return GMConstant.ERR_PARAMATER_ERROR;
			
		int sid=Integer.parseInt(sidStr);
		int id=Integer.parseInt(idStr);
		int type=Integer.parseInt(op_typeStr);
		int endTime=SeaBackKit.parseFormatTime(etime)+rewardTime;
		etime=SeaBackKit.formatDataTime(endTime);
		String ret=null;
		String[] state=null;
		initData+="|"+rewardTime;
		try
		{
//			if(sid==ActivityContainer.EXP_ID)
//			{
//				initData=params.get("percent")+","+initData;
//			}
			if(type==1)
			{
				ret=ActivityContainer.getInstance().startActivity(sid,stime,
					etime,initData);
			}
			else if(type==2)
			{
				ret=ActivityContainer.getInstance().resetActivity(sid,stime,
					etime,initData,id);
			}
			else
			{
				state=ActivityContainer.getInstance().getActivityState(sid);
			}
			if(ret==null&&state==null) return GMConstant.ERR_UNKNOWN;
			if(ret!=null)
			{
				JSONObject jo=new JSONObject(ret);
				jsonArray.put(jo);
			}
			else
			{
				for(int i=0;i<state.length;i++)
				{
					JSONObject jo=new JSONObject(state[i]);
					jsonArray.put(jo);
				}
			}
			return GMConstant.ERR_SUCCESS;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return GMConstant.ERR_UNKNOWN;
	}
	
	private boolean checkInit(String initData)
	{
		String[] strArr = initData.split(";");
		for(int i=0;i<strArr.length;i++)
		{
			String[] subStrArr = strArr[i].split(",");
			//第一位为宝石
			for(int j=1;j<subStrArr.length;j+=2)
			{
				int sid = Integer.parseInt(subStrArr[j]);
				int check = SeaBackKit.getSidType(sid);
				if(check == Prop.VALID && !isOfficer(sid))
					return false;
			}
		}
		return true;
	}
	
	private boolean isOfficer(int sid)
	{
		Object obj=OfficerManager.factory.getSample(sid);
		if(obj!=null) return true;
		return false;
	}

}
