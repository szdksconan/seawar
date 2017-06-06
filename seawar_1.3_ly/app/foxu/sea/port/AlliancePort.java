package foxu.sea.port;

import mustang.field.Fields;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.math.MathKit;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.text.TextValidity;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.PlayerGameDBAccess;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.ds.PlayerKit;
import foxu.ds.SWDSManager;
import foxu.sea.ContextVarManager;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.OrderList;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceAutoTransfer;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.alliance.AllianceFlag;
import foxu.sea.alliance.alliancebattle.AllianceBattleFight;
import foxu.sea.alliance.alliancebattle.AllianceBattleManager;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.DonateRank;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.alliance.chest.AllianceChest;
import foxu.sea.alliance.chest.AllianceChestManager;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.task.TaskEventExecute;

/** ����ͨѶ�˿� */
public class AlliancePort extends AccessPort
{

	/** ���˹㲥��Ϣ���� */
	public static final int KICK_OUT=1,// ���߳�����
					PLAYER_JOIN=2,// ������Ҽ�������
					NOTICE_CHANGE=3,// ���˹����޸�
					EXP_LEVEL_CHANGE=4,// ���˵ȼ�,��������
					SOME_ONE_EIXT=5,// ����˳�����
					SINGLE_SKILL_CHANGE=6,// ���˵�������ˢ��(ˢ��ָ��sid���ܵľ���͵ȼ�)
					RIGHT_CHANGE=7,// ����Ȩ���޸�(�����ƽ�����Ȩ��)
					ADD_REQUEST=8,// �����������
					REMOVE_REQUEST=9,// �Ƴ���������
					DIS_MISS_ALLIANCE_PUSH=10,// ��ɢ����
					CANCEL_ALL_APPCAITON=11,
					ALIIANCE_ADD=12,
					MEMBER_LOGIN_STATE_CHANGE=14,
					BOSS_SEND_VALUE=15,// ˢ�����ѵ�½״̬
					PLAYER_GIVE_VALUE=16,//����ˢ�¹��׵�
					CHANGE_PNAME=17,//�޸�����������
					JOIN_ALLIANCE_SETTING=18;//��������
					
				

	/** �����¼�ҳ�� */
	public static final int MAX_EVENT_SIZE=20;
	/** �������ҳ�� */
	public static final int MAX_PAGE=5;
	/** �������������˸��� */
	public static final int MAX_APP_NUM=5;
	/** ����ǰ̨�˿ڳ��� */
	public static final int ALLIANCE_PORT=2005;
	/**
	 * ���˹㲥��ǰ̨���� EXIST_ALLIANCE�Ƴ����� DIS_MISS_ALLIANCE��ɢ����
	 * CHANGE_ALLIANCE_DES�ı������͹��� NEW_PLAYER_IN����Ҽ��� SEND_KICK_PLAYER�ߵ�ĳ�����
	 * SEND_CHANGE_PLAYER_DUTY �޸���ҵ�ְ�� 
	 */
	public static final int EXIST_ALLIANCE=1,DIS_MISS_ALLIANCE=2,
					CHANGE_ALLIANCE_DES=3,NEW_PLAYER_IN=4,
					SEND_KICK_PLAYER=5,SEND_CHANGE_PLAYER_DUTY=6;

	public static final int MIN_LEN=1,MAX_LEN=12;
	/**
	 * ���� CREATE_A_ALLIAN=1�������� GET_ALLIAN_SORT��ȡ�������� SET_HOSTILE=24 ���õж�����
	 * ALIANCE_JOIN_SET=26�������� ALLIANCE_CHEST=27���˱��� SET_ALLIANCEFLAG=28 ������������
	 */
	public static final int CREATE_A_ALLIAN=1,GET_ALLIAN_SORT=2,
					SERACH_ALLIAN=3,VIEW_ALLIAN=4,APPLICATION_ALLIAN=5,
					CANCEL_APPLICATION_ALLIAN=6,EXIT_ALLIAN=7,DIS_ALLIAN=8,
					GET_ALLIAN_SELF_RANK=9,CHANGE_DES_AND_AN=10,
					GET_ALL_ALLIANCE_EVENT=11,MASTER_ARGEE_PLAYERS=12,
					CANCEL_ALL_APPAICATION=13,GET_ALL_APPAICATION=14,
					KICK_PLAYER=15,CHANGE_PLAYER_DUTY=16,
					REFUSE_ONE_APPCATION=17,ALLIANCE_GIVE_VALUE=18,
					GET_FIGHT_RANK=19,INVITE_PLAYER=20,
					ALLIANCE_INVITATION=21,ALLIANCE_INVITATION_ACCEPT=22,
					ALLIANCE_INVITATION_REFUSE=23,SET_HOSTILE=24,
					DIS_HOSTILE=25,ALIANCE_JOIN_SET=26,ALLIANCE_CHEST=27,
					SET_ALLIANCEFLAG=28;

	/** ��ҳ��С */
	public static final int PAGE_SIZE=20;
	/** ���˸���ս�����г��� */
	public static final int FIGHT_SIZE=100;
	/** ����ʱ���� 1��Сʱ */
	public static final int FLUSH_TIME=60*60/2;

	/** ���ݻ�ȡ�� */
	CreatObjectFactory objectFactory;
	/** ��һ��ˢ�µ�ʱ�� */
	int lastTime;
	/** ���˻���ˢ�±�ʶ */
	boolean isResetLuckyPoint=true;
	/** ���˵����� */
	ArrayList alliances=new ArrayList();
	/** ���������ս������ */
	OrderList personFight=new OrderList();
	/** ���������ս������ˢ�µ�ʱ�� */
	int flushTime;
	
	TextValidity tv;

	/** dsmanager */
	SWDSManager dsmanager;
	/** ��ս������ */
	AllianceFightManager afightManager;
	/** �᳤�Զ��ƽ� */
	AllianceAutoTransfer autoTransfer;
	/** ���˱�������� */
	AllianceChestManager allianceChestManager;
	/**������ս**/
	AllianceBattleFight battleFight;
	/**������ս������**/
	AllianceBattleManager battleFigtmanager;
	
