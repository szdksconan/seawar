package foxu.sea.alliance.alliancebattle;

import mustang.back.BackKit;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.Sample;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.dcaccess.datasave.BattleIslandSave;
import foxu.fight.FightScene;
import foxu.sea.ContextVarManager;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;
import foxu.sea.port.ChatMessagePort;
import foxu.sea.shipdata.ShipCheckData;

/****
 * 联盟争夺岛屿战
 * 
 * @author lhj
 * 
 */
public class AllianceBattleFight implements TimerListener
{

	mustang.log.Logger log=mustang.log.LogFactory.getLogger(mustang.log.Logger.class);
	/** 基于数据库存储的表的对象 **/
	CreatObjectFactory factory;
	/** 每个一段时间进行检查 **/
	private static final int time=3000;
	/** 每个15分钟保存数据 **/
	private static final int savetime=15*60*1000;
	/** 每次发联盟战报的长度 **/
	public static final int ALLIANCE_REPORT_SIZE=1;
	/** 总的战报数量 **/
	public static final int MAX_ALLIANCE_MESSAGE=200;
	/** 阶段_时间 0：物资的结算时间 1：算的是当前联盟的竞标的计算时间 2：玩家报名时间 3:联盟战的开战时间 **/
	/** 基本时间 **/
	public static final int[] BASICE_TIME=new int[]{5*24*60*60,1*24*60*60,
		18*60*60,6*60*60};
	/**线程是否执行完成(联盟战阶段)**/
	public boolean CHECK_STAGE_STATE=true;
	/** 联盟战的间隔时间 **/
	public static final int ALLIANCE_FIGHT_TIME=60;
	/**占领的时间段的获取奖励记录加入**/
	public static final int  CAPTURE_ISLAND=60*60*2;
	/** 新联盟站的状态对象 **/
	Stage allianceStage=new Stage();
	/** 联盟邮件群体发送 **/
	ArrayList message1List=new ArrayList();

	ArrayList message2List=new ArrayList();

