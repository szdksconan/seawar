package foxu.dcaccess.mem;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import mustang.field.Fields;
import mustang.field.StringField;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.GameDataDBAccess;
import foxu.dcaccess.OrderGameDBAccess;
import foxu.dcaccess.datasave.GameDataSave;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.GameData;
import foxu.sea.Player;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;


/**
 * 基于设备id的运营数据
 * @author yw
 *
 */
public class GDataByDidMemCache extends MemCache
{
	/***/
	public static final int RATE_10000=10000;
	/** 1小时更新到数据库,发送到中心 */
	public static final int GAME_DATA_DB_TIME=60*60*1;
	/** 强制获取最新数据时间 */
	public static final int FORCE_DB_TIME=60*60*1;
	/** 任意平台标识 */
	public static final String ANY_PLAT="any";
	/** 数据提供器 */
	CreatObjectFactory factory;
	/** 各平台运营数据 */
	HashMap<String,GameDataSave> saveMap=new HashMap<String,GameDataSave>();
	
	@Override
	public Object createObect()
	{
		// TODO 自动生成方法存根
		return null;
	}

	/** 关服存储当前数据 */
	public int saveAndExit()
	{
		upPlatData(true,0);
		return 0;
	}

	public void init()
	{
		cacheMap=new IntKeyHashMap();
		int year=SeaBackKit.getTheYear();
		int month=SeaBackKit.getTheMonth();
		int day=SeaBackKit.getDayOfMonth();
		initPlatSave(year,month,day);// 各平台数据
		// 启动定时器
		TimerCenter.getMinuteTimer().add(eventDB);

	}

	/** 初始化各平台数据 */
	public void initPlatSave(int year,int month,int day)
	{
		String[] keys=getKeys();
		if(keys==null||keys.length<=0) return;
		/** 当日运营数据 */
		String sql="SELECT * FROM gdid_data where the_year="+year
			+" and the_month="+month+" and the_day="+day;
		for(int i=0;i<keys.length;i++)
		{
			GameData gameData[]=(GameData[])dbaccess.loadBySql(sql+" and plat='"+keys[i]+"'");
			GameDataSave save=null;
			if(gameData!=null&&gameData.length>0)
			{
				save=new GameDataSave();
				save.setData(gameData[0]);
				save.setSaveTimeDB(TimeKit.getSecondTime());
				cacheMap.put(gameData[0].getId(),save);
			}
			if(save==null)
			{
				// 为今天建一个运营数据记录
				save=new GameDataSave();
				GameData data=new GameData();
				data.setId(uidkit.getPlusUid());
				data.setThe_year(year);
				data.setThe_month(month);
				data.setThe_day(day);
				data.setPlat(keys[i]);
				save.setData(data);
				cacheMap.put(data.getId(),save);
			}
			GameData data=save.getData();
			flushTodayData(data,0);
			save.setSaveTimeDB(TimeKit.getSecondTime());
			saveMap.put(data.getPlat(),save);
			dbaccess.save(data);
		}
	}
	/** 获取平台键值 */
	public String[] getKeys()
	{
		String sql_user="SELECT plat from users GROUP BY plat";
		OrderGameDBAccess orderAccess=(OrderGameDBAccess)factory
			.getOrderCache().getDbaccess();
		HashSet<String> keys=new HashSet<String>();
		Fields[] fields=orderAccess.loadSqls(sql_user);
		if(fields!=null)
		{
			for(int i=0;i<fields.length;i++)
			{
				String key=((StringField)fields[i].get("plat")).value;
				if(key==null||key.equals("")) continue;
				keys.add(key);
			}
		}
		String[] keyStr=new String[keys.size()+1];
		keys.toArray(keyStr);
		keyStr[keys.size()]="any";
		return keyStr;
	}
	/** 获取某个月的运营数据 */
	public ArrayList getMonthData(int year,int month,String plat)
	{
		ArrayList list=new ArrayList();
		String sql="select * from gdid_data where the_year="+year
			+" and the_month="+month;
		sql+=" and plat='"+plat+"'";
		GameData[] datas=((GameDataDBAccess)dbaccess).loadBySql(sql);
		if(datas!=null&&datas.length>0)
		{
			for(int i=0;i<datas.length;i++)
			{
				list.add(datas[i]);
			}
		}
		return list;
	}