	Logger log=LogFactory.getLogger(AlliancePort.class);
	
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
		int type=data.readUnsignedByte();
		// ��������
		if(type==CREATE_A_ALLIAN)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.CREATE_ALLIANCE_LEVEL_LIMIT,player,
				"create_alliance_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			/** �������� */
			String name=data.readUTF();
			int image_sid=data.readUnsignedShort();
			int clour_sid=data.readUnsignedShort();
			int modle_sid=data.readUnsignedShort();
			String str=SeaBackKit.checkFlagSid(image_sid,clour_sid,modle_sid);
			if(str!=null)
				throw new DataAccessException(0,str);
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().loadOnly(
						player.getAttributes(PublicConst.ALLIANCE_ID));
				if(alliance!=null)
					throw new DataAccessException(0,"you have alliance");
			}
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
			if(tv.valid(name,true)!=null || name.indexOf(",")>=0)
				throw new DataAccessException(0,"alliance name is not valid");
			if(SeaBackKit.isContainValue(PublicConst.SHIELD_WORD,name.trim()))
			{
				throw new DataAccessException(0,"alliance name is not valid");
			}
			// ���Դ�Сд�ж������Ƿ����
			if(objectFactory.getAllianceMemCache().loadByName(name,false)!=null)
			{
				throw new DataAccessException(0,
					"alliance name has been used");
			}
			// �ж������Ƿ����
			if(objectFactory.getAllianceMemCache().getDbaccess()
				.isExist(name,0))
			{
				throw new DataAccessException(0,
					"alliance name has been used");
			}
			// �жϽ���Ƿ��㹻
			if(!Resources.checkResources(0,0,0,0,
				PublicConst.ALLIANCE_CREATE_MONEY,player.getResources()))
			{
				throw new DataAccessException(0,"money is not enough");
			}
			Resources.reduceResources(player.getResources(),0,0,0,0,
				PublicConst.ALLIANCE_CREATE_MONEY,player);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.createObect();
			alliance.setName(name);
			alliance.setMasterPlayerId(player.getId());
			alliance.addPlayer(player.getId());
			alliance.pushAllFightScore(objectFactory);
			alliance.setAttribute(PublicConst.ALLIANCE_CREATE_TIME,
				String.valueOf(TimeKit.getSecondTime()));
			/**�����´�������Ѵ���Ϊ0**/
			alliance.getFlag().setFreeTime(0);
			objectFactory.getAllianceMemCache().getDbaccess().save(alliance);

			// ��ս���
			afightManager.creatAlliance(alliance.getId());
			afightManager.jionAlliance(player,alliance.getId());
			
			//
			removeFightRank(player);
			alliance.getFlag().setAllianceFlag(image_sid,clour_sid,modle_sid);
			data.clear();
			player.setAttribute(PublicConst.ALLIANCE_ID,alliance.getId()+"");
			player.setAttribute(PublicConst.ALLIANCE_JOIN_TIME,
				TimeKit.getSecondTime()+"");
			player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
			alliance.showBytesWrite(data,player,objectFactory);
			afightManager.showBytesWrite(alliance.getId(),data);
			player.delInvitedRecords();
			autoTransfer.updateAutoAlliance(alliance);
			// �����ʼ�
			String title=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"new_alliance_title");
			String content=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"new_alliance_content");
			content=TextKit.replace(content,"%",alliance.getName());
			MessageKit.sendSystemMessages(player,objectFactory,content,title);
		}
		// ����
		else if(type==GET_ALLIAN_SORT)
		{
			// ��ҳ ÿҳ20��
			int pageIndex=data.readUnsignedByte();
			// ֧��ǰ400��������
			if(pageIndex>MAX_PAGE||pageIndex<=0) return null;
			data.clear();
			flushFileds(false);
			int num=alliances.size()-(pageIndex-1)*PAGE_SIZE;
			if(num<0) num=0;
			if(num>PAGE_SIZE) num=PAGE_SIZE;
			int page=getPage();
			if(page==0) page=1;
			// ������ҳ��
			data.writeByte(page);
			data.writeByte(num);
			for(int i=(pageIndex-1)*PAGE_SIZE;i<pageIndex*PAGE_SIZE;i++)
			{
				if(i>=alliances.size()) break;
				RankInfo rank=(RankInfo)alliances.get(i);
				String allianceName=rank.getPlayerName();
				data.writeShort(i+1);
				data.writeUTF(allianceName);
				// ����
				Alliance alliance=objectFactory.getAllianceMemCache()
					.loadByName(rank.getPlayerName(),true);
				data.writeByte(alliance.playersNum());
				alliance.pushAllFightScore(objectFactory);
				// ս����
				data.writeInt(rank.getRankInfo());
				// ����״̬
				String appName=player
					.getAttributes(PublicConst.ALLIANCE_APPLICATION);
				if(appName!=null)
				{
					//ת����id
					appName=nameChaneId(appName);
					String apps[]=TextKit.split(appName,"%");
					boolean bool=false;
					for(int j=0;j<apps.length;j++)
					{
						if(TextKit.parseInt(apps[j])==alliance.getId())
						//if(apps[j].equals(allianceName))
						{
							bool=true;
							break;
						}
					}
					data.writeBoolean(bool);
				}
				else
				{
					data.writeBoolean(false);
				}
			}
		}
		// �������˾�ȷ����
		else if(type==SERACH_ALLIAN)
		{
			String name=data.readUTF();
			Alliance alliance=objectFactory.getAllianceMemCache()
				.loadByName(name,false);
			data.clear();
			if(alliance==null)
			{
				throw new DataAccessException(0,"not this alliance");
			}
			else
			{
				flushAllianceRank(alliance);
				data.writeByte(1);
				if(alliance.getRankNum()<=0)
				{
					alliance.setRankNum(MathKit.randomValue(101,140));
				}
				data.writeShort(alliance.getRankNum());
				data.writeUTF(alliance.getName());
				data.writeByte(alliance.playersNum());
				// ս����
				data.writeInt(alliance.getAllFightScore());
				// ����״̬
				String appName=player
					.getAttributes(PublicConst.ALLIANCE_APPLICATION);
				if(appName!=null)
				{
					//ת����id
					appName=nameChaneId(appName);
					String apps[]=TextKit.split(appName,"%");
					boolean bool=false;
					for(int j=0;j<apps.length;j++)
					{
						if(TextKit.parseInt(apps[j])==alliance.getId())
						//if(apps[j].equals(alliance.getName()))
						{
							bool=true;
							break;
						}
					}
					data.writeBoolean(bool);
				}
				else
				{
					data.writeBoolean(false);
				}
			}
		}
		// �鿴����
		else if(type==VIEW_ALLIAN)
		{
			/** ���� */
			String name=data.readUTF();
			Alliance alliance=objectFactory.getAllianceMemCache()
				.loadByName(name,false);
			if(alliance==null)
			{
				throw new DataAccessException(0,"alliance has been dismiss");
			}
			flushAllianceRank(alliance);
			data.clear();
			data.writeInt(alliance.getId());
			data.writeUTF(name);
			data.writeByte(alliance.getAllianceLevel());
			data.writeByte(alliance.playersNum());
			if(alliance.getRankNum()<=0)
			{
				alliance.setRankNum(MathKit.randomValue(101,140));
			}
			data.writeShort(alliance.getRankNum()-1);
			data.writeUTF(alliance.getMasterName(objectFactory));
			data.writeUTF(alliance.getDescription());
			alliance.getFlag().showBytesWriteAllianceFlag(data);
			//�жϵ�ǰ����Ƿ�������
			String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
			boolean flag=false;
			if(aid!=null && aid.length()!=0)
			{
				data.writeBoolean(true);
				Alliance calliance=objectFactory.getAlliance(TextKit.parseInt(aid),false);
				if(SeaBackKit.isHostile(calliance,alliance))
				{
					data.writeBoolean(true);
					flag=true;
				}
				else
					data.writeBoolean(false);
			}
			if(!flag)	
			{
				// ����״̬
				data.writeBoolean(false);
				String appName=player
					.getAttributes(PublicConst.ALLIANCE_APPLICATION);
				if(appName!=null)
				{
					appName=nameChaneId(appName);
					String apps[]=TextKit.split(appName,"%");
					boolean bool=false;
					for(int j=0;j<apps.length;j++)
					{
						if(TextKit.parseInt(apps[j])==alliance.getId())
						// if(apps[j].equals(alliance.getName()))
						{
							bool=true;
							break;
						}
					}
					data.writeBoolean(bool);
				}
				else
				{
					data.writeBoolean(false);
				}
			}
		}
		// ���빫��
		else if(type==APPLICATION_ALLIAN)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.JOIN_ALLIANCE_LEVEL_LIMIT,player,
				"join_alliance_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			String name=data.readUTF();
			if(player.getAttributes(PublicConst.ALLIANCE_APPLICATION)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_APPLICATION)
					.equals(""))
			{
				String appiacneName=player
					.getAttributes(PublicConst.ALLIANCE_APPLICATION);
				if(appiacneName!=null&&!appiacneName.equals(""))
				{
					String allians[]=TextKit.split(appiacneName,"%");
					if(allians.length>=5)
					{
						throw new DataAccessException(0,
							"you can appcation five alliance");
					}
				}
			}
			// �Ƿ�������
			String alliance_id=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(alliance_id!=null&&!alliance_id.equals(""))
			{
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().loadOnly(alliance_id+"");
				if(alliance!=null)
					throw new DataAccessException(0,"you have alliance");
			}
			// ���˲�����
			Alliance alliance=objectFactory.getAllianceMemCache()
				.loadByName(name,true);
			if(alliance==null)
			{
				throw new DataAccessException(0,"alliance has been dismiss");
			}
			if(alliance.getApplicationList().size()>=Alliance.APPLICATION_MAX_NUM)
			{
				throw new DataAccessException(0,
					"alliance application is full");
			}
			// ������������
			int limitNum=PublicConst.ALLIANCE_LEVEL_NUMS[alliance
				.getAllianceLevel()-1];
			if(alliance.playersNum()>=limitNum)
			{
				throw new DataAccessException(0,"alliance is full");
			}
			//�ж������Ƿ����Զ�����  ���������Զ����� �����ϼ��������б�
			if(checkAllianceJoinSeeting(alliance,player))
			{
				alliance.getBoss().addBossAwardJoin(player.getId());
				alliance.addPlayer(player.getId());
				player.setAttribute(PublicConst.ALLIANCE_ID,alliance.getId()+"");
				player.setAttribute(PublicConst.ALLIANCE_JOIN_TIME,
					TimeKit.getSecondTime()+"");
				// �����¼�
				AllianceEvent event=new AllianceEvent(
					AllianceEvent.ALLIANCE_EVENT_AUTO_JOIN,null,
					player.getName(),"",TimeKit.getSecondTime());
				alliance.addEvent(event);
				alliance.addAllianceSkills(player);
				player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
				player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
				// ��ս���
				afightManager.jionAlliance(player,alliance.getId());
				alliance.setPlayerGiveValue(player,objectFactory);
				// �Ƴ����������ս��
				removeFightRank(player);
				// �㲥�������������
				data.clear();
				data.writeShort(ALLIANCE_PORT);
				data.writeByte(PLAYER_JOIN);
				data.writeInt(player.getId());
				data.writeUTF(player.getName());
				data.writeByte(player.getLevel());
				data.writeByte(Alliance.MILITARY_RANK1);
				data.writeInt(player.getFightScore());
				data.writeShort((TimeKit.getSecondTime()-player.getUpdateTime())
					/PublicConst.DAY_SEC);
				//�������а���Ϣ
				data.writeShort(0);
				data.writeShort(0);
				data.writeShort(0);
				data.writeInt(0);
				data.writeInt(0);
				sendAllAlliancePlayers(data,alliance);
				//�����������
				JBackKit.sendPlayerMaterialRank(alliance.getPlayerList(),
					objectFactory,player,0,Alliance.MILITARY_RANK1);
				data.clear();
				data.writeShort(ALLIANCE_PORT);
				data.writeByte(ALIIANCE_ADD);
				alliance.showBytesWrite(data,player,objectFactory);
				afightManager.showBytesWrite(alliance.getId(),data);
				player.delInvitedRecords();
				JBackKit.sendAllicace(data,player);
				JBackKit.sendAllianceBattleInfo(player,battleFigtmanager);
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.SOMETHING_ELSE);
//				throw new DataAccessException(0,"join the alliance success");
				data.clear();
				data.writeBoolean(true);
			}
			else
			{
				alliance.addApllication(player.getId());
				String appiacneName=player
					.getAttributes(PublicConst.ALLIANCE_APPLICATION);
				if(appiacneName!=null&&!appiacneName.equals(""))
				{
					// appiacneName+="%";
					// appiacneName+=alliance.getName();
					appiacneName=nameChaneId(appiacneName);
					appiacneName+="%"+alliance.getId();
				}
				else
				{
					// appiacneName="";
					// appiacneName+=alliance.getName();
					appiacneName=alliance.getId()+"";
				}
				player.setAttribute(PublicConst.ALLIANCE_APPLICATION,
					appiacneName);
				data.clear();
				data.writeShort(ALLIANCE_PORT);
				data.writeByte(ADD_REQUEST);
				data.writeInt(player.getId());
				data.writeUTF(player.getName());
				data.writeByte(player.getLevel());
				data.writeInt(player.getFightScore());
				sendAllAlliancePlayers(data,alliance);
				data.clear();
				data.writeBoolean(false);
			}
		}
		// ȡ�����빫��
		else if(type==CANCEL_APPLICATION_ALLIAN)
		{
			String name=data.readUTF();
			// ���˲�����
			Alliance alliance=objectFactory.getAllianceMemCache()
				.loadByName(name,true);
			if(alliance==null)
			{
				throw new DataAccessException(0,"alliance has been dismiss");
			}
			alliance.removeApllication(player.getId());
			String appiacneName=player
				.getAttributes(PublicConst.ALLIANCE_APPLICATION);
			if(appiacneName!=null&&!appiacneName.equals(""))
			{
				String appianceAfter=getPlayerAppInfo(appiacneName,alliance.getId());
				player.setAttribute(PublicConst.ALLIANCE_APPLICATION,
					appianceAfter);
			}
			else
			{
				player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
			}
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(REMOVE_REQUEST);
			data.writeUTF(player.getName());
			sendAllAlliancePlayers(data,alliance);
		}
		// �˳�����
		else if(type==EXIT_ALLIAN)
		{
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// ���˲�����
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						Integer.parseInt(allianceId)+"");
				if(alliance!=null)
				{
					// ����ǻ᳤�����˳�
					if(alliance.getMasterName(objectFactory)
						.equalsIgnoreCase(player.getName()))
					{
						throw new DataAccessException(0,"you can not exit");
					}
					// ��һ�첻���Ƴ�
					if(player.getAttributes(PublicConst.ALLIANCE_JOIN_TIME)!=null)
					{
						int time=Integer.parseInt(player
							.getAttributes(PublicConst.ALLIANCE_JOIN_TIME));
						int someDayEnd=SeaBackKit
							.getSomeDayEndTime(time*1000l);
						if(someDayEnd>TimeKit.getSecondTime())
						{
							throw new DataAccessException(0,
								"today not allow exit");
						}
					}
					// ���Э��
					if(checkDefense(player))
					{
						throw new DataAccessException(0,
							"deffense not allow exit");
					}
					if(checkLeaveAlliance(alliance,player.getId()))
					{
						throw new DataAccessException(0,
										"join alliancefight can not leave alliance");
					}
					alliance.clearAllianceDefence(player,objectFactory);
					alliance.removePlayerId(player.getId());
					player.setAttribute(PublicConst.ALLIANCE_ID,null);
					// ȥ�����˼���
					player.resetAdjustment();
					// ȥ��������������
					player.getAllianceList().clear();
					player.setAttribute(
						PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
					player.setAttribute(PublicConst.ALLIANCE_APPLICATION,
						null);
					// �����¼�
					AllianceEvent event=new AllianceEvent(
						AllianceEvent.ALLIANCE_EVENT_PLAYER_LEFT,
						player.getName(),"","",TimeKit.getSecondTime());
					alliance.addEvent(event);

					// ��ս���
					afightManager.exitAlliance(player,alliance.getId());
					//��¼��ҵľ�����Ϣ
					SeaBackKit.leaveAllianceRecord(player,alliance);
					//�Ƴ���ҵ�����������Ϣ
					SeaBackKit.removeAllianceRank(player,alliance);
					// �㲥�������������
					data.clear();
					data.writeShort(ALLIANCE_PORT);
					data.writeByte(SOME_ONE_EIXT);
					data.writeUTF(player.getName());
					sendAllAlliancePlayers(data,alliance);
					JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.SOMETHING_ELSE);
				}
			}
		}
		// ��ɢ����
		else if(type==DIS_ALLIAN)
		{
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			if(alliance.getMasterPlayerId()!=player.getId())
			{
				throw new DataAccessException(0,"you are not master");
			}
			if(checkDisAlliance(alliance))
			{
				throw new DataAccessException(0,"alliance  be in battlefight");
			}
			// ���첻�ܽ�ɢ����
			String ctStr=alliance
				.getAttributes(PublicConst.ALLIANCE_CREATE_TIME);
			long createTime=ctStr!=null?Long.parseLong(ctStr):0l;
			int endTime=SeaBackKit.getSomeDayEndTime(createTime*1000l);
			if(endTime>TimeKit.getSecondTime())
			{
				throw new DataAccessException(0,
					"alliance_create_time_to_short");
			}

			// ��ս���
			afightManager.dismissAlliance(alliance);
			//���õ�ǰ��ҵĹ��׵�
			setAllAllianceGiveValue(alliance);
			// �㲥�������������
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(DIS_MISS_ALLIANCE_PUSH);
			sendAllAlliancePlayers(data,alliance);
			//���ж����˷�����Ϣ
			sendHostileMessage(alliance);
			alliance.dismiss(objectFactory);
			flushFileds(true);
			allianceChestManager.dismissAlliance(alliance);
			data.clear();
		}
		// ��ȡ�Լ���������
		else if(type==GET_ALLIAN_SELF_RANK)
		{
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			int sortNum=10000;
			if(allianceId!=null&&!allianceId.equals(""))
			{
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						Integer.parseInt(allianceId)+"");
				if(alliance!=null)
				{
					flushAllianceRank(alliance);
					sortNum=alliance.getRankNum();
				}
				if(alliance!=null&&sortNum<=0)
				{
					alliance.setRankNum(MathKit.randomValue(101,140));
					sortNum=alliance.getRankNum();
				}
			}
			data.clear();
			data.writeShort(sortNum-1);
		}
		// �������
		else if(type==ALLIANCE_GIVE_VALUE)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.ALLIANCE_DONATE_LEVEL_LIMIT,player,
				"alliance_donate_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			// ����ĳ�����ܻ��߹���ȼ�*
			int skillSid=data.readUnsignedShort();
			// ��������
			int typeGive=data.readUnsignedByte();
			if(!(typeGive>=Resources.METAL&&typeGive<=Resources.GEMS))
			{
				throw new DataAccessException(0,"typeGive is wrong");
			}
			// �жϾ��������Ƿ�����
