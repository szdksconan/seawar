/**
 * 
 */
package foxu.cc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mustang.back.BackKit;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.net.DataAccessException;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import shelby.cc.DBCCAccess;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.User;
import foxu.sea.arena.ClearPlayerInfoManager;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;

/**
 * ��Ϸ��ʹ�õ����ݷ����� </p> ���û�����֤,���û��ʹ����û�
 * 
 * @author rockzyt
 */
public class GameDBCCAccess extends DBCCAccess
{

	/** ��Ϸ����IP��ַ */
	public static String GAME_CENTER_IP;
	/** ��Ϸ����HTTP�˿� */
	public static int GAME_CENTER_HTTP_PORT=9163;
	/**
	 * ����UID_LOGIN_SUCCESS���ٵ�¼�ɹ� ACCOUNT_LOGIN_SUCCESS�˺������¼�ɹ�
	 * LOGIN_PASSWORD_FAIL���벻�� USER_IS_FULL���ע�������� SAVE_USER_FAIL����userʧ��
	 * SAVE_USER_SUCCESS����user�ɹ� LOGIN_LIMIT=12 ��¼����
	 */
	public final static int UID_LOGIN_SUCCESS=1,ACCOUNT_LOGIN_SUCCESS=2,
					LOGIN_PASSWORD_FAIL=3,USER_IS_FULL=4,SAVE_USER_FAIL=5,
					SAVE_USER_SUCCESS=6,USER_NOT_EXIST=7,LOGIN_LIMIT=12;
	/** ��Ӫ���ĵ����뷵��type */
	public static final int PASSWORD_WRONG=1,PASSWORD_RIGHT=2;
	/**
	 * BIND_USER=1���û� CHANGE_PASSWORD=2�ı����� CHANGE_PRE_USERACCOUNT=11
	 * �����û���ǰ׺
	 */
	public static final int REGIST_USER=1,BIND_USER=2,CHANGE_PASSWORD=3,
					NEW_SERVER_LOGIN=4,PASSWORD_REGIST=5,
					CHANGE_PRE_USERACCOUNT=14,IS_DEVICE_UNLIMIT=10;
	/** һ����������ע������� */
	public static int ACCOUT_NUM=3;
	/* static fields */
	/** guest�˺ű�ʾ */
	public final static String GUEST="guest";
	/** ������֤��ʾ */
	public final static String UID_FLAG="@uid@",FACEBOOK_FLAG="@facebook@";
	/** �˺Žӹܱ�־ */
	public final static String SHARP="#";

	/***/

	/* fields */
	/** ��Ϸ���󴴽����� */
	CreatObjectFactory factory;

	String serverNotOpen=InterTransltor.getInstance().getTransByKey(
		PublicConst.SERVER_LOCALE,"server_is_not_open");

	Map<String,CertifyHandler> handlers=new HashMap<String,CertifyHandler>();

	/* properties */
	/** ������Ϸ���󴴽����� */
	public void setCreateObjectFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	public void setCertifyHandler(String name,CertifyHandler handler)
	{
		if(name==null||name.isEmpty()||handler==null) return;
		handlers.put(name,handler);
	}

	/* methods */

	/**
	 * ���ָ������Ƿ񱻽ӹ�״̬
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isTakeOver(int playerId)
	{
		Player player=factory.getPlayerById(playerId);
		if(player==null) return false;
		return player.isTakeOver();
	}

	/**
	 * �ӹܵ�¼�˺ţ���֤player���Ƿ�Ľӹܱ�־�����û���򲻽��нӹܵ�¼��֤����������¼����
	 * 
	 * @param id
	 * @param pwd
	 * @param address
	 * @return
	 */
	private ByteBuffer takeOverLogin(String id,String pwd,String address)
	{
		try
		{
			String str=id.substring(1);
			String[] strs=TextKit.split(str,"|");
			Player player=factory.getPlayerById(Integer.parseInt(strs[0]));
			if(player==null) return null;
			str=player.getAttributes(PublicConst.TAKE_OVER);
			if(str==null) return null;
			strs=TextKit.split(str,"|");
			if(pwd.equals(strs[0]))
			{
				ByteBuffer data=new ByteBuffer();
				User user=factory.getUserDBAccess().load(
					player.getUser_id()+"");
				writePlayerId(user,data);
				data.writeUTF(user.getUserAccount());
				data.writeUTF(user.getBannedDevice());
				writePlayerList(user,data);
				writeGuestPlayer(pwd,false,data);
				return data;
			}
			return null;
		}
		catch(Throwable t)
		{

		}
		return null;
	}

