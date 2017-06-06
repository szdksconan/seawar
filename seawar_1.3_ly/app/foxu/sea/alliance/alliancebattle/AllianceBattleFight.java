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
 * �������ᵺ��ս
 * 
 * @author lhj
 * 
 */
public class AllianceBattleFight implements TimerListener
{

	mustang.log.Logger log=mustang.log.LogFactory.getLogger(mustang.log.Logger.class);
	/** �������ݿ�洢�ı�Ķ��� **/
	CreatObjectFactory factory;
	/** ÿ��һ��ʱ����м�� **/
	private static final int time=3000;
	/** ÿ��15���ӱ������� **/
	private static final int savetime=15*60*1000;
	/** ÿ�η�����ս���ĳ��� **/
	public static final int ALLIANCE_REPORT_SIZE=1;
	/** �ܵ�ս������ **/
	public static final int MAX_ALLIANCE_MESSAGE=200;
	/** �׶�_ʱ�� 0�����ʵĽ���ʱ�� 1������ǵ�ǰ���˵ľ���ļ���ʱ�� 2����ұ���ʱ�� 3:����ս�Ŀ�սʱ�� **/
	/** ����ʱ�� **/
	public static final int[] BASICE_TIME=new int[]{5*24*60*60,1*24*60*60,
		18*60*60,6*60*60};
	/**�߳��Ƿ�ִ�����(����ս�׶�)**/
	public boolean CHECK_STAGE_STATE=true;
	/** ����ս�ļ��ʱ�� **/
	public static final int ALLIANCE_FIGHT_TIME=60;
	/**ռ���ʱ��εĻ�ȡ������¼����**/
	public static final int  CAPTURE_ISLAND=60*60*2;
	/** ������վ��״̬���� **/
	Stage allianceStage=new Stage();
	/** �����ʼ�Ⱥ�巢�� **/
	ArrayList message1List=new ArrayList();

	ArrayList message2List=new ArrayList();

