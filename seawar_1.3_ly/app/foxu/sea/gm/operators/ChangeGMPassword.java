package foxu.sea.gm.operators;

import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.codec.MD5;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMManager;
import foxu.sea.gm.ServerInfo;


public class ChangeGMPassword extends ToCenterOperator
{

	public int operate(String user,Map<String,String> params,JSONArray array,
		ServerInfo info)
	{
		JSONObject jo;
		try
		{
			String oldpassword=params.get("oldpassword");
			String newpassword=params.get("newpassword");
			String renewpassword=params.get("retypepassword");
			GMManager manager=(GMManager)info;
			MD5 md5=new MD5();
			// 验证两次输入的新密码是否一致
			if(newpassword==null||!newpassword.equals(renewpassword))
				return GMConstant.ERR_NEW_PASSWORD_ERROR;
			// 验证旧密码是否正确，先更新一次数据
			manager.updateUserInfo(user);
			oldpassword=md5.encode(oldpassword);
			if(!manager.checkPassword(user,oldpassword))
				return GMConstant.ERR_PASSWORD_ERROR;
			// 通知平台修改密码
			newpassword=md5.encode(newpassword);
			Map<String,String> map=new HashMap<String,String>();
			map.put("type","3");
			map.put("user_account",user);
			map.put("password",newpassword);
			String ret=sendHttpDataToCenter(7,map);
			if(ret==null)
				return GMConstant.ERR_GAME_CENTER_COMUNICATION_ERROR;
			jo=new JSONObject(ret);
			boolean b=jo.getBoolean("success");
			if(!b)
				return GMConstant.ERR_PASSWORD_ERROR;
			// 修改成功后更新本地密码
			manager.updatePassword(user,newpassword);
			return GMConstant.ERR_SUCCESS;
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}
	}

}
