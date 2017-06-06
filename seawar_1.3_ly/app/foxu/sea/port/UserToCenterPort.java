package foxu.sea.port;

import java.io.IOException;
import java.util.HashMap;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import shelby.cc.CCManager;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.PlayerGameDBAccess;
import foxu.dcaccess.UserGameDBAccess;
import foxu.ds.PlayerKit;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.User;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.task.TaskEventExecute;

/** 绑定账号 注册账号 1004 */
public class UserToCenterPort extends AccessPort
{

	/** 账号的长度限制 */
	public static final int USER_ACCOUNT_LENGTH=50,PASSWORD_MAX_LENGTH=32;
	/** email正式表达式 */
	//public static final String EMAIL_MATCHES="\\w+(\\.\\w+)*@\\w+(\\.\\w+)+";
	public static final String EMAIL_MATCHES="^[a-zA-Z0-9.!#$%&*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
	/**
	 * REGIST_USER=1注册用户 BIND_USER=绑定用户 CHANGE_PASSWORD=2改变密码
	 * 绑定账号成功BIND_USER_SUCCESS=8 CHANGE_ACCOUNT=11 修改账号
	 */
	public static final int REGIST_USER=1,BIND_USER=2,CHANGE_PASSWORD=3,
					USER_IS_FULL=4,USER_NOT_EXIST=7,BIND_USER_SUCCESS=8,
					BIND_USER_FAIL=9,BIND_FACEBOOK_USER=10,CHANGE_ACCOUNT=11;
	/** 平台id */
	public static int PLAT_ID=2;
	/** 本服务器ID */
	public static int SERVER_ID=114;
	/** 区域ID */
	public static int AREA_ID=1;
	/**平台**/
	public static final int HTTP_ACCOUNT=14;
	
	/** 本服务器的名字对应的翻译 */
	public static String SERVER_NAME;
	/** 各个服务器对应的翻译 */
	public static String SERVER_NAME_LIST[];
	/* fields */
	UserGameDBAccess dbaccess;

	CCManager ccmanager;

	/** 游戏对象创建工厂 */
	CreatObjectFactory factory;

