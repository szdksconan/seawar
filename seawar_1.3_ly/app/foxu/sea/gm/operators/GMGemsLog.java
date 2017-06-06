package foxu.sea.gm.operators;

import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.orm.ConnectionManager;
import mustang.orm.SqlKit;
import mustang.text.TextKit;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 查询GM添加的宝石<br />
 * 详细记录，GM账号<br />
 * GM账号,宝石数量,是否记入最大宝石数,玩家,日期
 * @author comeback
 *
 */
public class GMGemsLog extends GMOperator
{
	
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String yearStr=params.get("year");
		String monthStr=params.get("month");
		if(yearStr==null||monthStr==null||yearStr.length()==0||monthStr.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		int start,end;
		try
		{
			int year=Integer.parseInt(yearStr);
			int month=Integer.parseInt(monthStr);
			Calendar cal=Calendar.getInstance();
			//cal.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			cal.set(Calendar.YEAR,year);
			cal.set(Calendar.MONTH,month>1?month-1:0);
			cal.set(Calendar.DAY_OF_MONTH,1);
			cal.set(Calendar.HOUR_OF_DAY,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
			start=(int)(cal.getTimeInMillis()/1000L);
			cal.add(Calendar.MONTH,1);
			end=(int)(cal.getTimeInMillis()/1000L);
		}
		catch(Exception e)
		{
			return GMConstant.ERR_PARAMATER_ERROR;
		}
		String sql="SELECT * FROM gm_tracks WHERE command='"+AddResources.COMMAND+"' and created_at>"+start+" and created_at<="+end;
		
		ConnectionManager cm=info.getConnectionManager();
		Fields[] fields=SqlKit.querys(cm,sql);
		if(fields==null)
			return GMConstant.ERR_SUCCESS;
		for(int i=0;i<fields.length;i++)
		{
			String account=((StringField)fields[i].get("user_account")).value;
			String playerName=((StringField)fields[i].get("player_name")).value;
			String parameters=((StringField)fields[i].get("parameters")).value;
			parameters=URLDecoder.decode(parameters);
			int time=((IntField)fields[i].get("created_at")).value;
			JSONObject jo=createRecord(account,playerName,parameters,time);
			if(jo!=null)
				jsonArray.put(jo);
		}
		return GMConstant.ERR_SUCCESS;
	}

	private JSONObject createRecord(String account,String playerName,String parameters,int time)
	{
		int start,end;
		
		// 查询gems
		start=parameters.indexOf("max_gems=".intern());
		if(start<0)
			return null;
		end=parameters.indexOf('&',start);
		if(end<0)
			end=parameters.length();
		String maxGemsStr=parameters.substring(start+9,end);
		// 处理掉gems
		parameters=TextKit.replace(parameters,"max_gems","");
		
		start=parameters.indexOf("gems=");
		if(start<0)
			return null;
		end=parameters.indexOf('&',start);
		if(end<0)
			end=parameters.length();
		String gemsStr=parameters.substring(start+5,end);
		if("0".equals(gemsStr))
			return null;
		if(parameters.indexOf("reduce")>0)
			gemsStr="-"+gemsStr;
		try
		{
			JSONObject jo=new JSONObject();
			jo.put(GMConstant.ACCOUNT,account);
			jo.put(GMConstant.PLAYER_NAME,playerName);
			jo.put(GMConstant.GEMS,gemsStr);
			jo.put(GMConstant.MAX_GEMS,maxGemsStr);
			jo.put(GMConstant.TIME,time);
			
			return jo;
		}
		catch(JSONException e)
		{
		}
		return null;
	}
}
