package foxu.dcaccess;

import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.util.TimeKit;
import foxu.sea.PublicConst;
import foxu.sea.activity.Activity;
import shelby.dc.GameDBAccess;

/**
 * 活动数据库操作中心
 * @author yw
 *
 */
public class ActivityDBAccess extends GameDBAccess
{
	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(AllianceDBAccess.class);
	private static final int DAYS=60;

	/** 保存方法 返回是否操作成功 */
	public boolean save(Activity activity)
	{
		// 据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",activity
			.getId()),mapping(activity));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+activity);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[5];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("sid",0);
		array[i++]=FieldKit.create("stime",0);
		array[i++]=FieldKit.create("etime",0);
		array[i++]=FieldKit.create("initData",(byte[])null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public Fields mapping(Activity activity)
	{
		FieldObject[] array=new FieldObject[5];
		int i=0;
		array[i++]=FieldKit.create("id",activity.getId());
		array[i++]=FieldKit.create("sid",activity.getSid());
		array[i++]=FieldKit.create("stime",activity.getStartTime());
		array[i++]=FieldKit.create("etime",activity.getEndTime());
//		array[i++]=FieldKit.create("activeTime",activity.getActiveTime());
		if(activity.getInitData()!=null)
		{
			array[i++]=FieldKit.create("initData",activity.getInitData()
				.toArray());
		}else
		{
			array[i++]=FieldKit.create("initData",new byte[0]);
		}
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
	/** 映射成域对象 */
	public Activity mapping(Fields fields,CreatObjectFactory factory)
	{
		int sid=((IntField)fields.get("sid")).value;
		Activity activity=(Activity)Activity.factory.newSample(sid);
		if(activity==null)return null;
		activity.setStartTime(((IntField)fields.get("stime")).value);
		activity.setEndTime(((IntField)fields.get("etime")).value);
		activity.setId(((IntField)fields.get("id")).value);
		byte[] array=((ByteArrayField)fields.get("initData")).value;
		if(array!=null&&array.length>0)
			activity.initData(new ByteBuffer(array),factory,true);
		return activity;
	}

	public Fields[] initData()
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields[] array=null;
		try
		{
			SqlKit.execute(
				sp.getConnectionManager(),
				"delete from activity where etime>0 and etime<="
					+(TimeKit.getSecondTime()-DAYS*PublicConst.DAY_SEC));
			array=SqlKit.querys(
				sp.getConnectionManager(),
				"select * from activity where etime>"
					+TimeKit.getSecondTime()+" or etime=0");
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"ActivityDBAccess loadBysql valid, db error");
		}
		return array;
	}
	
	/** 获取活动by Id */
	public Activity getActById(int id,CreatObjectFactory factory)
	{
		String sql="select * from activity where id="+id;
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields fields=SqlKit.query(sp.getConnectionManager(),sql);
		if(fields==null)return null;
		return mapping(fields,factory);
	}

	/** 获取活动数量by Sid */
	public int getCountBySid(int sid)
	{
		String sql="select count(1) as num from activity where sid="+sid;
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields fields=SqlKit.query(sp.getConnectionManager(),sql);
		if(fields==null)return 0;
		int num=(int)((LongField)fields.get("num")).value;
		return num;
	}
}
