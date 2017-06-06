package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import foxu.cross.server.CrossAct;
import foxu.cross.server.CrossActManager;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


/**
 * 跨服战管理
 * @author yw
 *
 */
public class CrossWarGM extends GMOperator
{
	/** 开启，修改，关闭，查看 */
	int OPEN=1,MODY=2,CLOSE=3,LOOK=4;
	
	CrossActManager actManager;

	
	public CrossActManager getActManager()
	{
		return actManager;
	}
	
	public void setActManager(CrossActManager actManager)
	{
		this.actManager=actManager;
	}

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		if(!PublicConst.crossServer) return GMConstant.ERR_NOT_DO;
		//System.out.println(params.toString());
		int atype=TextKit.parseInt(params.get("atype"));
		if(atype==OPEN)
		{
			String date=params.get("date");
			String award=params.get("award");
			//System.out.println("------date------:"+date);
			//System.out.println("------award------:"+award);
			int res=actManager.createAct(CrossAct.WAR_SID,date,award);
			//System.out.println("-------res--------:"+res);
			if(res!=0)
			{
				return GMConstant.ERR_PARAMATER_ERROR;
			}
		}
		else if(atype==MODY)
		{
			String award=params.get("award");
			int res=actManager.setAward(CrossAct.WAR_SID,award);
			if(res!=0)
			{
				return GMConstant.ERR_PARAMATER_ERROR;
			}
		}
		else if(atype==CLOSE)
		{
			actManager.forceOver(CrossAct.WAR_SID);
		}
		actManager.getActInfoBySid(CrossAct.WAR_SID,jsonArray);
		return GMConstant.ERR_SUCCESS;
	}

}
