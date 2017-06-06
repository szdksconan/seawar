package foxu.sea.port;

import mustang.back.BackKit;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.text.TextValidity;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AFightEventSave;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.dcaccess.datasave.BattleGroundSave;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.dcaccess.mem.AllianceMemCache;
import foxu.ds.PlayerKit;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.WebShowActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.alliance.alliancebattle.AllianceBattleManager;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFightEvent;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.alliance.alliancefight.BattleGround;
import foxu.sea.event.FightEvent;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;

/** 修改联盟名称 **/
public class ModifyNamePort extends AccessPort
{

	public static final int MODIFY_NAME_SID=900;
	public static final int MODIFY_ANAME_SID=901;
	public static final int MIN_LEN=1,MAX_LEN=12;
	/** PLAYER_NAME=1 修改玩家名称 ALLIANCENAME=2 修改联盟名称 **/
	public static final int PLAYER_NAME=1,ALLIANCENAME=2;

	TextValidity tv;
	/** 数据获取类 **/
	CreatObjectFactory objectFactory;
	/**新联盟战管理器**/
	AllianceBattleManager battleFightManager;
	Logger log=LogFactory.getLogger(ModifyNamePort.class);
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayerOnly(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		// 修改玩家名称
		if(type==PLAYER_NAME)
		{
			// 是否有物品
			int  count=player.getBundle().getPropCount(MODIFY_NAME_SID);
			if(count<=0)
				throw new DataAccessException(0,"prop is null");
			String newName=data.readUTF();
			if(newName.getBytes().length==newName.length())
			{
				if(newName.length()<MIN_LEN||newName.length()>MAX_LEN)
					throw new DataAccessException(0,"name length wrong");
			}
			else
			{
				if(newName.length()<(MIN_LEN/2)
					||newName.length()>(MAX_LEN/2))
					throw new DataAccessException(0,"name length wrong");
			}
			if(tv.valid(newName,true)!=null||newName.indexOf(",")>=0)
				throw new DataAccessException(0,"name is not valid");
			
			if(SeaBackKit.checkBlank(newName))
				throw new DataAccessException(0,"name is not valid");
			
			if(SeaBackKit.isContainValue(PublicConst.NONEPLAYER_NAME,
				newName))
				throw new DataAccessException(0,"name has been used");
			Player newPlayer=objectFactory.getPlayerByName(newName,false);
			if(newPlayer!=null)
				throw new DataAccessException(0,"name has been used");
			String name=player.getName();
			String str=modifyName(player.getName(),newName);//todo player
			if(str!=null) throw new DataAccessException(0,str);
			log.error("Original name:"+name+"---------modifyName:"+newName);
			player.getBundle().decrProp(MODIFY_NAME_SID);
			JBackKit.sendResetBunld(player);
			JBackKit.sendPlayerName(player);
			modifyEventName(player);//TODO
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						player.getAttributes(PublicConst.ALLIANCE_ID+""));
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
//				alliancePort.sendPNameChange(player);
			}
			data.clear();
			data.writeByte(PLAYER_NAME);
			data.writeBoolean(true);
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
		}
		// 修改联盟名称
		else if(type==ALLIANCENAME)
		{
			// 是否有物品
			int count=player.getBundle().getPropCount(MODIFY_ANAME_SID);
			if(count<=0)
			{
				throw new DataAccessException(0,"prop is null");
			}
			// 是否有联盟
			String alliance_id=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(alliance_id==null||alliance_id.equals(""))
				throw new DataAccessException(0,"alliance has been dismiss");
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadOnly(alliance_id+"");
			if(alliance==null)
				throw new DataAccessException(0,"alliance has been dismiss");

			if(alliance.getMasterPlayerId()!=player.getId())
				throw new DataAccessException(0,"you are not master");

			String name=data.readUTF();
			if(name.getBytes().length==name.length())
			{
				if(name.length()<MIN_LEN||name.length()>MAX_LEN)
					throw new DataAccessException(0,
						"alliance name length wrong");
			}
			else
			{
				if(name.length()<(MIN_LEN/2)||name.length()>(MAX_LEN/2))
					throw new DataAccessException(0,
						"alliance name length wrong");
			}
			if(tv.valid(name,true)!=null||name.indexOf(",")>=0)
				throw new DataAccessException(0,"alliance name is not valid");
			
			if(SeaBackKit.checkBlank(name))
				throw new DataAccessException(0,"name is not valid");
			
			if(SeaBackKit.isContainValue(PublicConst.SHIELD_WORD,name))
				throw new DataAccessException(0,"alliance name is not valid");			
			Alliance alliances=objectFactory.getAllianceMemCache()
				.loadByName(name,false);
			if(alliances!=null)
				throw new DataAccessException(0,
					"alliance name has been used");		
			String oriname=alliance.getName();
			alliance.setName(name);
			boolean flag=objectFactory.getAllianceMemCache().getDbaccess()
				.save(alliance);
			if(!flag)
			{
				alliance.setName(oriname);
				throw new DataAccessException(0,"alliance name is not valid");
			}
			log.error("Original Alliancename:"+oriname+"---------modifyAllianceName:"+name);
			player.getBundle().decrProp(MODIFY_ANAME_SID);
			/** 保存数据 **/
			AllianceMemCache cache=objectFactory.getAllianceMemCache();
			cache.save(alliance.getId()+"",alliance);

			/** 修改排行榜的数据 **/
			AlliancePort alliancePort=(AlliancePort)BackKit.getContext()
				.get("alliancePort");
			alliancePort.setAllianceName(oriname,name);

			// 联盟事件
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_MODIFY,player.getName(),
				player.getName(),name,TimeKit.getSecondTime());
			alliance.addEvent(event);

			// alliancePort.flushFileds(true);
			String title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_list");
			String content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_modify_info");
			content=TextKit.replace(content,"%",alliance.getName());
			sendAllianceMail(alliance,title,content,null);
			//给敌对联盟发送消息
			sendHostileMessage(alliance,oriname);
			// 发送系统信息
			String messcontent=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"sysmessage_modify_name");
			messcontent=TextKit.replace(messcontent,"%",oriname);
