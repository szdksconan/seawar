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

/** 联盟通讯端口 */
public class AlliancePort extends AccessPort
{

	/** 联盟广播消息常量 */
	public static final int KICK_OUT=1,// 被踢出联盟
					PLAYER_JOIN=2,// 有新玩家加入联盟
					NOTICE_CHANGE=3,// 联盟公告修改
					EXP_LEVEL_CHANGE=4,// 联盟等级,经验提升
					SOME_ONE_EIXT=5,// 玩家退出联盟
					SINGLE_SKILL_CHANGE=6,// 联盟单个技能刷新(刷新指定sid技能的经验和等级)
					RIGHT_CHANGE=7,// 联盟权限修改(包含移交盟主权限)
					ADD_REQUEST=8,// 添加入盟申请
					REMOVE_REQUEST=9,// 移除入盟申请
					DIS_MISS_ALLIANCE_PUSH=10,// 解散联盟
					CANCEL_ALL_APPCAITON=11,
					ALIIANCE_ADD=12,
					MEMBER_LOGIN_STATE_CHANGE=14,
					BOSS_SEND_VALUE=15,// 刷新盟友登陆状态
					PLAYER_GIVE_VALUE=16,//联盟刷新贡献点
					CHANGE_PNAME=17,//修改申请者名称
					JOIN_ALLIANCE_SETTING=18;//入盟设置
					
				

	/** 联盟事件页数 */
	public static final int MAX_EVENT_SIZE=20;
	/** 最大联盟页数 */
	public static final int MAX_PAGE=5;
	/** 最多能申请的联盟个数 */
	public static final int MAX_APP_NUM=5;
	/** 联盟前台端口常量 */
	public static final int ALLIANCE_PORT=2005;
	/**
	 * 联盟广播给前台常量 EXIST_ALLIANCE推出联盟 DIS_MISS_ALLIANCE解散联盟
	 * CHANGE_ALLIANCE_DES改变描述和公告 NEW_PLAYER_IN新玩家加入 SEND_KICK_PLAYER踢掉某个玩家
	 * SEND_CHANGE_PLAYER_DUTY 修改玩家的职务 
	 */
	public static final int EXIST_ALLIANCE=1,DIS_MISS_ALLIANCE=2,
					CHANGE_ALLIANCE_DES=3,NEW_PLAYER_IN=4,
					SEND_KICK_PLAYER=5,SEND_CHANGE_PLAYER_DUTY=6;

	public static final int MIN_LEN=1,MAX_LEN=12;
	/**
	 * 常量 CREATE_A_ALLIAN=1创建联盟 GET_ALLIAN_SORT获取联盟排名 SET_HOSTILE=24 设置敌对联盟
	 * ALIANCE_JOIN_SET=26入盟设置 ALLIANCE_CHEST=27联盟宝箱 SET_ALLIANCEFLAG=28 设置联盟旗帜
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

	/** 分页大小 */
	public static final int PAGE_SIZE=20;
	/** 无盟个人战力排行长度 */
	public static final int FIGHT_SIZE=100;
	/** 更新时间间隔 1个小时 */
	public static final int FLUSH_TIME=60*60/2;

	/** 数据获取类 */
	CreatObjectFactory objectFactory;
	/** 上一次刷新的时间 */
	int lastTime;
	/** 幸运积分刷新标识 */
	boolean isResetLuckyPoint=true;
	/** 联盟的排行 */
	ArrayList alliances=new ArrayList();
	/** 无联盟玩家战力排名 */
	OrderList personFight=new OrderList();
	/** 无联盟玩家战力排名刷新的时间 */
	int flushTime;
	
	TextValidity tv;

