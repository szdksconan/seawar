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
import mustang.util.TimeKit;
import foxu.sea.PasswordRecord;
import shelby.dc.GameDBAccess;

/**
 * 密码重置记录数据库操作中心
 * 
 * @author Alan
 * 
 */
public class PasswordDBAccess extends GameDBAccess
{

	/** 日志记录  */
	private static final Logger log=LogFactory
		.getLogger(PasswordDBAccess.class);

	int days=20;

	/** 保存方法 返回是否操作成功 */
	public boolean save(PasswordRecord pwdRecord)
	{
		// 据映射成域对象存入数据库中
		// int t=gamePersistence.set(
		// FieldKit.create("id",0),
		// mapping(pwdRecord));
		// if(log.isInfoEnabled()) log.info("save, "+t+" "+pwdRecord);
		// return t==Persistence.OK||t==Persistence.ADD;
		String sql="INSERT INTO pwdrecord (deviceId,userAccount,createTime) VALUE('"
			+pwdRecord.getDeviceId()
			+"','"
			+pwdRecord.getUserAccount()
			+"',"
			+pwdRecord.getCreatTime()+")";
		try
		{
			SqlPersistence sp=(SqlPersistence)getGamePersistence();
			SqlKit.execute(sp.getConnectionManager(),sql);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[3];
		int i=0;
		array[i++]=FieldKit.create("deviceId",(String)null);
		array[i++]=FieldKit.create("userAccount",(String)null);
		array[i++]=FieldKit.create("createTime",0);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public Fields mapping(PasswordRecord pwdRecord)
	{
		FieldObject[] array=new FieldObject[3];
		int i=0;
		array[i++]=FieldKit.create("deviceId",pwdRecord.getDeviceId());
		array[i++]=FieldKit.create("userAccount",pwdRecord.getUserAccount());
		array[i++]=FieldKit.create("createTime",pwdRecord.getCreatTime());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
	/** 映射成域对象 */
	public PasswordRecord mapping(Fields fields)
	{
		PasswordRecord pwdRecord=new PasswordRecord();
		pwdRecord.setDeviceId(((StringField)fields.get("deviceId")).value);
		pwdRecord
			.setUserAccount(((StringField)fields.get("userAccount")).value);
		pwdRecord.setCreatTime(((IntField)fields.get("createTime")).value);
		return pwdRecord;
	}

	public PasswordRecord[] loadBySql(String sql)
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
				"PasswordDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		PasswordRecord[] pwdRecords=new PasswordRecord[array.length];
		for(int i=0;i<array.length;i++)
		{
			pwdRecords[i]=mapping(array[i]);
		}
		return pwdRecords;
	}

	public void init()
	{
		String sql="delete from pwdrecord where createTime<"
			+(TimeKit.getSecondTime()-days*24*60*60);
		try
		{
			SqlPersistence sp=(SqlPersistence)getGamePersistence();
			SqlKit.execute(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"PasswordDBAccess init valid, db error");
		}
	}

}
