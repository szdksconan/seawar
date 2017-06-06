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

/** ˢ��ǰ̨�� */
public class JBackKit
{

	/** ǰ��ˢ�¶˿� */
	public static final int FROE_PORT=2000;
	/** EMAIL_PORT=2003 */
	public static final int EMAIL_PORT=2003;
	/** ��֤��ǰ�˶˿� */
	public static final int VERTIFY_PORT=2007;
	/** ����ǰ�˶˿� */
	public static final int OFFICER_PORT=2008;
	/** �»ǰ�˶˿� */
	public static final int ACT_PORT=2009;
	/** �˿�2007���ͳ���  VERTIFY_POPUP�����֤��*/
	public static final int VERTIFY_POPUP=63;
	/** �±�����ǰ�˶˿� */
	public static final int RECRUIT_PORT=2011;
	/**����սˢ�¶˿�**/
	public static final int BATTLE_FIGHT=2010;
	/**��ļ��Ϣ�˿�**/
	public static final int COMRADE_PORT=2012;
	/**����������˿�**/
	public static final int CROSS_LEAGUE_PORT=2013;
	
	/**
	 * ����type TASK_FINISH=1����ı� ADD_TASK=2 RESET_RESOURCE=3ˢ����Դ
	 * RESET_ACTIVIC=4ˢ������ RESET_SERVICE=5ˢ�·��� RESET_PLAYER=6ˢ�����
	 * RESET_ISLAND=7ˢ�µ��� RESET_BUNLD=8ˢ�°��� RESET_SELFPOINT=9ˢ�¹ؿ�
	 * RESET_TASK=10ˢ������ SEND_AWARD =11���͸�����Ʒsid SEND_RANDOM_TASK=12
	 * SEND_SHIPS=13ˢ�´�ֻ �յ�һ���ʼ� SEND_FIGHT_EVENT=15ˢ���¼�RESET_HURT_TROOPS=16�˱�
	 * RESET_HONOR_YE���� RESET_MAIN_GROUPˢ����������ACTIVITY_FLSUH�ˢ��
	 * LIMIT_SALE_ACTIVITY ��ʱ����ˢ��DISCOUNT_ACTIVITY=27���ۻ
	 * POINT_BUFF=33ˢ�¹ؿ��ӳɵȼ� ANNOUNCE_PUSH �������� ANNOUNCE_REMOVE �Ƴ�����
	 * SWITCH_STATE=48 ��ȡ��,������,���쳵��״̬ RECHANGEPAY_STATE=50������֧��״̬
	 * MODIFY_PNAME=57 ˢ��������� MODIFY_ANAME=58 ˢ��alliance���� MODIFY_PANAME=59
	 * ���޸�������Ƶ�ʱ�������˾�ˢ�������ڵ����� ARENA_RESET=60 ��������������� FIGHT_SCORE=61 ս���ı�����
	 * MARCH_LINE=62�����ͼ�о��� VERTIFY_POPUP=63������֤�� WAR_MANICս���������� 
	 * PAY_RELAY=66��ֵ����� MEALTIME_ENERGY=67 ������������ LOGIN_REWARD=69 ��½���������������
	 * FLUSH_BATTLE_FIGHT=70 ˢ��(��)����ս����Ϣ GROWTH_PLAN=71 ���ͳɳ��ƻ� OFFICER_SHOP=72 �����̵�ˢ�� 
	 * ,RESET_PROSPERITY=73 ˢ�·��ٶ�,FLUSH_HEAD=77ͷ�񼤻�ˢ��
	 * PEACE_ACT=74  ��ƽ�� RESET_SCIENCE=75 ˢ�¿�����Ϣ    FLUSH_FLAG=78 ˢ����������
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
	/** �ʼ�ˢ�¶˿� 
	 *  RM_MSG=70 ɾ���ʼ�
	 */
	public static final int RM_MSG=3;
	