//			messcontent=SeaBackKit.setSystemContent(messcontent,"%",ChatMessage.SEPARATORS,name);
			messcontent=TextKit.replace(messcontent,"%",name);
			SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
				messcontent);
			JBackKit.sendAllianceName(alliance,objectFactory);
			JBackKit.sendResetBunld(player);
			Object[] grounds=AllianceFightManager.amanager.getBGrounds();
			for(int i=0;i<grounds.length;i++)
			{
				BattleGround bground=(BattleGround)((BattleGroundSave)grounds[i])
					.getData();
				if(bground.getId()==alliance.getId())
				{
					JBackKit.sendGround(objectFactory,bground,AllianceFightManager.amanager);
				}
			}
			flushBattleFightAllianceName(alliance);
			data.clear();
			data.writeByte(ALLIANCENAME);
			data.writeBoolean(true);
		}
		return data;
	}

	/** 修改名称 **/
	public String modifyName(String name,String newName)
	{
		Player player=objectFactory.getPlayerByName(name,false);
		player.setName(newName);
		
		boolean flag=objectFactory.getPlayerCache().getDbaccess()
			.save(player);
		if(!flag)
		{
			player.setName(name);
			return "name is not valid";
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
		return null;
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

	/**
	 * @param tv 要设置的 tv
	 */
	public void setTv(TextValidity tv)
	{
		this.tv=tv;
	}
	
	
	/** 所有联盟如果有这敌对联盟在解散的时候要发邮件 **/
	public void sendHostileMessage(Alliance myAlliance,String orgname)
	{
		Object[] objs=objectFactory.getAllianceMemCache().getCacheMap()
			.valueArray();
		if(objs==null||objs.length==0) return;
		Player player=objectFactory.getPlayerById(myAlliance
			.getMasterPlayerId());
		String title=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"modify_hostile");
		String content=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"modify_hostile_content");
		content=TextKit.replace(content,"%",orgname);
		content=TextKit.replace(content,"%",myAlliance.getName());
		for(int i=0;i<objs.length;i++)
		{
			if(objs[i]==null) continue;
			AllianceSave save=((AllianceSave)objs[i]);
			if(save==null) continue;
			Alliance alliance=save.getData();
			if(alliance==null) continue;
			if(SeaBackKit.isHostile(alliance,myAlliance))
			{
				Player hplayer=objectFactory.getPlayerById(alliance
					.getMasterPlayerId());
				if(hplayer==null) continue;
				MessageKit.sendSystemMessages(hplayer,objectFactory,content,
					title);
			}
		}
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
	
	
	/**刷新新联盟战的联盟名称**/
	public  void flushBattleFightAllianceName(Alliance alliance)
	{
		if(alliance.getBetBattleIsland()==0) return;
		BattleIsland island=(BattleIsland)objectFactory
			.getBattleIslandMemCache()
			.load(alliance.getBetBattleIsland()+"");
		if(island==null) return;
		int stage=battleFightManager.getAllianceFight().getAllianceStage()
			.getStage();
		if(stage!=Stage.STAGE_FOUR)
		{
			JBackKit.sendAllianceBetInfo(null,objectFactory);
		}
		else
		{
			JBackKit.sendAllianceBetInfo(island,objectFactory);
		}
	}
	
	public void setBattleFightManager(AllianceBattleManager battleFightManager)
	{
		this.battleFightManager=battleFightManager;
	}
	
}