//			int num[]=player.todayAlliance();
			DonateRank rank=(DonateRank)alliance.getGiveValue().get(player.getId());
			if(rank==null) 
			{
				rank=new DonateRank();
				rank.setPlayerId(player.getId());
				alliance.getGiveValue().put(player.getId(),rank);
			}
			int[] num=rank.getDonaterecord();
			if(num[typeGive]>=PublicConst.ALLIANCE_MAX_VALUE)
			{
				throw new DataAccessException(0,"today typeGive is max");
			}
			// �����Դ
			int needResource[]=new int[6];
			needResource[typeGive]=PublicConst.ALLIANCE_RESOURCE_COST[num[typeGive]];
			int gemsOrResource=data.readUnsignedByte();
			// if(typeGive==Resources.GEMS)
			// {
			// needResource[typeGive]=num[typeGive]
			// *PublicConst.ALLIANCE_GIVE_GEMS
			// +PublicConst.ALLIANCE_GIVE_GEMS;
			// }
			if(gemsOrResource>0)
			{
				needResource[typeGive]=0;
				needResource[Resources.GEMS]=num[typeGive]
					*PublicConst.ALLIANCE_GIVE_GEMS
					+PublicConst.ALLIANCE_GIVE_GEMS;
			}
			if(!Resources.checkResources(needResource,player.getResources()))
			{
				throw new DataAccessException(0,"resource limit");
			}
			// �����ĵ���
			int point=num[typeGive]+1;
			// ���Ž���Ʒ
			int awardSid=PublicConst.ALLIANCE_AWARD_SID[num[typeGive]];
			Award award=(Award)Award.factory.getSample(awardSid);
			// ����0����������ȼ�
			if(skillSid==0)
			{
				if(alliance.getAllianceLevel()>=PublicConst.MAX_ALLIANCE_LEVEL)
				{
					throw new DataAccessException(0,"alliance level is max");
				}
				// ���Ӿ���
				int addLevel=alliance.incrExp(point);
				if(addLevel>0)
				{
					// �����¼�
					AllianceEvent event=new AllianceEvent(
						AllianceEvent.ALLIANCE_EVENT_LEVEL,"","",
						alliance.getAllianceLevel()+"",
						TimeKit.getSecondTime());
					alliance.addEvent(event);
					// ��ʾ
					String message=InterTransltor.getInstance()
						.getTransByKey(PublicConst.SERVER_LOCALE,
							"alliance_level_up");
					message=TextKit.replace(message,"%",alliance.getName());
					message=TextKit.replace(message,"%",
						alliance.getAllianceLevel()+"");
					SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
						message);
				}
				award.awardSelf(player,TimeKit.getSecondTime(),null,null,
					null,new int[]{EquipmentTrack.FROM_ALLIANCE});
				// ���Ӵ���
//				player.addGiveValueForAlliance(typeGive);
				// �㲥
				data.clear();
				data.writeShort(ALLIANCE_PORT);
				data.writeByte(EXP_LEVEL_CHANGE);
				data.writeInt(alliance.getAllianceExp());
				data.writeByte(alliance.getAllianceLevel());
				sendAllAlliancePlayers(data,alliance);
			}
			else
			{
				// �������˼���
				if(!isHaveSkillSid(skillSid,player,
					alliance.getAllianceLevel()))
				{
					throw new DataAccessException(0,"alliance level limit");
				}
				// �Ƿ��ܵȼ��������˵ȼ�
				if(alliance.getSkillExp(skillSid)[1]>=alliance
					.getAllianceLevel())
				{
					throw new DataAccessException(0,"alliance level limit");
				}
				// ���Ӿ���
				int addLevel=alliance.incrSkillExp(point,skillSid);
				award.awardSelf(player,TimeKit.getSecondTime(),null,null,
					null,new int[]{EquipmentTrack.FROM_ALLIANCE});
				if(addLevel>0)
				{
					int level[]=alliance.getSkillExp(skillSid);
					alliance.flushAllianceSkill(objectFactory);
					// �����¼�
					AllianceEvent event=new AllianceEvent(
						AllianceEvent.ALLIANCE_EVENT_SKILL_LEVEL,
						skillSid+"",skillSid+"",level[1]+"",
						TimeKit.getSecondTime());
					alliance.addEvent(event);
					JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.ALLIANCE_SKILL_UP);
				}
