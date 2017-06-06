package foxu.dcaccess;

import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import shelby.dc.GameDBAccess;
import foxu.sea.GameData;

/**
 * 邮件加载器 author:icetiger
 */
public class GameDataDBAccess extends GameDBAccess
{
	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(MessageGameDBAccess.class);

	public GameData load(String id)
	{
		// 构造一个空域（包括了player表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"GameDataDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		GameData gameData=mapping(fields);
		return gameData;
	}

	public GameData[] loadBySql(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields[] array=null;
		try
		{
			array=SqlKit.querys(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"GameDataDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		GameData[] gameData=new GameData[array.length];
		for(int i=0;i<array.length;i++)
		{
			gameData[i]=mapping(array[i]);
		}
		return gameData;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[32];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("new_user",0);
		array[i++]=FieldKit.create("new_udid",0);
		array[i++]=FieldKit.create("dau",0);
		array[i++]=FieldKit.create("mau",0);
		array[i++]=FieldKit.create("max_online",0);
		array[i++]=FieldKit.create("charge_amount",0);
		array[i++]=FieldKit.create("charge_people",0);
		
		array[i++]=FieldKit.create("arpu1",0);
		array[i++]=FieldKit.create("arpu3",0);
		array[i++]=FieldKit.create("arpu7",0);
		array[i++]=FieldKit.create("arpu14",0);
		array[i++]=FieldKit.create("arpu30",0);
		array[i++]=FieldKit.create("arpu60",0);
		
		array[i++]=FieldKit.create("last_day_rate",0);
		array[i++]=FieldKit.create("three_day_rate",0);
		array[i++]=FieldKit.create("week_rate",0);
		array[i++]=FieldKit.create("doublu_week_rate",0);
		array[i++]=FieldKit.create("month_rate",0);
		array[i++]=FieldKit.create("double_month_rate",0);
		
		array[i++]=FieldKit.create("arpu",0);
		array[i++]=FieldKit.create("arppu",0);
		array[i++]=FieldKit.create("total_user",0);
		array[i++]=FieldKit.create("charge_total_user",0);
		array[i++]=FieldKit.create("charge_rate",0);
		array[i++]=FieldKit.create("total_charge",0);
		array[i++]=FieldKit.create("the_year",0);
		array[i++]=FieldKit.create("the_month",0);
		array[i++]=FieldKit.create("the_day",0);
		array[i++]=FieldKit.create("plat",(String)null);
		array[i++]=FieldKit.create("login_count",0);
		array[i++]=FieldKit.create("online_time",0);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public GameData mapping(Fields fields)
	{
		GameData gameData=new GameData();
		int gameDataId=((IntField)fields.get("id")).value;
		gameData.setId(gameDataId);
		int data=((IntField)fields.get("new_user")).value;
		gameData.setNew_user(data);
		int new_udid=((IntField)fields.get("new_udid")).value;
		gameData.setNew_udid(new_udid);
		int dau=((IntField)fields.get("dau")).value;
		gameData.setDau(dau);
		int mau=((IntField)fields.get("mau")).value;
		gameData.setMau(mau);
		int max_online=((IntField)fields.get("max_online")).value;
		gameData.setMaxOnline(max_online);
		int chargeAmount=((IntField)fields.get("charge_amount")).value;
		gameData.setCharge_amount(chargeAmount);
		int chargePeople=((IntField)fields.get("charge_people")).value;
		gameData.setCharge_people(chargePeople);
		
		int arpu1=((IntField)fields.get("arpu1")).value;
		gameData.setArpu1(arpu1);
		int arpu3=((IntField)fields.get("arpu3")).value;
		gameData.setArpu3(arpu3);
		int arpu7=((IntField)fields.get("arpu7")).value;
		gameData.setArpu7(arpu7);
		int arpu14=((IntField)fields.get("arpu14")).value;
		gameData.setArpu14(arpu14);
		int arpu30=((IntField)fields.get("arpu30")).value;
		gameData.setArpu30(arpu30);
		int arpu60=((IntField)fields.get("arpu60")).value;
		gameData.setArpu60(arpu60);
		
		int last_day_rate=((IntField)fields.get("last_day_rate")).value;
		gameData.setLast_day_rate(last_day_rate);
		int week_rate=((IntField)fields.get("week_rate")).value;
		gameData.setWeek_rate(week_rate);
		int doublu_week_rate=((IntField)fields.get("doublu_week_rate")).value;
		gameData.setDoublu_week_rate(doublu_week_rate);
		int month_rate=((IntField)fields.get("month_rate")).value;
		gameData.setMonth_rate(month_rate);
		int arpu=((IntField)fields.get("arpu")).value;
		gameData.setArpu(arpu);
		int arppu=((IntField)fields.get("arppu")).value;
		gameData.setArppu(arppu);
		int total_user=((IntField)fields.get("total_user")).value;
		gameData.setTotal_user(total_user);
		int charge_total_user=((IntField)fields.get("charge_total_user")).value;
		gameData.setCharge_total_user(charge_total_user);
		int charge_rate=((IntField)fields.get("charge_rate")).value;
		gameData.setCharge_rate(charge_rate);
		int total_charge=((IntField)fields.get("total_charge")).value;
		gameData.setTotal_charge(total_charge);
		int the_year=((IntField)fields.get("the_year")).value;
		gameData.setThe_year(the_year);
		int the_month=((IntField)fields.get("the_month")).value;
		gameData.setThe_month(the_month);
		int the_day=((IntField)fields.get("the_day")).value;
		gameData.setThe_day(the_day);
		String plat=((StringField)fields.get("plat")).value;
		gameData.setPlat(plat);
		int login_count=((IntField)fields.get("login_count")).value;
		gameData.setLoginCount(login_count);
		int online_time=((IntField)fields.get("online_time")).value;
		gameData.setOnlineTime(online_time);
		return gameData;
	}

	/** 保存方法 使用包含玩家数据的字节数组初始化指定姓名的玩家并保存 */
	public boolean save(int id,Object gameData)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+gameData.toString());
		try
		{
			// int offset=data.offset();
			// Message message=new Message();
			// message.bytesRead(data);
			// data.setOffset(offset);
			return save(gameData);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object gameData)
	{
		if(gameData==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",((GameData)gameData)
			.getId()),mapping(gameData));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+gameData);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object m)
	{
		GameData gameData=(GameData)m;
		FieldObject[] array=new FieldObject[32];
		int i=0;
		array[i++]=FieldKit.create("id",gameData.getId());
		array[i++]=FieldKit.create("new_user",gameData.getNew_user());
		array[i++]=FieldKit.create("new_udid",gameData.getNew_udid());
		array[i++]=FieldKit.create("dau",gameData.getDau());
		array[i++]=FieldKit.create("mau",gameData.getMau());
		array[i++]=FieldKit.create("max_online",gameData.getMaxOnline());
		array[i++]=FieldKit.create("charge_amount",gameData
			.getCharge_amount());
		array[i++]=FieldKit.create("charge_people",gameData
			.getCharge_people());
		
		array[i++]=FieldKit.create("arpu1",gameData.getArpu1());
		array[i++]=FieldKit.create("arpu3",gameData.getArpu3());
		array[i++]=FieldKit.create("arpu7",gameData.getArpu7());
		array[i++]=FieldKit.create("arpu14",gameData.getArpu14());
		array[i++]=FieldKit.create("arpu30",gameData.getArpu30());
		array[i++]=FieldKit.create("arpu60",gameData.getArpu60());

		array[i++]=FieldKit.create("last_day_rate",gameData
			.getLast_day_rate());
		array[i++]=FieldKit.create("three_day_rate",gameData
			.getLast_day_rate());
		array[i++]=FieldKit.create("week_rate",gameData.getWeek_rate());
		array[i++]=FieldKit.create("doublu_week_rate",gameData
			.getDoublu_week_rate());
		array[i++]=FieldKit.create("month_rate",gameData.getMonth_rate());
		array[i++]=FieldKit.create("double_month_rate",gameData.getMonth_rate());
		
		array[i++]=FieldKit.create("arpu",gameData.getArpu());
		array[i++]=FieldKit.create("arppu",gameData.getArppu());
		array[i++]=FieldKit.create("total_user",gameData.getTotal_user());
		array[i++]=FieldKit.create("charge_total_user",gameData
			.getCharge_total_user());
		array[i++]=FieldKit.create("charge_rate",gameData.getCharge_rate());
		array[i++]=FieldKit
			.create("total_charge",gameData.getTotal_charge());
		array[i++]=FieldKit.create("the_year",gameData.getThe_year());
		array[i++]=FieldKit.create("the_month",gameData.getThe_month());
		array[i++]=FieldKit.create("the_day",gameData.getThe_day());
		
		array[i++]=FieldKit.create("plat",gameData.getPlat());
		array[i++]=FieldKit.create("login_count",gameData.getLoginCount());
		array[i++]=FieldKit.create("online_time",gameData.getOnlineTime());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
}
