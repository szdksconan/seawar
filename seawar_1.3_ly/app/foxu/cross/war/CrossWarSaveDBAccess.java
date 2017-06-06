package foxu.cross.war;

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


/**
 * 战报数据库 操作中心
 * @author yw
 *
 */
public class CrossWarSaveDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(CrossWarSaveDBAccess.class);
	
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
				"CrossWarSaveDBAccess excuteSql valid, db error");
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
				"CrossWarSaveDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}

	public CrossWarRoundSave[] loadBySql(String sql)
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
				"CrossWarSaveDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		CrossWarRoundSave[] crs=new CrossWarRoundSave[array.length];
		for(int i=0;i<array.length;i++)
		{
			crs[i]=mapping(array[i]);
		}
		return crs;
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
				"CrossWarPlayerDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public CrossWarRoundSave mapping(Fields fields)
	{
		CrossWarRoundSave save=new CrossWarRoundSave();
		int id=((IntField)fields.get("id")).value;
		save.setId(id);
		int type=((IntField)fields.get("type")).value;
		save.setType(type);
		int warid=((IntField)fields.get("warid")).value;
		save.setWarid(warid);
		int attackid=((IntField)fields.get("attackid")).value;
		save.setAttackid(attackid);
		int defenceid=((IntField)fields.get("defenceid")).value;
		save.setDefenceid(defenceid);
		int winid=((IntField)fields.get("winid")).value;
		save.setWinid(winid);
		int attackpid=((IntField)fields.get("attackpid")).value;
		save.setAttackpid(attackpid);
		String attackname=((StringField)fields.get("attackname")).value;
		save.setAttackname(attackname);
		String aservername=((StringField)fields.get("aservername")).value;
		save.setAservername(aservername);
		String attackip=((StringField)fields.get("attackip")).value;
		save.setAttackip(attackip);
		String anational=((StringField)fields.get("anational")).value;
		save.setAnational(anational);
		int attacklv=((IntField)fields.get("attacklv")).value;
		save.setAttacklv(attacklv);
		int defencepid=((IntField)fields.get("defencepid")).value;
		save.setDefencepid(defencepid);
		String defencename=((StringField)fields.get("defencename")).value;
		save.setDefencename(defencename);
		String dservername=((StringField)fields.get("dservername")).value;
		save.setDservername(dservername);
		String defenceip=((StringField)fields.get("defenceip")).value;
		save.setDefenceip(defenceip);
		String dnational=((StringField)fields.get("dnational")).value;
		save.setDnational(dnational);
		int defencelv=((IntField)fields.get("defencelv")).value;
		save.setDefencelv(defencelv);
		int createtime=((IntField)fields.get("createtime")).value;
		save.setCreatetime(createtime);

		byte[] array=((ByteArrayField)fields.get("s1")).value;
		if(array!=null&&array.length>0)
			save.bytesReadS1(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("s2")).value;
		if(array!=null&&array.length>0)
			save.bytesReadS2(new ByteBuffer(array));

		return save;
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object save)
	{
		if(save==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(
			FieldKit.create("id",((CrossWarRoundSave)save).getId()),
			mapping(save));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+save);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object sv)
	{
		CrossWarRoundSave save=(CrossWarRoundSave)sv;
		FieldObject[] array=new FieldObject[23];
		int i=0;
//		array[i++]=FieldKit.create("id",save.getId());
		array[i++]=FieldKit.create("type",save.getType());
		array[i++]=FieldKit.create("warid",save.getWarid());
		array[i++]=FieldKit.create("attackid",save.getAttackid());
		array[i++]=FieldKit.create("defenceid",save.getDefenceid());
		array[i++]=FieldKit.create("winid",save.getWinid());
		array[i++]=FieldKit.create("attackpid",save.getAttackpid());
		array[i++]=FieldKit.create("attackname",save.getAttackname());
		array[i++]=FieldKit.create("aservername",save.getAservername());
		array[i++]=FieldKit.create("attackip",save.getAttackip());
		array[i++]=FieldKit.create("anational",save.getAnational());
		array[i++]=FieldKit.create("attacklv",save.getAttacklv());
		array[i++]=FieldKit.create("defencepid",save.getDefencepid());
		array[i++]=FieldKit.create("defencename",save.getDefencename());
		array[i++]=FieldKit.create("dservername",save.getDservername());
		array[i++]=FieldKit.create("defenceip",save.getDefenceip());
		array[i++]=FieldKit.create("dnational",save.getDnational());
		array[i++]=FieldKit.create("defencelv",save.getDefencelv());
		array[i++]=FieldKit.create("createtime",save.getCreatetime());

		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		save.bytesWriteS1(bb);
		array[i++]=FieldKit.create("s1",bb.toArray());

		bb.clear();
		save.bytesWriteS2(bb);
		array[i++]=FieldKit.create("s2",bb.toArray());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}


}
