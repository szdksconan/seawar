package foxu.dcaccess;

import foxu.sea.bind.BindingTrack;
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

/** 绑定日志记录 */
public class BindingTrackDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(BindingTrackDBAccess.class);

	public BindingTrack load(String id)
	{
		// 构造一个空域（包括了NpcIsland表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"BindingTrackDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		BindingTrack gemsTrack=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return gemsTrack;
	}

	public BindingTrack[] loadBySql(String sql)
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
				"BindingTrackDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		BindingTrack[] gameData=new BindingTrack[array.length];
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
				"BindingTrackDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[12];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("bindType",0);
		array[i++]=FieldKit.create("trackType",0);
		array[i++]=FieldKit.create("actionType",0);
		array[i++]=FieldKit.create("uid",0);
		array[i++]=FieldKit.create("pid",0);
		array[i++]=FieldKit.create("time",0);
		array[i++]=FieldKit.create("operateInfo",(String)null);
		array[i++]=FieldKit.create("lastRecord",(String)null);
		array[i++]=FieldKit.create("currentRecord",(String)null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public BindingTrack mapping(Fields fields)
	{
		BindingTrack bindingTrack=new BindingTrack();
		int id=((IntField)fields.get("id")).value;
		bindingTrack.setId(id);
		int bindType=((IntField)fields.get("bindType")).value;
		bindingTrack.setBindType(bindType);
		int trackType=((IntField)fields.get("trackType")).value;
		bindingTrack.setTrackType(trackType);
		int actionType=((IntField)fields.get("actionType")).value;
		bindingTrack.setActionType(actionType);
		int uid=((IntField)fields.get("uid")).value;
		bindingTrack.setUid(uid);
		int pid=((IntField)fields.get("pid")).value;
		bindingTrack.setPid(pid);
		int time=((IntField)fields.get("time")).value;
		bindingTrack.setTime(time);
		String operateInfo=((StringField)fields.get("operateInfo")).value;
		if("".equals(operateInfo)) operateInfo=null;
		bindingTrack.setOperateInfo(operateInfo);
		String lastRecord=((StringField)fields.get("lastRecord")).value;
		if("".equals(lastRecord)) lastRecord=null;
		bindingTrack.setLastRecord(lastRecord);
		String currentRecord=((StringField)fields.get("currentRecord")).value;
		if("".equals(currentRecord)) currentRecord=null;
		bindingTrack.setCurrentRecord(currentRecord);
		return bindingTrack;
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
			((BindingTrack)gemsTrack).getId()),mapping(gemsTrack));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+gemsTrack);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object m)
	{
		BindingTrack gemsTrack=(BindingTrack)m;
		FieldObject[] array=new FieldObject[12];
		int i=0;
		array[i++]=FieldKit.create("id",gemsTrack.getId());
		array[i++]=FieldKit.create("bindType",gemsTrack.getBindType());
		array[i++]=FieldKit.create("trackType",gemsTrack.getTrackType());
		array[i++]=FieldKit.create("actionType",gemsTrack.getActionType());
		array[i++]=FieldKit.create("uid",gemsTrack.getUid());
		array[i++]=FieldKit.create("pid",gemsTrack.getPid());
		array[i++]=FieldKit.create("time",gemsTrack.getTime());
		array[i++]=FieldKit.create("operateInfo",gemsTrack.getOperateInfo());
		array[i++]=FieldKit.create("lastRecord",gemsTrack.getLastRecord());
		array[i++]=FieldKit.create("currentRecord",gemsTrack.getCurrentRecord());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
