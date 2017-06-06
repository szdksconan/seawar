package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.sea.PublicConst;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/**
 * 限时活动
 * 
 * @author comeback
 *
 */
public class TimeActivity extends GMOperator
{
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String sidStr=params.get("sid");
		String op_typeStr=params.get("op_type");
		String stime=params.get("stime");
		String etime=params.get("etime");
		String initData=params.get("initData");
		String idStr=params.get("id");
		if(sidStr==null||sidStr.length()==0||TextKit.valid(sidStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(idStr==null||idStr.length()==0||TextKit.valid(idStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(op_typeStr==null||op_typeStr.length()==0||TextKit.valid(op_typeStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(SeaBackKit.checkBlank(initData))
			return GMConstant.ERR_PRO_IS_SPACE;
		int sid=Integer.parseInt(sidStr);
		int id=Integer.parseInt(idStr);
		int type=Integer.parseInt(op_typeStr);
		String ret=null;
		String[] state=null;
		try
		{
			if(sid==ActivityContainer.DISCOUNT_ID)
			{
				initData=params.get("percent")+","+initData;
			}
			else if(sid==ActivityContainer.JIGSAW_ID)
			{
				initData=params.get("percent")+"-"+initData;
			}
			if(type==1)
			{
				if(sid==ActivityContainer.LIMIT_ID)
				{
					if(initData==null ||initData.length()==0)
						return GMConstant.ERR_PRO_IS_NULL;
					String flag=isableopenactivity(initData);
					if(flag!=null && flag.length()!=0)
					return Integer.parseInt(flag);
				}
				ret= ActivityContainer.getInstance().startActivity(sid,stime,etime,initData);
			}
			else if(type==2)
			{
				if(sid==ActivityContainer.LIMIT_ID)
				{
				if(initData==null ||initData.length()==0)
					return GMConstant.ERR_PRO_IS_NULL;
				String flag=isableopenactivity(initData);
				if(flag!=null && flag.length()!=0)
					return Integer.parseInt(flag);
				}
				ret= ActivityContainer.getInstance().resetActivity(sid,stime,etime,initData,id);
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

	public String isableopenactivity(String initData)
	{
		boolean flag=false;
		String [] str=initData.split(",");
		for(int i=0;i<str.length;i++)
		{
			if(str[i].split("-").length>1 && TextKit.valid(str[i].split("-")[1],TextKit.NUMBER)==0)
			{
				if(TextKit.valid(str[i].split("-")[0],TextKit.NUMBER)==0)
				{
					if(Prop.factory.getSample(Integer.parseInt(str[i].split("-")[0]))==null)
						return String.valueOf(GMConstant.ERR_PRO_IS_ERRO);
					else
					{
						for(int j=0;j<PublicConst.LIMIT_SHOP_SIDS.length;j++)
						{
								if(PublicConst.LIMIT_SHOP_SIDS[j]==Integer.parseInt(str[i].split("-")[0]))
								{
									flag=true;
									break;
								}
						}
						if(!flag) return String.valueOf(GMConstant.ERR_PRO_IS_ERRO);
						flag=false;
					}
				}
				else return String.valueOf(GMConstant.ERR_PRO_IS_ERRO);
			}
			else  return String.valueOf(GMConstant.ERR_PRO_NUM_IS_ERRO);		
		}		
		return"";
	}
}
