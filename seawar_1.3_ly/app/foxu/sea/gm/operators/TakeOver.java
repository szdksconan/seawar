package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.codec.MD5;
import mustang.math.MathKit;
import mustang.net.Session;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 接管账号
 * @author comeback
 *
 */
public class TakeOver extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String timeStr=params.get("time");
		
		CreatObjectFactory objectFactory=info.getObjectFactory();
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		Player targetPlayer=null;
		targetPlayer=objectFactory.getPlayerByName(playerName,false);
		if(targetPlayer==null)
		{
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		}
		try
		{
			int time=Integer.parseInt(timeStr);
			if(time<=0)
			{
				targetPlayer.setAttribute(PublicConst.TAKE_OVER,null);
				return GMConstant.ERR_SUCCESS;
			}
			//生成长度为6的由字母和数字组成的随机密码，排队Il1这三个不易识别的
			//String chars="ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyx023456789";
			String chars="0123456789";
			String passwd="";
			for(int i=0;i<6;i++)
			{
				passwd+=chars.charAt(MathKit.randomValue(0,chars.length()));
			}
			String md5passwd=(new MD5()).encode(passwd);
			time*=60; //传的时间为分钟，转换为秒
			time+=TimeKit.getSecondTime();
			if(time<0)
				time=Integer.MAX_VALUE;
			targetPlayer.setAttribute(PublicConst.TAKE_OVER,md5passwd+"|"+time);
			Session session=(Session)targetPlayer.getSource();
			if(session!=null&&session.getConnect()!=null)
				session.getConnect().close();
			JSONObject jo=new JSONObject();
			jo.put(GMConstant.ACCOUNT,"#"+targetPlayer.getId());
			jo.put(GMConstant.PASSWORD,passwd);
			jsonArray.put(jo);
			return GMConstant.ERR_SUCCESS;
		}
		catch(Exception e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
	}

}
