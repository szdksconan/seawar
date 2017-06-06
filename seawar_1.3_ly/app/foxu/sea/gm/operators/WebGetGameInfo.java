package foxu.sea.gm.operators;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.CenterHttpToServer;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.User;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.WebConstant;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.port.UserToCenterPort;
import foxu.sea.proplist.AwardProp;
import foxu.sea.proplist.Prop;

/****
 *  web服务器获取游戏数据
 * @author lhj
 *
 */
public class WebGetGameInfo implements HttpHandlerInterface 
{

	public static Logger log=LogFactory.getLogger(CenterHttpToServer.class);
	/** 默认的Base64编解码算法 */
	public static final Base64 BASE64=new Base64();
	
	public static final int WEB_NEED_LEVE=20;
	/**GET_PLAYER_INFO=1获取玩家数据  SET_AWARD=给玩家发奖 SEND_AWARD_MESSAGE=5 发送邮件**/
	public  final int  GET_PLAYER_INFO=1,SET_AWARD=3,SEND_AWARD_MESSAGE=5;
	
	/** 使用的Base64编解码算法 */
	Base64 base64=BASE64;
	
	/** 创建对象管理器 */
	CreatObjectFactory objectFactory;
	
	int[] no_open={2010,2011,2012,2013};
	
	@Override
	public byte[] excute(HttpRequestMessage request,String ip)
	{
		return null;
	}

