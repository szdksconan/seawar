package foxu.sea.kit;

import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.MessageGameDBAccess;
import foxu.fight.FightScene;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Service;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.NianActivity;
import foxu.sea.activity.WarManicActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.award.Award;
import foxu.sea.comparator.FightEventComparator;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.AllianceSkill;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;
import foxu.sea.port.FightPort;
import foxu.sea.shipdata.ShipCheckData;
import foxu.sea.task.TaskEventExecute;
import foxu.sea.worldboss.BossHurt;
import foxu.sea.worldboss.NianBoss;
import foxu.sea.worldboss.WorldBoss;

/** 战斗相关 */
public class FightKit
{

	/** 联盟服务对应的个人服务 */
	public static final int BOSS_SERVICE_SID[]={201,1,202,2,203,3,204,4,205,
		5};

	/** 海水的sid */
	public static final int WATER_ISLAND_SID=11501;

	/** 一个星期的秒数 */
	public static final int MONTH_SECOND=86400*7;

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(MessageGameDBAccess.class);
	
	private static final Object lock=new Object();

	/** 是否可以战斗事件加速 */
	public static String checkUpFightEvent(Player player,int fightEventId,
		CreatObjectFactory objectFactory)
	{
		FightEvent event=(FightEvent)objectFactory.getEventCache().loadOnly(
			fightEventId+"");
		if(event==null) return "event is null";
		// 是否属于自己
		if(event.getPlayerId()!=player.getId()) return "event is not your";
		// 驻守状态
		if(event.getEventState()==FightEvent.HOLD_ON)
			return "event is hold";
		// 如果已经删除状态或者完成
		if(event.getDelete()==FightEvent.DELETE_TYPE)
			return "event is delete";
		// 计算宝石是否足够
		int needGems=SeaBackKit.getGemsForTime((event.getCreatAt()+event
			.getNeedTime())
			-TimeKit.getSecondTime());
		if(needGems==0) return "event is finished";
		if(!Resources.checkGems(needGems,player.getResources()))
			return "not enough gems";
		// // 攻打boss的不能加速
		// NpcIsland island=objectFactory.getIslandByIndexOnly(event
		// .getAttackIslandIndex()
		// +"");
		// if(island.getIslandType()==NpcIsland.WORLD_BOSS)
		// return "worldboss can not use";
		return null;
	}

	/** 事件加速 */
	public static int fightEventUp(Player player,int fightEventId,
		CreatObjectFactory objectFactory)
	{
		FightEvent event=(FightEvent)objectFactory.getEventCache().loadOnly(
			fightEventId+"");
		int time=TimeKit.getSecondTime();
		// 计算宝石是否足够
		int needGems=SeaBackKit.getGemsForTime((event.getCreatAt()+event
			.getNeedTime())
			-time);
		// 扣除宝石
		Resources.reduceGems(needGems,player.getResources(),player);
		// 宝石消费记录
		objectFactory.createGemTrack(GemsTrack.FIGHT_EVENT_UP,
			player.getId(),needGems,event.getId(),
			Resources.getGems(player.getResources()));
		// 改变事件属性(时间)之前锁定事件
		// 如果事件needTime刚好设置为0并且被攻打玩家刚好进行当前事件监测
		// 先于主动方进入checkFightEvent的同步锁(双方都通过了事件attack状态监测)
		// 此情况下会造成同一事件两次攻击的bug
		synchronized(event)
		{
			event.setCreatAt(time);
			// 事件
			event.setNeedTimeDB(0);
			FightKit.checkFightEvent(event,player,objectFactory);
		}
		return needGems;
	}