	/** 获得玩家总数 */
	public int getTotalUser(String plat)
	{
		String sql="SELECT count(distinct udid) FROM users";
		if(!plat.equals(ANY_PLAT)) sql+=" where plat='"+plat+"'";
		return SeaBackKit.loadBySqlOneData(sql,factory.getUserDBAccess());
	}

	/** 获得多少天前到今天结束登陆的玩家数据 day=0是今天 */
	public int someDayAllDau(int day,String plat)
	{
		// 某天的开始时间
		int someDayBegin=SeaBackKit
			.getSomedayBegin(SeaBackKit.DAY_MILL_TIMES*day);
		String sql="SELECT count(distinct udid) FROM users WHERE login_time>="
			+someDayBegin;
		if(!plat.equals(ANY_PLAT)) sql+=" and plat='"+plat+"'";
		return SeaBackKit.loadBySqlOneData(sql,factory.getUserDBAccess());
	}

	/** 获取今天充值用户总数 不重复 */
	public int getChargeUserDay(int day,String plat)
	{
		int todayStartTime=SeaBackKit
			.getSomedayBegin(SeaBackKit.DAY_MILL_TIMES*day);
		int todayEndTime=SeaBackKit.getSomedayEnd(SeaBackKit.DAY_MILL_TIMES
			*day);
		String sql="SELECT count(distinct udid) FROM orders WHERE create_at>="
			+todayStartTime+" AND create_at<="+todayEndTime;
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		return SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess);
	}

	/**
	 * 某天的注册人数今天登陆过的
	 */
	public int getSomeDayRate(int day,int offset,String plat)
	{
		// 某天的开始时间
		int someDayBegin=SeaBackKit
			.getSomedayBegin(SeaBackKit.DAY_MILL_TIMES*day);
		// 某天的结束时间
		int someDayEnd=SeaBackKit.getSomedayEnd(SeaBackKit.DAY_MILL_TIMES
			*day);
		// 今天开始时间
		int todayBegin=SeaBackKit.getSomedayBegin(SeaBackKit.DAY_MILL_TIMES
			*offset);
		String sql="SELECT count(distinct udid) FROM users WHERE create_at>="
			+someDayBegin+" AND create_at<="+someDayEnd+" AND login_time>="
			+todayBegin;
		if(!plat.equals(ANY_PLAT)) sql+=" and plat='"+plat+"'";
		return SeaBackKit.loadBySqlOneData(sql,factory.getUserDBAccess());
	}

	/** 某天的新增UDID */
	public int getSomeDayNewUdid(int day,String plat)
	{
		// 某天的开始时间
		int someDayBegin=SeaBackKit
			.getSomedayBegin(SeaBackKit.DAY_MILL_TIMES*day);
		// 某天的结束时间
		int someDayEnd=SeaBackKit.getSomedayEnd(SeaBackKit.DAY_MILL_TIMES
			*day);
		String sql="SELECT count(distinct udid) FROM users WHERE create_at>="
			+someDayBegin+" AND create_at<="+someDayEnd;
		if(!plat.equals(ANY_PLAT)) sql+=" and plat='"+plat+"'";
		return SeaBackKit.loadBySqlOneData(sql,factory.getUserDBAccess());
	}

	/** 某天的注册人数 */
	public int getSomeDayNewUsers(int day,String plat)
	{
		// 某天的开始时间
		int someDayBegin=SeaBackKit
			.getSomedayBegin(SeaBackKit.DAY_MILL_TIMES*day);
		// 某天的结束时间
		int someDayEnd=SeaBackKit.getSomedayEnd(SeaBackKit.DAY_MILL_TIMES
			*day);
		String sql="SELECT count(*) FROM users WHERE create_at>="
			+someDayBegin+" AND create_at<"+someDayEnd;
		if(!plat.equals(ANY_PLAT)) sql+=" and plat='"+plat+"'";
		return SeaBackKit.loadBySqlOneData(sql,factory.getUserDBAccess());
	}

	/** 获取某天充值的总数 */
	public int getChargeToDay(int day,String plat)
	{
		int todayStartTime=SeaBackKit
			.getSomedayBegin(SeaBackKit.DAY_MILL_TIMES*day);
		int todayEndTime=SeaBackKit.getSomedayEnd(SeaBackKit.DAY_MILL_TIMES
			*day);
		String sql;
		float usmoney=0;
		// 6元
		sql="SELECT count(*) FROM orders WHERE create_at>="+todayStartTime
			+" AND create_at<"+todayEndTime+" AND money=6";
		if(plat!=null) sql+=" and plat_id='"+plat+"'";
		usmoney+=99f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 18
		sql="SELECT count(*) FROM orders WHERE create_at>="+todayStartTime
			+" AND create_at<"+todayEndTime+" AND money=18";
		if(plat!=null) sql+=" and plat_id='"+plat+"'";
		usmoney+=299f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 50
		sql="SELECT count(*) FROM orders WHERE create_at>="+todayStartTime
			+" AND create_at<"+todayEndTime+" AND money=50";
		if(plat!=null) sql+=" and plat_id='"+plat+"'";
		usmoney+=799f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 98
		sql="SELECT count(*) FROM orders WHERE create_at>="+todayStartTime
			+" AND create_at<"+todayEndTime+" AND money=98";
		if(plat!=null) sql+=" and plat_id='"+plat+"'";
		usmoney+=1499f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 328
		sql="SELECT count(*) FROM orders WHERE create_at>="+todayStartTime
			+" AND create_at<"+todayEndTime+" AND money=328";
		if(plat!=null) sql+=" and plat_id='"+plat+"'";
		usmoney+=4999f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 648
		sql="SELECT count(*) FROM orders WHERE create_at>="+todayStartTime
			+" AND create_at<"+todayEndTime+" AND money=648";
		if(plat!=null) sql+=" and plat_id='"+plat+"'";
		usmoney+=9999f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 25
		sql="SELECT count(*) FROM orders WHERE create_at>="+todayStartTime
			+" AND create_at<"+todayEndTime+" AND money=25";
		if(plat!=null) sql+=" and plat_id='"+plat+"'";
		usmoney+=399f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		usmoney+=0.5;
		return (int)usmoney;
	}

	/** 获取充值用户总数 不重复 */
	public int getChargeUsers(String plat)
	{
//		String sql="SELECT count(distinct user_id) FROM orders";
		String sql="SELECT count(distinct udid) FROM orders";
		
		if(!plat.equals(ANY_PLAT)) sql+=" where plat_id='"+plat+"'";
		return SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess);
	}

	/** 获得充值总额 */
	public int getChargeTotal(String plat)
	{
		String sql="SELECT count(*) FROM orders where money=6";
		float usmoney=0;
		// 6元
		sql="SELECT count(*) FROM orders where money=6";
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		usmoney+=99f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 18
		sql="SELECT count(*) FROM orders where money=18";
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		usmoney+=299f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 50
		sql="SELECT count(*) FROM orders where money=50";
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		usmoney+=799f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 98
		sql="SELECT count(*) FROM orders where money=98";
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		usmoney+=1499f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 328
		sql="SELECT count(*) FROM orders where money=328";
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		usmoney+=4999f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 648
		sql="SELECT count(*) FROM orders where money=648";
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		usmoney+=9999f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		// 25
		sql="SELECT count(*) FROM orders where money=25";
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		usmoney+=399f*SeaBackKit.loadBySqlOneData(sql,
			factory.getOrderCache().dbaccess)/100f;
		usmoney+=0.5;
		return (int)usmoney;
	}

	public Object load(String key)
	{
		GameDataSave data=(GameDataSave)cacheMap.get(Integer.parseInt(key));
		if(data!=null)
		{
			if(changeListMap.get(Integer.parseInt(key))==null)
			{
				changeListMap.put(Integer.parseInt(key),data);
			}
			return data.getData();
		}
		return null;
	}

	public Object[] loads(String[] keys)
	{
		// TODO 自动生成方法存根
		return null;
	}

	public void save(String key,Object data)
	{
		// TODO 自动生成方法存根

	}

	/** 刷新各平台数据 */
	public void upPlatData(boolean bool,int forceTime)
	{
		int nowTime=TimeKit.getSecondTime();
		String[] keys=getKeys();
		if(keys==null||keys.length<=0) return;
		// 判断数据库最后一条记录是不是今天
		int year=SeaBackKit.getTheYear();
		int month=SeaBackKit.getTheMonth();
		int day=SeaBackKit.getDayOfMonth();
		for(int i=0;i<keys.length;i++)
		{
			GameDataSave save=saveMap.get(keys[i]);
			GameData data=null;
			if(save==null)
			{
				save=new GameDataSave();
				// 新建一个记录数据
				data=new GameData();
				data.setId(uidkit.getPlusUid());
				data.setThe_year(year);
				data.setThe_month(month);
				data.setThe_day(day);
				data.setPlat(keys[i]);
				save.setData(data);
				save.setSaveTimeDB(TimeKit.getSecondTime());
				cacheMap.put(data.getId(),save);
				saveMap.put(data.getPlat(),save);
			}
			data=save.getData();
			if(day!=data.getThe_day())
			{
				flushTodayData(data,1);
				// 存储当前的
				dbaccess.save(data);
				sendToGameCenter(data);
				save=new GameDataSave();
				// 新建一个记录数据
				data=new GameData();
				data.setId(uidkit.getPlusUid());
				data.setThe_year(year);
				data.setThe_month(month);
				data.setThe_day(day);
				data.setPlat(keys[i]);
				save.setData(data);
				save.setSaveTimeDB(TimeKit.getSecondTime());
				cacheMap.put(data.getId(),save);
				saveMap.put(data.getPlat(),save);
				continue;
			}
			if(bool)
			{
				if((save.getForceDbTime()+forceTime)>nowTime) continue;
				save.setForceDbTime(TimeKit.getSecondTime());
				// 更新最新数据
				flushTodayData(data,0);
				dbaccess.save(data);
				sendToGameCenter(data);
				continue;
			}
			if((save.getSaveTimeDB()+GAME_DATA_DB_TIME)>nowTime) continue;
			// 更新最新数据
			flushTodayData(data,0);
			// 存储数据
			save.setSaveTimeDB(nowTime);
			dbaccess.save(data);
			sendToGameCenter(data);
			continue;
		}

	}

	/**
	 * 将本服数据发送到game center
	 * 
	 * @param data
	 */
	private void sendToGameCenter(GameData data)
	{
		ByteBuffer bb=new ByteBuffer();
		data.bytesWriteToCenter(bb);
		String b64=SeaBackKit.createBase64(bb);

		HttpRequester request=new HttpRequester();
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",b64);
		map.put("serverID",String.valueOf(UserToCenterPort.SERVER_ID));
		map.put("areaID",String.valueOf(UserToCenterPort.AREA_ID));
		map.put("table_type","9");
		// 设置port
		map.put("port","3");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
				+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,null);
		}
		catch(IOException e)
		{
			// TODO 自动生成 catch 块
			e.printStackTrace();
			return;
		}
	}

	/** 每4小时存储数据库 */
	public void onTimer(TimerEvent e)
	{
		// 每4个小时更新一次
		if(e.getParameter().equals("db"))
		{
			upPlatData(false,0);
		}
	}

	// 更新最新数据
	public void flushTodayData(GameData data,int offset)
	{
		// new_user
		int new_user=getSomeDayNewUsers(offset,data.getPlat());
		data.setNew_user(new_user);
		int new_udid=getSomeDayNewUdid(offset,data.getPlat());
		data.setNew_udid(new_udid);
		// dau
		int dau=someDayAllDau(offset,data.getPlat());
		data.setDau(dau);
		// mau
		int mau=someDayAllDau(30+offset,data.getPlat());
		data.setMau(mau);
		// 充值额度
		data.setCharge_amount(getChargeToDay(offset,data.getPlat()));
		// 今天充值人数
		int charge_people=getChargeUserDay(offset,data.getPlat());
		data.setCharge_people(charge_people);
		int someDayNewUsers=getSomeDayNewUdid(1+offset,data.getPlat());
		int last_day_rate=0;
		// 隔日重登率 扩大10000倍
		int rate1_m=getSomeDayRate(1+offset,offset,data.getPlat());
		if(someDayNewUsers!=0)
			last_day_rate=rate1_m*RATE_10000/someDayNewUsers;
		data.setLast_day_rate(last_day_rate);
		data.setRate1_m(rate1_m);
		data.setRate1_d(someDayNewUsers);
		someDayNewUsers=getSomeDayNewUdid(3+offset,data.getPlat());
		int three_day_rate=0;
		// 3日重登率 扩大10000倍
		int rate3_m=getSomeDayRate(3+offset,offset,data.getPlat());
		if(someDayNewUsers!=0)
			three_day_rate=rate3_m*RATE_10000/someDayNewUsers;
		data.setThree_day_rate(three_day_rate);
		data.setRate3_m(rate3_m);
		data.setRate3_d(someDayNewUsers);
		int week_rate=0;
		someDayNewUsers=getSomeDayNewUdid(7+offset,data.getPlat());
		// 大于7天重登率 扩大10000倍
		int rate7_m=getSomeDayRate(7+offset,offset,data.getPlat());
		if(someDayNewUsers!=0) week_rate=rate7_m*RATE_10000/someDayNewUsers;
		data.setWeek_rate(week_rate);
		data.setRate7_m(rate7_m);
		data.setRate7_d(someDayNewUsers);
		// 大于14天重登率 扩大10000倍
		int double_week_rate=0;
		someDayNewUsers=getSomeDayNewUdid(14+offset,data.getPlat());
		int rate14_m=getSomeDayRate(14+offset,offset,data.getPlat());
		if(someDayNewUsers!=0)
			double_week_rate=rate14_m*RATE_10000/someDayNewUsers;
		data.setDoublu_week_rate(double_week_rate);
		data.setRate14_m(rate14_m);
		data.setRate14_d(someDayNewUsers);
		// 大于30天重登率 扩大10000倍
		int month_rate=0;
		someDayNewUsers=getSomeDayNewUdid(30+offset,data.getPlat());
		int rate30_m=getSomeDayRate(30+offset,offset,data.getPlat());
		if(someDayNewUsers!=0)
			month_rate=rate30_m*RATE_10000/someDayNewUsers;
		data.setMonth_rate(month_rate);
		data.setRate30_m(rate30_m);
		data.setRate30_d(someDayNewUsers);
		// 60天重登率 扩大10000倍
		int double_month_rate=0;
		someDayNewUsers=getSomeDayNewUdid(60+offset,data.getPlat());
		int rate60_m=getSomeDayRate(60+offset,offset,data.getPlat());
		if(someDayNewUsers!=0)
			double_month_rate=rate60_m*RATE_10000/someDayNewUsers;
		data.setDouble_month_rate(double_month_rate);
		data.setRate60_m(rate60_m);
		data.setRate60_d(someDayNewUsers);
		// 总用户数量
		int totalUsers=getTotalUser(data.getPlat());
		data.setTotal_user(totalUsers);
		// 充值用户总数
		int chargeUser=getChargeUsers(data.getPlat());
		data.setCharge_total_user(chargeUser);
		if(totalUsers==0) totalUsers=1;
		// 总充值率
		int charge_rate=chargeUser*RATE_10000/totalUsers;
		data.setCharge_rate(charge_rate);
		// 总充值额度
		int charge_total=getChargeTotal(data.getPlat());
		data.setTotal_charge(charge_total);
		// 新增 扩大10000倍
		int arpu1=0;
		someDayNewUsers=getSomeDayNewUdid(1,data.getPlat());
		int arpu1_m=getSomeCharge(1,offset,data.getPlat());
		if(someDayNewUsers!=0)
			arpu1=(int)(arpu1_m/(float)someDayNewUsers*RATE_10000);
		data.setArpu1(arpu1);
		data.setArpu1_m(arpu1_m);
		data.setArpu1_d(someDayNewUsers);
		int arpu3=0;
		someDayNewUsers=getSomeDayNewUdid(3,data.getPlat());
		int arpu3_m=getSomeCharge(3,offset,data.getPlat());
		if(someDayNewUsers!=0)
			arpu3=(int)(arpu3_m/(float)someDayNewUsers*RATE_10000);
		data.setArpu3(arpu3);
		data.setArpu3_m(arpu3_m);
		data.setArpu3_d(someDayNewUsers);
		int arpu7=0;
		someDayNewUsers=getSomeDayNewUdid(7,data.getPlat());
		int arpu7_m=getSomeCharge(7,offset,data.getPlat());
		if(someDayNewUsers!=0)
			arpu7=(int)(arpu7_m/(float)someDayNewUsers*RATE_10000);
		data.setArpu7(arpu7);
		data.setArpu7_m(arpu7_m);
		data.setArpu7_d(someDayNewUsers);
		int arpu14=0;
		someDayNewUsers=getSomeDayNewUdid(14,data.getPlat());
		int arpu14_m=getSomeCharge(14,offset,data.getPlat());
		if(someDayNewUsers!=0)
			arpu1=(int)(arpu14_m/(float)someDayNewUsers*RATE_10000);
		data.setArpu14(arpu14);
		data.setArpu14_m(arpu14_m);
		data.setArpu14_d(someDayNewUsers);
		int arpu30=0;
		someDayNewUsers=getSomeDayNewUdid(30,data.getPlat());
		int arpu30_m=getSomeCharge(30,offset,data.getPlat());
		if(someDayNewUsers!=0)
			arpu30=(int)(arpu30_m/(float)someDayNewUsers*RATE_10000);
		data.setArpu30(arpu30);
		data.setArpu30_m(arpu30_m);
		data.setArpu30_d(someDayNewUsers);
		int arpu60=0;
		someDayNewUsers=getSomeDayNewUdid(60,data.getPlat());
		int arpu60_m=getSomeCharge(60,offset,data.getPlat());
		if(someDayNewUsers!=0)
			arpu60=(int)(arpu60_m/(float)someDayNewUsers*RATE_10000);
		data.setArpu60(arpu60);
		data.setArpu60_m(arpu60_m);
		data.setArpu60_d(someDayNewUsers);
		Calculation(data);
		if(data.getPlat().equals(ANY_PLAT))
		{
			data.setOnlineTime(cpuOnlineTime(dau));
		}
	}
	public void Calculation(GameData data)
	{
		// 今天arpdau 扩大10000倍
		int arpu=0;
		if(data.getDau()!=0)
			arpu=(data.getCharge_amount()*RATE_10000)/data.getDau();
		data.setArpu(arpu);
		// 今天arppdau 扩大10000倍
		int arppu=0;
		if(data.getCharge_people()!=0)
			arppu=(data.getCharge_amount()*RATE_10000)
				/data.getCharge_people();
		data.setArppu(arppu);

		// 总arpu值 (LTV(All))
		float total_arpu=0.000f;
		if(data.getTotal_user()!=0)
			total_arpu=((float)data.getTotal_charge())
				/(float)data.getTotal_user();
		data.setTotal_arpu(total_arpu);
		float total_arppu=0.000f;
		// 总arppu(LTV(Paid))
		if(data.getCharge_total_user()!=0)
			total_arppu=(float)data.getTotal_charge()
				/(float)data.getCharge_total_user();
		data.setTotal_arppu(total_arppu);

		// 累计存留
		int total_rate=0;
		if(data.getTotal_user()!=0)
			total_rate=(data.getDau()*RATE_10000)/data.getTotal_user();
		data.setTotal_rate(total_rate);
		// 活跃付费率
		int dau_charge_rate=0;
		if(data.getDau()!=0)
			dau_charge_rate=(data.getCharge_people()*RATE_10000)
				/data.getDau();
		data.setDau_charge_rate(dau_charge_rate);
	}
	
	/**
	 *  计算在线时间
	 */
	public int cpuOnlineTime(int dau)
	{
		if(dau<=0)return 0;
		Object[] objs=factory.getPlayerCache().getCacheMap().valueArray();
		if(objs==null)return 0;
		int totalTime=0;
		for(int i=0;i<objs.length;i++)
		{
			if(objs[i]==null)continue;
			Player player=((PlayerSave)objs[i]).getData();
			if(player==null)continue;
			totalTime+=player.getCurrOnlineTime();
		}
		return totalTime/dau;
	}

	/**
	 * @return factory
	 */
	public CreatObjectFactory getFactory()
	{
		return factory;
	}

	/**
	 * @param factory 要设置的 factory
	 */
	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	@Override
	public void deleteCache(Object save)
	{
		// TODO 自动生成方法存根

	}

	/**
	 * 某天的注册玩家一段时间内的累计充值量
	 */
	public int getSomeCharge(int day,int offset,String plat)
	{
		// 时间段的结束时间
		int someDayEnd=SeaBackKit.getSomedayEnd(SeaBackKit.DAY_MILL_TIMES
			*offset);
		// 注册的开始时间
		int registerBegin=SeaBackKit
			.getSomedayBegin(SeaBackKit.DAY_MILL_TIMES*day);
		// 注册结束时间
		int registerEnd=SeaBackKit.getSomedayEnd(SeaBackKit.DAY_MILL_TIMES
			*day);

		String sql="select count(orders.money) from orders,users where users.create_at>="
			+registerBegin
			+" and users.create_at<="
			+registerEnd
			+" and orders.create_at>="
			+registerBegin
			+" and orders.create_at<="
			+someDayEnd
			+" and orders.user_id=users.player_id";
		if(!plat.equals(ANY_PLAT)) sql+=" and plat_id='"+plat+"'";
		float usmoney=0;
		// 6元
		usmoney+=99f*SeaBackKit.loadBySqlOneData(sql+" AND money=6",
			factory.getOrderCache().dbaccess)/100f;
		// 18
		usmoney+=299f*SeaBackKit.loadBySqlOneData(sql+" AND money=18",
			factory.getOrderCache().dbaccess)/100f;
		// 50
		usmoney+=799f*SeaBackKit.loadBySqlOneData(sql+" AND money=50",
			factory.getOrderCache().dbaccess)/100f;
		// 98
		usmoney+=1499f*SeaBackKit.loadBySqlOneData(sql+" AND money=98",
			factory.getOrderCache().dbaccess)/100f;
		// 328
		usmoney+=4999f*SeaBackKit.loadBySqlOneData(sql+" AND money=328",
			factory.getOrderCache().dbaccess)/100f;
		// 648
		usmoney+=9999f*SeaBackKit.loadBySqlOneData(sql+" AND money=648",
			factory.getOrderCache().dbaccess)/100f;
		// 25
		usmoney+=399f*SeaBackKit.loadBySqlOneData(sql+" AND money=25",
			factory.getOrderCache().dbaccess)/100f;
		usmoney+=0.5;
		return (int)usmoney;

	}

	/** 获取昨天的运营数据 */
	public int getYesterdayMaxOnline(int year,int month,int day)
	{
		int max=0;
		String sql="select * from game_data where the_year="+year
			+" and the_month="+month+" and the_day="+day;
		GameData[] datas=((GameDataDBAccess)dbaccess).loadBySql(sql);
		if(datas!=null&&datas.length>0)
		{
			for(int i=0;i<datas.length;i++)
			{
				if(datas[i].getMax_online()>max)
					max=datas[i].getMax_online();
			}
		}
		return max;
	}
	/**获取运营数据的条数**/
	public boolean getCount()
	{
		String sql="select * from game_data";
		GameData[] datas=((GameDataDBAccess)dbaccess).loadBySql(sql);
		if(datas!=null&&datas.length!=0) return false;
		return true;
	}
}