	/**
	 *  ALLIANCE_MESSAGE_SEND=1 ˢ��ǰ̨�ʼ� ALLIANCE_CHANGE_STAGE=2 ������ս�Ľ׶α仯
	 *  ALLIANCE_CHANGE_BET=3 ����ֵ�仯 		   ALLIANCE_WAR_TIME=4  ����ս��ս��ʱ��
	 *  ALLIANCE_RESOURCE=5 ���ʱ仯 ALLIANCE_MATERIAL_RANK =7 �������а�
	 *  REMOVE_ALL_BATTLE_REPORT=10 �Ƴ�����ս�� REMOVE_PLAYER_INLIST=11 �Ƴ������е��������
	 *  PLAYER_LEAVE_ALLIANCE=12  ˢ�����ʺ;������а� ALLIANCE_OUTOF_RANK=13 ���˱��������а�
	 */
	
	public static final int ALLIANCE_MESSAGE_SEND=1,ALLIANCE_CHANGE_STAGE=2,ALLIANCE_CHANGE_BET=3,
					ALLIANCE_WAR_TIME=4,ALLIANCE_RESOURCE=5,ALLIANCE_MATERIAL_RANK=7,PLAYER_INTEGRAL=8,	
				  VOTE_TIMES=9, RESET_PLAYER_SHIP=10,REMOVE_PLAYER_INLIST=11,PLAYER_LEAVE_ALLIANCE=12,
				ALLIANCE_OUTOF_RANK=13;
	
	/**FLUSH_COMRADE_FREIEND=1 ˢ��ս������������   FLUSH_PLAYER_COMRADE=2 ���µ�ս�Ѽ���**/
	public static final int RESET_COMRADE_FREIEND=1,RESET_PLAYER_COMRADE_INFO=2;
	/**
	 * ��������� </p>CROSS_LEAGUE_PLAYER_INFO=1 �����Ϣ,CROSS_LEAGUE_SHOP=2
	 * �̵���Ϣ,CROSS_LEAGUE_CHALLENGE_LIST=3 ��ս�б�,CROSS_LEAGUE_WHOLE_INFO=4 ȫ����Ϣ
	 * */
	public static final int CROSS_LEAGUE_PLAYER_INFO=1,CROSS_LEAGUE_SHOP=2,
					CROSS_LEAGUE_CHALLENGE_LIST=3,CROSS_LEAGUE_WHOLE_INFO=4;
	/** ���һ������Ĺ㲥 */
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
		// ��ñ��������ĻỰ��
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
		// boss�Ƿ��ڱ���
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
						// �Ƿ���ͬ��
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

	// Ϊ[ʱ�»]Ԥ��
	// /** ��Ӷ������Ĺ㲥 */
	// public static void flushIsland(DSManager manager,Object[] lands,
	// CreatObjectFactory objectFactory)
	// {
	// if(lands==null) return;
	// // ��ñ��������ĻỰ��
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
	// // boss�Ƿ��ڱ���
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
	// // �Ƿ���ͬ��
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

	/** һ������õ���������� ���͸�ǰ̨ */
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

	/** ����ÿ�������5��������� */
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

	/** �¼�һ������ */
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

	/** ���ͽ���Ʒ */
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