	public void init()
	{
		checkBattleIsland();
		/** ��ʼ��״̬���� **/
		allianceStage.init();
		while(allianceStage.getEtime()<TimeKit.getSecondTime())
		{
			checkStage();
		}
		startTimer();
	}
	/** ������ʱ�� */
	public void startTimer()
	{
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"alliancebattlefight",time));
		// ��ʱ��������
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"alliancebattlesave",savetime));
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(!CHECK_STAGE_STATE) return;
		/** ���׶��� **/
		if(e.getParameter().equals("alliancebattlefight"))
		{
			CHECK_STAGE_STATE=false;
			checkStage();
			CHECK_STAGE_STATE=true;
		}
		/**���ݱ���**/
		else if(e.getParameter().equals("alliancebattlesave"))
		{
			if(!PublicConst.READY) allianceStage.saveAndExit();
		}
	}
	/** ���׶��� **/
	public void checkStage()
	{
		// �����ǰ��û��ִ������ô����ִ��
		if(!allianceStage.isOver())
		{
			executeStage();
		}
		else
		{
			// ���ִ������˼��׶���
			checkStageTime();
		}
	}

	/**ͨ���¼�ȥ���׶���**/
	public void checkStageTime()
	{
		if(allianceStage.getStage()==0)
		{
			initStage();
			return;
		}
		if(allianceStage.getEtime()>TimeKit.getSecondTime()) return;
		if(allianceStage.getStage()==Stage.STAGE_THREE) savePlayersLog();
		/**�����˾����ʱ����������ӽ׶�Ȼ���ٷ����ʼ�
		 * ��ȷ������һ˲��������ҵľ��겻�����**/
		allianceStage.addStage();
		// ���˾�������Ժ����ʼ�
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
			//��¼��־
			saveWeekStartTime(allianceStage.getStime());
		}
		// ������Դ
		if(allianceStage.getStage()==Stage.STAGE_ONE)
		{
			allianceStage.resetEtime(BASICE_TIME[0]);
			//Ĭ�ϵ����Ѿ��콱
			allianceStage.resetStime(CAPTURE_ISLAND);
		}
		// ����
		else if(allianceStage.getStage()==Stage.STAGE_TWO)
		{
			allianceStage.resetEtime(BASICE_TIME[1]);
		}
		// ��ұ���
		else if(allianceStage.getStage()==Stage.STAGE_THREE)
		{
			allianceStage.resetEtime(BASICE_TIME[2]);
		}
		// ����ս
		else if(allianceStage.getStage()==Stage.STAGE_FOUR)
		{
			allianceStage.resetEtime(BASICE_TIME[3]);
			allianceStage.resetStime(ALLIANCE_FIGHT_TIME);
			JBackKit.sendAllianceBetInfo(null,factory);
			flushAllianceFightTime();
		}
		JBackKit.sendAllianceStageInfo(allianceStage,factory);
	}

	/** ��ʼ����ǰ��״̬ **/
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
		// �������˵�ʱ��
		for(int i=0;i<BASICE_TIME.length;i++)
		{
			if(i==0)
				stateTime[i]=weekSart+BASICE_TIME[i];
			else
				stateTime[i]=stateTime[i-1]+BASICE_TIME[i];
		}
		// ���ñ��ܿ�ʼ��Ĭ��ʱ��
		allianceStage.setStime(weekSart);
		//����ʱ��
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
		// ����ʱ��
		else if(allianceStage.getStage()==Stage.STAGE_TWO)
		{
			allianceStage.resetStime(stateTime[0]);
			allianceStage.setEtime(stateTime[1]);
		}
		// ��ұ���
		else if(allianceStage.getStage()==Stage.STAGE_THREE)
		{
			allianceStage.resetStime(stateTime[1]);
			allianceStage.setEtime(stateTime[2]);
		}
		// ����ս
		else if(allianceStage.getStage()==Stage.STAGE_FOUR)
		{
			allianceStage.setStime(stateTime[2]);
			allianceStage.setEtime(stateTime[3]);
		}
		log.error("------stage--change--init=============="+allianceStage.getStage());
	}

	/** ִ�н׶� **/
	public void executeStage()
	{
		/** ���˷���Դ **/
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
		/** ���˾��� **/
		else if(allianceStage.getStage()==Stage.STAGE_TWO)
		{
			/** ȫ������ **/
			clearAll();
			JBackKit.sendAllianceBetInfo(null,factory);
			// ����ȫ����Ϣ
			sendAllServerMessage();
			// �����¼�
			sendAllBetStart();
			// �׶��Ѿ�ִ�����
			allianceStage.setOver(true);

		}
		/** ��ʼ���� **/
		else if(allianceStage.getStage()==Stage.STAGE_THREE)
		{
			allianceStage.setOver(true);
		}
		/** ��ʼ����ս **/
		else if(allianceStage.getStage()==Stage.STAGE_FOUR)
		{
			if(allianceStage.getEtime()<TimeKit.getSecondTime())
			{
				allianceFight(MAX_ALLIANCE_MESSAGE);
				allianceStage.setOver(true);
				// ��������վ����Ӯ��
				setBattleIslandWin();
				checkStageTime();
			}
			else if(allianceStage.getStime()<TimeKit.getSecondTime())
			{
				allianceFight(ALLIANCE_REPORT_SIZE);
				allianceStage.resetStime(ALLIANCE_REPORT_SIZE
					*ALLIANCE_FIGHT_TIME);
				//ˢ�µ�ǰ�����ʱ��
				flushAllianceFightTime();
			}
		}
	}

	/** ���˶�ս��ʼ ��ս�Ĵ��� size **/
	public void allianceFight(int size)
	{
		/** ��ȡ���еľ������� **/
		Object[] bIlslands=getBattleIslands(true);
		if(bIlslands==null) return;
		for(int i=0;i<bIlslands.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslands[i])
				.getData();
			if(bIlsland==null||!bIlsland.isState()) continue;
			fightEntrance(bIlsland,size);
			// ���ŷ��ʼ�
			sendMessage();
		}
	}

	/** ս����� **/
	public void fightEntrance(BattleIsland battleIlsland,int size)
	{
		int length=battleIlsland.getAlliancesBetLength();
		/** ˵����ǰֻ��һ�������ھ��� **/
		if(length<=1)
		{
			battleIlsland.setState(false);
			// ˢ��ǰ̨ ��ֻ֪��ʾ������ս�Ľ������� ͬʱ���������¼�
			flushAllianceFightTime(battleIlsland,0);
			return;
		}
		// ��ʼ����ս
		fight(size,battleIlsland);
	}

	/** ��ʼ��ս **/
	public void fight(int size,BattleIsland bIlsland)
	{
		int[] rankValue=bIlsland.getRankValue();
		Alliance a1;
		Alliance a2;
		IntList list1=null;
		IntList list2=null;
		ByteBuffer data=null;
		/** ���ݷ�ս���ĳ�����ȷ����ս���غ� **/
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
			/** ÿ�ζԴ��ʱ�򶼻�ȡ��һ���� **/
			int pId1=list1.getFirst();
			int pId2=list2.getFirst();

			PlayerAllianceFight fight1=bIlsland.getPlayerFight(pId1);
			// ����1
			IntList playerShip1=fight1.getList();
			FleetGroup group1=createFleetGroup(playerShip1,pId1);

			PlayerAllianceFight fight2=bIlsland.getPlayerFight(pId2);
			// ����2
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
			/** ���ս����ǰ����Ϣ */
			SeaBackKit.conAllianceBattleFightRecord(data,record.getRecord(),
				player1.getName(),player1.getLevel(),player2.getName(),
				player2.getLevel(),PublicConst.FIGHT_TYPE_18,player1,
				player2,group1,group2,false);

			MessageKit.allianceFight(player1,player2,factory,data,group1,
				group2,scene.getSuccessTeam()==0,a1,a2,message1List,
				message2List);
			/** �����Ӯ�˵Ļ� �����ҵĴ�ֻ��¼ ͬʱ���������´�ֻ������ �����´������´���һֱ���� **/
			// ���ô�ֻ��¼
			fight1.setList(group1.getShipIntList());
			fight2.setList(group2.getShipIntList());

			if(scene.getSuccessTeam()==0)
			{
				fight1.setFightScore(SeaBackKit.getPlayerFightScroe(
					factory.getPlayerById(pId1),factory,fight1.getAllianceFightShip(),fight1.getOfficerBattle(),true));
				fight2.setFightScore(0);
				// ���������ҳ���
				fight2.setOut(true);
				JBackKit.sendRemovePlayer(factory,player2.getId(),
					a2.getId(),bIlsland.getSid(),player2.getName(),true);
			}
			else
			{
				fight2.setFightScore(SeaBackKit.getPlayerFightScroe(
					factory.getPlayerById(pId2),factory,fight2.getAllianceFightShip(),fight2.getOfficerBattle(),true));
				// ���������ҳ���
				fight1.setFightScore(0);
				fight1.setOut(true);
				JBackKit.sendRemovePlayer(factory,player1.getId(),
					a1.getId(),bIlsland.getSid(),player1.getName(),true);
			}
			addHurtShips(player1,group1,TimeKit.getSecondTime());
			addHurtShips(player2,group2,TimeKit.getSecondTime());
			/** ��ֻ��־ */
			factory.addShipTrack(0,ShipCheckData.FIGHT_REST_SHIP,
						player1,fight1.getList(),null,false);
			/** ��ֻ��־ */
			factory.addShipTrack(0,ShipCheckData.FIGHT_REST_SHIP,
						player2,fight2.getList(),null,false);
			list1.remove(pId1);
			list2.remove(pId2);
			JBackKit.sendResetPlayerShip(player1,bIlsland);
			JBackKit.sendResetPlayerShip(player2,bIlsland);
			// �����´γ��ֵ�˳��
			bIlsland.setAttack(!bIlsland.isAttack());
			if(checkFightList(a1,a2,bIlsland)) return;
		}
	}

	/** ������Ƿ���Ҫ����ս����Ա **/
	public boolean checkFightList(Alliance a1,Alliance a2,
		BattleIsland bIlsland)
	{
		// ȡ����ս������
		IntList list1=bIlsland.getFirstList();
		IntList list2=bIlsland.getlastlist();
		// �������Ȼ���ڸ��¼���
		if(list1.size()==0||list2.size()==0)
		{
			// ���
			list1.clear();
			list2.clear();
			// ս������������
			bIlsland.fightRankList(bIlsland.getRankValue()[0],bIlsland.getFirstList());
			bIlsland.fightRankList(bIlsland.getRankValue()[2],bIlsland.getlastlist());

			return checkWinner(a1,a2,bIlsland);
		}
		return false;
	}

	/** ������Ƿ���Ҫ����ս **/
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

	/** ����ϵͳ�ʼ� **/
	public void sendSystemMessage(String allianceName,String islandName)
	{
		// ����ϵͳ��Ϣ
		String messcontent=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"sysmessage_modify_name");
		messcontent=TextKit.replace(messcontent,"%",allianceName);
		messcontent=TextKit.replace(messcontent,"%",islandName);
		SeaBackKit.sendSystemMsg(factory.getDsmanager(),messcontent);
	}

	/** ��ȡ�������˾������� */
	public Object[] getBattleIslands(boolean bool)
	{
		return factory.getBattleIslandMemCache().loadBattleIslands(bool);
	}

	/** ͨ��Id��ȡ�Ŀ��Ծ��굺�� **/
	public BattleIsland getBattleIslandById(int id,boolean bool)
	{
		return (BattleIsland)factory.getBattleIslandMemCache().load(id,bool);
	}
	/** ����һ֧���� **/
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

	/** ���㲢����˱� */
	public void addHurtShips(Player player,FleetGroup after,int time)
	{
		if(player==null) return;
		// �����˱�
		after.hurtTroops(player,time);
		// �������˱�����Ӻ� ��ʼ������������Ϊ��ǰ����
		after.resetLastNum();
		// ˢ���˱�
		JBackKit.resetHurtTroops(player);
	}
	/** ���Ҫ�����ʼ����� **/
	public void clearMessageList()
	{
		message1List.clear();
		message2List.clear();
	}

	/** �����ʼ� **/
	public void sendMessage()
	{
		sendMessagelist(message1List);
		sendMessagelist(message2List);
		// ��������ս��
		sendAllianceChat(message1List);
		sendAllianceChat(message2List);
		// ����ʼ�����
		clearMessageList();
	}

	/** ��ս�� **/
	public void sendMessagelist(ArrayList array)
	{
		if(array.size()==0) return;
		Object[] messages=(Object[])array.getArray();
		Message message=(Message)messages[0];
		Alliance alliance=factory.getAlliance(message.getReceiveId(),false);
		JBackKit.sendAllianceReport(alliance.getPlayerList(),factory,
			messages);
	}
	/** �µ�����վ���������һ����ǰ�����ڵ���Ϣ **/
	public void clearAll()
	{
		allianceStage.clear();
		/** ��ȡ���еľ������� **/
		Object[] bIlslands=getBattleIslands(true);
		if(bIlslands==null) return;
		for(int i=0;i<bIlslands.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslands[i])
				.getData();
			if(bIlsland==null) continue;
			bIlsland.clear(factory);
		}
		// �Ƿ���Ҫ���������ϵ��ʼ�
		factory.getMessageCache().clearAllianceMessage();
	}

	/** ����ȫ������ **/
	public void sendAllServerMessage()
	{
		// ����ϵͳ��Ϣ
		String messcontent=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"new_alliance_fight_start");
		SeaBackKit.sendSystemMsg(factory.getDsmanager(),messcontent);
	}

	/** �������սʤ�������� ÿ����ӿƼ��� **/
	public void addAllianceSciencePoint()
	{
		/** ��ȡ���еľ������� **/
		Object[] bIlslandsaves=getBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0) return;
		for(int i=0;i<bIlslandsaves.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
				.getData();
			bIlsland.addAllianceSciencePoint(factory);
		}
	}

	/** ϵͳ��������ս��ս�� **/
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

	/** ���һ�����˼�¼ **/
	public void addAllianceChat(Message message,Alliance alliance)
	{
		ChatMessage cmessage=new ChatMessage();
		cmessage.setMessageId(message.getMessageId());
		String src=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_battle_fight_report");
		cmessage.setSrc(src);
		// ����ϵͳ��Ϣ
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

	/** ��ⴴ�����굺�� */
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
	/** ���澺�굺�� */
	public void saveBIsland(BattleIsland bgIsland)
	{
		if(bgIsland==null) return;
		factory.getBattleIslandMemCache()
			.save(bgIsland.getSid()+"",bgIsland);
	}

	/** ���õ����ʤ **/
	public void setBattleIslandWin()
	{
		/** ��ȡ���еľ������� **/
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

	/** ˢ��ĳ������ǰ̨������ս�Ľ���ʱ�� **/
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
	/** ��������Ժ����ʼ� ͬʱ���õ�����Ҫ��������ս**/
	public void sendEndBetMessage()
	{
		/** ��ȡ���еľ������� **/
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
	/** ǰ̨���л� **/
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

	/** ���л�������Ϣ **/
	public void showBytesBattleIsland(Alliance alliance,ByteBuffer data)
	{
		/** ��ȡ���еľ������� **/
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
	/** ���л�����״̬ **/
	public void showBytesWriteSignUp(ByteBuffer data,Alliance alliance,
		int playerId)
	{
		if(allianceStage.getStage()<Stage.STAGE_THREE
			||alliance.getBetBattleIsland()==0)
		{
			// �Ƿ񾺱�ɹ�
			data.writeBoolean(false);
			data.writeBoolean(false);
			return;
		}
		BattleIsland battleIsland=getBattleIslandById(alliance
			.getBetBattleIsland(),false);
		if(battleIsland==null)
		{
			// �Ƿ񾺱�ɹ�
			data.writeBoolean(false);
			data.writeBoolean(false);
			return;
		}
		// �Ƿ񾺱�ɹ�
		data.writeBoolean(true);
		data.writeBoolean(battleIsland.isHavePlayer(playerId));
	}

	/** ��ұ�����ֻ���� **/
	public void showBytesWriteShips(Alliance alliance,int playerId,
		ByteBuffer data)
	{
		if(allianceStage.getStage()<Stage.STAGE_THREE
			||alliance.getBetBattleIsland()==0) return;
		BattleIsland battleIsland=getBattleIslandById(alliance
			.getBetBattleIsland(),false);
		if(battleIsland==null) return;
		// ��ҵı���״̬
		if(battleIsland.isHavePlayer(playerId))
			battleIsland.showBytesWriteShips(data,playerId);
	}

	/** ���л�����ս�� **/
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

	/** ��ȡ��ǰ��ҵĵ�������ս ս���Ƿ���� **/
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

	/** ����ս��ʼ֮ǰ������ʤ�����˽������� **/
	public void sortRankVlaue()
	{
		/** ��ȡ���еľ������� **/
		Object[] bIlslandsaves=getBattleIslands(false);
		if(bIlslandsaves==null||bIlslandsaves.length==0) return;
		for(int i=0;i<bIlslandsaves.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)bIlslandsaves[i])
				.getData();
			bIlsland.sortRankValue();
		}
	}

	/** ��һ�־������������¼� **/
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
	
	/**ˢ��ȫ������ĸ���ʱ��**/
	public void flushAllianceFightTime()
	{
		/** ��ȡ���еľ������� **/
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
	
	/**����ÿ�ܵĿ�ʼʱ��ͽ���ʱ��**/
	public  void saveWeekStartTime(int stime)
	{
		factory.createAllianceFightRecordTrack(0,0,"","",Stage.STAGE_ONE,0,
			AllianceFightRecordTrack.SAVE_WEEK_TIME,stime,stime+7
				*PublicConst.DAY_SEC);
	}
	/**ˢ��ȫ������ĸ���ʱ��**/
	public void savePlayersLog()
	{
		/** ��ȡ���еľ������� **/
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
	/**���澺����Ϣ**/
	public void saveBetLog()
	{
		/** ��ȡ���еľ������� **/
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