	private void writePlayerId(User user,ByteBuffer data)
	{
		if(user==null)
		{
			data.writeInt(0);
			return;
		}
		// // д�뵱ǰ��¼��ɫ,��������˺ŵ�¼����д������
		// if(user.getLockPlayerId()>0)
		// data.writeInt(user.getLockPlayerId());
		// else
		data.writeInt(user.getPlayerId());
	}
	/**
	 * ��֤��Ϣ�ĺ���Я����ɫ�б�
	 * 
	 * @param user
	 * @param data
	 */
	private void writePlayerList(User user,ByteBuffer data)
	{
		if(user==null||user.getPlayerIds()==null
			||user.getPlayerIds().length==0)
		{
			data.writeByte(0);
			return;
		}
		// д��
		int[] playerIds=user.getPlayerIds();
		data.writeByte(playerIds.length);
		for(int i=0;i<playerIds.length;i++)
		{
			Player player=factory.getPlayerById(playerIds[i]);
			if(player==null)
			{
				data.writeInt(0);
				continue;
			}
			data.writeInt(playerIds[i]);
			data.writeShort(player.getSid());
			data.writeShort(player.getLevel());
			data.writeUTF(player.getName());
		}
	}

	private void writeGuestPlayer(String udid,boolean needwrite,
		ByteBuffer data)
	{
		if(!needwrite)
		{
			data.writeBoolean(false);
			return;
		}
		String sql="SELECT * FROM users WHERE udid='"+udid+"'"
			+" AND userType="+User.GUEST+"";
		User user=factory.getUserDBAccess().loadUserBySql(sql);
		if(user==null||user.getPlayerId()==0)
		{
			data.writeBoolean(false);
		}
		else
		{
			Player player=factory.getPlayerById(user.getPlayerId());
			data.writeBoolean(true);
			data.writeInt(user.getPlayerId());
			data.writeShort(player.getSid());
			data.writeShort(player.getLevel());
			data.writeUTF(player.getName());
		}

	}

