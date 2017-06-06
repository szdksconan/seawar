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
import foxu.sea.NpcIsland;

/**
 * �ʼ������� author:icetiger
 */
public class NpcIslandGameDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(NpcIslandGameDBAccess.class);

	public NpcIsland load(String id)
	{
		// ����һ�����򣨰�����NpcIsland���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"NpcIslandGameDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		NpcIsland island=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// message.bytesWrite(bb);
		return island;
	}

	public NpcIsland[] loadBySql(String sql)
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
				"NpcIslandGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		NpcIsland[] island=new NpcIsland[array.length];
		for(int i=0;i<array.length;i++)
		{
			island[i]=mapping(array[i]);
		}
		return island;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[7];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("index",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("sid",0);
		array[i++]=FieldKit.create("tempAttackId",0);
		array[i++]=FieldKit.create("endTime",0);
		array[i++]=FieldKit.create("buff",(String)null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public NpcIsland mapping(Fields fields)
	{
		int sampleSid=((IntField)fields.get("sid")).value;
		NpcIsland island=(NpcIsland)NpcIsland.factory.newSample(sampleSid);
		int id=((IntField)fields.get("id")).value;
		island.setId(id);
		island.bindUid(id);
		int index=((IntField)fields.get("index")).value;
		island.setIndex(index);
		int playerId=((IntField)fields.get("playerId")).value;
		island.setPlayerId(playerId);
		int tempAttackId = ((IntField)fields.get("tempAttackId")).value;
		island.setTempAttackEventId(tempAttackId);
		int endTime=((IntField)fields.get("endTime")).value;
		island.setEndTime(endTime);
		String str=((StringField)fields.get("buff")).value;
		island.setBuff(str);
		return island;
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
	public boolean save(Object island)
	{
		if(island==null) return false;// ż�������
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.set(FieldKit.create("id",((NpcIsland)island)
			.getId()),mapping(island));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+island);
		return t==Persistence.OK||t==Persistence.ADD;
	}


	/** ӳ�������� */
	public Fields mapping(Object m)
	{
		NpcIsland island=(NpcIsland)m;
		FieldObject[] array=new FieldObject[7];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",island.getId());
		array[i++]=FieldKit.create("index",island.getIndex());
		array[i++]=FieldKit.create("playerId",island.getPlayerId());
		array[i++]=FieldKit.create("sid",island.getSid());
		array[i++]=FieldKit.create("tempAttackId",island.getTempAttackEventId());
		array[i++]=FieldKit.create("endTime",island.getEndTime());
		array[i++]=FieldKit.create("buff",island.getBuff());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
