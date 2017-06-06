package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


public class VertifySetting extends GMOperator
{
	public static final int OPEN=1,CHECK=2;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String statusStr=params.get("vertify_status");
			String triggerCountStr=params.get("vertify_trigger_count");
			String triggerIntervalStr=params.get("vertify_trigger_interval");
			String timeStr=params.get("vertify_time");
			String maxCountStr=params.get("vertify_max_count");
			JSONObject json=new JSONObject();
			if(statusStr!=null)
			{
				int status=Integer.parseInt(statusStr);
				if(status!=CHECK)
				{
					PublicConst.VERTIFY_STATUS=status;
					if(status==OPEN)
					{
						if(triggerCountStr!=null)
						{
							int triggerCount=Integer.parseInt(triggerCountStr);
							PublicConst.VERTIFY_TRIGGER_COUNT=triggerCount;
						}
						if(triggerIntervalStr!=null)
						{
							int triggerInterval=Integer.parseInt(triggerIntervalStr);
							PublicConst.VERTIFY_TRIGGER_INTERVAL=triggerInterval;
						}
						if(timeStr!=null)
						{
							int time=Integer.parseInt(timeStr);
							PublicConst.VERTIFY_TIME=time;
						}
						if(maxCountStr!=null)
						{
							int maxCount=Integer.parseInt(maxCountStr);
							PublicConst.VERTIFY_MAX_COUNT=maxCount;
						}
					}
				}
			}
			
			json.put("status",PublicConst.VERTIFY_STATUS);
			json.put("count",PublicConst.VERTIFY_TRIGGER_COUNT);
			json.put("interval",PublicConst.VERTIFY_TRIGGER_INTERVAL);
			json.put("vertify_time",PublicConst.VERTIFY_TIME);
			json.put("max_count",PublicConst.VERTIFY_MAX_COUNT);
			jsonArray.put(json);
//			System.out.println("-----------------------------");
//			System.out.println("1:"+PublicConst.VERTIFY_STATUS);
//			System.out.println("2:"+PublicConst.VERTIFY_TRIGGER_COUNT);
//			System.out.println("3:"+PublicConst.VERTIFY_TRIGGER_INTERVAL);
//			System.out.println("4:"+PublicConst.VERTIFY_TIME);
//			System.out.println("5:"+PublicConst.VERTIFY_MAX_COUNT);
			return GMConstant.ERR_SUCCESS;
		}
		catch(Exception e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
	}

}
