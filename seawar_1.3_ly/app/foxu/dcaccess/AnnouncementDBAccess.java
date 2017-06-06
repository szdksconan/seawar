package foxu.dcaccess;

import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import shelby.dc.GameDBAccess;
import foxu.sea.announcement.Announcement;

/**
 * �������ݿ��������
 * 
 * @author lihongji
 * 
 */
public class AnnouncementDBAccess extends GameDBAccess
{

	/** ���淽�� �����Ƿ�����ɹ� */
	public boolean save(Object announce)
	{
		if(announce==null) return false;
		// ��ӳ��������������ݿ���
		int t=gamePersistence.set(
			FieldKit.create("id",((Announcement)announce).getId()),
			mapping(announce));
		return t==Persistence.OK||t==Persistence.ADD;
	}
	public Announcement load(String id)
	{
		// ����һ������
		Fields fields=mapping();
		// ʹ�ó־���
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"MessageGameDBAccess db error");
		// �����й����������װ��һ���������
		Announcement announce=mapping(fields);
		return announce;
	}
	/** ���ع��� */
	public Announcement[] loadBySql(String sql)
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
		Announcement[] annouce=new Announcement[array.length];
		for(int i=0;i<array.length;i++)
		{
			annouce[i]=mapping(array[i]);
		}
		return annouce;
	}
	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[13];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("permanent",0);
		array[i++]=FieldKit.create("stime",0);
		array[i++]=FieldKit.create("etime",0);
		array[i++]=FieldKit.create("title",0);
		array[i++]=FieldKit.create("content",0);
		array[i++]=FieldKit.create("btnname",0);
		array[i++]=FieldKit.create("introduction",0);
		array[i++]=FieldKit.create("awardsid",(byte[])null);
		array[i++]=FieldKit.create("readannounce",(byte[])null);
		array[i++]=FieldKit.create("awardplayer",(byte[])null);
		array[i++]=FieldKit.create("readplayer",(byte[])null);
		array[i++]=FieldKit.create("ableawardplayer",(byte[])null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
	/** ӳ�������� */
	public Fields mapping(Object data)
	{
		Announcement announce=(Announcement)data;
		FieldObject[] array=new FieldObject[13];
		int i=0;
		array[i++]=FieldKit.create("id",announce.getId());
		array[i++]=FieldKit.create("permanent",announce.getPermanent());
		array[i++]=FieldKit.create("stime",announce.getStartTime());
		array[i++]=FieldKit.create("etime",announce.getEndTime());
		array[i++]=FieldKit.create("title",announce.getTitle());
		array[i++]=FieldKit.create("content",announce.getContent());
		array[i++]=FieldKit.create("btnname",announce.getBtnname());
		array[i++]=FieldKit
			.create("introduction",announce.getIntroduction());
		if(announce.getInitData()!=null)
		{
			array[i++]=FieldKit.create("awardsid",announce.getInitData()
				.toArray());
		}
		else
		{
			array[i++]=FieldKit.create("awardsid",new byte[0]);
		}
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		announce.bytesWriteAnnounce(bb);
		array[i++]=FieldKit.create("readannounce",bb.toArray());

		bb.clear();
		announce.bytesWriteAwardPlayer(bb);
		array[i++]=FieldKit.create("awardplayer",bb.toArray());

		bb.clear();
		announce.bytesWritereadPlayer(bb);
		array[i++]=FieldKit.create("readplayer",bb.toArray());

		bb.clear();
		announce.bytesWriteableAwardPlayer(bb);
		array[i++]=FieldKit.create("ableawardplayer",bb.toArray());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
	/** ӳ�������� */
	public Announcement mapping(Fields fields)
	{
		Announcement announce=new Announcement();
		announce.setId(((IntField)fields.get("id")).value);
		announce.setPermanent(((IntField)fields.get("permanent")).value);
		announce.setStartTime(((IntField)fields.get("stime")).value);
		announce.setEndTime(((IntField)fields.get("etime")).value);
		String btnname=((StringField)fields.get("btnname")).value;
		announce.setBtnname(btnname);
		String title=((StringField)fields.get("title")).value;
		announce.setTitle(title);
		String content=((StringField)fields.get("content")).value;
		announce.setContent(content);
		String introduction=((StringField)fields.get("introduction")).value;
		announce.setIntroduction(introduction);
		byte[] array=((ByteArrayField)fields.get("awardsid")).value;
		if(array!=null&&array.length>0)
			announce.initData(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("readannounce")).value;
		if(array!=null&&array.length>0)
			announce.bytesReadAnnounce(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("awardplayer")).value;
		if(array!=null&&array.length>0)
			announce.bytesReadAwardPlayer(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("readplayer")).value;
		if(array!=null&&array.length>0)
			announce.bytesReadPlayer(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("ableawardplayer")).value;
		if(array!=null&&array.length>0)
			announce.bytesReadAwardPlayer(new ByteBuffer(array));
		return announce;
	}
	/** ɾ������ */
	public void delete(Object announce)
	{
		if(announce==null) return;// ż�������
		// ���������ӳ��������������ݿ���
		gamePersistence.delete(FieldKit.create("id",
			((Announcement)announce).getId()));
	}

}
