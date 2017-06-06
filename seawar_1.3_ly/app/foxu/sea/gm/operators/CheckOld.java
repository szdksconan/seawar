package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 开启旧的GMSetManager中的代码，为避免新工具不可用的问题
 * @author comeback
 *
 */
public class CheckOld extends GMOperator
{
	public static boolean IS_OPEN=false;

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String boolStr=params.get("boolean");
		if("true".equals(boolStr))
			IS_OPEN=true;
		else
			IS_OPEN=false;
		return 0;
	}

}
