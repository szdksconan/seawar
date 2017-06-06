package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.mem.ForbidMemCache;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.ServerInfo;

/**
 * ∑‚…Ë±∏
 * 
 * @author comeback
 *
 */
public class BannedDevice extends ToCenterOperator
{
	private int ADD=1,DELETE=2,VIEW=3;
	
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			int type=Integer.parseInt(params.get("type"));
			String deviceId=params.get("deviceId");
			if(type!=VIEW&&(deviceId==null||deviceId.equals("")))return GMConstant.ERR_UNKNOWN;
			ForbidMemCache forbid=info.getObjectFactory().getForbidMemCache();
			
			if(type==ADD)
			{
				forbid.add(deviceId);
				
			}else if(type==DELETE)
			{
				forbid.delete(deviceId);
			}else if(type==VIEW)
			{
				Object[] devices=forbid.getDeviceMap().keySet().toArray();
				for(int i=0;i<devices.length;i++)
				{
					jsonArray.put(devices[i]);
				}
			}
			
		}
		catch(Exception e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}

}