	/** ˢ��ǰ̨��Դ */
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
	/** ˢ��ǰ̨���ٶ� */
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
		data.writeInt(player.getProsperityInfo()[0]);//���ٶ�ָ��
		data.writeInt(player.getProsperityInfo()[2]);//���ٶ�MAX
		c.send(data);
	}
	

	/** ˢ��ǰ̨���� */
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

	/** ˢ��ǰ̨���� */
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

	/** ˢ��ǰ̨���� */
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

	/** ˢ��ǰ̨�ؿ� */
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

	/** ˢ��ǰ̨���� */
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

	/** ˢ�´�ֻ */
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

	/** ��������ʼ� */
	public static void collatePlayerMail(Message message,Player player,
		CreatObjectFactory factory)
	{
		if(message.getMessageType()==Message.SYSTEM_TYPE)
			return;
		else if(message.getMessageType()==Message.FIGHT_TYPE)// ����
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
		else if(message.getMessageType()==0)// �ռ���
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

	/** ��ȡ��ȫ���ʼ�(�ռ��䣬����) */
	public static Object[] getNotServerMessages(int messageType,
		Player player,boolean selfSend,CreatObjectFactory factory)
	{
		ArrayList messageList=factory.getMessageCache().getMessageListById(
			player.getId());
		// ����ռ���
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

	/** ����һ���ʼ������ */
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
		// һ���ʼ�
		data.writeByte(1);
		message.showBytesWrite(data,state,"",player);
		c.send(data);
	}

	/** ����һ���ʼ������ */
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
		// һ���ʼ�
		data.writeByte(1);
		message.showBytesWrite(data,state,message.getContent(),player);
		c.send(data);
	}

	/** SEND_EXPˢ�¾��� */
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

	/** RESET_HURT_TROOPS ˢ���˱� */
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

	/** resetHonor ˢ������ */
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

	/** �������� */
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

	/** ˢ���������� */
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

	/** ǰ̨��ʾ��ʾ��Ϣ */
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

	/** ˢ�µ������� */
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

	/** ɾ����ĳ���¼� */
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

	/** ˢ���¼� */
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
		// ɾ����ʱ�� ˢ���������Դ�ͽ���
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

	/** ˢ����ҵ���״̬ */
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

	/** ˢ������ */
	public static void sendAllicace(ByteBuffer data,Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		c.send(data);
	}
	/** ˢ�����л */
	public static void sendActivityFlush(SessionMap map,ByteBuffer infoData)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(ACTIVITY_FLSUH);
		data.write(infoData.getArray(),infoData.offset(),infoData.length());
		map.send(data);
	}
	
	/**ˢ���������ͻ**/
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
	
	/** ˢ����ʱ�齱� */
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
			data.writeShort(player.getDrawDay());// ���̳齱����
			data.writeShort(player.getClassicDrawDay());// ����齱����
			c.send(data);
		}
	}
	/** ˢ�´��ۻ */
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
	/** ˢ����ʱ���� */
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
	/** ˢ�¸�����ʱ���� */
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

	/** ˢ�¾ݵ���Ϣ (����) */
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
	/** ˢ��ȫ���ݵ���Ϣ */
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
	/** ˢ�¿�洬ֻ ��ȫ���� */
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
	/** ˢ���������� */
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
	/** ˢ�ºŽ� */
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

	/** ˢ�¹ؿ��ӳ� */
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
	/** ˢ�³ɳɾ�,isFlush=�Ƿ������ɾ����ж�����ǿ��ˢ�� */
	public static void sendFlushAchieve(Player player,int sid,long cvalue,
		boolean isFlush)
	{
		// �Ƿ������ɾ����ж�����ǿ��ˢ��
		if(!isFlush)
		{
			Achievement achieve=(Achievement)Achievement.factory
				.getSample(sid);
			if(achieve==null) return;
			if(achieve.getAchieveType()==Achievement.OTHER) return;
			int progress=player.getAchieveProgress(sid);
			// �����ǰ�ɾ���ȫ����ɣ����ٽ�������
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
	/** ˢ���콵���� */
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
	/** ˢ�¸����콵���� */
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

	/** ˢ������������ʾ */
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

	/** ˢ��ǰ̨vip�ɳ� **/
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
	/** ˢ��ǰ̨GEM��¼ **/
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
	/** ˢ��ǰ̨�¿���Ϣ **/
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
					data.writeByte(0);// �жϵ����Ƿ������ȡ��ʯ
				else
					data.writeByte(1);
			}
		}
		c.send(data);
	}

	/** ˢ���׳影�� */
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

	/** ˢ�½���������Ϣ */
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

	/** �����ض��������͸�ǰ̨ */
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

	/** �����ض�����״̬ */
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

	/** ˢ�¹��� */
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
				data.writeByte(ANNOUNCE_PUSH);// ˢ�� ����46
				announcement.showByteWrite(data);
				c.send(data);
			}
		}
	}
	/** �Ƴ����� */
	public static void sendRemoveAnnouncement(SessionMap smap,
		Announcement announcement)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(ANNOUNCE_REMOVE);// �Ƴ�����47
		data.writeInt(announcement.getId());
		smap.send(data);
	}
	/** ���ͻ�Ծ��������� */
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

	/** �������װ����Ϣ */
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

	/** ����ÿ���ۿ���Ʒ״̬ */
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
		data.writeByte(DATE_OFF);// ÿ���ۿ�״̬49
		player.showByteWriteDatePriceOff(data);
		c.send(data);
	}

	/** ����ÿ���ۿ���Ʒ������Ϣ */
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

	/** �����ۼ����ѻ��Ϣ */
	public static void sendConsumeGemsState(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(CONSUME_GEMS);// �ۼ�����״̬53
		ConsumeGemsActivity.showBytesWrite(player,data);
		c.send(data);
	}

	/** ������������ۼ����ѻ��Ϣ */
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

	/** ���ͱ�ʯ˫�������������Ϣ */
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
		data.writeByte(DOUBLE_GEMS);// ˫������״̬54
		activity.showByteWrite(data,player);
		c.send(data);
	}

	/** ���ͱ�ʯ˫�������������Ϣ */
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

	/** ���͵�����֧����ʽ״̬ */
	public static void sendRechangeState(SessionMap smap)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RECHANGEPAY_STATE);// ������֧��״̬50
		data.writeBoolean(PublicConst.RECHANGE_STATLE);
		smap.send(data);
	}

	/** ���͵�����֧����ʽ״̬ */
	public static void sendRechangeState(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RECHANGEPAY_STATE);// ������֧��״̬50
		data.writeBoolean(PublicConst.RECHANGE_STATLE);
		c.send(data);
	}

	/** ���������룬�һ���ȿ���״̬ */
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
	/** ˢ���ۼƳ�ֵ��Ϣ */
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

	/** ˢ�¸����ۼƳ�ֵ��Ϣ */
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

	/** ˢ�¹���ϵͳ��Ϣ */
	public static void sendScrollMessage(DSManager manager,String content)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(SCROLL_MESSAGE);// 52
		data.writeUTF(content);
		manager.getSessionMap().send(data);
	}

	/** ˢ�¾�����������Ϣ */
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

	/** ˢ��������� */
	public static void sendPlayerName(Player player)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MODIFY_PNAME);// ˢ���������
		data.writeUTF(player.getName());
		c.send(data);
	}

	/** ˢ���������� */
	public static void sendAllianceName(Alliance alliance,
		CreatObjectFactory factory)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MODIFY_ANAME);// ˢ���������
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

	/** ���޸�������Ƶ�ʱ�������˾�ˢ�������ڵ����� */
	public static void sendmodifyPAname(Alliance alliance,
		CreatObjectFactory factory,int pid,String name)
	{
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(MODIFY_PANAME);// ˢ���������
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

	/** ���¼���ս�������͵�ǰ̨ */
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
		data.writeByte(FIGHT_SCORE);// ˢ�����ս��
		if(reset)
			SeaBackKit.setPlayerFightScroe(player,factory);
		data.writeInt(player.getFightScore());
		data.writeByte(reason);
		c.send(data);
	}

	/** ������֤�� */
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
		data.writeByte(VERTIFY_POPUP);// ������֤��
		Vertify v=(Vertify)manager.player_vertify.get(player.getId());
		data.writeInt(v.getVertifyWrongCount());
		data.writeInt(PublicConst.VERTIFY_TIME);
		data.writeInt(PublicConst.MAX_COUNT);
		c.send(data);
	}
	
	/** ��ֵ����� */
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
	
	/** �����о�·�� */
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
		// data.writeByte(stype);// 1 ���� 2�з� 3����
		// c.send(data);
		// }
	}
	
	/** �����о�·�� */
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
	 * ����ս������
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
	 * ȫ������ս������
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
	
	/** �����±�����(��������) */
	public static void sendRecruit(int taskType,Player player)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(RECRUIT_PORT);
		data.writeByte(2);//����
		RecruitKit.sendTask(data,taskType,player);
		c.send(data);
	}
	
	/** ���͸���(���Ŀ) */
	public static void sendRecruitAll(RecruitWelfareManager rmanager,Player player)
	{
		if(player==null) return;
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(RECRUIT_PORT);
		data.writeByte(1);// ���Ŀ
		rmanager.showBytesWrite(player,data);
		c.send(data);
	}
	
	/** ������Ҿ�����Ϣ */
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
	

	/**ȫ��ˢ�����˾�����Ϣ**/
	public static void sendAllianceBetInfo(BattleIsland battleIsland,
		CreatObjectFactory objectFactory)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(ALLIANCE_CHANGE_BET);
		if(battleIsland!=null)
		{
			// ����ĳ���
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
	
	
	/**ȫ��ˢ�����˽׶���Ϣ�仯**/
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
	
	/**ˢ������ս ս��ʱ��**/
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
	
	/**ˢ�����а�**/
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
	
	/**ˢ�»���*/
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
	/**ˢ������ս�ľ��״���**/
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
	
	/**ˢ�¹�Ա��������**/
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
	
	/**ˢ�¾��걻�������а�**/
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
	/**ˢ����ұ�����ֻ��Ϣ**/
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
	
	/**������ս��ս��ʶ�����Ƴ�ĳһ�����**/
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
	
	/** �����������˷�������ս�ʼ� */
	public static void sendAllianceReport(IntList list,CreatObjectFactory factory,Object[] messages)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(BATTLE_FIGHT);
		data.writeByte(ALLIANCE_MESSAGE_SEND);// ˢ������ս�ʼ�
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
	
	/** ���ͷ���(����)���Ϣ */
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
	
	/** ��½���� ÿ��24�����������������Ϣ */
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
	/** ɾ���ʼ�  num��������*/
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
	
	
	/**����ҽ�����������������ս��Ϣ**/
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
	
	/** ���ͳɳ��ƻ���Ϣ **/
	public static void sendGrowthPlan(CreatObjectFactory factory,
		GrowthPlanManager manager)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(FROE_PORT);
		data.writeByte(GROWTH_PLAN);
		manager.showBytesWrite(data);
		factory.getDsmanager().getSessionMap().send(data);
	}
	
	/** ˢ�µ�ǰ��ļս�������ߵ�״̬ **/
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
		// ����
		data.writeByte(outLinePlayer.getPlayerType());
		// ս��
		data.writeInt(outLinePlayer.getFightScore());
