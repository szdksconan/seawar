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
import foxu.sea.PlayerAdvice;

/** ��ҽ��� BUG�ύ */
public class PlayerAdviceDBAccess extends GameDBAccess
{
	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(MessageGameDBAccess.class);

	public PlayerAdvice load(String id)
	{
		// ����һ�����򣨰�����player���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"PlayerAdviceDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		PlayerAdvice advice=mapping(fields);
		return advice;
	}

	public PlayerAdvice[] loadBySql(String sql)
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
				"PlayerAdviceDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		PlayerAdvice[] advice=new PlayerAdvice[array.length];
		for(int i=0;i<array.length;i++)
		{
			advice[i]=mapping(array[i]);
		}
		return advice;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[8];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("creatTime",0);
		array[i++]=FieldKit.create("state",0);

		array[i++]=FieldKit.create("titile",(String)null);
		array[i++]=FieldKit.create("content",(String)null);
		array[i++]=FieldKit.create("playerName",(String)null);
		array[i++]=FieldKit.create("gm_response",(String)null);

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public PlayerAdvice mapping(Fields fields)
	{
		PlayerAdvice advice=new PlayerAdvice();
		int id=((IntField)fields.get("id")).value;
		advice.setId(id);
		int playerId=((IntField)fields.get("playerId")).value;
		advice.setPlayerId(playerId);
		int creatTime=((IntField)fields.get("creatTime")).value;
		advice.setCreatTime(creatTime);
		int state=((IntField)fields.get("state")).value;
		advice.setState(state);

		String titile=((StringField)fields.get("titile")).value;
		advice.setTitile(titile);
		String content=((StringField)fields.get("content")).value;
		advice.setContent(content);
		String playerName=((StringField)fields.get("playerName")).value;
		advice.setPlayerName(playerName);
		String gm_response=((StringField)fields.get("gm_response")).value;
		advice.setGmResponse(gm_response);
		return advice;
	}

	/** ���淽�� ʹ�ð���������ݵ��ֽ������ʼ��ָ����������Ҳ����� */
	public boolean save(int id,Object advice)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+advice.toString());
		try
		{
			return save(advice);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** ���淽�� �����Ƿ�����ɹ� */
	public boolean save(Object advice)
	{
		if(advice==null) return false;// ż�������
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.set(FieldKit.create("id",
			((PlayerAdvice)advice).getId()),mapping(advice));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+advice);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	public boolean isDelete(Object advice)
	{
		if(advice==null) return false;
		PlayerAdvice ad=(PlayerAdvice)advice;
		return ad.getState()==1;
	}

	/** ɾ������ */
	public void delete(Object advice)
	{
		if(advice==null) return;// ż�������
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.delete(FieldKit.create("id",
			((PlayerAdvice)advice).getId()));
		if(log.isInfoEnabled()) log.info("delete, "+t+" "+advice);
	}

	/** ӳ�������� */
	public Fields mapping(Object a)
	{
		PlayerAdvice advice=(PlayerAdvice)a;
		FieldObject[] array=new FieldObject[8];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",advice.getId());
		array[i++]=FieldKit.create("playerId",advice.getPlayerId());
		array[i++]=FieldKit.create("creatTime",advice.getCreatTime());
		array[i++]=FieldKit.create("state",advice.getState());
		array[i++]=FieldKit.create("titile",advice.getTitile());
		array[i++]=FieldKit.create("content",advice.getContent());
		array[i++]=FieldKit.create("playerName",advice.getPlayerName());
		array[i++]=FieldKit.create("gm_response",advice.getGmResponse());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
}
