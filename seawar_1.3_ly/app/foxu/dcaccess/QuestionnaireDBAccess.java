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
import foxu.sea.QuestionnaireRecord;


public class QuestionnaireDBAccess extends GameDBAccess
{
	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(OfficerTrackDBAccess.class);

	public QuestionnaireRecord load(String id)
	{
		// ����һ�����򣨰�����NpcIsland���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"QuestionnaireDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		QuestionnaireRecord qr=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return qr;
	}

	public QuestionnaireRecord[] loadBySql(String sql)
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
				"QuestionnaireDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		QuestionnaireRecord[] gameData=new QuestionnaireRecord[array.length];
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
				"QuestionnaireDBAccess loadBysql valid, db error");
		}
		return array;
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
				"QuestionnaireDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[9];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("player_id",0);
		array[i++]=FieldKit.create("act_id",0);
		array[i++]=FieldKit.create("topic_index",0);
		array[i++]=FieldKit.create("topic_type",0);
		array[i++]=FieldKit.create("answer",(String)null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public QuestionnaireRecord mapping(Fields fields)
	{
		QuestionnaireRecord qr=new QuestionnaireRecord();
		int id=((IntField)fields.get("id")).value;
		qr.setId(id);
		int playerId=((IntField)fields.get("player_id")).value;
		qr.setPlayerId(playerId);
		int actId=((IntField)fields.get("act_id")).value;
		qr.setActId(actId);
		int topicIndex=((IntField)fields.get("topic_index")).value;
		qr.setTopicIndex(topicIndex);
		int topicType=((IntField)fields.get("topic_type")).value;
		qr.setTopicType(topicType);
		String answer=((StringField)fields.get("answer")).value;
		qr.setAnswer(answer);
		return qr;
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
			((QuestionnaireRecord)gemsTrack).getId()),mapping(gemsTrack));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+gemsTrack);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ӳ�������� */
	public Fields mapping(Object m)
	{
		QuestionnaireRecord gemsTrack=(QuestionnaireRecord)m;
		FieldObject[] array=new FieldObject[9];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",gemsTrack.getId());
		array[i++]=FieldKit.create("player_id",gemsTrack.getPlayerId());
		array[i++]=FieldKit.create("act_id",gemsTrack.getActId());
		array[i++]=FieldKit.create("topic_index",gemsTrack.getTopicIndex());
		array[i++]=FieldKit.create("topic_type",gemsTrack.getTopicType());
		array[i++]=FieldKit.create("answer",gemsTrack.getAnswer());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
}
