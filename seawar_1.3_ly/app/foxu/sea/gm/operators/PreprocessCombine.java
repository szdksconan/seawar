package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.combine.CombineManager;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 合服数据预处理
 * 
 * @author comeback
 * 
 */
public class PreprocessCombine extends GMOperator
{
	CombineManager combineManager;
	
	public void setCombineManager(CombineManager manager)
	{
		this.combineManager=manager;
	}
	
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		boolean bool =Boolean.parseBoolean(params.get("boolean"));
		if(bool)
		{
			combineManager.startCombine();
		}
		else
		{
			combineManager.preprocessData();
		}
		return 0;
	}
	
	

}
