package foxu.sea.kit;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import shelby.ds.DSManager;
import foxu.cross.goalleague.ClientLeagueManager;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.dcaccess.datasave.BattleGroundSave;
import foxu.dcaccess.datasave.BattleIslandSave;
import foxu.dcaccess.datasave.FightEventSave;
import foxu.ds.PlayerKit;
import foxu.sea.MealTimeManager;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.achieve.AchieveManager;
import foxu.sea.achieve.Achievement;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.AwardShippingActivity;
import foxu.sea.activity.ConsumeGemsActivity;
import foxu.sea.activity.DoubleGemsAcitivity;
import foxu.sea.activity.LuckyExploredActivity;
import foxu.sea.activity.PeaceActivity;
import foxu.sea.activity.RobFestivalActivity;
import foxu.sea.activity.TotalBuyActivity;
import foxu.sea.activity.VaribleAwardActivity;
import foxu.sea.activity.WarManicActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.AllianceBattleManager;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFight;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.alliance.alliancefight.BattleGround;
import foxu.sea.announcement.Announcement;
import foxu.sea.award.Award;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.comparator.MessageComparator;
import foxu.sea.comrade.ComradeHandler;
import foxu.sea.event.FightEvent;
import foxu.sea.growth.GrowthPlanManager;
import foxu.sea.messgae.Message;
import foxu.sea.port.MessagePort;
import foxu.sea.port.WorldScenePort;
import foxu.sea.recruit.RecruitKit;
import foxu.sea.recruit.RecruitWelfareManager;
import foxu.sea.task.CombinationTask;
import foxu.sea.task.Task;
import foxu.sea.vertify.Vertify;
import foxu.sea.vertify.VertifyManager;
import foxu.sea.worldboss.WorldBoss;

/** 刷新前台类 */
public class JBackKit
{

	/** 前天刷新端口 */
	public static final int FROE_PORT=2000;
	/** EMAIL_PORT=2003 */
	public static final int EMAIL_PORT=2003;
	/** 验证器前端端口 */
	public static final int VERTIFY_PORT=2007;
	/** 军官前端端口 */
	public static final int OFFICER_PORT=2008;
	/** 新活动前端端口 */
	public static final int ACT_PORT=2009;
	/** 端口2007推送常量  VERTIFY_POPUP侦察验证码*/
	public static final int VERTIFY_POPUP=63;
	/** 新兵福利前端端口 */
	public static final int RECRUIT_PORT=2011;
	/**联盟战刷新端口**/
	public static final int BATTLE_FIGHT=2010;
	/**招募消息端口**/
	public static final int COMRADE_PORT=2012;
	/**跨服积分赛端口**/
	public static final int CROSS_LEAGUE_PORT=2013;
	
	/**
	 * 类型type TASK_FINISH=1任务改变 ADD_TASK=2 RESET_RESOURCE=3刷新资源
	 * RESET_ACTIVIC=4刷新能量 RESET_SERVICE=5刷新服务 RESET_PLAYER=6刷新玩家
	 * RESET_ISLAND=7刷新岛屿 RESET_BUNLD=8刷新包裹 RESET_SELFPOINT=9刷新关卡
	 * RESET_TASK=10刷新任务 SEND_AWARD =11推送个奖励品sid SEND_RANDOM_TASK=12
	 * SEND_SHIPS=13刷新船只 收到一封邮件 SEND_FIGHT_EVENT=15刷新事件RESET_HURT_TROOPS=16伤兵
	 * RESET_HONOR_YE声望 RESET_MAIN_GROUP刷新主力舰队ACTIVITY_FLSUH活动刷新
	 * LIMIT_SALE_ACTIVITY 限时购买活动刷新DISCOUNT_ACTIVITY=27打折活动
	 * POINT_BUFF=33刷新关卡加成等级 ANNOUNCE_PUSH 公告推送 ANNOUNCE_REMOVE 移除公告
	 * SWITCH_STATE=48 领取码,邀请码,制造车间状态 RECHANGEPAY_STATE=50第三方支付状态
	 * MODIFY_PNAME=57 刷新玩家名称 MODIFY_ANAME=58 刷新alliance名称 MODIFY_PANAME=59
	 * 当修改玩家名称的时候有联盟就刷新联盟内的名称 ARENA_RESET=60 环球军演重置推送 FIGHT_SCORE=61 战力改变推送
	 * MARCH_LINE=62世界地图行军线 VERTIFY_POPUP=63弹出验证器 WAR_MANIC战争狂人推送 
	 * PAY_RELAY=66充值接力活动 MEALTIME_ENERGY=67 饭点能量推送 LOGIN_REWARD=69 登陆有礼活动推送在线玩家
	 * FLUSH_BATTLE_FIGHT=70 刷新(新)联盟战的信息 GROWTH_PLAN=71 推送成长计划 OFFICER_SHOP=72 军官商店刷新 
	 * ,RESET_PROSPERITY=73 刷新繁荣度,FLUSH_HEAD=77头像激活刷新
	 * PEACE_ACT=74  和平旗活动 RESET_SCIENCE=75 刷新科研信息    FLUSH_FLAG=78 刷新联盟旗帜
	 */
	public static final int TASK_CHANGE=1,ADD_TASK=2,RESET_RESOURCE=3,
					RESET_ACTIVIC=4,RESET_SERVICE=5,RESET_PLAYER=6,
					RESET_ISLAND=7,RESET_BUNLD=8,RESET_SELFPOINT=9,
					RESET_TASK=10,SEND_AWARD=11,SEND_RANDOM_TASK=12,
					SEND_SHIPS=13,SEND_EXP=14,SEND_FIGHT_EVENT=15,
					RESET_HURT_TROOPS=16,RESET_HONOR_YES=17,
					RESET_MAIN_GROUP=18,RESET_HONOR_SCROE=19,
					MESSAGE_VIEW=20,RESET_ONE_BUILD=21,
					PLAYER_ISLAND_STATE_CHANGE=22,FLUSH_ISLAND_INDEX=23,
					ACTIVITY_FLSUH=25,LIMIT_SALE_ACTIVITY=26,
					AFIGHT_GROUND=27,AFIGHT_FLEET=28,AFIGHT_UPSHIP=29,
					AFIGHT_HORN=30,DISCOUNT_ACTIVITY=31,LUCKY_DRAWCOUNT=32,
					POINT_BUFF=33,ACHIEVE_FLUSH=34,VARIBLE_AWARD=35,
					ALLIANCE_INVITATION=36,PLAYER_VIP_POTIN=37,
					GEM_CHANGE=38,MOUTH_CARD=39,FP_AWRD=40,
					BUILD_AUTO_START=41,LEVEL_TO_PUSH=42,SERVICE_STAUTS=43,
					VITALITY_TASK=44,EQUIP_INFO=45,ANNOUNCE_PUSH=46,
					ANNOUNCE_REMOVE=47,SWITCH_STATE=48,RECHANGEPAY_STATE=50,
					DATE_OFF=49,TOTALBUY_AWARD=52,CONSUME_GEMS=53,
					DOUBLE_GEMS=54,SHORT_TOTALBUY_AWARD=55,
					SCROLL_MESSAGE=56,MODIFY_PNAME=57,MODIFY_ANAME=58,
					MODIFY_PANAME=59,ARENA_RESET=60,FIGHT_SCORE=61,MARCH_LINE=62,
					OFFICER_INFO=63,WAR_MANIC=64,OFFICER_EFFECT=65,PAY_RELAY=66,
					MEALTIME_ENERGY=67,WAR_MANIC_ACT=68,LOGIN_REWARD=69,FLUSH_BATTLE_FIGHT=70,
					GROWTH_PLAN=71,OFFICER_SHOP=72,RESET_PROSPERITY=73,PEACE_ACT=74,RESET_SCIENCE=75,
					OWNED_OFFICER=76,FLUSH_HEAD=77,FLUSH_FLAG=78,FLUSH_FRIEND_EVNET=79,FLUSH_FRIENDINFO=80;
	/** 邮件刷新端口 
	 *  RM_MSG=70 删除邮件
	 */
	public static final int RM_MSG=3;
	
