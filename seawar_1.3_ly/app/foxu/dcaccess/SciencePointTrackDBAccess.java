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
import foxu.sea.alliance.alliancebattle.SciencePointTrack;

/***
 * ���˿Ƽ���ļ�¼
 * 
 * @author lhj
 * 
 */
public class SciencePointTrackDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(SciencePointTrackDBAccess.class);

	public SciencePointTrack load(String id)
	{
		// ����һ�����򣨰�����NpcIsland���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"SciencePointTrack db error");
		// ������������ݵ�������װ��һ����Ҷ���
		SciencePointTrack track=mapping(fields);
		return track;
	}

	public SciencePointTrack[] loadBySql(String sql)
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
				"SciencePointTrack loadBysql valid, db error");
		}
		if(array==null) return null;
		SciencePointTrack[] gameData=new SciencePointTrack[array.length];
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
				"SciencePointTrack loadBysql valid, db error");
		}
		return array;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[13];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("type",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("allianceId",0);
		array[i++]=FieldKit.create("num",0);
		array[i++]=FieldKit.create("nowLeft",0);
		array[i++]=FieldKit.create("createAt",0);
		array[i++]=FieldKit.create("year",0);
		array[i++]=FieldKit.create("month",0);
		array[i++]=FieldKit.create("day",0);
		array[i++]=FieldKit.create("state",0);
		array[i++]=FieldKit.create("style",0);
		array[i++]=FieldKit.create("extra",(String)null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public SciencePointTrack mapping(Fields fields)
	{
		SciencePointTrack track=new SciencePointTrack();
		int id=((IntField)fields.get("id")).value;
		track.setId(id);
		int type=((IntField)fields.get("type")).value;
		track.setType(type);
		int playerId=((IntField)fields.get("playerId")).value;
		track.setPlayerId(playerId);
		int allianceId=((IntField)fields.get("allianceId")).value;
		track.setAllianceId(allianceId);
		int num=((IntField)fields.get("num")).value;
		track.setNum(num);
		int createAt=((IntField)fields.get("createAt")).value;
		track.setCreateAt(createAt);
		int year=((IntField)fields.get("year")).value;
		track.setYear(year);
		int month=((IntField)fields.get("month")).value;
		track.setMonth(month);
		int day=((IntField)fields.get("day")).value;
		track.setDay(day);
		int nowLeft=((IntField)fields.get("nowLeft")).value;
		track.setNowLeft(nowLeft);
		int state=((IntField)fields.get("state")).value;
		track.setState(state);
		int style=((IntField)fields.get("style")).value;
		track.setStyle(style);
		String extra=((StringField)fields.get("extra")).value;
		track.setExtra(extra);
		return track;
	}

	/** ���淽�� ʹ�ð���������ݵ��ֽ������ʼ��ָ����������Ҳ����� */
	public boolean save(int id,Object track)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+track.toString());
		try
		{
			return save(track);
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
		int t=gamePersistence.set(
			FieldKit.create("id",((SciencePointTrack)track).getId()),
			mapping(track));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+track);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ӳ�������� */
	public Fields mapping(Object m)
	{
		SciencePointTrack track=(SciencePointTrack)m;
		FieldObject[] array=new FieldObject[13];
		int i=0;
		array[i++]=FieldKit.create("id",track.getId());
		array[i++]=FieldKit.create("type",track.getType());
		array[i++]=FieldKit.create("playerId",track.getPlayerId());
		array[i++]=FieldKit.create("allianceId",track.getAllianceId());
		array[i++]=FieldKit.create("num",track.getNum());
		array[i++]=FieldKit.create("createAt",track.getCreateAt());
		array[i++]=FieldKit.create("year",track.getYear());
		array[i++]=FieldKit.create("month",track.getMonth());
		array[i++]=FieldKit.create("day",track.getDay());
		array[i++]=FieldKit.create("nowLeft",track.getNowLeft());
		array[i++]=FieldKit.create("state",track.getState());
		array[i++]=FieldKit.create("style",track.getStyle());
		array[i++]=FieldKit.create("extra",track.getExtra());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
