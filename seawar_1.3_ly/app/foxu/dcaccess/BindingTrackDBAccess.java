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

/** ����־��¼ */
public class BindingTrackDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(BindingTrackDBAccess.class);

	public BindingTrack load(String id)
	{
		// ����һ�����򣨰�����NpcIsland���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"BindingTrackDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
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

	/** ӳ�������� */
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

	/** ӳ�������� */
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

	/** ���淽�� ʹ�ð���������ݵ��ֽ������ʼ��ָ����������Ҳ����� */
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
	/** ���淽�� �����Ƿ�����ɹ� */
	public boolean save(Object gemsTrack)
	{
		if(gemsTrack==null) return false;// ż�������
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.set(FieldKit.create("id",
			((BindingTrack)gemsTrack).getId()),mapping(gemsTrack));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+gemsTrack);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ӳ�������� */
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
