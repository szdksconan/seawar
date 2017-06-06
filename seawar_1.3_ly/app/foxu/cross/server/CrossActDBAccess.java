package foxu.cross.server;

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
import foxu.cross.war.CrossWar;
import foxu.cross.war.CrossWarPlayerDBAccess;
import shelby.dc.GameDBAccess;

/**
 * 跨服活动保存信息  sql中心
 * @author yw
 *
 */
public class CrossActDBAccess extends GameDBAccess
{
	
	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(CrossWarPlayerDBAccess.class);
	
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
				"CrossActDBAccess excuteSql valid, db error");
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
				"CrossActDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}

	public CrossAct[] loadBySql(String sql)
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
				"CrossActDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		CrossAct[] acts=new CrossAct[array.length];
		for(int i=0;i<array.length;i++)
		{
			acts[i]=mapping(array[i]);
		}
		return acts;
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
				"CrossActDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public CrossAct mapping(Fields fields)
	{
		int sid=((IntField)fields.get("sid")).value;
		CrossAct act=createAct(sid);
		act.setSid(sid);
		int id=((IntField)fields.get("id")).value;
		act.setId(id);
		int stime=((IntField)fields.get("stime")).value;
		act.setStime(stime);
		int etime=((IntField)fields.get("etime")).value;
		act.setEtime(etime);
		boolean forceover=((IntField)fields.get("forceover")).value==1;
		act.setForceover(forceover);
		byte[] bb=((ByteArrayField)fields.get("data")).value;
		if(bb!=null&&bb.length>0)
		{
			ByteBuffer data=new ByteBuffer(bb);
			act.readData(data);
		}

		return act;
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object act)
	{
		if(act==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(
			FieldKit.create("id",((CrossAct)act).getId()),mapping(act));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+act);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object act)
	{
		CrossAct a=(CrossAct)act;
		FieldObject[] array=new FieldObject[6];
		int i=0;
		array[i++]=FieldKit.create("id",a.getId());
		array[i++]=FieldKit.create("sid",a.getSid());
		array[i++]=FieldKit.create("stime",a.getStime());
		array[i++]=FieldKit.create("etime",a.getEtime());
		array[i++]=FieldKit.create("forceover",a.isForceover());
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		a.writeData(bb);
		array[i++]=FieldKit.create("data",bb.toArray());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 创建活动 */
	public CrossAct createAct(int sid)
	{
		CrossAct act=null;
		if(sid==CrossAct.WAR_SID)
		{
			act=new CrossWar();
		}
		else
		{
			act=new CrossAct();
		}
		return act;
	}
}
