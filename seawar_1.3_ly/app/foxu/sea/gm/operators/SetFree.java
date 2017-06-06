package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


/**
 * 修改限制登录白名单
 * @author yw
 *
 */
public class SetFree extends GMOperator
{
	public static int ADD=1,DELETE=2;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int type=TextKit.parseInt(params.get("actionType"));
		String did=params.get("did");
		CreatObjectFactory factory=info.getObjectFactory();
		if(type==ADD)
		{
			factory.getForbidMemCache().addFree(did);
		}else if(type==DELETE)
		{
			factory.getForbidMemCache().deleteFree(did);
		}
		return GMConstant.ERR_SUCCESS;
	}

}
