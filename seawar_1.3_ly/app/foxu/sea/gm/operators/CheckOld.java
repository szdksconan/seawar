package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * �����ɵ�GMSetManager�еĴ��룬Ϊ�����¹��߲����õ�����
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
