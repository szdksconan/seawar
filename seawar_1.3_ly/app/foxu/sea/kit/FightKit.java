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

/** ս����� */
public class FightKit
{

	/** ���˷����Ӧ�ĸ��˷��� */
	public static final int BOSS_SERVICE_SID[]={201,1,202,2,203,3,204,4,205,
		5};

	/** ��ˮ��sid */
	public static final int WATER_ISLAND_SID=11501;

	/** һ�����ڵ����� */
	public static final int MONTH_SECOND=86400*7;

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(MessageGameDBAccess.class);
	
	private static final Object lock=new Object();

	/** �Ƿ����ս���¼����� */
	public static String checkUpFightEvent(Player player,int fightEventId,
		CreatObjectFactory objectFactory)
	{
		FightEvent event=(FightEvent)objectFactory.getEventCache().loadOnly(
			fightEventId+"");
		if(event==null) return "event is null";
		// �Ƿ������Լ�
		if(event.getPlayerId()!=player.getId()) return "event is not your";
		// פ��״̬
		if(event.getEventState()==FightEvent.HOLD_ON)
			return "event is hold";
		// ����Ѿ�ɾ��״̬�������
		if(event.getDelete()==FightEvent.DELETE_TYPE)
			return "event is delete";
		// ���㱦ʯ�Ƿ��㹻
		int needGems=SeaBackKit.getGemsForTime((event.getCreatAt()+event
			.getNeedTime())
			-TimeKit.getSecondTime());
		if(needGems==0) return "event is finished";
		if(!Resources.checkGems(needGems,player.getResources()))
			return "not enough gems";
		// // ����boss�Ĳ��ܼ���
		// NpcIsland island=objectFactory.getIslandByIndexOnly(event
		// .getAttackIslandIndex()
		// +"");
		// if(island.getIslandType()==NpcIsland.WORLD_BOSS)
		// return "worldboss can not use";
		return null;
	}

	/** �¼����� */
	public static int fightEventUp(Player player,int fightEventId,
		CreatObjectFactory objectFactory)
	{
		FightEvent event=(FightEvent)objectFactory.getEventCache().loadOnly(
			fightEventId+"");
		int time=TimeKit.getSecondTime();
		// ���㱦ʯ�Ƿ��㹻
		int needGems=SeaBackKit.getGemsForTime((event.getCreatAt()+event
			.getNeedTime())
			-time);
		// �۳���ʯ
		Resources.reduceGems(needGems,player.getResources(),player);
		// ��ʯ���Ѽ�¼
		objectFactory.createGemTrack(GemsTrack.FIGHT_EVENT_UP,
			player.getId(),needGems,event.getId(),
			Resources.getGems(player.getResources()));
		// �ı��¼�����(ʱ��)֮ǰ�����¼�
		// ����¼�needTime�պ�����Ϊ0���ұ�������Ҹպý��е�ǰ�¼����
		// ��������������checkFightEvent��ͬ����(˫����ͨ�����¼�attack״̬���)
		// ������»����ͬһ�¼����ι�����bug
		synchronized(event)
		{
			event.setCreatAt(time);
			// �¼�
			event.setNeedTimeDB(0);
			FightKit.checkFightEvent(event,player,objectFactory);
		}
		return needGems;
	}

