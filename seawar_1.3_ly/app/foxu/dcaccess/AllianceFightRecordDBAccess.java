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
import foxu.sea.alliance.alliancebattle.AllianceFightRecordTrack;

/***
 * 联盟战日志操作中心
 * 
 * @author lhj
 * 
 */
public class AllianceFightRecordDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(AllianceFightRecordDBAccess.class);

	public AllianceFightRecordTrack load(String id)
	{
		// 构造一个空域（包括了NpcIsland表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"AllianceFightRecordTrack db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		AllianceFightRecordTrack track=mapping(fields);
		return track;
	}

	public AllianceFightRecordTrack[] loadBySql(String sql)
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
				"AllianceFightRecordTrack loadBysql valid, db error");
		}
		if(array==null) return null;
		AllianceFightRecordTrack[] gameData=new AllianceFightRecordTrack[array.length];
		for(int i=0;i<array.length;i++)
		{
			gameData[i]=mapping(array[i]);
		}
		return gameData;
	}

	public Fields loadSql(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields array=null;
		try
		{
			array=SqlKit.query(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"AllianceFightRecordTrack loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[15];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("rankvalue",(String)null);
		array[i++]=FieldKit.create("players",(String)null);
		array[i++]=FieldKit.create("type",0);
		array[i++]=FieldKit.create("battleisland",0);
		array[i++]=FieldKit.create("num",0);
		array[i++]=FieldKit.create("createAt",0);
		array[i++]=FieldKit.create("year",0);
		array[i++]=FieldKit.create("month",0);
		array[i++]=FieldKit.create("day",0);
		array[i++]=FieldKit.create("stage",0);
		array[i++]=FieldKit.create("state",0);
		array[i++]=FieldKit.create("stime",0);
		array[i++]=FieldKit.create("etime",0);
		array[i++]=FieldKit.create("allianceid",0);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public AllianceFightRecordTrack mapping(Fields fields)
	{
		AllianceFightRecordTrack track=new AllianceFightRecordTrack();
		int id=((IntField)fields.get("id")).value;
		track.setId(id);
		int battleIsland=((IntField)fields.get("battleisland")).value;
		track.setBattleIsland(battleIsland);
		int num=((IntField)fields.get("num")).value;
		track.setNum(num);
		int createAt=((IntField)fields.get("createAt")).value;
		track.setCreateAt(createAt);
		int year=((IntField)fields.get("year")).value;
		track.setYear(year);
		int month=((IntField)fields.get("month")).value;
		track.setMonth(month);
		int day=((IntField)fields.get("day")).value;
		track.setDay(day);
		int stage=((IntField)fields.get("stage")).value;
		track.setStage(stage);
		int state=((IntField)fields.get("state")).value;
		track.setState(state);
		int type=((IntField)fields.get("type")).value;
		track.setType(type);
		int stime=((IntField)fields.get("stime")).value;
		track.setStime(stime);
		int etime=((IntField)fields.get("etime")).value;
		track.setEtime(etime);
		int allianceId=((IntField)fields.get("allianceId")).value;
		track.setAllianceId(allianceId);
		String players=((StringField)fields.get("players")).value;
		track.setPlayers(players);
		String  rankvalue=((StringField)fields.get("rankvalue")).value;
		track.setRankvalue(rankvalue);
		return track;
	}

	/** 保存方法 使用包含玩家数据的字节数组初始化指定姓名的玩家并保存 */
	public boolean save(int id,Object track)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+track.toString());
		try
		{
			return save(track);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object track)
	{
		if(track==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(
			FieldKit.create("id",((AllianceFightRecordTrack)track).getId()),
			mapping(track));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+track);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object m)
	{
		AllianceFightRecordTrack track=(AllianceFightRecordTrack)m;
		FieldObject[] array=new FieldObject[15];
		int i=0;
		array[i++]=FieldKit.create("id",track.getId());
		array[i++]=FieldKit.create("rankvalue",track.getRankvalue());

		array[i++]=FieldKit.create("type",track.getType());
		array[i++]=FieldKit.create("battleisland",track.getBattleIsland());
		array[i++]=FieldKit.create("num",track.getNum());
		array[i++]=FieldKit.create("createAt",track.getCreateAt());
		array[i++]=FieldKit.create("year",track.getYear());
		array[i++]=FieldKit.create("month",track.getMonth());
		array[i++]=FieldKit.create("day",track.getDay());
		array[i++]=FieldKit.create("stage",track.getStage());
		array[i++]=FieldKit.create("state",track.getState());
		array[i++]=FieldKit.create("stime",track.getStime());
		array[i++]=FieldKit.create("etime",track.getEtime());
		array[i++]=FieldKit.create("allianceId",track.getAllianceId());
		array[i++]=FieldKit.create("players",track.getPlayers());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
