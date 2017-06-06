package foxu.dcaccess;

import foxu.sea.builds.produce.ProducePropTrack;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import shelby.dc.GameDBAccess;

/**
 * ��Ʒ��¼
 * @author Alan
 *
 */
public class ProducePropTrackDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(ProducePropTrackDBAccess.class);

	public ProducePropTrack load(String id)
	{
		// ����һ�����򣨰�����NpcIsland���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"ProducePropTrackDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		ProducePropTrack track=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return track;
	}

	public ProducePropTrack[] loadBySql(String sql)
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
				"ProducePropTrackDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		ProducePropTrack[] gameData=new ProducePropTrack[array.length];
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
				"ProducePropTrackDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[11];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("type",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("propSid",0);
		array[i++]=FieldKit.create("num",0);
		array[i++]=FieldKit.create("createAt",0);
		array[i++]=FieldKit.create("needTime",0);
		array[i++]=FieldKit.create("buildIndex",0);
		array[i++]=FieldKit.create("buildSid",0);
		array[i++]=FieldKit.create("buildLv",0);
		array[i++]=FieldKit.create("productId",0);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public ProducePropTrack mapping(Fields fields)
	{
		ProducePropTrack track=new ProducePropTrack();
		int id=((IntField)fields.get("id")).value;
		track.setId(id);
		int type=((IntField)fields.get("type")).value;
		track.setType(type);
		int playerId=((IntField)fields.get("playerId")).value;
		track.setPlayerId(playerId);
		int propSid=((IntField)fields.get("propSid")).value;
		track.setPropSid(propSid);
		int num=((IntField)fields.get("num")).value;
		track.setNum(num);
		int createAt=((IntField)fields.get("createAt")).value;
		track.setCreateAt(createAt);
		int needTime=((IntField)fields.get("needTime")).value;
		track.setNeedTime(needTime);
		int buildIndex=((IntField)fields.get("buildIndex")).value;
		track.setBuildSid(buildIndex);
		int buildSid=((IntField)fields.get("buildSid")).value;
		track.setBuildSid(buildSid);
		int buildLv=((IntField)fields.get("buildLv")).value;
		track.setBuildLv(buildLv);
		int productId=((IntField)fields.get("productId")).value;
		track.setProductId(productId);
		return track;
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
	public boolean save(Object track)
	{
		if(track==null) return false;// ż�������
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.set(FieldKit.create("id",
			((ProducePropTrack)track).getId()),mapping(track));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+track);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ӳ�������� */
	public Fields mapping(Object m)
	{
		ProducePropTrack track=(ProducePropTrack)m;
		FieldObject[] array=new FieldObject[11];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",track.getId());
		array[i++]=FieldKit.create("type",track.getType());
		array[i++]=FieldKit.create("playerId",track.getPlayerId());
		array[i++]=FieldKit.create("propSid",track.getPropSid());
		array[i++]=FieldKit.create("num",track.getNum());
		array[i++]=FieldKit.create("createAt",track.getCreateAt());
		array[i++]=FieldKit.create("needTime",track.getNeedTime());
		array[i++]=FieldKit.create("buildIndex",track.getBuildIndex());
		array[i++]=FieldKit.create("buildSid",track.getBuildSid());
		array[i++]=FieldKit.create("buildLv",track.getBuildLv());
		array[i++]=FieldKit.create("productId",track.getProductId());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
