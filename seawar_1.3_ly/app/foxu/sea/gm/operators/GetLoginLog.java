package foxu.sea.gm.operators;

import java.util.Calendar;
import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.mem.LoginLogMemCache;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 查看登陆日志
 * 
 * @author yw
 * 
 */
public class GetLoginLog extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{

		try
		{
			String type=params.get("type");
			String pname=params.get("pname");
			String nm=params.get("month");
			String ny=params.get("year");
			int month=1;
			if(nm!=null && nm.trim().length()!=0)
				month=TextKit.parseInt(nm);
			int year=0;
			if(ny!=null && ny.trim().length()!=0)
				year=Integer.parseInt(ny);
			if(month<=0||month>12)
				return GMConstant.ERR_PARAMATER_ERROR;
			Player player=null;
			if("1".equals(type))
			{
				player=info.getObjectFactory().getPlayerByName(pname,false);
			}
			else if("2".equals(type))
			{
				player=info.getObjectFactory().getPlayerById(
					Integer.parseInt(pname));
			}
			else
			{
				return GMConstant.ERR_PARAMATER_ERROR;
			}
			if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
			JSONObject json=new JSONObject();
			json.put("pname",player.getName());
			jsonArray.put(json);
			LoginLogMemCache.loginLogMem.query(player.getId(),
				getTimeStart(month,year),jsonArray);

		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		return 0;
	}

	public int getTimeStart(int month,int year)
	{
		Calendar c=Calendar.getInstance();
		if(year!=0)
			c.set(Calendar.YEAR,year);
		c.set(Calendar.MONTH,month-1);
		c.set(Calendar.DAY_OF_MONTH,1);
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
		return (int)(c.getTimeInMillis()/1000);
	}

}
