package foxu.dcaccess;

import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import foxu.sea.gems.GemsTrack;
import shelby.dc.GameDBAccess;

/** ��ʯ������־��¼ */
public class GemsTrackDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(NpcIslandGameDBAccess.class);

	public GemsTrack load(String id)
	{
		// ����һ�����򣨰�����NpcIsland���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"GemsTrackDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		GemsTrack gemsTrack=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return gemsTrack;
	}

	public GemsTrack[] loadBySql(String sql)
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
				"GameDataDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		GemsTrack[] gameData=new GemsTrack[array.length];
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
				"GemsTrackDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[10];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("type",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("gems",0);
		array[i++]=FieldKit.create("nowGems",0);
		array[i++]=FieldKit.create("createAt",0);
		array[i++]=FieldKit.create("item_id",0);
		array[i++]=FieldKit.create("year",0);
		array[i++]=FieldKit.create("month",0);
		array[i++]=FieldKit.create("day",0);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public GemsTrack mapping(Fields fields)
	{
		GemsTrack gemsTrack=new GemsTrack();
		int id=((IntField)fields.get("id")).value;
		gemsTrack.setId(id);
		int type=((IntField)fields.get("type")).value;
		gemsTrack.setType(type);
		int playerId=((IntField)fields.get("playerId")).value;
		gemsTrack.setPlayerId(playerId);
		int gems=((IntField)fields.get("gems")).value;
		gemsTrack.setGems(gems);
		int createAt=((IntField)fields.get("createAt")).value;
		gemsTrack.setCreateAt(createAt);
		int item_id=((IntField)fields.get("item_id")).value;
		gemsTrack.setItem_id(item_id);
		int year=((IntField)fields.get("year")).value;
		gemsTrack.setYear(year);
		int month=((IntField)fields.get("month")).value;
		gemsTrack.setMonth(month);
		int day=((IntField)fields.get("day")).value;
		gemsTrack.setDay(day);
		long nowGems=((LongField)fields.get("nowGems")).value;
		gemsTrack.setNowGems(nowGems);
		return gemsTrack;
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
			((GemsTrack)gemsTrack).getId()),mapping(gemsTrack));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+gemsTrack);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ӳ�������� */
	public Fields mapping(Object m)
	{
		GemsTrack gemsTrack=(GemsTrack)m;
		FieldObject[] array=new FieldObject[10];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",gemsTrack.getId());
		array[i++]=FieldKit.create("type",gemsTrack.getType());
		array[i++]=FieldKit.create("playerId",gemsTrack.getPlayerId());
		array[i++]=FieldKit.create("gems",gemsTrack.getGems());
		array[i++]=FieldKit.create("createAt",gemsTrack.getCreateAt());
		array[i++]=FieldKit.create("item_id",gemsTrack.getItem_id());
		array[i++]=FieldKit.create("year",gemsTrack.getYear());
		array[i++]=FieldKit.create("month",gemsTrack.getMonth());
		array[i++]=FieldKit.create("day",gemsTrack.getDay());
		array[i++]=FieldKit.create("nowGems",gemsTrack.getNowGems());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