	/** dsmanager */
	SWDSManager dsmanager;
	/** 盟战管理器 */
	AllianceFightManager afightManager;
	/** 会长自动移交 */
	AllianceAutoTransfer autoTransfer;
	/** 联盟宝箱管理器 */
	AllianceChestManager allianceChestManager;
	/**新联盟战**/
	AllianceBattleFight battleFight;
	/**新联盟战管理器**/
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
		// 创建联盟
		if(type==CREATE_A_ALLIAN)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.CREATE_ALLIANCE_LEVEL_LIMIT,player,
				"create_alliance_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			/** 联盟名字 */
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
			// 忽略大小写判断名字是否存在
			if(objectFactory.getAllianceMemCache().loadByName(name,false)!=null)
			{
				throw new DataAccessException(0,
					"alliance name has been used");
			}
			// 判断名字是否存在
			if(objectFactory.getAllianceMemCache().getDbaccess()
				.isExist(name,0))
			{
				throw new DataAccessException(0,
					"alliance name has been used");
			}
			// 判断金币是否足够
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
			/**设置新创建的免费次数为0**/
			alliance.getFlag().setFreeTime(0);
			objectFactory.getAllianceMemCache().getDbaccess().save(alliance);

			// 盟战相关
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
			// 发送邮件
			String title=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"new_alliance_title");
			String content=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"new_alliance_content");
			content=TextKit.replace(content,"%",alliance.getName());
			MessageKit.sendSystemMessages(player,objectFactory,content,title);
		}
		// 排名
		else if(type==GET_ALLIAN_SORT)
		{
			// 分页 每页20个
			int pageIndex=data.readUnsignedByte();
			// 支持前400名的排行
			if(pageIndex>MAX_PAGE||pageIndex<=0) return null;
			data.clear();
			flushFileds(false);
			int num=alliances.size()-(pageIndex-1)*PAGE_SIZE;
			if(num<0) num=0;
			if(num>PAGE_SIZE) num=PAGE_SIZE;
			int page=getPage();
			if(page==0) page=1;
			// 联盟总页数
			data.writeByte(page);
			data.writeByte(num);
			for(int i=(pageIndex-1)*PAGE_SIZE;i<pageIndex*PAGE_SIZE;i++)
			{
				if(i>=alliances.size()) break;
				RankInfo rank=(RankInfo)alliances.get(i);
				String allianceName=rank.getPlayerName();
				data.writeShort(i+1);
				data.writeUTF(allianceName);
				// 人数
				Alliance alliance=objectFactory.getAllianceMemCache()
					.loadByName(rank.getPlayerName(),true);
				data.writeByte(alliance.playersNum());
				alliance.pushAllFightScore(objectFactory);
				// 战斗力
				data.writeInt(rank.getRankInfo());
				// 申请状态
				String appName=player
					.getAttributes(PublicConst.ALLIANCE_APPLICATION);
				if(appName!=null)
				{
					//转换成id
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
		// 搜索联盟精确查找
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
				// 战斗力
				data.writeInt(alliance.getAllFightScore());
				// 申请状态
				String appName=player
					.getAttributes(PublicConst.ALLIANCE_APPLICATION);
				if(appName!=null)
				{
					//转换成id
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
		// 查看公会
		else if(type==VIEW_ALLIAN)
		{
			/** 公会 */
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
			//判断当前玩家是否有联盟
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
				// 申请状态
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
		// 申请公会
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
			// 是否有联盟
			String alliance_id=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(alliance_id!=null&&!alliance_id.equals(""))
			{
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().loadOnly(alliance_id+"");
				if(alliance!=null)
					throw new DataAccessException(0,"you have alliance");
			}
			// 联盟不存在
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
			// 联盟人数限制
			int limitNum=PublicConst.ALLIANCE_LEVEL_NUMS[alliance
				.getAllianceLevel()-1];
			if(alliance.playersNum()>=limitNum)
			{
				throw new DataAccessException(0,"alliance is full");
			}
			//判断联盟是否开启自动加入  符合条件自动加入 不符合加入申请列表
			if(checkAllianceJoinSeeting(alliance,player))
			{
				alliance.getBoss().addBossAwardJoin(player.getId());
				alliance.addPlayer(player.getId());
				player.setAttribute(PublicConst.ALLIANCE_ID,alliance.getId()+"");
				player.setAttribute(PublicConst.ALLIANCE_JOIN_TIME,
					TimeKit.getSecondTime()+"");
				// 联盟事件
				AllianceEvent event=new AllianceEvent(
					AllianceEvent.ALLIANCE_EVENT_AUTO_JOIN,null,
					player.getName(),"",TimeKit.getSecondTime());
				alliance.addEvent(event);
				alliance.addAllianceSkills(player);
				player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
				player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
				// 盟战相关
				afightManager.jionAlliance(player,alliance.getId());
				alliance.setPlayerGiveValue(player,objectFactory);
				// 移除无联盟玩家战力
				removeFightRank(player);
				// 广播给联盟在线玩家
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
				//捐献排行榜信息
				data.writeShort(0);
				data.writeShort(0);
				data.writeShort(0);
				data.writeInt(0);
				data.writeInt(0);
				sendAllAlliancePlayers(data,alliance);
				//添加物资排行
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
		// 取消申请公会
		else if(type==CANCEL_APPLICATION_ALLIAN)
		{
			String name=data.readUTF();
			// 联盟不存在
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
		// 退出联盟
		else if(type==EXIT_ALLIAN)
		{
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// 联盟不存在
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						Integer.parseInt(allianceId)+"");
				if(alliance!=null)
				{
					// 如果是会长不能退出
					if(alliance.getMasterName(objectFactory)
						.equalsIgnoreCase(player.getName()))
					{
						throw new DataAccessException(0,"you can not exit");
					}
					// 第一天不能推出
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
					// 检测协防
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
					// 去掉联盟技能
					player.resetAdjustment();
					// 去掉联盟主动技能
					player.getAllianceList().clear();
					player.setAttribute(
						PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
					player.setAttribute(PublicConst.ALLIANCE_APPLICATION,
						null);
					// 联盟事件
					AllianceEvent event=new AllianceEvent(
						AllianceEvent.ALLIANCE_EVENT_PLAYER_LEFT,
						player.getName(),"","",TimeKit.getSecondTime());
					alliance.addEvent(event);

					// 盟战相关
					afightManager.exitAlliance(player,alliance.getId());
					//记录玩家的捐献信息
					SeaBackKit.leaveAllianceRecord(player,alliance);
					//移除玩家的物资排行信息
					SeaBackKit.removeAllianceRank(player,alliance);
					// 广播给联盟在线玩家
					data.clear();
					data.writeShort(ALLIANCE_PORT);
					data.writeByte(SOME_ONE_EIXT);
					data.writeUTF(player.getName());
					sendAllAlliancePlayers(data,alliance);
					JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.SOMETHING_ELSE);
				}
			}
		}
		// 解散联盟
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
			// 当天不能解散联盟
			String ctStr=alliance
				.getAttributes(PublicConst.ALLIANCE_CREATE_TIME);
			long createTime=ctStr!=null?Long.parseLong(ctStr):0l;
			int endTime=SeaBackKit.getSomeDayEndTime(createTime*1000l);
			if(endTime>TimeKit.getSecondTime())
			{
				throw new DataAccessException(0,
					"alliance_create_time_to_short");
			}

			// 盟战相关
			afightManager.dismissAlliance(alliance);
			//设置当前玩家的贡献点
			setAllAllianceGiveValue(alliance);
			// 广播给联盟在线玩家
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(DIS_MISS_ALLIANCE_PUSH);
			sendAllAlliancePlayers(data,alliance);
			//给敌对联盟发送信息
			sendHostileMessage(alliance);
			alliance.dismiss(objectFactory);
			flushFileds(true);
			allianceChestManager.dismissAlliance(alliance);
			data.clear();
		}
		// 获取自己联盟排名
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
		// 公会捐献
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
			// 提升某个技能或者公会等级*
			int skillSid=data.readUnsignedShort();
			// 捐赠类型
			int typeGive=data.readUnsignedByte();
			if(!(typeGive>=Resources.METAL&&typeGive<=Resources.GEMS))
			{
				throw new DataAccessException(0,"typeGive is wrong");
			}
			// 判断捐赠次数是否已满
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
			// 检查资源
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
			// 提升的点数
			int point=num[typeGive]+1;
			// 发放奖励品
			int awardSid=PublicConst.ALLIANCE_AWARD_SID[num[typeGive]];
			Award award=(Award)Award.factory.getSample(awardSid);
			// 等于0是提升公会等级
			if(skillSid==0)
			{
				if(alliance.getAllianceLevel()>=PublicConst.MAX_ALLIANCE_LEVEL)
				{
					throw new DataAccessException(0,"alliance level is max");
				}
				// 增加经验
				int addLevel=alliance.incrExp(point);
				if(addLevel>0)
				{
					// 联盟事件
					AllianceEvent event=new AllianceEvent(
						AllianceEvent.ALLIANCE_EVENT_LEVEL,"","",
						alliance.getAllianceLevel()+"",
						TimeKit.getSecondTime());
					alliance.addEvent(event);
					// 提示
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
				// 增加次数
//				player.addGiveValueForAlliance(typeGive);
				// 广播
				data.clear();
				data.writeShort(ALLIANCE_PORT);
				data.writeByte(EXP_LEVEL_CHANGE);
				data.writeInt(alliance.getAllianceExp());
				data.writeByte(alliance.getAllianceLevel());
				sendAllAlliancePlayers(data,alliance);
			}
			else
			{
				// 提升联盟技能
				if(!isHaveSkillSid(skillSid,player,
					alliance.getAllianceLevel()))
				{
					throw new DataAccessException(0,"alliance level limit");
				}
				// 是否技能等级超过联盟等级
				if(alliance.getSkillExp(skillSid)[1]>=alliance
					.getAllianceLevel())
				{
					throw new DataAccessException(0,"alliance level limit");
				}
				// 增加经验
				int addLevel=alliance.incrSkillExp(point,skillSid);
				award.awardSelf(player,TimeKit.getSecondTime(),null,null,
					null,new int[]{EquipmentTrack.FROM_ALLIANCE});
				if(addLevel>0)
				{
					int level[]=alliance.getSkillExp(skillSid);
					alliance.flushAllianceSkill(objectFactory);
					// 联盟事件
					AllianceEvent event=new AllianceEvent(
						AllianceEvent.ALLIANCE_EVENT_SKILL_LEVEL,
						skillSid+"",skillSid+"",level[1]+"",
						TimeKit.getSecondTime());
					alliance.addEvent(event);
					JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.ALLIANCE_SKILL_UP);
				}
//				// 增加次数
//				player.addGiveValueForAlliance(typeGive);
				// 广播
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
				// 发送change消息
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,
					needResource[Resources.GEMS]);
			}
			else
			{
				// 扣除资源
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
//			// 增加贡献度
//			giveValue+=(num[typeGive]+1);
			//成就数据采集
			AchieveCollect.allianceOffer(num[typeGive]+1,player);
//			// 贡献度提示
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
				// 联盟事件
				AllianceEvent event=new AllianceEvent(
					AllianceEvent.ALLIANCE_EVENT_PLAYER_CONTRI,
					player.getName(),"",rank.getTotleValue()+"",TimeKit.getSecondTime());
				alliance.addEvent(event);
			}
			data.clear();
			int todayNum[]=rank.getDonaterecord();
			// 捐献后 添加联盟宝箱次数 周一~周六
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
			//返回玩家联盟宝箱次数
			data.writeByte(acCount);
			// 宝石日志
			if(gemsOrResource>0)
			{
				// 宝石日志记录
				objectFactory.createGemTrack(GemsTrack.ALLIANCE_GIVE,
					player.getId(),needResource[Resources.GEMS],alliance.getId(),
					Resources.getGems(player.getResources()));
			}
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ALLIANCE_GIVE_TASK_EVENT,null,player,null);
		}
		// 修改描述和公告
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
			// 联盟事件
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_ANNOUCE,player.getName(),
				player.getName(),"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			// 广播给联盟在线玩家
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(NOTICE_CHANGE);
			data.writeUTF(alliance.getDescription());
			data.writeUTF(alliance.getAnnouncement());
			sendAllAlliancePlayers(data,alliance);
		}
		// 获取事件
		else if(type==GET_ALL_ALLIANCE_EVENT)
		{
			String str=checkAlliance(player);
			if(str!=null) throw new DataAccessException(0,str);
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.load(player.getAttributes(PublicConst.ALLIANCE_ID));
			// 分页 每页20个
			int pageIndex=data.readUnsignedByte();
			// 支持前400名的排行
			if(pageIndex>MAX_EVENT_SIZE||pageIndex<=0)
			{
				throw new DataAccessException(0,"pageIndex is wrong");
			}
			data.clear();
			int num=alliance.getEventList().size()-(pageIndex-1)
				*MAX_EVENT_SIZE;
			if(num<0) num=0;
			if(num>MAX_EVENT_SIZE) num=MAX_EVENT_SIZE;
			// 联盟总页数
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
		// 同意某个玩家进入
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
			// 联盟人数限制
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
				// 是否有联盟了
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
			// 联盟事件
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_NEW_PLAYER,player.getName(),
				appplayer.getName(),"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			alliance.addAllianceSkills(appplayer);
			appplayer.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,
				null);
			appplayer.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);

			// 盟战相关
			afightManager.jionAlliance(appplayer,alliance.getId());
			alliance.setPlayerGiveValue(appplayer,objectFactory);
			//
			removeFightRank(appplayer);
			
			// 广播给联盟在线玩家
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
			//捐献排行榜信息
			data.writeShort(0);
			data.writeShort(0);
			data.writeShort(0);
			data.writeInt(0);
			data.writeInt(0);
			//添加物资排行
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
		// 取消所有申请者
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
					// 发送邮件
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
		// 踢掉某个玩家
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
			// // 第一天不能踢出
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
			// 联盟事件
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_KICK_PLAYER,player.getName(),
				kickPlayer.getName(),"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			String content=InterTransltor.getInstance().getTransByKey(
				kickPlayer.getLocale(),"alliance_be_kick_content");
			content=TextKit.replace(content,"%",player.getName());
			MessageKit.sendSystemMessages(kickPlayer,objectFactory,content);

			// 盟战相关
			afightManager.exitAlliance(kickPlayer,alliance.getId());
			//记录玩家的捐献信息
			SeaBackKit.leaveAllianceRecord(kickPlayer,alliance);
			//移除玩家的物资排行信息
			SeaBackKit.removeAllianceRank(kickPlayer,alliance);
			// 广播给联盟在线玩家
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
		// 获取所有申请者
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
		// 提升某个玩家为副会长
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
			// 查看某玩家是否在联盟里面
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
			// 移交会长
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
			// 联盟事件
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_MASTER_CHANGE,player.getName(),
				bePlayer.getName(),rank+"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			// 广播给联盟在线玩家
			data.clear();
			data.writeShort(ALLIANCE_PORT);
			data.writeByte(RIGHT_CHANGE);
			data.writeUTF(bePlayer.getName());
			data.writeByte(rank);
			sendAllAlliancePlayers(data,alliance);
			// 增加权限的广播
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
		// 拒绝某个的请求
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
		// 获取无盟个人战力排名 
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
		// 邀请玩家加入
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
		// 获取联盟邀请消息
		else if(type==ALLIANCE_INVITATION)
		{
			// 得到页序
			int page=data.readInt();
			data.clear();
			// 从玩家属性列表得到邀请字符串
			String invitationStr=player
				.getAttributes(PublicConst.ALLIANCE_INVITATION_RECORD);
			if(invitationStr!=null&&!invitationStr.equals(""))
			{
				// 分割邀请字符串获取邀请记录
				String[] invitations=invitationStr.split(",");
				// 获取总页数
				int invitationLen=invitations.length;
				int pages=invitationLen
					/PublicConst.DEFAOULT_ALLIANCE_SIZE;
				if(pages*PublicConst.DEFAOULT_ALLIANCE_SIZE<invitationLen)
				{
					// 多出记录不足一页的补一页
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
		// 接受联盟邀请
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
			// 联盟人数限制
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
			// 联盟事件
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_NEW_PLAYER,appplayer.getName(),
				player.getName(),"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			alliance.addAllianceSkills(player);
			player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
			player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);

			// 盟战相关
			afightManager.jionAlliance(player,alliance.getId());
			alliance.setPlayerGiveValue(player,objectFactory);
			
			// 广播给联盟在线玩家
			
			// 前端协议制定////////////////////////////////
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
			//添加物资排行
			JBackKit.sendPlayerMaterialRank(alliance.getPlayerList(),
				objectFactory,player,0,Alliance.MILITARY_RANK1);
			data.clear();
			data.writeShort(AlliancePort.ALLIANCE_PORT);
			data.writeByte(AlliancePort.ALIIANCE_ADD);
			alliance.showBytesWrite(data,player,objectFactory);
			afightManager.showBytesWrite(alliance.getId(),data);
			JBackKit.sendAllicace(data,player);
			JBackKit.sendAllianceBattleInfo(player,battleFigtmanager);
			// 前端协议制定/////////////////////
			data.clear();
			data.writeByte(result);
			player.delInvitedRecords();
			JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.SOMETHING_ELSE);
		}
		// 拒绝联盟邀请
		else if(type==ALLIANCE_INVITATION_REFUSE)
		{
			String allianceName=data.readUTF();
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadByName(allianceName,false);
			if(alliance!=null) player.delInvitedRecord(alliance.getId());
			data.clear();
		}
		//设置敌对事件
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
		/**取消设置**/
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
		// 入盟设置
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
		// 联盟宝箱
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
			// 0 查看
			if(subType==0)
			{
				//周一重置各数据 0周日
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
						// 星期一重置联盟幸运点数
						allianceChestManager.resetLuckyPoint();
						// 重置所有玩家幸运宝箱领奖次数
						allianceChestManager.resetPlayerLucyAwardCount();
						// 重置排行榜
						allianceChestManager.resetRank();
					}
				}
				//返回数据
				data.clear();
				//玩家信息
				ac.showBytesWrite(data);
				//排行榜信息
