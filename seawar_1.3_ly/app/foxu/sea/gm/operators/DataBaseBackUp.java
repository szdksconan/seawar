package foxu.sea.gm.operators;

import java.io.IOException;
import java.util.Map;

import javapns.json.JSONArray;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;


/**
 * 数据备份
 * @author yw
 *
 */
public class DataBaseBackUp extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String sure=params.get("sure");
		if(sure==null||!sure.equals("true"))
			return GMConstant.ERR_NOT_DO;
		try
		{
			String  path=System.getProperty("user.dir");
			Runtime.getRuntime().exec(path+"/mysql_databak.sh");
		}
		catch(IOException e)
		{
			SeaBackKit.log.error("data backup fail:"+e.toString());
			return GMConstant.ERR_NOT_DO;
		}
		return GMConstant.ERR_SUCCESS;
	}

}
