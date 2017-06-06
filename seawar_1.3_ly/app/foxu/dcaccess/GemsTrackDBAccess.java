package foxu.dcaccess;

import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import foxu.sea.gems.GemsTrack;
import shelby.dc.GameDBAccess;

/** 宝石消费日志记录 */
public class GemsTrackDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(NpcIslandGameDBAccess.class);

	public GemsTrack load(String id)
	{
		// 构造一个空域（包括了NpcIsland表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"GemsTrackDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		GemsTrack gemsTrack=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return gemsTrack;
	}

	public GemsTrack[] loadBySql(String sql)
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
		GemsTrack[] gameData=new GemsTrack[array.length];
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
				"GemsTrackDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[10];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("type",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("gems",0);
		array[i++]=FieldKit.create("nowGems",0);
		array[i++]=FieldKit.create("createAt",0);
		array[i++]=FieldKit.create("item_id",0);
		array[i++]=FieldKit.create("year",0);
		array[i++]=FieldKit.create("month",0);
		array[i++]=FieldKit.create("day",0);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public GemsTrack mapping(Fields fields)
	{
		GemsTrack gemsTrack=new GemsTrack();
		int id=((IntField)fields.get("id")).value;
		gemsTrack.setId(id);
		int type=((IntField)fields.get("type")).value;
		gemsTrack.setType(type);
		int playerId=((IntField)fields.get("playerId")).value;
		gemsTrack.setPlayerId(playerId);
		int gems=((IntField)fields.get("gems")).value;
		gemsTrack.setGems(gems);
		int createAt=((IntField)fields.get("createAt")).value;
		gemsTrack.setCreateAt(createAt);
		int item_id=((IntField)fields.get("item_id")).value;
		gemsTrack.setItem_id(item_id);
		int year=((IntField)fields.get("year")).value;
		gemsTrack.setYear(year);
		int month=((IntField)fields.get("month")).value;
		gemsTrack.setMonth(month);
		int day=((IntField)fields.get("day")).value;
		gemsTrack.setDay(day);
		long nowGems=((LongField)fields.get("nowGems")).value;
		gemsTrack.setNowGems(nowGems);
		return gemsTrack;
	}

	/** 保存方法 使用包含玩家数据的字节数组初始化指定姓名的玩家并保存 */
	public boolean save(int id,Object island)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+island.toString());
		try
		{
			// int offset=data.offset();
			// Message message=new Message();
			// message.bytesRead(data);
			// data.setOffset(offset);
			return save(island);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object gemsTrack)
	{
		if(gemsTrack==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",
			((GemsTrack)gemsTrack).getId()),mapping(gemsTrack));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+gemsTrack);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object m)
	{
		GemsTrack gemsTrack=(GemsTrack)m;
		FieldObject[] array=new FieldObject[10];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",gemsTrack.getId());
		array[i++]=FieldKit.create("type",gemsTrack.getType());
		array[i++]=FieldKit.create("playerId",gemsTrack.getPlayerId());
		array[i++]=FieldKit.create("gems",gemsTrack.getGems());
		array[i++]=FieldKit.create("createAt",gemsTrack.getCreateAt());
		array[i++]=FieldKit.create("item_id",gemsTrack.getItem_id());
		array[i++]=FieldKit.create("year",gemsTrack.getYear());
		array[i++]=FieldKit.create("month",gemsTrack.getMonth());
		array[i++]=FieldKit.create("day",gemsTrack.getDay());
		array[i++]=FieldKit.create("nowGems",gemsTrack.getNowGems());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