	/** У���ʺ����룬��������Ӧ�����ƣ�����null��ʾʧ�� id����˺� password������� udid���udid */
	public ByteBuffer valid(String id,String pwd,String address)
	{
		if(id.contains("'")||id.contains("\""))
			throw new DataAccessException(0,"valid id");
		String did=TextKit.split(id,"|")[1];
		// ���豸���
		if(factory.getForbidMemCache().isForbid(did))
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"connection_timeout"));
		// �׵����
		if(PublicConst.READY&&!factory.getForbidMemCache().isFree(did))
			throw new DataAccessException(0,"server_hold_on");
		// ����ǽӹ�״̬��־
		if(id.startsWith(SHARP))
		{
			ByteBuffer data=takeOverLogin(id,pwd,address);
			if(data!=null) return data;
		}
		ByteBuffer data=new ByteBuffer();
		// ������֤ ǰֻ̨����udid ��̨����guest�˺�
		boolean bool=true;
		if(id.startsWith(UID_FLAG))
		{
			data.clear();
			// pwdΪudid ������ֻ֤�ܵ�½����������guest�˺�
			String sql="SELECT * FROM users WHERE udid='"+pwd+"'"
				+" AND userType='"+User.GUEST+"'";
			User user=factory.getUserDBAccess().loadUserBySql(sql);
			if(user!=null)
			{
				if(PublicConst.LOGIN_LIMIT.equals("true"))
				{
					for(int i=0;i<PublicConst.USER_ID_LIMIT.length;i++)
					{
						if(user.getId()==PublicConst.USER_ID_LIMIT[i])
						{
							bool=false;
						}
					}
					if(bool) throw new DataAccessException(0,serverNotOpen);
				}
				if(user.getBanned()!=0)
				{
					throw new DataAccessException(0,"user is banned");
				}
				// ����֤�Ƿ��ڽӹ�״̬,����ǽӹ�״̬��ʱ��ʾ�˺ű�����
				if(user.getPlayerId()>0&&isTakeOver(user.getPlayerId()))
				{
					throw new DataAccessException(0,"user is banned");
				}

				creatPlayer(user);// ����Ĭ�����

				user.setLoginTime(TimeKit.getSecondTime());
				factory.getUserDBAccess().save(user);
				writePlayerId(user,data);
				data.writeUTF(user.getUserAccount());
				data.writeUTF(user.getBannedDevice());
				writePlayerList(user,data);
				writeGuestPlayer(pwd,false,data);
//				updatePlayerState(user,data);
			}
			else
			{
				if(PublicConst.LOGIN_LIMIT.equals("true"))
				{
					throw new DataAccessException(0,serverNotOpen);
				}
				// �鿴�ܷ���ʹ�ÿ����˺�
				sql="SELECT * FROM users WHERE udid='"+pwd+"'";
				User users[]=factory.getUserDBAccess().loadBySql(sql);
				data.writeByte(IS_DEVICE_UNLIMIT);
				data.writeUTF(pwd);
				// ��ȡ�豸�Ƿ��ڷ�����״̬
				boolean isUnlimited=sendHttpData(data).readBoolean();
				if(!isUnlimited&&users!=null&&users.length>=ACCOUT_NUM)
				{
					String str=InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"user_count_full");
					// ������������ ��������豸�󶨵��˺�
					for(int i=0;i<users.length;i++)
					{
						str=TextKit.replace(str,"%",
							users[i].getUserAccount());
					}
					throw new DataAccessException(0,str);
				}
				// ����һ��guest�˺� guest�˺Ų�����Ϸ���Ĵ��� �Ȼ�ȡuserId
				int userId=factory.getUserDBAccess().getUidkit()
					.getPlusUid();
				// ���ط���������
				String account=GameDBCCAccess.GUEST+":"+userId;
				factory.createGuestUser(pwd,userId,account);
				// ����Ĭ�����
				sql="SELECT * FROM users WHERE user_account='"+account+"'";
				user=factory.getUserDBAccess().loadUserBySql(sql);
				creatPlayer(user);

				data.clear();
				writePlayerId(user,data);
				data.writeUTF(account);
				data.writeUTF("");
				writePlayerList(user,data);
				writeGuestPlayer(pwd,false,data);
//				updatePlayerState(user,data);
				return data;
			}
		}
		else if(id.startsWith(FACEBOOK_FLAG))
		{
			return facebookLogin(id,pwd,address);
		}
		else if(id.indexOf('#')>0)
		{
			int index=id.indexOf('#');
			String platform=id.substring(0,index);
			id=id.substring(index+1);
			CertifyHandler handler=handlers.get(platform);
			if(handler==null)
				throw new DataAccessException(0,
					"cannot_support_this_platform");
			CertifyUser cu=handler.certify(id,pwd,address);
			if(cu==null)
				throw new DataAccessException(0,"user_or_password_error");
			return platformLogin(cu.getAccount(),pwd,address,data);
		}
		// �����˺���֤
		else
		{
			String str[]=TextKit.split(id,"|");
			if(!factory.getUserDBAccess().isExist(str[0],0))
			{
				if(PublicConst.LOGIN_LIMIT.equals("true"))
				{
					throw new DataAccessException(0,serverNotOpen);
				}
				data.clear();
				// ����idΪ��ҵ�udid
				data.writeByte(NEW_SERVER_LOGIN);
				data.writeUTF(str[0]);
				data.writeUTF(pwd);
				data.writeInt(UserToCenterPort.SERVER_ID);
				data=sendHttpData(data);
				if(data==null)
					throw new DataAccessException(0,"gamecenter login fail");
				int type=data.readUnsignedByte();
				// ��Ӫ�����ҵ��˺� �����˺����� �ڱ��ط����������˺�
				if(type==ACCOUNT_LOGIN_SUCCESS)
				{
					if(!factory.getUserDBAccess().isExist(str[0],0))
					{
						String tel=data.readUTF();
						factory.getUserDBAccess().createUser(str.length>1?str[1]:null,str[0],pwd,tel);
					}
				}
				else if(type==LOGIN_PASSWORD_FAIL)
				{
					throw new DataAccessException(0,"password is wrong");
				}
				else if(type==USER_NOT_EXIST)
				{
					throw new DataAccessException(0,"user not exist");
				}
				else if(type==LOGIN_LIMIT)
				{
					throw new DataAccessException(0,"login limit");
				}
				// ����Ĭ�����
				String sql="SELECT * FROM users WHERE user_account='"+str[0]
					+"'";
				User user=factory.getUserDBAccess().loadUserBySql(sql);
				creatPlayer(user);

				data.clear();
				writePlayerId(user,data);
				data.writeUTF(str[0]);
				data.writeUTF("");
				writePlayerList(user,data);
				// writeGuestPlayer(str.length>1?str[1]:null,str.length>1,data);
				writeGuestPlayer(str.length>1?str[1]:null,false,data);
				// User
				// user=factory.getUserDBAccess().loadUserBySql("select * from users where user_account='"+str[0]+"'");
				// updatePlayerState(user,data);
				return data;
			}
			User user=factory.getUserDBAccess().loadUser(str[0]);
			// �ڲ���Ա�ɽ�

			// �ڷ������ҵ����
			if(user!=null)
			{
				// ����֤�Ƿ��ڽӹ�״̬,����ǽӹ�״̬��ʱ��ʾ�˺ű�����
				if(isTakeOver(user.getPlayerId()))
				{
					throw new DataAccessException(0,"user is banned");
				}
				if(PublicConst.LOGIN_LIMIT.equals("true"))
				{
					for(int i=0;i<PublicConst.USER_ID_LIMIT.length;i++)
					{
						if(user.getId()==PublicConst.USER_ID_LIMIT[i])
						{
							bool=false;
						}
					}
					if(bool) throw new DataAccessException(0,serverNotOpen);
				}
				if(user.getBanned()!=0)
				{
					throw new DataAccessException(0,"user is banned");
				}
				// ����֤�Ƿ��ڽӹ�״̬,����ǽӹ�״̬��ʱ��ʾ�˺ű�����
				if(user.getPlayerId()>0&&isTakeOver(user.getPlayerId()))
				{
					throw new DataAccessException(0,"user is banned");
				}
				if(pwd.equalsIgnoreCase(user.getPassword()))
				{
					// ����Ĭ�����
					creatPlayer(user);

					data.clear();
					user.setLoginTime(TimeKit.getSecondTime());
					factory.getUserDBAccess().save(user);
					writePlayerId(user,data);
					data.writeUTF(user.getUserAccount());
					data.writeUTF(user.getBannedDevice());
					writePlayerList(user,data);
					// writeGuestPlayer(str.length>1?str[1]:null,str.length>1&&user.getPlayerId()==0,data);
					writeGuestPlayer(str.length>1?str[1]:null,false,data);
//					updatePlayerState(user,data);
					// �ж��Ƿ���ɾ��״̬
					// if(user.getDeleteTime()>0)
					// updatePlayerState(user,data);
					return data;
				}
				// ������� ����Ӫƽ̨ͨ��
				else
				{
					// ��Ӫƽ̨����������ȷ �޸ı��ط�����������
					data.clear();
					data.writeByte(PASSWORD_REGIST);
					data.writeUTF(str[0]);
					data.writeUTF(pwd);
					data=sendHttpData(data);
					int type=data.readUnsignedByte();
					if(type==PASSWORD_RIGHT)
					{
						// ����Ĭ�����
						creatPlayer(user);

						data.clear();
						user.setPassword(pwd);
						user.setLoginTime(TimeKit.getSecondTime());
						factory.getUserDBAccess().save(user);
						writePlayerId(user,data);
						data.writeUTF(user.getUserAccount());
						data.writeUTF(user.getBannedDevice());
						writePlayerList(user,data);
						// writeGuestPlayer(str.length>1?str[1]:null,str.length>1&&user.getPlayerId()==0,data);
						writeGuestPlayer(str.length>1?str[1]:null,false,data);
						// updatePlayerState(user,data);
						return data;
					}
					else
					{
						throw new DataAccessException(0,"password is wrong");
					}
				}
			}
		}
		return data;
	}
	public ByteBuffer sendHttpData(ByteBuffer data)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// ����port
		map.put("port","1");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GAME_CENTER_IP+":"
				+GAME_CENTER_HTTP_PORT+"/","POST",map,null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}

	private ByteBuffer platformLogin(String id,String pwd,String address,
		ByteBuffer data)
	{
		data.clear();
		// ������֤ ǰֻ̨����udid ��̨����guest�˺�
		boolean bool=true;
		String str[]=TextKit.split(id,"|");
		String sql="SELECT * FROM users WHERE user_account='"+str[0]+"'";
		User user=factory.getUserDBAccess().loadUserBySql(sql);
		if(user!=null)
		{
			if(PublicConst.LOGIN_LIMIT.equals("true"))
			{
				for(int i=0;i<PublicConst.USER_ID_LIMIT.length;i++)
				{
					if(user.getId()==PublicConst.USER_ID_LIMIT[i])
					{
						bool=false;
					}
				}
				if(bool) throw new DataAccessException(0,serverNotOpen);
			}
			if(user.getBanned()!=0)
			{
				throw new DataAccessException(0,"user is banned");
			}
			// ����Ĭ�����
			creatPlayer(user);
			
			user.setLoginTime(TimeKit.getSecondTime());
			factory.getUserDBAccess().save(user);
			writePlayerId(user,data);
			data.writeUTF(user.getUserAccount());
			data.writeUTF(user.getBannedDevice());
			writePlayerList(user,data);
			writeGuestPlayer(str.length>1?str[1]:null,
				str.length>1&&user.getPlayerId()==0,data);
		}
		else
		{
			if(PublicConst.LOGIN_LIMIT.equals("true"))
			{
				throw new DataAccessException(0,serverNotOpen);
			}
			// ���ط���������
			String account=str[0];
			// factory.createGuestUser(pwd,userId,account);
			factory.getUserDBAccess().createUser(str.length>1?str[1]:null,
				str[0],pwd,null);
			
			// ����Ĭ�����
			sql="SELECT * FROM users WHERE user_account='"+account+"'";
			user=factory.getUserDBAccess().loadUserBySql(sql);
			creatPlayer(user);
						
			data.clear();
			writePlayerId(user,data);
			data.writeUTF(account);
			data.writeUTF("");
			writePlayerList(user,data);
			writeGuestPlayer(str.length>1?str[1]:null,str.length>1,data);
		}
		return data;
	}

	private ByteBuffer facebookLogin(String id,String pwd,String address)
	{
		ByteBuffer data=new ByteBuffer();
		// ������֤ ǰֻ̨����udid ��̨����guest�˺�
		boolean bool=true;
		id=id.substring(FACEBOOK_FLAG.length());
		String str[]=TextKit.split(id,"|");
		String sql="SELECT * FROM users WHERE user_account='"+str[0]+"'";
		User user=factory.getUserDBAccess().loadUserBySql(sql);
		if(user!=null)
		{
			if(PublicConst.LOGIN_LIMIT.equals("true"))
			{
				for(int i=0;i<PublicConst.USER_ID_LIMIT.length;i++)
				{
					if(user.getId()==PublicConst.USER_ID_LIMIT[i])
					{
						bool=false;
					}
				}
				if(bool) throw new DataAccessException(0,serverNotOpen);
			}
			if(user.getBanned()!=0)
			{
				throw new DataAccessException(0,"user is banned");
			}
			// ����Ĭ�����
			creatPlayer(user);

			user.setLoginTime(TimeKit.getSecondTime());
			factory.getUserDBAccess().save(user);
			writePlayerId(user,data);
			data.writeUTF(user.getUserAccount());
			data.writeUTF(user.getBannedDevice());
			writePlayerList(user,data);
			writeGuestPlayer(str.length>1?str[1]:null,
				str.length>1&&user.getPlayerId()==0,data);
		}
		else
		{
			if(PublicConst.LOGIN_LIMIT.equals("true"))
			{
				throw new DataAccessException(0,serverNotOpen);
			}
			// ���ط���������
			String account=str[0];
			// factory.createGuestUser(pwd,userId,account);
			factory.getUserDBAccess().createUser(str.length>1?str[1]:null,
				str[0],pwd,null);

			// ����Ĭ�����
			sql="SELECT * FROM users WHERE user_account='"+account+"'";
			user=factory.getUserDBAccess().loadUserBySql(sql);
			creatPlayer(user);

			data.clear();
			writePlayerId(user,data);
			data.writeUTF(account);
			data.writeUTF("");
			writePlayerList(user,data);
			writeGuestPlayer(str.length>1?str[1]:null,str.length>1,data);
		}
		return data;
	}

	/** �ı���ҵ�״̬ **/
	public void updatePlayerState(User user,ByteBuffer data)
	{
		if(user==null||user.getUserType()==User.GUEST
			||user.getDeleteTime()==0)
		{
			data.writeInt(0);
			return;
		}
		int time=TimeKit.getSecondTime()
			-(user.getDeleteTime()+(int)(SeaBackKit.WEEK_MILL_TIMES/1000));
		if(time>0)
		{
			factory.getUserDBAccess().updatePrefixUser(user);
			ClearPlayerInfoManager manager=(ClearPlayerInfoManager)BackKit
				.getContext().get("clearplayerManager");
			int[] playerIds=user.getPlayerIds();
			if(user.getPlayerIds()==null||user.getPlayerIds().length==0)
			{
				playerIds=new int[1];
				playerIds[0]=user.getPlayerId();
			}
			manager.deletePlayerInfo(playerIds);
			throw new DataAccessException(0,"user not exist");
		}
		data.writeInt(((int)SeaBackKit.WEEK_MILL_TIMES/1000)
			-(TimeKit.getSecondTime()-user.getDeleteTime()));
	}

	/** ����Ĭ����� */
	public String creatPlayer(User user)
	{
		if(user==null||user.getPlayerId()>0) return null;
		String userAccount=user.getUserAccount();
		int roleSid=MathKit.randomValue(1,3);
		synchronized(factory.getIslandCache())
		{
			NpcIsland island=factory.getIslandCache().getRandomSpace();
			if(island==null) return "The world is full";
			// �������ʧ�ܣ�����ɫ�Ƿ����
			String playname=factory.createPlayer(userAccount,null,roleSid);
			if(playname==null)
			{
				throw new DataAccessException(0,"name has been used");
			}
			Player p=factory.getPlayerByName(playname,true);
			if(p==null)
				throw new DataAccessException(0,"create role error");
			user.setPlayerId(p.getId());
			island.setPlayerId(p.getId());
			factory.getIslandCache().getDbaccess().save(island);
			factory.getIslandCache().addPlayerIsLandMap(island);
			factory.getIslandCache().removeSpaceIsland(island);
		}
		return null;
	}

}
