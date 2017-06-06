package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.sea.PublicConst;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.LuckyExploredActivity;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

public class TimeAwardActivity extends GMOperator
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
		int sid=Integer.parseInt(sidStr);
		int id=Integer.parseInt(idStr);
		int type=Integer.parseInt(op_typeStr);
		// if(!ActivityContainer.getInstance().checkId(id))
		// {
		// return GMConstant.ERR_ACTIVITY_NOT_EXISTS;
		// }
		String ret=null;
		String[] state=null;
		try
		{
			if(sid==ActivityContainer.AWARD_ID
				||sid==ActivityContainer.AWARD_CLASSIC_ID
				||sid==ActivityContainer.AWARD_SHIPPING_ID
				||sid==ActivityContainer.AWARD_ROB_ID)
			{
				if(type==1||type==2)
				{
					int returnData=validateAwardInfo(initData,sid,0);
					if(returnData!=GMConstant.ERR_SUCCESS)
						return returnData;
				}
				initData=params.get("gems")+","+params.get("times")+","
					+initData;
			}
			/**验证 **/
			else if(sid==ActivityContainer.LUCKY_EXPLORED_ID)
			{
				String initData1=params.get("data");
				String initData2=params.get("data2");
				String initData3=params.get("data3");
				if(type==1||type==2)
				{
					int returnData=validateAwardInfo(initData,sid,LuckyExploredActivity.SHIP);
					if(returnData!=GMConstant.ERR_SUCCESS)
						return returnData;
					 returnData=validateAwardInfo(initData1,sid,LuckyExploredActivity.START);
					if(returnData!=GMConstant.ERR_SUCCESS)
						return returnData;
					 returnData=validateAwardInfo(initData2,sid,LuckyExploredActivity.PRO);
					if(returnData!=GMConstant.ERR_SUCCESS)
						return returnData;
					 returnData=validateAwardInfo(initData3,sid,LuckyExploredActivity.EQUIP);
					if(returnData!=GMConstant.ERR_SUCCESS)
						return returnData;
				}
				initData=initData+";"+initData1+"|"+initData2+"|"+initData3+"|"+params.get("gems")+"|"+params.get("tenGems")+"|"+params.get("times");
			}
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
		}
		return GMConstant.ERR_UNKNOWN;
	}

	/*** 验证奖励包的信息是否正确 */
	public int validateAwardInfo(String initData,int sid,int type)
	{
		if(initData==null||initData.length()==0)
			return GMConstant.ERR_ACTIVITY_AWARD_IS_NULL;
		String[] initStr=initData.split(",");
		if(initStr.length%PublicConst.AWARD_LENGTH!=0)
			return GMConstant.ERR_ACTIVITY_AWARD_LENGTH_ERRO;
		if(sid==ActivityContainer.AWARD_ID)
		{
			if(initStr.length/PublicConst.AWARD_LENGTH!=PublicConst.AWARD_TOTAL_ALLLENGTH)
				return GMConstant.ERR_ACTIVITY_AWARD_LENGTH_ERRO;
		}
		if(sid==ActivityContainer.LUCKY_EXPLORED_ID)
		{
			int result=checkAward(type,initData);
			if(result!=GMConstant.ERR_SUCCESS) return result;
		}
		if(!validateAwardPro(initStr))
			return GMConstant.ERR_ACTIVITY_AWARD_PROBABILITY_ERRO;
		return GMConstant.ERR_SUCCESS;
	}

	/** 验证奖励包的概率是否正确 **/
	public boolean validateAwardPro(String[] initData)
	{
		int count=0;
		for(int i=0;i<initData.length;i+=3)
		{
			if(i==0)
				count+=TextKit.parseInt(initData[i+2]);
			else
			{
				if(TextKit.parseInt(initData[i+2])
					-TextKit.parseInt(initData[i-1])<0) return false;
				count+=TextKit.parseInt(initData[i+2])
					-TextKit.parseInt(initData[i-1]);
			}
		}
		if(count!=PublicConst.AWARD_TOTAL_LENGTH) return false;
		return true;
	}

	/**根据类型检测**/
	public int checkAward(int type,String initData)
	{
		String [] str=initData.split(",");
		for(int i=0;i<str.length;i+=3)
		{
			if(type==LuckyExploredActivity.START
				&&TextKit.parseInt(str[i])!=2009)
				return GMConstant.ERR_AWARD_SHIPS_START;
			else if(type==LuckyExploredActivity.SHIP)
			{
				int result=SeaBackKit.getSidType(TextKit.parseInt(str[i]));
				if(result!=Prop.SHIP) return GMConstant.ERR_AWARD_SHIPS_ERRO;
			}
			else if(type==LuckyExploredActivity.PRO)
			{
				int result=SeaBackKit.getSidType(TextKit.parseInt(str[i]));
				if(result!=Prop.PROP) return GMConstant.ERR_AWARD_SHIPS_PRO;
			}
			else if(type==LuckyExploredActivity.EQUIP)
			{
				int result=SeaBackKit.getSidType(TextKit.parseInt(str[i]));
				if(result!=Prop.EQUIP && result!=Prop.OFFICER) return GMConstant.ERR_AWARD_SHIPS_EQU;
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
	
//	public  void checkAwardShip(int sid)
//	{
//		for(int i=0;i<PublicConst.SHIP_FOR_SID.length;i++)
//		{
//			if()
//		}
//		
//	}
	
	
//	public int validateLuckyExplored(String initData)
//	{
//		if(initData==null||initData.length()==0)
//			return GMConstant.ERR_ACTIVITY_AWARD_IS_NULL;
//		String[] initStr=initData.split(";");
//		for(int i=0;i<initStr.length;i++)
//		{
//			String[] awardStr=initStr[i].split(",");
//			if(awardStr.length%PublicConst.AWARD_LENGTH!=0)
//				return GMConstant.ERR_ACTIVITY_AWARD_LENGTH_ERRO;
//			int type=Integer.parseInt(awardStr[0]);
//			int start=Integer.parseInt(awardStr[1]);
//			int num=Integer.parseInt(awardStr[2]);
//			if(type!=LuckyExploredActivity.SHIP&&type!=LuckyExploredActivity.EQUIP&&type!=LuckyExploredActivity.OFFICER
//							&&type!=LuckyExploredActivity.OTHER)
//				return GMConstant.ERR_PARAMATER_ERROR;
//			if(start>=LuckyExploredActivity.GRID_SIZE || (start+num)>LuckyExploredActivity.GRID_SIZE)
//				return GMConstant.ERR_PARAMATER_ERROR;
//		}
//		return GMConstant.ERR_SUCCESS;
//	}
}
