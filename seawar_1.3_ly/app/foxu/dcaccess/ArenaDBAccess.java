package foxu.dcaccess;

import foxu.sea.arena.SeawarGladiator;
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
import mustang.text.TextKit;
import shelby.dc.GameDBAccess;

/**
 * 竞技场存储类
 * @author comeback
 *
 */
public class ArenaDBAccess extends GameDBAccess
{
	private static Logger log=LogFactory.getLogger(ArenaDBAccess.class);

	@Override
	public Object load(String id)
	{
		// TODO Auto-generated method stub
		return super.load(id);
	}

	@Override
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[9];
		int i=0;
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("awardSid",0);
		array[i++]=FieldKit.create("win",0);
		array[i++]=FieldKit.create("lose",0);
		array[i++]=FieldKit.create("ships",(String)null);
		array[i++]=FieldKit.create("ranking",0);
		array[i++]=FieldKit.create("yesterday",0);
		array[i++]=FieldKit.create("lastBattleTime",0);
		array[i++]=FieldKit.create("todayBattleCount",0);
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	@Override
	public Object mapping(Fields fields)
	{
		SeawarGladiator sg=new SeawarGladiator();
		int playerId=((IntField)fields.get("playerId")).value;
		sg.setPlayerId(playerId);
		int awardSid=((IntField)fields.get("awardSid")).value;
		sg.setAwardSid(awardSid);
		int win=((IntField)fields.get("win")).value;
		sg.setWin(win);
		int lose=((IntField)fields.get("lose")).value;
		sg.setLose(lose);
		int ranking=((IntField)fields.get("ranking")).value;
		sg.setRanking(ranking);
		String shipsStr=((StringField)fields.get("ships")).value;
		if(shipsStr!=null&&shipsStr.length()>0)
		{
			int[] ships=TextKit.parseIntArray(TextKit.split(shipsStr,":"));
			for(int i=0;i<ships.length;i+=3)
			{
				sg.setShipSid(ships[i],ships[i+1]);
				sg.setShipCount(ships[i],ships[i+2]);
			}
		}
		int yesterday=((IntField)fields.get("yesterday")).value;
		sg.setLastDayRanking(yesterday);
		int lastBattleTime=((IntField)fields.get("lastBattleTime")).value;
		sg.setLastBattleTime(lastBattleTime);
		int todayBattleCount=((IntField)fields.get("todayBattleCount")).value;
		sg.setTodayBattleCount(todayBattleCount);
		return sg;
	}

	@Override
	public Fields mapping(Object p)
	{
		SeawarGladiator sg=(SeawarGladiator)p;
		FieldObject[] array=new FieldObject[9];
		int i=0;
		array[i++]=FieldKit.create("playerId",sg.getPlayerId());
		array[i++]=FieldKit.create("awardSid",sg.getAwardSid());
		array[i++]=FieldKit.create("win",sg.getWin());
		array[i++]=FieldKit.create("lose",sg.getLose());
		array[i++]=FieldKit.create("ranking",sg.getRanking());
		String ships="";
		for(int k=0;k<SeawarGladiator.FLEET_MAX_COUNT;k++)
		{
			int sid=sg.getShipSidByIndex(k);
			int count=sg.getShipCountByIndex(k);
			if(sid>0&&count>0)
			{
				if(ships.length()>0)
					ships+=":";
				ships+=k+":"+sid+":"+count;
			}
		}
		array[i++]=FieldKit.create("ships",ships);
		array[i++]=FieldKit.create("yesterday",sg.getLastDayRanking());
		array[i++]=FieldKit.create("lastBattleTime",sg.getLastBattleTime());
		array[i++]=FieldKit.create("todayBattleCount",sg.getTodayBattleCount());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	@Override
	public boolean save(Object object)
	{
		if(object==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("playerId",((SeawarGladiator)object)
			.getPlayerId()),mapping(object));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+object);
		return t==Persistence.OK||t==Persistence.ADD;
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
				"AllianceDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
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
				"AllianceDBAccess loadBysql valid, db error");
		}
		return array;
	}
	
	public SeawarGladiator[] loadBySql(String sql)
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
				"AllianceDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		SeawarGladiator[] gladiators=new SeawarGladiator[array.length];
		for(int i=0;i<array.length;i++)
		{
			gladiators[i]=(SeawarGladiator)mapping(array[i]);
		}
		return gladiators;
	}
}
