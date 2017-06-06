package foxu.dcaccess;

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
import foxu.sea.shipdata.ShipCheckData;
import shelby.dc.GameDBAccess;

/** 宝石消费日志记录 */
public class ShipDataDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(ShipDataDBAccess.class);

	public ShipCheckData load(String id)
	{
		// 构造一个空域（包括了NpcIsland表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"ShipDataDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		ShipCheckData gemsTrack=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return gemsTrack;
	}

	public ShipCheckData[] loadBySql(String sql)
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
				"ShipDataDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		ShipCheckData[] shipCheckData=new ShipCheckData[array.length];
		for(int i=0;i<array.length;i++)
		{
			shipCheckData[i]=mapping(array[i]);
		}
		return shipCheckData;
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
				"ShipDataDBAccess loadBysql valid, db error");
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
		array[i++]=FieldKit.create("createAt",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("attackPlayerName",(String)null);
		array[i++]=FieldKit.create("extra",(String)null);
		array[i++]=FieldKit.create("leftList",(byte[])null);
		array[i++]=FieldKit.create("eventList",(byte[])null);
		array[i++]=FieldKit.create("hurtList",(byte[])null);
		array[i++]=FieldKit.create("list",(byte[])null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public ShipCheckData mapping(Fields fields)
	{
		ShipCheckData shipCheckData=new ShipCheckData();
		int id=((IntField)fields.get("id")).value;
		shipCheckData.setId(id);
		int type=((IntField)fields.get("type")).value;
		shipCheckData.setType(type);
		int playerId=((IntField)fields.get("playerId")).value;
		shipCheckData.setPlayerId(playerId);
		int createAt=((IntField)fields.get("createAt")).value;
		shipCheckData.setCreateAt(createAt);
		String str=((StringField)fields.get("attackPlayerName")).value;
		shipCheckData.setAttackPlayerName(str);
		str = ((StringField)fields.get("extra")).value;
		shipCheckData.setExtra(str);

		byte[] array=((ByteArrayField)fields.get("leftList")).value;
		if(array!=null&&array.length>0)
			shipCheckData.bytesReadLeftList(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("eventList")).value;
		if(array!=null&&array.length>0)
			shipCheckData.bytesReadEventList(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("hurtList")).value;
		if(array!=null&&array.length>0)
			shipCheckData.bytesReadhurtList(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("list")).value;
		if(array!=null&&array.length>0)
			shipCheckData.bytesReadList(new ByteBuffer(array));
		return shipCheckData;
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
			((ShipCheckData)gemsTrack).getId()),mapping(gemsTrack));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+gemsTrack);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object m)
	{
		ShipCheckData shipCheckData=(ShipCheckData)m;
		FieldObject[] array=new FieldObject[10];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",shipCheckData.getId());
		array[i++]=FieldKit.create("type",shipCheckData.getType());
		array[i++]=FieldKit.create("playerId",shipCheckData.getPlayerId());
		array[i++]=FieldKit.create("createAt",shipCheckData.getCreateAt());
		
		String str=shipCheckData.getAttackPlayerName();
		if(str==null)
			array[i++]=FieldKit.create("attackPlayerName",(String)null);
		else
			array[i++]=FieldKit.create("attackPlayerName",str);
		
		str=shipCheckData.getExtra();
		if(str==null)
			array[i++]=FieldKit.create("extra",(String)null);
		else
			array[i++]=FieldKit.create("extra",str);
		
		
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		shipCheckData.bytesWriteLeftList(bb);
		array[i++]=FieldKit.create("leftList",bb.toArray());

		bb.clear();
		shipCheckData.bytesWriteEventList(bb);
		array[i++]=FieldKit.create("eventList",bb.toArray());
		
		bb.clear();
		shipCheckData.bytesWritehurtList(bb);
		array[i++]=FieldKit.create("hurtList",bb.toArray());
		
		bb.clear();
		shipCheckData.bytesWriteList(bb);
		array[i++]=FieldKit.create("list",bb.toArray());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