	public void init()
	{
		checkBattleIsland();
		/** 初始化状态对象 **/
		allianceStage.init();
		while(allianceStage.getEtime()<TimeKit.getSecondTime())
		{
			checkStage();
		}
		startTimer();
	}
	/** 启动定时器 */
	public void startTimer()
	{
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"alliancebattlefight",time));
		// 定时保存数据
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"alliancebattlesave",savetime));
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(!CHECK_STAGE_STATE) return;
		/** 检测阶段性 **/
		if(e.getParameter().equals("alliancebattlefight"))
		{
			CHECK_STAGE_STATE=false;
			checkStage();
			CHECK_STAGE_STATE=true;
		}
		/**数据保存**/
		else if(e.getParameter().equals("alliancebattlesave"))
		{
			if(!PublicConst.READY) allianceStage.saveAndExit();
		}
	}
	/** 检测阶段性 **/
	public void checkStage()
	{
		// 如果当前还没有执行完那么继续执行
		if(!allianceStage.isOver())
		{
			executeStage();
		}
		else
		{
			// 如果执行完成了检测阶段性
			checkStageTime();
		}
	}

	/**通过事件去检测阶段性**/
	public void checkStageTime()
	{
		if(allianceStage.getStage()==0)
		{
			initStage();
			return;
		}
		if(allianceStage.getEtime()>TimeKit.getSecondTime()) return;
		if(allianceStage.getStage()==Stage.STAGE_THREE) savePlayersLog();
		/**在联盟竞标的时候必须先增加阶段然后再发送邮件
		 * 能确保在这一瞬间其他玩家的竞标不会出错**/
		allianceStage.addStage();
		// 联盟竞标结束以后发送邮件
		if(allianceStage.getStage()==Stage.STAGE_THREE)
		{
			sortRankVlaue();
			sendEndBetMessage();
			saveBetLog();
		}
		allianceStage.setOver(false);
		allianceStage.setStime(allianceStage.getEtime());
		if(allianceStage.getStage()>4)
		{
			allianceStage.setStage(Stage.STAGE_ONE);
			int record=ContextVarManager.getInstance().getVarValue(
				ContextVarManager.ALLIANCE_FIGHT_RECORD);
			record++;
			ContextVarManager.getInstance().setVarValue(
				ContextVarManager.ALLIANCE_FIGHT_RECORD,record);
			//记录日志
			saveWeekStartTime(allianceStage.getStime());
		}
		// 分配资源
		if(allianceStage.getStage()==Stage.STAGE_ONE)
		{
			allianceStage.resetEtime(BASICE_TIME[0]);
			//默认当天已经领奖
			allianceStage.resetStime(CAPTURE_ISLAND);
		}
		// 竞标
		else if(allianceStage.getStage()==Stage.STAGE_TWO)
		{
			allianceStage.resetEtime(BASICE_TIME[1]);
		}
		// 玩家报名
		else if(allianceStage.getStage()==Stage.STAGE_THREE)
		{
			allianceStage.resetEtime(BASICE_TIME[2]);
		}
		// 联盟战
		else if(allianceStage.getStage()==Stage.STAGE_FOUR)
		{
			allianceStage.resetEtime(BASICE_TIME[3]);
			allianceStage.resetStime(ALLIANCE_FIGHT_TIME);
			JBackKit.sendAllianceBetInfo(null,factory);
			flushAllianceFightTime();
		}
		JBackKit.sendAllianceStageInfo(allianceStage,factory);
	}

	/** 初始化当前的状态 **/
	public void initStage()
	{
		int timeNow=TimeKit.getSecondTime();
		int weekEnd=SeaBackKit.getWeekEndSunTime();
		if(PublicConst.READY_SATURDAY)
		{
			weekEnd=SeaBackKit.getWeekEndTime();
		}
		int weekSart=weekEnd-PublicConst.DAY_SEC*7;
		int[] stateTime=new int[BASICE_TIME.length];
		// 设置联盟的时间
		for(int i=0;i<BASICE_TIME.length;i++)
		{
			if(i==0)
				stateTime[i]=weekSart+BASICE_TIME[i];
			else
				stateTime[i]=stateTime[i-1]+BASICE_TIME[i];
		}
		// 设置本周开始的默认时间
		allianceStage.setStime(weekSart);
		//保存时间
		saveWeekStartTime(weekSart);
		allianceStage.setStage(Stage.STAGE_ONE);
		allianceStage.setEtime(stateTime[0]);
		allianceStage.setOver(false);
		for(int i=0;i<stateTime.length;i++)
		{
			if(timeNow<stateTime[i]) break;
			allianceStage.addStage();
		}
		if(allianceStage.getStage()==Stage.STAGE_ONE)
		{
			while(allianceStage.getStime()+CAPTURE_ISLAND<timeNow)
			{
				allianceStage.resetStime(PublicConst.DAY_SEC);
				allianceStage.addResourceTimes();
			}
			allianceStage.setStime(timeNow);
			allianceStage.setEtime(stateTime[0]);
		}
		// 竞标时间
		else if(allianceStage.getStage()==Stage.STAGE_TWO)
		{
			allianceStage.resetStime(stateTime[0]);
			allianceStage.setEtime(stateTime[1]);
		}
		// 玩家报名
		else if(allianceStage.getStage()==Stage.STAGE_THREE)
		{
			allianceStage.resetStime(stateTime[1]);
			allianceStage.setEtime(stateTime[2]);
		}
		// 联盟战
		else if(allianceStage.getStage()==Stage.STAGE_FOUR)
		{
			allianceStage.setStime(stateTime[2]);
			allianceStage.setEtime(stateTime[3]);
		}
		log.error("------stage--change--init=============="+allianceStage.getStage());
	}

	/** 执行阶段 **/
	public void executeStage()
	{
		/** 联盟发资源 **/
		if(allianceStage.getStage()==Stage.STAGE_ONE)
		{
			int timeNow=TimeKit.getSecondTime();
			if(!SeaBackKit.isSameDay(allianceStage.getStime(),timeNow)
				&&allianceStage.getResourceTimes()<Stage.RESOURCE_MAX)
			{
				addAllianceSciencePoint();
				allianceStage.resetStime(PublicConst.DAY_SEC);
				allianceStage.addResourceTimes();
			}
			if(allianceStage.getResourceTimes()>=Stage.RESOURCE_MAX)
			{
				allianceStage.setResourceTimes(0);
				allianceStage.setOver(true);
				checkStageTime();
			}
		}
		/** 联盟竞标 **/
		else if(allianceStage.getStage()==Stage.STAGE_TWO)
		{
			/** 全服推送 **/
			clearAll();
			JBackKit.sendAllianceBetInfo(null,factory);
			// 发送全服消息
			sendAllServerMessage();
			// 联盟事件
			sendAllBetStart();
			// 阶段已经执行完成
			allianceStage.setOver(true);

		}
		/** 开始报名 **/
		else if(allianceStage.getStage()==Stage.STAGE_THREE)
		{
			allianceStage.setOver(true);
		}
		/** 开始联盟战 **/
		else if(allianceStage.getStage()==Stage.STAGE_FOUR)
		{
			if(allianceStage.getEtime()<TimeKit.getSecondTime())
			{
				allianceFight(MAX_ALLIANCE_MESSAGE);
				allianceStage.setOver(true);
				// 设置联盟站最后的赢家
				setBattleIslandWin();
				checkStageTime();
			}
			else if(allianceStage.getStime()<TimeKit.getSecondTime())
			{
				allianceFight(ALLIANCE_REPORT_SIZE);
				allianceStage.resetStime(ALLIANCE_REPORT_SIZE
					*ALLIANCE_FIGHT_TIME);
				//刷新当前争夺的时间
				flushAllianceFightTime();
			}
		}
	}

	/** 联盟对战开始 开战的次数 size **/
	public void allianceFight(int size)
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslands=getBattleIslands(true);
		if(bIlslands==null) return;
		for(int i=0;i<bIlslands.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslands[i])
				.getData();
			if(bIlsland==null||!bIlsland.isState()) continue;
			fightEntrance(bIlsland,size);
			// 最后才发邮件
			sendMessage();
		}
	}

	/** 战斗入口 **/
	public void fightEntrance(BattleIsland battleIlsland,int size)
	{
		int length=battleIlsland.getAlliancesBetLength();
		/** 说明当前只有一个联盟在竞争 **/
		if(length<=1)
		{
			battleIlsland.setState(false);
			// 刷新前台 告知只显示联盟团战的结束世界 同时产生联盟事件
			flushAllianceFightTime(battleIlsland,0);
			return;
		}
		// 开始联盟战
		fight(size,battleIlsland);
	}

	/** 开始对战 **/
	public void fight(int size,BattleIsland bIlsland)
	{
		int[] rankValue=bIlsland.getRankValue();
		Alliance a1;
		Alliance a2;
		IntList list1=null;
		IntList list2=null;
		ByteBuffer data=null;
		/** 根据发战报的长度来确定对战几回合 **/
		for(int i=0;i<size;i++)
		{
			data=new ByteBuffer();
			if(bIlsland.isAttack())
			{
				a1=factory.getAlliance(rankValue[0],true);
				a2=factory.getAlliance(rankValue[2],true);
				if(checkFightList(a1,a2,bIlsland)) return;
				list1=bIlsland.getFirstList();
				list2=bIlsland.getlastlist();
			}
			else
			{
				a1=factory.getAlliance(rankValue[2],true);
				a2=factory.getAlliance(rankValue[0],true);
				if(checkFightList(a1,a2,bIlsland)) return;
				list2=bIlsland.getFirstList();
				list1=bIlsland.getlastlist();
			}
			if(a1==null||a2==null) continue;
			allianceStage.addTimes();
			/** 每次对打的时候都获取第一个人 **/
			int pId1=list1.getFirst();
			int pId2=list2.getFirst();

			PlayerAllianceFight fight1=bIlsland.getPlayerFight(pId1);
			// 舰队1
			IntList playerShip1=fight1.getList();
			FleetGroup group1=createFleetGroup(playerShip1,pId1);

			PlayerAllianceFight fight2=bIlsland.getPlayerFight(pId2);
			// 舰队2
			IntList playerShip2=fight2.getList();
			FleetGroup group2=createFleetGroup(playerShip2,pId2);

			FightScene scene=null;
			FightShowEventRecord record=null;
			Player player1=factory.getPlayerCache().load(pId1+"");
			Player player2=factory.getPlayerCache().load(pId2+"");
			group1.setOfficerBattle(fight1.getOfficerBattle());
			group2.setOfficerBattle(fight2.getOfficerBattle());
			
			scene=FightSceneFactory.factory.create(group1,group2);
			record=FightSceneFactory.factory.fight(scene,null);
			/** 组合战报和前提信息 */
			SeaBackKit.conAllianceBattleFightRecord(data,record.getRecord(),
				player1.getName(),player1.getLevel(),player2.getName(),
				player2.getLevel(),PublicConst.FIGHT_TYPE_18,player1,
				player2,group1,group2,false);

			MessageKit.allianceFight(player1,player2,factory,data,group1,
				group2,scene.getSuccessTeam()==0,a1,a2,message1List,
				message2List);
			/** 如果是赢了的话 清除玩家的船只记录 同时重新设置下船只的数量 方便下次在重新创建一直舰队 **/
			// 重置船只记录
			fight1.setList(group1.getShipIntList());
			fight2.setList(group2.getShipIntList());

			if(scene.getSuccessTeam()==0)
			{
				fight1.setFightScore(SeaBackKit.getPlayerFightScroe(
					factory.getPlayerById(pId1),factory,fight1.getAllianceFightShip(),fight1.getOfficerBattle(),true));
				fight2.setFightScore(0);
				// 设置这个玩家出局
				fight2.setOut(true);
				JBackKit.sendRemovePlayer(factory,player2.getId(),
					a2.getId(),bIlsland.getSid(),player2.getName(),true);
			}
			else
			{
				fight2.setFightScore(SeaBackKit.getPlayerFightScroe(
					factory.getPlayerById(pId2),factory,fight2.getAllianceFightShip(),fight2.getOfficerBattle(),true));
				// 设置这个玩家出局
				fight1.setFightScore(0);
				fight1.setOut(true);
				JBackKit.sendRemovePlayer(factory,player1.getId(),
					a1.getId(),bIlsland.getSid(),player1.getName(),true);
			}
			addHurtShips(player1,group1,TimeKit.getSecondTime());
			addHurtShips(player2,group2,TimeKit.getSecondTime());
			/** 船只日志 */
			factory.addShipTrack(0,ShipCheckData.FIGHT_REST_SHIP,
						player1,fight1.getList(),null,false);
			/** 船只日志 */
			factory.addShipTrack(0,ShipCheckData.FIGHT_REST_SHIP,
						player2,fight2.getList(),null,false);
			list1.remove(pId1);
			list2.remove(pId2);
			JBackKit.sendResetPlayerShip(player1,bIlsland);
			JBackKit.sendResetPlayerShip(player2,bIlsland);
			// 设置下次出手的顺序
			bIlsland.setAttack(!bIlsland.isAttack());
			if(checkFightList(a1,a2,bIlsland)) return;
		}
	}

	/** 检测下是否需要更新战斗人员 **/
	public boolean checkFightList(Alliance a1,Alliance a2,
		BattleIsland bIlsland)
	{
		// 取联盟战斗的人
		IntList list1=bIlsland.getFirstList();
		IntList list2=bIlsland.getlastlist();
		// 清除集合然后在更新集合
		if(list1.size()==0||list2.size()==0)
		{
			// 清除
			list1.clear();
			list2.clear();
			// 战斗力重置排行
			bIlsland.fightRankList(bIlsland.getRankValue()[0],bIlsland.getFirstList());
			bIlsland.fightRankList(bIlsland.getRankValue()[2],bIlsland.getlastlist());

			return checkWinner(a1,a2,bIlsland);
		}
		return false;
	}

	/** 检测是是否需要联盟战 **/
	public boolean checkWinner(Alliance a1,Alliance a2,BattleIsland island)
	{
		if(island.checkNeedFight())
		{
			island.setState(false);
			flushAllianceFightTime(island,0);
			return true;
		}
		return false;
	}

	/** 发送系统邮件 **/
	public void sendSystemMessage(String allianceName,String islandName)
	{
		// 发送系统信息
		String messcontent=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"sysmessage_modify_name");
		messcontent=TextKit.replace(messcontent,"%",allianceName);
		messcontent=TextKit.replace(messcontent,"%",islandName);
		SeaBackKit.sendSystemMsg(factory.getDsmanager(),messcontent);
	}

	/** 获取所有联盟竞争岛屿 */
	public Object[] getBattleIslands(boolean bool)
	{
		return factory.getBattleIslandMemCache().loadBattleIslands(bool);
	}

	/** 通过Id获取的可以竞标岛屿 **/
	public BattleIsland getBattleIslandById(int id,boolean bool)
	{
		return (BattleIsland)factory.getBattleIslandMemCache().load(id,bool);
	}
	/** 构建一支队伍 **/
	public FleetGroup createFleetGroup(IntList list,int playerId)
	{
		FleetGroup group=new FleetGroup();
		for(int i=0;i<list.size();i+=3)
		{
			int location=list.get(i+2);
			int num=list.get(i+1);
			if(num<=0) continue;
			int shipSid=list.get(i);
			if(shipSid==0) continue;
			Fleet fleet=new Fleet();
			Player player=factory.getPlayerById(playerId);
			fleet.setPlayter(player);
			fleet.initNum(num);
			fleet.setLocation(location);
			fleet.setShip((Ship)Ship.factory.newSample(shipSid));
			fleet.setBuffEffect(false);
			group.setFleet(location,fleet);
		}
		return group;
	}

	/** 计算并添加伤兵 */
	public void addHurtShips(Player player,FleetGroup after,int time)
	{
		if(player==null) return;
		// 计算伤兵
		after.hurtTroops(player,time);
		// 计算玩伤兵并添加后 初始舰队数量设置为当前数量
		after.resetLastNum();
		// 刷新伤兵
		JBackKit.resetHurtTroops(player);
	}
	/** 清空要发的邮件集合 **/
	public void clearMessageList()
	{
		message1List.clear();
		message2List.clear();
	}

	/** 发送邮件 **/
	public void sendMessage()
	{
		sendMessagelist(message1List);
		sendMessagelist(message2List);
		// 发送联盟战报
		sendAllianceChat(message1List);
		sendAllianceChat(message2List);
		// 清空邮件集合
		clearMessageList();
	}

	/** 发战报 **/
	public void sendMessagelist(ArrayList array)
	{
		if(array.size()==0) return;
		Object[] messages=(Object[])array.getArray();
		Message message=(Message)messages[0];
		Alliance alliance=factory.getAlliance(message.getReceiveId(),false);
		JBackKit.sendAllianceReport(alliance.getPlayerList(),factory,
			messages);
	}
	/** 新的联盟站开启可清除一下以前联盟在的信息 **/
	public void clearAll()
	{
		allianceStage.clear();
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslands=getBattleIslands(true);
		if(bIlslands==null) return;
		for(int i=0;i<bIlslands.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslands[i])
				.getData();
			if(bIlsland==null) continue;
			bIlsland.clear(factory);
		}
		// 是否需要清除玩家身上的邮件
		factory.getMessageCache().clearAllianceMessage();
	}

	/** 发送全服公告 **/
	public void sendAllServerMessage()
	{
		// 发送系统信息
		String messcontent=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"new_alliance_fight_start");
		SeaBackKit.sendSystemMsg(factory.getDsmanager(),messcontent);
	}

	/** 添加联盟战胜利的联盟 每天添加科技点 **/
	public void addAllianceSciencePoint()
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslandsaves=getBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0) return;
		for(int i=0;i<bIlslandsaves.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
				.getData();
			bIlsland.addAllianceSciencePoint(factory);
		}
	}

	/** 系统分享联盟战的战报 **/
	public void sendAllianceChat(ArrayList messageList)
	{
		if(messageList.size()==0) return;
		Alliance alliance=factory.getAlliance(
			((Message)messageList.get(0)).getReceiveId(),false);
		if(alliance==null) return;
		for(int i=0;i<messageList.size();i++)
		{
			Message message=(Message)messageList.get(i);
			addAllianceChat(message,alliance);
		}
	}

	/** 添加一条联盟记录 **/
	public void addAllianceChat(Message message,Alliance alliance)
	{
		ChatMessage cmessage=new ChatMessage();
		cmessage.setMessageId(message.getMessageId());
		String src=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_battle_fight_report");
		cmessage.setSrc(src);
		// 发送系统信息
		String text=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"send_alliance_report");
		String[] title=message.getAllianceFightTitle();
		text=TextKit.replace(text,"%",title[0]+"("+title[1]+")");
		text=TextKit.replace(text,"%",title[2]+"("+title[3]+")");
		text+=" "+PublicConst.FIGHT_TYPE_18;
		cmessage.setText(text);
		cmessage.setType(ChatMessage.ALLIANCE_FIGHT_DATA);
		cmessage.setTime(TimeKit.getSecondTime());
		ChatMessagePort messport=(ChatMessagePort)BackKit.getContext().get(
			"chatMessagePort");
		messport.addAllianceFightData(cmessage,alliance);
	}

	/** 检测创建竞标岛屿 */
	public void checkBattleIsland()
	{
		Sample[] sample=BattleIsland.factory.getSamples();
		for(int i=1;i<sample.length;i++)
		{
			if(sample[i]==null) continue;
			int sid=sample[i].getSid();
			if(getBattleIslandById(sid,false)==null)
			{
				saveBIsland((BattleIsland)BattleIsland.factory
					.newSample(sid));
			}
		}
	}
	/** 保存竞标岛屿 */
	public void saveBIsland(BattleIsland bgIsland)
	{
		if(bgIsland==null) return;
		factory.getBattleIslandMemCache()
			.save(bgIsland.getSid()+"",bgIsland);
	}

	/** 设置岛屿获胜 **/
	public void setBattleIslandWin()
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslands=getBattleIslands(true);
		if(bIlslands==null) return;
		for(int i=0;i<bIlslands.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslands[i])
				.getData();
			if(bIlsland==null) continue;
			bIlsland.sendFightEndTask(factory);
		}
		JBackKit.sendAllianceBetInfo(null,factory);
	}

	/** 刷新某个岛屿前台联盟团战的结束时间 **/
	public void flushAllianceFightTime(BattleIsland battleIsland,int time)
	{
		int[] rankValue=battleIsland.getRankValue();
		for(int i=0;i<rankValue.length;i+=2)
		{
			Alliance alliance=factory.getAlliance(rankValue[i],false);
			if(alliance==null) continue;
			JBackKit.sendAllianceWarTime(alliance.getPlayerList(),time,factory,allianceStage.getTimes());
		}
	}
	/** 竞标结束以后发送邮件 同时设置岛屿需要进行联盟战**/
	public void sendEndBetMessage()
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslands=getBattleIslands(true);
		if(bIlslands==null) return;
		for(int i=0;i<bIlslands.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslands[i])
				.getData();
			if(bIlsland==null) continue;
			bIlsland.sendBetEndMessage(factory);
			bIlsland.setIslandState();
		}
	}
	/** 前台序列化 **/
	public void showByteWrite(ByteBuffer data,Alliance alliance,Player player)
	{
		allianceStage.showBytesWriteStage(data);
		showBytesWriteSignUp(data,alliance,player.getId());
		allianceStage.showByteWrite(data,
			getPlayerBattleIslandById(alliance.getBetBattleIsland()));
		showBytesBattleIsland(alliance,data);
		showBytesWriteShips(alliance,player.getId(),data);
		showBytesWriteAllianceReport(alliance,data);
	}

	/** 序列化岛屿信息 **/
	public void showBytesBattleIsland(Alliance alliance,ByteBuffer data)
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslandsaves=getBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0)
			data.writeShort(0);
		else
		{
			data.writeShort(bIlslandsaves.length);
			for(int i=0;i<bIlslandsaves.length;i++)
			{
				BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
					.getData();
				bIlsland.showBytesBattleInfo(data,factory);
			}
		}
	}
	/** 序列化报名状态 **/
	public void showBytesWriteSignUp(ByteBuffer data,Alliance alliance,
		int playerId)
	{
		if(allianceStage.getStage()<Stage.STAGE_THREE
			||alliance.getBetBattleIsland()==0)
		{
			// 是否竞标成功
			data.writeBoolean(false);
			data.writeBoolean(false);
			return;
		}
		BattleIsland battleIsland=getBattleIslandById(alliance
			.getBetBattleIsland(),false);
		if(battleIsland==null)
		{
			// 是否竞标成功
			data.writeBoolean(false);
			data.writeBoolean(false);
			return;
		}
		// 是否竞标成功
		data.writeBoolean(true);
		data.writeBoolean(battleIsland.isHavePlayer(playerId));
	}

	/** 玩家报名船只数据 **/
	public void showBytesWriteShips(Alliance alliance,int playerId,
		ByteBuffer data)
	{
		if(allianceStage.getStage()<Stage.STAGE_THREE
			||alliance.getBetBattleIsland()==0) return;
		BattleIsland battleIsland=getBattleIslandById(alliance
			.getBetBattleIsland(),false);
		if(battleIsland==null) return;
		// 玩家的报名状态
		if(battleIsland.isHavePlayer(playerId))
			battleIsland.showBytesWriteShips(data,playerId);
	}

	/** 序列化联盟战报 **/
	public void showBytesWriteAllianceReport(Alliance alliance,
		ByteBuffer data)
	{
		ArrayList messageList=factory.getMessageCache()
			.getAllianceFightMessage(alliance.getId());
		if(messageList==null||messageList.size()==0)
		{
			data.writeShort(0);
			return;
		}
		int top=data.top();
		int length=0;
		Object[] messages=(Object[])messageList.toArray();
		data.writeShort(0);
		for(int i=0;i<messages.length;i++)
		{
			if(messages[i]==null) continue;
			Message message=(Message)messages[i];
			message.showBytesWrite(data,message.getRecive_state(),
				message.getContent(),null);
			length++;
		}
		if(length>0)
		{
			int nowTop=data.top();
			data.setTop(top);
			data.writeShort(length);
			data.setTop(nowTop);
		}
	}

	/** 获取当前玩家的岛屿联盟战 战斗是否结束 **/
	public boolean getPlayerBattleIslandById(int id)
	{
		if(allianceStage.getStage()!=Stage.STAGE_FOUR) return false;
		if(id==0) return false;
		BattleIsland battleIsland=getBattleIslandById(id,false);
		if(battleIsland==null) return false;
		return battleIsland.isState();
	}

	public Stage getAllianceStage()
	{
		return allianceStage;
	}

	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	/** 联盟战开始之前给竞标胜利的人进行排序 **/
	public void sortRankVlaue()
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslandsaves=getBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0) return;
		for(int i=0;i<bIlslandsaves.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
				.getData();
			bIlsland.sortRankValue();
		}
	}

	/** 新一轮竞标生成联盟事件 **/
	public void sendAllBetStart()
	{
		Object[] objects=factory.getAllianceMemCache().getCacheMap()
			.valueArray();
		for(int i=0;i<objects.length;i++)
		{
			if(objects[i]==null) continue;
			AllianceSave allianceSave=(AllianceSave)objects[i];
			Alliance alliance =allianceSave.getData();
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_BET_START,"","","",
				TimeKit.getSecondTime());
			alliance.addEvent(event);
		}
	}	
	
	/**刷新全部岛屿的更新时间**/
	public void flushAllianceFightTime()
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslandsaves=getBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0) return;
		for(int i=0;i<bIlslandsaves.length;i++)
		{
			BattleIsland battleIsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
				.getData();
			if(battleIsland.isState())
			{
				flushAllianceFightTime(battleIsland,allianceStage.getStime());
			}
		}
	}
	
	/**保存每周的开始时间和结束时间**/
	public  void saveWeekStartTime(int stime)
	{
		factory.createAllianceFightRecordTrack(0,0,"","",Stage.STAGE_ONE,0,
			AllianceFightRecordTrack.SAVE_WEEK_TIME,stime,stime+7
				*PublicConst.DAY_SEC);
	}
	/**刷新全部岛屿的更新时间**/
	public void savePlayersLog()
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslandsaves=getBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0) return;
		for(int i=0;i<bIlslandsaves.length;i++)
		{
			BattleIsland battleIsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
				.getData();
			String players=battleIsland.recordPlayersLog();
			factory.createAllianceFightRecordTrack(0,0,players,"",Stage.STAGE_THREE,battleIsland.getSid(),
				AllianceFightRecordTrack.SAVE_PLAYER_INFO,0,0);
		}
	}
	/**保存竞标信息**/
	public void saveBetLog()
	{
		/** 获取所有的竞争岛屿 **/
		Object[] bIlslandsaves=getBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0) return;
		for(int i=0;i<bIlslandsaves.length;i++)
		{
			BattleIsland battleIsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
				.getData();
			battleIsland.saveBetLogTime(factory);
		}
	}
}
