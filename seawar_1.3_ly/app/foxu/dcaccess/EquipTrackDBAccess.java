package foxu.dcaccess;

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
import foxu.sea.equipment.EquipmentTrack;
import shelby.dc.GameDBAccess;

/** װ����־��¼ */
public class EquipTrackDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(EquipTrackDBAccess.class);

	public EquipmentTrack load(String id)
	{
		// ����һ�����򣨰�����NpcIsland���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"EquipTrackDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		EquipmentTrack gemsTrack=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return gemsTrack;
	}

	public EquipmentTrack[] loadBySql(String sql)
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
				"EquipTrackDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		EquipmentTrack[] gameData=new EquipmentTrack[array.length];
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
				"EquipTrackDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[12];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("type",0);
		array[i++]=FieldKit.create("reason",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("equipSid",0);
		array[i++]=FieldKit.create("num",0);
		array[i++]=FieldKit.create("nowLeft",0);
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
	public EquipmentTrack mapping(Fields fields)
	{
		EquipmentTrack equipTrack=new EquipmentTrack();
		int id=((IntField)fields.get("id")).value;
		equipTrack.setId(id);
		int type=((IntField)fields.get("type")).value;
		equipTrack.setType(type);
		int reason=((IntField)fields.get("reason")).value;
		equipTrack.setReason(reason);
		int playerId=((IntField)fields.get("playerId")).value;
		equipTrack.setPlayerId(playerId);
		int equipSid=((IntField)fields.get("equipSid")).value;
		equipTrack.setEquipSid(equipSid);
		int num=((IntField)fields.get("num")).value;
		equipTrack.setNum(num);
		int createAt=((IntField)fields.get("createAt")).value;
		equipTrack.setCreateAt(createAt);
		int item_id=((IntField)fields.get("item_id")).value;
		equipTrack.setItem_id(item_id);
		int year=((IntField)fields.get("year")).value;
		equipTrack.setYear(year);
		int month=((IntField)fields.get("month")).value;
		equipTrack.setMonth(month);
		int day=((IntField)fields.get("day")).value;
		equipTrack.setDay(day);
		int nowLeft=((IntField)fields.get("nowLeft")).value;
		equipTrack.setNowLeft(nowLeft);
		return equipTrack;
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
			((EquipmentTrack)gemsTrack).getId()),mapping(gemsTrack));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+gemsTrack);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ӳ�������� */
	public Fields mapping(Object m)
	{
		EquipmentTrack gemsTrack=(EquipmentTrack)m;
		FieldObject[] array=new FieldObject[12];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",gemsTrack.getId());
		array[i++]=FieldKit.create("type",gemsTrack.getType());
		array[i++]=FieldKit.create("reason",gemsTrack.getReason());
		array[i++]=FieldKit.create("playerId",gemsTrack.getPlayerId());
		array[i++]=FieldKit.create("num",gemsTrack.getNum());
		array[i++]=FieldKit.create("createAt",gemsTrack.getCreateAt());
		array[i++]=FieldKit.create("item_id",gemsTrack.getItem_id());
		array[i++]=FieldKit.create("year",gemsTrack.getYear());
		array[i++]=FieldKit.create("month",gemsTrack.getMonth());
		array[i++]=FieldKit.create("day",gemsTrack.getDay());
		array[i++]=FieldKit.create("equipSid",gemsTrack.getEquipSid());
		array[i++]=FieldKit.create("nowLeft",gemsTrack.getNowLeft());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
