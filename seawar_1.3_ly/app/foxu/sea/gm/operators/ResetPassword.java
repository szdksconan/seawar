package foxu.sea.gm.operators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.codec.MD5;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.User;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;

/**
 * 修改密码
 * 
 * @author comeback
 * 
 */
public class ResetPassword extends GMOperator
{

	public int operate(String user,Map<String,String> params,JSONArray array,
		ServerInfo info)
	{
		String playerName=params.get("player_name");
		String newPassword=params.get("password");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		if(newPassword==null||newPassword.length()==0)
			return GMConstant.ERR_NEW_PASSWORD_ERROR;
		Player player=objectFactory.getPlayerByName(playerName,false);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		MD5 md5=new MD5();
		String md5pwd=md5.encode(newPassword);
		User userObj=objectFactory.getUserDBAccess().load(String.valueOf(
			player.getUser_id()));
		if(user==null||userObj.getUserType()==User.GUEST)
		{
			return GMConstant.ERR_IS_GUEST_ACCOUNT;
		}
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeByte(UserToCenterPort.CHANGE_PASSWORD);
		data.writeUTF(userObj.getUserAccount());
		data.writeUTF(md5pwd);
		data.writeUTF(userObj.getPassword());
		data.writeInt(TimeKit.getSecondTime());
		//大区
		data.writeInt(UserToCenterPort.AREA_ID);
		data.writeBoolean(true);
		data=sendHttpData(data,info);
		if(data==null) return GMConstant.ERR_GAME_CENTER_COMUNICATION_ERROR;
		int typeReturn=data.readUnsignedByte();
		if(typeReturn==0)
		{
			userObj.setPassword(md5pwd);
			objectFactory.getUserDBAccess().save(userObj);
		}
		else
		{
			return GMConstant.ERR_ACCOUNT_NOT_EXISTS;
		}
		return GMConstant.ERR_SUCCESS;
	}

	private ByteBuffer sendHttpData(ByteBuffer data,ServerInfo info)
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
				"http://"+info.getGameCenterIP()+":"
					+info.getGameCenterPort()+"/","POST",map,null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
}