	/** 推算和某个玩家岛屿相关的战斗事件 */
	public static Object[] pushFightEvent(int islandIndex,
		CreatObjectFactory objectFactory)
	{
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandIndex);
		if(fightEventList==null) return null;
		// 进行时间排序
		Object events[]=fightEventList.toArray();
		SetKit.sort(events,FightEventComparator.getInstance());
		return events;
	}

	/** 战斗事件的处理 */
	public static void checkFightEvent(Object events[],
		CreatObjectFactory objectFactory,Player player)
	{
		// 时间排序后的事件处理
		for(int i=0;i<events.length;i++)
		{
			FightEvent dataA=(FightEvent)events[i];
			synchronized(dataA)
			{
				if(dataA.getEventState()==FightEvent.HOLD_ON) continue;
				checkFightEvent(dataA,player,objectFactory);
				// 加入改变列表 等待储存
				objectFactory.getEventCache().load(dataA.getId()+"");
			}
		}
	}

	/** 战斗事件的处理(适用于搬迁岛屿时检测事件) */
	public static void checkFightEvent(Object events[],
		CreatObjectFactory objectFactory)
	{
		int checkTime=TimeKit.getSecondTime();
		// 时间排序后的事件处理
		for(int i=0;i<events.length;i++)
		{
			FightEvent dataA=(FightEvent)events[i];
			Player player=objectFactory.getPlayerById(dataA.getPlayerId());
			synchronized(dataA)
			{
				// 有驻守事件立即返航
				if(dataA.getEventState()==FightEvent.HOLD_ON) 
				{
					dataA.setCreatAt(checkTime);
					dataA.setEventState(FightEvent.RETRUN_BACK);
					// 找到当前玩家的岛屿
					NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
						player.getId());
					// 计算时间
					int needTime=needTime(island.getIndex(),dataA.getAttackIslandIndex());
					dataA.setNeedTime(needTime,player,checkTime);
				}
				else
					checkFightEvent(dataA,player,objectFactory);
				// 加入改变列表 等待储存
				objectFactory.getEventCache().load(dataA.getId()+"");
			}
		}
	}
	
	/** 世界boss战斗 */
	public static void attackBossNpc(FightEvent event,Player player,
		CreatObjectFactory objectFactory,NpcIsland beIsland)
	{
		WorldBoss boss=objectFactory.getWorldBossBySid(beIsland.getSid());
		player.removeBossFid(event.getId());
		if(boss==null)
		{
			player.setAttribute(PublicConst.ATTACK_BOSS_TIME,
				TimeKit.getSecondTime()+"");
			return;
		}
		if(!boss.getFleetGroup().existShip())
		{
			// 移除boss岛屿
			beIsland.updateSid(WATER_ISLAND_SID);
			// 刷新前台
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			// boss被击杀
			boss.bekilled();
			// 刷新新的等级
			objectFactory.getWorldBossCache().addBossOnKill(boss);
			player.setAttribute(PublicConst.ATTACK_BOSS_TIME,
				TimeKit.getSecondTime()+"");
			return;
		}
		int checkTime=event.getCreatAt()+event.getNeedTime();
		int beAttackLevel=0;
		// 重新计算主动方技能
		SeaBackKit.resetPlayerSkill(player,objectFactory);
		// 打boss
		Object[] object=boss.fight(event.getFleetGroup());
		// beAttackName=boss.getName();
		beAttackLevel=boss.getBossLevel();
		FightScene scene=(FightScene)object[0];
		FightShowEventRecord r=(FightShowEventRecord)object[1];
		// 战斗演播数据
		ByteBuffer data=new ByteBuffer();
		SeaBackKit.conFightRecord(data,r.getRecord(),player.getName(),
			player.getLevel(),beIsland.getName(),beAttackLevel,
			PublicConst.FIGHT_TYPE_13,player,null,event.getFleetGroup(),
			boss.getFleetGroup(),false,null,null);
		event.setCreatAt(checkTime);
		event.setEventState(FightEvent.RETRUN_BACK);
		// 找到当前玩家的岛屿
		NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
			player.getId());
		// 计算时间
		int needTime=needTime(island.getIndex(),beIsland.getIndex());
		event.setNeedTime(needTime,player,checkTime);
		// 计算击毁船只数数量
		int attackNum=boss.lostNum();
		if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadOnly(player.getAttributes(PublicConst.ALLIANCE_ID)+"");
			if(attackNum>0&&alliance!=null)
			{
				boss.addLostNum(alliance.getId(),attackNum);
			}
		}
		// 战斗胜利
		if(scene.getSuccessTeam()==0)
		{
			String message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"boss_has_been_last_kill");
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
			message=TextKit.replace(message,"%",boss.getBossLevel()+"");
			SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),message);
			// 系统公告
			message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"boss_has_been_killed");
			message=TextKit.replace(message,"%",boss.getBossLevel()+"");
			Object objectAlliances[]=boss.getHurtMostAllianceId(objectFactory);
			if(objectAlliances!=null&&objectAlliances.length>0)
			{
				BossHurt bosshurt=(BossHurt)objectAlliances[0];
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().loadOnly(
						bosshurt.getId()+"");
				if(alliance!=null)
				{
					message=TextKit.replace(message,"%",alliance.getName()
						+"");
					message=TextKit.replace(message,"%",bosshurt
						.getHurtNum()
						+"");
					// 系统公告
					SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
						message);
				}
				// 前10联盟的奖励
				Service service=(Service)Service.factory.newSample(boss
					.getServiceSid());
				if(service!=null)
				{
					int maxNum=boss.getFleetMaxNum();
					// 最多前10个联盟发放奖励品 objectAlliances里面为联盟id和伤害船只数量
					for(int i=0;i<objectAlliances.length;i++)
					{
						if(i>=10) break;
						bosshurt=(BossHurt)objectAlliances[i];
						alliance=(Alliance)objectFactory
							.getAllianceMemCache().loadOnly(
								bosshurt.getId()+"");
						if(alliance!=null&&(bosshurt.getHurtNum()<=maxNum))
						{
							//
							for(int j=0;j<BOSS_SERVICE_SID.length;j+=2)
							{
								if(service.getServiceType()==BOSS_SERVICE_SID[j])
								{
									int serviceTime=service.getServiceTime()
										*bosshurt.getHurtNum()/maxNum;
									// 给该联盟所有人发放
									alliance.addPlayerServices(
										objectFactory,BOSS_SERVICE_SID[j+1],
										serviceTime);
									// 联盟消息
									AllianceEvent allianceEvent=new AllianceEvent(
										AllianceEvent.ALLIANCE_WORLD_BOSS_AWARD,
										String.valueOf(boss.getBossLevel()),
										String.valueOf(service
											.getServiceType()),String
											.valueOf(serviceTime),TimeKit
											.getSecondTime());
									alliance.addEvent(allianceEvent);
									break;
								}
							}
						}
					}
				}
			}
			// 击杀奖励
			Award killAward=(Award)Award.factory.newSample(boss
				.getKillAwardSid());
			// 发送邮件
			int sourceIndex=objectFactory.getIslandCache()
				.getPlayerIsLandId(player.getId());
			int exp=addHurtExp(player,boss.getFleetGroup(),TimeKit
				.getSecondTime());
			// 根据经验值活动重新计算经验值
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			if(killAward!=null) killAward.setExperienceAward(exp);
			MessageKit.attackBossNpcIsLand(player,beIsland,objectFactory,
				data,event,true,sourceIndex,killAward,boss.getFleetGroup());
			// 移除boss岛屿
			beIsland.updateSid(WATER_ISLAND_SID);
			// 刷新前台
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			// boss被击杀
			boss.bekilled();
			// 刷新新的等级
			objectFactory.getWorldBossCache().addBossOnKill(
				boss);
			//成就数据采集 
			AchieveCollect.killBoss(player);
		}
		// 战斗失败
		else
		{
			// 发送邮件
			int sourceIndex=objectFactory.getIslandCache()
				.getPlayerIsLandId(player.getId());
			int exp=addHurtExp(player,boss.getFleetGroup(),TimeKit
				.getSecondTime());
			// 根据经验值活动重新计算经验值
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			Award award=new Award();
			award.setExperienceAward(exp);
			MessageKit.attackBossNpcIsLand(player,beIsland,objectFactory,
				data,event,false,sourceIndex,award,boss.getFleetGroup());
		}
		boss.resetLostNum();
		// boss战不损失兵力
		event.getFleetGroup().resetBossShips();
		checkFightEvent(event,player,objectFactory);
		//成就数据采集 
		AchieveCollect.attackBoss(player);
		// 战争狂人积分
		WarManicActivity activity=(WarManicActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
		if(activity!=null&&activity.isActive(TimeKit.getSecondTime()))
		{
			activity.addPScore(WarManicActivity.BOSS,attackNum,player);
		}
		// 刷新事件
		JBackKit.sendFightEvent(player,event,objectFactory);
		// 船只日志 主动方
		ShipCheckData shipdata=objectFactory.addShipTrack(event.getId(),
			ShipCheckData.WORLDBOSS_FIGHT,player,new IntList(),null,true);
		shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland.getIndex())
			+","+beIsland.getName()+","+beIsland.getIslandLevel()+","
			+scene.getSuccessTeam());
	}
	
	/** 年兽boss战斗 */
	public static void attackNianNpc(FightEvent event,Player player,
		CreatObjectFactory objectFactory,NpcIsland beIsland)
	{
		NianActivity acti=(NianActivity)ActivityContainer.getInstance()
			.getActivity(ActivityContainer.NIAN_SID,0);
		if(acti==null||acti.getBoss()==null)
		{
			// 移除boss岛屿
			beIsland.updateSid(WATER_ISLAND_SID);
			// 刷新前台
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			player.setAttribute(PublicConst.ATTACK_NIAN_TIME,
				TimeKit.getSecondTime()+"");
			return;
		}
		NianBoss boss=acti.getBoss();
		if(!boss.getFleetGroup().existShip())
		{
			// 移除boss岛屿
			beIsland.updateSid(WATER_ISLAND_SID);
			// 刷新前台
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			// boss被击杀
			boss.bekilled();
			player.setAttribute(PublicConst.ATTACK_NIAN_TIME,
				TimeKit.getSecondTime()+"");
			return;
		}
		int checkTime=event.getCreatAt()+event.getNeedTime();
		// 重新计算主动方技能
		SeaBackKit.resetPlayerSkill(player,objectFactory);
		// 打boss
		Object[] object=boss.fight(event.getFleetGroup());
		int beAttackLevel=boss.getBossLevel();
		FightScene scene=(FightScene)object[0];
		FightShowEventRecord r=(FightShowEventRecord)object[1];
		// 战斗演播数据
		ByteBuffer data=new ByteBuffer();
		SeaBackKit.conFightRecord(data,r.getRecord(),player.getName(),
			player.getLevel(),beIsland.getName(),beAttackLevel,
			PublicConst.FIGHT_TYPE_17,player,null,event.getFleetGroup(),
			boss.getFleetGroup(),false,null,null);
		event.setCreatAt(checkTime);
		event.setEventState(FightEvent.RETRUN_BACK);
		// 找到当前玩家的岛屿
		NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
			player.getId());
		// 计算时间
		int needTime=needTime(island.getIndex(),beIsland.getIndex());
		event.setNeedTime(needTime,player,checkTime);
		
		// 计算击毁船只数数量
		int lostNum=boss.lostNum();
		
		//增加被攻击次数
		boss.incrBeAttack();
		
		if(lostNum>0)
		{
			boss.addLostNum(player.getId(),lostNum,true);
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
					.loadOnly(player.getAttributes(PublicConst.ALLIANCE_ID)+"");
				if(alliance!=null)
				{
					boss.addLostNum(alliance.getId(),lostNum,false);
				}
			}
		}
		boolean win=false;
		// 战斗胜利
		if(scene.getSuccessTeam()==0)
		{
			String message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"nian_has_been_last_kill");
			message=TextKit.replace(message,"%",boss.getBossLevel()+"");
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
//			message=TextKit.replace(message,"%",player.getName());
//			message=TextKit.replace(message,"%",player.getName());
			
		
			// 系统公告
			SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),message);
			
			message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"nian_has_been_killed");
			message=TextKit.replace(message,"%",boss.getBossLevel()+"");
			Object objectAlliances[]=boss.getHurtRank(objectFactory,
				boss.getHurtList_a());
			int repCount=0;
			for(int i=0;i<boss.getAllRange();i++)
			{
				if(objectAlliances==null) break;
				if(objectAlliances.length<=i) break;
				BossHurt bosshurt=(BossHurt)objectAlliances[i];
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().loadOnly(bosshurt.getId()+"");
				String aname=alliance==null?"??":alliance.getName();
				message=TextKit.replace(message,"%",aname);
				repCount++;
			}
			if(repCount>0)
			{
				message=message.substring(0,message.length()-(boss.getAllRange()-repCount)*10);
				// 系统公告
				SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
					message);
			}
			
			// 击杀奖励
			boss.sendKillAward(player,objectFactory);
			//联盟排名奖励
			boss.sendAllRankAward(objectFactory);
			//个人排名奖励	
			boss.sendPlayerRankAward(objectFactory);
			
			win=true;
		}
		//攻击奖励
		int sourceIndex=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		int exp=addHurtExp(player,boss.getFleetGroup(),
			TimeKit.getSecondTime());
		// 根据经验值活动重新计算经验值
		exp=ActivityContainer.getInstance().resetActivityExp(exp);
		Award award=boss.getAttack_award();
		award.setExperienceAward(exp);
		//发邮件
		MessageKit.attackNianNpcIsLand(player,beIsland,objectFactory,
			data,event,win,sourceIndex,award,boss.getFleetGroup());
		
		boss.resetLostNum();
		// boss战不损失兵力
		event.getFleetGroup().resetBossShips();
		checkFightEvent(event,player,objectFactory);
		// 刷新事件
		JBackKit.sendFightEvent(player,event,objectFactory);
		if(win)
		{
			// 移除boss岛屿
			beIsland.updateSid(WATER_ISLAND_SID);
			// 刷新前台
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			// boss被击杀
			boss.bekilled();
			// 结束活动
			acti.setEndTime(TimeKit.getSecondTime());
		}
		// 船只日志 主动方
		ShipCheckData shipdata=objectFactory.addShipTrack(event.getId(),
			ShipCheckData.NIAN_FIGHT,player,new IntList(),null,true);
		shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland.getIndex())
			+","+beIsland.getName()+","+beIsland.getIslandLevel()+","
			+scene.getSuccessTeam());
	}
	
	public static void checkFightEvent(FightEvent event,Player player,
		CreatObjectFactory objectFactory)
	{
		// 当前时间
		int nowTime=TimeKit.getSecondTime();
		checkFightEvent(event,player,objectFactory,nowTime);
	}
	/** 战斗事件结算 */
	public static void checkFightEvent(FightEvent event,Player player,
		CreatObjectFactory objectFactory,int nowTime)
	{
		// 事件状态
		int eventState=event.getEventState();
		// 当前事件结算时间
		int finishTime=event.getCreatAt()+event.getNeedTime();
		if(finishTime>nowTime) return;
		// 攻击中
		if(eventState==FightEvent.ATTACK)
		{
			synchronized(lock)
			{
				NpcIsland beIsland=objectFactory.getIslandCache().load(
					event.getAttackIslandIndex()+"");
				// 新手引导部分特殊操作
				if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
					beIsland=(NpcIsland)NpcIsland.factory
						.newSample(FightPort.NEW_PLAYER_ATT_ISLAND);
				// 计算战斗
				// 自己为主动方
				if(event.getPlayerId()==player.getId())
				{
					if(beIsland==null)
					{
						log.warn("fight_error:island is null:id="
							+event.getAttackIslandIndex());
						// 事件的发出者
						Player sourcePlayer=objectFactory.getPlayerCache()
							.load(event.getPlayerId()+"");
						event.setDelete(FightEvent.DELETE_TYPE);
						event.getFleetGroup().cancel(sourcePlayer,true);
						// 刷新事件
						JBackKit.sendFightEvent(player,event,objectFactory);
						return;
					}
					// 先检查是否是攻击boss
					if(beIsland.getIslandType()==NpcIsland.WORLD_BOSS)
					{
						try
						{
							attackBossNpc(event,player,objectFactory,
								beIsland);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						TaskEventExecute.getInstance().executeEvent(
							PublicConst.ATTACK_BOSS_TASK_EVENT,null,player,
							null);
					}
					// 检查是否是攻击年兽
					else if(beIsland.getIslandType()==NpcIsland.NIAN_BOSS)
					{
						try
						{
							attackNianNpc(event,player,objectFactory,
								beIsland);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					// 攻击野地 有可能是玩家现在占领的
					else if(beIsland.getPlayerId()==0)
					{
						if(beIsland.checkDismiss())
							objectFactory.getGemManger().removeGemsIsland(
								beIsland.getIndex());			
						//攻击的岛屿为空时
						if(beIsland.getIslandType()==NpcIsland.ISLAND_WARTER)
						{
							int checkTime=event.getCreatAt()+event.getNeedTime();
							event.setCreatAt(checkTime);
							NpcIsland island=objectFactory.getIslandCache()
								.getPlayerIsland(player.getId());
							int needTime=10;
							if(island!=null)
							{
								needTime=needTime(island.getIndex(),beIsland.getIndex());
							}
							event.setNeedTime(needTime,player,checkTime);
							event.setEventState(FightEvent.RETRUN_BACK);
							// 刷新事件
							JBackKit.sendFightEvent(player,event,objectFactory);
							//发邮件
							SeaBackKit.fight_send_every(objectFactory,
								player,PublicConst.FIGHT_TYPE_14,
								player.getName(),beIsland.getIndex(),
								player.getName(),null,true,null,null,event,
								null,null,0,beIsland.getSid(),
								Message.RETURN_BACK,0,"",beIsland.getFleetGroup(),0);
							if(player.isBossFid(event.getId()))
							{
								player.setAttribute(
									PublicConst.ATTACK_BOSS_TIME,
									TimeKit.getSecondTime()+"");
								player.removeBossFid(event.getId());
							}
							
						}
						else
						{
							attackNpcWild(event,player,beIsland,finishTime,
								objectFactory);
						}
						TaskEventExecute.getInstance().executeEvent(
							PublicConst.ATTACK_NPCISLAND_TASK_EVENT,null,player,
							null);
					}
					// 攻击玩家
					else if(beIsland.getPlayerId()!=0)
					{
						attackPlayer(event,beIsland,objectFactory);
						TaskEventExecute.getInstance().executeEvent(
							PublicConst.ATTACK_PLAYER_TASK_EVENT,null,player,
							null);
					}
					// // 攻击NPC城市
					// else
					// if(beIsland.getIslandType()==NpcIsland.ISLAND_NPC)
					// {
					// attackNpcIsland(event,player,beIsland,nowTime,
					// objectFactory);
					// }
				}
				else
				{
					// 自己为被动方 可能现在自己已经搬家了
					if(beIsland.getPlayerId()!=player.getId())
					{
						// 推主动方
						// 事件的发出者
						Player sourcePlayer=objectFactory.getPlayerCache()
							.load(event.getPlayerId()+"");
						checkFightEvent(event,sourcePlayer,objectFactory);
						return;
					}
					// 战斗
					attackPlayer(event,beIsland,objectFactory);
				}
			}
		}
		// 返回
		else if(eventState==FightEvent.RETRUN_BACK)
		{
			// 事件的发出者
			Player sourcePlayer=objectFactory.getPlayerCache().load(
				event.getPlayerId()+"");
			// 查看某种资源是否已达上限
			Resources.filterFullResources(event.getResources(),sourcePlayer);
			// 新手引导特殊操作
			if(sourcePlayer.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
			{
				NpcIsland island=(NpcIsland)NpcIsland.factory
								.getSample(FightPort.NEW_PLAYER_ATT_ISLAND);
				event.setResources(new int[Player.RESOURCES_SIZE]);
				island.forceSetResource(event.getResources(),FightPort.NEW_PLAYER_HOLD_RESOURCE);
				sourcePlayer.setAttribute(PublicConst.NEW_FOLLOW_PLAYER_HOLD,null);
			}
			// 回归资源
			int gems=Resources.addResources(sourcePlayer.getResources(),
				event.getResources(),sourcePlayer);
			if(gems>0)
			{
				objectFactory.createGemTrack(GemsTrack.FIGHT_GEM_ISLAND,
					player.getId(),gems,0,
					Resources.getGems(player.getResources())+gems);
			}
			// 回归舰队
			event.getFleetGroup().cancel(sourcePlayer,true);
			// 删除事件
			event.setDelete(FightEvent.DELETE_TYPE);
			// 刷新事件
			JBackKit.sendFightEvent(sourcePlayer,event,objectFactory);

			// 船只日志
			objectFactory.addShipTrack(event.getId(),ShipCheckData.SHIP_BACK_HOME,
				sourcePlayer,new IntList(),null,false);
		}
		JBackKit.sendMarchLine(objectFactory,event);

	}
	// 攻击NPC野地
	public static void attackNpcWild(FightEvent event,Player player,
		NpcIsland beIsland,int nowTime,CreatObjectFactory objectFactory)
	{
		Object[] object=null;
		/** 被攻击者 */
		Player beAttacker=null;
		FightEvent holdEvent=null;
		String beAttackName="";
		int beAttackLevel=0;
		int checkTime=event.getCreatAt()+event.getNeedTime();
		String attackPlayerName="";
		boolean success=false;
		int maxResouce=0;
		// 是否需要继续推算事件
		boolean goOnPushEvent=false;
		// 重新计算主动方技能
		SeaBackKit.resetPlayerSkill(player,objectFactory);
		int passTime=0;
		// 防守方为占岛玩家或者npc
		FleetGroup defendGroup=null;
		/**buff**/
		IntList beAttackBuff=null;
		if(beIsland.getTempAttackEventId()!=0)
		{
			// 打玩家
			holdEvent=(FightEvent)objectFactory.getEventCache().load(
				beIsland.getTempAttackEventId()+"");
			// 错误判断
			if(holdEvent==null)
			{
				beIsland.setTempAttackEventId(0);
				attackNpcWild(event,player,beIsland,nowTime,objectFactory);
				return;
			}
			defendGroup=holdEvent.getFleetGroup();
			beAttacker=objectFactory.getPlayerCache().load(
				holdEvent.getPlayerId()+"");
			// 被攻击者
			SeaBackKit.resetPlayerSkill(beAttacker,objectFactory);
			beAttackName=beAttacker.getName();
			beAttackLevel=beAttacker.getLevel();

			// 计算驻守方的最大资源
			passTime=nowTime-holdEvent.getCreatAt();
			int carryResource=SeaBackKit.groupCarryResource(holdEvent
				.getFleetGroup());
			int addition=SeaBackKit.groupResourceAddition(player,event.getFleetGroup(),beIsland.getIslandType());
			//避免因为多次整数计算带来明显误差，采集数据与战报数据显示不一致
			int maxPassTime=(int)((float)carryResource*addition
				/PublicConst.AWARD_TOTAL_LENGTH/beIsland.getResource()*60);
			passTime=passTime>maxPassTime?maxPassTime:passTime;
			maxResouce=(int)((float)beIsland.getResource()*addition
				/PublicConst.AWARD_TOTAL_LENGTH/60*passTime);
			
			boolean allianceBool=false;
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadOnly(beAttacker.getAttributes(PublicConst.ALLIANCE_ID));
			if(alliance!=null)
			{
				if(alliance.inAlliance(event.getPlayerId()))
				{
					allianceBool=true;
				}
				// 驻守时，也算上联盟防御技能
//				Object[] array=alliance.getAllianSkills().getArray();
//				for(int i=0;i<array.length;i++)
//				{
//					if(array[i]==null) continue;
//					AllianceSkill skill=(AllianceSkill)array[i];
//					if(!skill.isDefenceSkill()) continue;
//					skill.setChangeValue(beAttacker.getAdjstment());
//				}
			}
			// 自己的队伍 或联盟的队伍 TODO
			if(holdEvent.getPlayerId()==event.getPlayerId()||allianceBool)
			{
				// 发送邮件
				int sourceIndex=objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId());
				int fightType=PublicConst.FIGHT_TYPE_6;
				if(allianceBool) fightType=PublicConst.FIGHT_TYPE_10;
				SeaBackKit.fight_send_every(objectFactory,player,fightType,
					player.getName(),beIsland.getIndex(),player.getName(),
					"",true,null,null,event,null,null,sourceIndex,beIsland
						.getSid(),Message.RETURN_BACK,0,"",defendGroup,0);
				// 直接返航
				event.setCreatAt(checkTime);
				event.setEventState(FightEvent.RETRUN_BACK);
				// 计算时间
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(player.getId());
				int needTime=needTime(island.getIndex(),beIsland.getIndex());
				event.setNeedTime(needTime,player,checkTime);
				// 刷新事件
				JBackKit.sendFightEvent(player,event,objectFactory);
				//如果是同一玩家，则不进行显示事件的删除
				if(holdEvent.getPlayerId()!=event.getPlayerId())
					JBackKit.deleteFightEvent(beAttacker,event);
				// 继续推算 是否已经回家
				checkFightEvent(event,player,objectFactory);
				return;
			}
			holdEvent.flushBuff(beIsland,objectFactory,true);
			beAttackBuff=beIsland.getServices();
			object=holdEvent.fight(event.getFleetGroup(),true);
			//成就数据采集
//			AchieveCollect.attackPlayer(player);//攻打野地驻守玩家不推动此成就
			AchieveCollect.perishShip(event.getFleetGroup(),beAttacker);
			AchieveCollect.perishShip(holdEvent.getFleetGroup(),player);
			//判定敌对  战争狂人积分
			WarManicActivity activity=(WarManicActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
			if(activity!=null&&activity.isActive(TimeKit.getSecondTime()))
			{
				Alliance attack=null;
				String alid=player.getAttributes(PublicConst.ALLIANCE_ID);
				if(alid!=null&&!alid.equals(""))
				{
					attack=objectFactory.getAlliance(TextKit.parseInt(alid),
						false);
				}
				if(!SeaBackKit.isSameAlliance(attack,alliance))
				{
					activity.addPScore(WarManicActivity.HOSTILE,holdEvent
						.getFleetGroup().getDeadPoint(),player);
				}
				if(!SeaBackKit.isSameAlliance(alliance,attack))
				{
					activity.addPScore(WarManicActivity.HOSTILE,event
						.getFleetGroup().getDeadPoint(),beAttacker);
				}
			}
			JBackKit.sendMarchLine(objectFactory,beAttacker,event);
		}
		else
		{
			// 打野地
			defendGroup=beIsland.getFleetGroup();
			object=beIsland.fight(event.getFleetGroup());
			beAttackName=beIsland.getName();
			beAttackLevel=beIsland.getIslandLevel();
			//成就数据采集
			AchieveCollect.attackNpc(beIsland,player);
		}
		FightScene scene=(FightScene)object[0];
		FightShowEventRecord r=(FightShowEventRecord)object[1];
		// 战斗演播数据
		ByteBuffer data=new ByteBuffer();
		SeaBackKit.conFightRecord(data,r.getRecord(),player.getName(),player
			.getLevel(),beAttackName,beAttackLevel,PublicConst.FIGHT_TYPE_1,
			player,beAttacker,event.getFleetGroup(),defendGroup,false,null,beAttackBuff);
		// 战斗胜利
		if(scene.getSuccessTeam()==0)
		{
			success=true;
			// NPC岛屿上有玩家驻守
			if(beIsland.getTempAttackEventId()!=0)
			{
				// 删除岛屿之前的event 敌方必然被歼灭
				holdEvent=(FightEvent)objectFactory.getEventCache().load(
					beIsland.getTempAttackEventId()+"");
				// 掠夺玩家驻守获得的资源
				beIsland.addResource(holdEvent.getResources(),event.getResources());
				beIsland.setResource(player,event.getResources(),passTime/60,event.getFleetGroup(),maxResouce);
				int exp=addHurtExp(beAttacker,holdEvent.getFleetGroup(),
					nowTime);
				// 根据经验值活动重新计算经验值
				exp=ActivityContainer.getInstance().resetActivityExp(exp);
				// 发送邮件
				MessageKit.attackTempPlayer(player,beAttacker,event,
					holdEvent,objectFactory,beIsland,data,true,exp);
				// 发送邮件结束
				holdEvent.setDelete(FightEvent.DELETE_TYPE);
				// 刷新事件
				JBackKit.sendFightEvent(beAttacker,holdEvent,objectFactory);
				// 计算时间
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(beAttacker.getId());
				// 检查自己的事件 移除对方的攻击事件
				if(island!=null&&beIsland!=null)
					objectFactory.getEventCache().removeHoldOnEvent(
						island.getIndex(),beIsland.getIndex(),beAttacker);

				attackPlayerName=beAttacker.getName();

				// 驻守玩家被攻打 日志
				ShipCheckData shipdata=objectFactory.addShipTrack(
					event.getId(),ShipCheckData.BE_FIGHT_YEDI,beAttacker,new IntList(),null,false);
				shipdata.setAttackPlayerName(player.getName());
				shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland
					.getIndex())
					+","
					+beIsland.getName()
					+","
					+beIsland.getIslandLevel()
					+","+success+",->"+player.getName());
			}
			else
			{
				// 发送邮件
				int sourceIndex=objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId());
				MessageKit.attackNpcIsLand(player,beIsland,objectFactory,
					data,event,true,sourceIndex);
				// 重新计算NPC的兵力
				beIsland.createFleetGroup();
			}
			// 继续驻守
			if(event.getType()==FightEvent.ATTACK_HOLD)
			{
				// 加入改变列表(非新手引导状态下)
				if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
					objectFactory.getIslandCache().load(
						event.getAttackIslandIndex()+"");
				// 设置野地的玩家占领
				beIsland.setTempAttackEventId(event.getId());
				event.setCreatAt(checkTime);
				event.setEventState(FightEvent.HOLD_ON);
				// 刷新事件
				JBackKit.sendFightEvent(player,event,objectFactory);
			}
			else
			{
				event.setCreatAt(checkTime);
				// 找到当前玩家的岛屿
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(player.getId());
				int needTime=10;
				if(island!=null)
				{
					// 计算时间
					needTime=needTime(island.getIndex(),beIsland.getIndex());
				}
				// 把别人打跑 自己也走了
				beIsland.setTempAttackEventId(0);
				event.setNeedTime(needTime,player,checkTime);
				event.setEventState(FightEvent.RETRUN_BACK);
				// 刷新事件
				JBackKit.sendFightEvent(player,event,objectFactory);
				// 继续推算 是否已经回家
				// checkFightEvent(event,player,objectFactory);
				goOnPushEvent=true;
			}
			// 攻击事件
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_TASK_EVENT,beIsland,player,true);
		}
		// 战斗失败
		else
		{
			boolean bool=fightGroupDown(event,player,checkTime,objectFactory);
			// NPC岛屿上有玩家驻守
			if(beIsland.getTempAttackEventId()!=0)
			{
				int exp=addHurtExp(player,event.getFleetGroup(),nowTime);
				// 根据经验值活动重新计算经验值
				exp=ActivityContainer.getInstance().resetActivityExp(exp);
				// 发送邮件
				MessageKit.attackTempPlayer(player,beAttacker,event,
					holdEvent,objectFactory,beIsland,data,false,exp);
				// 刷新事件告知驻守方 事件结束
				JBackKit.deleteFightEvent(beAttacker,event);
				// 刷新原事件
				FightEvent tempEvent=(FightEvent)objectFactory
					.getEventCache()
					.load(beIsland.getTempAttackEventId()+"");
				JBackKit.sendFightEvent(beAttacker,tempEvent,objectFactory);
			}
			else
			{
				int sourceIndex=objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId());
				// 攻打野地 发送奖励品 发送邮件
				MessageKit.attackNpcIsLand(player,beIsland,objectFactory,
					data,event,false,sourceIndex);
				// 重新计算NPC的兵力
				beIsland.createFleetGroup();
			}
			// 如果被歼灭
			if(bool)
			{
				// 刷新事件
				JBackKit.sendFightEvent(player,event,objectFactory);
			}
			else
			{
				event.setCreatAt(checkTime);
				event.setEventState(FightEvent.RETRUN_BACK);
				// 找到当前玩家的岛屿
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(player.getId());
				// 计算时间
				int needTime=needTime(island.getIndex(),beIsland.getIndex());
				event.setNeedTime(needTime,player,checkTime);
				// 刷新事件
				JBackKit.sendFightEvent(player,event,objectFactory);
				// 继续推算 是否已经回家
				// checkFightEvent(event,player,objectFactory);
				goOnPushEvent=true;
			}
			// 攻击事件
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_TASK_EVENT,beIsland,player,false);
		}
		// 计算伤兵 主动方
		addHurtShips(player,event.getFleetGroup(),nowTime,objectFactory);
		// 被动方如果是玩家 伤兵计算
		if(beAttacker!=null&&holdEvent!=null)
			addHurtShips(beAttacker,holdEvent.getFleetGroup(),nowTime,objectFactory);
		/** 船只日志 */
		ShipCheckData shipdata=objectFactory.addShipTrack(
			event.getId(),ShipCheckData.FIGHT_NPC_ISLAND,player,new IntList(),null,false);
		shipdata.setAttackPlayerName(attackPlayerName);
		shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland.getIndex())
			+","+beIsland.getName()+","+beIsland.getIslandLevel()+","
			+success+",->"+attackPlayerName);
		if(goOnPushEvent)
		{
			checkFightEvent(event,player,objectFactory);
		}
	}

	/** 计算主动方的经验值 */
	public static int addHurtExp(Player player,FleetGroup after,int time)
	{
		if(player==null) return 0;
		int exp=0;
		// 计算伤兵
		exp=after.hurtTroopsExp(player,time);
		if(exp<0) exp=0;
		return exp;
	}

	/** 计算并添加伤兵 */
	public static void addHurtShips(Player player,FleetGroup after,int time,
		CreatObjectFactory objectFactory)
	{
		
		if(player==null) return;
		// 计算伤兵
		after.hurtTroops(player,time);
		// 计算玩伤兵并添加后 初始舰队数量设置为当前数量
		after.resetLastNum();
		// 刷新伤兵
		JBackKit.resetHurtTroops(player);
		int fs=player.getFightScore();
		SeaBackKit.setPlayerFightScroe(player,objectFactory);
		if(player.getFightScore()!=fs)
		{
			IntList fightList=after.hurtList(FleetGroup.HURT_TROOPS);
			//TODO
			JBackKit.sendFightScore(player,objectFactory,false,FightScoreConst.FIGHT_LOSE);
		}
	}

	/** 攻击玩家 beIsland被攻击的玩家岛屿 event攻击事件 */
	public static void attackPlayer(FightEvent event,NpcIsland beIsland,
		CreatObjectFactory objectFactory)
	{
		// 攻击者
		Player attackPlayer=objectFactory.getPlayerCache().load(
			event.getPlayerId()+"");
		// 被攻击者
		Player beAttackPlayer=objectFactory.getPlayerCache().load(
			beIsland.getPlayerId()+"");
		// 重新计算技能
		SeaBackKit.resetPlayerSkill(attackPlayer,objectFactory);
		SeaBackKit.resetPlayerSkill(beAttackPlayer,objectFactory);
		// 战斗胜利
		int checkTime=event.getCreatAt()+event.getNeedTime();
		int sourceIndex=objectFactory.getIslandCache().getPlayerIsLandId(
			attackPlayer.getId());
		boolean success=false;
		boolean sameAlliance=false;
		if(attackPlayer.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&beAttackPlayer.getAttributes(PublicConst.ALLIANCE_ID)!=null)
		{
			if(attackPlayer.getAttributes(PublicConst.ALLIANCE_ID).equals(
				beAttackPlayer.getAttributes(PublicConst.ALLIANCE_ID)))
			{
				sameAlliance=true;
			}
		}
		// 盟军驻守
		if(event.getType()==FightEvent.ATTACK_HOLD
			&&beIsland.getPlayerId()!=0&&sameAlliance)
		{
			event.setCreatAt(checkTime);
			event.setEventState(FightEvent.HOLD_ON);
			// 刷新事件
			JBackKit.sendFightEvent(attackPlayer,event,objectFactory);
			// 刷新事件
			JBackKit.sendFightEvent(beAttackPlayer,event,objectFactory);
			return;
		}
		// 被攻击者有免战buff
		if(beAttackPlayer.checkService(PublicConst.NOT_FIGHT_BUFF,checkTime)!=null)
		{
			// 发送邮件
			SeaBackKit.fight_send_every(objectFactory,attackPlayer,
				PublicConst.FIGHT_TYPE_7,beAttackPlayer.getName(),beIsland
					.getIndex(),attackPlayer.getName(),beAttackPlayer
					.getName(),true,null,null,event,null,null,sourceIndex,0,
				Message.RETURN_BACK,0,"",beAttackPlayer.getIsland().getMainGroup(),0);
			// 返航
			event.setCreatAt(checkTime);
			event.setEventState(FightEvent.RETRUN_BACK);
			// 计算时间
			NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
				attackPlayer.getId());
			int needTime=needTime(island.getIndex(),beIsland.getIndex());
			event.setNeedTime(needTime,attackPlayer,checkTime);
			// 刷新事件
			JBackKit.sendFightEvent(attackPlayer,event,objectFactory);

			JBackKit.deleteFightEvent(beAttackPlayer,event);
			// 继续推算 是否已经回家
			checkFightEvent(event,attackPlayer,objectFactory);
			return;
		}
		boolean goOnPushEvent=false;
		// 推算被攻击者
		beAttackPlayer.getIsland().pushAll(
			event.getCreatAt()+event.getNeedTime(),objectFactory);
		// 如果有联防的
		FleetGroup allianceDefendGroup=null;
		Player allianceDefendPlayer=null;
		FightEvent allianceEvent=null;
		if(beAttackPlayer.getAttributes(PublicConst.ALLIANCE_DEFND_ATT)!=null
			&&!beAttackPlayer.getAttributes(PublicConst.ALLIANCE_DEFND_ATT)
				.equals(""))
		{
			int eventId=Integer.parseInt(beAttackPlayer
				.getAttributes(PublicConst.ALLIANCE_DEFND_ATT));
			allianceEvent=(FightEvent)objectFactory.getEventCache().load(
				eventId+"");
			if(allianceEvent!=null
				&&allianceEvent.getFleetGroup().existShip())
			{
				// 协防战斗
				allianceDefendGroup=allianceEvent.getFleetGroup();
				allianceDefendPlayer=objectFactory.getPlayerCache().load(
					allianceEvent.getPlayerId()+"");
				// 协防者技能重新计算
				SeaBackKit.resetPlayerSkill(allianceDefendPlayer,
					objectFactory);
			}
		}
		// 被攻击舰队
		FleetGroup beAttackGroup=beAttackPlayer.getIsland().getMainGroup();
		beAttackGroup.getOfficerFleetAttr().initOfficers(beAttackPlayer);
		if(allianceDefendGroup!=null) beAttackGroup=allianceDefendGroup;
		boolean bool=false;
		// 掠夺资源量
		int resource[]=SeaBackKit.attackResourceP(event.getFleetGroup(),
			SeaBackKit.canResourceP(beAttackPlayer));
		//成就数据采集 
		AchieveCollect.attackPlayer(attackPlayer);
		// 防守方没有兵力或者没有参战
		int[] honorScore;
		if(!beAttackGroup.existShip()||!beAttackPlayer.isFight())
		{
			// 战斗胜利增加军官功勋
			addOfficerFeatsInPvP(PublicConst.PLAYER_FIGHT_SUCCESS_FEATS,attackPlayer);
			// 计算荣誉积分
			honorScore=honorScore(attackPlayer,beAttackPlayer,true,objectFactory);
			// 被攻击玩家减少
			Resources.reduceResources(beAttackPlayer.getResources(),
				resource,beAttackPlayer);
			event.setResources(resource);
			int reduceProsperity = beAttackPlayer.reduceProsperity();
			// // 计算掠夺资源
			// long
			// thisPlunder=SeaBackKit.resourceTotal(event.getResources());
			// attackPlayer.addPlunderResource(thisPlunder);
			// 发送邮件
			// 发送push 邮件
			SeaBackKit.fight_send_every(objectFactory,attackPlayer,
				PublicConst.FIGHT_TYPE_2,beAttackPlayer.getName(),beIsland
					.getIndex(),attackPlayer.getName(),beAttackPlayer
					.getName(),true,null,null,event,null,null,sourceIndex,0,
				0,honorScore[0],"",beAttackGroup,PublicConst.PLAYER_FIGHT_SUCCESS_FEATS,reduceProsperity);

			SeaBackKit.fight_send_every(objectFactory,beAttackPlayer,
				PublicConst.FIGHT_TYPE_3,beAttackPlayer.getName(),beIsland
					.getIndex(),attackPlayer.getName(),beAttackPlayer
					.getName(),false,null,null,event,null,null,sourceIndex,
				0,0,honorScore[0],"",beAttackGroup,0,reduceProsperity);
			// 攻击事件
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_TASK_EVENT,beIsland,attackPlayer,true);

			// 记录联盟事件
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(beAttackPlayer.getAttributes(PublicConst.ALLIANCE_ID));
			if(alliance!=null)
			{
				// 查看对方是否有联盟
				AllianceEvent allianEvent=new AllianceEvent(
					AllianceEvent.ALLIANCE_EVENT_BEFIGHT,attackPlayer
						.getName()
						+SeaBackKit.getAllianceByPlayer(attackPlayer,
							objectFactory),beAttackPlayer.getName(),"",
					checkTime);
				alliance.addEvent(allianEvent);
			}
		}
		// 计算战斗
		else
		{
			// 攻击玩家城市 需要把玩家的城防加上 火炮 空军 导弹 6,7,8
			// SeaBackKit.addDefendBuild(beAttackPlayer,beAttackGroup);
			// 防守加联盟技能
			Alliance defend=null;
			if(beAttackPlayer.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!beAttackPlayer.getAttributes(PublicConst.ALLIANCE_ID)
					.equals(""))
			{
				int alliance_id=Integer.parseInt(beAttackPlayer
					.getAttributes(PublicConst.ALLIANCE_ID));
				defend=(Alliance)objectFactory.getAllianceMemCache()
					.loadOnly(alliance_id+"");
				if(defend!=null)
				{
					Object[] array=defend.getAllianSkills().getArray();
					for(int i=0;i<array.length;i++)
					{
						if(array[i]==null) continue;
						AllianceSkill skill=(AllianceSkill)array[i];
						if(!skill.isDefenceSkill()) continue;
						skill.setChangeValue(beAttackPlayer.getAdjstment());
					}
				}
			}
			FightScene scene=FightSceneFactory.factory.create(event
				.getFleetGroup(),beAttackGroup);
			scene.setDefend(true);
			FightShowEventRecord r=FightSceneFactory.factory.fight(scene,
				null);
			// 联盟主动技能在前面的resetPlayerSkills已经添加了，这里再次调用会清除之前添加的被动技能
//			if(defend!=null)
//			{
//				defend.addAllianceSkills(beAttackPlayer);
//			}
			//判定敌对  战争狂人积分
			WarManicActivity activity=(WarManicActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
			if(activity!=null&&activity.isActive(TimeKit.getSecondTime()))
			{
				Alliance attack=null;
				String alid=attackPlayer
					.getAttributes(PublicConst.ALLIANCE_ID);
				if(alid!=null&&!alid.equals(""))
				{
					attack=objectFactory.getAlliance(TextKit.parseInt(alid),
						false);
				}
				if(!SeaBackKit.isSameAlliance(attack,defend))
				{
					activity.addPScore(WarManicActivity.HOSTILE,
						beAttackGroup.getDeadPoint(),attackPlayer);
				}
				if(!SeaBackKit.isSameAlliance(defend,attack))
				{
					if(allianceDefendPlayer!=null)
						activity.addPScore(WarManicActivity.HOSTILE,event
							.getFleetGroup().getDeadPoint(),allianceDefendPlayer);
					else
						activity.addPScore(WarManicActivity.HOSTILE,event
							.getFleetGroup().getDeadPoint(),beAttackPlayer);
				}
			}
			// 战斗演播数据
			ByteBuffer data=new ByteBuffer();
			SeaBackKit.conFightRecord(data,r.getRecord(),attackPlayer
				.getName(),attackPlayer.getLevel(),
				allianceDefendPlayer==null?beAttackPlayer.getName()
					:allianceDefendPlayer.getName(),
				allianceDefendPlayer==null?beAttackPlayer.getLevel()
					:allianceDefendPlayer.getLevel(),
				PublicConst.FIGHT_TYPE_2,attackPlayer,
				allianceDefendPlayer==null?beAttackPlayer
					:allianceDefendPlayer,event.getFleetGroup(),
				beAttackGroup,false,null,null);
			// 攻击方胜利
			if(scene.getSuccessTeam()==0)
			{
				success=true;
				// 战斗胜利增加军官功勋
				addOfficerFeatsInPvP(PublicConst.PLAYER_FIGHT_SUCCESS_FEATS,attackPlayer);
				// 计算荣誉积分
				honorScore=honorScore(attackPlayer,beAttackPlayer,true,objectFactory);
//				// 协防的
//				if(allianceDefendPlayer!=null)
//				{
//					honorScore=honorScore(attackPlayer,allianceDefendPlayer,
//						true);
//				}
				// 掠夺资源量(发生战斗后,需要考虑攻击方船只损失来计算最终的资源掠夺量)
				resource=SeaBackKit.attackResourceP(event.getFleetGroup(),
					SeaBackKit.canResourceP(beAttackPlayer));
				// 被攻击玩家减少
				Resources.reduceResources(beAttackPlayer.getResources(),
					resource,beAttackPlayer);
				event.setResources(resource);
				// 被攻击玩家减少繁荣度 
				int reduceProsperity = beAttackPlayer.reduceProsperity();
				// // 计算掠夺资源
				// long thisPlunder=SeaBackKit.resourceTotal(event
				// .getResources());
				// attackPlayer.addPlunderResource(thisPlunder);
				// 经验
				// 记录联盟事件
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						beAttackPlayer
							.getAttributes(PublicConst.ALLIANCE_ID));
				if(alliance!=null)
				{
					// 查看对方是否有联盟
					AllianceEvent allianEvent=new AllianceEvent(
						AllianceEvent.ALLIANCE_EVENT_BEFIGHT,attackPlayer
							.getName()
							+SeaBackKit.getAllianceByPlayer(attackPlayer,
								objectFactory),beAttackPlayer.getName(),"",
						checkTime);
					alliance.addEvent(allianEvent);
				}
				int exp=addHurtExp(beAttackPlayer,beAttackPlayer.getIsland()
					.getMainGroup(),checkTime);
				// 协防的
				if(allianceDefendPlayer!=null)
				{
					exp=addHurtExp(allianceDefendPlayer,beAttackGroup,
						checkTime);
					// 删除协防事件
					allianceEvent.setDelete(FightEvent.DELETE_TYPE);
					JBackKit.deleteFightEvent(beAttackPlayer,allianceEvent);
					JBackKit.deleteFightEvent(allianceDefendPlayer,
						allianceEvent);
				}
				// 根据经验值活动重新计算经验值
				exp=ActivityContainer.getInstance().resetActivityExp(exp);
				// 发送邮件开始 攻击方
				MessageKit.attackPlayer(attackPlayer,beAttackPlayer,event,
					objectFactory,beIsland,data,true,exp,honorScore[0],
					allianceDefendPlayer,beAttackGroup,PublicConst.PLAYER_FIGHT_SUCCESS_FEATS,reduceProsperity);
				// 攻击事件
				TaskEventExecute.getInstance()
					.executeEvent(PublicConst.ATTACK_TASK_EVENT,beIsland,
						attackPlayer,true);
			}
			// 攻击方失败
			else
			{
				// 战斗失败增加军官功勋
				addOfficerFeatsInPvP(PublicConst.PLAYER_FIGHT_FAIL_FEATS,attackPlayer);
				honorScore=honorScore(attackPlayer,beAttackPlayer,false,objectFactory);
				int exp=addHurtExp(attackPlayer,event.getFleetGroup(),
					checkTime);
				// 协防的
				if(allianceDefendPlayer!=null)
				{
					// 攻击失败时，经验值始终按攻击方损失的船只计算。
					// exp=addHurtExp(allianceDefendPlayer,beAttackGroup,
					// checkTime);
					// 刷新事件
					JBackKit.sendFightEvent(beAttackPlayer,event,
						objectFactory);
					// 刷新事件
					JBackKit.sendFightEvent(allianceDefendPlayer,event,
						objectFactory);
				}
				// 根据经验值活动重新计算经验值
				exp=ActivityContainer.getInstance().resetActivityExp(exp);
				// 发送邮件开始 攻击方
				MessageKit.attackPlayer(attackPlayer,beAttackPlayer,event,
					objectFactory,beIsland,data,false,exp,honorScore[0],
					allianceDefendPlayer,beAttackGroup,PublicConst.PLAYER_FIGHT_FAIL_FEATS,0);
				// 失败
				bool=fightGroupDown(event,attackPlayer,checkTime,
					objectFactory);
				// 攻击事件
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.ATTACK_TASK_EVENT,beIsland,attackPlayer,
					false);
			}
			// 计算伤兵
			//成就数据采集
			if(allianceDefendPlayer!=null)
			{
				AchieveCollect.perishShip(event.getFleetGroup(),allianceDefendPlayer);
			}
			else
			{
				AchieveCollect.perishShip(event.getFleetGroup(),beAttackPlayer);
			}
			
			// 主动方
			addHurtShips(attackPlayer,event.getFleetGroup(),checkTime,objectFactory);
			// 防守方
			if(allianceDefendPlayer!=null)
			{
				//成就数据采集
				AchieveCollect.perishShip(beAttackGroup,attackPlayer);
				
				addHurtShips(allianceDefendPlayer,beAttackGroup,checkTime,objectFactory);
			}
			else
			{
				//成就数据采集
				AchieveCollect.perishShip(beAttackPlayer.getIsland()
					.getMainGroup(),attackPlayer);
				
				addHurtShips(beAttackPlayer,beAttackPlayer.getIsland()
					.getMainGroup(),checkTime,objectFactory);
				// 防守方自动补充主力舰队
				beAttackPlayer.autoAddMainGroup();
			}
		}
		// 如果被歼灭
		if(bool)
		{
			// 刷新事件
			JBackKit.sendFightEvent(attackPlayer,event,objectFactory);

			// 刷新事件
			JBackKit.deleteFightEvent(beAttackPlayer,event);
		}
		else
		{
			// 返航
			event.setCreatAt(checkTime);
			event.setEventState(FightEvent.RETRUN_BACK);
			// 计算时间
			NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
				attackPlayer.getId());
			if(island!=null)
			{
				int needTime=needTime(island.getIndex(),beIsland.getIndex());
				event.setNeedTime(needTime,attackPlayer,checkTime);
			}
			else
				event.setNeedTime(1,attackPlayer,checkTime);
			// 刷新事件
			JBackKit.sendFightEvent(attackPlayer,event,objectFactory);
			// 刷新事件
			JBackKit.deleteFightEvent(beAttackPlayer,event);
			// 继续推算 是否已经回家
			// checkFightEvent(event,attackPlayer,objectFactory);
			goOnPushEvent=true;
		}
		try
		{
			// 船只日志 主动方
			ShipCheckData shipdata=objectFactory
				.addShipTrack(event.getId(),ShipCheckData.FIGHT_PLAYER_ISLAND,
					attackPlayer,new IntList(),honorScore,true);
			shipdata.setAttackPlayerName(beAttackPlayer.getName());
			shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland
				.getIndex())
				+","
				+beIsland.getName()
				+","
				+beIsland.getIslandLevel()
				+","
				+success+",->"+beAttackPlayer.getName());

			if(allianceDefendPlayer!=null)
			{
				// 船只日志 被动方
				shipdata=objectFactory.addShipTrack(
					event.getId(),ShipCheckData.BE_FIGHT_ISLAND,allianceDefendPlayer,
					new IntList(),null,false);
				shipdata.setAttackPlayerName(attackPlayer.getName());
				shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland
					.getIndex())
					+","
					+beIsland.getName()
					+","
					+beIsland.getIslandLevel()
					+","+!success+",->"+attackPlayer.getName());
				objectFactory.addShipTrack(event.getId(),ShipCheckData.BE_ALLIANCE_HELP,honorScore,beAttackPlayer);
			}
			else
			{
				// 船只日志 被动方
				shipdata=objectFactory.addShipTrack(
					event.getId(),ShipCheckData.BE_FIGHT_ISLAND,beAttackPlayer,
					new IntList(),honorScore,false);
				shipdata.setAttackPlayerName(attackPlayer.getName());
				shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland
					.getIndex())
					+","
					+beIsland.getName()
					+","
					+beIsland.getIslandLevel()
					+","+!success+",->"+attackPlayer.getName());
			}
			// 刷新被动方的船
			JBackKit.resetMainGroup(beAttackPlayer);
			JBackKit.sendResetTroops(beAttackPlayer);
		}
		catch(Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
		if(goOnPushEvent)
		{
			checkFightEvent(event,attackPlayer,objectFactory);
		}
	}
	// /** 攻击NPC城市 */
	// public static void attackNpcIsland(FightEvent event,Player player,
	// NpcIsland beIsland,int nowTime,CreatObjectFactory objectFactory)
	// {
	// int checkTime=event.getCreatAt()+event.getNeedTime();
	// // 如果NPC岛屿等级小于玩家岛屿等级 立即返航
	// if(beIsland.getIslandLevel()<=player.getIsland().getIslandLevel())
	// {
	// // 返航
	// event.setCreatAt(checkTime);
	// event.setEventState(FightEvent.RETRUN_BACK);
	// // 继续推算 是否已经回家
	// checkFightEvent(event,player,objectFactory);
	// return;
	// }
	// // NPC城市 击败后可以搬迁
	// Object[] object=beIsland.fight(event.getFleetGroup());
	// // 重新计算NPC的兵力
	// beIsland.createFleetGroup();
	// FightScene scene=(FightScene)object[0];
	// // 计算伤兵 主动方
	// addHurtShips(player,event.getFleetGroup());
	// // 重新计算NPC的兵力
	// beIsland.createFleetGroup();
	// // 战斗胜利
	// if(scene.getSuccessTeam()==0)
	// {
	// // 占领岛屿
	// if(event.getType()==FightEvent.ATTACK_HOLD)
	// {
	// // 剩余舰队回归
	// event.getFleetGroup().cancel(player);
	// // 更换岛屿
	// NpcIsland island=objectFactory.getIslandCache()
	// .getPlayerIsland(player.getId());
	// island.setPlayerId(0);
	// // 新岛屿
	// beIsland.setPlayerId(player.getId());
	// // 玩家岛屿升级
	// player.getIsland().setIslandLevel(beIsland.getIslandLevel());
	// event.setDelete(FightEvent.DELETE_TYPE);
	// return;
	// }
	// }
	// // 战斗失败
	// fightGroupDown(event,player,checkTime,objectFactory);
	//
	// }

	/** 舰队失败返航 或者被歼灭 */
	public static boolean fightGroupDown(FightEvent event,Player player,
		int checkTime,CreatObjectFactory objectFactory)
	{
		// 被歼灭了
		if(!event.getFleetGroup().existShip())
		{
			// 事件等待删除
			event.setDelete(FightEvent.DELETE_TYPE);
			return true;
		}
		return false;
	}

	/** 计算攻击时间 */
	public static int needTime(int attackIndex,int beIndex)
	{
		int x=attackIndex%NpcIsland.WORLD_WIDTH,y=attackIndex
			/NpcIsland.WORLD_WIDTH;
		int beX=beIndex%NpcIsland.WORLD_WIDTH,beY=beIndex
			/NpcIsland.WORLD_WIDTH;
		double needTime=Math.sqrt(((x-beX)*(x-beX)+(y-beY)*(y-beY)));
		// return 15;
		return (int)(needTime*20+120);
	}

	/** 计算荣誉积分 bool 主动方是否胜利 */
	public static int[] honorScore(Player player,Player bePlayer,boolean bool,CreatObjectFactory objectFactory)
	{
		int honor[]=new int[3];
		int hs=player.getHonorScore();
		int beHs=bePlayer.getHonorScore();
		int honorScore=0;
		if(bool&&beHs>0)
		{
			honorScore=(int)Math.ceil(beHs*0.01);
			player.changeHonorScore(honorScore);
			bePlayer.changeHonorScore(-honorScore);
		}else if(!bool&&hs>0)
		{
			honorScore=(int)Math.ceil(hs*0.01);
			player.changeHonorScore(-honorScore);
			bePlayer.changeHonorScore(honorScore);
		}
			JBackKit.resetHonorScore(player);
			JBackKit.resetHonorScore(bePlayer);
			//objectFactory.addPlayerHonorSocor(player,bePlayer,honorScore,hs,beHs);
			honor[0]=honorScore;
			honor[1]=hs;
			honor[2]=beHs;
		return honor;
	}
	
	/** 玩家战斗增加军官功勋 */
	public static void addOfficerFeatsInPvP(int addFeats,Player attacker)
	{
		attacker.getOfficers().incrFeats(addFeats);
		JBackKit.sendOfficerInfo(attacker);
	}
}