	@Override
	public String excuteString(HttpRequestMessage request,String arg1)
	{
		String stringData=request.getParameter("data");
		ByteBuffer data=load(stringData);
		int type=data.readUnsignedByte();
		JSONObject json=new JSONObject();
		//System.out.println("--------WebGetGameInfo-----type----:"+type);
		//获取玩家数据
		if(type==GET_PLAYER_INFO)
		{
			String account=data.readUTF();
			Player [] players=getPlayersByAccount(account);
			//System.out.println("--------players---------:"+players);
			try
			{
				JSONArray  jso=new JSONArray();
				if(players==null || players.length==0 || isablePlayerList(players))
				{
					//角色名称为空
					json.put(WebConstant.SUCCESS,WebConstant.ROLENAME_NULL);
					json.put(WebConstant.LENGTH,0);
					return json.toString();
				}
				for(int i=0;i<players.length;i++)
				{
					Player player=players[i];
					if(player.getLevel()<WEB_NEED_LEVE)
						continue;
					//获取玩家数据
					JSONObject baseInfo=getPlayerBaseInfo(player);
					jso.put(baseInfo);
				}
				//封装成json 
				json.put(WebConstant.PIONEER,jso);
				json.put(WebConstant.SUCCESS,GMConstant.ERR_SUCCESS);
				json.put(WebConstant.LENGTH,players.length);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		/**web 发奖**/
		else if(type==SET_AWARD)
		{
			//账号
			String account=data.readUTF();
			//玩家
			int player_id=data.readInt();
			//奖励id
			int award_id=data.readInt();
			try
			{
				// System.out.println(player_id+"=============player_id");
				Player player=objectFactory.getPlayerById(player_id);
				if(player==null)
				{
					json.put(WebConstant.SUCCESS,WebConstant.PLAYER_NAME_IS_NULL);
					return json.toString();
				}
				AwardProp prop=(AwardProp)Prop.factory.newSample(award_id);
				data.clear();
				if(SeaBackKit.isContainValue(no_open,prop.getSid()))
				{
					player.getBundle().incrProp(prop,true);
				}else
				{
					prop.use(player,objectFactory,data);
				}
				JBackKit.sendResetBunld(player);
				json.put(WebConstant.SUCCESS,GMConstant.ERR_SUCCESS);
				return json.toString();
			}
			catch(Exception e)
			{
				
			}
			
		}
		//发送邮件
		else if(type==SEND_AWARD_MESSAGE)
		{
			try
			{
				// 主题
				String title=data.readUTF();
				// 内容
				String content=data.readUTF();
				// 玩家id
				int player_id=data.readInt();
				Player player=objectFactory.getPlayerById(player_id);
				if(player==null)
				{
					json.put(WebConstant.SUCCESS,WebConstant.PLAYER_NAME_IS_NULL);
					return json.toString();
				}
				json.put(WebConstant.SUCCESS,sendMail(player,title,content));
			}
			catch(Exception e)
			{

			}
		}
			
		//发送奖励
//		else  if(type==SET_AWARD)
//		{
//			String player_name=data.readUTF();
//			String title=data.readUTF();
//			String content=InterTransltor.getInstance().getTransByKey(
//								PublicConst.SERVER_LOCALE,"activity_content");
//			Player player=objectFactory.getPlayerByName(player_name,false);
//			if(player==null)
//			{
//				// 直接返回错误
//				json.put(SUCCESS,GMConstant.ERR_PLAYER_NAME_NULL);
//				return json.toString();
//			}
//			// 要发送奖励包的长度
//			int length=data.readInt();
//			StringBuffer errPro=new StringBuffer();
//			StringBuffer econtet=new StringBuffer();
// 			for(int i=0;i<length;i++)
//			{
//				int sid=data.readInt();
//				int count=data.readInt();
//				Prop prop=(Prop)Prop.factory.newSample(sid);
//				if(prop==null) errPro.append(sid+","+count);
//				econtet.append(prop.getName()+"X"+count);
//				if(prop instanceof NormalProp)
//					((NormalProp)prop).setCount(count);
//				//有可能玩家的包裹已满  满了 那么也把当前的sid 记录下来	
//				if(!player.getBundle().incrProp(prop,true))
//				{
//					errPro.append(sid+","+count);
//				}
//				content=TextKit.replace(content,"%",econtet.toString());
//			}
//			sendMail(player,title,content);
//			//返回成功
//			bb.writeInt(GMConstant.ERR_SUCCESS);
//			//返回玩家未收到的奖励品
//			bb.writeUTF(errPro.toString());
//		}	
		return json.toString();
	
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

	/**
	 * 通过账号查询玩家
	 * @param account
	 * @param objectFactory
	 * @return
	 */
	public Player[] getPlayersByAccount(String account)
	{
		if(account==null||account.length()==0) return null;
		// String
		// sql="SELECT * FROM players WHERE players.user_id=(SELECT id FROM users WHERE users.user_account='"+account+"')";
		// Player[]
		// objs=(Player[])objectFactory.getPlayerCache().getDbaccess().loadBySql(sql);
		User user=objectFactory.getUserDBAccess().loadUser(account);
		if(user==null) return null;
		int[] ps=user.getPlayerIds();
		Player[] objs=null;
		if(ps!=null&&ps.length>0)
		{
			objs=new Player[ps.length];
			int len=0;
			for(int i=0;i<ps.length;i++)
			{
				objs[i]=objectFactory.getPlayerById(ps[i]);
				if(objs[i]!=null) len++;
			}
			if(len<=0) return null;
			if(len<ps.length)
			{
				Player[] nps=new Player[len];
				for(int i=0,j=0;i<objs.length&&j<len;i++)
				{
					if(objs[i]==null) continue;
					nps[j]=objs[i];
					j++;
				}
				objs=nps;
			}
		}
		else
		{
			objs=new Player[1];
			objs[0]=objectFactory.getPlayerById(user.getPlayerId());
			if(objs[0]==null) return null;
		}
		return objs;
	}
	
	
	/**
	 * 组装玩家基本信息
	 * @param player
	 * @return
	 */
	private JSONObject getPlayerBaseInfo(Player player)
	{
		JSONObject jo=new JSONObject();
		try
		{
			// 玩家名字
			jo.put(GMConstant.NAME,player.getName());
			// 玩家ID
			jo.put(WebConstant.PLAYER_ID,player.getId());
			// 玩家等级
			jo.put(GMConstant.LEVEL,player.getLevel());
			// 战力
			jo.put(GMConstant.POWER,player.getFightScore());
			//服务器id
			jo.put(WebConstant.SERVER_ID,UserToCenterPort.SERVER_ID);
			//大区id
			jo.put(WebConstant.AREA_ID,UserToCenterPort.AREA_ID);
			//角色的创建时间
			jo.put(WebConstant.CREATETIME,player.getCreateTime());
			//角色的登录时间
			jo.put(WebConstant.LOGINTIME,player.getUpdateTime());
			//大区id
			jo.put(WebConstant.PLAT,UserToCenterPort.PLAT_ID);
			
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jo;
	}
	
	/**验证当前是否满足web服务器的条件**/
	public boolean isablePlayerList(Player[] players)
	{
		for(int i=0;i<players.length;i++)
		{
			Player player=players[i];
			if(player==null) 
				continue;
			if(player.getLevel()>=WEB_NEED_LEVE)
			{
				return false;
			}
		}
		return true;
	}
	
	/**给玩家发邮件(活动中所获取的奖励)**/
	private int sendMail(Player player,String title,String content)
	{
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		
		Message message=objectFactory.createMessage(0,player.getId(),
			content,sendName,player.getName(),0,title,true);
		// 刷新前台
		JBackKit.sendRevicePlayerMessage(player,message,message
			.getRecive_state(),objectFactory);
		return GMConstant.ERR_SUCCESS;
	}

	
	public int[] getNo_open()
	{
		return no_open;
	}

	
	public void setNo_open(int[] no_open)
	{
		this.no_open=no_open;
	}
	
	
}
