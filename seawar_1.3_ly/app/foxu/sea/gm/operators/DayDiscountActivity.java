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
import foxu.sea.proplist.Prop;

/***
 * 每日折扣活动
 * 
 * @author lihongji
 */
public class DayDiscountActivity extends GMOperator
{

	// 商品的长度
	public static final int INITDATELENGTH=5,MAXPRONUM=100;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{

		String sidStr=params.get("sid");
		String op_typeStr=params.get("op_type");
		String stime="2013-10-10 10:0:0";
		String etime="2038-01-19 11:14:07";
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
//		if(SeaBackKit.checkBlank(initData))
//			return GMConstant.ERR_PRO_IS_SPACE;
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
			switch(type)
			{
				case 1:
					int endStr=vlidateInitData(initData);
					if(endStr!=GMConstant.ERR_SUCCESS) return endStr;
					ret=ActivityContainer.getInstance().startActivity(sid,
						stime,etime,initData);
					break;
				case 3:
					state=ActivityContainer.getInstance().getActivityState(
						sid);
					break;
				default:
					switch(type)
					{
						case 2:
							initData="1";
							break;
						case 4:
							endStr=vlidateInitData(initData);
							if(endStr!=GMConstant.ERR_SUCCESS)
								return endStr;
							initData+=",2";
							break;
						case 5:
							initData="3";
							break;
					}
					ret=ActivityContainer.getInstance().resetActivity(sid,
						stime,etime,initData,id);
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
	/** 验证输入的sid,数量,时间格式是否正确 **/
	public int vlidateInitData(String initData)
	{
		if(initData==null||initData.equals(""))
				return GMConstant.ERR_SUCCESS;
		String[] data=initData.split(",");
		if(data.length%INITDATELENGTH!=0)
			return GMConstant.ERR_ACTIVITY_PRO_LENGTH_ERRO;
		for(int i=0;i<data.length;i+=INITDATELENGTH)
		{
			int sid=TextKit.parseInt(data[i]);
			Prop prop=(Prop)Prop.factory.getSample(sid);
			if(prop==null) return GMConstant.ERR_DAY_SID_IS_ERRO;
			int num=TextKit.parseInt(data[i+1]);
			if(num>MAXPRONUM) return GMConstant.ERR_DAY_PRO_ABOUT_LENGTH;
//			int price=TextKit.parseInt(data[i+2]);
			// if(price<0) return GMConstant.ERR_
			int stime=SeaBackKit.parseFormatTime(data[i+3]);
			int etime=SeaBackKit.parseFormatTime(data[i+4]);
			if(stime>=etime) return GMConstant.ERR_TIME_ERRO;
		}
		return GMConstant.ERR_SUCCESS;
	}
}