//				// ���Ӵ���
//				player.addGiveValueForAlliance(typeGive);
				// �㲥
				data.clear();
				data.writeShort(ALLIANCE_PORT);
				data.writeByte(SINGLE_SKILL_CHANGE);
				data.writeShort(skillSid);
				int exp[]=alliance.getSkillExp(skillSid);
				data.writeInt(exp[0]);
				data.writeByte(exp[1]);
				sendAllAlliancePlayers(data,alliance);
			}
			if(gemsOrResource>0)
			{
				Resources.reduceGems(needResource[Resources.GEMS],
					player.getResources(),player);
				// ����change��Ϣ
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,
					needResource[Resources.GEMS]);
			}
			else
			{
				// �۳���Դ
				Resources.reduceResources(player.getResources(),
					needResource,player);
			}

//			String alreadyGive=player
//				.getAttributes(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER);
//			int giveValue=0;
//			if(alreadyGive!=null&&!alreadyGive.equals(""))
//			{
//				giveValue+=Integer.parseInt(alreadyGive);
//			}
//			int before=giveValue/50;
//			// ���ӹ��׶�
//			giveValue+=(num[typeGive]+1);
			//�ɾ����ݲɼ�
			AchieveCollect.allianceOffer(num[typeGive]+1,player);
//			// ���׶���ʾ
//			player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,
//				giveValue+"");
			
			boolean flag=rank.addGiveValue(point,typeGive,player);
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(PLAYER_GIVE_VALUE);
			data.writeUTF(player.getName());
//			data.writeInt(Integer.parseInt(player.getAttributes(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER)));
			data.writeShort(rank.getDayValue());
			data.writeShort(rank.getWeekValue());
			data.writeShort(rank.getMouthValue());
			data.writeInt((int)rank.getTotleValue());
			sendAllAlliancePlayers(data,alliance);