	/** �����ĳ����ҵ�����ص�ս���¼� */
	public static Object[] pushFightEvent(int islandIndex,
		CreatObjectFactory objectFactory)
	{
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandIndex);
		if(fightEventList==null) return null;
		// ����ʱ������
		Object events[]=fightEventList.toArray();
		SetKit.sort(events,FightEventComparator.getInstance());
		return events;
	}

	/** ս���¼��Ĵ��� */
	public static void checkFightEvent(Object events[],
		CreatObjectFactory objectFactory,Player player)
	{
		// ʱ���������¼�����
		for(int i=0;i<events.length;i++)
		{
			FightEvent dataA=(FightEvent)events[i];
			synchronized(dataA)
			{
				if(dataA.getEventState()==FightEvent.HOLD_ON) continue;
				checkFightEvent(dataA,player,objectFactory);
				// ����ı��б� �ȴ�����
				objectFactory.getEventCache().load(dataA.getId()+"");
			}
		}
	}

	/** ս���¼��Ĵ���(�����ڰ�Ǩ����ʱ����¼�) */
	public static void checkFightEvent(Object events[],
		CreatObjectFactory objectFactory)
	{
		int checkTime=TimeKit.getSecondTime();
		// ʱ���������¼�����
		for(int i=0;i<events.length;i++)
		{
			FightEvent dataA=(FightEvent)events[i];
			Player player=objectFactory.getPlayerById(dataA.getPlayerId());
			synchronized(dataA)
			{
				// ��פ���¼���������
				if(dataA.getEventState()==FightEvent.HOLD_ON) 
				{
					dataA.setCreatAt(checkTime);
					dataA.setEventState(FightEvent.RETRUN_BACK);
					// �ҵ���ǰ��ҵĵ���
					NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
						player.getId());
					// ����ʱ��
					int needTime=needTime(island.getIndex(),dataA.getAttackIslandIndex());
					dataA.setNeedTime(needTime,player,checkTime);
				}
				else
					checkFightEvent(dataA,player,objectFactory);
				// ����ı��б� �ȴ�����
				objectFactory.getEventCache().load(dataA.getId()+"");
			}
		}
	}
	
	/** ����bossս�� */
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
			// �Ƴ�boss����
			beIsland.updateSid(WATER_ISLAND_SID);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			// boss����ɱ
			boss.bekilled();
			// ˢ���µĵȼ�
			objectFactory.getWorldBossCache().addBossOnKill(boss);
			player.setAttribute(PublicConst.ATTACK_BOSS_TIME,
				TimeKit.getSecondTime()+"");
			return;
		}
		int checkTime=event.getCreatAt()+event.getNeedTime();
		int beAttackLevel=0;
		// ���¼�������������
		SeaBackKit.resetPlayerSkill(player,objectFactory);
		// ��boss
		Object[] object=boss.fight(event.getFleetGroup());
		// beAttackName=boss.getName();
		beAttackLevel=boss.getBossLevel();
		FightScene scene=(FightScene)object[0];
		FightShowEventRecord r=(FightShowEventRecord)object[1];
		// ս���ݲ�����
		ByteBuffer data=new ByteBuffer();
		SeaBackKit.conFightRecord(data,r.getRecord(),player.getName(),
			player.getLevel(),beIsland.getName(),beAttackLevel,
			PublicConst.FIGHT_TYPE_13,player,null,event.getFleetGroup(),
			boss.getFleetGroup(),false,null,null);
		event.setCreatAt(checkTime);
		event.setEventState(FightEvent.RETRUN_BACK);
		// �ҵ���ǰ��ҵĵ���
		NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
			player.getId());
		// ����ʱ��
		int needTime=needTime(island.getIndex(),beIsland.getIndex());
		event.setNeedTime(needTime,player,checkTime);
		// ������ٴ�ֻ������
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
		// ս��ʤ��
		if(scene.getSuccessTeam()==0)
		{
			String message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"boss_has_been_last_kill");
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
			message=TextKit.replace(message,"%",boss.getBossLevel()+"");
			SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),message);
			// ϵͳ����
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
					// ϵͳ����
					SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
						message);
				}
				// ǰ10���˵Ľ���
				Service service=(Service)Service.factory.newSample(boss
					.getServiceSid());
				if(service!=null)
				{
					int maxNum=boss.getFleetMaxNum();
					// ���ǰ10�����˷��Ž���Ʒ objectAlliances����Ϊ����id���˺���ֻ����
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
									// �������������˷���
									alliance.addPlayerServices(
										objectFactory,BOSS_SERVICE_SID[j+1],
										serviceTime);
									// ������Ϣ
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
			// ��ɱ����
			Award killAward=(Award)Award.factory.newSample(boss
				.getKillAwardSid());
			// �����ʼ�
			int sourceIndex=objectFactory.getIslandCache()
				.getPlayerIsLandId(player.getId());
			int exp=addHurtExp(player,boss.getFleetGroup(),TimeKit
				.getSecondTime());
			// ���ݾ���ֵ����¼��㾭��ֵ
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			if(killAward!=null) killAward.setExperienceAward(exp);
			MessageKit.attackBossNpcIsLand(player,beIsland,objectFactory,
				data,event,true,sourceIndex,killAward,boss.getFleetGroup());
			// �Ƴ�boss����
			beIsland.updateSid(WATER_ISLAND_SID);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			// boss����ɱ
			boss.bekilled();
			// ˢ���µĵȼ�
			objectFactory.getWorldBossCache().addBossOnKill(
				boss);
			//�ɾ����ݲɼ� 
			AchieveCollect.killBoss(player);
		}
		// ս��ʧ��
		else
		{
			// �����ʼ�
			int sourceIndex=objectFactory.getIslandCache()
				.getPlayerIsLandId(player.getId());
			int exp=addHurtExp(player,boss.getFleetGroup(),TimeKit
				.getSecondTime());
			// ���ݾ���ֵ����¼��㾭��ֵ
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			Award award=new Award();
			award.setExperienceAward(exp);
			MessageKit.attackBossNpcIsLand(player,beIsland,objectFactory,
				data,event,false,sourceIndex,award,boss.getFleetGroup());
		}
		boss.resetLostNum();
		// bossս����ʧ����
		event.getFleetGroup().resetBossShips();
		checkFightEvent(event,player,objectFactory);
		//�ɾ����ݲɼ� 
		AchieveCollect.attackBoss(player);
		// ս�����˻���
		WarManicActivity activity=(WarManicActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
		if(activity!=null&&activity.isActive(TimeKit.getSecondTime()))
		{
			activity.addPScore(WarManicActivity.BOSS,attackNum,player);
		}
		// ˢ���¼�
		JBackKit.sendFightEvent(player,event,objectFactory);
		// ��ֻ��־ ������
		ShipCheckData shipdata=objectFactory.addShipTrack(event.getId(),
			ShipCheckData.WORLDBOSS_FIGHT,player,new IntList(),null,true);
		shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland.getIndex())
			+","+beIsland.getName()+","+beIsland.getIslandLevel()+","
			+scene.getSuccessTeam());
	}
	
	/** ����bossս�� */
	public static void attackNianNpc(FightEvent event,Player player,
		CreatObjectFactory objectFactory,NpcIsland beIsland)
	{
		NianActivity acti=(NianActivity)ActivityContainer.getInstance()
			.getActivity(ActivityContainer.NIAN_SID,0);
		if(acti==null||acti.getBoss()==null)
		{
			// �Ƴ�boss����
			beIsland.updateSid(WATER_ISLAND_SID);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			player.setAttribute(PublicConst.ATTACK_NIAN_TIME,
				TimeKit.getSecondTime()+"");
			return;
		}
		NianBoss boss=acti.getBoss();
		if(!boss.getFleetGroup().existShip())
		{
			// �Ƴ�boss����
			beIsland.updateSid(WATER_ISLAND_SID);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			// boss����ɱ
			boss.bekilled();
			player.setAttribute(PublicConst.ATTACK_NIAN_TIME,
				TimeKit.getSecondTime()+"");
			return;
		}
		int checkTime=event.getCreatAt()+event.getNeedTime();
		// ���¼�������������
		SeaBackKit.resetPlayerSkill(player,objectFactory);
		// ��boss
		Object[] object=boss.fight(event.getFleetGroup());
		int beAttackLevel=boss.getBossLevel();
		FightScene scene=(FightScene)object[0];
		FightShowEventRecord r=(FightShowEventRecord)object[1];
		// ս���ݲ�����
		ByteBuffer data=new ByteBuffer();
		SeaBackKit.conFightRecord(data,r.getRecord(),player.getName(),
			player.getLevel(),beIsland.getName(),beAttackLevel,
			PublicConst.FIGHT_TYPE_17,player,null,event.getFleetGroup(),
			boss.getFleetGroup(),false,null,null);
		event.setCreatAt(checkTime);
		event.setEventState(FightEvent.RETRUN_BACK);
		// �ҵ���ǰ��ҵĵ���
		NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
			player.getId());
		// ����ʱ��
		int needTime=needTime(island.getIndex(),beIsland.getIndex());
		event.setNeedTime(needTime,player,checkTime);
		
		// ������ٴ�ֻ������
		int lostNum=boss.lostNum();
		
		//���ӱ���������
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
		// ս��ʤ��
		if(scene.getSuccessTeam()==0)
		{
			String message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"nian_has_been_last_kill");
			message=TextKit.replace(message,"%",boss.getBossLevel()+"");
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
//			message=TextKit.replace(message,"%",player.getName());
//			message=TextKit.replace(message,"%",player.getName());
			
		
			// ϵͳ����
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
				// ϵͳ����
				SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
					message);
			}
			
			// ��ɱ����
			boss.sendKillAward(player,objectFactory);
			//������������
			boss.sendAllRankAward(objectFactory);
			//������������	
			boss.sendPlayerRankAward(objectFactory);
			
			win=true;
		}
		//��������
		int sourceIndex=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		int exp=addHurtExp(player,boss.getFleetGroup(),
			TimeKit.getSecondTime());
		// ���ݾ���ֵ����¼��㾭��ֵ
		exp=ActivityContainer.getInstance().resetActivityExp(exp);
		Award award=boss.getAttack_award();
		award.setExperienceAward(exp);
		//���ʼ�
		MessageKit.attackNianNpcIsLand(player,beIsland,objectFactory,
			data,event,win,sourceIndex,award,boss.getFleetGroup());
		
		boss.resetLostNum();
		// bossս����ʧ����
		event.getFleetGroup().resetBossShips();
		checkFightEvent(event,player,objectFactory);
		// ˢ���¼�
		JBackKit.sendFightEvent(player,event,objectFactory);
		if(win)
		{
			// �Ƴ�boss����
			beIsland.updateSid(WATER_ISLAND_SID);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),beIsland,
				objectFactory);
			// boss����ɱ
			boss.bekilled();
			// �����
			acti.setEndTime(TimeKit.getSecondTime());
		}
		// ��ֻ��־ ������
		ShipCheckData shipdata=objectFactory.addShipTrack(event.getId(),
			ShipCheckData.NIAN_FIGHT,player,new IntList(),null,true);
		shipdata.setExtra(SeaBackKit.getIslandLocation(beIsland.getIndex())
			+","+beIsland.getName()+","+beIsland.getIslandLevel()+","
			+scene.getSuccessTeam());
	}
	
	public static void checkFightEvent(FightEvent event,Player player,
		CreatObjectFactory objectFactory)
	{
		// ��ǰʱ��
		int nowTime=TimeKit.getSecondTime();
		checkFightEvent(event,player,objectFactory,nowTime);
	}
	/** ս���¼����� */
	public static void checkFightEvent(FightEvent event,Player player,
		CreatObjectFactory objectFactory,int nowTime)
	{
		// �¼�״̬
		int eventState=event.getEventState();
		// ��ǰ�¼�����ʱ��
		int finishTime=event.getCreatAt()+event.getNeedTime();
		if(finishTime>nowTime) return;
		// ������
		if(eventState==FightEvent.ATTACK)
		{
			synchronized(lock)
			{
				NpcIsland beIsland=objectFactory.getIslandCache().load(
					event.getAttackIslandIndex()+"");
				// �������������������
				if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
					beIsland=(NpcIsland)NpcIsland.factory
						.newSample(FightPort.NEW_PLAYER_ATT_ISLAND);
				// ����ս��
				// �Լ�Ϊ������
				if(event.getPlayerId()==player.getId())
				{
					if(beIsland==null)
					{
						log.warn("fight_error:island is null:id="
							+event.getAttackIslandIndex());
						// �¼��ķ�����
						Player sourcePlayer=objectFactory.getPlayerCache()
							.load(event.getPlayerId()+"");
						event.setDelete(FightEvent.DELETE_TYPE);
						event.getFleetGroup().cancel(sourcePlayer,true);
						// ˢ���¼�
						JBackKit.sendFightEvent(player,event,objectFactory);
						return;
					}
					// �ȼ���Ƿ��ǹ���boss
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
					// ����Ƿ��ǹ�������
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
					// ����Ұ�� �п������������ռ���
					else if(beIsland.getPlayerId()==0)
					{
						if(beIsland.checkDismiss())
							objectFactory.getGemManger().removeGemsIsland(
								beIsland.getIndex());			
						//�����ĵ���Ϊ��ʱ
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
							// ˢ���¼�
							JBackKit.sendFightEvent(player,event,objectFactory);
							//���ʼ�
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
					// �������
					else if(beIsland.getPlayerId()!=0)
					{
						attackPlayer(event,beIsland,objectFactory);
						TaskEventExecute.getInstance().executeEvent(
							PublicConst.ATTACK_PLAYER_TASK_EVENT,null,player,
							null);
					}
					// // ����NPC����
					// else
					// if(beIsland.getIslandType()==NpcIsland.ISLAND_NPC)
					// {
					// attackNpcIsland(event,player,beIsland,nowTime,
					// objectFactory);
					// }
				}
				else
				{
					// �Լ�Ϊ������ ���������Լ��Ѿ������
					if(beIsland.getPlayerId()!=player.getId())
					{
						// ��������
						// �¼��ķ�����
						Player sourcePlayer=objectFactory.getPlayerCache()
							.load(event.getPlayerId()+"");
						checkFightEvent(event,sourcePlayer,objectFactory);
						return;
					}
					// ս��
					attackPlayer(event,beIsland,objectFactory);
				}
			}
		}
		// ����
		else if(eventState==FightEvent.RETRUN_BACK)
		{
			// �¼��ķ�����
			Player sourcePlayer=objectFactory.getPlayerCache().load(
				event.getPlayerId()+"");
			// �鿴ĳ����Դ�Ƿ��Ѵ�����
			Resources.filterFullResources(event.getResources(),sourcePlayer);
			// ���������������
			if(sourcePlayer.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
			{
				NpcIsland island=(NpcIsland)NpcIsland.factory
								.getSample(FightPort.NEW_PLAYER_ATT_ISLAND);
				event.setResources(new int[Player.RESOURCES_SIZE]);
				island.forceSetResource(event.getResources(),FightPort.NEW_PLAYER_HOLD_RESOURCE);
				sourcePlayer.setAttribute(PublicConst.NEW_FOLLOW_PLAYER_HOLD,null);
			}
			// �ع���Դ
			int gems=Resources.addResources(sourcePlayer.getResources(),
				event.getResources(),sourcePlayer);
			if(gems>0)
			{
				objectFactory.createGemTrack(GemsTrack.FIGHT_GEM_ISLAND,
					player.getId(),gems,0,
					Resources.getGems(player.getResources())+gems);
			}
			// �ع齢��
			event.getFleetGroup().cancel(sourcePlayer,true);
			// ɾ���¼�
			event.setDelete(FightEvent.DELETE_TYPE);
			// ˢ���¼�
			JBackKit.sendFightEvent(sourcePlayer,event,objectFactory);

			// ��ֻ��־
			objectFactory.addShipTrack(event.getId(),ShipCheckData.SHIP_BACK_HOME,
				sourcePlayer,new IntList(),null,false);
		}
		JBackKit.sendMarchLine(objectFactory,event);

	}
	// ����NPCҰ��
	public static void attackNpcWild(FightEvent event,Player player,
		NpcIsland beIsland,int nowTime,CreatObjectFactory objectFactory)
	{
		Object[] object=null;
		/** �������� */
		Player beAttacker=null;
		FightEvent holdEvent=null;
		String beAttackName="";
		int beAttackLevel=0;
		int checkTime=event.getCreatAt()+event.getNeedTime();
		String attackPlayerName="";
		boolean success=false;
		int maxResouce=0;
		// �Ƿ���Ҫ���������¼�
		boolean goOnPushEvent=false;
		// ���¼�������������
		SeaBackKit.resetPlayerSkill(player,objectFactory);
		int passTime=0;
		// ���ط�Ϊռ����һ���npc
		FleetGroup defendGroup=null;
		/**buff**/
		IntList beAttackBuff=null;
		if(beIsland.getTempAttackEventId()!=0)
		{
			// �����
			holdEvent=(FightEvent)objectFactory.getEventCache().load(
				beIsland.getTempAttackEventId()+"");
			// �����ж�
			if(holdEvent==null)
			{
				beIsland.setTempAttackEventId(0);
				attackNpcWild(event,player,beIsland,nowTime,objectFactory);
				return;
			}
			defendGroup=holdEvent.getFleetGroup();
			beAttacker=objectFactory.getPlayerCache().load(
				holdEvent.getPlayerId()+"");
			// ��������
			SeaBackKit.resetPlayerSkill(beAttacker,objectFactory);
			beAttackName=beAttacker.getName();
			beAttackLevel=beAttacker.getLevel();

			// ����פ�ط��������Դ
			passTime=nowTime-holdEvent.getCreatAt();
			int carryResource=SeaBackKit.groupCarryResource(holdEvent
				.getFleetGroup());
			int addition=SeaBackKit.groupResourceAddition(player,event.getFleetGroup(),beIsland.getIslandType());
			//������Ϊ���������������������ɼ�������ս��������ʾ��һ��
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
				// פ��ʱ��Ҳ�������˷�������
//				Object[] array=alliance.getAllianSkills().getArray();
//				for(int i=0;i<array.length;i++)
//				{
//					if(array[i]==null) continue;
//					AllianceSkill skill=(AllianceSkill)array[i];
//					if(!skill.isDefenceSkill()) continue;
//					skill.setChangeValue(beAttacker.getAdjstment());
//				}
			}
			// �Լ��Ķ��� �����˵Ķ��� TODO
			if(holdEvent.getPlayerId()==event.getPlayerId()||allianceBool)
			{
				// �����ʼ�
				int sourceIndex=objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId());
				int fightType=PublicConst.FIGHT_TYPE_6;
				if(allianceBool) fightType=PublicConst.FIGHT_TYPE_10;
				SeaBackKit.fight_send_every(objectFactory,player,fightType,
					player.getName(),beIsland.getIndex(),player.getName(),
					"",true,null,null,event,null,null,sourceIndex,beIsland
						.getSid(),Message.RETURN_BACK,0,"",defendGroup,0);
				// ֱ�ӷ���
				event.setCreatAt(checkTime);
				event.setEventState(FightEvent.RETRUN_BACK);
				// ����ʱ��
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(player.getId());
				int needTime=needTime(island.getIndex(),beIsland.getIndex());
				event.setNeedTime(needTime,player,checkTime);
				// ˢ���¼�
				JBackKit.sendFightEvent(player,event,objectFactory);
				//�����ͬһ��ң��򲻽�����ʾ�¼���ɾ��
				if(holdEvent.getPlayerId()!=event.getPlayerId())
					JBackKit.deleteFightEvent(beAttacker,event);
				// �������� �Ƿ��Ѿ��ؼ�
				checkFightEvent(event,player,objectFactory);
				return;
			}
			holdEvent.flushBuff(beIsland,objectFactory,true);
			beAttackBuff=beIsland.getServices();
			object=holdEvent.fight(event.getFleetGroup(),true);
			//�ɾ����ݲɼ�
//			AchieveCollect.attackPlayer(player);//����Ұ��פ����Ҳ��ƶ��˳ɾ�
			AchieveCollect.perishShip(event.getFleetGroup(),beAttacker);
			AchieveCollect.perishShip(holdEvent.getFleetGroup(),player);
			//�ж��ж�  ս�����˻���
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
			// ��Ұ��
			defendGroup=beIsland.getFleetGroup();
			object=beIsland.fight(event.getFleetGroup());
			beAttackName=beIsland.getName();
			beAttackLevel=beIsland.getIslandLevel();
			//�ɾ����ݲɼ�
			AchieveCollect.attackNpc(beIsland,player);
		}
		FightScene scene=(FightScene)object[0];
		FightShowEventRecord r=(FightShowEventRecord)object[1];
		// ս���ݲ�����
		ByteBuffer data=new ByteBuffer();
		SeaBackKit.conFightRecord(data,r.getRecord(),player.getName(),player
			.getLevel(),beAttackName,beAttackLevel,PublicConst.FIGHT_TYPE_1,
			player,beAttacker,event.getFleetGroup(),defendGroup,false,null,beAttackBuff);
		// ս��ʤ��
		if(scene.getSuccessTeam()==0)
		{
			success=true;
			// NPC�����������פ��
			if(beIsland.getTempAttackEventId()!=0)
			{
				// ɾ������֮ǰ��event �з���Ȼ������
				holdEvent=(FightEvent)objectFactory.getEventCache().load(
					beIsland.getTempAttackEventId()+"");
				// �Ӷ����פ�ػ�õ���Դ
				beIsland.addResource(holdEvent.getResources(),event.getResources());
				beIsland.setResource(player,event.getResources(),passTime/60,event.getFleetGroup(),maxResouce);
				int exp=addHurtExp(beAttacker,holdEvent.getFleetGroup(),
					nowTime);
				// ���ݾ���ֵ����¼��㾭��ֵ
				exp=ActivityContainer.getInstance().resetActivityExp(exp);
				// �����ʼ�
				MessageKit.attackTempPlayer(player,beAttacker,event,
					holdEvent,objectFactory,beIsland,data,true,exp);
				// �����ʼ�����
				holdEvent.setDelete(FightEvent.DELETE_TYPE);
				// ˢ���¼�
				JBackKit.sendFightEvent(beAttacker,holdEvent,objectFactory);
				// ����ʱ��
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(beAttacker.getId());
				// ����Լ����¼� �Ƴ��Է��Ĺ����¼�
				if(island!=null&&beIsland!=null)
					objectFactory.getEventCache().removeHoldOnEvent(
						island.getIndex(),beIsland.getIndex(),beAttacker);

				attackPlayerName=beAttacker.getName();

				// פ����ұ����� ��־
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
				// �����ʼ�
				int sourceIndex=objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId());
				MessageKit.attackNpcIsLand(player,beIsland,objectFactory,
					data,event,true,sourceIndex);
				// ���¼���NPC�ı���
				beIsland.createFleetGroup();
			}
			// ����פ��
			if(event.getType()==FightEvent.ATTACK_HOLD)
			{
				// ����ı��б�(����������״̬��)
				if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
					objectFactory.getIslandCache().load(
						event.getAttackIslandIndex()+"");
				// ����Ұ�ص����ռ��
				beIsland.setTempAttackEventId(event.getId());
				event.setCreatAt(checkTime);
				event.setEventState(FightEvent.HOLD_ON);
				// ˢ���¼�
				JBackKit.sendFightEvent(player,event,objectFactory);
			}
			else
			{
				event.setCreatAt(checkTime);
				// �ҵ���ǰ��ҵĵ���
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(player.getId());
				int needTime=10;
				if(island!=null)
				{
					// ����ʱ��
					needTime=needTime(island.getIndex(),beIsland.getIndex());
				}
				// �ѱ��˴��� �Լ�Ҳ����
				beIsland.setTempAttackEventId(0);
				event.setNeedTime(needTime,player,checkTime);
				event.setEventState(FightEvent.RETRUN_BACK);
				// ˢ���¼�
				JBackKit.sendFightEvent(player,event,objectFactory);
				// �������� �Ƿ��Ѿ��ؼ�
				// checkFightEvent(event,player,objectFactory);
				goOnPushEvent=true;
			}
			// �����¼�
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_TASK_EVENT,beIsland,player,true);
		}
		// ս��ʧ��
		else
		{
			boolean bool=fightGroupDown(event,player,checkTime,objectFactory);
			// NPC�����������פ��
			if(beIsland.getTempAttackEventId()!=0)
			{
				int exp=addHurtExp(player,event.getFleetGroup(),nowTime);
				// ���ݾ���ֵ����¼��㾭��ֵ
				exp=ActivityContainer.getInstance().resetActivityExp(exp);
				// �����ʼ�
				MessageKit.attackTempPlayer(player,beAttacker,event,
					holdEvent,objectFactory,beIsland,data,false,exp);
				// ˢ���¼���֪פ�ط� �¼�����
				JBackKit.deleteFightEvent(beAttacker,event);
				// ˢ��ԭ�¼�
				FightEvent tempEvent=(FightEvent)objectFactory
					.getEventCache()
					.load(beIsland.getTempAttackEventId()+"");
				JBackKit.sendFightEvent(beAttacker,tempEvent,objectFactory);
			}
			else
			{
				int sourceIndex=objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId());
				// ����Ұ�� ���ͽ���Ʒ �����ʼ�
				MessageKit.attackNpcIsLand(player,beIsland,objectFactory,
					data,event,false,sourceIndex);
				// ���¼���NPC�ı���
				beIsland.createFleetGroup();
			}
			// ���������
			if(bool)
			{
				// ˢ���¼�
				JBackKit.sendFightEvent(player,event,objectFactory);
			}
			else
			{
				event.setCreatAt(checkTime);
				event.setEventState(FightEvent.RETRUN_BACK);
				// �ҵ���ǰ��ҵĵ���
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(player.getId());
				// ����ʱ��
				int needTime=needTime(island.getIndex(),beIsland.getIndex());
				event.setNeedTime(needTime,player,checkTime);
				// ˢ���¼�
				JBackKit.sendFightEvent(player,event,objectFactory);
				// �������� �Ƿ��Ѿ��ؼ�
				// checkFightEvent(event,player,objectFactory);
				goOnPushEvent=true;
			}
			// �����¼�
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_TASK_EVENT,beIsland,player,false);
		}
		// �����˱� ������
		addHurtShips(player,event.getFleetGroup(),nowTime,objectFactory);
		// �������������� �˱�����
		if(beAttacker!=null&&holdEvent!=null)
			addHurtShips(beAttacker,holdEvent.getFleetGroup(),nowTime,objectFactory);
		/** ��ֻ��־ */
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

	/** �����������ľ���ֵ */
	public static int addHurtExp(Player player,FleetGroup after,int time)
	{
		if(player==null) return 0;
		int exp=0;
		// �����˱�
		exp=after.hurtTroopsExp(player,time);
		if(exp<0) exp=0;
		return exp;
	}

	/** ���㲢����˱� */
	public static void addHurtShips(Player player,FleetGroup after,int time,
		CreatObjectFactory objectFactory)
	{
		
		if(player==null) return;
		// �����˱�
		after.hurtTroops(player,time);
		// �������˱�����Ӻ� ��ʼ������������Ϊ��ǰ����
		after.resetLastNum();
		// ˢ���˱�
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

	/** ������� beIsland����������ҵ��� event�����¼� */
	public static void attackPlayer(FightEvent event,NpcIsland beIsland,
		CreatObjectFactory objectFactory)
	{
		// ������
		Player attackPlayer=objectFactory.getPlayerCache().load(
			event.getPlayerId()+"");
		// ��������
		Player beAttackPlayer=objectFactory.getPlayerCache().load(
			beIsland.getPlayerId()+"");
		// ���¼��㼼��
		SeaBackKit.resetPlayerSkill(attackPlayer,objectFactory);
		SeaBackKit.resetPlayerSkill(beAttackPlayer,objectFactory);
		// ս��ʤ��
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
		// �˾�פ��
		if(event.getType()==FightEvent.ATTACK_HOLD
			&&beIsland.getPlayerId()!=0&&sameAlliance)
		{
			event.setCreatAt(checkTime);
			event.setEventState(FightEvent.HOLD_ON);
			// ˢ���¼�
			JBackKit.sendFightEvent(attackPlayer,event,objectFactory);
			// ˢ���¼�
			JBackKit.sendFightEvent(beAttackPlayer,event,objectFactory);
			return;
		}
		// ������������սbuff
		if(beAttackPlayer.checkService(PublicConst.NOT_FIGHT_BUFF,checkTime)!=null)
		{
			// �����ʼ�
			SeaBackKit.fight_send_every(objectFactory,attackPlayer,
				PublicConst.FIGHT_TYPE_7,beAttackPlayer.getName(),beIsland
					.getIndex(),attackPlayer.getName(),beAttackPlayer
					.getName(),true,null,null,event,null,null,sourceIndex,0,
				Message.RETURN_BACK,0,"",beAttackPlayer.getIsland().getMainGroup(),0);
			// ����
			event.setCreatAt(checkTime);
			event.setEventState(FightEvent.RETRUN_BACK);
			// ����ʱ��
			NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
				attackPlayer.getId());
			int needTime=needTime(island.getIndex(),beIsland.getIndex());
			event.setNeedTime(needTime,attackPlayer,checkTime);
			// ˢ���¼�
			JBackKit.sendFightEvent(attackPlayer,event,objectFactory);

			JBackKit.deleteFightEvent(beAttackPlayer,event);
			// �������� �Ƿ��Ѿ��ؼ�
			checkFightEvent(event,attackPlayer,objectFactory);
			return;
		}
		boolean goOnPushEvent=false;
		// ���㱻������
		beAttackPlayer.getIsland().pushAll(
			event.getCreatAt()+event.getNeedTime(),objectFactory);
		// �����������
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
				// Э��ս��
				allianceDefendGroup=allianceEvent.getFleetGroup();
				allianceDefendPlayer=objectFactory.getPlayerCache().load(
					allianceEvent.getPlayerId()+"");
				// Э���߼������¼���
				SeaBackKit.resetPlayerSkill(allianceDefendPlayer,
					objectFactory);
			}
		}
		// ����������
		FleetGroup beAttackGroup=beAttackPlayer.getIsland().getMainGroup();
		beAttackGroup.getOfficerFleetAttr().initOfficers(beAttackPlayer);
		if(allianceDefendGroup!=null) beAttackGroup=allianceDefendGroup;
		boolean bool=false;
		// �Ӷ���Դ��
		int resource[]=SeaBackKit.attackResourceP(event.getFleetGroup(),
			SeaBackKit.canResourceP(beAttackPlayer));
		//�ɾ����ݲɼ� 
		AchieveCollect.attackPlayer(attackPlayer);
		// ���ط�û�б�������û�в�ս
		int[] honorScore;
		if(!beAttackGroup.existShip()||!beAttackPlayer.isFight())
		{
			// ս��ʤ�����Ӿ��ٹ�ѫ
			addOfficerFeatsInPvP(PublicConst.PLAYER_FIGHT_SUCCESS_FEATS,attackPlayer);
			// ������������
			honorScore=honorScore(attackPlayer,beAttackPlayer,true,objectFactory);
			// ��������Ҽ���
			Resources.reduceResources(beAttackPlayer.getResources(),
				resource,beAttackPlayer);
			event.setResources(resource);
			int reduceProsperity = beAttackPlayer.reduceProsperity();
			// // �����Ӷ���Դ
			// long
			// thisPlunder=SeaBackKit.resourceTotal(event.getResources());
			// attackPlayer.addPlunderResource(thisPlunder);
			// �����ʼ�
			// ����push �ʼ�
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
			// �����¼�
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_TASK_EVENT,beIsland,attackPlayer,true);

			// ��¼�����¼�
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(beAttackPlayer.getAttributes(PublicConst.ALLIANCE_ID));
			if(alliance!=null)
			{
				// �鿴�Է��Ƿ�������
				AllianceEvent allianEvent=new AllianceEvent(
					AllianceEvent.ALLIANCE_EVENT_BEFIGHT,attackPlayer
						.getName()
						+SeaBackKit.getAllianceByPlayer(attackPlayer,
							objectFactory),beAttackPlayer.getName(),"",
					checkTime);
				alliance.addEvent(allianEvent);
			}
		}
		// ����ս��
		else
		{
			// ������ҳ��� ��Ҫ����ҵĳǷ����� ���� �վ� ���� 6,7,8
			// SeaBackKit.addDefendBuild(beAttackPlayer,beAttackGroup);
			// ���ؼ����˼���
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
			// ��������������ǰ���resetPlayerSkills�Ѿ�����ˣ������ٴε��û����֮ǰ��ӵı�������
//			if(defend!=null)
//			{
//				defend.addAllianceSkills(beAttackPlayer);
//			}
			//�ж��ж�  ս�����˻���
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
			// ս���ݲ�����
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
			// ������ʤ��
			if(scene.getSuccessTeam()==0)
			{
				success=true;
				// ս��ʤ�����Ӿ��ٹ�ѫ
				addOfficerFeatsInPvP(PublicConst.PLAYER_FIGHT_SUCCESS_FEATS,attackPlayer);
				// ������������
				honorScore=honorScore(attackPlayer,beAttackPlayer,true,objectFactory);
//				// Э����
//				if(allianceDefendPlayer!=null)
//				{
//					honorScore=honorScore(attackPlayer,allianceDefendPlayer,
//						true);
//				}
				// �Ӷ���Դ��(����ս����,��Ҫ���ǹ�������ֻ��ʧ���������յ���Դ�Ӷ���)
				resource=SeaBackKit.attackResourceP(event.getFleetGroup(),
					SeaBackKit.canResourceP(beAttackPlayer));
				// ��������Ҽ���
				Resources.reduceResources(beAttackPlayer.getResources(),
					resource,beAttackPlayer);
				event.setResources(resource);
				// ��������Ҽ��ٷ��ٶ� 
				int reduceProsperity = beAttackPlayer.reduceProsperity();
				// // �����Ӷ���Դ
				// long thisPlunder=SeaBackKit.resourceTotal(event
				// .getResources());
				// attackPlayer.addPlunderResource(thisPlunder);
				// ����
				// ��¼�����¼�
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						beAttackPlayer
							.getAttributes(PublicConst.ALLIANCE_ID));
				if(alliance!=null)
				{
					// �鿴�Է��Ƿ�������
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
				// Э����
				if(allianceDefendPlayer!=null)
				{
					exp=addHurtExp(allianceDefendPlayer,beAttackGroup,
						checkTime);
					// ɾ��Э���¼�
					allianceEvent.setDelete(FightEvent.DELETE_TYPE);
					JBackKit.deleteFightEvent(beAttackPlayer,allianceEvent);
					JBackKit.deleteFightEvent(allianceDefendPlayer,
						allianceEvent);
				}
				// ���ݾ���ֵ����¼��㾭��ֵ
				exp=ActivityContainer.getInstance().resetActivityExp(exp);
				// �����ʼ���ʼ ������
				MessageKit.attackPlayer(attackPlayer,beAttackPlayer,event,
					objectFactory,beIsland,data,true,exp,honorScore[0],
					allianceDefendPlayer,beAttackGroup,PublicConst.PLAYER_FIGHT_SUCCESS_FEATS,reduceProsperity);
				// �����¼�
				TaskEventExecute.getInstance()
					.executeEvent(PublicConst.ATTACK_TASK_EVENT,beIsland,
						attackPlayer,true);
			}
			// ������ʧ��
			else
			{
				// ս��ʧ�����Ӿ��ٹ�ѫ
				addOfficerFeatsInPvP(PublicConst.PLAYER_FIGHT_FAIL_FEATS,attackPlayer);
				honorScore=honorScore(attackPlayer,beAttackPlayer,false,objectFactory);
				int exp=addHurtExp(attackPlayer,event.getFleetGroup(),
					checkTime);
				// Э����
				if(allianceDefendPlayer!=null)
				{
					// ����ʧ��ʱ������ֵʼ�հ���������ʧ�Ĵ�ֻ���㡣
					// exp=addHurtExp(allianceDefendPlayer,beAttackGroup,
					// checkTime);
					// ˢ���¼�
					JBackKit.sendFightEvent(beAttackPlayer,event,
						objectFactory);
					// ˢ���¼�
					JBackKit.sendFightEvent(allianceDefendPlayer,event,
						objectFactory);
				}
				// ���ݾ���ֵ����¼��㾭��ֵ
				exp=ActivityContainer.getInstance().resetActivityExp(exp);
				// �����ʼ���ʼ ������
				MessageKit.attackPlayer(attackPlayer,beAttackPlayer,event,
					objectFactory,beIsland,data,false,exp,honorScore[0],
					allianceDefendPlayer,beAttackGroup,PublicConst.PLAYER_FIGHT_FAIL_FEATS,0);
				// ʧ��
				bool=fightGroupDown(event,attackPlayer,checkTime,
					objectFactory);
				// �����¼�
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.ATTACK_TASK_EVENT,beIsland,attackPlayer,
					false);
			}
			// �����˱�
			//�ɾ����ݲɼ�
			if(allianceDefendPlayer!=null)
			{
				AchieveCollect.perishShip(event.getFleetGroup(),allianceDefendPlayer);
			}
			else
			{
				AchieveCollect.perishShip(event.getFleetGroup(),beAttackPlayer);
			}
			
			// ������
			addHurtShips(attackPlayer,event.getFleetGroup(),checkTime,objectFactory);
			// ���ط�
			if(allianceDefendPlayer!=null)
			{
				//�ɾ����ݲɼ�
				AchieveCollect.perishShip(beAttackGroup,attackPlayer);
				
				addHurtShips(allianceDefendPlayer,beAttackGroup,checkTime,objectFactory);
			}
			else
			{
				//�ɾ����ݲɼ�
				AchieveCollect.perishShip(beAttackPlayer.getIsland()
					.getMainGroup(),attackPlayer);
				
				addHurtShips(beAttackPlayer,beAttackPlayer.getIsland()
					.getMainGroup(),checkTime,objectFactory);
				// ���ط��Զ�������������
				beAttackPlayer.autoAddMainGroup();
			}
		}
		// ���������
		if(bool)
		{
			// ˢ���¼�
			JBackKit.sendFightEvent(attackPlayer,event,objectFactory);

			// ˢ���¼�
			JBackKit.deleteFightEvent(beAttackPlayer,event);
		}
		else
		{
			// ����
			event.setCreatAt(checkTime);
			event.setEventState(FightEvent.RETRUN_BACK);
			// ����ʱ��
			NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
				attackPlayer.getId());
			if(island!=null)
			{
				int needTime=needTime(island.getIndex(),beIsland.getIndex());
				event.setNeedTime(needTime,attackPlayer,checkTime);
			}
			else
				event.setNeedTime(1,attackPlayer,checkTime);
			// ˢ���¼�
			JBackKit.sendFightEvent(attackPlayer,event,objectFactory);
			// ˢ���¼�
			JBackKit.deleteFightEvent(beAttackPlayer,event);
			// �������� �Ƿ��Ѿ��ؼ�
			// checkFightEvent(event,attackPlayer,objectFactory);
			goOnPushEvent=true;
		}
		try
		{
			// ��ֻ��־ ������
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
				// ��ֻ��־ ������
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
				// ��ֻ��־ ������
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
			// ˢ�±������Ĵ�
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
	// /** ����NPC���� */
	// public static void attackNpcIsland(FightEvent event,Player player,
	// NpcIsland beIsland,int nowTime,CreatObjectFactory objectFactory)
	// {
	// int checkTime=event.getCreatAt()+event.getNeedTime();
	// // ���NPC����ȼ�С����ҵ���ȼ� ��������
	// if(beIsland.getIslandLevel()<=player.getIsland().getIslandLevel())
	// {
	// // ����
	// event.setCreatAt(checkTime);
	// event.setEventState(FightEvent.RETRUN_BACK);
	// // �������� �Ƿ��Ѿ��ؼ�
	// checkFightEvent(event,player,objectFactory);
	// return;
	// }
	// // NPC���� ���ܺ���԰�Ǩ
	// Object[] object=beIsland.fight(event.getFleetGroup());
	// // ���¼���NPC�ı���
	// beIsland.createFleetGroup();
	// FightScene scene=(FightScene)object[0];
	// // �����˱� ������
	// addHurtShips(player,event.getFleetGroup());
	// // ���¼���NPC�ı���
	// beIsland.createFleetGroup();
	// // ս��ʤ��
	// if(scene.getSuccessTeam()==0)
	// {
	// // ռ�쵺��
	// if(event.getType()==FightEvent.ATTACK_HOLD)
	// {
	// // ʣ�ར�ӻع�
	// event.getFleetGroup().cancel(player);
	// // ��������
	// NpcIsland island=objectFactory.getIslandCache()
	// .getPlayerIsland(player.getId());
	// island.setPlayerId(0);
	// // �µ���
	// beIsland.setPlayerId(player.getId());
	// // ��ҵ�������
	// player.getIsland().setIslandLevel(beIsland.getIslandLevel());
	// event.setDelete(FightEvent.DELETE_TYPE);
	// return;
	// }
	// }
	// // ս��ʧ��
	// fightGroupDown(event,player,checkTime,objectFactory);
	//
	// }

	/** ����ʧ�ܷ��� ���߱����� */
	public static boolean fightGroupDown(FightEvent event,Player player,
		int checkTime,CreatObjectFactory objectFactory)
	{
		// ��������
		if(!event.getFleetGroup().existShip())
		{
			// �¼��ȴ�ɾ��
			event.setDelete(FightEvent.DELETE_TYPE);
			return true;
		}
		return false;
	}

	/** ���㹥��ʱ�� */
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

	/** ������������ bool �������Ƿ�ʤ�� */
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
	
	/** ���ս�����Ӿ��ٹ�ѫ */
	public static void addOfficerFeatsInPvP(int addFeats,Player attacker)
	{
		attacker.getOfficers().incrFeats(addFeats);
		JBackKit.sendOfficerInfo(attacker);
	}
}
