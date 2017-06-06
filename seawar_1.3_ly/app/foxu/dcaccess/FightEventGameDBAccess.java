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
import mustang.text.CharBuffer;
import mustang.text.CharBufferThreadLocal;
import mustang.text.TextKit;
import shelby.dc.GameDBAccess;
import foxu.sea.event.FightEvent;

/**
 * ս���¼������� author:icetiger
 */
public class FightEventGameDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(MessageGameDBAccess.class);

	public FightEvent load(String id)
	{
		// ����һ�����򣨰�����player���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"FightEventGameDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		FightEvent fightEvent=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// fightEvent.bytesWrite(bb);
		return fightEvent;
	}

	public FightEvent[] loadBySql(String sql)
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
				"FightEventGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		FightEvent[] fightEvent=new FightEvent[array.length];
		for(int i=0;i<array.length;i++)
		{
			fightEvent[i]=mapping(array[i]);
		}
		return fightEvent;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[13];
		int i=0;
		// array[i++]=FieldKit.create("immure",0);
		// array[i++]=FieldKit.create("cause",(String)null);
		// array[i++]=FieldKit.create("sid",0);
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("event_type",0);
		array[i++]=FieldKit.create("event_state",0);
		array[i++]=FieldKit.create("attack_id",0);
		array[i++]=FieldKit.create("player_id",0);
		array[i++]=FieldKit.create("created_at",0);
		array[i++]=FieldKit.create("need_time",0);
		array[i++]=FieldKit.create("delete",0);
		array[i++]=FieldKit.create("island_id",0);
		array[i++]=FieldKit.create("rewards",(String)null);

		array[i++]=FieldKit.create("fight_ships",(byte[])null);
		array[i++]=FieldKit.create("officers",(byte[])null);

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public FightEvent mapping(Fields fields)
	{
		// FieldObject f=fields.get("immure");
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
		FightEvent fightEvent=new FightEvent();
		int id=((IntField)fields.get("id")).value;
		fightEvent.setId(id);
		int eventState=((IntField)fields.get("event_state")).value;
		fightEvent.setEventState(eventState);
		int eventType=((IntField)fields.get("event_type")).value;
		fightEvent.setType(eventType);
		int attackId=((IntField)fields.get("attack_id")).value;
		fightEvent.setAttackIslandIndex(attackId);
		int playerId=((IntField)fields.get("player_id")).value;
		fightEvent.setPlayerId(playerId);
		int createdTime=((IntField)fields.get("created_at")).value;
		fightEvent.setCreatAt(createdTime);
		int needTime=((IntField)fields.get("need_time")).value;
		fightEvent.setNeedTimeDB(needTime);
		int delete=((IntField)fields.get("delete")).value;
		fightEvent.setDelete(delete);
		int islandId=((IntField)fields.get("island_id")).value;
		fightEvent.setSourceIslandId(islandId);
		String str=((StringField)fields.get("rewards")).value;

		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,":");
			int[] rewards=new int[strs.length];
			for(int i=0;i<strs.length;i++)
				rewards[i]=Integer.parseInt(strs[i]);
			fightEvent.setResources(rewards);
		}
		byte[] array=((ByteArrayField)fields.get("fight_ships")).value;
		if(array!=null&&array.length>0)
			fightEvent.bytesReadShips(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("officers")).value;
		if(array!=null&&array.length>0)
			fightEvent.getFleetGroup().getOfficerFleetAttr()
				.bytesRead(new ByteBuffer(array));

		return fightEvent;
	}

	/** ���淽�� ʹ�ð���������ݵ��ֽ������ʼ��ָ����������Ҳ����� */
	public boolean save(int id,Object fightEvent)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+fightEvent.toString());
		try
		{
			// int offset=data.offset();
			// FightEvent fightEvent=new FightEvent();
			// fightEvent.bytesRead(data);
			// data.setOffset(offset);
			return save(fightEvent);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** ���淽�� �����Ƿ�����ɹ� */
	public boolean save(Object fightEvent)
	{
		if(fightEvent==null) return false;// ż�������
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.set(FieldKit.create("id",
			((FightEvent)fightEvent).getId()),mapping(fightEvent));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+fightEvent);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ɾ������ */
	public void delete(Object fightEvent)
	{
		if(fightEvent==null) return;// ż�������
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.delete(FieldKit.create("id",
			((FightEvent)fightEvent).getId()));
		if(log.isInfoEnabled()) log.info("delete, "+t+" "+fightEvent);
	}

	/** �Ƿ��ɾ�� */
	public boolean isDelete(Object event)
	{
		if(event==null) return false;
		FightEvent ev=(FightEvent)event;
		return ev.getDelete()==FightEvent.DELETE_TYPE;
	}

	/** ӳ�������� */
	public Fields mapping(Object f)
	{
		FightEvent fightEvent=(FightEvent)f;
		FieldObject[] array=new FieldObject[13];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",fightEvent.getId());
		array[i++]=FieldKit.create("event_type",fightEvent.getType());
		array[i++]=FieldKit.create("event_state",fightEvent.getEventState());
		array[i++]=FieldKit.create("attack_id",fightEvent
			.getAttackIslandIndex());
		array[i++]=FieldKit.create("player_id",fightEvent.getPlayerId());
		array[i++]=FieldKit.create("created_at",fightEvent.getCreatAt());
		array[i++]=FieldKit.create("need_time",fightEvent.getNeedTime());
		array[i++]=FieldKit.create("delete",fightEvent.getDelete());
		array[i++]=FieldKit.create("island_id",fightEvent
			.getSourceIslandIndex());
		CharBuffer cb=CharBufferThreadLocal.getCharBuffer();
		cb.clear();
		int[] temp=fightEvent.getResources();
		for(int j=0;j<temp.length;j++)
			cb.append(temp[j]).append(':');
		if(cb.length()>0) cb.setTop(cb.length()-1);
		array[i++]=FieldKit.create("rewards",cb.getString());

		cb.clear();
		ByteBuffer bb=new ByteBuffer();

		bb.clear();
		fightEvent.bytesWriteShips(bb);
		array[i++]=FieldKit.create("fight_ships",bb.toArray());
		
		bb.clear();
		fightEvent.getFleetGroup().getOfficerFleetAttr().bytesWrite(bb);
		array[i++]=FieldKit.create("officers",bb.toArray());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
}
