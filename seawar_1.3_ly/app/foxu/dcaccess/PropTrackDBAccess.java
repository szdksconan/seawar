package foxu.dcaccess;

import foxu.sea.proplist.PropTrack;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import shelby.dc.GameDBAccess;

/**
 * 生产物品记录
 * @author Alan
 *
 */
public class PropTrackDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(PropTrackDBAccess.class);

	public PropTrack load(String id)
	{
		// 构造一个空域（包括了NpcIsland表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"PropTrackDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		PropTrack track=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return track;
	}

	public PropTrack[] loadBySql(String sql)
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
				"PropTrackDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		PropTrack[] gameData=new PropTrack[array.length];
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
				"PropTrackDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[7];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("type",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("propSid",0);
		array[i++]=FieldKit.create("createAt",0);
		array[i++]=FieldKit.create("invokeNum",0);
		array[i++]=FieldKit.create("leftNum",0);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public PropTrack mapping(Fields fields)
	{
		PropTrack track=new PropTrack();
		int id=((IntField)fields.get("id")).value;
		track.setId(id);
		int type=((IntField)fields.get("type")).value;
		track.setType(type);
		int playerId=((IntField)fields.get("playerId")).value;
		track.setPlayerId(playerId);
		int propSid=((IntField)fields.get("propSid")).value;
		track.setPropSid(propSid);
		int createAt=((IntField)fields.get("createAt")).value;
		track.setCreateAt(createAt);
		int invokeNum=((IntField)fields.get("invokeNum")).value;
		track.setInvokeNum(invokeNum);
		int leftNum=((IntField)fields.get("leftNum")).value;
		track.setLeftNum(leftNum);
		return track;
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
	public boolean save(Object track)
	{
		if(track==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",
			((PropTrack)track).getId()),mapping(track));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+track);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object m)
	{
		PropTrack track=(PropTrack)m;
		FieldObject[] array=new FieldObject[7];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",track.getId());
		array[i++]=FieldKit.create("type",track.getType());
		array[i++]=FieldKit.create("playerId",track.getPlayerId());
		array[i++]=FieldKit.create("propSid",track.getPropSid());
		array[i++]=FieldKit.create("createAt",track.getCreateAt());
		array[i++]=FieldKit.create("invokeNum",track.getInvokeNum());
		array[i++]=FieldKit.create("leftNum",track.getLeftNum());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
