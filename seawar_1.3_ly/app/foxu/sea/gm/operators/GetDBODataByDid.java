package foxu.sea.gm.operators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.io.ByteBuffer;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.sea.GameData;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;


/**
 * 获取大区运营数据  基于设备id
 * @author yw
 *
 */
public class GetDBODataByDid extends GMOperator
{
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String yearStr=params.get("year");
			String monthStr=params.get("month");
			String plat=params.get("platid");
			ByteBuffer data=sendHttpData(yearStr,monthStr,plat,UserToCenterPort.AREA_ID+"");
			int len=data.readUnsignedByte();
			for(int i=0;i<len;i++)
			{
				GameData gdata=new GameData();
				gdata.bytesReadFromCenter(data);
				jsonArray.put(creatGdataJson(gdata));
			}
		}
		catch(Exception e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
		
		return GMConstant.ERR_SUCCESS;
	}

	public ByteBuffer sendHttpData(String year,String month,String plat,String area)
	{
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		// 设置port
		map.put("port","3");
		map.put("table_type","10");
		map.put("year",year);
		map.put("month",month);
		map.put("plat",plat);
		map.put("area",area);
		HttpRespons re=null;
		try
		{
			re=request.send(
				"http://"+GameDBCCAccess.GAME_CENTER_IP+":"+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,
				null);
		}
		catch(IOException e)
		{
			// TODO 自动生成 catch 块
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
	
	public JSONObject creatGdataJson(GameData data)
	{
		float last_day_rate=data.getLast_day_rate()/100f;
		last_day_rate=(float)(Math.round(last_day_rate*100))/100;
		
		float three_day_rate=data.getThree_day_rate()/100f;
		three_day_rate=(float)(Math.round(three_day_rate*100))/100;
		
		float week_rate=data.getWeek_rate()/100f;
		week_rate=(float)(Math.round(week_rate*100))/100;

		float doublu_week_rate=data.getDoublu_week_rate()/100f;
		doublu_week_rate=(float)(Math
			.round(doublu_week_rate*100))/100;

		float month_rate=data.getMonth_rate()/100f;
		month_rate=(float)(Math.round(month_rate*100))/100;
		
		float double_month_rate=data.getDouble_month_rate()/100f;
		double_month_rate=(float)(Math.round(double_month_rate*100))/100;

		float charge_rate=data.getCharge_rate()/100f;
		charge_rate=(float)(Math.round(charge_rate*100))/100;

		// arpdau
		float arpu=data.getArpu()/10000f;
		arpu=(float)(Math.round(arpu*10000))/10000;

		// arppdau
		float arppu=data.getArppu()/10000f;
		arppu=(float)(Math.round(arppu*10000))/10000;
		
		//apru
		float arpu1=data.getArpu1()/10000f;
		arpu1=(float)(Math.round(arpu1*10000))/10000;
		float arpu3=data.getArpu3()/10000f;
		arpu3=(float)(Math.round(arpu3*10000))/10000;
		float arpu7=data.getArpu7()/10000f;
		arpu7=(float)(Math.round(arpu7*10000))/10000;
		float arpu14=data.getArpu14()/10000f;
		arpu14=(float)(Math.round(arpu14*10000))/10000;
		float arpu30=data.getArpu30()/10000f;
		arpu30=(float)(Math.round(arpu30*10000))/10000;
		float arpu60=data.getArpu60()/10000f;
		arpu60=(float)(Math.round(arpu60*10000))/10000;
		
		//活跃付费率
		float dau_charge_rate=data.getDau_charge_rate()/100f;
		dau_charge_rate=(float)(Math.round(dau_charge_rate*100))/100;
		//累计留存
		float total_rate=data.getTotal_rate()/100f;
		total_rate=(float)(Math.round(total_rate*100))/100;
		JSONObject jo=new JSONObject();
		try
		{
			jo.put(GMConstant.DATE,data.getThe_year()+"-"+data.getThe_month()+"-"+data.getThe_day());
			jo.put(GMConstant.NEW_USER,data.getNew_user());
			jo.put(GMConstant.NEW_UDID,data.getNew_udid());
			jo.put(GMConstant.DAU,data.getDau());
			jo.put(GMConstant.RECHARGE,data.getCharge_amount());
			jo.put(GMConstant.RECHARGE_USER,data.getCharge_people());
			jo.put(GMConstant.MAU,data.getMau());
			jo.put(GMConstant.TOP_ONLINE,data.getMaxOnline());
			jo.put(GMConstant.DAY_RETENTION,String.valueOf(last_day_rate));
			jo.put(GMConstant.THDAY_RETENTION,String.valueOf(three_day_rate));
			jo.put(GMConstant.WEEK_RETENTION,String.valueOf(week_rate));
			jo.put(GMConstant.DBWEEK_RETENTION,String.valueOf(doublu_week_rate));
			jo.put(GMConstant.MONTH_RETENTION,String.valueOf(month_rate));
			jo.put(GMConstant.DBMONTH_RETENTION,String.valueOf(double_month_rate));
			jo.put(GMConstant.ARPU,String.valueOf(arpu));
			jo.put(GMConstant.ARPPU,String.valueOf(arppu));
			jo.put(GMConstant.TOTAL_USER,data.getTotal_user());
			jo.put(GMConstant.TOTAL_RECHARGE_USER,data.getCharge_total_user());
			jo.put(GMConstant.PAY_RATE,String.valueOf(charge_rate));
			jo.put(GMConstant.TOTAL_RECHARGE,data.getTotal_charge());
			jo.put(GMConstant.TOTAL_ARPU,String.valueOf(data.getTotal_arpu()));
			jo.put(GMConstant.TOTAL_ARPPU,String.valueOf(data.getTotal_arppu()));
			
			jo.put(GMConstant.ARPU1,String.valueOf(arpu1));
			jo.put(GMConstant.ARPU3,String.valueOf(arpu3));
			jo.put(GMConstant.ARPU7,String.valueOf(arpu7));
			jo.put(GMConstant.ARPU14,String.valueOf(arpu14));
			jo.put(GMConstant.ARPU30,String.valueOf(arpu30));
			jo.put(GMConstant.ARPU60,String.valueOf(arpu60));
			
			jo.put(GMConstant.DAU_PAY_RATE,String.valueOf(dau_charge_rate));
			jo.put(GMConstant.TOTAL_RETENTION,String.valueOf(total_rate));
			
			jo.put(GMConstant.PLAT,data.getPlat());
			
			jo.put(GMConstant.LOGIN_COUNT,data.getLoginCount());
			jo.put(GMConstant.ONLINE_TIME,data.getOnlineTime());
		}
		catch(Exception e)
		{
		}
		
		return jo;
	}

}
