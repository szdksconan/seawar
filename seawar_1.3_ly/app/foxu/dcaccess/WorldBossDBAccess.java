package foxu.dcaccess;

import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import shelby.dc.GameDBAccess;
import foxu.sea.worldboss.WorldBoss;

/**
 * 战斗事件加载器 author:icetiger
 */
public class WorldBossDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(WorldBossDBAccess.class);

	public WorldBoss load(String id)
	{
		// 构造一个空域（包括了player表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"WorldBossDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		WorldBoss worldboss=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// fightEvent.bytesWrite(bb);
		return worldboss;
	}

	public WorldBoss[] loadBySql(String sql)
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
				"WorldBossDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		WorldBoss[] worldboss=new WorldBoss[array.length];
		for(int i=0;i<array.length;i++)
		{
			worldboss[i]=mapping(array[i]);
		}
		return worldboss;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[9];
		int i=0;
		// array[i++]=FieldKit.create("immure",0);
		// array[i++]=FieldKit.create("cause",(String)null);
		// array[i++]=FieldKit.create("sid",0);
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("lastTime",0);
		array[i++]=FieldKit.create("protectTime",0);
		array[i++]=FieldKit.create("createTime",0);
		array[i++]=FieldKit.create("awardSid",0);
		array[i++]=FieldKit.create("sid",0);
		array[i++]=FieldKit.create("index",0);
//		array[i++]=FieldKit.create("killAwardSid",0);
		

		array[i++]=FieldKit.create("hurtList",(byte[])null);
		array[i++]=FieldKit.create("fleetGroup",(byte[])null);

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public WorldBoss mapping(Fields fields)
	{
		// FieldObject f=fields.get("immure");
		// if(f==null)
		// throw new DataAccessException(
		// DataAccessException.SERVER_INTERNAL_ERROR,"db not immure");
		// int immure=((IntField)f).value;
		// f=fields.get("cause");
		// if(f==null)
		// throw new DataAccessException(
		// DataAccessException.SERVER_INTERNAL_ERROR,"db not cause");
		// String cause=((StringField)f).value;
		// int time=(int)(System.currentTimeMillis()/1000);
		// if(immure>time)
		// throw new DataAccessException(
		// DataAccessException.SERVER_ACCESS_REFUSED,cause);

		// int sampleId=((IntField)fields.get("sid")).value;
		/** TODO */
		// Player p=(Player)Role.factory.newSample(sampleId);
		int sid=((IntField)fields.get("sid")).value;
		WorldBoss worldBoss=(WorldBoss)WorldBoss.factory.newSample(sid);
		int lastTime=((IntField)fields.get("lastTime")).value;
		worldBoss.setLastTime(lastTime);
		int protectTime=((IntField)fields.get("protectTime")).value;
		worldBoss.setProtectTime(protectTime);
		int createTime=((IntField)fields.get("createTime")).value;
		worldBoss.setCreateTime(createTime);
		int awardSid=((IntField)fields.get("awardSid")).value;
		worldBoss.setServiceSid(awardSid);
//		int killAwardSid=((IntField)fields.get("killAwardSid")).value;
//		worldBoss.setKillAwardSid(killAwardSid);
		int index=((IntField)fields.get("index")).value;
		worldBoss.setIndex(index);

		byte[] array=((ByteArrayField)fields.get("hurtList")).value;
		if(array!=null&&array.length>0)
			worldBoss.bytesReadHurtList(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("fleetGroup")).value;
		if(array!=null&&array.length>0)
			worldBoss.bytesReadFleetGroup(new ByteBuffer(array));

		return worldBoss;
	}

	/** 保存方法 使用包含玩家数据的字节数组初始化指定姓名的玩家并保存 */
	public boolean save(int id,Object worldBoss)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+worldBoss.toString());
		try
		{
			// int offset=data.offset();
			// FightEvent fightEvent=new FightEvent();
			// fightEvent.bytesRead(data);
			// data.setOffset(offset);
			return save(worldBoss);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object worldBoss)
	{
		if(worldBoss==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",
			((WorldBoss)worldBoss).getSid()),mapping(worldBoss));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+worldBoss);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 删除方法 */
	public void delete(Object worldBoss)
	{
		if(worldBoss==null) return;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.delete(FieldKit.create("id",
			((WorldBoss)worldBoss).getSid()));
		if(log.isInfoEnabled()) log.info("delete, "+t+" "+worldBoss);
	}

	/** 是否该删除 */
	public boolean isDelete(Object event)
	{
		return false;
	}

	/** 映射成域对象 */
	public Fields mapping(Object f)
	{
		WorldBoss worldBoss=(WorldBoss)f;
		FieldObject[] array=new FieldObject[9];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",worldBoss.getSid());
		array[i++]=FieldKit.create("lastTime",worldBoss.getLastTime());
		array[i++]=FieldKit.create("protectTime",worldBoss.getProtectTime());
		array[i++]=FieldKit.create("createTime",worldBoss.getCreateTime());
		array[i++]=FieldKit.create("awardSid",worldBoss.getServiceSid());
		array[i++]=FieldKit.create("sid",worldBoss.getSid());
		array[i++]=FieldKit.create("index",worldBoss.getIndex());
//		array[i++]=FieldKit.create("killAwardSid",worldBoss.getKillAwardSid());

		ByteBuffer bb=new ByteBuffer();

		bb.clear();
		worldBoss.bytesWriteHurtList(bb);
		array[i++]=FieldKit.create("hurtList",bb.toArray());

		bb.clear();
		worldBoss.bytesWirteFleetGroup(bb);
		array[i++]=FieldKit.create("fleetGroup",bb.toArray());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
}
