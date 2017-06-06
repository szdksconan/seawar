package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import foxu.sea.ContextVarManager;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import javapns.json.JSONArray;
import javapns.json.JSONObject;

/**
 * 上下文变量管理
 * 
 * @author Alan
 */
public class ContextVarManage extends GMOperator
{

	public static final int VIEW=1,SET=2;
	public static final String ALL_VARS="all_vars";
	public static String[] allVars={ContextVarManager.WORLD_CHAT_LEVEL,
		ContextVarManager.PRIVATE_CHAT_LEVEL,ContextVarManager.EMAIL_LEVEL,
		ContextVarManager.CREATE_ALLIANCE_LEVEL_LIMIT,
		ContextVarManager.JOIN_ALLIANCE_LEVEL_LIMIT,
		ContextVarManager.ALLIANCE_DONATE_LEVEL_LIMIT,
		ContextVarManager.ALLIANCE_SHIP_DONATE_LEVEL_LIMIT};

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int type=TextKit.parseInt(params.get("type"));
		String var=params.get("var");
		String[] vars={var};
		if(ALL_VARS.equals(var)) vars=allVars;
		if(type==VIEW)
		{
			return returnJSON(vars,jsonArray);
		}
		else if(type==SET)
		{
			int value=TextKit.parseInt(params.get("value"));
			String dest=params.get("dest");
			for(int i=0;i<vars.length;i++)
			{
				ContextVarManager.VarEntry varObj=ContextVarManager
					.getInstance().getVarEntry(vars[i]);
				if(varObj.getVar()==Integer.MIN_VALUE)
					return GMConstant.ERR_PARAMATER_ERROR;
				ContextVarManager.getInstance().putVar(varObj.getKey(),
					value,dest);
			}
			return returnJSON(vars,jsonArray);
		}
		return GMConstant.ERR_SUCCESS;
	}

	public int returnJSON(String[] vars,JSONArray jsonArray)
	{
		for(int i=0;i<vars.length;i++)
		{
			try
			{
				ContextVarManager.VarEntry var=ContextVarManager
					.getInstance().getVarEntry(vars[i]);
				if(var.getVar()==Integer.MIN_VALUE)
					return GMConstant.ERR_PARAMATER_ERROR;
				JSONObject json=new JSONObject();
				json.put("var",var.getKey());
				json.put("value",var.getVar());
				json.put("dest",var.getDest());
				jsonArray.put(json);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return GMConstant.ERR_UNKNOWN;
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
}