//			int after=giveValue/50;
//			if(after>before)
			if(flag)
			{
				// �����¼�
				AllianceEvent event=new AllianceEvent(
					AllianceEvent.ALLIANCE_EVENT_PLAYER_CONTRI,
					player.getName(),"",rank.getTotleValue()+"",TimeKit.getSecondTime());
				alliance.addEvent(event);
			}
			data.clear();
			int todayNum[]=rank.getDonaterecord();
			// ���׺� ������˱������ ��һ~����
			int acCount=0;
			if(SeaBackKit.getDayOfWeek(TimeKit.getMillisTime())!=0)
			{
				allianceChestManager.checkPlayerChest(player);
				AllianceChest ac=player.getAllianceChest();
				if(ac==null)
					ac=new AllianceChest();
				int todayPoints=player.todayAlliancePoints(todayNum);
				int pointLv=ac.getGiveLv();
				float nowLv=(float)(todayPoints-PublicConst.ALLIANCE_CHEST_COUNT_BASE)
					/PublicConst.ALLIANCE_CHEST_COUNT_COEF;
				if(nowLv<0)
					ac.setGiveLv(-1);
				if(nowLv>=(pointLv+1))
				{
					synchronized(ac)
					{
						ac.setGiveLv((int)nowLv);
						ac.setCount(ac.getCount()+1);
					}
				}
				acCount=ac.getCount();
			}
			data.writeByte(typeGive);
			data.writeByte(todayNum[typeGive]);
			//����������˱������
			data.writeByte(acCount);
			// ��ʯ��־
			if(gemsOrResource>0)
			{
				// ��ʯ��־��¼
				objectFactory.createGemTrack(GemsTrack.ALLIANCE_GIVE,
					player.getId(),needResource[Resources.GEMS],alliance.getId(),
					Resources.getGems(player.getResources()));
			}
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ALLIANCE_GIVE_TASK_EVENT,null,player,null);
		}
		// �޸������͹���
		else if(type==CHANGE_DES_AND_AN)
		{
			String des=data.readUTF();
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			alliance.setDescription(des);
			des=data.readUTF();
			alliance.setAnnouncement(des);
			// �����¼�
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_ANNOUCE,player.getName(),
				player.getName(),"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			// �㲥�������������
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(NOTICE_CHANGE);
			data.writeUTF(alliance.getDescription());
			data.writeUTF(alliance.getAnnouncement());
			sendAllAlliancePlayers(data,alliance);
		}
		// ��ȡ�¼�
		else if(type==GET_ALL_ALLIANCE_EVENT)
		{
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			// ��ҳ ÿҳ20��
			int pageIndex=data.readUnsignedByte();
			// ֧��ǰ400��������
			if(pageIndex>MAX_EVENT_SIZE||pageIndex<=0)
			{
				throw new DataAccessException(0,"pageIndex is wrong");
			}
			data.clear();
			int num=alliance.getEventList().size()-(pageIndex-1)
				*MAX_EVENT_SIZE;
			if(num<0) num=0;
			if(num>MAX_EVENT_SIZE) num=MAX_EVENT_SIZE;
			// ������ҳ��
			data.writeByte(alliance.getEventPageSize());
			data.writeByte(num);
			for(int i=(pageIndex-1)*MAX_EVENT_SIZE;i<pageIndex
				*MAX_EVENT_SIZE;i++)
			{
				if(i>=alliance.getEventList().size()) break;
				AllianceEvent event=alliance.getEventList().get(i);
				event.showBytesWrite(data);
			}
		}
		// ͬ��ĳ����ҽ���
		else if(type==MASTER_ARGEE_PLAYERS)
		{
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			String playerName=data.readUTF();
			Player appplayer=objectFactory.getPlayerCache().loadByName(
				playerName,true);
			if(appplayer==null || TextKit.parseInt(player
				.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE)
			{
				throw new DataAccessException(0,"player is not exist");
			}
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.JOIN_ALLIANCE_LEVEL_LIMIT,appplayer,
				"dest_join_alliance_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			if(!alliance.isApplication(appplayer.getId()))
			{
				throw new DataAccessException(0,
					"player is cancel application");
			}
			// ������������
			int limitNum=PublicConst.ALLIANCE_LEVEL_NUMS[alliance
				.getAllianceLevel()-1];
			if(alliance.playersNum()>=limitNum)
			{
				throw new DataAccessException(0,"alliance is full not join");
			}
			if(appplayer.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!appplayer.getAttributes(PublicConst.ALLIANCE_ID).equals(
					""))
			{
				// �Ƿ���������
				Alliance haveAlliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						appplayer.getAttributes(PublicConst.ALLIANCE_ID));
				if(haveAlliance!=null)
				{
					alliance.removeApllication(appplayer.getId());
					throw new DataAccessException(0,
						"player is have alliance");
				}
			}
			alliance.getBoss().addBossAwardJoin(appplayer.getId());
			alliance.getApplicationList().remove(appplayer.getId());
			alliance.addPlayer(appplayer.getId());
			appplayer.setAttribute(PublicConst.ALLIANCE_ID,alliance.getId()
				+"");
			appplayer.setAttribute(PublicConst.ALLIANCE_JOIN_TIME,
				TimeKit.getSecondTime()+"");
			// �����¼�
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_NEW_PLAYER,player.getName(),
				appplayer.getName(),"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			alliance.addAllianceSkills(appplayer);
			appplayer.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,
				null);
			appplayer.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);

			// ��ս���
			afightManager.jionAlliance(appplayer,alliance.getId());
			alliance.setPlayerGiveValue(appplayer,objectFactory);
			//
			removeFightRank(appplayer);
			
			// �㲥�������������
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(PLAYER_JOIN);
			data.writeInt(appplayer.getId());
			data.writeUTF(appplayer.getName());
			data.writeByte(appplayer.getLevel());
			data.writeByte(Alliance.MILITARY_RANK1);
			data.writeInt(appplayer.getFightScore());
			data.writeShort((TimeKit.getSecondTime()-appplayer
				.getUpdateTime())/PublicConst.DAY_SEC);
			//�������а���Ϣ
			data.writeShort(0);
			data.writeShort(0);
			data.writeShort(0);
			data.writeInt(0);
			data.writeInt(0);
			//�����������
			JBackKit.sendPlayerMaterialRank(alliance.getPlayerList(),
				objectFactory,player,0,Alliance.MILITARY_RANK1);
			sendAllAlliancePlayers(data,alliance);
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(ALIIANCE_ADD);
			alliance.showBytesWrite(data,appplayer,objectFactory);
			afightManager.showBytesWrite(alliance.getId(),data);
			appplayer.delInvitedRecords();
			JBackKit.sendAllicace(data,appplayer);
			JBackKit.sendAllianceBattleInfo(appplayer,battleFigtmanager);
			JBackKit.sendFightScore(appplayer,objectFactory,true,FightScoreConst.SOMETHING_ELSE);
		}
		// ȡ������������
		else if(type==CANCEL_ALL_APPAICATION)
		{
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			IntList list=alliance.getApplicationList();
			for(int i=0;i<list.size();i++)
			{
				Player appPlayer=objectFactory.getPlayerCache().load(
					list.get(i)+"");
				if(appPlayer!=null)
				{
					// �����ʼ�
					String content=InterTransltor.getInstance()
						.getTransByKey(appPlayer.getLocale(),
							"alliance_refuse_content");
					content=TextKit.replace(content,"%",player.getName());
					MessageKit.sendSystemMessages(appPlayer,objectFactory,
						content);
					// appPlayer.setAttribute(PublicConst.ALLIANCE_APPLICATION,
					// null);
					String appiacneName=appPlayer
						.getAttributes(PublicConst.ALLIANCE_APPLICATION);
					String appianceAfter="";
					if(appiacneName!=null&&!appiacneName.equals(""))
						 appianceAfter=getPlayerAppInfo(appiacneName,alliance.getId());
					appPlayer.setAttribute(PublicConst.ALLIANCE_APPLICATION,
						appianceAfter);
				}
			}
			alliance.getApplicationList().clear();
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(CANCEL_ALL_APPCAITON);
			sendAllAlliancePlayers(data,alliance);
		}
		// �ߵ�ĳ�����
		else if(type==KICK_PLAYER)
		{
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			String name=data.readUTF();
			Player kickPlayer=objectFactory.getPlayerByName(name,true);
			if(kickPlayer==null)
			{
				throw new DataAccessException(0,"player is not exist");
			}
			if(alliance.getMasterPlayerId()==kickPlayer.getId())
			{
				throw new DataAccessException(0,"you can not kick master");
			}
			if(!alliance.isHavePlayer(kickPlayer.getId()))
			{
				throw new DataAccessException(0,"player is not in alliance");
			}
			if(checkLeaveAlliance(alliance,kickPlayer.getId()))
			{
				throw new DataAccessException(0,
								"join alliancefight can not leave alliance");
			}
			// // ��һ�첻���߳�
			if(kickPlayer.getAttributes(PublicConst.ALLIANCE_JOIN_TIME)!=null)
			{
				int time=Integer.parseInt(kickPlayer
					.getAttributes(PublicConst.ALLIANCE_JOIN_TIME));
				int someDayEnd=SeaBackKit.getSomeDayEndTime(time*1000l);
				if(someDayEnd>TimeKit.getSecondTime())
				{
					throw new DataAccessException(0,"today not allow kick");
				}
			}
			// �����¼�
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_KICK_PLAYER,player.getName(),
				kickPlayer.getName(),"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			String content=InterTransltor.getInstance().getTransByKey(
				kickPlayer.getLocale(),"alliance_be_kick_content");
			content=TextKit.replace(content,"%",player.getName());
			MessageKit.sendSystemMessages(kickPlayer,objectFactory,content);

			// ��ս���
			afightManager.exitAlliance(kickPlayer,alliance.getId());
			//��¼��ҵľ�����Ϣ
			SeaBackKit.leaveAllianceRecord(kickPlayer,alliance);
			//�Ƴ���ҵ�����������Ϣ
			SeaBackKit.removeAllianceRank(kickPlayer,alliance);
			// �㲥�������������
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(KICK_OUT);
			data.writeUTF(kickPlayer.getName());
			sendAllAlliancePlayers(data,alliance);
			alliance.clearAllianceDefence(kickPlayer,objectFactory);
			alliance.removePlayerId(kickPlayer.getId());
			kickPlayer.setAttribute(PublicConst.ALLIANCE_ID,null);
			kickPlayer.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,
				null);
			kickPlayer.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
			kickPlayer.getAllianceList().clear();
			kickPlayer.resetAdjustment();
			JBackKit.sendFightScore(kickPlayer,objectFactory,true,FightScoreConst.SOMETHING_ELSE);
		}
		// ��ȡ����������
		else if(type==GET_ALL_APPAICATION)
		{
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			IntList list=alliance.getApplicationList();
			data.clear();
			data.writeByte(list.size());
			Player appPlayer=null;
			for(int i=0;i<list.size();i++)
			{
				appPlayer=objectFactory.getPlayerById(list.get(i));
				if(appPlayer!=null)
				{
					data.writeUTF(appPlayer.getName());
					data.writeByte(appPlayer.getLevel());
					data.writeInt(appPlayer.getFightScore());
				}
			}
		}
		// ����ĳ�����Ϊ���᳤
		else if(type==CHANGE_PLAYER_DUTY)
		{
			String name=data.readUTF();
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			if(alliance.getMasterPlayerId()!=player.getId())
			{
				throw new DataAccessException(0,"you are not master");
			}
			// �鿴ĳ����Ƿ�����������
			Player bePlayer=objectFactory.getPlayerCache().loadByName(name,
				true);
			if(bePlayer==null)
			{
				throw new DataAccessException(0,"player is not exist");
			}
			if(!alliance.inAlliance(bePlayer.getId()))
			{
				throw new DataAccessException(0,"player is not in alliance");
			}
			int rank=data.readUnsignedByte();
			int playerRank=Alliance.MILITARY_RANK3;
			alliance.removeVicePlayer(bePlayer.getId());
			// �ƽ��᳤
			if(rank==Alliance.MILITARY_RANK3)
			{
				alliance.setMasterPlayerId(bePlayer.getId());
				alliance.addVicePlayer(player.getId());
				playerRank=Alliance.MILITARY_RANK2;
				autoTransfer.updateAutoAlliance(alliance);
			}
			else if(rank==Alliance.MILITARY_RANK2)
			{
				alliance.addVicePlayer(bePlayer.getId());
			}
			// �����¼�
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_MASTER_CHANGE,player.getName(),
				bePlayer.getName(),rank+"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			// �㲥�������������
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(RIGHT_CHANGE);
			data.writeUTF(bePlayer.getName());
			data.writeByte(rank);
			sendAllAlliancePlayers(data,alliance);
			// ����Ȩ�޵Ĺ㲥
			if(playerRank!=Alliance.MILITARY_RANK3)
			{
				data.clear();
				data.writeShort(ALLIANCE_PORT);
				data.writeByte(RIGHT_CHANGE);
				data.writeUTF(player.getName());
				data.writeByte(playerRank);
				sendAllAlliancePlayers(data,alliance);
			}
		}
		// �ܾ�ĳ��������
		else if(type==REFUSE_ONE_APPCATION)
		{
			String name=data.readUTF();
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			Player appPlayer=objectFactory.getPlayerByName(name,true);
			if(appPlayer==null)
			{
				throw new DataAccessException(0,"player is not exist");
			}
			String content=InterTransltor.getInstance().getTransByKey(
				appPlayer.getLocale(),"alliance_refuse_content");
			content=TextKit.replace(content,"%",player.getName());
			MessageKit.sendSystemMessages(appPlayer,objectFactory,content);
			alliance.removeApllication(appPlayer.getId());
			String appiacneName=appPlayer
				.getAttributes(PublicConst.ALLIANCE_APPLICATION);
			String appianceAfter="";
			if(appiacneName!=null&&!appiacneName.equals(""))
					getPlayerAppInfo(appiacneName,alliance.getId());
			appPlayer.setAttribute(PublicConst.ALLIANCE_APPLICATION,
				appianceAfter);
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(REMOVE_REQUEST);
			data.writeUTF(appPlayer.getName());
			sendAllAlliancePlayers(data,alliance);
		}
		// ��ȡ���˸���ս������ 
		else if(type==GET_FIGHT_RANK)
		{
			if(TimeKit.getSecondTime()>flushTime+FLUSH_TIME||personFight.size()<=FIGHT_SIZE/2)
			{
				flushPersonFight();
				flushTime=TimeKit.getSecondTime();
			}
			int page=data.readUnsignedByte()-1;
			int start=page*PAGE_SIZE;
			int end=start+PAGE_SIZE>personFight.size()?personFight.size():start+PAGE_SIZE;
			int len=end-start;
			if(len<0)len=0;
			data.clear();
			int pageMax=personFight.size()%PAGE_SIZE==0?personFight.size()/PAGE_SIZE:personFight.size()/PAGE_SIZE+1;
			data.writeByte(pageMax);
			data.writeByte(len);
			for(int i=start;i<start+len;i++)
			{
				RankInfo rank=(RankInfo)personFight.get(i);
				data.writeUTF(rank.getPlayerName());
				data.writeByte(i+1);
				data.writeShort(rank.getPlayerLevel());
				data.writeInt(rank.getRankInfo());
			}
		}
		// ������Ҽ���
		else if(type==INVITE_PLAYER)
		{
			String name=data.readUTF();
			int time=TimeKit.getSecondTime();
			data.clear();
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			Player appPlayer=objectFactory.getPlayerByName(name,false);
			boolean isInvitable=true;
			if(appPlayer==null)
			{
				isInvitable=false;
				//throw new DataAccessException(0,"player is not exist");
			}else{
				String checkString=SeaBackKit.checkChatOpen(
					ContextVarManager.JOIN_ALLIANCE_LEVEL_LIMIT,appPlayer,
					"dest_join_alliance_level_limit");
				if(checkString!=null)
					throw new DataAccessException(0,checkString);
				String allianceId=appPlayer.getAttributes(PublicConst.ALLIANCE_ID);
				if(allianceId!=null&&!"".equals(allianceId)){
					isInvitable=false;
				}else{
					appPlayer.addInvitedRecord(alliance.getId(),player.getId(),
						time);
					JBackKit.sendAllianceInvitation(appPlayer);
				}
			}
			data.writeBoolean(isInvitable);
		}
		// ��ȡ����������Ϣ
		else if(type==ALLIANCE_INVITATION)
		{
			// �õ�ҳ��
			int page=data.readInt();
			data.clear();
			// ����������б�õ������ַ���
			String invitationStr=player
				.getAttributes(PublicConst.ALLIANCE_INVITATION_RECORD);
			if(invitationStr!=null&&!invitationStr.equals(""))
			{
				// �ָ������ַ�����ȡ�����¼
				String[] invitations=invitationStr.split(",");
				// ��ȡ��ҳ��
				int invitationLen=invitations.length;
				int pages=invitationLen
					/PublicConst.DEFAOULT_ALLIANCE_SIZE;
				if(pages*PublicConst.DEFAOULT_ALLIANCE_SIZE<invitationLen)
				{
					// �����¼����һҳ�Ĳ�һҳ
					pages++;
				}
				
				int start=(page-1)*PublicConst.DEFAOULT_ALLIANCE_SIZE;
				start=start>=invitationLen?invitationLen:start;
				int len=0;
				int top=data.top();
				data.writeInt(len);
				data.writeInt(pages);
				for(int i=start;i<invitationLen&&len<PublicConst.DEFAOULT_ALLIANCE_SIZE;i++)
				{
					String[] infos=invitations[i].split(":");
					Alliance alliance=(Alliance)objectFactory
						.getAllianceMemCache().loadOnly(infos[0]);
					if(alliance!=null)
					{
						data.writeInt(alliance.getAllianceLevel());
						data.writeUTF(alliance.getName());
						String tempName="";
						Player tempPlayer=objectFactory.getPlayerById(TextKit.parseInt(infos[1]));
						if(tempPlayer!=null)
							tempName=tempPlayer.getName();
						data.writeUTF(tempName);
						int seconds=TimeKit.getSecondTime()
							-Integer.valueOf(infos[2]).intValue();
						data.writeInt(seconds);
						len++;
					}
					else
					{
						player.delInvitedRecord(TextKit.parseInt(infos[0]));
					}
				}
				int newTop=data.top();
				data.setTop(top);
				data.writeInt(len);
				data.setTop(newTop);
			}
			else
			{
				data.writeInt(0);
				data.writeInt(1);
			}

		}
		// ������������
		else if(type==ALLIANCE_INVITATION_ACCEPT)
		{
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
				throw new DataAccessException(0,"player is have alliance");
			String allianceName=data.readUTF();
			data.clear();
			int result=0;
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadByName(allianceName,true);
			if(alliance==null)
			{
				data.writeByte(2);
				return data;
				// throw new DataAccessException(0,"alliance is no exist");
			}
			// ������������
			int limitNum=PublicConst.ALLIANCE_LEVEL_NUMS[alliance
				.getAllianceLevel()-1];
			if(alliance.playersNum()>=limitNum)
			{
				player.delInvitedRecord(alliance.getId());
				data.writeByte(1);
				return data;
				// throw new
				// DataAccessException(0,"alliance is full not join");
			}
			String pid=player.getAlliancePlayer(alliance.getId());
			if(pid==null || pid.length()==0)
				pid=alliance.getMasterPlayerId()+"";
			Player appplayer=objectFactory.getPlayerById(TextKit.parseInt(pid));
			alliance.getBoss().addBossAwardJoin(player.getId());

			alliance.addPlayer(player.getId());
			player.setAttribute(PublicConst.ALLIANCE_ID,alliance.getId()+"");
			player.setAttribute(PublicConst.ALLIANCE_JOIN_TIME,
				TimeKit.getSecondTime()+"");
			// �����¼�
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_NEW_PLAYER,appplayer.getName(),
				player.getName(),"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			alliance.addAllianceSkills(player);
			player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
			player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);

			// ��ս���
			afightManager.jionAlliance(player,alliance.getId());
			alliance.setPlayerGiveValue(player,objectFactory);
			
			// �㲥�������������
			
			// ǰ��Э���ƶ�////////////////////////////////
			data.writeShort(AlliancePort.ALLIANCE_PORT);
			data.writeByte(AlliancePort.PLAYER_JOIN);
			data.writeInt(player.getId());
			data.writeUTF(player.getName());
			data.writeByte(player.getLevel());
			data.writeByte(Alliance.MILITARY_RANK1);
			data.writeInt(player.getFightScore());
			data.writeShort((TimeKit.getSecondTime()-player.getUpdateTime())
				/PublicConst.DAY_SEC);
			data.writeShort(0);
			data.writeShort(0);
			data.writeShort(0);
			data.writeInt(0);
			data.writeInt(0);
			sendAllAlliancePlayers(data,alliance);
			//�����������
			JBackKit.sendPlayerMaterialRank(alliance.getPlayerList(),
				objectFactory,player,0,Alliance.MILITARY_RANK1);
			data.clear();
			data.writeShort(AlliancePort.ALLIANCE_PORT);
			data.writeByte(AlliancePort.ALIIANCE_ADD);
			alliance.showBytesWrite(data,player,objectFactory);
			afightManager.showBytesWrite(alliance.getId(),data);
			JBackKit.sendAllicace(data,player);
			JBackKit.sendAllianceBattleInfo(player,battleFigtmanager);
			// ǰ��Э���ƶ�/////////////////////
			data.clear();
			data.writeByte(result);
			player.delInvitedRecords();
			JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.SOMETHING_ELSE);
		}
		// �ܾ���������
		else if(type==ALLIANCE_INVITATION_REFUSE)
		{
			String allianceName=data.readUTF();
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadByName(allianceName,false);
			if(alliance!=null) player.delInvitedRecord(alliance.getId());
			data.clear();
		}
		//���õж��¼�
		else if(type==SET_HOSTILE)
		{
			Alliance myalliance=(Alliance)objectFactory
							.getAllianceMemCache().load(
								player.getAttributes(PublicConst.ALLIANCE_ID));
			if(myalliance.getMasterPlayerId()!=player.getId())
				throw new DataAccessException(0,"you are not master");
			String aName=data.readUTF();
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadByName(aName,false);
			if(alliance==null)
				throw new DataAccessException(0,"alliance is null");
			if(myalliance.getId()==alliance.getId())
				throw new DataAccessException(0,"can not set own alliance");
			if(SeaBackKit.isHostile(myalliance,alliance))
				throw new DataAccessException(0,
					"Hostile alliance already exists");
			if(myalliance.getHostile()!=null
				&& myalliance.getHostile().split(",").length>=Alliance.HOSTILE_LENGTH)
				throw new DataAccessException(0,
					"hostile Alliance has aleady max length");
			myalliance.addHostile(alliance.getId());
			data.clear();
			data.writeInt(alliance.getId());
			data.writeUTF(alliance.getName());
			data.writeByte(alliance.playersNum());
			data.writeInt(alliance.getAllFightScore());
			alliance.getFlag().showBytesWriteAllianceFlag(data);
		}
		/**ȡ������**/
		else if(type==DIS_HOSTILE)
		{
			
			Alliance myalliance=(Alliance)objectFactory
							.getAllianceMemCache().load(
								player.getAttributes(PublicConst.ALLIANCE_ID));
			if(myalliance.getMasterPlayerId()!=player.getId())
				throw new DataAccessException(0,"you are not master");
			int aid=data.readInt();
			Alliance alliance=objectFactory.getAlliance(aid,false);
			if(SeaBackKit.isHostile(myalliance,alliance))
			{
				myalliance.removeHostile(aid);
			}
			data.clear();
			data.writeInt(aid);
		}
		// ��������
		else if(type==ALIANCE_JOIN_SET)
		{
			String str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			int autoJoin=data.readByte();
			int joinPlayerLevel=data.readShort();
			int joinFightScore=data.readInt();
			boolean isjoinPlayerLevel=data.readBoolean();
			boolean isjoinPlayerScore=data.readBoolean();
			alliance.setAutoJoin(autoJoin);
			alliance.setJoinFightScore(joinFightScore);
			alliance.setJoinPlayerLevel(joinPlayerLevel);
			alliance.setAttribute(PublicConst.ALLIANCE_JOIN_LEVEL_BOOL,
				Boolean.toString(isjoinPlayerLevel));
			alliance.setAttribute(PublicConst.ALLIANCE_JOIN_SCORE_BOOL,
				Boolean.toString(isjoinPlayerScore));

			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(JOIN_ALLIANCE_SETTING);
			data.writeByte(autoJoin);
			data.writeShort(joinPlayerLevel);
			data.writeInt(joinFightScore);
			data.writeBoolean(isjoinPlayerLevel);
			data.writeBoolean(isjoinPlayerScore);
			sendAllAlliancePlayers(data,alliance);
		}
		// ���˱���
		else if(type==ALLIANCE_CHEST)
		{
			//TODO
			Alliance alliance=(Alliance)objectFactory
							.getAllianceMemCache().load(
								player.getAttributes(PublicConst.ALLIANCE_ID));
			AllianceChest ac=player.getAllianceChest();
			if(ac==null)
				ac=new AllianceChest();
			int dayOfWeek=SeaBackKit.getDayOfWeek(TimeKit.getMillisTime());
			int subType=data.readByte();
			int freeCount=ac.getFreeCount();
			int count=ac.getCount();
			int level=ac.getChestLv();
			int lastDay=ac.getLastDay();
			int nowDay=SeaBackKit.getDayOfYear();
			int nowLv=ac.getGiveLv();
			if(lastDay!=nowDay)
			{
				freeCount=1;
				count=0;
				lastDay=nowDay;
				nowLv=-1;
			}
			// 0 �鿴
			if(subType==0)
			{
				//��һ���ø����� 0����
				if(dayOfWeek==0)
				{
					if(isResetLuckyPoint)
					{
						isResetLuckyPoint=false;
					}
				}
				else
				{
					if(!isResetLuckyPoint)
					{
						isResetLuckyPoint=true;
						// ����һ�����������˵���
						allianceChestManager.resetLuckyPoint();
						// ��������������˱����콱����
						allianceChestManager.resetPlayerLucyAwardCount();
						// �������а�
						allianceChestManager.resetRank();
					}
				}
				//��������
				data.clear();
				//�����Ϣ
				ac.showBytesWrite(data);
				//���а���Ϣ
//				allianceChestManager.showBytesWrite(data,alliance.getId());
				//���˱��佱��
//				allianceChestManager.showBytesWriteChestAwards(data,player);
				//���˻��ֽ���
				allianceChestManager.showBytesWriteLuckyPointAwards(data,player);
			}
			// 1����齱
			else if(subType==1)
			{
				if(SeaBackKit.getDayOfWeek(TimeKit.getMillisTime())==0)
					throw new DataAccessException(0,
						"cannot get adward on sunday");
				String str=checkAlliance(player);
				if(str!=null) throw new DataAccessException(0,str);
				// 24Сʱ�����˲����콱
				int joinTime=Integer.parseInt(player
					.getAttributes(PublicConst.ALLIANCE_JOIN_TIME));
				if(joinTime>(TimeKit.getSecondTime()-24*3600))
					throw new DataAccessException(0,"join time need greater than 24h");
				if(freeCount>0)
					freeCount=0;
				else
				{
					if(count>0)
						count--;
					else
						throw new DataAccessException(0,"you have no number");
				}
				data.clear();
				//�콱ǰ����ȼ�
				data.writeByte(level);
				// �����ȡ�ȼ�Ϊlevel�ı��� ��������Ϊ1
				data.writeByte(1);
				Award aw=(Award)Award.factory
					.newSample(PublicConst.ALLIANCE_CHEST_AWARD[level]);
				aw.awardSelf(player,TimeKit.getSecondTime(),data,
					objectFactory,null,
					new int[]{EquipmentTrack.ALLIANCE_CHEST_AWARD});
				//����Ƿ�Ϊ���˻��ֵ���
				if(allianceChestManager.isLuckyAward(data))
				{
					alliance.setLuckyCreateAt(TimeKit.getSecondTime());
					data.writeBoolean(true);
					//ɾ���������˻��ֵ���
					int num=player.getBundle().getCountBySid(PublicConst.ALLIANCE_CHEST_SID);
					player.getBundle().decrProp(PublicConst.ALLIANCE_CHEST_SID,num);
					//ˢ��ǰ̨����
					JBackKit.sendResetBunld(player);
					alliance.addLuckyPoints();
					// ˢ�����а�
					allianceChestManager.flushRank(alliance);
					// ϵͳͨ�� XX(����)XX(����)�����˱����г�ȡ��"�������˵���"XX(����)���˻���+1
					String message=InterTransltor.getInstance()
						.getTransByKey(PublicConst.SERVER_LOCALE,
							"alliance_lucky_points");
					message=TextKit.replace(message,"%",alliance.getName());
					message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
					message=TextKit.replace(message,"%",alliance.getName());
					SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
						message);
				}
				else
					data.writeBoolean(false);
				// ��������
				float random=MathKit.randomValue(0.0f,1.0f);
				if(random<PublicConst.ALLIANCE_CHEST_UPGRADE_ODDS&&level<3)
					level++;
				else
					level=0;
				data.writeByte(freeCount);
				data.writeShort(count);
				data.writeByte(level);
			}
			//��ȡ���а���
			else if(subType==2)
			{
				// ֻ���������콱
				if(dayOfWeek!=0)
					throw new DataAccessException(0,
						"can only accept the prize on sunday");
				String msg=allianceChestManager.checkChestAward(player,ac);
				if(msg!=null)throw new DataAccessException(0,msg);
				int awardLevel=allianceChestManager.getRankAwardLv(alliance.getId());
				Award aw=(Award)Award.factory
					.newSample(PublicConst.ALLIANCE_LUCKY_POINT_AWARD[awardLevel]);
				aw.awardSelf(player,TimeKit.getSecondTime(),data,
					objectFactory,null,
					new int[]{EquipmentTrack.ALLIANCE_LUCKY_POINT_AWARD});
				data.clear();
				aw.viewAward(data,player);
				ac.setLuckyCount(0);
				player.setAllianceChest(ac);
			}
			//�����������
			else if(subType==3)
			{
				data.clear();
				//���а���Ϣ
				allianceChestManager.showBytesWrite(data,alliance.getId(),ac);
			}
			ac.setFreeCount(freeCount);
			ac.setCount(count);
			ac.setChestLv(level);
			ac.setLastDay(lastDay);
			ac.setGiveLv(nowLv);
			player.setAllianceChest(ac);
		}
		/**������������**/
		else if(type==SET_ALLIANCEFLAG)
		{
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			if(alliance==null)
				throw new DataAccessException(0,"alliance is null");
			if(!alliance.isMaster(player.getId()))
				throw new DataAccessException(0,"not master");
			if(!alliance.getFlag().checkFreeTimes(1)
				&&!Resources.checkGems(AllianceFlag.FLAG_COST_GEMS,
					player.getResources()))
				throw new DataAccessException(0,"gems limit");
			/** ����ͼ�� **/
			int igSid=data.readUnsignedShort();
			/** ������ɫ **/
			int colour=data.readUnsignedShort();
			/** �������� **/
			int model=data.readUnsignedShort();
			String result=alliance.getFlag().checkAllianceFlag(igSid,colour,model);
			if(result!=null) throw new DataAccessException(0,result);
			String str=SeaBackKit.checkFlagSid(igSid,colour,model);
			if(str!=null) throw new DataAccessException(0,str);
			if(alliance.getFlag().checkFreeTimes(1))
			{
				alliance.getFlag().reduceFreeTime(1);
			}
			else
			{
				Resources.reduceGems(AllianceFlag.FLAG_COST_GEMS,
					player.getResources(),player);
				objectFactory.createGemTrack(GemsTrack.BUY_ALLIANCE_FLAG,
					player.getId(),AllianceFlag.FLAG_COST_GEMS,0,
					Resources.getGems(player.getResources()));
				// ����change��Ϣ
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,null,player,
					AllianceFlag.FLAG_COST_GEMS);
			}
			log.error("===modifyAllianceFlag===id:"+alliance.getId()
				+"=Name:"+alliance.getName()+"=info:"
				+alliance.getFlag().getAllianceFlagToString());
			alliance.getFlag().setAllianceFlag(igSid,colour,model);
			log.error("===modifyAllianceFlag===id:"+alliance.getId()
				+"=Name:"+alliance.getName()+"=info:"
				+alliance.getFlag().getAllianceFlagToString());
			/** ��ȫ���˵���ˢ���������� **/
			JBackKit.sendAllianceFlag(alliance,objectFactory);
		}
		return data;
	}


	/** ���˳����ж� */
	public String checkAlliance(Player player)
	{
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(allianceId==null||allianceId.equals(""))
		{
			return "you have no alliance";
		}
		// ���˲�����
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.load(allianceId);
		if(alliance==null)
		{
			return "alliance has been dismiss";
		}
		return null;
	}
	
	/** ���˹㲥 */
	public void sendAllAlliancePlayers(ByteBuffer data,Alliance alliance)
	{
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=objectFactory.getPlayerById(list.get(i));
			if(player==null) continue;
			Session sessions=(Session)player.getSource();
			if(sessions==null) continue;
			Connect con=sessions.getConnect();
			if(con!=null&&con.isActive()) con.send(data);
		}
	}

	/** ������ҳ�� */
	public int getPage()
	{
		if(alliances.size()%AlliancePort.PAGE_SIZE==0)
		{
			return alliances.size()/AlliancePort.PAGE_SIZE;
		}
		else
		{
			return alliances.size()/AlliancePort.PAGE_SIZE+1;
		}
	}

	/** �Ƿ�������ĳ������ */
	public static boolean isHaveSkillSid(int skillSid,Player player,
		int allianceLevel)
	{
		for(int i=0;i<PublicConst.ALLIANCE_LEVEL_OPEN_SKILL.length;i++)
		{
			String skillSids[]=TextKit.split(
				PublicConst.ALLIANCE_LEVEL_OPEN_SKILL[i],":");
			if(allianceLevel<Integer.parseInt(skillSids[0])) break;
			for(int j=1;j<skillSids.length;j++)
			{
				if(skillSid==Integer.parseInt(skillSids[j]))
				{
					return true;
				}
			}
		}
		return false;
	}

	/** ����Ȩ���ж� */
	public String checkAllianceMaster(Player player)
	{
		// ���˲�����
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.load(player.getAttributes(PublicConst.ALLIANCE_ID));
		// �Ƿ��ǻ᳤�򸱻᳤
		if(!alliance.isMaster(player.getId()))
		{
			throw new DataAccessException(0,"you are not master");
		}
		return null;
	}

	/** ˢ���Լ������� */
	public void flushAllianceRank(Alliance alliance)
	{
		if(alliance==null) return;
		alliance.pushAllFightScore(objectFactory);
		// int nowTime=TimeKit.getSecondTime();
		// if(alliance.getRankTime()>nowTime) return;
		// alliance.setRankTime(nowTime+FLUSH_TIME);
		// String sql="SELECT COUNT(*) FROM alliances WHERE allFightScore>"
		// +alliance.getAllFightScore();
		// Fields field=objectFactory.getAllianceMemCache().getDbaccess()
		// .loadSql(sql);
		// int rankNum=0;
		// if(field!=null)
		// rankNum=Integer.parseInt(field.getArray()[0].getValue()
		// .toString());
		// alliance.setRankNum(rankNum);
	}

	public void flushFileds(boolean bool)
	{
		int nowTime=TimeKit.getSecondTime();
		if(lastTime>nowTime&&!bool) return;
		lastTime=nowTime+FLUSH_TIME;
		String sql="SELECT name,allianceLevel,allFightScore FROM alliances ORDER BY allFightScore DESC LIMIT 100";
		Fields fields[]=((PlayerGameDBAccess)objectFactory.getPlayerCache()
			.getDbaccess()).loadSqls(sql);
		alliances.clear();
		if(fields==null||fields.length<=0) return;
		for(int i=0;i<fields.length;i++)
		{
			RankInfo rank=new RankInfo(fields[i].getArray()[0].getValue()
				.toString(),Integer.parseInt(fields[i].getArray()[1]
				.getValue().toString()),Integer.parseInt(fields[i]
				.getArray()[2].getValue().toString()));
			alliances.add(rank);
			Alliance alliance=objectFactory.getAllianceMemCache()
				.loadByName(rank.getPlayerName(),true);
			if(alliance!=null) alliance.setRankNum(i+1);
		}
	}

	/** �����������Э�� */
	public boolean checkDefense(Player player)
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
			if(event.getType()==FightEvent.ATTACK_HOLD
				&&event.getEventState()!=FightEvent.RETRUN_BACK)
			{
				NpcIsland beIsland=objectFactory.getIslandCache().loadOnly(
					event.getAttackIslandIndex()+"");
				if(event.getSourceIslandIndex()==islandId
					&&beIsland.getPlayerId()!=0)// Э��
				{
					return true;
				}
			}
		}
		return false;
	}

	private class RankInfo
	{

		/** ������� */
		String playerName;
		/** ��ҵȼ� */
		int playerLevel;
		/** �������� */
		int rankInfo;

		public RankInfo(String playerName,int playerLevel,int rankInfo)
		{
			this.playerName=playerName;
			this.playerLevel=playerLevel;
			this.rankInfo=rankInfo;
		}

		/**
		 * @return playerLevel
		 */
		public int getPlayerLevel()
		{
			return playerLevel;
		}

		/**
		 * @param playerLevel Ҫ���õ� playerLevel
		 */
		public void setPlayerLevel(int playerLevel)
		{
			this.playerLevel=playerLevel;
		}

		/**
		 * @return playerName
		 */
		public String getPlayerName()
		{
			return playerName;
		}

		/**
		 * @param playerName Ҫ���õ� playerName
		 */
		public void setPlayerName(String playerName)
		{
			this.playerName=playerName;
		}

		/**
		 * @return rankInfo
		 */
		public int getRankInfo()
		{
			return rankInfo;
		}

		/**
		 * @param rankInfo Ҫ���õ� rankInfo
		 */
		public void setRankInfo(int rankInfo)
		{
			this.rankInfo=rankInfo;
		}
	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public TextValidity getTv()
	{
		return tv;
	}

	public void setTv(TextValidity tv)
	{
		this.tv=tv;
	}

	public SWDSManager getDsmanager()
	{
		return dsmanager;
	}

	public void setDsmanager(SWDSManager dsmanager)
	{
		this.dsmanager=dsmanager;
	}

	public AllianceFightManager getAfightManager()
	{
		return afightManager;
	}

	public void setAfightManager(AllianceFightManager afightManager)
	{
		this.afightManager=afightManager;
	}

	public AllianceChestManager getAllianceChestManager()
	{
		return allianceChestManager;
	}

	public void setAllianceChestManager()
	{
		this.allianceChestManager=AllianceChestManager.getInstance();
	}

	/** ˢ�°������ */
	public void flushRank()
	{
		flushFileds(true);
	}
	
	/** ˢ�����˸���ս������ */
	public void flushPersonFight()
	{
		synchronized(personFight)
		{
//			Session[] sessions=dsmanager.getSessionMap().getSessions();
			Object[] players=objectFactory.getPlayerCache().getCacheMap().valueArray();
			personFight.clear();
			OrderList longBefore=new OrderList();
			for(int i=0;i<players.length;i++)
			{
				Player player=((PlayerSave)players[i]).getData();
				if(player==null) continue;
				if(!SeaBackKit.isNewPlayerChangeName(player.getId(),
					objectFactory)) continue;
				String aname=player.getAttributes(PublicConst.ALLIANCE_ID);
				if(aname!=null&&!aname.equals("")) continue;
				if(player.getUpdateTime()>=TimeKit.getSecondTime()-24*60*60)
				{
					sortFightList(personFight,player);
				}else{
					sortFightList(longBefore,player);
				}
			}
			if(personFight.size()<FIGHT_SIZE)
			{
				int leftLength=FIGHT_SIZE-personFight.size()>longBefore
					.size()?longBefore.size():FIGHT_SIZE-personFight.size();
				for(int i=0;i<leftLength;i++){
					personFight.add(longBefore.get(i));
				}
			}
		
		}
	}
	/** ���ս�������е�ĳ��Ԫ�� */
	public void removeFightRank(Player player)
	{
		synchronized(personFight)
		{
			for(int i=0;i<personFight.size();i++)
			{
				RankInfo rank=(RankInfo)personFight.get(i);
				if(player.getName().equals(rank.getPlayerName()))
				{
					personFight.removeAt(i);
				}
			}
		}
	}
	/** ��orderlist����ս������������*/
	public void sortFightList(OrderList list,Player sortPlayer){
		int score=sortPlayer.getFightScore();
		int index=-1;
		int rankscore=0;
		if(list.size()<FIGHT_SIZE)
		{
			index=0;
		}
		else
		{
			rankscore=((RankInfo)list
				.get(list.size()-1)).getRankInfo();
			if(score<rankscore) return;
		}
		for(int k=list.size()-1;k>=0;k--)
		{
			rankscore=((RankInfo)list.get(k)).getRankInfo();
			if(score<rankscore)
			{
				index=k+1;
				break;
			}else
			{
				index=k;
			}
		}
		if(index>=0)
			list.addAt(
				new RankInfo(sortPlayer.getName(),sortPlayer.getLevel(),
					sortPlayer.getFightScore()),index);
		if(list.size()>FIGHT_SIZE) list.remove();
	}
	
	public void setAutoTransfer(AllianceAutoTransfer autoTransfer)
	{
		this.autoTransfer=autoTransfer;
	}
	
	/**�޸��������а��������������**/
	public void setAllianceName(String allianceName,String name)
	{
		Object[] objects=alliances.getArray();
		for(int i=0;i<objects.length;i++)
		{
			if(objects[i]==null) continue;
			RankInfo rankinfo=(RankInfo)objects[i];
			if(rankinfo.getPlayerName().equals(allianceName))
			{
				String oldName=rankinfo.getPlayerName();
				rankinfo.setPlayerName(name);
				allianceChestManager.resetAllianceName(oldName,name);
				break;
			}
		}
	}
	
	/**���Ƹĳ�id**/
	public String nameChaneId(String names)
	{
		String[] name=TextKit.split(names,"%");
		if(name.length==0) return null;
		String str=null;
		for(int i=0;i<name.length;i++)
		{
			Alliance alliance=objectFactory.getAllianceMemCache()
							.loadByName(name[i],false);
			if(alliance!=null)
				if(i==0)
					str=alliance.getId()+"";
				else str+="%"+alliance.getId();
		}
			return str==null?names:str;
	}
	
	/**�õ���ҵ�ǰ�������Ϣ**/
	public String getPlayerAppInfo(String appiacneName,int aid)
	{
		appiacneName=nameChaneId(appiacneName);
		String names[]=TextKit.split(appiacneName,"%");
		String appianceAfter="";
		for(int i=0;i<names.length;i++)
		{
			if(TextKit.parseInt(names[i])!=aid)
			{
				if(appianceAfter.equals(""))
					appianceAfter+=names[i];
				else
				{
					appianceAfter+="%";
					appianceAfter+=names[i];
				}
			}
		}
		return appianceAfter;
	}
	
	/**�޸�����ս������**/
	public void personFightChange(String name,String newName)
	{
		if(personFight==null || personFight.size()==0)
			return ;
		for(int i=0;i<personFight.size();i++)
		{
			RankInfo rank=(RankInfo)personFight.get(i);
			if(rank==null) continue;
			if(rank.getPlayerName().equals(name))
			{
				rank.setPlayerName(newName);
				return ;
			}
		}
	}
	
	/**�������change**/
	public void sendPNameChange(Player player)
	{
		String appcation=player
			.getAttributes(PublicConst.ALLIANCE_APPLICATION);
		if(appcation==null||appcation.length()==0) return;
		appcation=nameChaneId(appcation);
		String[] appcations=TextKit.split(appcation,"%");
		ByteBuffer data=new ByteBuffer();
		data.writeShort(ALLIANCE_PORT);
		data.writeByte(CHANGE_PNAME);
		data.writeInt(player.getId());
		data.writeUTF(player.getName());
		for(int i=0;i<appcations.length;i++)
		{
			Alliance alliance=objectFactory.getAlliance(
				TextKit.parseInt(appcations[i]),false);
			if(alliance!=null) sendAllAlliancePlayers(data,alliance);
		}
	}
	
	
	/** ���������������ж������ڽ�ɢ��ʱ��Ҫ���ʼ� **/
	public void sendHostileMessage(Alliance myAlliance)
	{
		Object[] objs=objectFactory.getAllianceMemCache().getCacheMap()
			.valueArray();
		if(objs==null||objs.length==0) return;
		Player player=objectFactory.getPlayerById(myAlliance
			.getMasterPlayerId());
		String title=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"dismiss_hostile");
		String content=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"dismiss_hostile_content");
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
				alliance.removeHostile(myAlliance.getId());
				Player hplayer=objectFactory.getPlayerById(alliance
					.getMasterPlayerId());
				if(hplayer==null) continue;
				MessageKit.sendSystemMessages(hplayer,objectFactory,content,
					title);
			}
		}
	}
	
	/**��֤����Ƿ����������ս**/
	public boolean checkLeaveAlliance(Alliance alliance,int playerId)
	{
		if(alliance.getBetBattleIsland()==0) return false;
		BattleIsland battleIsland=battleFight.getBattleIslandById(alliance.getBetBattleIsland(),false);
		if(battleIsland==null) return false;
		if(battleFight.getAllianceStage().getStage()<Stage.STAGE_THREE)
			return false;
		return battleIsland.isHavePlayer(playerId);
	}
	
	/**��ɢ������Ҫ��֤�Ƿ���Խ�ɢ**/
	public boolean checkDisAlliance(Alliance alliance)
	{
		if(alliance.getBetBattleIsland()==0) return false;
		BattleIsland battleIsland=battleFight.getBattleIslandById(alliance
			.getBetBattleIsland(),false);
		if(battleIsland==null) return false;
		int stage=battleFight.getAllianceStage().getStage();
		if(stage==Stage.STAGE_ONE) return false;
		return true;
	}

	/** ��������������� */
	private boolean checkAllianceJoinSeeting(Alliance alliance,Player player)
	{
		boolean isJoinPlayerLevel=Boolean.parseBoolean(alliance
			.getAttributes(PublicConst.ALLIANCE_JOIN_LEVEL_BOOL));
		boolean isJoinPlayerScore=Boolean.parseBoolean(alliance
			.getAttributes(PublicConst.ALLIANCE_JOIN_SCORE_BOOL));
		if(alliance.getAutoJoin()==0)
			return false;
		if(isJoinPlayerLevel&&player.getLevel()<alliance.getJoinPlayerLevel())
			return false;
		if(isJoinPlayerScore&&player.getFightScore()<alliance.getJoinFightScore())
			return false;
		return true;
	}
	
	public void setBattleFight(AllianceBattleFight battleFight)
	{
		this.battleFight=battleFight;
	}
	
	/**����ȫ���˵ľ���ֵ**/
	public void setAllAllianceGiveValue(Alliance alliance)
	{
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=objectFactory.getPlayerById(list.get(i));
			if(player==null) continue;
			SeaBackKit.leaveAllianceRecord(player,alliance);
		}
	}

	public void setBattleFigtmanager(AllianceBattleManager battleFigtmanager)
	{
		this.battleFigtmanager=battleFigtmanager;
	}

}