//				allianceChestManager.showBytesWrite(data,alliance.getId());
				//联盟宝箱奖励
//				allianceChestManager.showBytesWriteChestAwards(data,player);
				//幸运积分奖励
				allianceChestManager.showBytesWriteLuckyPointAwards(data,player);
			}
			// 1宝箱抽奖
			else if(subType==1)
			{
				if(SeaBackKit.getDayOfWeek(TimeKit.getMillisTime())==0)
					throw new DataAccessException(0,
						"cannot get adward on sunday");
				String str=checkAlliance(player);
				if(str!=null) throw new DataAccessException(0,str);
				// 24小时内入盟不能领奖
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
				//领奖前宝箱等级
				data.writeByte(level);
				// 玩家领取等级为level的宝箱 奖励长度为1
				data.writeByte(1);
				Award aw=(Award)Award.factory
					.newSample(PublicConst.ALLIANCE_CHEST_AWARD[level]);
				aw.awardSelf(player,TimeKit.getSecondTime(),data,
					objectFactory,null,
					new int[]{EquipmentTrack.ALLIANCE_CHEST_AWARD});
				//检查是否为幸运积分道具
				if(allianceChestManager.isLuckyAward(data))
				{
					alliance.setLuckyCreateAt(TimeKit.getSecondTime());
					data.writeBoolean(true);
					//删除联盟幸运积分道具
					int num=player.getBundle().getCountBySid(PublicConst.ALLIANCE_CHEST_SID);
					player.getBundle().decrProp(PublicConst.ALLIANCE_CHEST_SID,num);
					//刷新前台背包
					JBackKit.sendResetBunld(player);
					alliance.addLuckyPoints();
					// 刷新排行榜
					allianceChestManager.flushRank(alliance);
					// 系统通告 XX(联盟)XX(舰长)在联盟宝箱中抽取到"联盟幸运道具"XX(联盟)幸运积分+1
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
				// 宝箱升级
				float random=MathKit.randomValue(0.0f,1.0f);
				if(random<PublicConst.ALLIANCE_CHEST_UPGRADE_ODDS&&level<3)
					level++;
				else
					level=0;
				data.writeByte(freeCount);
				data.writeShort(count);
				data.writeByte(level);
			}
			//领取排行榜奖励
			else if(subType==2)
			{
				// 只有周日能领奖
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
			//进入积分排行
			else if(subType==3)
			{
				data.clear();
				//排行榜信息
				allianceChestManager.showBytesWrite(data,alliance.getId(),ac);
			}
			ac.setFreeCount(freeCount);
			ac.setCount(count);
			ac.setChestLv(level);
			ac.setLastDay(lastDay);
			ac.setGiveLv(nowLv);
			player.setAllianceChest(ac);
		}
		/**设置联盟旗帜**/
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
			/** 旗帜图案 **/
			int igSid=data.readUnsignedShort();
			/** 旗帜颜色 **/
			int colour=data.readUnsignedShort();
			/** 旗帜造型 **/
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
				// 发送change消息
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
			/** 给全联盟的人刷新联盟旗帜 **/
			JBackKit.sendAllianceFlag(alliance,objectFactory);
		}
		return data;
	}


	/** 联盟常规判断 */
	public String checkAlliance(Player player)
	{
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(allianceId==null||allianceId.equals(""))
		{
			return "you have no alliance";
		}
		// 联盟不存在
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.load(allianceId);
		if(alliance==null)
		{
			return "alliance has been dismiss";
		}
		return null;
	}
	
	/** 联盟广播 */
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

	/** 排名总页数 */
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

	/** 是否能提升某个技能 */
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

	/** 联盟权限判断 */
	public String checkAllianceMaster(Player player)
	{
		// 联盟不存在
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.load(player.getAttributes(PublicConst.ALLIANCE_ID));
		// 是否是会长或副会长
		if(!alliance.isMaster(player.getId()))
		{
			throw new DataAccessException(0,"you are not master");
		}
		return null;
	}

	/** 刷新自己的排名 */
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

	/** 检测有无主动协防 */
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
					&&beIsland.getPlayerId()!=0)// 协防
				{
					return true;
				}
			}
		}
		return false;
	}

	private class RankInfo
	{

		/** 玩家名字 */
		String playerName;
		/** 玩家等级 */
		int playerLevel;
		/** 排行数据 */
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
		 * @param playerLevel 要设置的 playerLevel
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
		 * @param playerName 要设置的 playerName
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
		 * @param rankInfo 要设置的 rankInfo
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

	/** 刷新帮会排名 */
	public void flushRank()
	{
		flushFileds(true);
	}
	
	/** 刷新无盟个人战力排名 */
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
	/** 清除战力排行中的某个元素 */
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
	/** 对orderlist进行战斗力倒序排列*/
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
	
	/**修改联盟排行榜里面的联盟名称**/
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
	
	/**名称改成id**/
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
	
	/**得到玩家当前申请的信息**/
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
	
	/**修改无力战斗排行**/
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
	
	/**玩家名称change**/
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
	
	
	/** 所有联盟如果有这敌对联盟在解散的时候要发邮件 **/
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
	
	/**验证玩家是否加入了联盟战**/
	public boolean checkLeaveAlliance(Alliance alliance,int playerId)
	{
		if(alliance.getBetBattleIsland()==0) return false;
		BattleIsland battleIsland=battleFight.getBattleIslandById(alliance.getBetBattleIsland(),false);
		if(battleIsland==null) return false;
		if(battleFight.getAllianceStage().getStage()<Stage.STAGE_THREE)
			return false;
		return battleIsland.isHavePlayer(playerId);
	}
	
	/**解散联盟需要验证是否可以解散**/
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

	/** 检查联盟入盟设置 */
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
	
	/**设置全联盟的捐献值**/
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