	/* properties */
	/** 设置游戏对象创建工厂 */
	public void setCreateObjectFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
//		if(connect.getSource()==null)
//		{
//			connect.close();
//			return null;
//		}
//		Session s=(Session)(connect.getSource());
//		Player player=PlayerKit.getInstance().getPlayer(s);
//		if(player==null)
//		{
//			throw new DataAccessException(0,"player is null");
//		}
		int type=data.readUnsignedByte();
		// 注册用户 只能在游戏中
		// if(type==REGIST_USER)
		// {
		// /** 账号（邮箱格式） */
		// String account=data.readUTF();
		// // 密码
		// String password=data.readUTF();
		// // 设备udid
		// String udid=data.readUTF();
		// String str=checkUser(account,password,udid);
		// if(str!=null) throw new DataAccessException(0,str);
		// }
		// 绑定用户 从guest绑定为正式用户
		if(type==BIND_USER)
		{
			/** 账号（邮箱格式） */
			String account=data.readUTF();
			// 密码
			String password=data.readUTF();
			// 设备udid
			String udid=data.readUTF();
			String sid=data.readUTF();
			// 邀请者玩家id
			String invetedStr=data.readUTF();
			if(udid==null||udid.length()==0)
				throw new DataAccessException(0, "udid_error") ;
			// 找到对应的游客号
			String sql="select * from users where udid='"+udid+"' and userType="+User.GUEST;
			User[] users=factory.getUserDBAccess().loadBySql(sql);
			if(users==null||users.length!=1||users[0].getPlayerId()==0)
				throw new DataAccessException(0, "user_error") ;
			Player player=factory.getPlayerById(users[0].getPlayerId());
			if(player==null)
				throw new DataAccessException(0, "player is null") ;
			String str=checkUser(account,password,udid);
			if(str!=null)
				throw new DataAccessException(0,InterTransltor.getInstance()
					.getTransByKey(player.getLocale(),str));
			int inveted=0;
			
			try
			{
				inveted=Integer.parseInt(invetedStr);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new DataAccessException(0,"inveted is wrong");
			}
			if(inveted>0)
			{
				if(inveted==player.getId())
				{
					throw new DataAccessException(0,
						"inveted can not yourself");
				}
				// 邀请者是否属于本服id
				int serverInveteId=inveted>>24;
				boolean bool=((PlayerGameDBAccess)factory.getPlayerCache()
					.getDbaccess()).isExistByID(inveted);
				if(serverInveteId!=SERVER_ID&&!bool)
				{
					String back=InterTransltor.getInstance().getTransByKey(
						player.getLocale(),"the invetid is");
//					String serverName="测试服";
//					for(int i=0;i<SERVER_NAME_LIST.length;i+=2)
//					{
//						if(serverInveteId==Integer
//							.parseInt(SERVER_NAME_LIST[i]))
//						{
//							serverName=SERVER_NAME_LIST[i+1];
//						}
//					}
//					back=TextKit.replace(back,"%",InterTransltor
//						.getInstance().getTransByKey(player.getLocale(),
//							serverName));
					throw new DataAccessException(0,InterTransltor
						.getInstance()
						.getTransByKey(player.getLocale(),back));
				}
				if(!bool)
					throw new DataAccessException(0,"invetid is not exist");
			}
			// 查看该设备udid的guest账号 没有就不能绑定
			sql="SELECT * FROM users WHERE udid='"+udid+"'"
				+" AND userType='"+User.GUEST+"'";
			User user=dbaccess.loadUserBySql(sql);
			if(user==null)
				throw new DataAccessException(0,"udid for user is null");
			// 向用户中心通信创建账号
			data.clear();
			data.writeByte(BIND_USER);
			data.writeUTF(account);
			data.writeUTF(password);
			data.writeUTF(udid);
			data.writeByte(SERVER_ID);
			data.writeByte(player.getLocale());
			data.writeBoolean(false);//非Gm
			data.writeByte(PublicConst.GAME_BINDING);
			data=sendHttpData(data);
			if(data==null)
			{
				throw new DataAccessException(0,InterTransltor.getInstance()
					.getTransByKey(player.getLocale(),"net is wrong"));
			}
			type=data.readUnsignedByte();
			// 绑定账号成功
			if(type==BIND_USER_SUCCESS)
			{
				deleteUser(account);
				user.setUserAccount(account);
				user.setPassword(password);
				user.setUserType(User.USER);
				user.setLoginTime(TimeKit.getSecondTime());
				data.clear();
				if(dbaccess.save(user))
				{
					data.writeByte(BIND_USER_SUCCESS);
					ccmanager.resetSessionId(sid,account);
					// 绑定成功就有宝石
					player.addOneInviter(player.getId());
					// 设置自己的邀请者
					player.setInveted(inveted);
					player.getIsland().pushInviet(factory);
					player.setBindIp(connect.getURL().getHost());
					if(inveted>0)
					{
						// 对方加上
						Player inveter=(Player)factory.getPlayerCache()
							.load(inveted+"");
						if(inveter!=null)
						{
							inveter.addOneInviter(player.getId());
							inveter.getIsland().pushInviet(factory);
							//成就数据采集 
							AchieveCollect.inviteUser(inveter);
						}
					}
					//成就数据采集
					AchieveCollect.bindUser(player);
					//账号绑定任务
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.BIND_ACCOUNT_EVENT,null,player,user);
				}
				else
				{
					throw new DataAccessException(0,"account is have");
				}
			}
			else if(type==USER_IS_FULL)
			{
				ByteBuffer newData=new ByteBuffer();
				newData.writeByte(USER_IS_FULL);
				int length=data.readUnsignedByte();
				newData.writeByte(length);
				for(int i=0;i<length;i++)
				{
					newData.writeUTF(data.readUTF());
				}
				data=newData;
			}
			else
			{
				data.clear();
				data.writeByte(type);
			}
		}
		// 绑定facebook账号
		else if(type==BIND_FACEBOOK_USER)
		{
			// 账号 (facebook账号信息加密串)
			String account=data.readUTF();
			// 密码
			String password=data.readUTF();
			// 设备udid
			String udid=data.readUTF();
			String sid=data.readUTF();
			// 邀请者玩家id
			String invetedStr=data.readUTF();
			// 找到对应的游客号
			String sql="select * from users where udid='"+udid+"' and userType="+User.GUEST;
			User[] users=factory.getUserDBAccess().loadBySql(sql);
			if(users==null||users.length!=1||users[0].getPlayerId()==0)
				throw new DataAccessException(0, "user_error") ;
			Player player=factory.getPlayerById(users[0].getPlayerId());
			if(player==null)
				throw new DataAccessException(0, "player is null") ;
			String str=checkFacebookUser(account,password,udid);
			if(str!=null)
				throw new DataAccessException(0,InterTransltor.getInstance()
					.getTransByKey(player.getLocale(),str));
			int inveted=0;
			try
			{
				inveted=Integer.parseInt(invetedStr);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new DataAccessException(0,"inveted is wrong");
			}
			if(inveted>0)
			{
				if(inveted==player.getId())
				{
					throw new DataAccessException(0,
						"inveted can not yourself");
				}
				// 邀请者是否属于本服id
				int serverInveteId=inveted>>24;
				boolean bool=((PlayerGameDBAccess)factory.getPlayerCache()
					.getDbaccess()).isExistByID(inveted);
				if(serverInveteId!=SERVER_ID&&!bool)
				{
					String back=InterTransltor.getInstance().getTransByKey(
						player.getLocale(),"the invetid is");
					throw new DataAccessException(0,InterTransltor
						.getInstance()
						.getTransByKey(player.getLocale(),back));
				}
				if(!bool)
					throw new DataAccessException(0,"invetid is not exist");
			}
			// 查看该设备udid的guest账号 没有就不能绑定
//			sql="SELECT * FROM users WHERE login_udid='"+udid+"'"
//				+" AND userType='"+User.GUEST+"'";
//			User user=dbaccess.loadUserBySql(sql);
//			if(user==null)
//				throw new DataAccessException(0,"udid for user is null");
			deleteUser(account);
			User user=users[0];
			user.setUserAccount(account);
			user.setPassword(null);
			user.setUserType(User.USER);
			user.setLoginTime(TimeKit.getSecondTime());
			data.clear();
			if(dbaccess.save(user))
			{
				data.writeByte(BIND_USER_SUCCESS);
				ccmanager.resetSessionId(sid,account);
				// 绑定成功就有宝石
				player.addOneInviter(player.getId());
				// 设置自己的邀请者
				player.setInveted(inveted);
				player.getIsland().pushInviet(factory);
				player.setBindIp(connect.getURL().getHost());
				if(inveted>0)
				{
					// 对方加上
					Player inveter=(Player)factory.getPlayerCache()
						.load(String.valueOf(inveted));
					if(inveter!=null)
					{
						inveter.addOneInviter(player.getId());
						inveter.getIsland().pushInviet(factory);
						//成就数据采集 
						AchieveCollect.inviteUser(inveter);
					}
				}
				//成就数据采集 
				AchieveCollect.bindUser(player);
				//账号绑定任务
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.BIND_ACCOUNT_EVENT,null,player,user);
			}
			else
			{
				throw new DataAccessException(0,"account is have");
			}
		}
		// 改变密码
		else if(type==CHANGE_PASSWORD)
		{
			Session s=(Session)(connect.getSource());
			Player player=PlayerKit.getInstance().getPlayer(s);
			if(player==null)
			{
				throw new DataAccessException(0,"player is null");
			}
			// 修改本地和运营中心的
			// 运营中心修改成功继续 否则抛错
			String oldPwd=data.readUTF();
			String pwd=data.readUTF();
			User user=dbaccess.load(player.getUser_id()+"");
			if(user==null||user.getUserType()==User.GUEST)
			{
				throw new DataAccessException(0,"user is not exist");
			}
			// 判断本地服务器的密码对否
			if(!user.getPassword().equals(oldPwd))
			{
				throw new DataAccessException(0,"old password is wrong");
			}
			data.clear();
			data.writeByte(CHANGE_PASSWORD);
			data.writeUTF(user.getUserAccount());
			data.writeUTF(pwd);
			data.writeUTF(oldPwd);
			data.writeInt(TimeKit.getSecondTime());
			//大区
			data.writeInt(AREA_ID);
			// 是否为GM修改，这里要用false
			data.writeBoolean(false);
			data=sendHttpData(data);
			int typeReturn=data.readUnsignedByte();
			if(typeReturn==0)
			{
				user.setPassword(pwd);
				dbaccess.save(user);
			}
			else
			{
				throw new DataAccessException(0,"user is not exist");
			}
		}
		/**修改账号**/
		else if(type==CHANGE_ACCOUNT)
		{
			if(connect.getSource()==null)
			{
				connect.close();
				return null;
			}
			Session s=(Session)(connect.getSource());
			Player player=PlayerKit.getInstance().getPlayer(s);
			if(player==null)
			{
				throw new DataAccessException(0,"player is null");
			}
			String str=player.getAttributes(PublicConst.ACCOUNT_INFO);
			if(str==null || str.length()==0 || TextKit.parseInt(str.split(",")[0])!=1)  
			{
				data.clear();
				data.writeByte(BIND_USER_FAIL);
				return data;
			}
			String  account=data.readUTF();
			String password=data.readUTF();
			String sql="select * from users where user_account='"+account+"'";
			User[] users=factory.getUserDBAccess().loadBySql(sql);
			if(users!=null && users.length!=0)
				throw new DataAccessException(0,"account has aleady exist");
			User user=factory.getUserDBAccess().load(player.getUser_id()+"");
			if(user==null)
				throw new DataAccessException(0,"user is null");
			// 当前宝石数量是否足够
			if(!Resources.checkGems(PublicConst.MODIFY_A_COST_GEMS,
				player.getResources()))
				throw new DataAccessException(0,"gems limit");
			data.clear();
			data.writeByte(HTTP_ACCOUNT);
			data.writeUTF(account);
			data.writeUTF(user.getUserAccount());
			data.writeUTF(password);
			data.writeInt(TimeKit.getSecondTime());
			//大区
			data.writeInt(AREA_ID);
			data=sendHttpData(data);
			if(data==null)
			{
				throw new DataAccessException(0,InterTransltor.getInstance()
					.getTransByKey(player.getLocale(),"net is wrong"));
			}
			int result=data.readUnsignedByte();
			if(result==BIND_USER_SUCCESS)
			{
				// 扣除宝石
				Resources.reduceGems(PublicConst.MODIFY_A_COST_GEMS,
					player.getResources(),player);
				// 日志记录
				factory.createGemTrack(GemsTrack.MODIFY_USERACCOUNT,
					player.getId(),PublicConst.MODIFY_A_COST_GEMS,0,
					Resources.getGems(player.getResources()));
				changAccount(user,account,password);
			}
			data.clear();
			data.writeByte(result);
		}
		return data;
	}

	/** 返回null为成功 这里需要翻译 */
	public String checkUser(String account,String password,String udid)
	{
		/** 邮箱格式 */
		if(account.getBytes().length<=0
			||account.getBytes().length>USER_ACCOUNT_LENGTH)
			return "account max length is 50";
		// // 密码
		// if(password.getBytes().length<=0
		// ||password.getBytes().length>=PASSWORD_MAX_LENGTH)
		// return "password max length is 20";
		String sql="select * from users where user_account='"+account+"'";
		User[] users=factory.getUserDBAccess().loadBySql(sql);
		if(users!=null&&(users.length!=1||users[0].getPlayerId()!=0))
			throw new DataAccessException(0, "account is have") ;
		// 设备udid
		if(udid==null||udid.equals("")) return "udid is wrong";
		if(!account.matches(EMAIL_MATCHES)) return "account is wrong";
		// 查看本地游戏数据库账号名
		//if(dbaccess.isExist(account,0)) return "account is have";
		return null;
	}
	
	/**
	 * 
	 * @param account
	 * @param password
	 * @param udid
	 * @return
	 */
	private String checkFacebookUser(String account,String password,String udid)
	{
		String sql="select * from users where user_account='"+account+"'";
		User[] users=factory.getUserDBAccess().loadBySql(sql);
		if(users!=null&&(users.length!=1||users[0].getPlayerId()!=0))
			throw new DataAccessException(0, "account is have") ;
//		// 查看本地游戏数据库账号名
//		if(dbaccess.isExist(account,0)) return "account is have";
		return null;
	}
	
	/**
	 * 删除指定账号的user，user上的playerId为0才能删除
	 * @param account
	 */
	private String deleteUser(String account)
	{
		String sql="select * from users where user_account='"+account+"'";
		User[] users=factory.getUserDBAccess().loadBySql(sql);

		if(users==null||users.length==0)
			return null;
		if(users.length!=1)
			return "account error";
		User user=users[0];
		if(user.getPlayerId()!=0)
			return "account is have";
		factory.getUserDBAccess().delete(user);
		return null;
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
			re=request.send(
				"http://"+GameDBCCAccess.GAME_CENTER_IP+":"+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,
				null);
		}
		catch(IOException e)
		{
			// TODO 自动生成 catch 块
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}

	/** 改变玩家的状态 **/
	public void setPlayerState(int id)
	{
		Player player=factory.getPlayerById(id);
		if(player!=null)
		{
			player.setAttribute(PublicConst.ACCOUNT_TIME,
				TimeKit.getSecondTime()+"");
			player.setAttribute(PublicConst.ACCOUNT_INFO,"");
		}
	}

	/** 修改账号 **/
	public void changAccount(User user,String newAccount,String password)
	{
		if(user!=null)
		{
			user.setUserAccount(newAccount);
			user.setPassword(password);
			factory.getUserDBAccess().save(user);
			// 当前玩家所有角色的解锁时间设置为7天
			if(user.getPlayerIds().length!=0)
			{
				int[] array=user.getPlayerIds();
				for(int j=0;j<array.length;j++)
				{
					setPlayerState(array[j]);
				}
			}
			else
				setPlayerState(user.getPlayerId());
		}
	}
	
	/**
	 * @return dbaccess
	 */
	public UserGameDBAccess getDbaccess()
	{
		return dbaccess;
	}

	/**
	 * @param dbaccess 要设置的 dbaccess
	 */
	public void setDbaccess(UserGameDBAccess dbaccess)
	{
		this.dbaccess=dbaccess;
	}

	/**
	 * @return ccmanager
	 */
	public CCManager getCcmanager()
	{
		return ccmanager;
	}

	/**
	 * @param ccmanager 要设置的 ccmanager
	 */
	public void setCcmanager(CCManager ccmanager)
	{
		this.ccmanager=ccmanager;
	}
}
