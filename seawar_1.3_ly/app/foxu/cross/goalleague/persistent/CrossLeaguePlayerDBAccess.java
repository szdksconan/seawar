package foxu.cross.goalleague.persistent;

import shelby.dc.GameDBAccess;
import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import foxu.cross.goalleague.LeaguePlayer;


/**
 * 跨服积分玩家数据管理
 * @author Alan
 *
 */
public class CrossLeaguePlayerDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(CrossLeaguePlayerDBAccess.class);
	
	public void excuteSql(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		try
		{
			SqlKit.execute(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"CrossLeaguePlayerDBAccess excuteSql valid, db error");
		}
	}
	
	public Fields[] loadSqls(String sql)
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
				"CrossLeaguePlayerDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}

	public LeaguePlayer[] loadBySqlAll()
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		String sql="select * from "+sp.getTable();
		return loadBySql(sql);
	}
	
	public LeaguePlayer[] loadBySql(String sql)
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
				"CrossLeaguePlayerDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		LeaguePlayer[] players=new LeaguePlayer[array.length];
		for(int i=0;i<array.length;i++)
		{
			players[i]=mapping(array[i]);
		}
		return players;
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
				"CrossLeaguePlayerDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public LeaguePlayer mapping(Fields fields)
	{
		LeaguePlayer player=new LeaguePlayer();
		int crossid=((IntField)fields.get("crossid")).value;
		player.setCrossid(crossid);
		int id=((IntField)fields.get("id")).value;
		player.setId(id);
		String name=((StringField)fields.get("name")).value;
		player.setName(name);
		int warid=((IntField)fields.get("warid")).value;
		player.setWarid(warid);
		int rank=((IntField)fields.get("rank")).value;
		player.setRank(rank);
		int bet=((IntField)fields.get("bet")).value;
		player.setBet(bet);
		int goal=((IntField)fields.get("goal")).value;
		player.setGoal(goal);
		int num=((IntField)fields.get("num")).value;
		player.setNum(num);
		int platid=((IntField)fields.get("platid")).value;
		player.setPlatid(platid);
		int areaid=((IntField)fields.get("areaid")).value;
		player.setAreaid(areaid);
		int severid=((IntField)fields.get("serverid")).value;
		player.setSeverid(severid);
		String sname=((StringField)fields.get("sname")).value;
		player.setSeverName(sname);
		String aname=((StringField)fields.get("aname")).value;
		player.setAname(aname);
		String national=((StringField)fields.get("national")).value;
		player.setNational(national);
		int sid=((IntField)fields.get("sid")).value;
		player.setSid(sid);
		int level=((IntField)fields.get("level")).value;
		player.setLevel(level);
		int fightscore=((IntField)fields.get("fscore")).value;
		player.setFightscore(fightscore);
		int jiontime=((IntField)fields.get("jiontime")).value;
		player.setJiontime(jiontime);
		byte[] array=((ByteArrayField)fields.get("adjustment")).value;
		if(array!=null&&array.length>0)
			player.bytesReadAdjustment(new ByteBuffer(array));
//		array=((ByteArrayField)fields.get("equiplist")).value;
//		if(array!=null&&array.length>0)
//			player.bytesReadEquiplist(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("shiplv")).value;
		if(array!=null&&array.length>0)
			player.bytesReadShiplevel(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("attacklist")).value;
		if(array!=null&&array.length>0)
			player.bytesReadAttacklist(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("defencelist")).value;
		if(array!=null&&array.length>0)
			player.bytesReadDefencelist(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("officer")).value;
		if(array!=null&&array.length>0)
			player.bytesReadOFS(new ByteBuffer(array));
		// LeaguePlayer
		array=((ByteArrayField)fields.get("currentList")).value;
		if(array!=null&&array.length>0)
			player.bytesReadCurrentList(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("challengeList")).value;
		if(array!=null&&array.length>0)
			player.bytesReadRecordList(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("propList")).value;
		if(array!=null&&array.length>0)
			player.bytesReadPropList(new ByteBuffer(array));
		
		int lastActiveTime=((IntField)fields.get("lastActiveTime")).value;
		player.setLastActiveTime(lastActiveTime);
		int currentFlushCount=((IntField)fields.get("currentFlushCount")).value;
		player.setCurrentFlushCount(currentFlushCount);
		int currentBattleCount=((IntField)fields.get("currentBattleCount")).value;
		player.setCurrentBattleCount(currentBattleCount);
		int currentBattleLimit=((IntField)fields.get("currentBattleLimit")).value;
		player.setCurrentBattleLimit(currentBattleLimit);
		int leagueId=((IntField)fields.get("leagueId")).value;
		player.setLeagueId(leagueId);
		// 初始化时不需要即时保存
		player.setSave(false);
		return player;
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object player)
	{
		if(player==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(
			FieldKit.create("id",((LeaguePlayer)player).getId()),
			mapping(player));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+player);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object player)
	{
		LeaguePlayer p=(LeaguePlayer)player;
		FieldObject[] array=new FieldObject[32];
		int i=0;
		//array[i++]=FieldKit.create("crossid",p.getCrossid());
		array[i++]=FieldKit.create("id",p.getId());
		array[i++]=FieldKit.create("name",p.getName());
		array[i++]=FieldKit.create("warid",p.getWarid());
		array[i++]=FieldKit.create("rank",p.getRank());
		array[i++]=FieldKit.create("bet",p.getBet());
		array[i++]=FieldKit.create("goal",p.getGoal());
		array[i++]=FieldKit.create("num",p.getNum());
		array[i++]=FieldKit.create("platid",p.getPlatid());
		array[i++]=FieldKit.create("areaid",p.getAreaid());
		array[i++]=FieldKit.create("serverid",p.getSeverid());
		array[i++]=FieldKit.create("sname",p.getSeverName());
		array[i++]=FieldKit.create("aname",p.getAname());
		array[i++]=FieldKit.create("national",p.getNational());
		array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("level",p.getLevel());
		array[i++]=FieldKit.create("fscore",p.getFightscore());
		array[i++]=FieldKit.create("jiontime",p.getJiontime());
		
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		p.bytesWriteAdjustment(bb);
		array[i++]=FieldKit.create("adjustment",bb.toArray());
		
//		bb.clear();
//		p.bytesWriteEquiplist(bb);
//		array[i++]=FieldKit.create("equiplist",bb.toArray());
		
		bb.clear();
		p.bytesWriteShiplevel(bb);
		array[i++]=FieldKit.create("shiplv",bb.toArray());
		
		bb.clear();
		p.bytesWriteAttacklist(bb);
		array[i++]=FieldKit.create("attacklist",bb.toArray());
		
		bb.clear();
		p.bytesWriteDefencelist(bb);
		array[i++]=FieldKit.create("defencelist",bb.toArray());
		
		bb.clear();
		p.bytesWriteOFS(bb);
		array[i++]=FieldKit.create("officer",bb.toArray());
		
		// LeaguePlayer
		bb.clear();
		p.bytesWriteCurrentList(bb);
		array[i++]=FieldKit.create("currentList",bb.toArray());
		
		bb.clear();
		p.bytesWriteRecordList(bb);
		array[i++]=FieldKit.create("challengeList",bb.toArray());
		
		bb.clear();
		p.bytesWritePropList(bb);
		array[i++]=FieldKit.create("propList",bb.toArray());
		
		array[i++]=FieldKit.create("lastActiveTime",p.getLastActiveTime());
		array[i++]=FieldKit.create("currentFlushCount",p.getCurrentFlushCount());
		array[i++]=FieldKit.create("currentBattleCount",p.getCurrentBattleCount());
		array[i++]=FieldKit.create("currentBattleLimit",p.getCurrentBattleLimit());
		array[i++]=FieldKit.create("leagueId",p.getLeagueId());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