//		int in=outLinePlayer.getUpdateTime();
		int out=outLinePlayer.getExitTime();
		// ����ʱ��
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
		// ��������
		data.writeUTF(aname);
		c.send(data);
	}
	/**����µ���ļ��Աˢ��ǰ̨ս���б�������б�***/
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
	
	/**ˢ��ǰ̨�����̵�**/
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
	
	/**ͨ�̺���**/
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
	/**ȫ������**/
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
	/**����̽��**/
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
	
	/**��ƽ��**/
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
	
	/**ˢ�¿Ƽ��о��б�**/
	public static void sendResetScience(Player player,PlayerBuild checkBuild)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		if(checkBuild==null) return;
		/** ������б��е�ʱ�� **/
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(FROE_PORT);
		data.writeByte(RESET_SCIENCE);
		data.writeByte(checkBuild.getIndex());
		checkBuild.getProduce().showBytesWrite(data,TimeKit.getSecondTime());
		c.send(data);
	}
	
	/** ˢ����������� */
	public static void sendSellingActivty(SessionMap smap,Activity activity)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(ACT_PORT);
		data.writeByte(WAR_MANIC_ACT);
		data.writeByte(1);
		ActivityContainer.getInstance().sendFlushActivity(activity,data);
		smap.send(data);
	}
	
	/** ˢ��ӵ�о��� */
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
	 * ˢ�¼���ͷ��
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
	
	/** ˢ�¿�������������Ϣ */
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
	
	/** ˢ�¿���������̵���Ϣ */
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
	
	/** ˢ�¿����������ս�б� */
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
	
	/** ˢ�¿��������ȫ��Ϣ */
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
	
	/**ȫ���˽���ˢ��**/
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
	
	/** ˢ�����ߵĺ�����Ϣ */
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

