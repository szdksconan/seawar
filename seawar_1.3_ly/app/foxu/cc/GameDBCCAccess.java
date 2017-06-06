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
 * 游戏中使用的数据访问类 </p> 有用户就认证,无用户就创建用户
 * 
 * @author rockzyt
 */
public class GameDBCCAccess extends DBCCAccess
{

	/** 游戏中心IP地址 */
	public static String GAME_CENTER_IP;
	/** 游戏中心HTTP端口 */
	public static int GAME_CENTER_HTTP_PORT=9163;
	/**
	 * 常量UID_LOGIN_SUCCESS快速登录成功 ACCOUNT_LOGIN_SUCCESS账号密码登录成功
	 * LOGIN_PASSWORD_FAIL密码不对 USER_IS_FULL玩家注册量已满 SAVE_USER_FAIL创建user失败
	 * SAVE_USER_SUCCESS创建user成功 LOGIN_LIMIT=12 登录受限
	 */
	public final static int UID_LOGIN_SUCCESS=1,ACCOUNT_LOGIN_SUCCESS=2,
					LOGIN_PASSWORD_FAIL=3,USER_IS_FULL=4,SAVE_USER_FAIL=5,
					SAVE_USER_SUCCESS=6,USER_NOT_EXIST=7,LOGIN_LIMIT=12;
	/** 运营中心的密码返回type */
	public static final int PASSWORD_WRONG=1,PASSWORD_RIGHT=2;
	/**
	 * BIND_USER=1绑定用户 CHANGE_PASSWORD=2改变密码 CHANGE_PRE_USERACCOUNT=11
	 * 增加用户的前缀
	 */
	public static final int REGIST_USER=1,BIND_USER=2,CHANGE_PASSWORD=3,
					NEW_SERVER_LOGIN=4,PASSWORD_REGIST=5,
					CHANGE_PRE_USERACCOUNT=14,IS_DEVICE_UNLIMIT=10;
	/** 一个服务器能注册的数量 */
	public static int ACCOUT_NUM=3;
	/* static fields */
	/** guest账号标示 */
	public final static String GUEST="guest";
	/** 快速认证标示 */
	public final static String UID_FLAG="@uid@",FACEBOOK_FLAG="@facebook@";
	/** 账号接管标志 */
	public final static String SHARP="#";

	/***/

	/* fields */
	/** 游戏对象创建工厂 */
	CreatObjectFactory factory;

	String serverNotOpen=InterTransltor.getInstance().getTransByKey(
		PublicConst.SERVER_LOCALE,"server_is_not_open");

	Map<String,CertifyHandler> handlers=new HashMap<String,CertifyHandler>();

