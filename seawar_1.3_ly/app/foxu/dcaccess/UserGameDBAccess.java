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
import mustang.util.TimeKit;
import shelby.dc.GameDBAccess;
import foxu.sea.User;
import foxu.sea.uid.UidKit;

/**
 * ����˺� author:icetiger
 */
public class UserGameDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(UserGameDBAccess.class);

	/** UID�ṩ�� */
	UidKit uidkit;

	/** �ж��û����Ƿ���� */
	public boolean isExist(Object key,int id)
	{
		Fields fields=mapping();
		int t=Persistence.EXCEPTION;
		t=getGamePersistence().get(
			FieldKit.create("user_account",(String)key),fields);
		if(t==Persistence.RESULTLESS) return false;
		if(t==Persistence.EXCEPTION) return true;
		return true;
	}

	/** ����һ������˺� */
	public boolean createUser(String udid,String account,String password,String tel)
	{
		if(isExist(account,0)) return false;
		User user=new User();
		user.setId(uidkit.getPlusUid());
		user.setUserAccount(account);
		user.setPassword(password);
		user.setUserType(User.USER);
		if(udid!=null)
		{
			user.setCreateUdid(udid);
			user.setLoginUdid(udid);
		}
		user.setCreateAt(TimeKit.getSecondTime());
		user.setLoginTime(TimeKit.getSecondTime());
		user.setBindingTel(tel);
		return save(user);
	}

	/** ����һ��guest�˻� */
	public boolean createGuestUser(String udid,int userId,String account)
	{
		if(isExist(account,0)) return false;
		User user=new User();
		user.setId(userId);
		user.setUserAccount(account);
		user.setUserType(User.GUEST);
		if(udid!=null)
		{
			user.setCreateUdid(udid);
			user.setLoginUdid(udid);
		}
		user.setCreateAt(TimeKit.getSecondTime());
		user.setLoginTime(TimeKit.getSecondTime());
		return save(user);
	}

	/** ���ݵ�¼udid��ȡ�˺� û�з���null */
	public User loadByLoginUser(String loginUdid)
	{
		// ����һ�����򣨰�����player���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("login_udid",loginUdid),
			fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS) return null;
		// ������������ݵ�������װ��һ����Ҷ���
		User user=mapping(fields);
		return user;
	}

	/** ����uid��ȡ�˺����� û�з���null */
	public User loadByCreateUdid(String udid)
	{
		// ����һ�����򣨰�����player���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("login_udid",udid),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS) return null;
		// ������������ݵ�������װ��һ����Ҷ���
		User user=mapping(fields);
		return user;
	}

	/** �����˺����ͺ�udid�ҵ����豸��guest�˺� */
	public User loadUserBySql(String sql)
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
				"UserGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		User user=mapping(array);
		return user;
	}
	
	/** ����id��ȡ�˺����� û�з���null */
	public User loadById(String id)
	{
		// ����һ�����򣨰�����player���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS) return null;
		// ������������ݵ�������װ��һ����Ҷ���
		User user=mapping(fields);
		return user;
	}
	
	/***/
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
				"UserGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}
	
	/***/
	public Fields[] loadsSql(String sql)
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
				"UserGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}

	/***/
	public User[] loadBySql(String sql)
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
				"UserGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		User[] user=new User[array.length];
		for(int i=0;i<array.length;i++)
		{
			user[i]=mapping(array[i]);
		}
		return user;
	}

	public User load(String id)
	{
		// ����һ�����򣨰�����player���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"UserGameDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		User user=mapping(fields);
		return user;
	}

	/** ɾ������ */
	public void delete(Object obj)
	{
		if(obj==null) return;// ż�������
		User user=(User)obj;
		if(user.getPlayerId()!=0) return;//�Ѿ��󶨽�ɫ���˺Ų���ɾ��
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.delete(FieldKit.create("id",((User)user)
			.getId()));
		if(log.isInfoEnabled()) log.info("delete, "+t+" "+user);
	}
	
	/** ͨ���˺��� ��ȡuser */
	public User loadUser(String userAccount)
	{
		// ����һ�����򣨰�����player���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit
			.create("user_account",userAccount),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"UserGameDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		User user=mapping(fields);
		return user;
	}

	/** ͨ���˺��� ��ȡuser(�Ϸ���ķ��������ܳ��ֵ��˺Ŷ��ɫ) */
	public User[] loadUsersByAccount(String userAccount)
	{
		String sql="select * from users where user_account='"+userAccount+"'";
		return loadBySql(sql);
	}
	
	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[21];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("player_id",0);
		array[i++]=FieldKit.create("playerIds",(String)null);
		array[i++]=FieldKit.create("create_at",0);
		array[i++]=FieldKit.create("login_time",0);
		array[i++]=FieldKit.create("deleteTime",0);
		array[i++]=FieldKit.create("userType",0);
		array[i++]=FieldKit.create("banned",0);
		array[i++]=FieldKit.create("bannedDevice",(String)null);
		array[i++]=FieldKit.create("udid",(String)null);
		
		array[i++]=FieldKit.create("login_udid",(String)null);
		array[i++]=FieldKit.create("user_account",(String)null);
		array[i++]=FieldKit.create("password",(String)null);
		array[i++]=FieldKit.create("email",(String)null);
		array[i++]=FieldKit.create("device",(String)null);
		array[i++]=FieldKit.create("osInfo",(String)null);
		array[i++]=FieldKit.create("idfa",(String)null);
		array[i++]=FieldKit.create("version",(String)null);
		array[i++]=FieldKit.create("plat",(String)null);
		array[i++]=FieldKit.create("pdid",(String)null);
		array[i++]=FieldKit.create("bindingTel",(String)null);
		

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public User mapping(Fields fields)
	{
		User user=new User();
		int id=((IntField)fields.get("id")).value;
		user.setId(id);
		int playerId=((IntField)fields.get("player_id")).value;
		user.setPlayerId(playerId);
		String playerIds=((StringField)fields.get("playerIds")).value;
		user.setPlayerIdsString(playerIds);
		int userType=((IntField)fields.get("userType")).value;
		user.setUserType(userType);
		int createAt=((IntField)fields.get("create_at")).value;
		user.setCreateAt(createAt);
		int loginTime=((IntField)fields.get("login_time")).value;
		user.setLoginTime(loginTime);
		int deleteTime=((IntField)fields.get("deleteTime")).value;
		user.setDeleteTime(deleteTime);
		int banned=((IntField)fields.get("banned")).value;
		user.setBanned(banned);
		String bannedDevice=((StringField)fields.get("bannedDevice")).value;
		user.setBannedDevice(bannedDevice);
		String udid=((StringField)fields.get("udid")).value;
		user.setCreateUdid(udid);
		String loginUdid=((StringField)fields.get("login_udid")).value;
		user.setLoginUdid(loginUdid);
		String userAccount=((StringField)fields.get("user_account")).value;
		user.setUserAccount(userAccount);
		String password=((StringField)fields.get("password")).value;
		user.setPassword(password);
		String email=((StringField)fields.get("email")).value;
		user.setEmail(email);
		String device=((StringField)fields.get("device")).value;
		user.setDevice(device);
		String osInfo=((StringField)fields.get("osInfo")).value;
		user.setOsInfo(osInfo);
		String idfa=((StringField)fields.get("idfa")).value;
		user.setIdfa(idfa);
		String version=((StringField)fields.get("version")).value;
		user.setVersion(version);
		String plat=((StringField)fields.get("plat")).value;
		user.setPlat(plat);
		String pdid=((StringField)fields.get("pdid")).value;
		user.setPayUdid(pdid);
		String bindingTel=((StringField)fields.get("bindingTel")).value;
		user.setBindingTel(bindingTel);
		return user;
	}

	/** ���淽�� ʹ�ð���������ݵ��ֽ������ʼ��ָ����������Ҳ����� */
	public boolean save(int id,Object user)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+user.toString());
		try
		{
			// int offset=data.offset();
			// FightEvent fightEvent=new FightEvent();
			// fightEvent.bytesRead(data);
			// data.setOffset(offset);
			return save(user);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** ���淽�� �����Ƿ�����ɹ� */
	public boolean save(Object user)
	{
		if(user==null) return false;// ż�������
		// ���������ӳ��������������ݿ���
		int t=gamePersistence.set(
			FieldKit.create("id",((User)user).getId()),mapping(user));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+user);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ӳ�������� */
	public Fields mapping(Object u)
	{
		User user=(User)u;
		FieldObject[] array=new FieldObject[21];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",user.getId());
		array[i++]=FieldKit.create("player_id",user.getPlayerId());
		array[i++]=FieldKit.create("playerIds",user.getPlayerIdsString());
		array[i++]=FieldKit.create("userType",user.getUserType());
		array[i++]=FieldKit.create("create_at",user.getCreateAt());
		array[i++]=FieldKit.create("login_time",user.getLoginTime());
		array[i++]=FieldKit.create("deleteTime",user.getDeleteTime());
		array[i++]=FieldKit.create("udid",user.getCreateUdid());
		array[i++]=FieldKit.create("login_udid",user.getLoginUdid());
		array[i++]=FieldKit.create("user_account",user.getUserAccount());
		array[i++]=FieldKit.create("password",user.getPassword());
		array[i++]=FieldKit.create("email",user.getEmail());
		array[i++]=FieldKit.create("banned",user.getBanned());
		array[i++]=FieldKit.create("bannedDevice",user.getBannedDevice());
		array[i++]=FieldKit.create("device",user.getDevice());
		array[i++]=FieldKit.create("osInfo",user.getOsInfo());
		array[i++]=FieldKit.create("idfa",user.getIdfa());
		array[i++]=FieldKit.create("version",user.getVersion());
		array[i++]=FieldKit.create("plat",user.getPlat());
		array[i++]=FieldKit.create("pdid",user.getPayUdid());
		array[i++]=FieldKit.create("bindingTel",user.getBindingTel());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	
	/**�ı䵱ǰ��ҵ��˺ŵ�ǰ׺**/
	public boolean updatePrefixUser(User user)
	{
		user.setUserAccount(User.USER_PREFIX_NAME
			+user.getUserAccount());
		return save(user);
	}
	
	/**���õ�ǰ��ҵ��˺ŵ�ɾ��״̬**/
	public boolean setUserState(User user,int time)
	{
		user.setDeleteTime(time);
		return save(user);
	}
	
	/**
	 * @return uidkit
	 */
	public UidKit getUidkit()
	{
		return uidkit;
	}

	/**
	 * @param uidkit Ҫ���õ� uidkit
	 */
	public void setUidkit(UidKit uidkit)
	{
		this.uidkit=uidkit;
	}
	
}
