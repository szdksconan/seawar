package foxu.sea;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.util.TimeKit;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.bind.BindingTrack;
import foxu.sea.kit.SeaBackKit;

/****
 * 接收从中心的数据 (中心群发到各个服务器)
 * 
 * @author lihongji
 * 
 */
public class CenterHttpToServer implements HttpHandlerInterface
{

	public static Logger log=LogFactory.getLogger(CenterHttpToServer.class);
	/** 默认的Base64编解码算法 */
	public static final Base64 BASE64=new Base64();

	/**
	 * 增加用户的前缀UPDATE_USER_INFO==1 USER_SET_TYPE=2 设置玩家删除状态
	 * PLAYER_NOT_INSULATE=3 玩家取消隔离
	 **/
	public static final int CHANGE_ACCOUNT=1,USER_SET_TYPE=2,
					CHANGE_PASSWORD=3,CHANGE_TEL=4;
	/** 使用的Base64编解码算法 */
	Base64 base64=BASE64;

	/** 创建对象管理器 */
	CreatObjectFactory objectFactory;

	@Override
	public byte[] excute(HttpRequestMessage request,String ip)
	{
		String stringData=request.getParameter("data");
		ByteBuffer data=load(stringData);
		int type=data.readUnsignedByte();
		ByteBuffer bb=new ByteBuffer();
		// 修改用户的删除状态
		if(type==USER_SET_TYPE)
		{
			String name=data.readUTF();
			User user=objectFactory.getUserDBAccess().loadUser(name);
			if(user==null)
				bb.writeBoolean(true);
			else
			{
				int time=data.readInt();
				objectFactory.getUserDBAccess().setUserState(user,time);
				deletePlayer(user,time);
				log.info("----name----+time--type----deleteTime---:"+name
					+"----"
					+SeaBackKit.formatDataTime(TimeKit.getSecondTime())
					+"-----"+type+"---deleteTime--"
					+(time==0?"0":SeaBackKit.formatDataTime(time)));
				bb.writeBoolean(true);
			}
		}
		else if(type==CHANGE_ACCOUNT)
		{
			String newAccount=data.readUTF();// 新的的账号
			String old_account=data.readUTF();// 旧的的账号
			String password=data.readUTF();
			String sql="SELECT * FROM users WHERE user_account='"
				+old_account+"'";
			User user[]=objectFactory.getUserDBAccess().loadBySql(sql);
			if(user==null||user.length==0)
				bb.writeBoolean(true);
			else
			{
				for(int i=0;i<user.length;i++)
				{
					changAccount(user[i],newAccount,password);
				}
				bb.writeBoolean(true);
			}
		}
		else if(type==CHANGE_PASSWORD)
		{
			String account=data.readUTF();
			String password=data.readUTF();
			String sql="SELECT * FROM users WHERE user_account='"+account
				+"'";
			User user[]=objectFactory.getUserDBAccess().loadBySql(sql);
			if(user==null||user.length==0)
				bb.writeBoolean(true);
			else
			{
				for(int i=0;i<user.length;i++)
				{
					if(user[i]!=null)
					{
						user[i].setPassword(password);
						objectFactory.getUserDBAccess().save(user[i]);
					}
				}
				bb.writeBoolean(true);
			}
		}
		else if(type==CHANGE_TEL)
		{
			String account=data.readUTF();
			String tel=data.readUTF();
			int source_server=data.readInt();
			User user=objectFactory.getUserDBAccess().loadUser(account);
			if(user==null)
				bb.writeBoolean(true);
			else
			{
				String lastRecord=user.getBindingTel();
				if("".equals(tel))
					tel=null;
				user.setBindingTel(tel);
				objectFactory.getUserDBAccess().save(user);
				bb.writeBoolean(true);
				objectFactory.createBindingTrack(BindingTrack.TELPHONE,
					BindingTrack.UPDATE_RECORD,BindingTrack.CENTER,
					user.getId(),source_server,tel,lastRecord,
					user.getBindingTel());
			}
		}
		return createBase64(bb).getBytes();
	}

	/**
	 * 根据key加载数据 单一key
	 * 
	 * @param key id
	 * @return 返回的ByteBuffer
	 */
	public ByteBuffer load(String data)
	{
		if(data==null||data.equals("null")) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		base64.decode(data,0,data.length(),bb);
		return bb;
	}

	/**
	 * Base64编解码算法 二进制数据转化为字符串
	 */
	public String createBase64(ByteBuffer data)
	{
		byte[] array=data.toArray();
		data.clear();
		base64.encode(array,0,array.length,data);
		return new String(data.getArray(),0,data.top());
	}

	/** 创建对象管理器 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	@Override
	public String excuteString(HttpRequestMessage request,String ip)
	{
		return null;
	}

	/** 改变玩家的状态 **/
	public void setPlayerState(int id)
	{
		Player player=objectFactory.getPlayerById(id);
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
			objectFactory.getUserDBAccess().save(user);
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

	/** 针对一个账号上的每一个玩家加上删除状态 **/
	public void deletePlayer(User user,int deleteTime)
	{
		if(user!=null)
		{
			if(user.getPlayerIds().length!=0)
			{
				int[] array=user.getPlayerIds();
				for(int j=0;j<array.length;j++)
				{
					deletePlayerState(array[j],deleteTime);
				}
			}
			else
				deletePlayerState(user.getPlayerId(),deleteTime);
		}
	}

	/** 添加状态 **/
	public void deletePlayerState(int id,int deleteTime)
	{
		Player player=objectFactory.getPlayerById(id);
		if(player!=null)
		{
			player.setDeleteTime(deleteTime);
			objectFactory.getPlayerCache().getDbaccess().save(player);
		}
	}
}
