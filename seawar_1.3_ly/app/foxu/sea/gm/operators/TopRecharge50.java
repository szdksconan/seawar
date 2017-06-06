package foxu.sea.gm.operators;

import java.util.Calendar;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.field.Fields;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.OrderGameDBAccess;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


public class TopRecharge50 extends GMOperator
{
	
	public static final int RANGE_ALL=1,RANGE_DATE=2;
	public static final int MEDAL_OF_HONOR=2021;
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String rangeStr=params.get("range");
		String yearStr=params.get("year");
		String monthStr=params.get("month");
		String dayStr=params.get("day");
		
		int year=0;
		int month=0;
		int day=0;
		int range=RANGE_ALL;
		if(rangeStr!=null)
			range=Integer.parseInt(rangeStr);
		String sql="";
		if(range==RANGE_ALL)
		{
			 sql="select user_name,sum(gems),sum(money) as money,user_id from (select * from orders order by create_at DESC)t group by user_id order by money desc limit 100";
		}
		else if(range==RANGE_DATE)
		{
			if(yearStr!=null&&yearStr.length()>0)
				year=Integer.parseInt(yearStr);
			if(monthStr!=null&&monthStr.length()>0)
				month=Integer.parseInt(monthStr);
			if(dayStr!=null&&dayStr.length()>0)
				day=Integer.parseInt(dayStr);
			Calendar cal=Calendar.getInstance();
			//cal.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			cal.set(Calendar.YEAR,year);
			cal.set(Calendar.MONTH,month>1?month-1:0);
			cal.set(Calendar.DAY_OF_MONTH,day>0?day:1);
			cal.set(Calendar.HOUR,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
			int time1=(int)(cal.getTimeInMillis()/1000L);
			if(day>0)
				cal.add(Calendar.DAY_OF_MONTH,1);
			else
				cal.add(Calendar.MONTH,1);
			int time2=(int)(cal.getTimeInMillis()/1000L);
			sql="select user_name,sum(gems),sum(money) as money,user_id from (select * from orders order by create_at DESC)t " +
				"where create_at>="+time1+" and create_at<"+time2+
				" group by user_id order by money desc limit 100";
		}
		else 
			return GMConstant.ERR_UNKNOWN;
		
		CreatObjectFactory objectFactory=info.getObjectFactory();
		
		Fields fields[]=((OrderGameDBAccess)objectFactory
						.getOrderCache().getDbaccess()).loadSqls(sql);
		if(fields==null)
			return GMConstant.ERR_SUCCESS;
		for(int i=0;i<fields.length;i++)
		{
			try
			{
				JSONObject jo=new JSONObject();
				// 排名
				jo.put(GMConstant.RANKING,(i+1));
				// 角色名
				jo.put(GMConstant.PLAYER_NAME,
					fields[i].getArray()[0].getValue());
				// 充值宝石
				jo.put(GMConstant.RECHARGE_GEMS,
					fields[i].getArray()[1].getValue());
				// 充值RMB
				jo.put(GMConstant.RECHARGE_RMB,
					fields[i].getArray()[2].getValue());
				Player player=objectFactory.getPlayerById((Integer)fields[i]
					.getArray()[3].getValue());
				// 登录时间
				jo.put(GMConstant.UPDATE_TIME,player.getUpdateTime());
				jo.put(GMConstant.RONGYU_ZHANG,player.getBundle()
					.getCountBySid(MEDAL_OF_HONOR));
				jsonArray.put(jo);
			}
			catch(Exception e)
			{
				
			}
		}
		return 0;
	}
	
}
