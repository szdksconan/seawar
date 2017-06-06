package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.set.ArrayList;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;

/**
 * 玩家邮件
 * 
 * @author comeback
 *
 */
public class PlayerMail extends GMOperator
{
	public static int PROP_LIMIT=30;
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerNames=params.get("player_name");
		String awardSidStr=params.get("award_sid");
		String content=params.get("content");
		String title=params.get("title");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		if(playerNames==null||playerNames.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		String[] names=TextKit.split(playerNames,",");
		if(names.length>GMConstant.MAX_PLAYER_COUNT)
			return GMConstant.ERR_TOO_MANY_PLAYERS;
		int[] propInfo=null;
		if(awardSidStr!=null&&awardSidStr.length()>0)
		{
			String[] strs=TextKit.split(awardSidStr,",");
			try
			{
				propInfo=TextKit.parseIntArray(strs);
				for(int i=0;i<propInfo.length;i+=2)
				{
					if(propInfo[i+1]>PROP_LIMIT)
						return GMConstant.ERR_PRO_NUM_IS_ERRO;
				}
			}
			catch(Throwable t)
			{
				return GMConstant.ERR_PARAMATER_ERROR;
			}
		}
		
		for(int i=0;i<names.length;i++)
		{
			String playerName=names[i]; 
//			if(playerName==null||playerName.length()==0)
//				return GMConstant.ERR_PLAYER_NAME_NULL;
//			Player player=objectFactory.getPlayerByName(playerName,true);
//			if(player==null)
//				return GMConstant.ERR_PLAYER_NOT_EXISTS;
			try
			{
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.PLAYER_NAME,playerName);
				boolean isExist=true;
				if(playerName==null||playerName.length()==0)
					isExist=false;
				Player player=objectFactory.getPlayerByName(playerName,true);
				if(player==null)
					isExist=false;
				jo.put(GMConstant.BASE_INFO,isExist);
				if(isExist){
					int ret=addPropToPlayer(player,propInfo);
					jo.put(GMConstant.PROP_STATE,ret);
					if(ret!=GMConstant.ERR_SUCCESS)
					{
						jo.put(GMConstant.MAIL_STATE,GMConstant.ERR_NOT_DO);
						continue;
					}
					
					ret=sendMail(player,title,content,objectFactory);
					jo.put(GMConstant.MAIL_STATE,ret);
				}
				jsonArray.put(jo);
			}
			catch(Throwable t)
			{
				return GMConstant.ERR_UNKNOWN;
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
	
	/**
	 * 给指定的玩家发送邮件
	 * @param playerName
	 * @param title
	 * @param content
	 * @param objectFactory
	 * @return
	 */
	private int sendMail(Player player,String title,String content,CreatObjectFactory objectFactory)
	{
//		String sendName=InterTransltor.getInstance().getTransByKey(
//			PublicConst.SERVER_LOCALE,"system_mail");
//		
//		Message message=objectFactory.createMessage(0,player.getId(),
//			content,sendName,player.getName(),Message.SYSTEM_ONE_TYPE,title,true);
//		// 刷新前台
//		JBackKit.sendRevicePlayerMessage(player,message,message
//			.getRecive_state(),objectFactory);
		return GMConstant.ERR_SUCCESS;
	}
	
	/**
	 * 给玩家增加物品
	 * @param player
	 * @param propInfo
	 * @return
	 */
	private int addPropToPlayer(Player player,int[] propInfo)
	{
		ArrayList list=new ArrayList();
		if(propInfo==null||propInfo.length==0||propInfo[0]==0)
			return GMConstant.ERR_SUCCESS;
		if(propInfo.length==1)
		{
			Prop prop=(Prop)Prop.factory.newSample(propInfo[0]);
			if(prop==null)
				return GMConstant.ERR_PROP_IS_NULL;
			list.add(prop);
		}
		else if(propInfo.length%2==0)
		{
			for(int i=0;i<propInfo.length;i+=2)
			{
				int sid=propInfo[i];
				int count=propInfo[i+1];
				if(count>NormalProp.MAX_COUNT||count<=0)
					return GMConstant.ERR_PARAMATER_ERROR;
				Prop prop=(Prop)Prop.factory.newSample(sid);
				if(prop==null)
					return GMConstant.ERR_PROP_IS_NULL;
				if(prop instanceof NormalProp)
				{
					((NormalProp)prop).setCount(count);
					list.add(prop);
				}
				else
				{
					list.add(prop);
					for(int j=1;j<count;j++)
					{
						prop=(Prop)Prop.factory.newSample(sid);
						list.add(prop);
					}
				}
			}
		}
		else
		{
			return GMConstant.ERR_PARAMATER_ERROR;
		}
		// 检查背包空间,这里没有考虑合并的情况
		if(player.getBundle().getEmptyNum()<list.size())
			return GMConstant.ERR_BUNDLE_IS_FULL;
		for(int i=0;i<list.size();i++)
		{
			Prop prop=(Prop)list.get(i);
			player.getBundle().incrProp(prop,true);
		}
		if(list.size()>0)
			JBackKit.sendResetBunld(player);
		
		return GMConstant.ERR_SUCCESS;
	}

}