	/**
	 *  ALLIANCE_MESSAGE_SEND=1 刷新前台邮件 ALLIANCE_CHANGE_STAGE=2 新联盟战的阶段变化
	 *  ALLIANCE_CHANGE_BET=3 竞标值变化 		   ALLIANCE_WAR_TIME=4  联盟战的战斗时间
	 *  ALLIANCE_RESOURCE=5 物资变化 ALLIANCE_MATERIAL_RANK =7 物资排行榜
	 *  REMOVE_ALL_BATTLE_REPORT=10 移除所有战报 REMOVE_PLAYER_INLIST=11 移除联盟中的死亡玩家
	 *  PLAYER_LEAVE_ALLIANCE=12  刷新物资和捐献排行榜 ALLIANCE_OUTOF_RANK=13 联盟被挤出排行榜
	 */
	
	public static final int ALLIANCE_MESSAGE_SEND=1,ALLIANCE_CHANGE_STAGE=2,ALLIANCE_CHANGE_BET=3,
					ALLIANCE_WAR_TIME=4,ALLIANCE_RESOURCE=5,ALLIANCE_MATERIAL_RANK=7,PLAYER_INTEGRAL=8,	
				  VOTE_TIMES=9, RESET_PLAYER_SHIP=10,REMOVE_PLAYER_INLIST=11,PLAYER_LEAVE_ALLIANCE=12,
				ALLIANCE_OUTOF_RANK=13;
	
