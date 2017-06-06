package foxu.sea.gm.operators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.codec.MD5;
import mustang.io.ByteBuffer;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import shelby.cc.CCManager;
import shelby.dc.GameDBAccess;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.User;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;


public class LinkAccount extends GMOperator
{
	public static final int USER_ACCOUNT_LENGTH=50;
	
	public static final String EMAIL_MATCHES="^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String bindingType=params.get("binding_type");//强制绑定或者是绑定
		String playerName=params.get("player_name");
		String account=params.get("account");
		String password=params.get("password");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		MD5 md5=new MD5();
		password=md5.encode(password);
		// 检查账号是否符合格式
		if(account==null||account.length()==0
			||account.getBytes().length>=USER_ACCOUNT_LENGTH)
			return GMConstant.ERR_ACCOUNT_NULL;
		if(!account.matches(EMAIL_MATCHES))
			return GMConstant.ERR_ACCOUNT_ERROR;
		// 检查账号是否已经绑定
		if(objectFactory.getUserDBAccess().isExist(account,0))
			return GMConstant.ERR_ACCOUNT_ALREADY_LINKED;
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		Player player=objectFactory.getPlayerByName(playerName,false);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		// 判断是否已经绑定
		User userObj=(User)objectFactory.getUserDBAccess().load(String.valueOf(player.getUser_id()));
		if(userObj==null)
			return GMConstant.ERR_USER_NOT_EXISTS;
		int type=TextKit.parseInt(bindingType);
		if(userObj.getUserType()!=User.GUEST && type!=2)
			return GMConstant.ERR_PLAYER_ALREADY_LINKED;
		int result=linkAccount(userObj,account,password,player,null,objectFactory.getUserDBAccess(),info.getCCManager(),type);
		//成就数据采集
		if(result==GMConstant.ERR_SUCCESS)AchieveCollect.bindUser(player);
		return result;
	}

	private int linkAccount(User user,String account,String password,Player player,String sid,GameDBAccess dbaccess,CCManager ccmanager,int btype)
	{
		// 向用户中心通信创建账号
		ByteBuffer data=new ByteBuffer();
		data.writeByte(UserToCenterPort.BIND_USER);
		data.writeUTF(account);
		data.writeUTF(password);
		data.writeUTF(user.getCreateUdid());
		data.writeByte(UserToCenterPort.SERVER_ID);
		data.writeByte(player.getLocale());
		data.writeBoolean(true);//GM操作
		if(btype==2)
		data.writeByte(PublicConst.GM_FORCE_BINGD);
		else
			data.writeByte(PublicConst.COMMON_BINDING);
		data=sendHttpData(data);
		if(data==null)
		{
			return GMConstant.ERR_GAME_CENTER_COMUNICATION_ERROR;
		}
		int type=data.readUnsignedByte();
		// 绑定账号成功
		if(type==UserToCenterPort.BIND_USER_SUCCESS)
		{
			//password=data.readUTF();
			user.setUserAccount(account);
			user.setPassword(password);
			user.setUserType(User.USER);
			user.setLoginTime(TimeKit.getSecondTime());
			data.clear();
			if(dbaccess.save(user))
			{
				data.writeByte(UserToCenterPort.BIND_USER_SUCCESS);
				ccmanager.resetSessionId(sid,account);
				return GMConstant.ERR_SUCCESS;
			}
			else
			{
				return GMConstant.ERR_UNKNOWN;
			}
		}
		else if(type==UserToCenterPort.USER_IS_FULL)
		{
			return GMConstant.ERR_ACCOUNT_FULL;
		}
		return GMConstant.ERR_ACCOUNT_EXISTS;
	}
	
	private ByteBuffer sendHttpData(ByteBuffer data)
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
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
}
