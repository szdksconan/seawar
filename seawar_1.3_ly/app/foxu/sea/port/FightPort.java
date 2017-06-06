package foxu.sea.port;

import mustang.codec.MD5;
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
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.PlayerGameDBAccess;
import foxu.ds.PlayerKit;
import foxu.fight.FightScene;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Ship;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.NianActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.arena.CombinedFleetManager;
import foxu.sea.award.Award;
import foxu.sea.builds.HurtTroop;
import foxu.sea.checkpoint.ArmsCheckPoint;
import foxu.sea.checkpoint.ArmsRoutePoint;
import foxu.sea.checkpoint.Chapter;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.checkpoint.EliteCheckPoint;
import foxu.sea.checkpoint.ElitePoint;
import foxu.sea.checkpoint.SelfCheckPoint;
import foxu.sea.checkpoint.TearCheckPoint;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;
import foxu.sea.recruit.RecruitDayTask;
import foxu.sea.recruit.RecruitKit;
import foxu.sea.shipdata.ShipCheckData;
import foxu.sea.task.TaskEventExecute;
import foxu.sea.vertify.VertifyManager;
import foxu.sea.worldboss.WorldBoss;

/**
 * 战斗关卡 1006 战斗相关关卡
 */
public class FightPort extends AccessPort
{
	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(FightPort.class);
	/** 新手引导攻打岛屿 */
	public static final int NEW_PLAYER_ATT_ISLAND=11201;
	/** 新手引导航行时间 */
	public static final int NEW_PLAYER_TRAVEL_TIME=2;
	/** 新手引导驻守岛屿总获取 */
	public static final int NEW_PLAYER_HOLD_RESOURCE=40000;
	/** 攻打boss的间隔时间 */
	public static final int ATTACK_BOSS_TIME=60*60*4;
	/** 金币修复 宝石修复 */
	public static final int MONEY_REPARI=1,GEMS_REPARI=2,MONEY_REPARI_ALL=3,
					GEMS_REPARI_ALL=4;
	/** 关卡战斗胜利 */
	public static final int SUCCESS=1;
	/** 扫荡关卡的卷轴 **/
	public static final int PRO_LET=902;
	/** 数据获取类 */
	CreatObjectFactory objectFactory;
	CombinedFleetManager combinedFleetManager;

	/** 验证器管理器 */
	VertifyManager vertifyManager;

	public void setVertifyManager(VertifyManager vertifyManager)
	{
		this.vertifyManager=vertifyManager;
	}

	/** 篡改验证 私钥 */
	String distortKey="nkyntCuBgsNIEGujwH2fHruYUx3YrnXqk0HS1GQbJJqltMlBMggWHymfiurt6kud";
	MD5 md5=new MD5();

	public void setCombinedFleetManager(
		CombinedFleetManager combinedFleetManager)
	{
		this.combinedFleetManager=combinedFleetManager;
	}