	/**FLUSH_COMRADE_FREIEND=1 刷新战友上下线数据   FLUSH_PLAYER_COMRADE=2 有新的战友加入**/
	public static final int RESET_COMRADE_FREIEND=1,RESET_PLAYER_COMRADE_INFO=2;
	/**
	 * 跨服积分赛 </p>CROSS_LEAGUE_PLAYER_INFO=1 玩家信息,CROSS_LEAGUE_SHOP=2
	 * 商店信息,CROSS_LEAGUE_CHALLENGE_LIST=3 挑战列表,CROSS_LEAGUE_WHOLE_INFO=4 全部信息
	 * */
	public static final int CROSS_LEAGUE_PLAYER_INFO=1,CROSS_LEAGUE_SHOP=2,
					CROSS_LEAGUE_CHALLENGE_LIST=3,CROSS_LEAGUE_WHOLE_INFO=4;
	/** 添加一个岛屿的广播 */
	public static void flushIsland(DSManager manager,NpcIsland land,
		CreatObjectFactory objectFactory)
	{
		if(land==null) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(FLUSH_ISLAND_INDEX);
		boolean bool=false;
		int playerSid=1;
		int state=0;
		int level=0;
		String playerName=null;
		// 获得本服务器的会话表
		SessionMap smap=manager.getSessionMap();
		Session sessions[]=smap.getSessions();
		Player player=null;
		Connect con=null;
		Player checkPlayer=null;
		boolean hostile=false;
		int [] flag=null;
		if(land.getPlayerId()!=0)
		{
			checkPlayer=objectFactory.getPlayerCache().loadPlayerOnly(
				land.getPlayerId()+"");
			if(checkPlayer==null)
			{
				land.setPlayerId(0);
				objectFactory.getIslandCache().load(land.getIndex()+"");
			}
			else
			{
				playerName=checkPlayer.getName();
				level=checkPlayer.getLevel();
				playerSid=checkPlayer.getSid();
				if(checkPlayer.checkService(PublicConst.NOT_FIGHT_BUFF,
					TimeKit.getSecondTime())!=null)
					state=PublicConst.NOT_FIGHT_STATE;
			}
		}
		boolean bossBool=false;
		// boss是否处于保护
		if(land.getIslandType()==NpcIsland.WORLD_BOSS)
		{
			WorldBoss boss=objectFactory.getWorldBossBySid(land.getSid());
			if(boss!=null)
			{
				if(boss.getProtectTime()>TimeKit.getSecondTime())
				{
					bossBool=true;
				}
			}
		}
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					if(land.getPlayerId()!=0)
					{
						// 是否是同盟
						if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
							&&checkPlayer.getAttributes(PublicConst.ALLIANCE_ID)!=null)
						{
							String paid=player.getAttributes(PublicConst.ALLIANCE_ID);
							String checkaid=checkPlayer.getAttributes(PublicConst.ALLIANCE_ID);
							if(paid.length()>0 && checkaid.length()>0 && checkaid.equals(paid))
								bool=true;
							Alliance palliance=objectFactory.getAlliance(
								TextKit.parseInt(paid),false);
							if(palliance!=null&&palliance.getHostile()!=null
								&&palliance.getHostile().length()!=0)
							{
								Alliance calliance=objectFactory.getAlliance(TextKit.parseInt(checkaid),false);
								if(SeaBackKit.isHostile(palliance,calliance))
									hostile=true;
							}
						}
							
					}
					if(checkPlayer!=null)
					{
						String checkaid=checkPlayer.getAttributes(PublicConst.ALLIANCE_ID);
						if(checkaid!=null)
						{
							Alliance calliance=objectFactory.getAlliance(
								TextKit.parseInt(checkaid),false);
							if(calliance!=null)
								flag=calliance.getFlag().getAllianceFlag();
						}
					}
					land.showBytesWrite(data,playerName,level,state,
						playerSid,bool,bossBool,hostile,flag);
					con.send(data);
				}
			}
		}
	}

	// 为[时事活动]预留
	// /** 添加多个岛屿的广播 */
	// public static void flushIsland(DSManager manager,Object[] lands,
	// CreatObjectFactory objectFactory)
	// {
	// if(lands==null) return;
	// // 获得本服务器的会话表
	// SessionMap smap=manager.getSessionMap();
	// Session sessions[]=smap.getSessions();
	// Player player=null;
	// Connect con=null;
	// ByteBuffer data=new ByteBuffer();
	// for(int i=0;i<sessions.length;i++)
	// {
	// if(sessions[i]!=null)
	// {
	// con=sessions[i].getConnect();
	// if(con!=null&&con.isActive())
	// {
	// player=(Player)sessions[i].getSource();
	// if(player==null) continue;
	// for(int j=0;j<lands.length;j++)
	// {
	// Player checkPlayer=null;
	// String playerName=null;
	// int playerSid=1;
	// int state=0;
	// int level=0;
	// boolean bool=false;
	// NpcIsland land=(NpcIsland)lands[j];
	// data.clear();
	// data.writeShort(FROE_PORT);
	// data.writeByte(FLUSH_ISLAND_INDEX);
	// if(land.getPlayerId()!=0)
	// {
	// checkPlayer=objectFactory.getPlayerCache().loadPlayerOnly(
	// land.getPlayerId()+"");
	// if(checkPlayer==null)
	// {
	// land.setPlayerId(0);
	// objectFactory.getIslandCache().load(land.getIndex()+"");
	// }
	// else
	// {
	// playerName=checkPlayer.getName();
	// level=checkPlayer.getLevel();
	// playerSid=checkPlayer.getSid();
	// if(checkPlayer.checkService(PublicConst.NOT_FIGHT_BUFF,
	// TimeKit.getSecondTime())!=null)
	// state=PublicConst.NOT_FIGHT_STATE;
	// }
	// }
	// boolean bossBool=false;
	// // boss是否处于保护
	// if(land.getIslandType()==NpcIsland.WORLD_BOSS)
	// {
	// WorldBoss boss=objectFactory.getWorldBossBySid(land.getSid());
	// if(boss!=null)
	// {
	// if(boss.getProtectTime()>TimeKit.getSecondTime())
	// {
	// bossBool=true;
	// }
	// }
	// }
	// if(land.getPlayerId()!=0)
	// {
	// // 是否是同盟
	// if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
	// &&checkPlayer
	// .getAttributes(PublicConst.ALLIANCE_ID)!=null)
	// {
	// if(player
	// .getAttributes(PublicConst.ALLIANCE_ID)
	// .equals(
	// checkPlayer
	// .getAttributes(PublicConst.ALLIANCE_ID)))
	// {
	// if(!player.getAttributes(
	// PublicConst.ALLIANCE_ID).equals(""))
	// {
	// bool=true;
	// }
	// }
	// }
	// }
	// land.showBytesWrite(data,playerName,level,state,
	// playerSid,bool,bossBool);
	// con.send(data);
	// }
	// }
	// }
	// }
	// }

	/** 一个任务得到完成条件了 推送给前台 */
	public static void sendChangeTaskPlayer(Player player,Task task)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(TASK_CHANGE);
		task.showBytesWrite(data,player);
		c.send(data);
	}

	/** 推送每日任务的5个随机任务 */
	public static void sendRandomTask(Player player,CombinationTask task)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SEND_RANDOM_TASK);
		data.writeShort(task.getSid());
		int randomSid[]=task.getRandomTasksSid();
		for(int i=0;i<randomSid.length;i++)
		{
			data.writeShort(randomSid[i]);
		}
		c.send(data);
	}

	/** 新加一个任务 */
	public static void sendAddTaskPlayer(Player player,Task task)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(ADD_TASK);
		task.showBytesWrite(data,player);
		c.send(data);
	}

	/** 推送奖励品 */
	public static void sendAward(Player player,Award award)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SEND_AWARD);
		award.bytesWrite(data);
		c.send(data);
	}

	/** 刷新前台资源 */
	public static void sendResetResources(Player player)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_RESOURCE);
		player.bytesWriteResources(data);
		c.send(data);
	}
	/** 刷新前台繁荣度 */
	public static void sendResetProsperity(Player player){
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_PROSPERITY);
		data.writeInt(player.getProsperityInfo()[0]);//繁荣度指数
		data.writeInt(player.getProsperityInfo()[2]);//繁荣度MAX
		c.send(data);
	}
	

	/** 刷新前台精力 */
	public static void sendResetActives(Session session,Player player)
	{
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_ACTIVIC);
		player.showBytesWriteActives(data,TimeKit.getSecondTime());
		c.send(data);
	}

	/** 刷新前台服务 */
	public static void sendResetService(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_SERVICE);
		player.showBytesWriteServices(data,TimeKit.getSecondTime());
		c.send(data);
	}

	/** 刷新前台包裹 */
	public static void sendResetBunld(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_BUNLD);
		player.getBundle().showBytesWrite(data,TimeKit.getSecondTime());
		c.send(data);
	}

	/** 刷新前台关卡 */
	public static void sendResetSelfPoint(Session session,Player player)
	{
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_SELFPOINT);
		player.bytesWriteSelfCheckPoint(data);
		c.send(data);
	}

	/** 刷新前台任务 */
	public static void sendResetTask(Session session,Player player)
	{
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_TASK);
		player.getTaskManager().showBytesWrite(data,TimeKit.getSecondTime());
		c.send(data);
	}

	/** 刷新船只 */
	public static void sendResetTroops(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SEND_SHIPS);
		player.getIsland()
			.showBytesWriteTroops(data,TimeKit.getSecondTime());
		c.send(data);
	}

	/** 整理玩家邮件 */
	public static void collatePlayerMail(Message message,Player player,
		CreatObjectFactory factory)
	{
		if(message.getMessageType()==Message.SYSTEM_TYPE)
			return;
		else if(message.getMessageType()==Message.FIGHT_TYPE)// 报告
		{
			Object[] objs=getNotServerMessages(Message.FIGHT_TYPE,player,
				false,factory);
			if(objs!=null&&objs.length>MessagePort.REPORT_SIZE)
			{
				for(int i=objs.length-1;i>=MessagePort.REPORT_SIZE;i--)
				{
					if(objs[i]==null) continue;
					Message mes=(Message)objs[i];
					mes.addReciveState(Message.ONE_DELETE);
					mes.setDelete(Message.DELETE);
				}
			}

		}
		else if(message.getMessageType()==0)// 收件箱
		{
			Object[] objs=getNotServerMessages(0,player,false,factory);
			if(objs!=null&&objs.length>MessagePort.GET_SIZE)
			{
				for(int i=objs.length-1;i>=MessagePort.GET_SIZE;i--)
				{
					if(objs[i]==null) continue;
					Message mes=(Message)objs[i];
					mes.addReciveState(Message.ONE_DELETE);
					if(mes.getSendId()==player.getId())
						mes.addState(Message.ONE_DELETE);
				}
			}
		}

	}

	/** 获取非全服邮件(收件箱，报告) */
	public static Object[] getNotServerMessages(int messageType,
		Player player,boolean selfSend,CreatObjectFactory factory)
	{
		ArrayList messageList=factory.getMessageCache().getMessageListById(
			player.getId());
		// 玩家收件箱
		ArrayList messages=new ArrayList();
		if(messageList!=null)
		{
			for(int i=0;i<messageList.size();i++)
			{
				Message message=(Message)messageList.get(i);
				if(message.getDelete()==Message.ONE_DELETE) continue;
				if(message.getReceiveId()!=player.getId()) continue;
				if(message.getSendId()==message.getReceiveId()) continue;
				if(message.getMessageType()!=messageType) continue;
				if(message.checkReciveState(Message.ONE_DELETE)) continue;
				messages.add(message);
			}
		}
		Object[] objects=messages.toArray();
		SetKit.sort(objects,MessageComparator.getInstance());
		return objects;
	}

	/** 发送一封邮件给玩家 */
	public static void sendRevicePlayerMessage(Player player,
		Message message,int state,CreatObjectFactory factory)
	{
		collatePlayerMail(message,player,factory);
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(EMAIL_PORT);
		data.writeByte(0);
		// 一封邮件
		data.writeByte(1);
		message.showBytesWrite(data,state,"",player);
		c.send(data);
	}

	/** 发送一封邮件给玩家 */
	public static void sendRenaRevicePlayerMessage(Player player,
		Message message,int state,CreatObjectFactory factory)
	{
		collatePlayerMail(message,player,factory);
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(EMAIL_PORT);
		data.writeByte(0);
		// 一封邮件
		data.writeByte(1);
		message.showBytesWrite(data,state,message.getContent(),player);
		c.send(data);
	}

	/** SEND_EXP刷新经验 */
	public static void sendExp(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SEND_EXP);
		data.writeLong(player.getExperience());
		data.writeByte(player.getLevel());
		c.send(data);
	}

	/** RESET_HURT_TROOPS 刷新伤兵 */
	public static void resetHurtTroops(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_HURT_TROOPS);
		player.getIsland().showBytesWritehurtsTroops(data,
			TimeKit.getSecondTime());
		c.send(data);
	}

	/** resetHonor 刷新声望 */
	public static void resetHonor(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_HONOR_YES);
		player.bytesWriteHonor(data);
		c.send(data);
	}

	/** 荣誉积分 */
	public static void resetHonorScore(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_HONOR_SCROE);
		data.writeInt(player.getHonorScore());
		c.send(data);
	}

	/** 刷新主力舰队 */
	public static void resetMainGroup(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_MAIN_GROUP);
		player.getIsland().showBytesWriteMainGroup(data);
		c.send(data);
	}

	/** 前台显示提示消息 */
	public static void sendMessageView(Player player,String str)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MESSAGE_VIEW);
		data.writeUTF(str);
		c.send(data);
	}

	/** 刷新单个建筑 */
	public static void resetOneBuild(Player player,PlayerBuild build)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_ONE_BUILD);
		data.writeByte(build.getIndex());
		build.showBytesWrite(data,TimeKit.getSecondTime());
		c.send(data);
	}

	/** 删除掉某个事件 */
	public static void deleteFightEvent(Player player,FightEvent event)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SEND_FIGHT_EVENT);
		data.writeByte(1);
		data.writeInt(event.getId());
		c.send(data);
	}

	/** 刷新事件 */
	public static void sendFightEvent(Player player,FightEvent event,
		CreatObjectFactory objectFactory)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SEND_FIGHT_EVENT);
		sendResetTroops(player);
		// 删除的时候 刷新下玩家资源和舰队
		if(event.getDelete()==FightEvent.DELETE_TYPE)
		{
			data.writeByte(1);
			data.writeInt(event.getId());
			c.send(data);
			return;
		}
		else
		{
			data.writeByte(0);
		}
		SeaBackKit.showByteswrite(data,TimeKit.getSecondTime(),event,
			objectFactory);
		c.send(data);
	}

	/** 刷新玩家岛屿状态 */
	public static void sendPlayerIslandState(SessionMap map,int state,
		int index)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(PLAYER_ISLAND_STATE_CHANGE);
		data.writeInt(index);
		data.writeByte(state);
		map.send(data);
	}

	/** 刷新联盟 */
	public static void sendAllicace(ByteBuffer data,Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		c.send(data);
	}
	/** 刷新所有活动 */
	public static void sendActivityFlush(SessionMap map,ByteBuffer infoData)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(ACTIVITY_FLSUH);
		data.write(infoData.getArray(),infoData.offset(),infoData.length());
		map.send(data);
	}
	
	/**刷新所有新型活动**/
	public static void sendActivityNewFlush(SessionMap map)
	{
		ActivityContainer container=ActivityContainer.getInstance();
		if(container.getActivityNewLength()==0) return ;
		ByteBuffer data=new ByteBuffer();
		Session[] sessions=map.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			data.clear();
			data.writeShort(ACT_PORT);
			data.writeByte(WAR_MANIC_ACT);
			container.showBytesWriteNew(player,data);
			c.send(data);
		}
	}
	
	/** 刷新限时抽奖活动 */
	public static void sendLuckyAwardActivty(SessionMap map)
	{
		ByteBuffer data=new ByteBuffer();
		Session[] sessions=map.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			data.clear();
			data.writeShort(FROE_PORT);
			data.writeByte(LUCKY_DRAWCOUNT);// 32
			data.writeShort(player.getDrawDay());// 轮盘抽奖次数
			data.writeShort(player.getClassicDrawDay());// 经典抽奖次数
			c.send(data);
		}
	}
	/** 刷新打折活动 */
	public static void sendDiscountActivty(SessionMap map)
	{
		ByteBuffer data=new ByteBuffer();
		Session[] sessions=map.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			data.clear();
			data.writeShort(FROE_PORT);
			data.writeByte(DISCOUNT_ACTIVITY);// 31
			player.showByteWriteDiscount(data);
			c.send(data);
		}
	}
	/** 刷新限时购买活动 */
	public static void sendLimitSaleActivty(SessionMap map)
	{
		ByteBuffer data=new ByteBuffer();
		Session[] sessions=map.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			data.clear();
			data.writeShort(FROE_PORT);
			data.writeByte(LIMIT_SALE_ACTIVITY);// 26
			player.showByteWriteLimitSale(data);
			c.send(data);
		}

	}
	/** 刷新个人限时购买 */
	public static void sendLimitSaleFlush(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(LIMIT_SALE_ACTIVITY);// 26
		player.showByteWriteLimitSale(data);
		c.send(data);
	}

	/** 刷新据点信息 (单个) */
	public static void sendGround(CreatObjectFactory objfactory,
		BattleGround ground,AllianceFightManager manager)
	{
		Object[] objs=objfactory.getAllianceMemCache().getCacheMap()
			.valueArray();
		Alliance alliance=null;
		Player player=null;
		ByteBuffer data=new ByteBuffer();
		for(int i=0;i<objs.length;i++)
		{
			alliance=(Alliance)((AllianceSave)objs[i]).getData();
			IntList list=alliance.getPlayerList();
			for(int k=0;k<list.size();k++)
			{
				player=objfactory.getPlayerById(list.get(k));
				if(player!=null&&player.getSource()!=null)
				{
					Connect c=((Session)player.getSource()).getConnect();
					if(c==null||!c.isActive()) return;
					data.clear();
					data.writeShort(FROE_PORT);
					data.writeByte(AFIGHT_GROUND);// 27
					ground.showBytesWrite(manager,data);
					c.send(data);
				}
			}
		}
	}
	/** 刷新全部据点信息 */
	public static void flushGrounds(Object[] grounds,Player player,
		AllianceFightManager manager)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		for(int i=0;i<grounds.length;i++)
		{
			data.clear();
			data.writeShort(FROE_PORT);
			data.writeByte(AFIGHT_GROUND);// 27
			((BattleGround)((BattleGroundSave)grounds[i]).getData())
				.showBytesWrite(manager,data);
			c.send(data);
		}

	}
	/** 刷新库存船只 （全船） */
	public static void sendAfightFleet(CreatObjectFactory objfactory,
		Alliance alliance,AllianceFight afight)
	{
		if(alliance==null||afight==null) return;
		IntList list=alliance.getPlayerList();
		Player player=null;
		ByteBuffer data=new ByteBuffer();
		for(int k=0;k<list.size();k++)
		{
			player=objfactory.getPlayerById(list.get(k));
			if(player!=null&&player.getSource()!=null)
			{
				Connect c=((Session)player.getSource()).getConnect();
				if(c==null||!c.isActive()) return;
				data.clear();
				data.writeShort(FROE_PORT);
				data.writeByte(AFIGHT_FLEET);// 28
				afight.showBytesWriteFleets(data);
				c.send(data);
			}
		}
	}
	/** 刷新生产队列 */
	public static void sendUpShip(CreatObjectFactory objfactory,
		Alliance alliance,AllianceFight afight)
	{
		IntList list=alliance.getPlayerList();
		Player player=null;
		ByteBuffer data=new ByteBuffer();
		for(int k=0;k<list.size();k++)
		{
			player=objfactory.getPlayerById(list.get(k));
			if(player!=null&&player.getSource()!=null)
			{
				Connect c=((Session)player.getSource()).getConnect();
				if(c==null||!c.isActive()) return;
				data.clear();
				data.writeShort(FROE_PORT);
				data.writeByte(AFIGHT_UPSHIP);// 29
				afight.flushUpShip(data);
				c.send(data);
			}
		}
	}
	/** 刷新号角 */
	public static void sendAfightHorn(CreatObjectFactory objfactory,
		Alliance alliance,AllianceFight afight)
	{
		IntList list=alliance.getPlayerList();
		Player player=null;
		ByteBuffer data=new ByteBuffer();
		for(int k=0;k<list.size();k++)
		{
			player=objfactory.getPlayerById(list.get(k));
			int percent=afight.getHorn().getCPercent();
			if(player!=null&&player.getSource()!=null)
			{
				Connect c=((Session)player.getSource()).getConnect();
				if(c==null||!c.isActive()) return;
				data.clear();
				data.writeShort(FROE_PORT);
				data.writeByte(AFIGHT_HORN);// 30
				data.writeByte(percent);
				c.send(data);
			}
		}
	}

	/** 刷新关卡加成 */
	public static void sendPointBuff(Player player,int type)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(POINT_BUFF);// 33
		data.writeShort(PublicConst.SHOW_SIDS[type]);
		data.writeByte(player.getPointBuff()[type]);
		c.send(data);
	}
	/** 刷新成成就,isFlush=是否跳过成就链判定进行强制刷新 */
	public static void sendFlushAchieve(Player player,int sid,long cvalue,
		boolean isFlush)
	{
		// 是否跳过成就链判定进行强制刷新
		if(!isFlush)
		{
			Achievement achieve=(Achievement)Achievement.factory
				.getSample(sid);
			if(achieve==null) return;
			if(achieve.getAchieveType()==Achievement.OTHER) return;
			int progress=player.getAchieveProgress(sid);
			// 如果当前成就链全部完成，不再进行推送
			if(progress>=achieve.getNeedValue().length) return;
		}
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(ACHIEVE_FLUSH);// 34
		data.writeShort(sid);
		data.writeByte(player.getAchieveProgress(sid));
		data.writeLong(cvalue);
		data.writeInt(player.getAchieveScore());
		data.writeInt(AchieveManager.instance.getMaxScore());
		data.writeInt(player.getAchieveScoreRank());
		c.send(data);
	}
	/** 刷新天降好礼 */
	public static void sendVaribleAwardActivty(SessionMap smap)
	{
		VaribleAwardActivity activity=(VaribleAwardActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.VARIBLE_AWARD,0);
		if(activity==null) return;
		ByteBuffer data=new ByteBuffer();
		Session[] sessions=smap.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			if(!activity.isPlayerNeedPush(player)) continue;
			data.clear();
			data.writeShort(FROE_PORT);
			data.writeByte(VARIBLE_AWARD);// 35
			activity.showByteWrite(data,player);
			c.send(data);
		}
	}
	/** 刷新个人天降好礼 */
	public static void sendPlayerVaribleAward(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		VaribleAwardActivity activity=(VaribleAwardActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.VARIBLE_AWARD,0);
		if(activity==null) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(VARIBLE_AWARD);// 35
		activity.showByteWrite(data,player);
		c.send(data);

	}

	/** 刷新联盟邀请提示 */
	public static void sendAllianceInvitation(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(ALLIANCE_INVITATION);// 36
		c.send(data);
	}

	/** 刷新前台vip成长 **/
	public static void sendPlayerVIPGrowthPoint(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(PLAYER_VIP_POTIN);// 37
		c.send(data);
	}
	/** 刷新前台GEM记录 **/
	public static void sendGemChange(Player player,boolean flag,int gems)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		if(gems==0) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(GEM_CHANGE);// 38
		data.writeBoolean(flag);
		data.writeInt(gems);
		c.send(data);
	}
	/** 刷新前台月卡信息 **/
	public static void sendMouthCard(Player player)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MOUTH_CARD);// 39
		String etime=player.getAttributes(PublicConst.END_TIME);
		int timenow=TimeKit.getSecondTime();
		int endtime=Integer.parseInt(etime);
		if(endtime<=timenow)
		{
			data.writeInt(0);
			data.writeInt(0);
			data.writeByte(2);
		}
		else
		{
			data.writeInt((endtime-timenow)/PublicConst.DAY_SEC+1);
			data.writeInt(SeaBackKit.getTimesnight()-timenow);
			String awardtime=player.getAttributes(PublicConst.AWARD_TIME);
			if(awardtime==null||awardtime=="")
				data.writeByte(1);
			else
			{
				int atime=Integer.parseInt(awardtime);
				if(SeaBackKit.isSameDay(atime,timenow))
					data.writeByte(0);// 判断当天是否可以领取宝石
				else
					data.writeByte(1);
			}
		}
		c.send(data);
	}

	/** 刷新首冲奖励 */
	public static void sendFPaward(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(FP_AWRD);// 40
		data.writeBoolean(player.getCanFAward());
		c.send(data);
	}

	/** 刷新建筑增益信息 */
	public static void sendBuildServiceInfo(Player player,int[] indexs,
		int length)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(BUILD_AUTO_START);// 41
		data.writeByte(length);
		for(int i=0;i<length;i++)
		{
			player.getIsland().getBuildByIndex(indexs[i],null)
				.showBytesWrite(data,TimeKit.getSecondTime());
		}
		c.send(data);
	}

	/** 到达特定级数推送给前台 */
	public static void sendLevel2Push(Player player,int level)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(LEVEL_TO_PUSH);// 42
		data.writeByte(level);
		c.send(data);
	}

	/** 推送特定服务状态 */
	public static void sendServiceStauts(Player player,int sid,boolean stauts)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SERVICE_STAUTS);// 43
		data.writeShort(sid);
		data.writeBoolean(stauts);
		c.send(data);
	}

	/** 刷新公告 */
	public static void sendAnnouncement(SessionMap smap,
		Announcement announcement)
	{
		ByteBuffer data=new ByteBuffer();
		Session[] sessions=smap.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			if(announcement.getReadplayer().isEmpty()
				||announcement.getReadplayer().contain(player.getId()))
			{
				data.clear();
				data.writeShort(FROE_PORT);
				data.writeByte(ANNOUNCE_PUSH);// 刷新 公告46
				announcement.showByteWrite(data);
				c.send(data);
			}
		}
	}
	/** 移除公告 */
	public static void sendRemoveAnnouncement(SessionMap smap,
		Announcement announcement)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(ANNOUNCE_REMOVE);// 移除公告47
		data.writeInt(announcement.getId());
		smap.send(data);
	}
	/** 推送活跃度任务进度 */
	public static void sendVitalityTask(Player player,int sid,int nowCount,
		boolean isMaxCount)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(VITALITY_TASK);// 44
		data.writeShort(sid);
		data.writeByte(nowCount);
		data.writeInt(player.getVitality(null)[1]);
		data.writeBoolean(isMaxCount);
		c.send(data);
	}

	/** 推送玩家装备信息 */
	public static void sendEquipInfo(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(EQUIP_INFO);// 45
		player.getEquips().showBytesWrite(data);
		c.send(data);
	}

	/** 推送每日折扣商品状态 */
	public static void sendDateOffPropState(Player player)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(DATE_OFF);// 每日折扣状态49
		player.showByteWriteDatePriceOff(data);
		c.send(data);
	}

	/** 推送每日折扣商品售卖信息 */
	public static void sendDateOffPropState(SessionMap smap)
	{
		Session[] sessions=smap.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=PlayerKit.getInstance().getPlayer(sessions[i]);
			sendDateOffPropState(player);
		}
	}

	/** 推送累计消费活动信息 */
	public static void sendConsumeGemsState(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(CONSUME_GEMS);// 累计消费状态53
		ConsumeGemsActivity.showBytesWrite(player,data);
		c.send(data);
	}

	/** 在线玩家推送累计消费活动信息 */
	public static void sendConsumeGemsState(SessionMap smap)
	{
		Session[] sessions=smap.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=PlayerKit.getInstance().getPlayer(sessions[i]);
			sendConsumeGemsState(player);
		}
	}

	/** 推送宝石双倍返利活动购买信息 */
	public static void sendDoubleGemsInfo(Player player,
		DoubleGemsAcitivity activity)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(DOUBLE_GEMS);// 双倍返利状态54
		activity.showByteWrite(data,player);
		c.send(data);
	}

	/** 推送宝石双倍返利活动购买信息 */
	public static void sendDoubleGemsInfo(SessionMap smap,
		DoubleGemsAcitivity activity)
	{
		Session[] sessions=smap.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=PlayerKit.getInstance().getPlayer(sessions[i]);
			sendDoubleGemsInfo(player,activity);
		}
	}

	/** 推送第三方支付方式状态 */
	public static void sendRechangeState(SessionMap smap)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RECHANGEPAY_STATE);// 第三方支付状态50
		data.writeBoolean(PublicConst.RECHANGE_STATLE);
		smap.send(data);
	}

	/** 推送第三方支付方式状态 */
	public static void sendRechangeState(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RECHANGEPAY_STATE);// 第三方支付状态50
		data.writeBoolean(PublicConst.RECHANGE_STATLE);
		c.send(data);
	}

	/** 推送邀请码，兑换码等开关状态 */
	public static void sendSwitchState(SessionMap smap)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SWITCH_STATE);
		for(int j=0;j<PublicConst.SWITCH_STATE.length;j++)
		{
			data.writeBoolean(PublicConst.SWITCH_STATE[j]);
		}
		smap.send(data);
	}
	/** 刷新累计充值信息 */
	public static void sendTotalBuyAwardActivty(SessionMap smap,
		TotalBuyActivity activity)
	{
		ByteBuffer data=new ByteBuffer();
		Session[] sessions=smap.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			data.clear();
			data.writeShort(FROE_PORT);
			data.writeByte(TOTALBUY_AWARD);// 52
			activity.showByteWrite(data,player);
			c.send(data);
		}
	}

	/** 刷新个人累计充值信息 */
	public static void sendPlayerTotalBuyAward(TotalBuyActivity activity,
		Player player,int awardSid)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;

		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(SHORT_TOTALBUY_AWARD);// 55
		activity.showShortByteWrite(data,awardSid,player);
		c.send(data);
	}

	/** 刷新滚动系统信息 */
	public static void sendScrollMessage(DSManager manager,String content)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SCROLL_MESSAGE);// 52
		data.writeUTF(content);
		manager.getSessionMap().send(data);
	}

	/** 刷新竞技场重置信息 */
	public static void resetArenaInfo(Player player,ByteBuffer arenaData)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(ARENA_RESET);// 60
		data.write(arenaData.getArray(),arenaData.offset(),arenaData.top()
			-arenaData.offset());
		c.send(data);
	}

	/** 刷新玩家名称 */
	public static void sendPlayerName(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MODIFY_PNAME);// 刷新玩家名称
		data.writeUTF(player.getName());
		c.send(data);
	}

	/** 刷新联盟名称 */
	public static void sendAllianceName(Alliance alliance,
		CreatObjectFactory factory)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MODIFY_ANAME);// 刷新玩家名称
		data.writeUTF(alliance.getName());
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) continue;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) continue;
			c.send(data);
		}
	}

	/** 当修改玩家名称的时候有联盟就刷新联盟内的名称 */
	public static void sendmodifyPAname(Alliance alliance,
		CreatObjectFactory factory,int pid,String name)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MODIFY_PANAME);// 刷新玩家名称
		data.writeInt(pid);
		data.writeUTF(name);
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) continue;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) continue;
			c.send(data);
		}
	}

	/** 重新计算战力并推送到前台 */
	public static void sendFightScore(Player player,
		CreatObjectFactory factory,boolean reset,int reason)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(FIGHT_SCORE);// 刷新玩家战力
		if(reset)
			SeaBackKit.setPlayerFightScroe(player,factory);
		data.writeInt(player.getFightScore());
		data.writeByte(reason);
		c.send(data);
	}

	/** 弹出验证器 */
	public static void sendPopupVertify(Player player,VertifyManager manager)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(VERTIFY_PORT);
		data.writeByte(VERTIFY_POPUP);// 弹出验证器
		Vertify v=(Vertify)manager.player_vertify.get(player.getId());
		data.writeInt(v.getVertifyWrongCount());
		data.writeInt(PublicConst.VERTIFY_TIME);
		data.writeInt(PublicConst.MAX_COUNT);
		c.send(data);
	}
	
	/** 充值接力活动 */
	public static void sendPayRelayActivity(Player player,int payDays)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(PAY_RELAY);
		data.writeByte(payDays);
		c.send(data);
	}
	
	/** 推送行军路线 */
	public static void sendMarchLine(CreatObjectFactory objectFactory,FightEvent event)
	{
		if(!WorldScenePort.march)return;
		int s_index=event.getSourceIslandIndex();
		int a_index=event.getAttackIslandIndex();
		int s_pid=objectFactory.getIslandByIndex(s_index+"").getPlayerId();
		int a_pid=objectFactory.getIslandByIndex(a_index+"").getPlayerId();
		if(a_pid==0)
		{
			int eid=objectFactory.getIslandByIndex(a_index+"")
				.getTempAttackEventId();
			if(eid!=0)
			{
				FightEventSave holdsave=(FightEventSave)objectFactory
					.getEventCache().getCacheMap().get(eid);
				if(holdsave!=null&&holdsave.getData()!=null)
				{
					a_pid=holdsave.getData().getPlayerId();
				}
			}
		}
		Player s_player=objectFactory.getPlayerById(s_pid);
		Player a_player=objectFactory.getPlayerById(a_pid);
//		System.out.println("---------s_player--------:"+s_player);
//		System.out.println("---------a_player--------:"+s_player);
		ByteBuffer data=new ByteBuffer();
		if(s_player!=null)
		{
			Session s=(Session)s_player.getSource();
			if(s!=null)
			{
				Connect c=s.getConnect();
				if(c!=null&&c.isActive())
				{
					data.clear();
					data.writeShort(FROE_PORT);
					data.writeByte(MARCH_LINE);
					data.writeInt(1);
					data.writeInt(event.getId());
					if(event.getEventState()!=FightEvent.RETRUN_BACK)
					{
						data.writeInt(event.getSourceIslandIndex());
						data.writeInt(event.getAttackIslandIndex());
					}
					else
					{
						data.writeInt(event.getAttackIslandIndex());
						data.writeInt(event.getSourceIslandIndex());
					}
					if(event.getEventState()!=FightEvent.HOLD_ON)
					{
						data.writeInt(event.getNeedTime()+event.getCreatAt()
							-TimeKit.getSecondTime());
						data.writeInt(event.getNeedTime());
					}
					else
					{
						data.writeInt(0);
						data.writeInt(0);
					}
					data.writeByte(1);
					c.send(data);
				}
			}
		}
		if(a_player!=null)
		{
			Session s=(Session)a_player.getSource();
			if(s!=null)
			{
				Connect c=s.getConnect();
				if(c!=null&&c.isActive())
				{
					data.clear();
					data.writeShort(FROE_PORT);
					data.writeByte(MARCH_LINE);
					data.writeInt(1);
					data.writeInt(event.getId());
					if(event.getEventState()!=FightEvent.RETRUN_BACK)
					{
						data.writeInt(event.getSourceIslandIndex());
						data.writeInt(event.getAttackIslandIndex());
					}
					else
					{
						data.writeInt(event.getAttackIslandIndex());
						data.writeInt(event.getSourceIslandIndex());
					}
					if(event.getEventState()!=FightEvent.HOLD_ON)
					{
						data.writeInt(event.getNeedTime()+event.getCreatAt()
							-TimeKit.getSecondTime());
						data.writeInt(event.getNeedTime());
					}
					else
					{
						data.writeInt(0);
						data.writeInt(0);
					}
					Alliance al=null;
					String aid=a_player
						.getAttributes(PublicConst.ALLIANCE_ID);
					if(aid!=null&&!aid.equals(""))
					{
						al=objectFactory.getAlliance(TextKit.parseInt(aid),
							false);
					}
					int stype=2;
					int spid=s_player==null?0:s_player.getId();
					if(al!=null&&al.getPlayerList().contain(spid)
						&&event.getType()==FightEvent.ATTACK_HOLD)
					{
						stype=1;
					}
					data.writeByte(stype);
					c.send(data);
				}
			}
		}
		// Session[]
		// ss=objectFactory.getDsmanager().getSessionMap().getSessions();
		// ByteBuffer data=new ByteBuffer();
		// for(int i=0;i<ss.length;i++)
		// {
		// if(ss[i]==null||ss[i].getSource()==null) continue;
		// Connect c=ss[i].getConnect();
		// if(c==null||!c.isActive()) continue;
		// Player player=(Player)ss[i].getSource();
		// data.clear();
		// data.writeShort(FROE_PORT);
		// data.writeByte(MARCH_LINE);
		// data.writeInt(1);
		// data.writeInt(event.getId());
		// if(event.getEventState()!=FightEvent.RETRUN_BACK)
		// {
		// data.writeInt(event.getSourceIslandIndex());
		// data.writeInt(event.getAttackIslandIndex());
		// }
		// else
		// {
		// data.writeInt(event.getAttackIslandIndex());
		// data.writeInt(event.getSourceIslandIndex());
		// }
		// data.writeInt(event.getNeedTime());
		// data.writeInt(TimeKit.getSecondTime()-event.getCreatAt());
		// Alliance al=null;
		// String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
		// if(aid!=null&&!aid.equals(""))
		// {
		// al=objectFactory.getAlliance(TextKit.parseInt(aid),false);
		// }
		// int index=objectFactory.getIslandCache().getPlayerIsLandId(
		// player.getId());
		// int stype=3;
		// if(index==event.getAttackIslandIndex())
		// {
		// if(al!=null&&al.getPlayerList().contain(event.getPlayerId()))
		// {
		// stype=1;
		// }
		// else
		// {
		// stype=2;
		// }
		// }
		// else
		// {
		// stype=player.getId()==event.getPlayerId()?1:3;
		// }
		// data.writeByte(stype);// 1 己方 2敌方 3中立
		// c.send(data);
		// }
	}
	
	/** 推送行军路线 */
	public static void sendMarchLine(CreatObjectFactory objectFactory,Player holdPlayer,FightEvent event)
	{
		if(!WorldScenePort.march) return;
		ByteBuffer data=new ByteBuffer();
		if(holdPlayer!=null)
		{
			Session s=(Session)holdPlayer.getSource();
			if(s!=null)
			{
				Connect c=s.getConnect();
				if(c!=null&&c.isActive())
				{
					data.clear();
					data.writeShort(FROE_PORT);
					data.writeByte(MARCH_LINE);
					data.writeInt(1);
					data.writeInt(event.getId());
					data.writeInt(event.getAttackIslandIndex());
					data.writeInt(event.getSourceIslandIndex());
					data.writeInt(0);
					data.writeInt(0);
					data.writeByte(2);
					c.send(data);
				}
			}
		}
	}
	/**
	 * 推送战争狂人
	 */
	public static void sendWarManic(WarManicActivity act,Player player)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(ACT_PORT);
		data.writeByte(WAR_MANIC);
		act.showBytesWrite(data,player);
		c.send(data);
	}
	/**
	 * 全服推送战争狂人
	 */
	public static void sendWarManicAll(WarManicActivity act,SessionMap smap)
	{
		Session[] ss=smap.getSessions();
		ByteBuffer data=new ByteBuffer();
		for(int i=0;i<ss.length;i++)
		{
			if(ss[i]==null) continue;
			Player player=(Player)ss[i].getSource();
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) return;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) return;
			data.clear();
			data.writeShort(ACT_PORT);
			data.writeByte(WAR_MANIC_ACT);
			data.writeByte(1);
			act.showByteWriteNew(data,player,null);
			c.send(data);
		}
	}
	
	/** 推送新兵福利(个人任务) */
	public static void sendRecruit(int taskType,Player player)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(RECRUIT_PORT);
		data.writeByte(2);//任务
		RecruitKit.sendTask(data,taskType,player);
		c.send(data);
	}
	
	/** 推送福利(活动条目) */
	public static void sendRecruitAll(RecruitWelfareManager rmanager,Player player)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(RECRUIT_PORT);
		data.writeByte(1);// 活动条目
		rmanager.showBytesWrite(player,data);
		c.send(data);
	}
	
	/** 推送玩家军官信息 */
	public static void sendOfficerInfo(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(OFFICER_PORT);
		data.writeByte(OFFICER_INFO);// 63
		player.getOfficers().showBytesWrite(data,player);
		c.send(data);
	}
	

	/**全服刷新联盟竞标信息**/
	public static void sendAllianceBetInfo(BattleIsland battleIsland,
		CreatObjectFactory objectFactory)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(ALLIANCE_CHANGE_BET);
		if(battleIsland!=null)
		{
			// 岛屿的长度
			data.writeShort(1);
			battleIsland.showBytesBattleInfo(data,objectFactory);
			objectFactory.getDsmanager().getSessionMap().send(data);
			return;
		}
		Object[] bIlslandsaves=objectFactory.getBattleIslandMemCache()
			.loadBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0)
			data.writeShort(0);
		else
		{
			data.writeShort(bIlslandsaves.length);
			for(int i=0;i<bIlslandsaves.length;i++)
			{
				BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
					.getData();
				bIlsland.showBytesBattleInfo(data,objectFactory);
			}
		}
		objectFactory.getDsmanager().getSessionMap().send(data);
	}
	
	
	/**全服刷新联盟阶段信息变化**/
	public static void sendAllianceStageInfo(Stage stage,
		CreatObjectFactory objectFactory)
	{
		ByteBuffer data=new ByteBuffer();
		if(stage.getStage()!=Stage.STAGE_THREE)
		{
			data.writeShort(BATTLE_FIGHT);
			data.writeByte(ALLIANCE_CHANGE_STAGE);
			stage.showBytesWriteStage(data);
			data.writeBoolean(false);
			data.writeBoolean(false);
			stage.showByteWrite(data,stage.getStage()==Stage.STAGE_FOUR);
			objectFactory.getDsmanager().getSessionMap().send(data);
		}
		else
		{
			DSManager manager=objectFactory.getDsmanager();
			Session sessions[]=manager.getSessionMap().getSessions();
			for(int i=0;i<sessions.length;i++)
			{
				if(sessions[i]==null) continue;
				Connect c=sessions[i].getConnect();
				if(c==null || !c.isActive()) continue;
				Player player=(Player)sessions[i].getSource();
				if(player==null) continue;
				String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
				if(aid==null) continue;
				Alliance alliance=objectFactory.getAlliance(
					TextKit.parseInt(aid),false);
				if(alliance==null) continue;
				data.writeShort(BATTLE_FIGHT);
				data.writeByte(ALLIANCE_CHANGE_STAGE);
				Object object=objectFactory.getBattleIslandMemCache().load(alliance.getBetBattleIsland()+"");
				if(object==null)
				{
					stage.showBytesWriteStage(data);
					data.writeBoolean(false);
					data.writeBoolean(false);
					stage.showByteWrite(data,false);
					c.send(data);
					continue;
				}
				BattleIsland battleIsland=(BattleIsland)object;
				stage.showBytesWriteStage(data);
				data.writeBoolean(true);
				data.writeBoolean(battleIsland.isHavePlayer(player
					.getId()));
				stage.showByteWrite(data,false);
				c.send(data);
			}
		}
	}
	
	/**刷新联盟战 战斗时间**/
	public static void sendAllianceWarTime(IntList list,int stime,CreatObjectFactory factory,int times)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(ALLIANCE_WAR_TIME);
		data.writeInt(stime);
		data.writeShort(times);
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) continue;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) continue;
			c.send(data);
		}
	}
	
	/**刷新排行榜**/
	public static void  sendPlayerMaterialRank(IntList list,CreatObjectFactory factory,Player sendPlayer,int num,int rank)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(ALLIANCE_MATERIAL_RANK);
		data.writeInt(sendPlayer.getId());
		data.writeUTF(sendPlayer.getName());
		data.writeByte(rank);
		data.writeByte(sendPlayer.getLevel());
		data.writeInt(sendPlayer.getFightScore());
		data.writeInt(num);
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) continue;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) continue;
			c.send(data);
		}
	}
	
	/**刷新积分*/
	public static void  sendPlayerIntegral(Player player)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(PLAYER_INTEGRAL);
		data.writeInt(player.getIntegral());
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		c.send(data);
	}
	/**刷新联盟战的捐献次数**/
	public static void sendPlayerVoteTimes(Player player,int times)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(VOTE_TIMES);
		data.writeByte(times);
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		c.send(data);
	}
	
	/**刷新官员联盟物资**/
	public  static  void  sendAllianceWarResource(CreatObjectFactory factory,Alliance alliance)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(ALLIANCE_RESOURCE);
		data.writeLong(alliance.getMaterial());
		data.writeLong(alliance.getSciencepoint());
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) continue;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) continue;
			c.send(data);
		}
	}
	
	/**刷新竞标被挤出排行榜**/
	public  static  void  sendOutOfRank(CreatObjectFactory factory,Alliance alliance)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(ALLIANCE_OUTOF_RANK);
		IntList list=alliance.getVicePlayers();
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) continue;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) continue;
			c.send(data);
		}
		int  masterId=alliance.getMasterPlayerId();
		Player player=factory.getPlayerById(masterId);
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		c.send(data);
	
	}
	/**刷新玩家报名船只信息**/
	public static void  sendResetPlayerShip(Player player,BattleIsland battleIsland)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(RESET_PLAYER_SHIP);
		battleIsland.showBytesWriteShips(data,player.getId());
		c.send(data);
	}	
	
	/**在联盟战对战中识别了移除某一个玩家**/
	public static void sendRemovePlayer(CreatObjectFactory factory,int playerId,int allianceId,int battleIsland,String playerName,boolean alive)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(REMOVE_PLAYER_INLIST);
		data.writeInt(battleIsland);
		data.writeInt(allianceId);
		data.writeInt(playerId);
		data.writeUTF(playerName);
		if(alive)
			data.writeByte(0);
		else
			data.writeByte(1);
		factory.getDsmanager().getSessionMap().send(data);
	}
	
	/** 给联盟所有人发送联盟战邮件 */
	public static void sendAllianceReport(IntList list,CreatObjectFactory factory,Object[] messages)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(ALLIANCE_MESSAGE_SEND);// 刷新联盟战邮件
		int top=data.top();
		data.writeShort(messages.length);
		int length=0;
		Message message=null;
		for(int j=0;j<messages.length;j++)
		{
			if(messages[j]==null) continue;
			message=(Message)messages[j];
			message.showBytesWrite(data,message.getRecive_state(),
				message.getContent(),null);
			length++;
		}
		if(length>0)
		{
			int nowtop=data.top();
			data.setTop(top);
			data.writeShort(length);
			data.setTop(nowtop);
		}
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) continue;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) continue;
			c.send(data);
		}
	}
	
	/** 推送饭点(能量)活动信息 */
	public static void sendMealTimeInfo(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MEALTIME_ENERGY);// 67
		MealTimeManager.getInstance().showBytesWrite(player,data);
		c.send(data);
	}
	
	/** 登陆有礼活动 每日24点向在线玩家推送信息 */
	public static void sendLoginReward(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(ACT_PORT);//2009
		data.writeByte(LOGIN_REWARD);// 68
		data.writeBoolean(true);
		c.send(data);
	}
	/** 删除邮件  num现有数量*/
	public static void sendRMmsg(IntList list,Player player,int[] num)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(EMAIL_PORT);
		data.writeByte(RM_MSG);
		data.writeShort(list.size());
		for(int i=0;i<list.size();i++)
		{
			data.writeInt(list.get(i));
		}
		for(int i=0;i<num.length;i++)
		{
			data.writeShort(num[i]);
		}
		c.send(data);
	}
	
	
	/**新玩家进入联盟推送下联盟战信息**/
	public static void sendAllianceBattleInfo(Player player,AllianceBattleManager manager)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(FLUSH_BATTLE_FIGHT);
		manager.showByteWriteAllianceInfo(data,player);
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		c.send(data);
	}
	
	/** 推送成长计划信息 **/
	public static void sendGrowthPlan(CreatObjectFactory factory,
		GrowthPlanManager manager)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(GROWTH_PLAN);
		manager.showBytesWrite(data);
		factory.getDsmanager().getSessionMap().send(data);
	}
	
	/** 刷新当前招募战友上下线的状态 **/
	public static void sendComradeFriendState(Player player,
		Player outLinePlayer,boolean online,CreatObjectFactory factory,Connect c)
	{
		if(c==null || !c.isActive())
			return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(COMRADE_PORT);
		data.writeByte(RESET_COMRADE_FREIEND);
		data.writeShort(1);
		data.writeInt(outLinePlayer.getId());
		data.writeShort(outLinePlayer.getSid());
		data.writeUTF(outLinePlayer.getName());
		data.writeByte(outLinePlayer.getLevel());
		// 军衔
		data.writeByte(outLinePlayer.getPlayerType());
		// 战力
		data.writeInt(outLinePlayer.getFightScore());
//		int in=outLinePlayer.getUpdateTime();
		int out=outLinePlayer.getExitTime();
		// 离线时间
//		System.out.println(in>out);
//		System.out.println(outLinePlayer.getName());
		data.writeInt(online?0:(TimeKit.getSecondTime()-out)+1);
		String aname="";
		String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(aid!=null)
		{
			Alliance alliance=factory.getAlliance(TextKit.parseInt(aid),
				false);
			if(alliance!=null) aname=alliance.getName();
		}
		// 联盟名称
		data.writeUTF(aname);
		c.send(data);
	}
	/**添加新的招募人员刷新前台战友列表和任务列表***/
	public static void sendComardeInfo(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(COMRADE_PORT);
		data.writeByte(RESET_PLAYER_COMRADE_INFO);
		ComradeHandler.getInstance().getComrades(data,player,
			TimeKit.getSecondTime());
		ComradeHandler.getInstance()
			.showBytesWriteCombradeTasks(player,data);
		c.send(data);
	}
	
	/**刷新前台军官商店**/
	public static  void sendRefreshOfficerShop(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(OFFICER_PORT);
		data.writeByte(OFFICER_SHOP);
		player.getOfficers().showBytesWriteShopInfo(data);
		c.send(data);
	}
	
	/**通商航运**/
	public static void sendAwardShippingActivity(SessionMap map,
		AwardShippingActivity activity,CreatObjectFactory factory)
	{
		Session[] sessions=map.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=PlayerKit.getInstance().getPlayer(sessions[i]);
			ByteBuffer data=new ByteBuffer();
			data.writeShort(ACT_PORT);
			data.writeByte(WAR_MANIC_ACT);
			data.writeByte(1);
			activity.showByteWriteNew(data,player,factory);
			c.send(data);
		}
	}
	/**全民抢节**/
	public static void sendRobActivity(SessionMap map,
		RobFestivalActivity activity,CreatObjectFactory factory)
	{
		Session[] sessions=map.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=PlayerKit.getInstance().getPlayer(sessions[i]);
			ByteBuffer data=new ByteBuffer();
			data.writeShort(ACT_PORT);
			data.writeByte(WAR_MANIC_ACT);
			data.writeByte(RobFestivalActivity.ROB_ATYPE);
			activity.showByteWriteNew(data,player,factory);
			c.send(data);
		}
	}
	/**幸运探险**/
	public static void sendLuckActivity(SessionMap map,
		LuckyExploredActivity activity,CreatObjectFactory factory)
	{
		Session[] sessions=map.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=PlayerKit.getInstance().getPlayer(sessions[i]);
			ByteBuffer data=new ByteBuffer();
			data.writeShort(ACT_PORT);
			data.writeByte(WAR_MANIC_ACT);
			data.writeByte(1);
			activity.showByteWriteNew(data,player,factory);
			c.send(data);
		}
	}
	
	/**和平旗活动**/
	public static void sendPeaceActivity(SessionMap map,
		PeaceActivity activity,CreatObjectFactory factory)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(ACT_PORT);
		data.writeByte(WAR_MANIC_ACT);
		data.writeByte(1);
		activity.showByteWriteNew(data,null,factory);
		map.send(data);
	}
	
	/**刷新科技研究列表**/
	public static void sendResetScience(Player player,PlayerBuild checkBuild)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		if(checkBuild==null) return;
		/** 检测下列表中的时间 **/
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_SCIENCE);
		data.writeByte(checkBuild.getIndex());
		checkBuild.getProduce().showBytesWrite(data,TimeKit.getSecondTime());
		c.send(data);
	}
	
	/** 刷新热销大礼包 */
	public static void sendSellingActivty(SessionMap smap,Activity activity)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(ACT_PORT);
		data.writeByte(WAR_MANIC_ACT);
		data.writeByte(1);
		ActivityContainer.getInstance().sendFlushActivity(activity,data);
		smap.send(data);
	}
	
	/** 刷新拥有军官 */
	public static void sendOwnedOfficer(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(OFFICER_PORT);
		data.writeByte(OWNED_OFFICER);
		player.getOfficers().showBytesWriteOwnedOfficers(data);
		c.send(data);
	}
	
	/**
	 * 刷新激活头像
	 */
	public static void sendEnabledHead(Player player, int headSid) {
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data = new ByteBuffer();
		data.writeShort(FROE_PORT);//2000
		data.writeByte(FLUSH_HEAD);//77
		data.writeInt(headSid);
		c.send(data);
	}
	
	/** 刷新跨服积分赛玩家信息 */
	public static void sendCrossLeaguePlayerInfo(CreatObjectFactory factory,
		Player player,ClientLeagueManager clm,int time)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(CROSS_LEAGUE_PORT);// 2013
		data.writeByte(CROSS_LEAGUE_PLAYER_INFO);// 1
		clm.showBytesWriteBase(factory,player,data,time);
		c.send(data);
	}
	
	/** 刷新跨服积分赛商店信息 */
	public static void sendCrossLeagueShopInfo(Player player,
		ClientLeagueManager clm,int time)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(CROSS_LEAGUE_PORT);// 2013
		data.writeByte(CROSS_LEAGUE_SHOP);// 2
		clm.showBytesWriteShop(data,player,time);
		c.send(data);
	}
	
	/** 刷新跨服积分赛挑战列表 */
	public static void sendCrossLeagueChallengeList(Player player,
		ClientLeagueManager clm)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(CROSS_LEAGUE_PORT);// 2013
		data.writeByte(CROSS_LEAGUE_CHALLENGE_LIST);// 3
		clm.showBytesWriteChallengeList(data,player);
		c.send(data);
	}
	
	/** 刷新跨服积分赛全信息 */
	public static void sendCrossLeagueWholeInfo(CreatObjectFactory factory,
		Player player,ClientLeagueManager clm,int time)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(CROSS_LEAGUE_PORT);// 2013
		data.writeByte(CROSS_LEAGUE_WHOLE_INFO);// 4
		clm.showBytesWriteImmed(factory,player,data,time);
		c.send(data);
	}
	
	/**全联盟进行刷新**/
	public static void sendAllianceFlag(Alliance alliance,CreatObjectFactory factory)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(FLUSH_FLAG);
		alliance.getFlag().showBytesWriteAllianceFlag(data);
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session session=(Session)player.getSource();
			if(session==null) continue;
			Connect c=session.getConnect();
			if(c==null||!c.isActive()) continue;
			c.send(data);
		}
		
	}
	
	/** 刷新在线的好友信息 */
	public static void sendOnLinePlayerFrindInfo(CreatObjectFactory factory)
	{
		SessionMap map = factory.getDsmanager().getSessionMap();
		ByteBuffer data=new ByteBuffer();
		Session[] sessions=map.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			data.clear();
			data.writeShort(FROE_PORT);// 2000
			data.writeByte(FLUSH_FRIENDINFO);// 80
			player.getFriendInfo().goodFriendWrite(data,factory);
			c.send(data);
		}
	}
	
	
	public static void sendPlayerFriendInfo(Player friend,CreatObjectFactory factory){
		Session session=(Session)friend.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);// 2000
		data.writeByte(FLUSH_FRIENDINFO);// 80
		friend.getFriendInfo().goodFriendWrite(data,factory);
		c.send(data);
	}
	
	
	public static void sendPlayerFriendEvents(Player friend,CreatObjectFactory factory){
		Session session=(Session)friend.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);// 2000
		data.writeByte(FLUSH_FRIEND_EVNET);// 79
		friend.getFriendInfo().eventWrite(data,factory);
		c.send(data);
	}
	
}

