package foxu.sea.gm.operators;

import java.util.Calendar;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.set.ArrayList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.GameDataMemCache;
import foxu.sea.GameData;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 获取运营数据
 * 
 * @author comeback
 * 
 */
public class GetOperationData extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String yearStr=params.get("year");
		String monthStr=params.get("month");
		String plat=params.get("platid");
		if(plat.equals("all"))plat=null;
		
		int year=Integer.parseInt(yearStr);
		int month=Integer.parseInt(monthStr);
		CreatObjectFactory objectFactory=info.getObjectFactory();
		GameDataMemCache memCache=objectFactory.getGameDataCache();
		if(memCache.getSave().getData().getThe_year()==year
			&&memCache.getSave().getData().getThe_month()==month)
		{
			memCache.updata(true,GameDataMemCache.FORCE_DB_TIME);
			memCache.upPlatData(true,GameDataMemCache.FORCE_DB_TIME);
		}
		
		ArrayList list=memCache.getMonthData(year,month,plat);
		ArrayList list1=getMonthData(memCache,year,month,1,plat);
		ArrayList list2=getMonthData(memCache,year,month,2,plat);
		ArrayList list3=getMonthData(memCache,year,month,3,plat);
		list=linkList(list,list1,list2,list3);
		Object listArray[]=sort(list);
		
		int online=objectFactory.getDsmanager().getSessionMap().size();
		for(int i=0;i<listArray.length;i++)
		{
			GameData data=(GameData)listArray[i];
			if(data.getThe_year()!=year||data.getThe_month()!=month) break;
			memCache.Calculation(data);
			JSONObject jo=recordToJSONObject(
				data,
				getGameData(i,listArray,1),
				getGameData(i,listArray,3),
				getGameData(i,listArray,7),
				getGameData(i,listArray,14),
				getGameData(i,listArray,30),
				getGameData(i,listArray,60),
				data.getPlat()==null?online:memCache.getOnline(data
					.getPlat()));
			if(jo!=null) jsonArray.put(jo);
		}
		
		return 0;
	}
	
	private JSONObject recordToJSONObject(GameData data,GameData data1,
		GameData data3,GameData data7,GameData data14,GameData data30,
		GameData data60,int online)
	{
//		System.out.println("==================================================================");
//		System.out.println(data.getThe_month()+":--------data--------:"+data.getThe_day());
		float last_day_rate=0;
		if(data1!=null)
		{
//			System.out.println(data1.getThe_month()+":--------data1--------:"+data1.getThe_day());
			last_day_rate=data1.getLast_day_rate()/100f;
			last_day_rate=(float)(Math.round(last_day_rate*100))/100;
		}

		float three_day_rate=0;
		if(data3!=null)
		{
//			System.out.println(data3.getThe_month()+":--------data3--------:"+data3.getThe_day());
			three_day_rate=data3.getThree_day_rate()/100f;
			three_day_rate=(float)(Math.round(three_day_rate*100))/100;
		}

		float week_rate=0;
		if(data7!=null)
		{
//			System.out.println(data7.getThe_month()+":--------data7--------:"+data7.getThe_day());
			week_rate=data7.getWeek_rate()/100f;
			week_rate=(float)(Math.round(week_rate*100))/100;
		}

		float doublu_week_rate=0;
		if(data14!=null)
		{
//			System.out.println(data14.getThe_month()+":--------data14--------:"+data14.getThe_day());
			doublu_week_rate=data14.getDoublu_week_rate()/100f;
			doublu_week_rate=(float)(Math.round(doublu_week_rate*100))/100;
		}

		float month_rate=0;
		if(data30!=null)
		{
//			System.out.println(data30.getThe_month()+":--------data30--------:"+data30.getThe_day());
			month_rate=data30.getMonth_rate()/100f;
			month_rate=(float)(Math.round(month_rate*100))/100;
		}

		float double_month_rate=0;
		if(data60!=null)
		{
//			System.out.println(data60.getThe_month()+":--------data60--------:"+data60.getThe_day());
			double_month_rate=data60.getDouble_month_rate()/100f;
			double_month_rate=(float)(Math.round(double_month_rate*100))/100;
		}

		float charge_rate=data.getCharge_rate()/100f;
		charge_rate=(float)(Math.round(charge_rate*100))/100;

		// arpdau
		float arpu=data.getArpu()/10000f;
		arpu=(float)(Math.round(arpu*10000))/10000;

		// arppdau
		float arppu=data.getArppu()/10000f;
		arppu=(float)(Math.round(arppu*10000))/10000;

		// apru
		float arpu1=0;
		if(data1!=null)
		{
			arpu1=data1.getArpu1()/10000f;
			arpu1=(float)(Math.round(arpu1*10000))/10000;
		}
		float arpu3=0;
		if(data3!=null)
		{
			arpu3=data3.getArpu3()/10000f;
			arpu3=(float)(Math.round(arpu3*10000))/10000;
		}
		float arpu7=0;
		if(data7!=null)
		{
			arpu7=data7.getArpu7()/10000f;
			arpu7=(float)(Math.round(arpu7*10000))/10000;
		}
		float arpu14=0;
		if(data14!=null)
		{
			arpu14=data14.getArpu14()/10000f;
			arpu14=(float)(Math.round(arpu14*10000))/10000;
		}
		float arpu30=0;
		if(data30!=null)
		{
			arpu30=data30.getArpu30()/10000f;
			arpu30=(float)(Math.round(arpu30*10000))/10000;
		}
		float arpu60=0;
		if(data60!=null)
		{
			arpu60=data60.getArpu60()/10000f;
			arpu60=(float)(Math.round(arpu60*10000))/10000;
		}

		// 活跃付费率
		float dau_charge_rate=data.getDau_charge_rate()/100f;
		dau_charge_rate=(float)(Math.round(dau_charge_rate*100))/100;
		// 累计留存
		float total_rate=data.getTotal_rate()/100f;
		total_rate=(float)(Math.round(total_rate*100))/100;

		try
		{
			JSONObject jo=new JSONObject();
			jo.put(
				GMConstant.DATE,
				data.getThe_year()+"-"+data.getThe_month()+"-"
					+data.getThe_day());
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
			jo.put(GMConstant.DBWEEK_RETENTION,
				String.valueOf(doublu_week_rate));
			jo.put(GMConstant.MONTH_RETENTION,String.valueOf(month_rate));
			jo.put(GMConstant.DBMONTH_RETENTION,
				String.valueOf(double_month_rate));
			jo.put(GMConstant.ARPU,String.valueOf(arpu));
			jo.put(GMConstant.ARPPU,String.valueOf(arppu));
			jo.put(GMConstant.TOTAL_USER,data.getTotal_user());
			jo.put(GMConstant.TOTAL_RECHARGE_USER,
				data.getCharge_total_user());
			jo.put(GMConstant.PAY_RATE,String.valueOf(charge_rate));
			jo.put(GMConstant.TOTAL_RECHARGE,data.getTotal_charge());
			jo.put(GMConstant.TOTAL_ARPU,
				String.valueOf(data.getTotal_arpu()));
			jo.put(GMConstant.TOTAL_ARPPU,
				String.valueOf(data.getTotal_arppu()));

			jo.put(GMConstant.ARPU1,String.valueOf(arpu1));
			jo.put(GMConstant.ARPU3,String.valueOf(arpu3));
			jo.put(GMConstant.ARPU7,String.valueOf(arpu7));
			jo.put(GMConstant.ARPU14,String.valueOf(arpu14));
			jo.put(GMConstant.ARPU30,String.valueOf(arpu30));
			jo.put(GMConstant.ARPU60,String.valueOf(arpu60));

			jo.put(GMConstant.DAU_PAY_RATE,String.valueOf(dau_charge_rate));
			jo.put(GMConstant.TOTAL_RETENTION,String.valueOf(total_rate));

			jo.put(GMConstant.ONLINE,online);
			
			jo.put(GMConstant.PLAT,data.getPlat()==null?"total":data.getPlat());
			jo.put(GMConstant.LOGIN_COUNT,data.getLoginCount());
			jo.put(GMConstant.ONLINE_TIME,data.getOnlineTime());
			
			return jo;
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public Object[] sort(ArrayList list)
	{
		Object[] listArray=list.toArray();
		Object temp;
		// 排序 日期排序
		for(int i=0;i<listArray.length;i++)
		{
			for(int j=i;j<listArray.length;j++)
			{
				GameData dataA=(GameData)listArray[i];
				GameData dataB=(GameData)listArray[j];
				if(dataA.getThe_year()>dataB.getThe_year()
					||(dataA.getThe_year()==dataB.getThe_year()&&dataA
						.getThe_month()>dataB.getThe_month())
					||(dataA.getThe_year()==dataB.getThe_year()
						&&dataA.getThe_month()==dataB.getThe_month()&&dataA
						.getThe_day()>dataB.getThe_day()))
				{
					temp=listArray[i];
					listArray[i]=listArray[j];
					listArray[j]=temp;
				}
			}
		}
		return listArray;
	}
	
	public ArrayList getMonthData(GameDataMemCache memCache,int year,
		int month,int offset,String plat)
	{
		month+=offset;
		if(month>12)
		{
			if(month%12==0)
			{
				year+=month/12-1;
				month=12;
			}else
			{
				year+=month/12;
				month=month%12;
			}
		}
		return memCache.getMonthData(year,month,plat);
	}
	
	public ArrayList linkList(ArrayList list,ArrayList list1,
		ArrayList list2,ArrayList list3)
	{
		list=linkListUnit(list,list1);
		list=linkListUnit(list,list2);
		list=linkListUnit(list,list3);
		return list;
	}
	
	public ArrayList linkListUnit(ArrayList list,ArrayList list1)
	{
		for(int i=0;i<list1.size();i++)
		{
			list.add(list1.get(i));
		}
		return list;
	}
	
	public GameData getGameData(int i,Object[] gameDatas,int offset)
	{
		GameData data=(GameData)gameDatas[i];
		Calendar c=Calendar.getInstance();
		c.set(Calendar.YEAR,data.getThe_year());
		c.set(Calendar.MONTH,data.getThe_month()-1);
		c.set(Calendar.DAY_OF_MONTH,data.getThe_day()+offset);
		int year=c.get(Calendar.YEAR);
		int month=c.get(Calendar.MONTH)+1;
		int day=c.get(Calendar.DAY_OF_MONTH);
		for(int k=i+1;k<gameDatas.length;k++)
		{
			GameData data_x=(GameData)gameDatas[k];
			if(data_x.getThe_day()!=day||data_x.getThe_month()!=month
				||data_x.getThe_year()!=year) continue;
			return data_x;
		}
		return null;
	}
	
}
