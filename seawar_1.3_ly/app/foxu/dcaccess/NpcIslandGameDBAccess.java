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
import foxu.sea.NpcIsland;

/**
 * 邮件加载器 author:icetiger
 */
public class NpcIslandGameDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(NpcIslandGameDBAccess.class);

	public NpcIsland load(String id)
	{
		// 构造一个空域（包括了NpcIsland表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"NpcIslandGameDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		NpcIsland island=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return island;
	}

	public NpcIsland[] loadBySql(String sql)
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
				"NpcIslandGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		NpcIsland[] island=new NpcIsland[array.length];
		for(int i=0;i<array.length;i++)
		{
			island[i]=mapping(array[i]);
		}
		return island;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[7];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("index",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("sid",0);
		array[i++]=FieldKit.create("tempAttackId",0);
		array[i++]=FieldKit.create("endTime",0);
		array[i++]=FieldKit.create("buff",(String)null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public NpcIsland mapping(Fields fields)
	{
		int sampleSid=((IntField)fields.get("sid")).value;
		NpcIsland island=(NpcIsland)NpcIsland.factory.newSample(sampleSid);
		int id=((IntField)fields.get("id")).value;
		island.setId(id);
		island.bindUid(id);
		int index=((IntField)fields.get("index")).value;
		island.setIndex(index);
		int playerId=((IntField)fields.get("playerId")).value;
		island.setPlayerId(playerId);
		int tempAttackId = ((IntField)fields.get("tempAttackId")).value;
		island.setTempAttackEventId(tempAttackId);
		int endTime=((IntField)fields.get("endTime")).value;
		island.setEndTime(endTime);
		String str=((StringField)fields.get("buff")).value;
		island.setBuff(str);
		return island;
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
	public boolean save(Object island)
	{
		if(island==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",((NpcIsland)island)
			.getId()),mapping(island));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+island);
		return t==Persistence.OK||t==Persistence.ADD;
	}


	/** 映射成域对象 */
	public Fields mapping(Object m)
	{
		NpcIsland island=(NpcIsland)m;
		FieldObject[] array=new FieldObject[7];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",island.getId());
		array[i++]=FieldKit.create("index",island.getIndex());
		array[i++]=FieldKit.create("playerId",island.getPlayerId());
		array[i++]=FieldKit.create("sid",island.getSid());
		array[i++]=FieldKit.create("tempAttackId",island.getTempAttackEventId());
		array[i++]=FieldKit.create("endTime",island.getEndTime());
		array[i++]=FieldKit.create("buff",island.getBuff());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