	/* properties */
	/** 设置游戏对象创建工厂 */
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
	 * 检查指定玩家是否被接管状态
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
	 * 接管登录账号，验证player上是否的接管标志，如果没有则不进行接管登录验证，走正常登录流程
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
		// // 写入当前登录角色,如果锁定账号登录，则写入锁定
		// if(user.getLockPlayerId()>0)
		// data.writeInt(user.getLockPlayerId());
		// else
		data.writeInt(user.getPlayerId());
	}
	/**
	 * 认证消息的后面携带角色列表
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
		// 写入
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

	/** 校验帐号密码，返回所对应的名称，返回null表示失败 id玩家账号 password玩家密码 udid玩家udid */
	public ByteBuffer valid(String id,String pwd,String address)
	{
		if(id.contains("'")||id.contains("\""))
			throw new DataAccessException(0,"valid id");
		String did=TextKit.split(id,"|")[1];
		// 封设备检测
		if(factory.getForbidMemCache().isForbid(did))
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"connection_timeout"));
		// 白单检测
		if(PublicConst.READY&&!factory.getForbidMemCache().isFree(did))
			throw new DataAccessException(0,"server_hold_on");
		// 如果是接管状态标志
		if(id.startsWith(SHARP))
		{
			ByteBuffer data=takeOverLogin(id,pwd,address);
			if(data!=null) return data;
		}
		ByteBuffer data=new ByteBuffer();
		// 快速认证 前台只发送udid 后台创建guest账号
		boolean bool=true;
		if(id.startsWith(UID_FLAG))
		{
			data.clear();
			// pwd为udid 快速认证只能登陆本机创建的guest账号
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
				// 　验证是否在接管状态,如果是接管状态暂时提示账号被锁定
				if(user.getPlayerId()>0&&isTakeOver(user.getPlayerId()))
				{
					throw new DataAccessException(0,"user is banned");
				}

				creatPlayer(user);// 创建默认玩家

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
				// 查看能否还能使用快速账号
				sql="SELECT * FROM users WHERE udid='"+pwd+"'";
				User users[]=factory.getUserDBAccess().loadBySql(sql);
				data.writeByte(IS_DEVICE_UNLIMIT);
				data.writeUTF(pwd);
				// 获取设备是否处于非限制状态
				boolean isUnlimited=sendHttpData(data).readBoolean();
				if(!isUnlimited&&users!=null&&users.length>=ACCOUT_NUM)
				{
					String str=InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"user_count_full");
					// 超过数量限制 返回这个设备绑定的账号
					for(int i=0;i<users.length;i++)
					{
						str=TextKit.replace(str,"%",
							users[i].getUserAccount());
					}
					throw new DataAccessException(0,str);
				}
				// 创建一个guest账号 guest账号不在游戏中心创建 先获取userId
				int userId=factory.getUserDBAccess().getUidkit()
					.getPlusUid();
				// 本地服务器创建
				String account=GameDBCCAccess.GUEST+":"+userId;
				factory.createGuestUser(pwd,userId,account);
				// 创建默认玩家
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
		// 密码账号认证
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
				// 这里id为玩家的udid
				data.writeByte(NEW_SERVER_LOGIN);
				data.writeUTF(str[0]);
				data.writeUTF(pwd);
				data.writeInt(UserToCenterPort.SERVER_ID);
				data=sendHttpData(data);
				if(data==null)
					throw new DataAccessException(0,"gamecenter login fail");
				int type=data.readUnsignedByte();
				// 运营中心找到账号 返回账号密码 在本地服务器创建账号
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
				// 创建默认玩家
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
			// 内部人员可进

			// 在服务器找到玩家
			if(user!=null)
			{
				// 　验证是否在接管状态,如果是接管状态暂时提示账号被锁定
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
				// 　验证是否在接管状态,如果是接管状态暂时提示账号被锁定
				if(user.getPlayerId()>0&&isTakeOver(user.getPlayerId()))
				{
					throw new DataAccessException(0,"user is banned");
				}
				if(pwd.equalsIgnoreCase(user.getPassword()))
				{
					// 创建默认玩家
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
					// 判断是否是删除状态
					// if(user.getDeleteTime()>0)
					// updatePlayerState(user,data);
					return data;
				}
				// 密码错误 向运营平台通信
				else
				{
					// 运营平台返回密码正确 修改本地服务器的密码
					data.clear();
					data.writeByte(PASSWORD_REGIST);
					data.writeUTF(str[0]);
					data.writeUTF(pwd);
					data=sendHttpData(data);
					int type=data.readUnsignedByte();
					if(type==PASSWORD_RIGHT)
					{
						// 创建默认玩家
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
		// 设置port
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
		// 快速认证 前台只发送udid 后台创建guest账号
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
			// 创建默认玩家
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
			// 本地服务器创建
			String account=str[0];
			// factory.createGuestUser(pwd,userId,account);
			factory.getUserDBAccess().createUser(str.length>1?str[1]:null,
				str[0],pwd,null);
			
			// 创建默认玩家
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
		// 快速认证 前台只发送udid 后台创建guest账号
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
			// 创建默认玩家
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
			// 本地服务器创建
			String account=str[0];
			// factory.createGuestUser(pwd,userId,account);
			factory.getUserDBAccess().createUser(str.length>1?str[1]:null,
				str[0],pwd,null);

			// 创建默认玩家
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

	/** 改变玩家的状态 **/
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

	/** 创建默认玩家 */
	public String creatPlayer(User user)
	{
		if(user==null||user.getPlayerId()>0) return null;
		String userAccount=user.getUserAccount();
		int roleSid=MathKit.randomValue(1,3);
		synchronized(factory.getIslandCache())
		{
			NpcIsland island=factory.getIslandCache().getRandomSpace();
			if(island==null) return "The world is full";
			// 如果创建失败，检查角色是否存在
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
