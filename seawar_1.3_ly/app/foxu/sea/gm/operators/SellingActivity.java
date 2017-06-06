package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.sea.PublicConst;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.award.Award;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/***
 * 热销大礼包
 * @author lhj
 *
 */
public class SellingActivity  extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String sidStr=params.get("sid");
		String stime=params.get("stime");
		String etime=params.get("etime");
		String name=params.get("name");
		String describe=params.get("describe");
		String price=params.get("price");
		String buyTimes=params.get("buyTimes");
		String initData=params.get("initData");
		String idStr=params.get("id");
		String op_typeStr=params.get("op_type");
		String prompt=params.get("prompt");
		
		if(sidStr==null||sidStr.length()==0||TextKit.valid(sidStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(idStr==null||idStr.length()==0||TextKit.valid(idStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(SeaBackKit.checkBlank(initData))return GMConstant.ERR_PRO_IS_SPACE;
		int sid=Integer.parseInt(sidStr);
		int id=Integer.parseInt(idStr);
		int type=Integer.parseInt(op_typeStr);
		if(prompt==null || prompt.length()==0) prompt="0";
		String ret=null;
		String[] state=null;
		try
		{
			if(sid==ActivityContainer.SELLING1 || sid==ActivityContainer.SELLING2
							|| sid==ActivityContainer.SELLING3)
			{
				if(type==1||type==2)
				{
					if(describe==null) return GMConstant.ERR_PARAMATER_ERROR;
					if(name==null) return GMConstant.ERR_PARAMATER_ERROR;
					if(price==null) return GMConstant.ERR_PARAMATER_ERROR;
					if(buyTimes==null) return GMConstant.ERR_PARAMATER_ERROR;
					int returnData=validateAwardInfo(initData,sid,0);
					if(returnData!=GMConstant.ERR_SUCCESS)
						return returnData;
				}
				initData=name+";"+describe+";"+buyTimes+";"+price+";"+prompt+";"+initData;
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
		String[] props=initData.split(";");
		String[] initStr=props[0].split(",");
		if(initStr.length%PublicConst.AWARD_LENGTH!=0)
			return GMConstant.ERR_ACTIVITY_AWARD_LENGTH_ERRO;
		if(props.length==2)
		{
			/** 随机奖励的配置 **/
			String[] initStr1=props[1].split(",");
			if(initStr1.length%(PublicConst.AWARD_LENGTH)!=0)
				return GMConstant.ERR_ACTIVITY_AWARD_LENGTH_ERRO;
			if(!validateAwardPro(initStr,3,2,false))
				return GMConstant.ERR_ACTIVITY_AWARD_PROBABILITY_ERRO;
			int result=checkAward(props[0],3);
			if(result!=GMConstant.ERR_SUCCESS) 
				return result;
		}
		if(!validateAwardPro(initStr,3,2,true))
			return GMConstant.ERR_ACTIVITY_AWARD_PROBABILITY_ERRO;
		int result=checkAward(props[0],3);
		if(result!=GMConstant.ERR_SUCCESS) 
			return result;
		return GMConstant.ERR_SUCCESS;
	}

	/** 验证奖励包的概率是否正确 **/
	//addLength 每次增加的 当前概率放的位置
	public boolean validateAwardPro(String[] initData,int addLength,int index,boolean flag)
	{
		int count=0;
		for(int i=0;i<initData.length;i+=addLength)
		{
			if(i==0)
				count+=TextKit.parseInt(initData[i+index]);
			else
			{
				if(TextKit.parseInt(initData[i+index])
					-TextKit.parseInt(initData[i-1])<0) return false;
				count+=TextKit.parseInt(initData[i+index])
					-TextKit.parseInt(initData[i-1]);
			}
		}
		if(flag&& count!=PublicConst.AWARD_TOTAL_LENGTH) return false;
		return true;
	}

	/**根据类型检测**/
	public int checkAward(String initData,int addLength)
	{
		String [] str=initData.split(",");
		for(int i=0;i<str.length;i+=addLength)
		{
			int type=SeaBackKit.getSidType(TextKit.parseInt(str[i]));
			if(type==Prop.VALID) return GMConstant.ERR_ACTIVITY_AWARD_LENGTH_ERRO;
		}
		return GMConstant.ERR_SUCCESS;
	}
	
	/**随机物品的概率检测**/
	public boolean checkRandomProps(String[] initData,int addLength,int index)
	{
		if(initData==null||initData.length==0) return true;
		for(int i=0;i<initData.length;i+=addLength)
		{
			if(TextKit.parseInt(initData[i+index])<0
				||TextKit.parseInt(initData[i+index])>Award.PROB_ABILITY)
				return false;
		}
		return true;
	}
}
