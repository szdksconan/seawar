package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.VaribleAwardActivity;
import foxu.sea.equipment.Equipment;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

public class VaribleAward extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String sidStr=params.get("sid");
		String op_typeStr=params.get("op_type");
		String award_type=params.get("award_type");
		String award_id=params.get("award_id");
		String stime=params.get("stime");
		String etime=params.get("etime");
		String initData=params.get("initData");

		if(sidStr==null||sidStr.length()==0
			||TextKit.valid(sidStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(op_typeStr==null||op_typeStr.length()==0
			||TextKit.valid(op_typeStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(SeaBackKit.checkBlank(initData))
			return GMConstant.ERR_PRO_IS_SPACE;
		int sid=Integer.parseInt(sidStr);
		int type=Integer.parseInt(op_typeStr);
		String ret=null;
		try
		{
			if(type==1)
			{
				if(award_type==null||award_type.length()==0
					||TextKit.valid(award_type,TextKit.NUMBER)!=0)
					return GMConstant.ERR_PARAMATER_ERROR;
				int vpro=validatPro(initData);
				if(vpro!=0)
					return vpro;
				initData=award_type+":"+initData;
				String level=params.get("level");
				String limitTime=params.get("limitTime");
				initData+=";"+level+";"+limitTime;
				ret=ActivityContainer.getInstance().startActivity(sid,stime,
					etime,initData);
			}
			else if(type==2)
			{
				if(award_id==null||award_id.length()==0
					||TextKit.valid(award_id,TextKit.NUMBER)!=0)
					return GMConstant.ERR_PARAMATER_ERROR;
				int vpro=validatPro(initData);
				if(vpro!=0)
					return vpro;
				initData=award_id+":"+initData;
				String level=params.get("level");
				String limitTime=params.get("limitTime");
				initData+=";"+level+";"+limitTime;
				ret=ActivityContainer.getInstance().resetActivity(sid,stime,
					etime,initData,0);
			}
			else
			{
				((VaribleAwardActivity)ActivityContainer.getInstance()
					.getActivity(sid,0)).getActivityInfo(jsonArray);
			}
			if(ret!=null)
			{
				JSONObject jo=new JSONObject();
				jo.put("others",ret);
				jsonArray.put(jo);
			}
			return GMConstant.ERR_SUCCESS;
		}
		catch(Exception e)
		{
		}
		return GMConstant.ERR_UNKNOWN;
	}
	/**验证天降好礼物品配置的长度和物品**/
	  public int validatPro(String initData)
	{
		if(initData==null||initData.length()==0)
			return GMConstant.ERR_PROINFO_IS_NULL;
		String[] proList=initData.split(",");
		if(proList.length%2!=0) return GMConstant.ERR_PRO_ERRO_LENGTH;
		for(int i=0;i<proList.length;i+=2)
		{
			if(SeaBackKit.getSidType(TextKit.parseInt(proList[i]))!=Prop.VALID) continue;
			return GMConstant.ERR_PRO_IS_NOT_PRO;
		}
		return GMConstant.ERR_SUCCESS;
	}

	// 验证是否是进阶曲线
	public boolean isQuality_stuffs(int sid)
	{
		for(int i=0;i<Equipment.QUALITY_STUFFS.length;i+=3)
		{
			if(Equipment.QUALITY_STUFFS[i]==sid) return true;
		}
		return false;
	}
	  
}
