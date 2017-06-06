package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.email.EmailManager;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * ·¢ËÍPUSHÏûÏ¢
 * @author comeback
 *
 */
public class PushMessage extends GMOperator
{

	public int operate(String gmUser,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int days=Integer.parseInt(params.get("days"));
		String push=params.get("push");
		String title=params.get("title");
		String content=params.get("content");
		String user=params.get("user");
		String pwd=params.get("pwd");
		String server=params.get("emailserver");
//		if(user==null||user.equals("")||pwd==null||pwd.equals("")
//			||server==null||server.equals(""))
//		{
//			return GMConstant.ERR_PARAMATER_ERROR;
//		}
		EmailManager.user=user;
		EmailManager.pwd=pwd;
		EmailManager.emailServer=server;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		String ret=EmailManager.getInstance().sendMailAndPush(objectFactory,days,push,title,content);
		if(ret==null)
			return GMConstant.ERR_SUCCESS;
		else
		{
			try
			{
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.MSG,ret);
				jsonArray.put(jo);
			}
			catch(JSONException e)
			{
			}
			return GMConstant.ERR_UNKNOWN;
		}
	}

}
