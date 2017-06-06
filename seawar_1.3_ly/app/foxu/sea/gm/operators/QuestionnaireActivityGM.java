package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.field.Fields;
import mustang.text.TextKit;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.OfficerManager;
import foxu.sea.proplist.Prop;

/**
 * 问卷调查GM工具
 */
public class QuestionnaireActivityGM extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String sidStr=params.get("sid");
		String op_typeStr=params.get("op_type");
		String stime=params.get("stime");
		String etime=params.get("etime");
		String introduction=params.get("introduction");
		String topic=params.get("topic");
		String levelLow=params.get("levelLow");
		String levelHigh=params.get("levelHigh");
		String idStr=params.get("id");
		String award=params.get("award");
		String sql=params.get("sql");

		if(introduction==null||introduction.length()==0
			||introduction.indexOf("#")<0||topic==null||topic.length()==0
			||levelLow==null||levelLow.length()==0||levelHigh==null
			||levelHigh.length()==0||award==null||award.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(sidStr==null||sidStr.length()==0
			||TextKit.valid(sidStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(idStr==null||idStr.length()==0
			||TextKit.valid(idStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(op_typeStr==null||op_typeStr.length()==0
			||TextKit.valid(op_typeStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(!checkInit(award)) return GMConstant.ERR_PARAMATER_ERROR;
		int sid=Integer.parseInt(sidStr);
		int id=Integer.parseInt(idStr);
		int type=Integer.parseInt(op_typeStr);
		String ret=null;
		String[] state=null;
		// 等级段 左16 下限 右16上限
		int level=(Integer.parseInt(levelLow)<<16)
			+Integer.parseInt(levelHigh);
		String initData=introduction+"|"+topic+"|"+level+"|"+award;
		if(SeaBackKit.checkBlank(initData))
			return GMConstant.ERR_PRO_IS_SPACE;
		String[] strArr=introduction.split("#");
		if(strArr[0]==null||strArr[0].equals("")||strArr[1]==null
			||strArr[1].equals("")) return GMConstant.ERR_PARAMATER_ERROR;
		try
		{
			// if(sid==ActivityContainer.EXP_ID)
			// {
			// initData=params.get("percent")+","+initData;
			// }
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
			else if(type==3)
			{
				state=ActivityContainer.getInstance().getActivityState(sid);
			}
			else
			{
				state=new String[1];
				if(sql==null||sql.trim().equals(""))
					state[0]="{\"erro\":\"sql can not null!\"}";
				else
				{
					Fields[] fields=ActivityContainer.getInstance()
						.getObjectFactory().getQuestionnaireDBAccess()
						.loadSqls(sql);
					StringBuffer sb=new StringBuffer();
					sb.append("{\"type\":1,\"fields\":[");
					for(int i=0;i<fields.length;i++)
					{
						sb.append("[");
						for(int j=0;j<6;j++)
						{
							if(fields[i].getArray()[j]!=null
								&&fields[i].getArray()[j].getValue()!=null
								&&!fields[i].getArray()[j].getValue()
									.equals(""))
							{
								sb.append(fields[i].getArray()[j].getValue()
									.toString());
								if(j!=5) sb.append(",");
							}
							else
							{
								sb.append("null");
								if(j!=5) sb.append(",");
							}
						}
						if(i!=fields.length-1)
							sb.append("],");
						else
							sb.append("]");
					}
					sb.append("]");
					sb.append("}");
					state[0]=sb.toString();
				}
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
		String[] subStrArr=initData.split(",");
		// 第一位为宝石
		for(int j=1;j<subStrArr.length;j+=2)
		{
			int sid=Integer.parseInt(subStrArr[j]);
			int check=SeaBackKit.getSidType(sid);
			if(check==Prop.VALID&&!isOfficer(sid)) return false;
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
