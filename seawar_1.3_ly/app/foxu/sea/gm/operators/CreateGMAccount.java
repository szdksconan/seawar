package foxu.sea.gm.operators;

import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.codec.MD5;
import mustang.text.TextKit;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMManager;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


public final class CreateGMAccount extends ToCenterOperator
{

	public int operate(String gmUser,Map<String,String> params,JSONArray array,
		ServerInfo info)
	{
		String user=params.get("user_account");
		String password=params.get("password");
		String repassword=params.get("retypepassword");
		String privilege=params.get("privilege");
		// 检查密码
		if(password==null||password.length()==0||!password.equals(repassword))
		{
			return GMConstant.ERR_PASSWORD_ERROR;
		}
		MD5 md5=new MD5();
		password=md5.encode(password);
		// 检查权限
		if(TextKit.valid(privilege,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PRIVILEGE_RANGE_ERROR;
		int priv=TextKit.parseInt(privilege);
		if(priv<GMOperator.MIN_PRIVILEGE||priv>GMOperator.MAX_PRIVILEGE)
			return GMConstant.ERR_PRIVILEGE_RANGE_ERROR;
		// 检查账号是否存在
		GMManager manager=(GMManager)info;
		if(manager.updateUserInfo(user)==GMConstant.ERR_SUCCESS)
			return GMConstant.ERR_ACCOUNT_EXISTS;
		Map<String,String> map=new HashMap<String,String>();
		map.put("type","2");
		map.put("user_account",user);
		map.put("password",password);
		map.put("privilege",privilege);
		String jso=this.sendHttpDataToCenter(7,map);
		if(jso==null)
			return GMConstant.ERR_GAME_CENTER_COMUNICATION_ERROR;
		try
		{
			JSONObject jo=new JSONObject(jso);
			boolean b=jo.getBoolean("success");
			if(!b)
				return GMConstant.ERR_UNKNOWN;
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return GMConstant.ERR_SUCCESS;
	}

}