	int pointSid[]={10096,10112,10128,10144,10160,10176,10192,10208,10224,
		10240};

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		// 类型
		int type=data.readUnsignedByte();
		objectFactory.pushAll(player,TimeKit.getSecondTime());
		// 关卡战斗
		if(type==PublicConst.FIGHT_CHECK_POINT
			||type==PublicConst.FIGHT_TEAR_POINT)
		{
			// 要攻打的关卡sid
			int checkPointSid=data.readUnsignedShort();
			SelfCheckPoint point=null;
			int cEnergy=player.getIsland()
				.gotEnergy(TimeKit.getSecondTime());// 精力
			if(type==PublicConst.FIGHT_CHECK_POINT)
			{
				point=player.getSelfCheckPoint();
				if(cEnergy<=0)
					throw new DataAccessException(0,"energy is 0");
			}
			else
			{
				point=player.getTearCheckPoint();
				if(cEnergy<PublicConst.TEAR_ENERGY)
					throw new DataAccessException(0,"energy is 0");

			}
			if(type==PublicConst.FIGHT_CHECK_POINT)
			{
				// 检查关卡sid
				if(point.getCheckPointSid()<checkPointSid)
					throw new DataAccessException(0,
						"you can not fight the sid");
			}
			CheckPoint checkPoint=(CheckPoint)CheckPoint.factory
				.newSample(checkPointSid);
			if(checkPoint==null)
				throw new DataAccessException(0,"checkPoint is null");
			if(type==PublicConst.FIGHT_TEAR_POINT)
			{
				if(((TearCheckPoint)point).getCheckPointSid(checkPoint
					.getChapter()-1)<checkPointSid)
					throw new DataAccessException(0,
						"you can not fight the sid");
			}
			if(type==PublicConst.FIGHT_TEAR_POINT
				&&((TearCheckPoint)point).isAttacked(
					checkPoint.getChapter()-1,checkPoint.getIndex()))
			{
				throw new DataAccessException(0,"today attacked this point");
			}
			// 等级限制
			if(checkPoint.getLevelLimit()>player.getLevel())
				throw new DataAccessException(0,"playerLevel need:"
					+checkPoint.getLevelLimit());
			// 军衔限制
			if(checkPoint.getRankLimit()>player.getPlayerType())
				throw new DataAccessException(0,"playerType need:"
					+checkPoint.getRankLimit());
			// 打关卡混编 可以加上城防的兵力
			int commanderNum=data.readUnsignedByte();// 统御书使用量
			commanderNum=0;// 暂不开放神统，消息处理完成后前台自己扣除统御书
			int length=data.readUnsignedByte();
			IntList list=new IntList();
			FleetGroup group=new FleetGroup();
			group.getOfficerFleetAttr().initOfficers(player);
			// 重新计算技能
			SeaBackKit.resetPlayerSkill(player,objectFactory);
			if(length==0)
				throw new DataAccessException(0,"you have no ship fight");
			String str=SeaBackKit.checkShipNumLimit(list,length,data,player,
				player.getIsland().getMainGroup(),commanderNum);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			// 组建舰队
			creatFleetGroup(list,player,group,false);
			Object[] object=checkPoint.fight(group);
			FightScene scene=(FightScene)object[0];
			int successStar=0;
			data.clear();
			data.writeShort(checkPoint.getSid());
			// 胜利
			if(scene.getSuccessTeam()==0)
			{
				int foreSid=player.getSelfCheckPoint().getCheckPointSid();
				int lastStar=player.getSelfCheckPoint().getStar(
					checkPoint.getChapter()-1,checkPoint.getIndex());
				// 发送奖励
				successStar=checkPoint.fightSuccess(player,group,data,
					objectFactory,type,true);
				if(type==PublicConst.FIGHT_CHECK_POINT)
				{
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.POINT_SUCCESS_TASK_EVENT,checkPoint,
						player,null);
					// 成就数据采集
					AchieveCollect.chapterLevel(player);
				}
				else if(type==PublicConst.FIGHT_TEAR_POINT)
				{
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.TEARPOINT_SUCCESS_TASK_EVENT,checkPoint,
						player,null);
				}
				int allstars=player.getSelfCheckPoint().getAllstars()
					+player.getHeritagePoint().getAllstars();
				// 设置总星星数量
				player.setPlunderResource(allstars);
				// 新兵福利
				RecruitKit.pushTask(RecruitDayTask.CHECKPOINT_STARS,
					allstars,player,true);
				for(int i=0;i<pointSid.length;i++)
				{
					if(foreSid==pointSid[i]&&lastStar==0)
					{
						// 系统公告
						String message=InterTransltor.getInstance()
							.getTransByKey(PublicConst.SERVER_LOCALE,
								"success_check_point");
						String checkPointString=InterTransltor.getInstance()
							.getTransByKey(PublicConst.SERVER_LOCALE,
								"success_check_"+foreSid);
						message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
						message=TextKit
							.replace(message,"%",checkPointString);
						SeaBackKit.sendSystemMsg(
							objectFactory.getDsmanager(),message);
						break;
					}
				}
				if(type==PublicConst.FIGHT_CHECK_POINT
					&& point.checkChapterStar((checkPoint.getChapter()-1)))
				{
					checkPoint.addPointBuff(player,objectFactory);
				}
				// 战斗胜利时，扣除精力
				if(type==PublicConst.FIGHT_CHECK_POINT)
				{
					player.reDuceEnergy();
				}
				else
				{
					player.reduceEnergyN(PublicConst.TEAR_ENERGY);
				}
			}
			else
			{
				data.writeByte(successStar);
			}
			// 伤兵计算(修改为无损)
			// IntList hurtList=group.hurtList(FleetGroup.HURT_TROOPS);
			// // 优先扣除港口兵力
			// for(int i=0;i<hurtList.size();i+=2)
			// {
			// int shipSid=hurtList.get(i);
			// int num=hurtList.get(i+1);
			// // 扣除指定sid的船只 优先扣除港口兵力
			// int reduceNum=player.getIsland().reduceShipBySid(shipSid,
			// num,null);
			// if(reduceNum<num)
			// {
			// // 扣除城防里面的
			// reduceShips(player,shipSid,(num-reduceNum));
			// }
			// }
			// 伤兵计算
			// int exp=group.hurtTroopsExp(player,TimeKit.getSecondTime());
			// group.hurtTroops(player,TimeKit.getSecondTime());
			// // 刷新前台
			// if(exp>0)
			// {
			// JBackKit.sendResetTroops(player);
			// JBackKit.resetHurtTroops(player);
			// JBackKit.resetMainGroup(player);
			// }
			// 攻击关卡的
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_TASK_EVENT,checkPoint.getIsland(),player,
				true);
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_POINT_TASK_EVENT,null,player,null);
			FightShowEventRecord record=(FightShowEventRecord)object[1];
			ByteBuffer fight=record.getRecord();
			SeaBackKit.conFightRecord(data,fight,player.getName(),
				player.getLevel(),checkPoint.getName(),
				checkPoint.getPointLevel(),checkPoint.getFightType(),player,
				null,group,checkPoint.getIsland().getFleetGroup(),true,null,null);
			// 船只日志
			// objectFactory.addShipTrack(ShipCheckData.FIGHT_GUAN_KA,player,
			// new IntList());
		}
		// 主建城防舰队
		else if(type==PublicConst.SET_MAIN_GROUP)
		{
			int commanderNum=data.readUnsignedByte();// 统御书使用量
			commanderNum=0;// 暂不开放神统
			// 判断舰队数量
			int length=data.readUnsignedByte();
			IntList list=new IntList();
			String str=SeaBackKit.checkShipNumLimit(list,length,data,player,
				player.getIsland().getMainGroup(),commanderNum);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			FleetGroup group=player.getIsland().getMainGroup();
			// 先释放
			group.cancel(player,false);
			// 组建舰队
			creatFleetGroup(list,player,group,true);
			data.clear();
			objectFactory.addShipTrack(0,ShipCheckData.SET_MAIN,player,
				new IntList(),null,false);
			player.getIsland().showBytesWriteTroops(data,0);
			data.writeByte(commanderNum);
		}
		// 联盟协防
		else if(type==PublicConst.ALLIANCE_DEFEND)
		{
			// vip对应的出战队列
			int vipDeque=PublicConst.VIP_LEVEL_FOR_BATTLE_DEQUE[player
				.getUser_state()];
			ArrayList eventList=SeaBackKit.getFightEventSelf(player,
				objectFactory);
			// 精力检查
			if(player.getIsland().gotEnergy(TimeKit.getSecondTime())<=0)
				throw new DataAccessException(0,"energy is 0");
			if(eventList!=null&&eventList.size()>=vipDeque)
				throw new DataAccessException(0,"fight deque is full");
			// 攻击岛屿的index
			int islandIndex=data.readInt();
			// 先push这个岛屿的事件
			Object object[]=FightKit.pushFightEvent(islandIndex,
				objectFactory);
			if(object!=null)
				FightKit.checkFightEvent(object,objectFactory,player);
			NpcIsland island=objectFactory.getIslandByIndexOnly(islandIndex
				+"");
			if(island==null)
				throw new DataAccessException(0,"island is null");
			// 海水不能去且没有玩家
			if(island.getIslandType()==NpcIsland.ISLAND_WARTER
				&&island.getPlayerId()==0)
				throw new DataAccessException(0,"island is water");
			// 攻击岛屿index
			int attackIndex=objectFactory.getIslandCache()
				.getPlayerIsLandId(player.getId());
			if(island.getPlayerId()==0)
				throw new DataAccessException(0,"you can not defend");
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID)+"");
			if(alliance==null)
			{
				throw new DataAccessException(0,"you are not same alliance");
			}
			// 联盟协防
			if(island.getPlayerId()!=0)
			{
				// 判断是否同盟玩家
				Player befighter=objectFactory.getPlayerById(island
					.getPlayerId());
				Alliance bealliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						befighter.getAttributes(PublicConst.ALLIANCE_ID)+"");
				if(bealliance==null)
					throw new DataAccessException(0,
						"you are not same alliance");
				if(bealliance!=null)
				{
					if(!alliance.getName().equals(bealliance.getName()))
					{
						throw new DataAccessException(0,
							"you are not same alliance");
					}
				}
				// 协防上限5只舰队
				if(allianceNum(befighter)>=5)
				{
					throw new DataAccessException(0,
						"alliance defend is full");
				}
				FleetGroup group=new FleetGroup();
				group.getOfficerFleetAttr().initOfficers(player);
				IntList list=new IntList();
				int commanderNum=data.readUnsignedByte();// 统御书使用量
				commanderNum=0;// 暂不开放神统
				// 舰队信息
				int length=data.readUnsignedByte();
				if(length<=0)
					throw new DataAccessException(0,"you have no ship");
				String str=SeaBackKit.checkShipNumLimit(list,length,data,
					player,player.getIsland().getMainGroup(),commanderNum);
				if(str!=null)
				{
					throw new DataAccessException(0,str);
				}
				// 如果船只都为0
				boolean all=true;
				for(int i=1;i<list.size();i+=3)
				{
					int num=list.get(i+1);
					if(num>0)
					{
						all=false;
						break;
					}
				}
				if(all) throw new DataAccessException(0,"no ships");
				// 组建舰队
				creatFleetGroup(list,player,group,true);
				// 计算时间
				int needTime=FightKit
					.needTime(attackIndex,island.getIndex());
				// 创建事件
				FightEvent event=objectFactory.createFightEvent(
					player.getId(),attackIndex,islandIndex,group);
				event.setCreatAt(TimeKit.getSecondTime());
				event.setNeedTime(needTime,player,TimeKit.getSecondTime());
				event.setType(FightEvent.ATTACK_HOLD);
				data.clear();
				data.writeInt(needTime);
				player.getIsland().showBytesWriteTroops(data,
					TimeKit.getSecondTime());
				data.writeByte(commanderNum);
				//扣除精力
				player.reDuceEnergy();
				//推送行军线
				JBackKit.sendMarchLine(objectFactory,event);
				// 刷新事件
				JBackKit.sendFightEvent(player,event,objectFactory);
				// 加载岛屿身上
				island=objectFactory.getIslandCache().getPlayerIsland(
					befighter.getId());
				JBackKit.sendFightEvent(befighter,event,objectFactory);
				// 船只日志
				IntList fightlist=new IntList();
				for(int i=1;i<list.size();i+=3)
				{
					fightlist.add(list.get(i));
					fightlist.add(list.get(i+1));
				}
				objectFactory.addShipTrack(event.getId(),
					ShipCheckData.ALLIANCE_DEFEND,player,fightlist,null,
					false);
			}
		}
		// 世界战斗 攻打野地,NPC岛屿，或者玩家
		else if(type==PublicConst.WORLD_FIGHT)
		{
			// vip对应的出战队列
			int vipDeque=PublicConst.VIP_LEVEL_FOR_BATTLE_DEQUE[player
				.getUser_state()];
			ArrayList eventList=SeaBackKit.getFightEventSelf(player,
				objectFactory);
			// 精力检查
			if(player.getIsland().gotEnergy(TimeKit.getSecondTime())<=0)
				throw new DataAccessException(0,"energy is 0");
			if(eventList!=null&&eventList.size()>=vipDeque)
				throw new DataAccessException(0,"fight deque is full");
			// 攻击岛屿的index
			int islandIndex=data.readInt();
			// 先push这个岛屿的事件
			Object object[]=FightKit.pushFightEvent(islandIndex,
				objectFactory);
			if(object!=null)
				FightKit.checkFightEvent(object,objectFactory,player);
			NpcIsland island=objectFactory.getIslandByIndexOnly(islandIndex
				+"");
			// 新手引导部分特殊操作
			if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
				island=(NpcIsland)NpcIsland.factory
					.getSample(NEW_PLAYER_ATT_ISLAND);
			if(island==null)
				throw new DataAccessException(0,"island is null");
			if(island.checkDismiss())
				objectFactory.getGemManger().removeGemsIsland(
					island.getIndex());			
			// 海水不能去且没有玩家
			if(island.getIslandType()==NpcIsland.ISLAND_WARTER
				&&island.getPlayerId()==0)
				throw new DataAccessException(0,"island is water");
			// 如果是boss战斗
			if(island.getIslandType()==NpcIsland.WORLD_BOSS)
			{
				// 判断保护时间
				WorldBoss boss=objectFactory.getWorldBossBySid(island
					.getSid());
				if(boss.getProtectTime()>TimeKit.getSecondTime())
				{
					throw new DataAccessException(0,"boss is protect");
				}
				// 判断玩家时间
				if(player.getAttributes(PublicConst.ATTACK_BOSS_TIME)!=null
					&&!player.getAttributes(PublicConst.ATTACK_BOSS_TIME)
						.equals(""))
				{
					int time=Integer.parseInt(player
						.getAttributes(PublicConst.ATTACK_BOSS_TIME));
					if(time>TimeKit.getSecondTime())
					{
						throw new DataAccessException(0,
							"boss attack time limit");
					}
				}
			}
			if(island.getIslandType()==NpcIsland.NIAN_BOSS)
			{
				NianActivity act=(NianActivity)ActivityContainer
					.getInstance().getActivity(ActivityContainer.NIAN_SID,0);
				if(act==null||act.getBoss()==null)
				{
					throw new DataAccessException(0,"nian not open");
				}
				// 判断玩家时间
				if(player.getAttributes(PublicConst.ATTACK_NIAN_TIME)!=null
					&&!player.getAttributes(PublicConst.ATTACK_NIAN_TIME)
						.equals(""))
				{
					int time=Integer.parseInt(player
						.getAttributes(PublicConst.ATTACK_NIAN_TIME));
					if(time>TimeKit.getSecondTime())
					{
						throw new DataAccessException(0,
							"nian attack time limit");
					}
				}
				player
					.setAttribute(PublicConst.ATTACK_NIAN_TIME,
						TimeKit.getSecondTime()
							+act.getBoss().getBeAttackCD()+"");

			}
			String content=null;
			Player befighter=null;
			// 攻击岛屿index
			int attackIndex=objectFactory.getIslandCache()
				.getPlayerIsLandId(player.getId());
			boolean attackPlayer=false;
			String  extra=island.getIslandType()+"";
			// 攻打玩家基地
			if(island.getPlayerId()!=0)
			{
				extra=NpcIsland.NPC_PLAYER+","+island.getPlayerId();
				attackPlayer=true;
				befighter=objectFactory.getPlayerById(island.getPlayerId());
				if(befighter==null)
				{
					throw new DataAccessException(0,"player is null");
				}
				if(befighter.checkService(PublicConst.NOT_FIGHT_BUFF,
					TimeKit.getSecondTime())!=null)
				{
					throw new DataAccessException(0,
						"player have noFightBuff attack");
				}
				if(befighter.getId()==player.getId())
				{
					throw new DataAccessException(0,
						"player can not fight youself");
				}
				content=InterTransltor.getInstance().getTransByKey(
					befighter.getLocale(),"be_attack_self_island_push");
				content=TextKit.replace(content,"%",player.getName());
				content=TextKit.replace(content,"%",befighter.getName());
				SeaBackKit.sendFightPush(content,befighter);
				JBackKit.sendMessageView(befighter,content);
				// 取消免战BUff
				player.removeService(PublicConst.NOT_FIGHT_BUFF);
				// 刷新前台
				JBackKit.sendResetService(player);
				JBackKit.sendPlayerIslandState(objectFactory.getDsmanager()
					.getSessionMap(),0,attackIndex);
			}
			// 被攻击岛屿index
			int beIndex=island.getIndex();
			FleetGroup group=new FleetGroup();
			group.getOfficerFleetAttr().initOfficers(player);
			IntList list=new IntList();
			int commanderNum=data.readUnsignedByte();// 统御书使用量
			// 舰队信息
			int length=data.readUnsignedByte();
			if(length<=0)
				throw new DataAccessException(0,"you have no ship");
			String str=SeaBackKit.checkShipNumLimit(list,length,data,player,
				player.getIsland().getMainGroup(),commanderNum);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			// 如果船只都为0
			boolean all=true;
			for(int i=1;i<list.size();i+=3)
			{
				int num=list.get(i+1);
				if(num>0)
				{
					all=false;
					break;
				}
			}
			if(all) throw new DataAccessException(0,"no ships");
			// 攻击岛屿的index
			if(objectFactory.getIslandByIndexOnly(attackIndex+"")==null)
				throw new DataAccessException(0,"player islandId null");
			// 如果是boss战斗,检测完毕之后进行boss进攻时间设置
			if(island.getIslandType()==NpcIsland.WORLD_BOSS)
			{
				player.setAttribute(PublicConst.ATTACK_BOSS_TIME,
					TimeKit.getSecondTime()+ATTACK_BOSS_TIME+"");
			}
			// 攻打玩家驻守的野地
			if(island.getTempAttackEventId()!=0)
			{
				FightEvent event=(FightEvent)objectFactory.getEventCache()
					.load(island.getTempAttackEventId()+"");
				if(event==null)
				{
					// 加入改变列表
					objectFactory.getIslandByIndex(islandIndex+"");
					island.setTempAttackEventId(0);
				}
				else
				{
					if(event.getPlayerId()!=player.getId())
					{
						befighter=objectFactory.getPlayerById(event
							.getPlayerId());
						if(befighter==null)
						{
							throw new DataAccessException(0,"player is null");
						}
						content=InterTransltor.getInstance().getTransByKey(
							befighter.getLocale(),
							"be_attack_npc_island_push");
						content=TextKit
							.replace(content,"%",player.getName());
						content=TextKit.replace(content,"%",
							befighter.getName());
						String islandName=InterTransltor.getInstance()
							.getTransByKey(befighter.getLocale(),
								island.getName());
						content=TextKit.replace(content,"%",islandName);
						content=TextKit.replace(content,"%",
							SeaBackKit.getIslandLocation(island.getIndex()));
						SeaBackKit.sendFightPush(content,befighter);
						JBackKit.sendMessageView(befighter,content);
					}
				}
			}
			// 组建舰队
			creatFleetGroup(list,player,group,true);
			// 计算时间
			int needTime=FightKit.needTime(attackIndex,beIndex);
			// 创建事件
			FightEvent event=objectFactory.createFightEvent(player.getId(),
				attackIndex,islandIndex,group);
			event.setCreatAt(TimeKit.getSecondTime());
			event.setNeedTime(needTime,player,TimeKit.getSecondTime());
			if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
			{
				event.setNeedTimeDB(NEW_PLAYER_TRAVEL_TIME);
				needTime=NEW_PLAYER_TRAVEL_TIME;
			}
			if(island.getIslandType()==NpcIsland.WORLD_BOSS)
			{
				player.addBossFid(event.getId());
			}
			// 战斗方式 攻击后驻守还是返回
			int attackType=data.readUnsignedByte();
			if(island.getPlayerId()!=0)
			{
				event.setType(FightEvent.ATTACK_BACK);
			}
			else
			{
				event.setType(attackType);
			}
			JBackKit.sendMarchLine(objectFactory,event);
			data.clear();
			data.writeInt(needTime);
			player.getIsland().showBytesWriteTroops(data,
				TimeKit.getSecondTime());
			data.writeByte(commanderNum);
			// 扣除精力
			player.reDuceEnergy();
			// 刷新事件
			JBackKit.sendFightEvent(player,event,objectFactory);
			if(befighter!=null)
			{
				if(!attackPlayer)
				{
					// 加载岛屿身上
					island=objectFactory.getIslandCache().getPlayerIsland(
						befighter.getId());
					if(island!=null)
						objectFactory.getEventCache().addFightEvent(event,
							island.getIndex());
				}
				JBackKit.sendFightEvent(befighter,event,objectFactory);
			}
			// 船只日志
			IntList fightlist=new IntList();
			for(int i=1;i<list.size();i+=3)
			{
				fightlist.add(list.get(i));
				fightlist.add(list.get(i+1));
			}
			ShipCheckData shipData=objectFactory.addShipTrack(event.getId(),
				ShipCheckData.FIGHT_SEND_SHIPS,player,fightlist,null,false);
			shipData.setExtra(extra+","+islandIndex);
		}
		// 侦查
		else if(type==PublicConst.WORLD_FIGHT_VIEW)
		{
			// 攻击岛屿的id
			int islandId=data.readInt();
			int num=data.readInt();
			String md=data.readUTF();
			StringBuffer sub=new StringBuffer();
			sub.append(type);
			sub.append(islandId);
			sub.append(num);
			sub.append(distortKey);
			String sign_md5=md5.encode(sub.toString());
			if(!md.equalsIgnoreCase(sign_md5))
			{
				throw new DataAccessException(0,"error");
			}
			NpcIsland island=objectFactory.getIslandByIndex(islandId+"");
			if(island==null)
				throw new DataAccessException(0,"island is null");
			// 海水不能去且没有玩家
			if(island.getIslandType()==NpcIsland.ISLAND_WARTER
				&&island.getPlayerId()==0)
				throw new DataAccessException(0,"island is water");
			if(island.checkDismiss())
			{
				objectFactory.getGemManger().removeGemsIsland(
						island.getIndex());			
				//清楚岛屿信息
				throw new DataAccessException(0,"gems island is null");
			}
			data.clear();
			// 先push这个岛屿的事件
			Object object[]=FightKit.pushFightEvent(islandId,objectFactory);
			if(object!=null)
				FightKit.checkFightEvent(object,objectFactory,player);
			// 玩家
			/** 判断是否是敌对联盟 **/
			boolean flag=false;
			/**buff**/
			IntList buff=null;
			int endtime=0;
			if(island.getPlayerId()!=0)
			{
				Player beAttacker=objectFactory.getPlayerById(island
					.getPlayerId());
				if(beAttacker==null)
				{
					throw new DataAccessException(0,"bePlayer is null");
				}
				if(beAttacker.checkService(PublicConst.NOT_FIGHT_BUFF,
					TimeKit.getSecondTime())!=null)
				{
					throw new DataAccessException(0,
						"player have noFightBuff view");
				}
				// 金币限制
				int money=PublicConst.SCOUT_MONEYCOST[beAttacker.getLevel()-1];
				if(!Resources.checkResources(0,0,0,0,money,
					player.getResources()))
				{
					JBackKit.sendResetResources(player);
					throw new DataAccessException(0,
						"city_info_not_enough_money");
				}
				// 扣除金币
				Resources.reduceResources(player.getResources(),0,0,0,0,
					money,player);
				// 帮被侦查方push消息
				objectFactory.pushAll(beAttacker,TimeKit.getSecondTime());
				// 创建邮件
				Message message=objectFactory.createMessageOnly(0,
					player.getId(),"","",player.getName(),
					Message.FIGHT_TYPE,"",true);
				FightEvent event=null;
				if(beAttacker.getAttributes(PublicConst.ALLIANCE_DEFND_ATT)!=null
					&&!beAttacker.getAttributes(
						PublicConst.ALLIANCE_DEFND_ATT).equals(""))
				{
					event=(FightEvent)objectFactory
						.getEventCache()
						.loadOnly(
							beAttacker
								.getAttributes(PublicConst.ALLIANCE_DEFND_ATT)
								+"");
				}
				String allianceName=SeaBackKit.getAllianceName(beAttacker,
					objectFactory);
				// 是否是敌对联盟
				if(allianceName!=null&&allianceName.length()!=0)
					flag=SeaBackKit.isHostile(objectFactory,player,
						allianceName);
				if(event!=null&&event.getFleetGroup().existShip())
				{
					Player alliancePlayer=objectFactory.getPlayerById(event
						.getPlayerId());
					data=message.createZheChaBuffer(beAttacker,0,
						island.getIndex(),event.getFleetGroup(),"",0,
						alliancePlayer.getName(),allianceName,flag,buff,endtime);
				}
				else
				{
					// 添加玩家的城防部队
					data=message.createZheChaBuffer(beAttacker,0,island
						.getIndex(),beAttacker.getIsland().getMainGroup(),
						"",0,"",allianceName,flag,buff,endtime);
				}
				message.addReciveState(Message.READ);
			}
			// NPC野地
			else
			{
				// 是否需要验证器验证
				boolean isTest=vertifyManager.isNeedTest(player);
				if(isTest)
				{
					JBackKit.sendPopupVertify(player,vertifyManager);
					throw new DataAccessException(0,"need_to_vertify");
				}
				// 金币限制
				int money=PublicConst.SCOUT_MONEYCOST[island
					.getIslandLevel()-1];
				if(!Resources.checkResources(0,0,0,0,money,
					player.getResources()))
				{
					throw new DataAccessException(0,
						"city_info_not_enough_money");
				}
				// 扣除金币
				Resources.reduceResources(player.getResources(),0,0,0,0,
					money,player);
				// 创建邮件
				Message message=objectFactory.createMessageOnly(0,
					player.getId(),"","",player.getName(),
					Message.FIGHT_TYPE,"",true);
				// 资源
				message.addState(Message.READ);
				message.addReciveState(Message.READ);
				String name="";
				int pluerResource=0;
				FleetGroup group=island.getFleetGroup();
				String allianceName="";
				endtime=island.getEndTime();
				if(island.getTempAttackEventId()!=0)
				{
					FightEvent event=(FightEvent)objectFactory
						.getEventCache().loadOnly(
							island.getTempAttackEventId()+"");
					if(event==null)
					{
						island.setTempAttackEventId(0);
					}
					else
					{
						/**查询buff**/
						island.checkBuff(TimeKit.getSecondTime()-event.getCreatAt(),objectFactory);
						buff=island.getServices();
						group=event.getFleetGroup();
						Player bePlayer=objectFactory.getPlayerCache()
							.loadPlayerOnly(event.getPlayerId()+"");
						name=bePlayer.getName();
						allianceName=SeaBackKit.getAllianceName(bePlayer,
							objectFactory);
						int passTime=(TimeKit.getSecondTime()-event
							.getCreatAt());
						if(passTime>FightKit.MONTH_SECOND)
							passTime=FightKit.MONTH_SECOND;
						// 避免因为多次整数计算带来采集玩家与侦查玩家显示不一致
						int canCarry=0;
						pluerResource=(int)((float)island.getResource()/60*passTime)
							+SeaBackKit.resourceTotal(event.getResources());
						// 掠夺资源量
						canCarry=SeaBackKit.groupCarryResource(group);
						if(pluerResource>canCarry)pluerResource=canCarry;
						if(island.getIslandType()==NpcIsland.ISLAND_GEMS)
							pluerResource=pluerResource/PublicConst.LOWLIMIT_GEMS_TIMES;
						// 是否是敌对联盟
						if(allianceName!=null&&allianceName.length()!=0)
							flag=SeaBackKit.isHostile(objectFactory,player,
								allianceName);
					}
				}
				data=message.createZheChaBuffer(player,island.getSid(),
					island.getIndex(),group,name,pluerResource,"",
					allianceName,flag,buff,endtime);
			}
			log.error(player.getId()+" : "+(islandId%600+1)+","+(islandId/600+1));
		}
		// 岛屿基本信息
		else if(type==PublicConst.VIEW_ISLAND_INFO)
		{
			/** index */
			int index=data.readInt();
			NpcIsland island=objectFactory.getIslandCache().loadOnly(
				index+"");
			if(island==null)
				throw new DataAccessException(0,"island is null");
			if(island.getPlayerId()==0)
			{
				throw new DataAccessException(0,"island is not player");
			}
			if(!((PlayerGameDBAccess)objectFactory.getPlayerCache()
				.getDbaccess()).isExistByID(island.getPlayerId()))
			{
				throw new DataAccessException(0,"playerId not exist");
			}
			Player beplayer=objectFactory
				.getPlayerById(island.getPlayerId());
			data.clear();
			data.writeByte(beplayer.getPlayerType());
			data.writeInt(beplayer.getFightScore());
			String allianceStr="";
			if(beplayer.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!beplayer.getAttributes(PublicConst.ALLIANCE_ID)
					.equals(""))
			{
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().loadOnly(
						beplayer.getAttributes(PublicConst.ALLIANCE_ID));
				if(alliance!=null)
				{
					allianceStr=alliance.getName();
				}
			}
			data.writeUTF(allianceStr);
			if(beplayer.getProsperityInfo()[1]==0){//繁荣度检测时间为0
				beplayer.getIsland().gotProsperityInfo(TimeKit.getSecondTime());//重置繁荣度
			}
			data.writeInt(beplayer.getProsperityInfo()[0]);
			data.writeInt(beplayer.getProsperityInfo()[2]);
			data.writeInt(beplayer.getAttrHead());
			data.writeInt(beplayer.getAttrHeadBorder());
		}
		// 修复船只
		else if(type==PublicConst.REPARI_SHIPS)
		{
			int repairType=data.readUnsignedByte();
			if(repairType==MONEY_REPARI)
			{
				int len=data.readUnsignedShort();
				IntList repairShips=new IntList();
				long need=0;
				for(int i=0;i<len;i++)
				{
					int sid=data.readUnsignedShort();
					int num=data.readInt();

					// 伤兵
					HurtTroop troop=player.getIsland().getHurtTroop(sid);
					if(troop==null)
						throw new DataAccessException(0,"no this hurtTroop");
					Ship ship=(Ship)Ship.factory.getSample(troop
						.getShipSid());
					int cannum=troop.getNum();
					cannum=num>cannum?cannum:num;
					long needMoney=((long)ship.getGlodCost())*cannum;
					if(need+needMoney>player.getResources()[Resources.MONEY])
					{
						cannum=(int)((player.getResources()[Resources.MONEY]-need)/ship
							.getGlodCost());
					}
					if(cannum<=0)
						throw new DataAccessException(0,"money not enough");
					need+=cannum*((long)ship.getGlodCost());
					repairShips.add(sid);
					repairShips.add(cannum);
				}
				Resources.reduceResources(player.getResources(),0,0,0,0,
					need,player);
				// 船只日志
				objectFactory.addShipTrack(0,ShipCheckData.REAPRIE_SHIP,
					player,repairShips,null,false);
				data.clear();
				data.writeShort(repairShips.size()/2);
				for(int i=0;i<repairShips.size();i+=2)
				{
					HurtTroop troop=player.getIsland().getHurtTroop(
						repairShips.get(i));
					// 移除伤兵
					boolean bool=player.getIsland().reduceHurtTroop(
						repairShips.get(i),repairShips.get(i+1));
					// 恢复伤兵
					if(bool)
					{
						player.getIsland().addTroop(repairShips.get(i),
							repairShips.get(i+1),
							player.getIsland().getTroops());
						player.autoAddMainGroup();
					}
					troop.showBytesWrite(data,TimeKit.getSecondTime());
				}
				player.getIsland().bytesWriteTroop(data);
				JBackKit.sendFightScore(player,objectFactory,true,
					FightScoreConst.REPARI_SHIPS);
				return data;
			}
			else if(repairType==GEMS_REPARI)
			{
				int len=data.readUnsignedShort();
				IntList repairShips=new IntList();
				int needGems=0;
				for(int i=0;i<len;i++)
				{
					int sid=data.readUnsignedShort();
					int num=data.readInt();
					// 伤兵
					HurtTroop troop=player.getIsland().getHurtTroop(sid);
					if(troop==null)
						throw new DataAccessException(0,"no this hurtTroop");
					Ship ship=(Ship)Ship.factory.getSample(troop
						.getShipSid());
					int cannum=troop.getNum();
					cannum=num>cannum?cannum:num;
					int fneedGems=(int)(Math.ceil(ship.getGemRepair()*cannum));
					int need=fneedGems+needGems;
					// needGems+=fneedGems==needGems?0:1;
					if(!Resources.checkGems(need,player.getResources()))
						throw new DataAccessException(0,"gems not enough");
					repairShips.add(sid);
					repairShips.add(num);
					needGems+=fneedGems;
				}
				if(!Resources.reduceGems(needGems,player.getResources(),
					player))
					throw new DataAccessException(0,"repair gems too much");
				// 宝石消费记录
				objectFactory.createGemTrack(GemsTrack.REPARIE_SHIPS,
					player.getId(),needGems,0,
					Resources.getGems(player.getResources()));
				// 发送change消息
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,needGems);
				// 船只日志
				objectFactory.addShipTrack(0,ShipCheckData.REAPRIE_SHIP,
					player,repairShips,null,false);
				data.clear();
				data.writeShort(repairShips.size()/2);
				for(int i=0;i<repairShips.size();i+=2)
				{
					HurtTroop troop=player.getIsland().getHurtTroop(
						repairShips.get(i));
					// 移除伤兵
					boolean bool=player.getIsland().reduceHurtTroop(
						repairShips.get(i),repairShips.get(i+1));
					// 恢复伤兵
					if(bool)
					{
						player.getIsland().addTroop(repairShips.get(i),
							repairShips.get(i+1),
							player.getIsland().getTroops());
						player.autoAddMainGroup();
					}
					troop.showBytesWrite(data,TimeKit.getSecondTime());
				}
				player.getIsland().bytesWriteTroop(data);
				JBackKit.sendFightScore(player,objectFactory,true,
					FightScoreConst.REPARI_SHIPS);
				return data;
			}
			else if(repairType==MONEY_REPARI_ALL)
			{
				long needMoney=0;
				Object object[]=player.getIsland().getHurtsTroops()
					.getArray();
				for(int i=0;i<object.length;i++)
				{
					if(object[i]==null) continue;
					HurtTroop troop=(HurtTroop)object[i];
					Ship ship=(Ship)Ship.factory.getSample(troop
						.getShipSid());
					if(ship!=null)
					{
						needMoney+=(long)ship.getGlodCost()*troop.getNum();
					}
				}
				if(needMoney>player.getResources()[Resources.MONEY])
					throw new DataAccessException(0,"money not enough");
				Resources.reduceResources(player.getResources(),0,0,0,0,
					needMoney,player);
				// 恢复所有伤兵
				player.getIsland().repairAllHurtTroops();
				// 移除所有伤兵
				player.getIsland().removeAllTroop();
				player.autoAddMainGroup();
				// 船只日志
				objectFactory.addShipTrack(0,ShipCheckData.REAPRIE_SHIP,
					player,new IntList(),null,false);
			}
			else if(repairType==GEMS_REPARI_ALL)
			{
				float needGems=0;
				Object object[]=player.getIsland().getHurtsTroops()
					.getArray();
				for(int i=0;i<object.length;i++)
				{
					if(object[i]==null) continue;
					HurtTroop troop=(HurtTroop)object[i];
					Ship ship=(Ship)Ship.factory.getSample(troop
						.getShipSid());
					if(ship!=null)
					{
						float need=ship.getGemRepair()*troop.getNum();
						int rgems=(int)need;
						if(need!=rgems) rgems+=1;
						needGems+=rgems;
					}
				}
				if(!Resources.checkGems((int)needGems,player.getResources()))
					throw new DataAccessException(0,"gems not enough");
				Resources.reduceGems((int)needGems,player.getResources(),
					player);
				// 恢复所有伤兵
				player.getIsland().repairAllHurtTroops();
				// 移除所有伤兵
				player.getIsland().removeAllTroop();
				player.autoAddMainGroup();
				// 宝石消费记录
				objectFactory.createGemTrack(GemsTrack.REPARIE_SHIPS,
					player.getId(),(int)needGems,1,
					Resources.getGems(player.getResources()));
				// 发送change消息
				TaskEventExecute.getInstance()
					.executeEvent(PublicConst.GEMS_ADD_SOMETHING,this,
						player,(int)needGems);
			}
			data.clear();
			player.getIsland().bytesWriteTroop(data);

			// 船只日志
			objectFactory.addShipTrack(0,ShipCheckData.REAPRIE_SHIP,player,
				new IntList(),null,false);
			JBackKit.sendFightScore(player,objectFactory,true,
				FightScoreConst.REPARI_SHIPS);
		}
		// 获取事件
		else if(type==PublicConst.GET_FIGHT_EVENT)
		{
			data.clear();
			// 找到玩家岛屿
			int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
				player.getId());
			ArrayList fightEventList=objectFactory.getEventCache()
				.getFightEventListById(islandId);
			if(fightEventList==null)
				data.writeShort(0);
			else
			{
				data.writeShort(fightEventList.size());
				for(int i=0;i<fightEventList.size();i++)
				{
					FightEvent event=(FightEvent)fightEventList.get(i);
					if(event.getDelete()==FightEvent.DELETE_TYPE) continue;
					SeaBackKit.showByteswrite(data,TimeKit.getSecondTime(),
						event,objectFactory);
				}
			}
		}
		// push事件 推事件
		else if(type==PublicConst.EVENT_PUSH)
		{
			int islandIndex=objectFactory.getIslandCache()
				.getPlayerIsLandId(player.getId());
			Object object[]=FightKit.pushFightEvent(islandIndex,
				objectFactory);
			if(object!=null)
				FightKit.checkFightEvent(object,objectFactory,player);
		}
		// 舰队返航
		else if(type==PublicConst.SHIP_RRETURN_BACK)
		{
			int eventId=data.readInt();
			FightEvent back=(FightEvent)objectFactory.getEventCache().load(
				eventId+"");
			String result=SeaBackKit.shipReturnBack(back,player);
			if(result!=null)
				throw new DataAccessException(0,result);
		}
		// 选择某一个事件
		else if(type==PublicConst.CHOOSE_FIGHT_EVENT)
		{
			int eventId=data.readInt();
			if(getAllianceDefendEvent(player,eventId)==null)
				throw new DataAccessException(0,"defend event is not exist");
			player.setAttribute(PublicConst.ALLIANCE_DEFND_ATT,eventId+"");
		}
		else if(type==PublicConst.CANCEL_EVENT)
		{
			player.setAttribute(PublicConst.ALLIANCE_DEFND_ATT,null);
		}
		// 联盟事件返航
		else if(type==PublicConst.ALLIANCE_DEFEND_BACK)
		{
			int eventId=data.readInt();
			FightEvent back=getAllianceDefendEvent(player,eventId);
			if(back==null)
				throw new DataAccessException(0,"defend event is not exist");
			// 设置状态
			back.setEventState(FightEvent.RETRUN_BACK);
			NpcIsland island=objectFactory.getIslandCache().load(
				back.getAttackIslandIndex()+"");
			NpcIsland beIsland=objectFactory.getIslandCache().load(
				back.getSourceIslandIndex()+"");
			int nowTime=TimeKit.getSecondTime();
			int needTime=FightKit.needTime(island.getIndex(),
				beIsland.getIndex());
			// 设置时间
			back.setCreatAt(nowTime);
			Player backPlayer=objectFactory
				.getPlayerById(back.getPlayerId());
			if(backPlayer!=null)
			{
				// 设置需要时间
				back.setNeedTime(needTime,
					objectFactory.getPlayerById(back.getPlayerId()),nowTime);
				removeFightEventId(player,eventId);
				//推送行军线
				JBackKit.sendMarchLine(objectFactory,back);
				JBackKit.sendFightEvent(backPlayer,back,objectFactory);
			}
			int event=0;
			if(player.getAttributes(PublicConst.ALLIANCE_DEFND_ATT)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_DEFND_ATT)
					.equals(""))
			{
				event=Integer.parseInt(player
					.getAttributes(PublicConst.ALLIANCE_DEFND_ATT));
			}
			if(event==back.getId())
			{
				player.setAttribute(PublicConst.ALLIANCE_DEFND_ATT,null);
			}
		}
		else if(type==PublicConst.CLEAR_TEAR_POINT)
		{
			int chapter=data.readUnsignedByte()-21;// 前台章节是从21开始
			TearCheckPoint point=player.getTearCheckPoint();
			if(!point.isAttackChapter(chapter))
			{
				throw new DataAccessException(0,"not need reset");
			}
			if(!point.canPayReset(chapter))
			{
				throw new DataAccessException(0,"max pay count");
			}
			if(!Resources.checkGems(PublicConst.TEAR_CLEAR_GEMS,
				player.getResources()))
			{
				throw new DataAccessException(0,"gems limit");
			}
			point.clear(chapter);
			Resources.reduceGems(PublicConst.TEAR_CLEAR_GEMS,
				player.getResources(),player);
			point.addPayCount(chapter);
			objectFactory.createGemTrack(GemsTrack.CLEAR_TEAR,
				player.getId(),PublicConst.TEAR_CLEAR_GEMS,0,
				Resources.getGems(player.getResources()));
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				PublicConst.TEAR_CLEAR_GEMS);
		}
		else if(type==PublicConst.GET_TEAR_POINT)
		{
			int chapter=data.readUnsignedByte()-21;
			TearCheckPoint point=player.getTearCheckPoint();
			data.clear();
			data.writeInt(point.getAttackValueByChapter(chapter));
			data.writeByte(point.getPayCount(chapter));
		}
		// 得到随机的种子
		else if(type==PublicConst.GET_RANDOM)
		{
			combinedFleetManager.getRandom(player,data);
		}
		// 联合舰队攻打关卡
		else if(type==PublicConst.SET_FLEET)
		{
			combinedFleetManager.comBinedFleetFight(player,data,type);
		}
		else if(type==PublicConst.GET_TIMENOW)
		{
			int timenow=TimeKit.getSecondTime();
			data.clear();
			data.writeInt(SeaBackKit.getWeekEndTime()-timenow);
		}
		else if(type==PublicConst.GET_COMBINDED_POINT)
		{
			// int commanderNum=data.readUnsignedByte();// 统御书使用量
			int commanderNum=0;
			IntList list=new IntList();
			IntKeyHashMap map=new IntKeyHashMap();
			String str=combinedFleetManager.fleetscheckShipNumLimit(map,
				list,data,player,player.getIsland().getMainGroup(),
				commanderNum);// 检测船只
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			// 保存玩家联合舰队的设置
			int f=1;
			for(int i=0;i<list.size();i+=3)
			{
				int[] ship=new int[3];
				ship[0]=list.get(i);
				ship[1]=list.get(i+1);
				ship[2]=list.get(i+2);
				map.put(f,ship);
				f++;
			}
			player.getHeritagePoint().setSetshipFleets(map);
			combinedFleetManager.comBinedFleetFight(player,data,type);
		}
		// 军备航线
		else if(type==PublicConst.FIGHT_ARMS_ROUTE
			||type==PublicConst.SWEEP_POINT)
		{
			// 要攻打的关卡sid
			int checkPointSid=data.readUnsignedShort();
			ArmsCheckPoint point=(ArmsCheckPoint)ArmsCheckPoint.factory
				.newSample(checkPointSid);
			if(point==null)
			{
				throw new DataAccessException(0,"checkPoint is null");
			}
			// 等级限制
			if(point.getLevelLimit()>player.getLevel())
				throw new DataAccessException(0,"playerLevel need:"
					+point.getLevelLimit());
			// 军衔限制
			if(point.getRankLimit()>player.getPlayerType())
				throw new DataAccessException(0,"playerType need:"
					+point.getRankLimit());
			ArmsRoutePoint arPoint=player.getArmsroutePoint();
			// 扫荡
			if(type==PublicConst.SWEEP_POINT)
			{
				if(!arPoint.canattact(checkPointSid))
					throw new DataAccessException(0,"star not three");
				// 扫荡的次数
				int times=data.readUnsignedByte();
				String attract=arPoint.canChalleng(checkPointSid,
					point.getChallengTime(),times);
				if(attract!=null) throw new DataAccessException(0,attract);
				int pnum=player.getBundle().getCountBySid(PRO_LET);
				int descpro=times-pnum;
				if(descpro>0)
				{
					// 当前宝石数量是否足够
					if(!Resources.checkGems(PublicConst.ARMS_SWEEP_COST_GEMS
						*descpro,player.getResources()))
						throw new DataAccessException(0,"gems limit");
					// 扣除宝石
					Resources.reduceGems(PublicConst.ARMS_SWEEP_COST_GEMS
						*descpro,player.getResources(),player);
					// 日志记录
					objectFactory.createGemTrack(GemsTrack.SWEEP_ARMS_POINT,
						player.getId(),PublicConst.ARMS_SWEEP_COST_GEMS
							*descpro,0,
						Resources.getGems(player.getResources()));
					player.getBundle().decrProp(PRO_LET,pnum);
					if(pnum>0)
					{
						JBackKit.sendResetBunld(player);
					}
					// player.getBundle().decrProp(PRO_LET,times-descpro);
				}
				else
				{
					player.getBundle().decrProp(PRO_LET,times);
					JBackKit.sendResetBunld(player);
				}
				data.clear();
				data.writeShort(checkPointSid);
				data.writeByte(times);
				for(int i=0;i<times;i++)
				{
					point.getIsland();// 初始化island
					point.fightSuccess(player,null,data,objectFactory,
						checkPointSid,false);
				}
				if(descpro>0)
				// 发送change消息
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.GEMS_ADD_SOMETHING,this,player,
						PublicConst.ARMS_SWEEP_COST_GEMS*descpro);

			}
			// 攻打军备航线
			else
			{
				String attract=arPoint.canChalleng(checkPointSid,
					point.getChallengTime(),1);
				if(attract!=null) throw new DataAccessException(0,attract);
				// 打关卡混编 可以加上城防的兵力
				int commanderNum=data.readUnsignedByte();// 统御书使用量
				commanderNum=0;// 暂不开放神统，消息处理完成后前台自己扣除统御书
				int length=data.readUnsignedByte();
				IntList list=new IntList();
				FleetGroup group=new FleetGroup();
				group.getOfficerFleetAttr().initOfficers(player);
				// 重新计算技能
				SeaBackKit.resetPlayerSkill(player,objectFactory);
				if(length==0)
					throw new DataAccessException(0,"you have no ship fight");
				String str=SeaBackKit.checkShipNumLimit(list,length,data,
					player,player.getIsland().getMainGroup(),commanderNum);
				if(str!=null)
				{
					throw new DataAccessException(0,str);
				}
				// 组建舰队
				creatFleetGroup(list,player,group,false);
				Object[] object=point.fight(group);
				FightScene scene=(FightScene)object[0];
				int successStar=0;
				data.clear();
				data.writeShort(point.getSid());
				// 胜利
				if(scene.getSuccessTeam()==0)
				{
					successStar=point.fightSuccess(player,group,data,
						objectFactory,checkPointSid,true);
				}
				else
				{
					data.writeByte(successStar);
				}
				FightShowEventRecord record=(FightShowEventRecord)object[1];
				ByteBuffer fight=record.getRecord();
				SeaBackKit.conFightRecord(data,fight,player.getName(),player
					.getLevel(),point.getName(),point.getPointLevel(),point
					.getFightType(),player,null,group,point.getIsland()
					.getFleetGroup(),true,null,null);
			}
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ARMS_POINT_EVENT,null,player,null);
		}
		/** 刷新军备航线的次数 **/
		else if(type==PublicConst.CLEAR_ARMS_POINT)
		{
			int checkPointSid=data.readUnsignedShort();
			ArmsCheckPoint point=(ArmsCheckPoint)CheckPoint.factory
				.newSample(checkPointSid);
			if(point==null)
			{
				throw new DataAccessException(0,"checkPoint is null");
			}
			ArmsRoutePoint arPoint=player.getArmsroutePoint();
			int count=arPoint.getPayCount(checkPointSid);
			// 付费达到上限
			if(count>=point.getPayCount())
				throw new DataAccessException(0,"times limit");
			// 挑战次数未达到上限
			if(point.getChallengTime()<arPoint
				.getChallengTimes(checkPointSid))
				throw new DataAccessException(0,"times is out");
			// 当前宝石数量是否足够
			if(!Resources.checkGems(point.getGems()[count],
				player.getResources()))
				throw new DataAccessException(0,"gems limit");
			// 扣除宝石
			Resources.reduceGems(point.getGems()[count],
				player.getResources(),player);
			objectFactory.createGemTrack(GemsTrack.CLEAR_ARMS_POINT,
				player.getId(),point.getGems()[count],0,
				Resources.getGems(player.getResources()));
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				point.getGems()[count]);
			// 清除关卡记录
			arPoint.clearChalleng(checkPointSid);
			data.clear();
			data.writeShort(checkPointSid);
			data.writeByte(arPoint.getChallengTimes(checkPointSid));
			data.writeByte(arPoint.getPayCount(checkPointSid));
		}
		// 关卡扫荡
		else if(type==PublicConst.CHECK_SWEEP)
		{
			// 要攻打的关卡sid
			int checkPointSid=data.readUnsignedShort();
			CheckPoint checkPoint=(CheckPoint)CheckPoint.factory
				.newSample(checkPointSid);
			if(checkPoint!=null)
				sweepCheckPoint(player,data,checkPoint,checkPointSid);
			else
				throw new DataAccessException(0,"checkPoint is null");
		}
		// 设置备用阵型
		else if(type==PublicConst.SET_FORMATION)
		{
			int index=data.readUnsignedByte();
			// 判断舰队数量
			int length=data.readUnsignedByte();
			// 存储时，默认计入最大神统
			int commanderNum=0;
			int vip=player.getPlayerType()-PublicConst.COMMANDER_OPEN_LEVEL;
			if(vip>=0) commanderNum=PublicConst.CLEVEL_CNUM[vip];
			IntList list=new IntList();
			String str=SeaBackKit.checkShipNumLimit(list,length,data,player,
				player.getIsland().getMainGroup(),commanderNum,false);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			// 去除list头部的神统记录
			list.removeIndex(0);
			data.clear();
			if(index<1)
				throw new DataAccessException(0,"ERROR_DESC_INVALID_PARAM");
			if(player.getUser_state()<PublicConst.FORMATION_VIP[index-1])
				throw new DataAccessException(0,"chooseType is wrong");
			ArrayList formations=player.getFormationList();
			initFormationGroups(player,formations);
			formations.set(list,index-1);
		}
		// 获取备用阵型
		else if(type==PublicConst.GET_FORMATION)
		{
			data.clear();
			ArrayList formations=player.getFormationList();
			initFormationGroups(player,formations);
			data.writeByte(formations.size());
			for(int i=0;i<formations.size();i++)
			{
				IntList fg=(IntList)formations.get(i);
				// 判断该阵容是否可用(数量)
				boolean isAvailable=true;
				if(SeaBackKit.checkFormationShipNum(player,player
					.getIsland().getMainGroup(),fg)!=null)
					isAvailable=false;
				data.writeBoolean(isAvailable);
				data.writeByte(fg.size()/3);
				for(int j=0;j<fg.size();j+=3)
				{
					data.writeShort(fg.get(j));
					data.writeShort(fg.get(j+1));
					data.writeByte(fg.get(j+2));
				}
			}
		}
		//关卡星级宝箱
		else if(type==PublicConst.CHECK_POINT_CHEST)
		{
			int chapter=data.readUnsignedShort();
			int index=data.readUnsignedByte();
			SelfCheckPoint scp=player.getSelfCheckPoint();
			if(scp==null) scp=new SelfCheckPoint();
			Chapter c = (Chapter)Chapter.factory.getSample(chapter);
			int[] awardSid=c.getAwards();
			Award aw=(Award)Award.factory.getSample(awardSid[index]);
			// 验证是否可以领奖
			int flag=scp.checkReceived(chapter-1,index,c);
			if(flag==1)
				throw new DataAccessException(0,
					"you can`t get the raward");
			if(flag==2)
				throw new DataAccessException(0,"you have been rewarded");
			// 领奖
			aw.awardSelf(player,TimeKit.getSecondTime(),data,
				objectFactory,null,new int[]{EquipmentTrack.PAY_RELAY});
			// 添加领奖记录
			scp.addRecord(chapter-1,index);
			data.clear();
			data.writeByte(index);
		}
		/**领取关卡宝箱**/
		else if(type==PublicConst.COMBINED_POINT_CHEST)
		{
			//前面默认未0
			int chapter=data.readUnsignedShort()+1;
			if(!SeaBackKit.isContainValue(PublicConst.COMBINED_CHAPTER,
				chapter)) throw new DataAccessException(0,"error chapter");
			int types=player.getHeritagePoint().checkGetAward(chapter);
			if(types==0)
				throw new DataAccessException(0,"you can not get the raward");
			else if(types==2)
				throw new DataAccessException(0,"you have been rewarded");
			else
				player.getHeritagePoint().addAwardRecord(chapter);
			Award award= (Award)Award.factory.newSample(PublicConst.COMBINED_AWARD[chapter-1]);
			// 领奖
			award.awardSelf(player,TimeKit.getSecondTime(),data,
				objectFactory,null,new int[]{EquipmentTrack.PAY_RELAY});
		}
		else if(type==PublicConst.FIGHT_ELITE_POINT
			||type==PublicConst.SWEEP_ELITE_POINT)
		{
			// 要攻打的关卡sid
			int checkPointSid=data.readUnsignedShort();
			EliteCheckPoint point=(EliteCheckPoint)EliteCheckPoint.factory
				.newSample(checkPointSid);
			if(point==null)
			{
				throw new DataAccessException(0,"checkPoint is null");
			}
			// 等级限制
			if(point.getLevelLimit()>player.getLevel())
				throw new DataAccessException(0,"playerLevel need:"
					+point.getLevelLimit());
			// 军衔限制
			if(point.getRankLimit()>player.getPlayerType())
				throw new DataAccessException(0,"playerType need:"
					+point.getRankLimit());
			ElitePoint ePoint=player.getElitePoint();
			// 扫荡
			if(type==PublicConst.SWEEP_ELITE_POINT)
			{
				if(!ePoint.canattact(checkPointSid))
					throw new DataAccessException(0,"star not three");
				// 扫荡的次数
				int times=data.readUnsignedByte();
				String attract=ePoint.canChalleng(checkPointSid,times);
				if(attract!=null) throw new DataAccessException(0,attract);
				int pnum=player.getBundle().getCountBySid(PRO_LET);
				int descpro=times-pnum;
				if(descpro>0)
				{
					// 当前宝石数量是否足够
					if(!Resources.checkGems(
						PublicConst.ELITE_SWEEP_COST_GEMS*descpro,
						player.getResources()))
						throw new DataAccessException(0,"gems limit");
					// 扣除宝石
					Resources.reduceGems(PublicConst.ELITE_SWEEP_COST_GEMS
						*descpro,player.getResources(),player);
					// 日志记录
					objectFactory.createGemTrack(
						GemsTrack.SWEEP_ELITE_POINT,player.getId(),
						PublicConst.ELITE_SWEEP_COST_GEMS*descpro,0,
						Resources.getGems(player.getResources()));
					player.getBundle().decrProp(PRO_LET,pnum);
					if(pnum>0)
					{
						JBackKit.sendResetBunld(player);
					}
				}
				else
				{
					player.getBundle().decrProp(PRO_LET,times);
					JBackKit.sendResetBunld(player);
				}
				data.clear();
				data.writeShort(checkPointSid);
				data.writeByte(ePoint.getAttackNums()+times);
				data.writeByte(times);
				for(int i=0;i<times;i++)
				{
					point.getIsland();// 初始化island
					point.fightSuccess(player,null,data,objectFactory,
						checkPointSid,false);
				}
				if(descpro>0)
				// 发送change消息
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.GEMS_ADD_SOMETHING,this,player,
						PublicConst.ARMS_SWEEP_COST_GEMS*descpro);

			}
			// 攻打精英战场
			else
			{
				String attract=ePoint.canChalleng(checkPointSid,1);
				if(attract!=null) throw new DataAccessException(0,attract);
				// 打关卡混编 可以加上城防的兵力
				int commanderNum=data.readUnsignedByte();// 统御书使用量
				commanderNum=0;// 暂不开放神统，消息处理完成后前台自己扣除统御书
				int length=data.readUnsignedByte();
				IntList list=new IntList();
				FleetGroup group=new FleetGroup();
				group.getOfficerFleetAttr().initOfficers(player);
				// 重新计算技能
				SeaBackKit.resetPlayerSkill(player,objectFactory);
				if(length==0)
					throw new DataAccessException(0,"you have no ship fight");
				String str=SeaBackKit.checkShipNumLimit(list,length,data,
					player,player.getIsland().getMainGroup(),commanderNum);
				if(str!=null)
				{
					throw new DataAccessException(0,str);
				}
				// 组建舰队
				creatFleetGroup(list,player,group,false);
				Object[] object=point.fight(group);
				FightScene scene=(FightScene)object[0];
				int successStar=0;
				data.clear();
				data.writeShort(point.getSid());
				// 胜利
				if(scene.getSuccessTeam()==0)
				{
					data.writeByte(ePoint.getAttackNums()+1);
					successStar=point.fightSuccess(player,group,data,
						objectFactory,checkPointSid,true);
				}
				else
				{
					data.writeByte(ePoint.getAttackNums());
					data.writeByte(successStar);
				}
				FightShowEventRecord record=(FightShowEventRecord)object[1];
				ByteBuffer fight=record.getRecord();
				SeaBackKit.conFightRecord(data,fight,player.getName(),player
					.getLevel(),point.getName(),point.getPointLevel(),point
					.getFightType(),player,null,group,point.getIsland()
					.getFleetGroup(),true,null,null);
			}
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ELITE_POINT_EVENT,null,player,null);
		}
		/**检查buff**/
		else if(type==PublicConst.CHECK_GEMS_BUFF)
		{
			int islandIndex=objectFactory.getIslandCache().getPlayerIsLandId(player.getId());
			Object[] object=FightKit.pushFightEvent(islandIndex,objectFactory);
			if(object!=null)
			{
				// 时间排序后的事件处理
				for(int i=0;i<object.length;i++)
				{
					FightEvent dataA=(FightEvent)object[i];
					if(checkBuff(dataA,objectFactory))
						JBackKit.sendFightEvent(player,dataA,objectFactory);
				}
			}
		}
		return data;
	}
	/** 组建舰队 */
	public boolean creatFleetGroup(IntList list,Player player,
		FleetGroup group,boolean reduce)
	{
		boolean resetMainGroup=false;
		player.getBundle().decrProp(PublicConst.COMMANDER_LEVEL_UP_SID,
			list.get(0));// 扣除统御书
		for(int i=1;i<list.size();i+=3)
		{
			int shipSid=list.get(i);
			int num=list.get(i+1);
			if(num<=0) continue;
			int location=list.get(i+2);
			if(reduce)
			{
				int reduceNum=player.getIsland().reduceShipBySid(shipSid,
					num,player.getIsland().getTroops());
				if(reduceNum<num)
				{
					resetMainGroup=true;
					// 扣除城防里面的
					reduceShips(player,shipSid,(num-reduceNum));
				}
			}
			Fleet fleet=new Fleet();
			fleet.setPlayter(player);
			fleet.initNum(num);
			fleet.setLocation(location);
			fleet.setShip((Ship)Ship.factory.newSample(shipSid));
			group.setFleet(location,fleet);
		}
		if(resetMainGroup) JBackKit.resetMainGroup(player);
		return true;
	}

	/** 扣除城防舰队指定sid船只 */
	public void reduceShips(Player player,int shipSid,int num)
	{
		FleetGroup group=player.getIsland().getMainGroup();
		Fleet fleet[]=group.getArray();
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null) continue;
			if(fleet[i].getShip().getSid()==shipSid&&fleet[i].getNum()>0)
			{
				int reduceNum=group.reduceShipByLocation(
					fleet[i].getLocation(),num);
				if(reduceNum>=num) return;
				reduceShips(player,shipSid,(num-reduceNum));
				break;
			}
		}
	}

	/** 获取盟军驻守事件的总数 */
	public int allianceNum(Player player)
	{
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null||fightEventList.size()<=0) return 0;
		int num=0;
		for(int i=0;i<fightEventList.size();i++)
		{
			if(fightEventList.get(i)==null) continue;
			FightEvent event=(FightEvent)fightEventList.get(i);
			if(event.getAttackIslandIndex()==islandId
				&&event.getType()==FightEvent.ATTACK_HOLD
				&&event.getEventState()!=FightEvent.RETRUN_BACK)
			{
				num++;
			}
		}
		return num;
	}

	/** 移除一个指定id的事件 */
	public void removeFightEventId(Player player,int eventId)
	{
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null||fightEventList.size()<=0) return;
		for(int i=0;i<fightEventList.size();i++)
		{
			if(fightEventList.get(i)==null) continue;
			FightEvent event=(FightEvent)fightEventList.get(i);
			if(event.getId()==eventId)
			{
				JBackKit.deleteFightEvent(player,event);
				fightEventList.remove(event);
				break;
			}
		}
	}

	/** 是否有联盟军队驻守 */
	public FightEvent getAllianceDefendEvent(Player player,int eventId)
	{
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null||fightEventList.size()<=0) return null;
		for(int i=0;i<fightEventList.size();i++)
		{
			if(fightEventList.get(i)==null) continue;
			FightEvent event=(FightEvent)fightEventList.get(i);
			if(event.getAttackIslandIndex()==islandId
				&&event.getEventState()==FightEvent.HOLD_ON)
			{
				if(event.getId()==eventId) return event;
			}
		}
		return null;
	}

	/** 扫荡关卡 **/
	public void sweepCheckPoint(Player player,ByteBuffer data,
		CheckPoint point,int checkPointSid)
	{
		int cEnergy=player.getIsland().gotEnergy(TimeKit.getSecondTime());// 精力
		int times=data.readUnsignedByte();
		if(cEnergy<times) throw new DataAccessException(0,"energy is 0");
		int lastStar=player.getSelfCheckPoint().getStar(
			point.getChapter()-1,point.getIndex());
		if(lastStar<SelfCheckPoint.THREE_STAR)
			throw new DataAccessException(0,"star not three");
		int pnum=player.getBundle().getCountBySid(PRO_LET);
		int descpro=times-pnum;
		if(descpro>0)
		{
			// 当前宝石数量是否足够
			if(!Resources.checkGems(
				PublicConst.ARMS_SWEEP_COST_GEMS*descpro,
				player.getResources()))
				throw new DataAccessException(0,"gems limit");
			// 扣除宝石
			Resources.reduceGems(PublicConst.ARMS_SWEEP_COST_GEMS*descpro,
				player.getResources(),player);
			// 日志记录
			objectFactory.createGemTrack(GemsTrack.SWEEP_ARMS_POINT,
				player.getId(),PublicConst.ARMS_SWEEP_COST_GEMS*descpro,0,
				Resources.getGems(player.getResources()));
			player.getBundle().decrProp(PRO_LET,pnum);
			if(pnum>0)
			{
				JBackKit.sendResetBunld(player);
			}
		}
		else
		{
			player.getBundle().decrProp(PRO_LET,times);
			JBackKit.sendResetBunld(player);
		}
		player.reduceEnergyN(times);
		data.clear();
		data.writeShort(checkPointSid);
		data.writeByte(times);
		for(int i=0;i<times;i++)
		{
			point.getIsland();// 初始化island
			point.fightSuccess(player,null,data,objectFactory,checkPointSid,
				false);
			// 攻击关卡的
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_TASK_EVENT,point.getIsland(),player,true);
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_POINT_TASK_EVENT,null,player,null);
		}
		if(descpro>0)
		// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				PublicConst.ARMS_SWEEP_COST_GEMS*descpro);

	}

	/** 初始化快速阵型 */
	public void initFormationGroups(Player player,ArrayList formations)
	{
		// 如果位置上没有布置，则使用空阵型填充
		IntList nullGroup=new IntList();
		// 初始化阵型长度
		int currentLen=1;
		if(player.getUser_state()<PublicConst.FORMATION_VIP[PublicConst.FORMATION_VIP.length-1])
		{
			for(int i=0;i<PublicConst.FORMATION_VIP.length;i++)
			{
				if(player.getUser_state()>PublicConst.FORMATION_VIP[i])
					continue;
				else if(player.getUser_state()<PublicConst.FORMATION_VIP[i])
					currentLen=i;
				else
					currentLen=i+1;
				break;
			}
		}
		else
			currentLen=PublicConst.FORMATION_VIP.length;
		while(currentLen>formations.size())
		{
			formations.add(nullGroup);
		}
	}

	/**检查宝石岛屿的buff**/
	public boolean checkBuff(FightEvent event,CreatObjectFactory factory)
	{
		if(event.getType()==FightEvent.ATTACK_HOLD)
		{
			NpcIsland island=objectFactory.getIslandByIndexOnly(event.getAttackIslandIndex()+"");
			if(island==null || island.getIslandType() != NpcIsland.ISLAND_GEMS) return false;
			return island.checkGemsBuff(TimeKit.getSecondTime(),factory);
		}
		return false;
	}
	
	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory 要设置的 objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
}
