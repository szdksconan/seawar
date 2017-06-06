package foxu.dcaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import mustang.back.SessionMap;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.cross.goalleague.ClientLeagueManager;
import foxu.cross.server.CrossActManager;
import foxu.cross.war.CrossWarManager;
import foxu.cross.war.CrossWarPlayerManager;
import foxu.cross.warclient.ClientWarManager;
import foxu.dcaccess.datasave.AllianceFightSave;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.dcaccess.mem.AFightEventMemCache;
import foxu.dcaccess.mem.ActivityLogMemCache;
import foxu.dcaccess.mem.AllianceFightMemCache;
import foxu.dcaccess.mem.AllianceFightRecordMemCache;
import foxu.dcaccess.mem.AllianceMemCache;
import foxu.dcaccess.mem.AnnounceMentMemCache;
import foxu.dcaccess.mem.ArenaMemCache;
import foxu.dcaccess.mem.BattleGroundMemCache;
import foxu.dcaccess.mem.BattleIslandMemCache;
import foxu.dcaccess.mem.BindingTrackMemCache;
import foxu.dcaccess.mem.CoinsTrackMemCache;
import foxu.dcaccess.mem.EquipTrackMemCache;
import foxu.dcaccess.mem.FightEventMemCache;
import foxu.dcaccess.mem.ForbidMemCache;
import foxu.dcaccess.mem.GDataByDidMemCache;
import foxu.dcaccess.mem.GameDataMemCache;
import foxu.dcaccess.mem.GemsTrackMemCache;
import foxu.dcaccess.mem.IntegrationTrackMemCache;
import foxu.dcaccess.mem.MessageMemCache;
import foxu.dcaccess.mem.NpcIsLandMemCache;
import foxu.dcaccess.mem.OfficerTrackMemCache;
import foxu.dcaccess.mem.OrderMemCache;
import foxu.dcaccess.mem.PlayerMemCache;
import foxu.dcaccess.mem.ProducePropTrackMemCache;
import foxu.dcaccess.mem.PropTrackMemCache;
import foxu.dcaccess.mem.SciencePointTrackMemCache;
import foxu.dcaccess.mem.ShipDataMemCaChe;
import foxu.dcaccess.mem.WorldBossMemCache;
import foxu.ds.SWDSManager;
import foxu.sea.ContextVarManager;
import foxu.sea.GemsNpcIslandManager;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PlayerAdvice;
import foxu.sea.PublicConst;
import foxu.sea.QuestionnaireRecord;
import foxu.sea.Resources;
import foxu.sea.Role;
import foxu.sea.Service;
import foxu.sea.User;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.AllianceBattleFight;
import foxu.sea.alliance.alliancebattle.AllianceFightRecordTrack;
import foxu.sea.alliance.alliancebattle.IntegrationTrack;
import foxu.sea.alliance.alliancebattle.SciencePointTrack;
import foxu.sea.alliance.alliancefight.AllianceFight;
import foxu.sea.arena.ArenaManager;
import foxu.sea.award.Award;
import foxu.sea.bind.BindingTrack;
import foxu.sea.builds.Build;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.Product;
import foxu.sea.builds.Troop;
import foxu.sea.builds.produce.ProducePropTrack;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.officer.CoinsTrack;
import foxu.sea.officer.OfficerTrack;
import foxu.sea.order.Order;
import foxu.sea.proplist.Prop;
import foxu.sea.proplist.PropTrack;
import foxu.sea.ship.ShipLog;
import foxu.sea.shipdata.ShipCheckData;
import foxu.sea.shipdata.ShipDataEvent;
import foxu.sea.worldboss.WorldBoss;

/**
 * �������ݿ�洢�ı�Ķ��� ����ֻ�ܴ����� players messages fight_events orders author:icetiger
 */
public class CreatObjectFactory
{

