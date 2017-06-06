package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.text.TextValidity;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AFightEventSave;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.WebShowActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.AllianceBattleManager;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFightEvent;
import foxu.sea.event.FightEvent;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.port.AlliancePort;

/**
 * 修改名称
 * @author lhj
 * 
 */
public class ModifyName extends GMOperator
{

	Logger log=LogFactory.getLogger(ModifyName.class);
	public static final int MIN_LEN=1,MAX_LEN=12;
	TextValidity tv;
	/** 数据获取类 **/
	CreatObjectFactory objectFactory;
	
	/**新联盟战管理器**/
	AllianceBattleManager battleFightManager;
	
	Object cachelock=new Object();

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String names=params.get("names");// 原始名称
		String newName=params.get("newnames");// 即将修改的名称
		objectFactory=info.getObjectFactory();
		int result=checkName(newName);
		if(result!=GMConstant.ERR_SUCCESS) return result;
		Player newPlayer=objectFactory.getPlayerByName(newName,false);
		if(newPlayer!=null) return GMConstant.ERR_NAME_BEEN_USED;
		Player player=objectFactory.getPlayerByName(names,false);
		String name=player.getName();
		int str=modifyName(player.getName(),newName);
		if(str!=GMConstant.ERR_SUCCESS) return str;
		log.error("Original name:"+name+"---------modifyName:"+newName);
		JBackKit.sendPlayerName(player);
		//modifyEventName(player);//todo
		if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID+""));
			if(alliance!=null)
			{
				JBackKit.sendmodifyPAname(alliance,objectFactory,
					player.getId(),newName);
				String title=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"player_role_list");
				String content=setContent(name,newName,"alliance_mail");
				sendAllianceMail(alliance,title,content,player);
				
			}
		}
		else
		{
			AlliancePort alliancePort=(AlliancePort)BackKit.getContext()
				.get("alliancePort");
			alliancePort.personFightChange(name,newName);
			alliancePort.sendPNameChange(player);
		}
		// 网站排行榜活动是否开启
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.WEB_SHOW_ID))
		{
			WebShowActivity activity=(WebShowActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.WEB_SHOW_ID,0);
			if(activity!=null)
			{
				activity.addNameRecord(player.getId());
			}
		}
		return GMConstant.ERR_SUCCESS;
	}

	/** 修改名称 **/
	public int modifyName(String name,String newName)
	{
		Player player=objectFactory.getPlayerByName(name,false);
		player.setName(newName);
		boolean flag=objectFactory.getPlayerCache().getDbaccess()
			.save(player);
		if(!flag)
		{
			player.setName(name);
			return GMConstant.ERR_NAME_UNVALID;
		}
		IntKeyHashMap cacheMap=objectFactory.getPlayerCache().getCacheMap();
		Object[] object=cacheMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			PlayerSave playerSave=(PlayerSave)object[i];
			Player oPlayer=playerSave.getData();
			// 移除黑名单
			ReplaceBlackFriednList(oPlayer,name,PublicConst.BLACK_LIST,
				newName);
			// 移除好友
			ReplaceBlackFriednList(oPlayer,name,PublicConst.FRIENDS_LIST,
				newName);
		}
		object=objectFactory.getaFightEventMemCache().getCacheMap().valueArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null)continue;
			AFightEventSave save=(AFightEventSave)object[i];
			AllianceFightEvent e=(AllianceFightEvent)save.getData();
			if(name.equals(e.getaName()))
			{
				e.setaName(newName);
				objectFactory.getaFightEventMemCache().getChangeListMap()
					.put(e.getUid(),save);
			}
		}
		return GMConstant.ERR_SUCCESS;
	}	
	/***
	 * 移除好友或者是黑名单有他的名称的
	 */
	public void ReplaceBlackFriednList(Player player,String name,String key,
		String newName)
	{
		String black=player.getAttributes(key);
		if(black!=null&&black.length()!=0)
		{
			String[] blacks=black.split(",");
			boolean isHave=false;
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<blacks.length;i++)
			{
				if(blacks[i].equals(name))
				{
					String title=InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,key);
					String content=setContent(name,newName,key);
					sendMail(player,title,content);
					isHave=true;
					blacks[i]=newName;
				}
				if(sb.length()==0)
					sb.append(blacks[i]);
				else
					sb.append(",").append(blacks[i]);
			}
			if(isHave) player.setAttribute(key,sb.toString());
		}
	}

	/** 是否有舰队出征 */
	public boolean modifyEventName(Player player)
	{
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null||fightEventList.size()<=0) return false;
		for(int i=0;i<fightEventList.size();i++)
		{
			if(fightEventList.get(i)==null) continue;
			FightEvent event=(FightEvent)fightEventList.get(i);
			if(event==null) continue;
			NpcIsland island=objectFactory.getIslandByIndexOnly(event
				.getAttackIslandIndex()+"");
			if(island==null) continue;
			if(island.getPlayerId()==0) continue;
			Player source=objectFactory.getPlayerById(event.getPlayerId());
			Player p=objectFactory.getPlayerById(island.getPlayerId());
			if(source!=null)
				JBackKit.sendFightEvent(source,event,objectFactory);
			if(p!=null) JBackKit.sendFightEvent(p,event,objectFactory);
		}
		return false;
	}

	/** 给指定的玩家发送邮件 **/
	public void sendMail(Player player,String title,String content)
	{
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");

		Message message=objectFactory.createMessage(0,player.getId(),
			content,sendName,player.getName(),0,title,true);
		// 刷新前台
		JBackKit.sendRevicePlayerMessage(player,message,
			message.getRecive_state(),objectFactory);
	}

	/** 给指定的alliance玩家发送邮件 **/
	public void sendAllianceMail(Alliance alliance,String title,
		String content,Player beplayer)
	{
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=objectFactory.getPlayerById(list.get(i));
			if(player==null) continue;
			if(beplayer!=null&&beplayer.getId()==player.getId()) continue;
			Message message=objectFactory.createMessage(0,player.getId(),
				content,sendName,player.getName(),0,title,true);
			// 刷新前台
			JBackKit.sendRevicePlayerMessage(player,message,
				message.getRecive_state(),objectFactory);
		}
	}

	/** 设置邮件发送的内容 **/
	public String setContent(String name,String newName,String key)
	{
		String content=null;
		if(key.equals(PublicConst.FRIENDS_LIST))
			content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"friend_mail");
		else if(key.equals(PublicConst.BLACK_LIST))
			content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"black_mail");
		else
			content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_mail");
		content=TextKit.replace(content,"%",name);
		content=TextKit.replace(content,"%",newName);
		return content;
	}

	/** 检测名称是否可以用 **/
	public int checkName(String newName)
	{
		if(newName.getBytes().length==newName.length())
		{
			if(newName.length()<MIN_LEN||newName.length()>MAX_LEN)
				return GMConstant.ERR_NAME_LENGTH;
		}
		else
		{
			if(newName.length()<(MIN_LEN/2)||newName.length()>(MAX_LEN/2))
				return GMConstant.ERR_NAME_LENGTH;
		}
		if(tv.valid(newName,true)!=null||newName.indexOf(",")>=0)
			return GMConstant.ERR_NAME_UNVALID;
		if(SeaBackKit.isContainValue(PublicConst.NONEPLAYER_NAME,
			newName))
			return GMConstant.ERR_NAME_UNVALID; 
		
		if(SeaBackKit.checkBlank(newName))
			return GMConstant.ERR_NAME_UNVALID; 
		
		return GMConstant.ERR_SUCCESS;
	}
	/**
	 * @param tv 要设置的 tv
	 */
	public void setTv(TextValidity tv)
	{
		this.tv=tv;
	}
	
	/**刷新联盟战玩家名称**/
	public void flushBattleFightPlayerName(Player player)
	{
		Alliance alliance=getAlliance(player);
		if(alliance==null) return;
		if(battleFightManager.getAllianceFight().getAllianceStage()
			.getStage()!=Stage.STAGE_FOUR) return;
		if(alliance.getBetBattleIsland()==0) return;
		JBackKit.sendRemovePlayer(objectFactory,player.getId(),
			alliance.getId(),alliance.getBetBattleIsland(),player.getName(),true);
	}
	/**获取联盟信息**/
	public Alliance getAlliance(Player player)
	{
		int alliance_id=0;
		if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			alliance_id=Integer.parseInt(player
				.getAttributes(PublicConst.ALLIANCE_ID));
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadOnly(alliance_id+"");
			if(alliance!=null
				&&alliance.getPlayerList().contain(player.getId()))
			{
				return alliance;
			}
		}
		return null;
	}

	
	public void setBattleFightManager(AllianceBattleManager battleFightManager)
	{
		this.battleFightManager=battleFightManager;
	}
	
}
