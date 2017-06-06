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
import shelby.dc.GameDBAccess;
import foxu.sea.messgae.Message;

/**
 * 邮件加载器 author:icetiger
 */
public class MessageGameDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(MessageGameDBAccess.class);

	public Message load(String id)
	{
		// 构造一个空域（包括了player表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"MessageGameDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		Message message=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return message;
	}

	public Message[] loadBySql(String sql)
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
				"PlayerGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		Message[] message=new Message[array.length];
		for(int i=0;i<array.length;i++)
		{
			message[i]=mapping(array[i]);
		}
		return message;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[21];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("mass",0);
		array[i++]=FieldKit.create("send_id",0);
		array[i++]=FieldKit.create("receive_id",0);
		array[i++]=FieldKit.create("create_at",0);
		array[i++]=FieldKit.create("state",0);
		array[i++]=FieldKit.create("message_type",0);
		array[i++]=FieldKit.create("fightType",0);
		array[i++]=FieldKit.create("delete",0);
		array[i++]=FieldKit.create("recive_state",0);
		array[i++]=FieldKit.create("fightVersion",0);
		array[i++]=FieldKit.create("feats",0);
		// array[i++]=FieldKit.create("language",0);

		array[i++]=FieldKit.create("send_name",(String)null);
		array[i++]=FieldKit.create("receive_name",(String)null);
		array[i++]=FieldKit.create("content",(byte[])null);
		array[i++]=FieldKit.create("title",(byte[])null);
		array[i++]=FieldKit.create("allianceFightTitle",(String)null);
		array[i++]=FieldKit.create("fightData",(byte[])null);
		array[i++]=FieldKit.create("fightDataFore",(byte[])null);
		array[i++]=FieldKit.create("award",(byte[])null);
		array[i++]=FieldKit.create("officerData",(byte[])null);

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public Message mapping(Fields fields)
	{
//		FieldObject f=fields.get("immure");
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
		Message message=new Message();
		int messageId=((IntField)fields.get("id")).value;
		message.setMessageId(messageId);
		int mass=((IntField)fields.get("mass")).value;
		message.setMass(mass);
		int sendId=((IntField)fields.get("send_id")).value;
		message.setSendId(sendId);
		int receiveId=((IntField)fields.get("receive_id")).value;
		message.setReceiveId(receiveId);
		int createAt=((IntField)fields.get("create_at")).value;
		message.setCreateAt(createAt);
		int state=((IntField)fields.get("state")).value;
		message.setState(state);
		int messageType=((IntField)fields.get("message_type")).value;
		message.setMessageType(messageType);
		int fightType=((IntField)fields.get("fightType")).value;
		message.setFightType(fightType);
		int delete=((IntField)fields.get("delete")).value;
		message.setDelete(delete);
		int recive_state=((IntField)fields.get("recive_state")).value;
		message.setRecive_state(recive_state);
		int fightVersion=((IntField)fields.get("fightVersion")).value;
		message.setFightVersion(fightVersion);
		int feats=((IntField)fields.get("feats")).value;
		message.setFeats(feats);

		String sendName=((StringField)fields.get("send_name")).value;
		message.setSendName(sendName);
		String receiveName=((StringField)fields.get("receive_name")).value;
		message.setReceiveName(receiveName);
		// int language=((IntField)fields.get("language")).value;
		// message.setLanguage(language);
		String allianceFightTitle=((StringField)fields.get("allianceFightTitle")).value;
		message.setAllianceFightTileInfo(allianceFightTitle);
		
		byte[] array=((ByteArrayField)fields.get("fightData")).value;
		if(array!=null&&array.length>0)
			message.bytesReadMessageData(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("fightDataFore")).value;
		if(array!=null&&array.length>0)
			message.bytesReadMessageDataFore(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("content")).value;
		if(array!=null&&array.length>0)
			message.bytesReadContent(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("title")).value;
		if(array!=null&&array.length>0)
			message.bytesReadTitle(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("award")).value;
		if(array!=null&&array.length>0)
			message.bytesReadAward(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("officerData")).value;
		if(array!=null&&array.length>0)
			message.bytesReadOfficerData(new ByteBuffer(array));
		
		return message;
	}

	/** 保存方法 使用包含玩家数据的字节数组初始化指定姓名的玩家并保存 */
	public boolean save(int id,Object message)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+message.toString());
		try
		{
			// int offset=data.offset();
			// Message message=new Message();
			// message.bytesRead(data);
			// data.setOffset(offset);
			return save(message);
		}
		catch(Exception e)
		{

			// 检查内容
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object message)
	{
		if(message==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",((Message)message)
			.getMessageId()),mapping(message));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+message);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	public boolean isDelete(Object message)
	{
		if(message==null) return false;
		Message mess=(Message)message;
		return mess.getDelete()!=0;
	}

	/** 删除方法 */
	public void delete(Object message)
	{
		if(message==null) return;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.delete(FieldKit.create("id",((Message)message)
			.getMessageId()));
		if(log.isInfoEnabled()) log.info("delete, "+t+" "+message);
	}

	/** 映射成域对象 */
	public Fields mapping(Object m)
	{
		Message message=(Message)m;
		FieldObject[] array=new FieldObject[21];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",message.getMessageId());
		array[i++]=FieldKit.create("mass",message.getMass());
		array[i++]=FieldKit.create("send_id",message.getSendId());
		array[i++]=FieldKit.create("receive_id",message.getReceiveId());
		array[i++]=FieldKit.create("create_at",message.getCreateAt());
		array[i++]=FieldKit.create("state",message.getState());
		array[i++]=FieldKit.create("recive_state",message.getRecive_state());
		array[i++]=FieldKit.create("message_type",message.getMessageType());
		array[i++]=FieldKit.create("fightType",message.getFightType());
		array[i++]=FieldKit.create("delete",message.getDelete());
		array[i++]=FieldKit.create("send_name",message.getSendName());
		array[i++]=FieldKit.create("receive_name",message.getReceiveName());
		array[i++]=FieldKit.create("allianceFightTitle",message.getStringFightTitleInfo());
		array[i++]=FieldKit.create("fightVersion",message.getFightVersion());
		array[i++]=FieldKit.create("feats",message.getFeats());
		
		// array[i++]=FieldKit.create("language",message.getLanguage());

		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		message.bytesWriteMessageData(bb);
		array[i++]=FieldKit.create("fightData",bb.toArray());

		bb.clear();
		message.bytesWriteMessageDataFore(bb);
		array[i++]=FieldKit.create("fightDataFore",bb.toArray());
		
		bb.clear();
		message.bytesWriteContent(bb);
		array[i++]=FieldKit.create("content",bb.toArray());
		
		bb.clear();
		message.bytesWriteTitle(bb);
		array[i++]=FieldKit.create("title",bb.toArray());
		
		bb.clear();
		message.bytesWriteAward(bb);
		array[i++]=FieldKit.create("award",bb.toArray());
		
		bb.clear();
		message.bytesWriteOfficerData(bb);
		array[i++]=FieldKit.create("officerData",bb.toArray());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