	/** Ĭ��ָ������sid */
	public static final int DIECTOR_SID=7,AIR_SID=11,MISSILE_SID=12,
					ARTILLERY_SID=13;
	/** ��� */
	PlayerMemCache playerCache;
	/** �ʼ� */
	MessageMemCache messageCache;
	/** ս���¼� */
	FightEventMemCache eventCache;
	/** ���� */
	OrderMemCache orderCache;
	/** ���� */
	NpcIsLandMemCache islandCache;
	/** ��Ӫ���� */
	GameDataMemCache gameDataCache;
	/** ��Ӫ���� �����豸id */
	GDataByDidMemCache gDataByDidCache;
	/** ����˺����� */
	UserGameDBAccess userDBAccess;
	/** ��ҽ������� */
	PlayerAdviceDBAccess playerAdviceDBAccess;
	/** �����ʾ���Ҵ����� */
	QuestionnaireDBAccess questionnaireDBAccess;
	/** ��ʯ������־���� */
	GemsTrackMemCache gemsTrackMemCache;
	/** װ����־���� */
	EquipTrackMemCache equipTrackMemCache;
	/** ������־���� */
	OfficerTrackMemCache officerTrackMemCache;
	/** ��Ʒ������־���� */
	ProducePropTrackMemCache produceTrackMemCache;
	/** ��Ʒ��־���� */
	PropTrackMemCache propTrackMemCache;
	/**��һ�����־**/
	IntegrationTrackMemCache integrationTrackMemCache;
	/** �������� */
	AllianceMemCache allianceMemCache;
	/** ��ֻ��־ */
	ShipDataMemCaChe shipCache;
	/** ���������� */
	ArenaMemCache arenaMemCache;
	/** ������������ */
	ArenaManager arenaManager;
	/** ����boss */
	WorldBossMemCache worldBossCache;
	/** ��ս */
	AllianceFightMemCache allianceFightMemCache;
	/** ��ս�¼� */
	AFightEventMemCache aFightEventMemCache;
	/** �ݵ� */
	BattleGroundMemCache battleGroundMemCache;
	/** ���־ */
	ActivityLogMemCache activityLogMemCache;
	/** ��ͣ�豸 */
	ForbidMemCache forbidMemCache;
	/** ���� */
	AnnounceMentMemCache annMemcahe;
	/** ������� */
	ActivityContainer actContainer;
	/** ����������(server) */
	CrossActManager cactmanager;
	/** ���ս������(server) */
	CrossWarManager cwarmanager;
	/** ���ս��ҹ�����(server) */
	CrossWarPlayerManager cwarPmanger;
	/** ���ս������(client)*/
	ClientWarManager cwarCmanager;
	/** dsmanager */
	SWDSManager dsmanager;
	/**���˵���**/
   BattleIslandMemCache battleIslandMemCache;
   /**�Ƽ�����־**/
   SciencePointTrackMemCache sciencePointMemCache;
   /**����ս��־��¼**/
   AllianceFightRecordMemCache allianceFightRecordMemCache;
   /**������ս�׶�**/
   AllianceBattleFight battleFight;
   /**����־*/
   BindingTrackMemCache bindingMemCache;
   /**��ʯ����**/
   GemsNpcIslandManager gemManger;
   /**������־**/
   CoinsTrackMemCache coinsMemCache;
   /**���������������**/
   ClientLeagueManager clientLeagueManager;
	/** �ط��洢 */
	public int[] saveAndExit(boolean isgm)
	{
		int last[]=new int[33];
		
		boolean baseback=true;
		try
		{
			last[0]=playerCache.saveAndExit();
			if(last[0]>0)baseback=false;
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		try
		{
			last[1]=eventCache.saveAndExit();
			if(last[1]>0)baseback=false;
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		try
		{
			last[2]=islandCache.saveAndExit();
			if(last[2]>0)baseback=false;
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		try
		{
			last[3]=messageCache.saveAndExit();
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		try
		{
			last[4]=allianceMemCache.saveAndExit();
			if(last[4]>0)baseback=false;
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		try
		{
			last[5]=gemsTrackMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		try
		{
			last[6]=gameDataCache.saveAndExit();
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		try
		{
			last[7]=shipCache.saveAndExit();
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		try
		{
			last[8]=arenaMemCache.saveAndExit();
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[9]=worldBossCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[10]=allianceFightMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[11]=aFightEventMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[12]=battleGroundMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[13]=activityLogMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[14]=annMemcahe.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[15]=actContainer.saveActivity(isgm);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[16]=equipTrackMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[17]=produceTrackMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[18]=propTrackMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[19]=gDataByDidCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[20]=cactmanager.saveActs();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[21]=cwarmanager.saveFinalRep(isgm);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[22]=cwarPmanger.savePlayers(isgm);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[23]=cwarCmanager.saveRep(isgm);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[25]=battleIslandMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[24]=officerTrackMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[26]=integrationTrackMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[27]=sciencePointMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[28]=allianceFightRecordMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[29]=battleFight.getAllianceStage().saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[30]=bindingMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[31]=coinsMemCache.saveAndExit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			last[32]=clientLeagueManager.savePlayer(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//��������  �����������ṹ������
		if(!baseback||!isgm)return last;
		try
		{
			String  path=System.getProperty("user.dir");
			Runtime.getRuntime().exec(path+"/mysql_databak.sh");
		}
		catch(Exception e)
		{
			SeaBackKit.log.error("data backup fail:"+e.toString());
		}
		return last;
	}

	/** ���ĳ��sid��boss */
	public WorldBoss getWorldBossBySid(int bossSid)
	{
		return (WorldBoss)worldBossCache.load(bossSid+"");
	}

	/** ����һ��������־ list���β����Ĵ�ֻ */
	public ShipCheckData addShipTrack(int eventId,int type,Player player,IntList list,int[] honorScore,boolean flag)
	{
		Map< Integer,Integer> map=new HashMap<Integer,Integer>();
//		String front="";
//		StringBuffer logStr=new StringBuffer();
		ShipCheckData data=new ShipCheckData();
//		data.setId(shipCache.getUidkit().getPlusUid());
		data.setPlayerId(player.getId());
		data.setType(type);
//		front+="type:"+type;//���ñ���type
//		front+=" id:"+player.getId();
//		front+=" eid:"+eventId;
		data.setCreateAt(TimeKit.getSecondTime());
		data.setList(list);
//		logStr.append(" port:");
		// ���õ�ǰ�ۿھ���
		IntList leftList=new IntList();
		Object object[]=player.getIsland().getTroops().getArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			Troop troop=(Troop)object[i];
			leftList.add(troop.getShipSid());
			leftList.add(troop.getNum());
//			logStr.append("sid:").append(troop.getShipSid()).append("num:").append(troop.getNum());
			map.put(troop.getShipSid(),troop.getNum());
		}
//		logStr.append(" defend:");
		// ��ǰ�Ƿ�����
		Fleet fleetdefend[]=player.getIsland().getMainGroup().getArray();
		if(fleetdefend!=null)
		{
			for(int i=0;i<fleetdefend.length;i++)
			{
				if(fleetdefend[i]==null) continue;
				leftList.add(fleetdefend[i].getShip().getSid());
				leftList.add(fleetdefend[i].getNum());
//				logStr.append("sid:").append(fleetdefend[i].getShip().getSid()).append("num:").append(fleetdefend[i].getNum());
				Integer num=fleetdefend[i].getNum();
				if(map.get(fleetdefend[i].getShip().getSid())!=null)
					num+=map.get(fleetdefend[i].getShip().getSid());
				map.put(fleetdefend[i].getShip().getSid(),num);
			}
		}
//		logStr.append(" hurt:");
		data.setLeftList(leftList);
		// ��ǰ�˱�
		leftList=new IntList();
		object=player.getIsland().getHurtsTroops().getArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			Troop troop=(Troop)object[i];
			leftList.add(troop.getShipSid());
			leftList.add(troop.getNum());
//			logStr.append("sid:").append(troop.getShipSid()).append("num:").append(troop.getNum());
		}
//		logStr.append(" event:");
		data.setHurtList(leftList);
		// ��ǰ�¼�����
		ArrayList eventList=SeaBackKit.getFightEventSelf(player,this);
		if(eventList!=null)
		{
			for(int i=0;i<eventList.size();i++)
			{
				if(eventList.get(i)==null) continue;
				FightEvent event=(FightEvent)eventList.get(i);
				if(event.getDelete()==FightEvent.DELETE_TYPE) continue;
				ShipDataEvent shipDataEvent=new ShipDataEvent();
				shipDataEvent.setEventId(event.getId());
				shipDataEvent.setIndex(event.getAttackIslandIndex());
				shipDataEvent.setState(event.getEventState());
				Fleet[] fleet=event.getFleetGroup().getArray();
				for(int j=0;j<fleet.length;j++)
				{
					if(fleet[j]==null) continue;
					shipDataEvent.addShips(fleet[j].getNum(),fleet[j]
						.getShip().getSid());
//					logStr.append("sid:").append(fleet[j].getShip().getSid()).append("num:").append(fleet[j].getNum());
					Integer num=fleet[j].getNum();
					if(map.get(fleet[j].getShip().getSid())!=null)
						num+=map.get(fleet[j].getShip().getSid());
					map.put(fleet[j].getShip().getSid(),num);
				}
//				logStr.append(";");
				data.addShipDataEvent(shipDataEvent);
			}
		}
//		StringBuffer strb=new StringBuffer();
//		for (Integer key : map.keySet()) {
//				strb.append("sid:").append(key+"num:").append(map.get(key));
//		}
//		if(honorScore!=null && honorScore.length!=0)
//		{
//			if(flag)
//				logStr=logStr.append(" honor:").append((player.getHonorScore()-honorScore[1])).append(":"+honorScore[1]).append(":"+player.getHonorScore());
//			else 
//				logStr=logStr.append(" honor:").append((player.getHonorScore()-honorScore[2])).append(":"+honorScore[2]).append(":"+player.getHonorScore());
//		}
//		front=front+" total:"+strb.toString()+logStr.toString();
//		ShipLog.log.info(front);
		shipCache.putTrack(data);
		return data;
	}

	/**Э�����¼���¼**/
	public void addShipTrack(int eventId,int type,int[]honorScore,Player player)
	{
		String logStr="type:"+type;//���ñ���type
		logStr+=" id:"+player.getId();
		logStr+=" eid:"+eventId;
		logStr=logStr+" honor:"+(player.getHonorScore()-honorScore[1])+":"+honorScore[1]+":"+player.getHonorScore();
		ShipLog.log.info(logStr);
	}
	/** ��ȡ�ܹ���ֵ��ʯ */
	public int getGems()
	{
		return playerCache.getMaxGems();
	}

	/** ͨ����ȡһ����� */
	public Player getPlayerByName(String name,boolean bool)
	{
		return playerCache.loadByName(name,bool);
	}

	/** ͨ��id��ȡһ����� ������ı��б� */
	public Player getPlayerById(int id)
	{
		return playerCache.loadPlayerOnly(id+"");
	}

	/** ͨ��index��ȡ���� */
	public NpcIsland getIslandByIndex(String index)
	{
		return islandCache.load(index);
	}

	/** ͨ��index��ȡ���첻����ı��б� */
	public NpcIsland getIslandByIndexOnly(String index)
	{
		return islandCache.loadOnly(index);
	}

	/** ���������¼� */
	public void pushAll(Player player,int checkTime)
	{
		int islandIndex=getIslandCache().getPlayerIsLandId(player.getId());
		Object object[]=FightKit.pushFightEvent(islandIndex,this);
		if(object!=null) FightKit.checkFightEvent(object,this,player);
		player.getIsland().pushAll(checkTime,this);
	}

	/** �½�һ��guest�˺� */
	public boolean createGuestUser(String udid,int userId,String account)
	{
		boolean bool=false;
		try
		{
			bool=userDBAccess.createGuestUser(udid,userId,account);
		}
		catch(Exception e)
		{
			// TODO: handle exceptionT
			e.printStackTrace();
			return false;
		}
		return bool;
	}

	/** ������Ϸ��ѡ������ �½�һ������(player) ����֮ǰ�˺�id��� ���������ύ */
	public String createPlayer(String userAccount,String playerName,
		int roleSid)
	{
//		boolean bool=false;
		// �ж���������Ƿ��ظ�
		PlayerGameDBAccess db=(PlayerGameDBAccess)playerCache.getDbaccess();
		boolean boolName=false;
		if(playerName!=null)boolName=db.isExist(playerName,0);
		if(boolName) return null;
		Statement stmt=null;
		Connection con=null;
		SqlPersistence sqlp=(SqlPersistence)userDBAccess
			.getGamePersistence();
		User user=userDBAccess.loadUser(userAccount);
		if(user==null) return null;
		if(user.getPlayerId()!=0) return null;
		int userId=user.getId();
		int playerId=playerCache.getUidkit().getPlusUid();
		if(playerName==null)playerName="player"+playerId;
		try
		{
			con=sqlp.getConnectionManager().getConnection();
			con.setAutoCommit(false);
			stmt=con.createStatement();
			String userUpdate="UPDATE users SET player_id='"+playerId
				+"' WHERE id='"+userId+"'";
			int nowTime=TimeKit.getSecondTime();
			String playerAdd="INSERT INTO players(id,user_id,player_name,sid,create_at,update_at) VALUES("
				+playerId
				+","
				+userId
				+",'"
				+playerName
				+"',"
				+roleSid
				+","
				+nowTime+","+nowTime+")";
			stmt.addBatch(playerAdd);
			stmt.addBatch(userUpdate);
			stmt.executeBatch();
			// �����ύ
			con.commit();
			// ����Ϊ�Զ��ύ,��ΪTRUE
			con.setAutoCommit(true);
//			bool=true;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			try
			{
				// �������κ�SQL�쳣����Ҫ���лع�,������ΪϵͳĬ�ϵ��ύ��ʽ,��ΪTRUE
				if(con!=null)
				{
					con.rollback();
					con.setAutoCommit(true);
				}
			}
			catch(SQLException se1)
			{
				se1.printStackTrace();
			}
			return null;
		}
		finally
		{
			try
			{
				if(stmt!=null)
				{
					stmt.close();
					stmt=null;
				}
				if(con!=null)
				{
					con.close();
					con=null;
				}
			}
			catch(SQLException se)
			{
				se.printStackTrace();
			}
		}
		// ���¼����player�����ڴ���
		Player player=(Player)Role.factory.newSample(roleSid);
		// Ĭ������
		// Ĭ����һ��ָ������
		PlayerBuild build=(PlayerBuild)Build.factory.newSample(DIECTOR_SID);
		player.getIsland().addBuildNow(build,BuildInfo.INDEX_0);
		/** Ĭ����Դ */
		long[] resources={10000,10000,10000,10000,10000,0,0,0,0};
		/** Ĭ������ */
		int[] honor={0,1};
		/** Ĭ��ϵͳ����Ϊ���� */
		int iosSystem=(1<<PublicConst.ISLAND_BE_ATTACK)
			|(1<<PublicConst.BUILD_FINISHED)|(1<<PublicConst.AUTO_HOLD)
			|(1<<PublicConst.ENERY_PUSH_IS_FULL)
			|(1<<PublicConst.DATE_OFF_PUSH)|(1<<PublicConst.ONLINE_AWARD_PUSH)
			|(1<<PublicConst.PEACE_TIME_PUSH)|(1<<PublicConst.MEAL_TIME_ENERGY_PUSH)
			|(1<<PublicConst.STATIONED_PUSH);
		player.getIsland().setIosSystem(iosSystem);
		int[] actives=new int[Player.ACTIVES_SIZE];
		/** Ĭ�Ͼ��� 20 */
		actives[0]=20;
		/** Ĭ����һ��sidΪ12����Ʒ�� 1 */
		Prop prop=(Prop)Prop.factory.newSample(12);
		player.getBundle().incrProp(prop,true);
		prop=(Prop)Prop.factory.newSample(1);
		player.getBundle().incrProp(prop,true);
		/** ��3��2��Ǳͧ,2��Ѳ�� */
		player.getIsland().addTroop(10012,2,null);
//		player.getIsland().addTroop(10022,1,null);	
		/** Ĭ����50��ʯ */
		Resources.addGemsNomal(50,resources,null);
		/** Ĭ����48��ս */
		Service service=(Service)Service.factory.newSample(9);
		int time=0;
		Activity activity=actContainer.getActivity(ActivityContainer.PEACE_ACT,0);
		if(activity!=null && activity.isOpen(TimeKit.getSecondTime()))
		{
			time=activity.getEndTime()-TimeKit.getSecondTime();
			if(time<=0) time=0;
		}
		service.setServiceTime(60*60*24+time);
		player.addService(service,TimeKit.getSecondTime());
		player.setActives(actives);
		player.setResources(resources);
		player.setHonor(honor);
		player.bindUid(playerId);
		player.setId(playerId);
		player.setUser_id(userId);
		player.setCreateTime(TimeKit.getSecondTime());
		player.setName(playerName);
		// player.setCreateAt(TimeKit.getSecondTime());
		player.setAttribute(PublicConst.CREAT_NAME,"c");
		// �°汾�д������½�ɫ
		player.setAttribute(PublicConst.NEW_FOLLOW_PLAYER,"t");
		// �°汾�д������½�ɫ�Ƿ����פ���¼�
		player.setAttribute(PublicConst.NEW_FOLLOW_PLAYER_HOLD,"t");
		player.getTaskManager().pushNextTask();
		//�ɾ���Ϣ�ɼ�
		AchieveCollect.resourceStock(player);
		// ��ʼ��ͷ����Ϣ
		player.parserHead();
		player.setAttribute(PublicConst.HEAD_TO_ACHIEVEMENT, PublicConst.HEAD_SIGN);
		// // Ĭ�Ϻ�ը��
		// build=(PlayerBuild)Build.factory.newSample(AIR_SID);
		// build.setBuildLevel(1);
		// player.getIsland().addBuildNow(build,BuildInfo.INDEX_7);
		// // Ĭ�ϵ�����
		// build=(PlayerBuild)Build.factory.newSample(MISSILE_SID);
		// build.setBuildLevel(1);
		// player.getIsland().addBuildNow(build,BuildInfo.INDEX_8);
		// // Ĭ�Ϻ�����
		// build=(PlayerBuild)Build.factory.newSample(ARTILLERY_SID);
		// build.setBuildLevel(1);
		// player.getIsland().addBuildNow(build,BuildInfo.INDEX_9);
		// Ĭ��ϵͳ����

		playerCache.getDbaccess().save(player);
		playerCache.save(playerId+"",player);
		return playerName;
	}

	/** �½�һ��ȫ���ʼ����� */
	public Message createSystemMessage(int mesType,String content,
		String title,String sendName,Award award)
	{
		Message message=messageCache.createObect();
		message.setSendName(sendName);
		message.setTitle(title);
		message.setReceiveName("none");
		message.setContent(content);
		message.setMessageType(mesType);
		message.setAward(award);
		message.setCreateAt(TimeKit.getSecondTime());
		messageCache.addSystemMessage(message);
		// �����������
		SessionMap smap=dsmanager.getSessionMap();
		Session[] sessions=smap.getSessions();
		Connect con=null;
		Player player=null;
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					JBackKit.sendRevicePlayerMessage(player,message,
						message.getState(),this);
				}
			}
		}
		return message;
	}

	/** ����ʼ����� ���洢* */
	public Message createMessageOnly(int sendId,int receiveId,
		String content,String sendName,String receiveName,int messageType,
		String title,boolean bool)
	{
		Message message=messageCache.createObectOnly();
		message.setSendId(sendId);
		message.setReceiveId(receiveId);
		message.setContent(content);
		message.setSendName(sendName);
		message.setTitle(title);
		message.setReceiveName(receiveName);
		message.setMessageType(messageType);
		message.setCreateAt(TimeKit.getSecondTime());
		return message;
	}

	/**
	 * �½�һ���ʼ����� ����������ݿ� sendId ������ID ϵͳΪ0 reciveId ������ID content �ʼ�����
	 * sendName ���������� reciveName ���������� messageData �ʼ������� ��Դ����Ʒ������ û�д�null
	 */
	public Message createMessage(int sendId,int receiveId,String content,
		String sendName,String receiveName,int messageType,String title,
		boolean bool)
	{
		return createMessage(sendId,receiveId,content,sendName,receiveName,messageType,title,bool,false,null);
	}
	
	public Message createMessage(int sendId,int receiveId,String content,
		String sendName,String receiveName,int messageType,String title,
		boolean bool,Award award)
	{
		return createMessage(sendId,receiveId,content,sendName,receiveName,messageType,title,bool,false,award);
	}
	
	/**
	 * �½�һ���ʼ����� ����������ݿ� sendId ������ID ϵͳΪ0 reciveId ������ID content �ʼ�����
	 * sendName ���������� reciveName ���������� messageData �ʼ������� ��Դ����Ʒ������ û�д�null
	 */
	public Message createMessage(int sendId,int receiveId,String content,
		String sendName,String receiveName,int messageType,String title,
		boolean bool,boolean sendInBlack,Award award)
	{
		Message message=messageCache.createObect();
		message.setSendId(sendId);
		message.setReceiveId(receiveId);
		message.setContent(content);
		message.setSendName(sendName);
		message.setTitle(title);
		message.setReceiveName(receiveName);
		message.setMessageType(messageType);
		message.setCreateAt(TimeKit.getSecondTime());
		message.setAward(award);
		// ����һ�Զ�map����
		if(message.getMessageType()==Message.SYSTEM_TYPE)
		{
			messageCache.addSystemMessage(message);
		}
		else if(message.getMessageType()==Message.ALLIANCE_FIGHT_TYPE)
		{
			messageCache.addAllianceMessage(message);
		}
		else
		{
			if(message.getSendId()!=0&&message.getMessageType()==0)
					message.filerText();
			// ������ͷ�û���ڽ��շ��ĺ����������ʼ���ӵ����շ�
			if(!sendInBlack)
				messageCache.addMessage(message);
			if(bool) messageCache.addSelfMessage(message);
		}
		return message;
	}

	/**
	 * �½�һ��ս���¼����� ����������ݿ� playerId:���ID attackislandIndex�������������index
	 * fleetGroup: ����
	 */
	public FightEvent createFightEvent(int playerId,int sourceIndex,
		int attackislandIndex,FleetGroup fleetGroup)
	{
		FightEvent event=eventCache.createObect();
		// ���ù���������id
		event.setPlayerId(playerId);
		event.setSourceIslandId(sourceIndex);
		event.setAttackIslandIndex(attackislandIndex);
		event.setEventState(FightEvent.ATTACK);
		event.setFleetGroup(fleetGroup);
		// ����һ�Զ��б�
		eventCache.addFightEvent(event,sourceIndex);
		eventCache.addFightEvent(event,attackislandIndex);
		eventCache.saveEvent(event);
		return event;
	}

	/** ��ȡ��ҽ��� */
	public PlayerAdvice getPlayerAdvice(String adviceId)
	{
		PlayerAdvice advice=playerAdviceDBAccess.load(adviceId);
		return advice;
	}

	/** �����Ѿ��ظ�������ҽ��� */
	public void gmResponse(String adviceId,String gmResponse)
	{
		PlayerAdvice advice=playerAdviceDBAccess.load(adviceId);
		advice.setGmResponse(gmResponse);
		advice.setState(PlayerAdvice.RESPONSE);
		playerAdviceDBAccess.save(advice);
	}

	/** �洢һ����ҽ��� */
	public void savePlayerAdvice(int playerId,String playerName,
		String title,String content)
	{
		PlayerAdvice advice=new PlayerAdvice();
		advice.setPlayerId(playerId);
		advice.setPlayerName(playerName);
		advice.setTitile(title);
		advice.setContent(content);
		advice.setCreatTime(TimeKit.getSecondTime());
		playerAdviceDBAccess.save(advice);
	}

	/** ��ȡ��δ�����ǰ50����ҽ��� */
	public PlayerAdvice[] getPlayerAdvice()
	{
		String sql="SELECT * FROM player_advice WHERE state=0 ORDER BY creatTime limit 50";
		return playerAdviceDBAccess.loadBySql(sql);
	}
	
	/** �洢��ҵ����ʾ���Ŀ�� */
	public boolean saveQuestionnaireAnswer(int playerId,int actId,
		int topicIndex,int topicType,String answer)
	{
		QuestionnaireRecord qr = new QuestionnaireRecord();
		qr.setPlayerId(playerId);
		qr.setActId(actId);
		qr.setTopicIndex(topicIndex);
		qr.setTopicType(topicType);
		qr.setAnswer(answer);
		return questionnaireDBAccess.save(qr);
	}
	
	/** ����һ������־ */
	public void createBindingTrack(int bindType,int trackType,
		int actionType,int uid,int pid,String operateInfo,String lastRecord,
		String currentRecord)
	{
		BindingTrack track=new BindingTrack();
		track.setBindType(bindType);
		track.setTrackType(trackType);
		track.setActionType(actionType);
		track.setUid(uid);
		track.setPid(pid);
		track.setOperateInfo(operateInfo);
		track.setLastRecord(lastRecord);
		track.setCurrentRecord(currentRecord);
		track.setTime(TimeKit.getSecondTime());
		bindingMemCache.putTrack(track);
	}
	
	/** ����һ��װ����־ */
	public void createEquipTrack(int type,int reason,int playerId,int sid,int num,int item_id,
		int nowLeft)
	{
		EquipmentTrack track=new EquipmentTrack();
//		track.setId(equipTrackMemCache.getUidkit().getPlusUid());
		track.setType(type);
		track.setReason(reason);
		track.setPlayerId(playerId);
		track.setEquipSid(sid);
		track.setNum(num);
		track.setCreateAt(TimeKit.getSecondTime());
		track.setYear(SeaBackKit.getTheYear());
		track.setMonth(SeaBackKit.getTheMonth());
		track.setDay(SeaBackKit.getDayOfMonth());
		track.setNowLeft(nowLeft);
		track.setItem_id(item_id);
		equipTrackMemCache.putTrack(track);
	}
	
	/** ����һ��������־ */
	public void createOfficerTrack(int type,int reason,int playerId,int sid,int num,int item_id,
		int nowLeft)
	{
		OfficerTrack track=new OfficerTrack();
//		track.setId(equipTrackMemCache.getUidkit().getPlusUid());
		track.setType(type);
		track.setReason(reason);
		track.setPlayerId(playerId);
		track.setOfficerSid(sid);
		track.setNum(num);
		track.setCreateAt(TimeKit.getSecondTime());
		track.setNowLeft(nowLeft);
		track.setItem_id(item_id);
		officerTrackMemCache.putTrack(track);
	}
	
	/** ����һ�����Ѽ�¼ */
	public void createGemTrack(int type,int playerId,int gems,int item_id,
		long nowGems)
	{
		GemsTrack track=new GemsTrack();
		track.setId(gemsTrackMemCache.getUidkit().getPlusUid());
		track.setType(type);
		track.setPlayerId(playerId);
		track.setGems(gems);
		track.setCreateAt(TimeKit.getSecondTime());
		track.setYear(SeaBackKit.getTheYear());
		track.setMonth(SeaBackKit.getTheMonth());
		track.setDay(SeaBackKit.getDayOfMonth());
		track.setNowGems(nowGems);
		track.setItem_id(item_id);
		gemsTrackMemCache.putTrack(track);
	}

	/** ����һ����Ʒ������־ */
	public void createProducePropTrack(int type,int playerId,Product product,
		int buildIndex,int buildSid,int buildLv,int productId)
	{
		ProducePropTrack track=new ProducePropTrack();
//		track.setId(produceTrackMemCache.getUidkit().getPlusUid());
		track.setType(type);
		track.setPlayerId(playerId);
		track.setPropSid(product.getSid());
		track.setNum(product.getNum());
		track.setNeedTime(product.getProduceTime());
		track.setCreateAt(TimeKit.getSecondTime());
		track.setBuildIndex(buildIndex);
		track.setBuildSid(buildSid);
		track.setBuildLv(buildLv);
		track.setProductId(productId);
		produceTrackMemCache.putTrack(track);
	}
	
	/** ����һ����Ʒ��־ */
	public void createPropTrack(int type,int playerId,int propSid,
		int invokeNum,int leftNum)
	{
		PropTrack track=new PropTrack();
//		track.setId(propTrackMemCache.getUidkit().getPlusUid());
		track.setType(type);
		track.setPlayerId(playerId);
		track.setPropSid(propSid);
		track.setCreateAt(TimeKit.getSecondTime());
		track.setInvokeNum(invokeNum);
		track.setLeftNum(leftNum);
		propTrackMemCache.putTrack(track);
	}
	
	/** ����һ��������־ */
	public void createIntegrationTrack(int type,int playerId,int sid,int num,int nowLeft,int state)
	{
		IntegrationTrack track=new IntegrationTrack();
//		track.setId(equipTrackMemCache.getUidkit().getPlusUid());
		track.setType(type);
		track.setPlayerId(playerId);
		track.setNum(num);
		track.setCreateAt(TimeKit.getSecondTime());
		track.setYear(SeaBackKit.getTheYear());
		track.setMonth(SeaBackKit.getTheMonth());
		track.setDay(SeaBackKit.getDayOfMonth());
		track.setNowLeft(nowLeft);
		track.setPropSid(sid);
		track.setState(state);
		integrationTrackMemCache.putTrack(track);
	}
	
	/** ����һ��������־ */
	public void createCoinsTrack(int type,int playerId,int sid,int num,int nowLeft,int state)
	{
		CoinsTrack track=new CoinsTrack();
//		track.setId(equipTrackMemCache.getUidkit().getPlusUid());
		track.setType(type);
		track.setPlayerId(playerId);
		track.setNum(num);
		track.setCreateAt(TimeKit.getSecondTime());
		track.setYear(SeaBackKit.getTheYear());
		track.setMonth(SeaBackKit.getTheMonth());
		track.setDay(SeaBackKit.getDayOfMonth());
		track.setNowLeft(nowLeft);
		track.setPropSid(sid);
		track.setState(state);
		coinsMemCache.putTrack(track);
	}
	
	/** ����һ�����˿Ƽ�����־ */
	public void createSciencePointTrack(int type,int allianceId,int num,long nowLeft,int state,int playerId,int style,String extra)
	{
		SciencePointTrack track=new SciencePointTrack();
//		track.setId(equipTrackMemCache.getUidkit().getPlusUid());
		track.setType(type);
		track.setNum(num);
		track.setCreateAt(TimeKit.getSecondTime());
		track.setYear(SeaBackKit.getTheYear());
		track.setMonth(SeaBackKit.getTheMonth());
		track.setDay(SeaBackKit.getDayOfMonth());
		track.setPlayerId(playerId);
		track.setAllianceId(allianceId);
		track.setNowLeft(nowLeft);
		track.setState(state);
		track.setStyle(style);
		track.setExtra(extra);
		sciencePointMemCache.putTrack(track);
	}
	
	/** ����һ�����˿Ƽ�����־ */
	public void createAllianceFightRecordTrack(int allianceId,int num,String players,String rankValue,int stage,int battleSid,int type,int stime,int etime)
	{
		AllianceFightRecordTrack track=new AllianceFightRecordTrack();
		track.setNum(num);
		track.setAllianceId(allianceId);
		track.setCreateAt(TimeKit.getSecondTime());
		track.setYear(SeaBackKit.getTheYear());
		track.setMonth(SeaBackKit.getTheMonth());
		track.setDay(SeaBackKit.getDayOfMonth());
		track.setPlayers(players);
		track.setBattleIsland(battleSid);
		track.setRankvalue(rankValue);
		track.setStage(stage);
		track.setType(type);
		track.setStime(stime);
		track.setEtime(etime);
		track.setState(ContextVarManager.getInstance().getVarValue(
			ContextVarManager.ALLIANCE_FIGHT_RECORD));
		allianceFightRecordMemCache.putTrack(track);
	}
	
	/** �½�һ��order���� ����������ݿ� */
	public Order createOrder()
	{
		return null;
	}
	/** ����allianceID ��ȡ��ս */
	public AllianceFight getAFight(int allianceID,boolean ischange)
	{
		Object obj=allianceFightMemCache.getCacheMap().get(allianceID);
		if(obj==null)return null;
		if(ischange)allianceFightMemCache.getChangeListMap().put(((AllianceFightSave)obj).getId(),obj);
		return (AllianceFight)((AllianceFightSave)obj).getData();
	}
	/** ����allianceID ��ȡ���� */
	public Alliance getAlliance(int allianceID,boolean ischange)
	{
		Object obj=allianceMemCache.getCacheMap().get(allianceID);
		if(obj==null)return null;
		if(ischange)allianceMemCache.getChangeListMap().put(((AllianceSave)obj).getId(),obj);
		return (Alliance)((AllianceSave)obj).getData();
	}

	/**
	 * @return eventCache
	 */
	public FightEventMemCache getEventCache()
	{
		return eventCache;
	}

	/**
	 * @param eventCache Ҫ���õ� eventCache
	 */
	public void setEventCache(FightEventMemCache eventCache)
	{
		this.eventCache=eventCache;
	}

	/**
	 * @return messageCache
	 */
	public MessageMemCache getMessageCache()
	{
		return messageCache;
	}

	/**
	 * @param messageCache Ҫ���õ� messageCache
	 */
	public void setMessageCache(MessageMemCache messageCache)
	{
		this.messageCache=messageCache;
	}

	/**
	 * @return orderCache
	 */
	public OrderMemCache getOrderCache()
	{
		return orderCache;
	}

	/**
	 * @param orderCache Ҫ���õ� orderCache
	 */
	public void setOrderCache(OrderMemCache orderCache)
	{
		this.orderCache=orderCache;
	}

	/**
	 * @return playerCache
	 */
	public PlayerMemCache getPlayerCache()
	{
		return playerCache;
	}

	/**
	 * @param playerCache Ҫ���õ� playerCache
	 */
	public void setPlayerCache(PlayerMemCache playerCache)
	{
		this.playerCache=playerCache;
	}

	/**
	 * @return userDBAccess
	 */
	public UserGameDBAccess getUserDBAccess()
	{
		return userDBAccess;
	}

	/**
	 * @param userDBAccess Ҫ���õ� userDBAccess
	 */
	public void setUserDBAccess(UserGameDBAccess userDBAccess)
	{
		this.userDBAccess=userDBAccess;
	}

	/**
	 * @return islandCache
	 */
	public NpcIsLandMemCache getIslandCache()
	{
		return islandCache;
	}

	/**
	 * @param islandCache Ҫ���õ� islandCache
	 */
	public void setIslandCache(NpcIsLandMemCache islandCache)
	{
		this.islandCache=islandCache;
	}

	/**
	 * @return playerAdviceDBAccess
	 */
	public PlayerAdviceDBAccess getPlayerAdviceDBAccess()
	{
		return playerAdviceDBAccess;
	}

	/**
	 * @param playerAdviceDBAccess Ҫ���õ� playerAdviceDBAccess
	 */
	public void setPlayerAdviceDBAccess(
		PlayerAdviceDBAccess playerAdviceDBAccess)
	{
		this.playerAdviceDBAccess=playerAdviceDBAccess;
	}
	
	public QuestionnaireDBAccess getQuestionnaireDBAccess()
	{
		return questionnaireDBAccess;
	}

	public void setQuestionnaireDBAccess(
		QuestionnaireDBAccess questionnaireDBAccess)
	{
		this.questionnaireDBAccess=questionnaireDBAccess;
	}

	/**
	 * @return gameDataCache
	 */
	public GameDataMemCache getGameDataCache()
	{
		return gameDataCache;
	}

	/**
	 * @param gameDataCache Ҫ���õ� gameDataCache
	 */
	public void setGameDataCache(GameDataMemCache gameDataCache)
	{
		this.gameDataCache=gameDataCache;
	}
	
	public GDataByDidMemCache getgDataByDidCache()
	{
		return gDataByDidCache;
	}
	
	public void setgDataByDidCache(GDataByDidMemCache gDataByDidCache)
	{
		this.gDataByDidCache=gDataByDidCache;
	}

	/**
	 * @return gemsTrackMemCache
	 */
	public GemsTrackMemCache getGemsTrackMemCache()
	{
		return gemsTrackMemCache;
	}

	/**
	 * @param gemsTrackMemCache Ҫ���õ� gemsTrackMemCache
	 */
	public void setGemsTrackMemCache(GemsTrackMemCache gemsTrackMemCache)
	{
		this.gemsTrackMemCache=gemsTrackMemCache;
	}

	/**
	 * @return dsmanager
	 */
	public SWDSManager getDsmanager()
	{
		return dsmanager;
	}

	/**
	 * @param dsmanager Ҫ���õ� dsmanager
	 */
	public void setDsmanager(SWDSManager dsmanager)
	{
		this.dsmanager=dsmanager;
	}

	public AllianceMemCache getAllianceMemCache()
	{
		return allianceMemCache;
	}

	public void setAllianceMemCache(AllianceMemCache allianceMemCache)
	{
		this.allianceMemCache=allianceMemCache;
	}

	public ShipDataMemCaChe getShipCache()
	{
		return shipCache;
	}

	public void setShipCache(ShipDataMemCaChe shipCache)
	{
		this.shipCache=shipCache;
	}

	public ArenaManager getArenaManager()
	{
		return this.arenaManager;
	}

	public void setArenaManager(ArenaManager arenaManager)
	{
		this.arenaManager=arenaManager;
	}

	public ArenaMemCache getArenaMemCache()
	{
		return this.arenaMemCache;
	}

	public void setArenaMemCache(ArenaMemCache arenaMemCache)
	{
		this.arenaMemCache=arenaMemCache;
	}

	public WorldBossMemCache getWorldBossCache()
	{
		return worldBossCache;
	}

	public void setWorldBossCache(WorldBossMemCache worldBossCache)
	{
		this.worldBossCache=worldBossCache;
	}
	
	public BattleGroundMemCache getBattleGroundMemCache()
	{
		return battleGroundMemCache;
	}

	public void setBattleGroundMemCache(BattleGroundMemCache battleGroundMemCache)
	{
		this.battleGroundMemCache=battleGroundMemCache;
	}
	
	public AFightEventMemCache getaFightEventMemCache()
	{
		return aFightEventMemCache;
	}
	
	public void setaFightEventMemCache(AFightEventMemCache aFightEventMemCache)
	{
		this.aFightEventMemCache=aFightEventMemCache;
	}

	public AllianceFightMemCache getAllianceFightMemCache()
	{
		return allianceFightMemCache;
	}
	
	public void setAllianceFightMemCache(
		AllianceFightMemCache allianceFightMemCache)
	{
		this.allianceFightMemCache=allianceFightMemCache;
	}

	
	public ActivityLogMemCache getActivityLogMemCache()
	{
		return activityLogMemCache;
	}

	
	public void setActivityLogMemCache(ActivityLogMemCache activityLogMemCache)
	{
		this.activityLogMemCache=activityLogMemCache;
	}

	
	public ForbidMemCache getForbidMemCache()
	{
		return forbidMemCache;
	}

	
	public void setForbidMemCache(ForbidMemCache forbidMemCache)
	{
		this.forbidMemCache=forbidMemCache;
	}

	public AnnounceMentMemCache getAnnMemcahe()
	{
		return annMemcahe;
	}

	public void setAnnMemcahe(AnnounceMentMemCache annMemcahe)
	{
		this.annMemcahe=annMemcahe;
	}

	public ActivityContainer getActContainer()
	{
		return actContainer;
	}

	public void setActContainer(ActivityContainer actContainer)
	{
		this.actContainer=actContainer;
	}

	
	public EquipTrackMemCache getEquipTrackMemCache()
	{
		return equipTrackMemCache;
	}

	
	public void setEquipTrackMemCache(EquipTrackMemCache equipTrackMemCache)
	{
		this.equipTrackMemCache=equipTrackMemCache;
	}

	public ProducePropTrackMemCache getProduceTrackMemCache()
	{
		return produceTrackMemCache;
	}

	public void setProduceTrackMemCache(
		ProducePropTrackMemCache produceTrackMemCache)
	{
		this.produceTrackMemCache=produceTrackMemCache;
	}
	
	public PropTrackMemCache getPropTrackMemCache()
	{
		return propTrackMemCache;
	}

	public void setPropTrackMemCache(PropTrackMemCache propTrackMemCache)
	{
		this.propTrackMemCache=propTrackMemCache;
	}
	
	public IntegrationTrackMemCache getIntegrationTrackMemCache()
	{
		return integrationTrackMemCache;
	}

	
	public void setIntegrationTrackMemCache(
		IntegrationTrackMemCache integrationTrackMemCache)
	{
		this.integrationTrackMemCache=integrationTrackMemCache;
	}

	public CrossActManager getCactmanager()
	{
		return cactmanager;
	}

	
	public void setCactmanager(CrossActManager cactmanager)
	{
		this.cactmanager=cactmanager;
	}

	
	public CrossWarManager getCwarmanager()
	{
		return cwarmanager;
	}

	
	public void setCwarmanager(CrossWarManager cwarmanager)
	{
		this.cwarmanager=cwarmanager;
	}

	
	public CrossWarPlayerManager getCwarPmanger()
	{
		return cwarPmanger;
	}

	
	public void setCwarPmanger(CrossWarPlayerManager cwarPmanger)
	{
		this.cwarPmanger=cwarPmanger;
	}

	
	public ClientWarManager getCwarCmanager()
	{
		return cwarCmanager;
	}

	
	public void setCwarCmanager(ClientWarManager cwarCmanager)
	{
		this.cwarCmanager=cwarCmanager;
	}

	/**���˵���**/
	public BattleIslandMemCache getBattleIslandMemCache()
	{
		return battleIslandMemCache;
	}

	
	public void setBattleIslandMemCache(BattleIslandMemCache battleIslandMemCache)
	{
		this.battleIslandMemCache=battleIslandMemCache;
	}
	

	public OfficerTrackMemCache getOfficerTrackMemCache()
	{
		return officerTrackMemCache;
	}

	
	public void setOfficerTrackMemCache(OfficerTrackMemCache officerTrackMemCache)
	{
		this.officerTrackMemCache=officerTrackMemCache;
	}

	
	public SciencePointTrackMemCache getSciencePointMemCache()
	{
		return sciencePointMemCache;
	}

	
	public void setSciencePointMemCache(
		SciencePointTrackMemCache sciencePointMemCache)
	{
		this.sciencePointMemCache=sciencePointMemCache;
	}

	
	public AllianceFightRecordMemCache getAllianceFightRecordMemCache()
	{
		return allianceFightRecordMemCache;
	}

	
	public void setAllianceFightRecordMemCache(
		AllianceFightRecordMemCache allianceFightRecordMemCache)
	{
		this.allianceFightRecordMemCache=allianceFightRecordMemCache;
	}

	
	public AllianceBattleFight getBattleFight()
	{
		return battleFight;
	}

	
	public void setBattleFight(AllianceBattleFight battleFight)
	{
		this.battleFight=battleFight;
	}

	
	public BindingTrackMemCache getBindingMemCache()
	{
		return bindingMemCache;
	}

	
	public void setBindingMemCache(BindingTrackMemCache bindingMemCache)
	{
		this.bindingMemCache=bindingMemCache;
	}

	
	public GemsNpcIslandManager getGemManger()
	{
		return gemManger;
	}

	
	public void setGemManger(GemsNpcIslandManager gemManger)
	{
		this.gemManger=gemManger;
	}

	
	public CoinsTrackMemCache getCoinsMemCache()
	{
		return coinsMemCache;
	}

	
	public void setCoinsMemCache(CoinsTrackMemCache coinsMemCache)
	{
		this.coinsMemCache=coinsMemCache;
	}

	public ClientLeagueManager getClientLeagueManager()
	{
		return clientLeagueManager;
	}

	public void setClientLeagueManager(
		ClientLeagueManager clientLeagueManager)
	{
		this.clientLeagueManager=clientLeagueManager;
	}
	
	
}
	
