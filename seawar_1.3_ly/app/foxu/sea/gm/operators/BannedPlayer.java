package foxu.sea.gm.operators;

import java.util.ArrayList;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.net.Session;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.User;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.port.ChatMessagePort;

/**
 * 禁言和封号
 * @author comeback
 *
 */
public class BannedPlayer extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String bannedStr=params.get("banned");
		String muteTimeStr=params.get("mute_time");
		String systemMessage=params.get("system_message");
		
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,false);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		// 封号
		User userObj=objectFactory.getUserDBAccess().load(
			String.valueOf(player.getUser_id()));
		if(userObj!=null)
		{
			boolean banned="true".equals(bannedStr);
			userObj.setBanned(banned?1:0);
			if(banned)
			{
				Session session=(Session)player.getSource();
				if(session!=null&&session.getConnect()!=null)
					session.getConnect().close();
			}
			objectFactory.getUserDBAccess().save(userObj);
		}
		// 禁言
		boolean isClearPlayerChat=false;
		if(muteTimeStr!=null&&TextKit.valid(muteTimeStr,TextKit.NUMBER)==0)
		{
			int muteTime=Integer.parseInt(muteTimeStr);
			if(muteTime>=0)
			{
				player.setMuteTime(TimeKit.getSecondTime()+muteTime*60*60);
				isClearPlayerChat=true;
			}
		}
		// 发系统消息
		systemMessage=systemMessage.trim();
		if(systemMessage!=null&&systemMessage.length()>0)
		{
			// 聊天消息
			ChatMessage message=new ChatMessage();
			// type
			message.setType(ChatMessage.SYSTEM_CHAT);
			message.setTime(TimeKit.getSecondTime());
			message.setSrc("");
			message.setText(systemMessage);
			SeaBackKit.sendAllMsg(message,info.getDSManager(),false);
			JBackKit.sendScrollMessage(info.getDSManager(),systemMessage);
			//存信息
			ChatMessagePort chatPort=(ChatMessagePort)BackKit.getContext().get(
				"chatMessagePort");
			chatPort.numFiler();
			chatPort.getChatMessages().add(message);
		}
		//存信息
		ChatMessagePort chatPort=(ChatMessagePort)BackKit.getContext().get(
			"chatMessagePort");
		// 清除被禁言玩家的世界聊天
		if(isClearPlayerChat)
		{
			ArrayList<ChatMessage> list=chatPort.getChatMessages();
			synchronized(list)
			{
				for(int i=0;i<list.size();i++)
				{
					ChatMessage m=list.get(i);
					if(m==null)	continue;
					if(m.getSrc().equals(player.getName()))
					{
						list.remove(i);
						i--;
					}
				}
			}
		}
		chatPort.numFiler();
		return GMConstant.ERR_SUCCESS;
	}

}
