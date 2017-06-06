package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 设置前台跳转的链接地址
 * 
 * @author lihongji
 */
public class JumpAddress extends GMOperator
{

	/** 查询状态,关闭 **/
	public static int JUMP_SELECT=2,JUMP_CLO=3;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		/** state 跳转的类型 1：一天跳一次 2:强制跳转 **/
		String bindingState=params.get("state");
		/** 跳转的原因 **/
		String bindingStr=params.get("bindingStr");
		/** type 1:开启2:查询3:关闭 **/
		String bType=params.get("type");
		/** url 地址 **/
		String address=params.get("address");
		/** 等级限制 **/
		String levelStr=params.get("level");
		/** 平台 **/
		String platid=params.get("platid");
		// int platid=TextKit.parseInt(plat_id);
		String platids=params.get("platids");
		int type=TextKit.parseInt(bType);
		// 查询
		if(type==JUMP_SELECT)
		{
			JSONObject jo=new JSONObject();
			try
			{
				//设置绑定的状态
				jo.put("jump","jump_"+PublicConst.JUMP_ADDRESS);
				if(PublicConst.JUMP_REASON==null || PublicConst.JUMP_REASON.length()==0)
					jo.put("jumpreason","无");
				else
				jo.put("jumpreason",PublicConst.JUMP_REASON);
				jo.put("jumplevel",PublicConst.JUMP_LEVEL);
				int length=0;
				if(PublicConst.URL_ADDRESS!=null && PublicConst.URL_ADDRESS.length!=0)
				{
					length=PublicConst.URL_ADDRESS.length;
					jo.put("jump_length",length/2);
					for(int i=0;i<length;i+=2)
					{
						jo.put("address"+i,PublicConst.URL_ADDRESS[i]+","+PublicConst.URL_ADDRESS[i+1]);
					}
				}
				else
					jo.put("jump_length",length);
				jsonArray.put(jo);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		// 关闭
		else if(type==JUMP_CLO)
		{
			PublicConst.JUMP_REASON=null;
			PublicConst.JUMP_ADDRESS=0;
			PublicConst.URL_ADDRESS=null;
			PublicConst.JUMP_LEVEL=0;
		}
		// 开启
		else
		{
			if(PublicConst.GAME_BINDING!=0)
			{
				return GMConstant.ERR_BINDING_OPNE;
			}
			int state=TextKit.parseInt(bindingState);
			if(bindingStr==null||bindingStr.length()==0)
				return GMConstant.ERR_REASON_IS_NULL;
			int result=urlvalidate(address);
			if(result!=0) return result;
			int level=TextKit.parseInt(levelStr);
			if(level<0||level>PublicConst.MAX_PLAYER_LEVEL)
				return GMConstant.ERR_LEVEL_PLAYER_ERRO;
			PublicConst.JUMP_REASON=bindingStr;
			PublicConst.JUMP_ADDRESS=state;

			PublicConst.JUMP_LEVEL=level;
			if(platid.equals("all"))
				validate(platids,address,info.getObjectFactory(),state);
			else
			{
				getURLAddress(platid,address);
				if(state==PublicConst.ADVISE_BINDING)
				{
					if(SeaBackKit.isSameDay(PublicConst.JUMP_TIME,
						TimeKit.getSecondTime()))
						removeState(info.getObjectFactory(),platid);
					PublicConst.JUMP_TIME=TimeKit.getSecondTime();
				}
			}
		}
		return GMConstant.ERR_SUCCESS;
	}

	/*** 去除玩家身上的状态 **/
	public void removeState(CreatObjectFactory factory,String platid)
	{
		IntKeyHashMap cache=factory.getPlayerCache().getCacheMap();
		for(int i=0;i<cache.size();i++)
		{
			PlayerSave psave=(PlayerSave)cache.get(i);
			if(psave==null) continue;
			Player player=psave.getData();
			if(player==null) continue;
			if(!platid.equals(player.getPlat())) continue;
			player.setAttribute(PublicConst.PLAYER_URL_TIME,"");
		}
	}

	/** 设置平台信息 **/
	public void getURLAddress(String platid,String address)
	{
		String[] url=PublicConst.URL_ADDRESS;
		/** 是否有开启过 **/
		if(url==null||url.length==0)
		{
			String urls[]=new String[1*2];
			urls[0]=platid+"";
			urls[1]=address;
			PublicConst.URL_ADDRESS=urls;
			return;
		}
		/** 验证是否有这个平台 **/
		for(int i=0;i<url.length;i+=2)
		{
			if(url[i].equals(platid+""))
			{
				url[i+1]=address;
				PublicConst.URL_ADDRESS=url;
				return;
			}
		}
		/** 添加新的平台 **/
		String urls[]=new String[url.length+2];
		for(int i=0;i<url.length;i++)
		{
			urls[i]=url[i];
		}
		urls[urls.length-2]=platid+"";
		urls[urls.length-1]=address;
		PublicConst.URL_ADDRESS=urls;
	}

	/** 检测 **/
	public void validate(String platids,String address,
		CreatObjectFactory factory,int state)
	{
		String[] str=platids.split(",");
		for(int j=0;j<str.length;j++)
		{
			getURLAddress(str[j],address);
			if(state==PublicConst.ADVISE_BINDING)
			{
				if(SeaBackKit.isSameDay(PublicConst.JUMP_TIME,
					TimeKit.getSecondTime())) removeState(factory,str[j]);
			}
		}
	}

	/** 检测URL是否正确 **/
	public int urlvalidate(String address)
	{
		if(address!=null&&address.length()!=0)
		{
			if(address.indexOf("http")==-1)
			{
				if(address.indexOf("market")==-1)
					return GMConstant.ERR_ADDRESS_NULL;
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
}
