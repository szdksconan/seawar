package foxu.sea.port;

import java.io.IOException;
import java.util.HashMap;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import shelby.httpserver.HttpServer;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.ActivityLogMemCache;
import foxu.dcaccess.mem.GameDataMemCache;
import foxu.dcaccess.mem.LoginLogMemCache;
import foxu.ds.PlayerKit;
import foxu.sea.FriendInfo;
import foxu.sea.InterTransltor;
import foxu.sea.IslandLocationSave;
import foxu.sea.MealTimeManager;
import foxu.sea.NpcIsland;
import foxu.sea.OnlineLuckyContainer;
import foxu.sea.PasswordManager;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.User;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.AwardActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.award.Award;
import foxu.sea.builds.AutoUpBuildManager;
import foxu.sea.builds.Island;
import foxu.sea.checkpoint.Chapter;
import foxu.sea.config.ConfigManager;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.Skill;
import foxu.sea.gems.GemsTrack;
import foxu.sea.growth.GrowthPlanManager;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.recruit.RecruitDayTask;
import foxu.sea.recruit.RecruitKit;
import foxu.sea.task.TaskEventExecute;
import foxu.sea.task.TaskManager;

/** 玩家属性端口 1010 */
public class PlayerAttPort extends AccessPort implements TimerListener
{
	public static int GOLDEN_WHEEL=6002;
	public static int SILVER_WHEEL=6001;
	//更换账号验证码过期时间 
	public static int UCODE_TIME=12*3600;
	//邮件发送时间
	public static int EMAIL_SEND_TIME=7*24*3600;
	
	CreatObjectFactory objectFactory;
	AutoUpBuildManager autoUpBuilding;
	PasswordManager passwordManager; 
	GrowthPlanManager planManager;

	GameDataMemCache gameDataMemCache;
	
	/**验证码的长度**/
	public final static int SEND_MAIL_LENGTH=6;
	
	public final static int DAY_SEND_LENGTH=5;

	/** 配置信息管理器 */
	ConfigManager configManager;
	
	int checkTime;
	
	/**玩家删除端口**/
	public final static int SET_USER_STATE=12;

	/**
	 * 玩家购买能量 EVERY_ACTIVES=5每次购买的活力值 EVERY_GEMS每次花费系数
	 * EVERY_DAY_LIMITE每天购买的次数
	 */
	public static int EVERY_ACTIVES=5,EVERY_GEMS=5,EVERY_DAY_LIMITE=10;
	

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		int type=data.readUnsignedByte();
		// 取服务端版本号
		if(type==PublicConst.SERVER_VIERSION)
		{
			try
			{
				int platid=data.readUnsignedByte();
				data.clear();
				data.writeFloat(configManager.getVersion(platid));
				data.writeUTF(configManager.getAddress(platid));
			}
			catch(Exception e)
			{
				data.clear();
				data.writeFloat(configManager.getVersion(0));
				data.writeUTF(configManager.getAddress(0));
			}
			return data;
		}
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
		// 军衔提升
		if(type==PublicConst.PLAYER_TYPE_UP)
		{
			// 现在军衔等级 默认为1级
			int nowLevel=player.getPlayerType();
			if(nowLevel>PublicConst.MILITARY_RANK_LEVEL.length)
				throw new DataAccessException(0,"playerType is limit");
			if(PublicConst.MILITARY_RANK_LEVEL[nowLevel-1]>player.getLevel())
			{
				throw new DataAccessException(0,"player level need:"
					+PublicConst.MILITARY_RANK_LEVEL[nowLevel-1]);
			}
			int needMoney=PublicConst.MILITARY_RANK_MONEY[nowLevel-1];
			if(!Resources.checkResources(0,0,0,0,needMoney,
				player.getResources()))
			{
				JBackKit.sendResetResources(player);
				throw new DataAccessException(0,"money not enough");
			}
			// 扣除金钱
			Resources.reduceResources(player.getResources(),0,0,0,0,
				needMoney,player);
			player.setPlayerType(player.getPlayerType()+1);
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.RANK_HONOR_TASK_EVENT,this,player,null);
			// 系统消息
			if(player.getPlayerType()>=2)
			{
				String message=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"player_type_up");
				message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
				message=TextKit.replace(
					message,
					"%",
					InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,
						"player_type_"+player.getPlayerType()));
				SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
					message);
			}
		}
		// 统帅提升
		else if(type==PublicConst.COMMAND_LEVEL_UP)
		{
			// 不能大于玩家等级
			if(player.getCommanderLevel()>=player.getLevel())
				throw new DataAccessException(0,
					"commanderLevel is over playerLevel");
			if(player.getCommanderLevel()>=PublicConst.COMMANDER_SUCCESS.length)
				throw new DataAccessException(0,"commanderLevel is limit");
			// 检查统御书是否足够
			if(!player.checkPropEnough(PublicConst.COMMANDER_LEVEL_UP_SID,1))
			{
				throw new DataAccessException(0,"not enough prop");
			}
			// 配置数据成功率
			int rate=PublicConst.COMMANDER_SUCCESS[player
				.getCommanderLevel()];
			// 用于记录升级统御失败次数
			int Number=0;
			if(player.getAttributes(PublicConst.UPCOMMANDER_FAILURE)!=null
				&&!player.getAttributes(PublicConst.UPCOMMANDER_FAILURE)
					.equals(""))
			{
				Number=Integer.parseInt(player
					.getAttributes(PublicConst.UPCOMMANDER_FAILURE));
			}
			double realrate=(double)rate
				/100
				+(1-(double)rate/100)
				*(double)Math.pow(
					(double)(Number*(double)rate/100/PublicConst.A),
					PublicConst.B);
			// 实际成功率*100
			double Realrate=realrate*100;
			int random=MathKit.randomValue(0,100);
			boolean bool=false;
			// 提升成功
			if(random<Realrate)
			{
				bool=true;
				player.setCommanderLevel(player.getCommanderLevel()+1);
				//成就数据采集
				AchieveCollect.commandLevel(player);
				// 新兵福利
				RecruitKit.pushTask(RecruitDayTask.COMMAND_LV,
					player.getCommanderLevel(),player,true);
				//将升级成功记录设为0（）
				player.setAttribute(PublicConst.UPCOMMANDER_FAILURE,"0");
				// 系统公告 统御
				if(player.getCommanderLevel()>10)
				{
					String message=InterTransltor.getInstance()
						.getTransByKey(PublicConst.SERVER_LOCALE,
							"commander_level_up");
					message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
					message=TextKit.replace(message,"%",
						player.getCommanderLevel()+"");
					SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
						message);
				}
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.COMMAND_LEVEL_UP);
			}
			else
			{
//				Resources.addResources(player.getResources(),0,0,0,0,
//					PublicConst.BACK_MONEY,player);
				// 失败次数加1
				player.setAttribute(PublicConst.UPCOMMANDER_FAILURE,
					String.valueOf(Number+1));
			}
			player.getBundle().decrProp(PublicConst.COMMANDER_LEVEL_UP_SID);
			data.clear();
			data.writeBoolean(bool);
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.COMMAND_UP_TASK_EVENT,null,player,null);
		}
		// 购买物品并提升统帅
		else if(type==PublicConst.BUY_AND_USE_COMMAND)
		{
			// 不能大于玩家等级
			if(player.getCommanderLevel()>=player.getLevel())
				throw new DataAccessException(0,
					"commanderLevel is over playerLevel");
			// 判断有没到最高等级
			if(player.getCommanderLevel()>=PublicConst.COMMANDER_SUCCESS.length)
				throw new DataAccessException(0,"commanderLevel is limit");
			Prop prop=(Prop)Prop.factory
				.newSample(PublicConst.COMMANDER_LEVEL_UP_SID);
			int fullPrice=prop.getNeedGems();
			// 如果统御书在打折活动中,收取折扣价
			int needGems=ActivityContainer.getInstance().discountGems(
				prop.getSid(),fullPrice);
			// 检查宝石是否足够
			if(!Resources.checkGems(needGems,player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			Resources.reduceGems(needGems,player.getResources(),player);
			// 宝石消费记录
			objectFactory
				.createGemTrack(GemsTrack.BUY_PROP,player.getId(),needGems,
					prop.getSid(),Resources.getGems(player.getResources()));
			if(fullPrice!=needGems)
				ActivityContainer.getInstance().discountGems(prop.getSid(),
					fullPrice,player.getId());
			int rate=PublicConst.COMMANDER_SUCCESS[player
				.getCommanderLevel()];
			// 用于记录升级统御失败次数
			int Number=0;
			if(player.getAttributes(PublicConst.UPCOMMANDER_FAILURE)!=null
				&&!player.getAttributes(PublicConst.UPCOMMANDER_FAILURE)
					.equals(""))
			{
				Number=Integer.parseInt(player
					.getAttributes(PublicConst.UPCOMMANDER_FAILURE));
			}
			// 实际成功概率
			double realrate=(double)rate
				/100
				+(1-(double)rate/100)
				*(double)Math.pow(
					(double)(Number*(double)rate/100/PublicConst.A),
					PublicConst.B);
			// 实际成功率*100
			double Realrate=realrate*100;
			int random=MathKit.randomValue(0,100);
			boolean bool=false;
			// 提升成功
			if(random<Realrate)
			{
				bool=true;
				player.setCommanderLevel(player.getCommanderLevel()+1);
				//成就数据采集
				AchieveCollect.commandLevel(player);
				// 新兵福利
				RecruitKit.pushTask(RecruitDayTask.COMMAND_LV,
					player.getCommanderLevel(),player,true);
				//将升级失败记录设为0
				player.setAttribute(PublicConst.UPCOMMANDER_FAILURE,"0");
				// 系统公告 统御
				if(player.getCommanderLevel()>10)
				{
					String message=InterTransltor.getInstance()
						.getTransByKey(PublicConst.SERVER_LOCALE,
							"commander_level_up");
					message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
					message=TextKit.replace(message,"%",
						player.getCommanderLevel()+"");
					SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
						message);
				}
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.COMMAND_LEVEL_UP);
			}
			else
			{
//				Resources.addResources(player.getResources(),0,0,0,0,
//					PublicConst.BACK_MONEY,player);
				player.setAttribute(PublicConst.UPCOMMANDER_FAILURE,
					String.valueOf(Number+1));
			}
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,needGems);
			data.clear();
			data.writeBoolean(bool);
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.COMMAND_UP_TASK_EVENT,null,player,null);
		}
		// 幸运抽奖
		else if(type==PublicConst.LUCKY_AWARD)
		{
			String message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"lucky_draw_prop2");// 消息待定
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
			// 限时抽奖活动中需要提示的道具，待定

			AwardActivity activity=(AwardActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.AWARD_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity not open");
			}
			int times=activity.getTimes();
			int count=player.getDrawDay();
			if(times!=-1&&count>=times)
			{
				throw new DataAccessException(0,
					"lucky_reward today times limite");
			}
			else
			// 发送奖品包
			{
				if(!Resources.checkGems(activity.getGems(),
					player.getResources()))
					throw new DataAccessException(0,
						"lucky_draw_gem_not_enough");
				if(Resources.reduceGems(activity.getGems(),
					player.getResources(),player))
				{
					objectFactory.createGemTrack(GemsTrack.AWARD_LETO,
						player.getId(),activity.getGems(),0,
						Resources.getGems(player.getResources()));
					// 创建记录
					data.clear();
					activity.getAward().awardLenth(data,player,objectFactory,
						message,SeaBackKit.getLuckySids(),
						new int[]{EquipmentTrack.FROM_CIRCLE_LUCKY,FightScoreConst.LUCKY_CIRCLE});
					player.setAttribute(PublicConst.LUCKY_DRAW,
						activity.getId()+","+(count+1));
					// 发送change消息
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.GEMS_ADD_SOMETHING,this,player,
						activity.getGems());
					//活动日志采集
					String sidnum=getSidNum(data);
					ActivityLogMemCache.getInstance().collectAlog(
						activity.getId(),sidnum,player.getId(),
						activity.getGems());
				}

			}

		}
		// 每日登录奖励
		else if(type==PublicConst.HONOR_GET_DAY)
		{
			int award[]=SeaBackKit.get2ShortInInt(player.getReward());
			int dayYear=SeaBackKit.getDayOfYear();
			// 判断今天是否领取过
			if(dayYear==award[0])
			{
				throw new DataAccessException(0,
					"login_reward today times limite");
			}
			else
			{
				int honor=PublicConst.MILITARY_RANK_HONOR[player
					.getPlayerType()-1];
				// 发放声望
				player.incrHonorExp(honor);
				JBackKit.resetHonor(player);
				if(dayYear-award[0]>1)
					award[1]=0;
				award[1]=award[1]%PublicConst.DAYAWARD.length;
				//if(award[1]<0) award[1]=0;
				Award aw=(Award)Award.factory
					.newSample(PublicConst.DAYAWARD[award[1]]);
				int gems=aw.getGemsAward(player.getLevel());
				Resources.addGemsDaily(gems,player.getResources(),player);

				player.flushVIPlevel();
				// 宝石消费记录
				objectFactory.createGemTrack(GemsTrack.GEMS_DAY_SEND,
					player.getId(),gems,0,
					Resources.getGems(player.getResources()));

				aw.setGemsAward(0);
				aw.awardSelf(player,TimeKit.getSecondTime(),data,
					objectFactory,null,new int[]{EquipmentTrack.FROM_DAY_AWARD});

				player.setReward(((dayYear<<16)+(award[1]+1)
					%PublicConst.DAYAWARD.length));
				data.clear();
				data.writeShort(PublicConst.DAYAWARD[award[1]]);// 奖品包sid
				data.writeShort(honor);
			}
		}
		// 玩家购买能量
		else if(type==PublicConst.BUY_ACTIVES_DAY)
		{
			int nowTimes=player.getTodayBuyTimes();
			// 根据vip等级获取可以购买的次数
			int canBuy=PublicConst.VIP_LEVEL_FOR_ENERGY_BUY_TIME[player
				.getUser_state()];
			if(nowTimes>=canBuy)
			{
				throw new DataAccessException(0,"energy today times limite");
			}
			// 需要宝石数量
			int needGems=nowTimes*EVERY_GEMS+EVERY_GEMS;
			if(!Resources.checkGems(needGems,player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			// 扣费
			Resources.reduceGems(needGems,player.getResources(),player);
			// 宝石消费记录
			objectFactory.createGemTrack(GemsTrack.BUY_ENERGY,
				player.getId(),needGems,nowTimes,
				Resources.getGems(player.getResources()));
			player.addEnergy(EVERY_ACTIVES);
			player.addBuyActivesTime();
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,needGems);
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.BUY_ENERGY_TASK_EVENT,null,player,null);
		}
		// 购买建筑位
		else if(type==PublicConst.BUY_DEQUEN)
		{
			// 当前拥有的建筑位
			int nowBuilds=player.getIsland().getBuildNum();
			if(nowBuilds>=Island.BUILD_MAX)
				throw new DataAccessException(0,"is maxNum");
			// 根据vip等级能否购买
			int canBuyNum=PublicConst.VIP_LEVEL_FOR_BUILD_DEQUE[player
				.getUser_state()];
			if(canBuyNum<=nowBuilds)
				throw new DataAccessException(0,"vip level limit");
			// 需要宝石
			int needGems=PublicConst.VIP_LEVEL_FOR_BUILD_QUEUE_BUY_COST[(nowBuilds-Island.BUILD_NUM)];
			if(!Resources.checkGems(needGems,player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			Resources.reduceGems(needGems,player.getResources(),player);
			player.getIsland().upBuildNum();
			autoUpBuilding.containedPlayer2Up(player);
			// 宝石消费记录
			objectFactory.createGemTrack(GemsTrack.BUY_BUILD_DEQUEN,
				player.getId(),needGems,player.getIsland().getBuildNum(),
				Resources.getGems(player.getResources()));
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,needGems);
			data.clear();
			data.writeByte(player.getIsland().getBuildNum());
		}

		// 设备号和udid
		else if(type==PublicConst.DEVICE_TOKEN)
		{
			// 接管登录状态不更新设备信息，保留原来的
			if(player.getAttributes(PublicConst.TAKE_OVER)!=null
				&&player.getAttributes(PublicConst.TAKE_OVER).length()>0)
			{
				data.clear();
				data.writeShort(HttpServer.DEFAULT_PORT);// http端口
				return data;
			}
			// 登录设备推送token
			String deviceToken=data.readUTF();
			// 去掉前后2个括号
			deviceToken=TextKit.replace(deviceToken,"<","");
			deviceToken=TextKit.replace(deviceToken,">","");
			deviceToken=TextKit.replaceAll(deviceToken," ","");
			player.setDeviceToken(deviceToken);
			// 本地语言
			int locale=data.readUnsignedByte();
			player.setLocale(locale);
			player.setLoginIp(connect.getURL().getHost());
			// 登录设备唯一编号
			String ourUdid=data.readUTF();
			User user=objectFactory.getUserDBAccess().load(
				player.getUser_id()+"");
			String oldOurUdid=user.getLoginUdid();
			player.setLoginUid(ourUdid);
			user.setLoginUdid(ourUdid);
			// 设备型号
			String device=data.readUTF();
			user.setDevice(device);
			// 系统信息
			String osInfo=data.readUTF();
			user.setOsInfo(osInfo);
			// 广告标示符
			String idfa=data.readUTF();
			user.setIdfa(idfa);
			// 客户端版本
			String version=data.readUTF();
			user.setVersion(version);
			// 平台编号
			int platid=data.readUnsignedByte();
			player.setPlatid(platid);
			user.setPlat(configManager.getPlatName(platid));
			player.setPlat(configManager.getPlatName(platid));
//			if(user.getPlat()==null) user.setPlat((String)platMap.get(0));
//			if(player.getPlat()==null)
//				player.setPlat((String)platMap.get(0));
			// 地区代码
			String area=data.readUTF();
			player.setArea(area);
			// 客户端Bundle ID
			String bundleId=data.readUTF();
			// 越狱标识,1为越狱用户
			int flag=data.readInt();
			// 充值设备ip
			String pdid=data.readUTF();
			user.setPayUdid(pdid);
			player.setEscapeDevice(flag);
			player.setBundleId(bundleId);
			
			gameDataMemCache.incrOnline(player.getPlat());
			objectFactory.getUserDBAccess().save(user);

			boolean force=false;
			if(ourUdid==null)
			{
				if(oldOurUdid!=null) force=true;
			}
			else if(!ourUdid.equals(oldOurUdid))
			{
				force=true;
			}
			// 能量发放方式调整为手动领取
//			MealTimeManager.getInstance().checkPlayerMealEnergy(player);
			LoginLogMemCache.loginLogMem.save(player,ourUdid,pdid,force);
			JBackKit.sendRechangeState(player);
		}
		// 收藏岛屿
		else if(type==PublicConst.SAVE_ISLAND_LOCATION)
		{
			int index=data.readInt();
			NpcIsland island=objectFactory.getIslandCache().loadOnly(
				index+"");
			if(island==null)
				throw new DataAccessException(0,"island is null");
			String name=data.readUTF();
			int shou_type=data.readUnsignedByte();
			ArrayList list=player.getLocationSaveList();
			// 收藏夹容量检测
			if(list.size()>=PublicConst.VIP_LEVEL_FOR_ISLAND_LOCATION[player.getUser_state()])
				throw new DataAccessException(0,"favorites_list_capacity_limit");
			for(int i=0;i<list.size();i++)
			{
				if(((IslandLocationSave)list.get(i)).getIndex()==index)
					throw new DataAccessException(0,"index is have");
			}
			IslandLocationSave save=new IslandLocationSave();
			save.setIndex(index);
			save.setName(name);
			save.setType(shou_type);
			player.getLocationSaveList().add(save);
		}
		// 收藏夹的修改
		else if(type==PublicConst.CHANGE_ISLAND_LOCATION)
		{
			int count=data.readUnsignedByte();
			ArrayList saveList=player.getLocationSaveList();
			for(int i=0;i<count;i++)
			{
				int index=data.readInt();
				for(int j=0;j<saveList.size();j++)
				{
					IslandLocationSave save=(IslandLocationSave)saveList
						.get(j);
					if(save.getIndex()==index)
					{
						int changeType=data.readUnsignedByte();
						// 删除
						if(changeType==1)
						{
							saveList.remove(save);
						}
						// 同时修改名字和收藏类型操作
						else if(changeType==2)
						{
							String name=data.readUTF();
							int shou_type=data.readUnsignedByte();
							save.setName(name);
							save.setType(shou_type);
						}
						// 只修改名字操作
						else if(changeType==3)
						{
							String name=data.readUTF();
							save.setName(name);
						}
						// 只修改收藏类型操作
						else if(changeType==4)
						{
							int shou_type=data.readUnsignedByte();
							save.setType(shou_type);
						}
						break;
					}
				}
			}
		}
		// 联盟收藏岛屿
		else if(type==PublicConst.ALLIANCE_SAVE_LOCATION)
		{
			int offset=data.offset()-3;
			int top=data.top();
			int index=data.readInt();
			NpcIsland island=objectFactory.getIslandCache().loadOnly(
				index+"");
			if(island==null)
				throw new DataAccessException(0,"island is null");

			Alliance alliance=null;
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					player.getAttributes(PublicConst.ALLIANCE_ID));
			}
			if(alliance==null)
				throw new DataAccessException(0,"you have no alliance");
			if(!alliance.isMaster(player.getId()))
			{
				throw new DataAccessException(0,"collect_no_right");
			}
//			if(alliance.getLocationSaveList().size()>=PublicConst.ALLIANCE_LOCATION_MAX)
//			{
//				throw new DataAccessException(0,"location max");
//			}
			java.util.ArrayList<IslandLocationSave> list=alliance
				.getLocationSaveList();
			// 收藏夹容量检测
			if(list.size()>=PublicConst.ALLIANCE_ISLANDS_LOCATION[alliance.getAllianceLevel()-1])
					throw new DataAccessException(0,"favorites_list_capacity_limit");
			for(int i=0;i<list.size();i++)
			{
				if(list.get(i).getIndex()==index)
					throw new DataAccessException(0,"index is have");
			}

			String name=data.readUTF();
			int shou_type=data.readUnsignedByte();
			IslandLocationSave save=new IslandLocationSave();
			save.setIndex(index);
			save.setName(name);
			save.setType(shou_type);
			list.add(save);
			// 广播
			data.setOffset(offset);
			sendAllAlliancePlayers(data,alliance,top);
		
			// 联盟事件
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_ISLAND,
				player.getName(),"",name+","+index,TimeKit.getSecondTime());
			alliance.addEvent(event);

		}
		// /联盟岛屿收藏修改
		else if(type==PublicConst.ALLIANCE_CHANGE_LOCATION)
		{
			Alliance alliance=null;
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					player.getAttributes(PublicConst.ALLIANCE_ID));
			}
			if(alliance==null)
				throw new DataAccessException(0,"you have no alliance");
			if(!alliance.isMaster(player.getId()))
			{
				throw new DataAccessException(0,"collect_no_right");
			}
			int offset=data.offset()-3;
			int top=data.top();
			int count=data.readUnsignedByte();
			java.util.ArrayList<IslandLocationSave> saveList=alliance
				.getLocationSaveList();
			String saveWord="";
			for(int i=0;i<count;i++)
			{
				int index=data.readInt();
				for(int j=0;j<saveList.size();j++)
				{
					IslandLocationSave save=saveList.get(j);
					if(save.getIndex()==index)
					{
						int changeType=data.readUnsignedByte();
						// 删除
						if(changeType==1)
						{
							saveWord=changeType+","+save.getName()+","+index;
							saveList.remove(save);
						}
						// 同时修改名字和收藏类型操作
						else if(changeType==2)
						{
							String name=data.readUTF();
							int shou_type=data.readUnsignedByte();
							save.setName(name);
							save.setType(shou_type);
							
						}
						// 只修改名字操作
						else if(changeType==3)
						{
							String name=data.readUTF();
							saveWord=changeType+","+save.getName()+","+index+","+name;
							save.setName(name);
						}
						// 只修改收藏类型操作
						else if(changeType==4)
						{
							int shou_type=data.readUnsignedByte();
							save.setType(shou_type);
						}
						if(saveWord!=null && saveWord.length()!=0)
						{
							// 联盟事件
							AllianceEvent event=new AllianceEvent(
								AllianceEvent.ALLIANCE_EVENT_ICHANGE,
								player.getName(),"",saveWord,TimeKit.getSecondTime());
							alliance.addEvent(event);
						}
						break;
					}
				}
			}
			// 广播
			data.setOffset(offset);
			sendAllAlliancePlayers(data,alliance,top);
		}
		// 新手任务
		else if(type==PublicConst.NEW_PLAYER_TASK)
		{
			if(player.getTaskMark()<100)
			{
				boolean isTaskComplete=data.readBoolean();
				int taskMark=data.readUnsignedShort();
				player.setTaskMark(taskMark);
				player.setPlayerTaskMark(taskMark);
				if(isTaskComplete||taskMark>=PublicConst.NEW_PLAYER_MARK_MAX)
				{
					awardNewComer(player);
					data.clear();
				}
			}
		}
		// 跳过新手任务
		else if(type==PublicConst.JUMP_NEW_TASK)
		{
			// 新手引导跳过步数记录
			String follow=data.readUTF();
			player.setAttribute(PublicConst.CURRENT_NEW_FOLLOW,follow);
			player.setTaskMark(PublicConst.NEW_PLAYER_MARK_MAX);
			awardNewComer(player);
			// 新加新手引导步骤特殊操作(如果玩家在事件过程中跳过,事件清理)
			ArrayList events=objectFactory.getEventCache().getFightEventListById(objectFactory.getIslandCache().getPlayerIsLandId(
				player.getId()));
			if(events!=null)
				for(int i=0;i<events.size();i++)
				{
					FightEvent event=(FightEvent)events.get(i);
					if(event.getDelete()==FightEvent.DELETE_TYPE)
						continue;
					event.setCreatAt(TimeKit.getSecondTime());
					event.setEventState(FightEvent.RETRUN_BACK);
					event.setNeedTimeDB(0);
					FightKit.checkFightEvent(event,player,objectFactory);
				}
			data.clear();
		}
		// 去产品地址
		else if(type==PublicConst.PRODUCE_URL)
		{
			data.clear();
			data.writeUTF(configManager.getShare(player.getPlatid()));
		}
		// 清理军官技能
		else if(type==PublicConst.CLEAR_SKILLS)
		{
			// 检查宝石数量
			if(!Resources.checkGems(PublicConst.CLEAR_SKILL_GEMS,
				player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			Resources.reduceGems(PublicConst.CLEAR_SKILL_GEMS,
				player.getResources(),player);
			// 清除玩家军官技能 退换荣誉勋章
			int num=0;
			// 计算荣誉勋章的个数
			Object skills[]=player.getSkills().getArray();
			for(int i=0;i<skills.length;i++)
			{
				if(skills[i]==null) continue;
				num+=skillNums((Skill)skills[i]);
			}
			// 添加荣誉勋章
			NormalProp prop=(NormalProp)Prop.factory
				.newSample(PublicConst.UP_SKILL_PROP_SID);
			prop.setCount(num);
			player.getBundle().incrProp(prop,true);
			player.getSkills().clear();
			data.clear();
			data.writeShort(num);
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				PublicConst.CLEAR_SKILL_GEMS);
			// 宝石消费记录
			objectFactory.createGemTrack(GemsTrack.RESET_SKILL,
				player.getId(),PublicConst.CLEAR_SKILL_GEMS,0,
				Resources.getGems(player.getResources()));
			JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.CLEAR_PLAYER_SKILLS);
		}
		// 取玩家
		else if(type==PublicConst.GET_PLAYER_INFO)
		{
			String playerName=data.readUTF();
			Player viewPlayer=objectFactory
				.getPlayerByName(playerName,false);
			if(viewPlayer==null)
			{
				if(SeaBackKit.isContainValue(PublicConst.NONEPLAYER_NAME,
					playerName))
				{
					throw new DataAccessException(0,
						"player is null");
				}
				throw new DataAccessException(0,"player is already modify name");
			}
			String allianceName="";
			if(viewPlayer.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!viewPlayer.getAttributes(PublicConst.ALLIANCE_ID).equals(
					""))
			{
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().load(
						viewPlayer.getAttributes(PublicConst.ALLIANCE_ID));
				if(alliance!=null)
				{
					allianceName=alliance.getName();
				}
				// 如果玩家身上的联盟标记无效，则进行清除
				else
					viewPlayer.setAttribute(PublicConst.ALLIANCE_ID,null);
			}
			data.clear();
			data.writeByte(viewPlayer.getLevel());
			data.writeByte(viewPlayer.getPlayerType());
			data.writeUTF(allianceName);
			data.writeShort(viewPlayer.getSid());
			data.writeInt(viewPlayer.getFightScore());
			data.writeInt(viewPlayer.getCommanderLevel());
			if(viewPlayer.getProsperityInfo()[1]==0){//繁荣度检测时间为0
				viewPlayer.getIsland().gotProsperityInfo(TimeKit.getSecondTime());//重置繁荣度
			}
			data.writeInt(viewPlayer.getProsperityInfo()[0]);//繁荣度指数
			data.writeInt(viewPlayer.getProsperityInfo()[2]);//繁荣度MAX
			data.writeInt(viewPlayer.getAttrHead());
			data.writeInt(viewPlayer.getAttrHeadBorder());
		}
		// // 获取邀请奖励
		// else if(type==PublicConst.GET_INVETD_AWARD)
		// {
		// int inveted[]=player.getInviter_id();
		// for(int i=0;i<inveted.length;i+=2)
		// {
		// int state=inveted[i+1];
		// if(state==0)
		// {
		// player.getInviter_id()[i+1]=1;
		// Prop prop=(Prop)Prop.factory
		// .newSample(PublicConst.PROP_SID);
		// if(prop!=null) player.getBundle().incrProp(prop,true);
		// }
		// }
		// JBackKit.sendResetBunld(player);
		// }
		// 获取宝石奖励 填写了邀请者ID的
		// else if(type==PublicConst.BE_GET_INVETD_GEMS)
		// {
		// if(player.getInveted()==0)
		// throw new DataAccessException(0,"inveted is wrong");
		// Resources.addGemsNomal(PublicConst.GEMS,player.getResources());
		// // 奖励宝石
		// JBackKit.sendResetResources(player);
		// }
		else if(type==PublicConst.FREE_LOTTO
				||type==PublicConst.LOW_LOTTO
				||type==PublicConst.HIGH_LOTTO
				||type==PublicConst.LOTTO_STATE)
		{
			return getLotto(player,type,data);
		}
		else if(type==PublicConst.ADD_APPLY_FRIEND)//添加好友申请
		{
			String name=data.readUTF();
			Player friend=objectFactory.getPlayerByName(name,false);
			String ret=addApplyFriend(player,friend);
			if(ret!=null) throw new DataAccessException(0,ret);
		}else if(type==PublicConst.ADD_FRIEND)//添加好友
		{
			int eventId=data.readInt();
			String name=data.readUTF();
			boolean agree = data.readBoolean();
			Player friend=objectFactory.getPlayerByName(name,false);
			String ret=addFriend(player,friend,eventId,agree);
			if(ret!=null) throw new DataAccessException(0,ret);
		}
		else if(type==PublicConst.REMOVE_FRIEND)//移除好友
		{
			String name=data.readUTF();
			Player friend=objectFactory.getPlayerByName(name,false);
			if(friend==null)
				throw new DataAccessException(0,"player is null");
			player.getFriendInfo().reMoveFriend(player,friend,objectFactory);
		
		}
		else if(type==PublicConst.ADD_BLACK)
		{
			String name=data.readUTF();
			Player black=objectFactory.getPlayerByName(name,false);
			String ret=addBlackList(player,black.getId());
			if(ret!=null) throw new DataAccessException(0,ret);
			data.clear();
			data.writeInt(black.getAttrHead());
			data.writeInt(black.getAttrHeadBorder());
		}
		else if(type==PublicConst.REMOVE_BLACK)
		{
			String name=data.readUTF();
			Player black=objectFactory.getPlayerByName(name,false);
			if(black==null)
				throw new DataAccessException(0,"player is null");
			String ret= reMoveBlackList(player,black.getId());
			if(ret!=null) throw new DataAccessException(0,ret);
		}
		else if(type==PublicConst.GET_INTIMACY_LUCKY)//亲密度抽奖
		{
			int intimacy = player.getFriendInfo().getIntimacy();//当前亲密度
			
			if(intimacy<FriendInfo.INTIMACY_LUCKY_NEED_NUM)
				throw new DataAccessException(0,"initmacy is not enough");
			
			player.getFriendInfo().reduceIntimacy(FriendInfo.INTIMACY_LUCKY_NEED_NUM);
			String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"lucky_intimacy_prop");
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
			Award award=(Award)Award.factory.newSample(FriendInfo.INTIMACY_LUCKY_AWARD_ID);
			// 创建记录
			data.clear();
			award.awardLenth(data,player,
				objectFactory,message,SeaBackKit.getLuckySids(),
				new int[]{EquipmentTrack.FROM_INTIMACY_LUCKY,FightScoreConst.LUCK_INTIMACY});
				
		}
		else if(type==PublicConst.GIVE_INTIMACY)
		{
			int friendId = data.readInt();
			Player friend = objectFactory.getPlayerById(friendId);
			String ret= player.getFriendInfo().giveIntimacy(player,friend,objectFactory);
			if(ret!=null) throw new DataAccessException(0,ret);
			
		}
		else if(type==PublicConst.RECEVIED_INTIMACY)
		{
			int friendId = data.readInt();
			String ret= player.getFriendInfo().receivedIntimacy(player,friendId,objectFactory);
			if(ret!=null) throw new DataAccessException(0,ret);
		}
		else if(type==PublicConst.FIRST_PAY_AWARD)
		{
			if(player.getResources()[Resources.MAXGEMS]
				-player.getDailyGemsCount()<=0)
				throw new DataAccessException(0,"must pay");
			if(player.getAttributes(PublicConst.FP_AWARD)==null)
				throw new DataAccessException(0,"must pay");
			int fp=Integer.parseInt(player.getAttributes(PublicConst.FP_AWARD));
			int sub=data.readUnsignedByte();
			if((fp&(1<<sub))!=0)
				throw new DataAccessException(0,"had got fp award");
			int gems=fp>>>16;
			Award award=(Award)Award.factory
				.newSample(PublicConst.F_PAY_AWARD[sub]);
			if(sub==0)award.setGemsAward(gems);
			data.clear();
			player.setAttribute(PublicConst.FP_AWARD,(fp|(1<<sub))+"");
			data.writeBoolean(player.getCanFAward());
			data.writeByte(player.getFPAward());
			award.awardLenth(data,player,objectFactory,null,null,new int[]{EquipmentTrack.FROM_FIRST_PAY});
		}
		else if(type==PublicConst.CHANGE_PLAYER)
		{
			int playerId=data.readInt();
			Player oplayer=objectFactory.getPlayerById(playerId);
			if(oplayer==null)
				throw new DataAccessException(0,"player_is_null");
			User user=objectFactory.getUserDBAccess().loadById(String.valueOf(oplayer.getUser_id()));
			if(user==null)
				throw new DataAccessException(0,"user_is_null");
			int[] players=user.getPlayerIds();
			if(players==null||players.length==0)
				throw new DataAccessException(0,"player_is_error");
			boolean b=false;
			for(int i=0;i<players.length;i++)
			{
				// 如果要切换的playerid属于该账号，且不是当前登录player
				if(players[i]==playerId&&user.getPlayerId()!=playerId)
				{
					user.setPlayerId(playerId);
					objectFactory.getUserDBAccess().save(user);
					b=true;
					break;
				}
			}
			if(!b)
				throw new DataAccessException(0,"player_is_not_users");
			data.clear();
		}
		//月卡领取宝石奖品
		else if(type==PublicConst.PLAYER_CARDAWARD)
		{
			String str=isAbleMouthCard(player);
			if(str!=null) throw new DataAccessException(0,str);
			player.setAttribute(PublicConst.AWARD_TIME,
				String.valueOf(TimeKit.getSecondTime()));
			Resources.addGemsNomal(PublicConst.MOUTHCARDAWARD,
				player.getResources(),player);
			objectFactory.createGemTrack(GemsTrack.MOUTHCARD_GET,player.getId(),
				PublicConst.MOUTHCARDAWARD,0,
				Resources.getGems(player.getResources()));
			data.clear();
			data.writeByte(0);
		}
		//领取活跃度奖励
		else if(type==PublicConst.VITALITY_AWARD)
		{
			int awardIndex=data.readByte();
			data.clear();
			if(player.getVitalityAward(awardIndex)){
				data.writeByte(0);
				data.writeByte(player.getVitality(objectFactory)[2]);
				return data;
			}
			int index=0;
			int[] award=new int[2];
			int[] awards=TaskManager.vitalityAward;
			for(int i=0;i<awards.length;i+=2)
			{
				index++;
				if(awardIndex==index)
				{
					award[0]=awards[i];
					award[1]=awards[i+1];
					break;
				}
			}
			if(player.getVitality(objectFactory)[1]>=award[0])
			{
				Award realAward=(Award)Award.factory.newSample(award[1]);
				if(realAward!=null)
				{
					realAward.awardLenth(data,player,
						objectFactory,null,null,null);
					player.setVitalityAward(awardIndex);
					
					data.writeByte(player.getVitality(objectFactory)[2]);
				}else{
					data.writeByte(0);
					data.writeByte(player.getVitality(objectFactory)[2]);
				}
			}
		}
		else if(type==PublicConst.GET_LUCKY_AWARD)
		{
			AwardActivity activity=(AwardActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.AWARD_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity not open");
			}
			// 奖品包信息
			// 创建记录
			data.clear();
			int[] props=activity.getAward().getRandomProps();// 获取活动全部奖励品
			int propsLength=props.length/3;
			data.clear();
			data.writeByte(propsLength);
			for(int i=0;i<propsLength;i++)
			{
				data.writeInt(props[3*i+1]);
				data.writeShort(props[3*i]);
			}

		}
		//军需乐透引导步骤
		else if(type==PublicConst.SET_LOTTO_FOLLOW){
			boolean isComplete=data.readBoolean();
			int follow=data.readShort();
			if(isComplete) follow=100;
			player.setAttribute(PublicConst.BASIC_LOTTO_FOLLOW,follow+"");
		}
		//重置活跃度任务
		else if(type==PublicConst.RESET_VITALITY){
			player.getVitality(objectFactory);
		}
		//领取在线奖励
		else if(type==PublicConst.ONLINE_LUCKY)
		{
			data.clear();
			OnlineLuckyContainer.getInstance().checkAward(player,
				objectFactory,data);
		}
		else if(type==PublicConst.GET_ONLINE_LUCKY)
		{
			data.clear();
			int[] info=OnlineLuckyContainer.getInstance().getPlayerAward(
				player);
			data.writeShort(info[0]);
			data.writeByte(info[1]);
		}
		// 经典幸运抽奖(无动画界面，可变长度随机奖励)
		else if(type==PublicConst.LUCKY_AWARD_CLASSIC)
		{
			String message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"lucky_draw_prop");// 消息待定
			message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
			// 限时抽奖活动中需要提示的道具，待定

			AwardActivity activity=(AwardActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.AWARD_CLASSIC_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity not open");
			}
			int times=activity.getTimes();
			int count=player.getClassicDrawDay();
			if(times!=-1&&count>=times)
			{
				throw new DataAccessException(0,
					"lucky_reward today times limite");
			}
			else
			// 发送奖品包
			{
				if(!Resources.checkGems(activity.getGems(),
					player.getResources()))
					throw new DataAccessException(0,
						"lucky_draw_gem_not_enough");
				if(Resources.reduceGems(activity.getGems(),
					player.getResources(),player))
				{
					objectFactory.createGemTrack(GemsTrack.AWARD,
						player.getId(),activity.getGems(),0,
						Resources.getGems(player.getResources()));
					// 创建记录
					data.clear();
					activity.getAward().awardLenth(data,player,
						objectFactory,message,SeaBackKit.getLuckySids(),
						new int[]{EquipmentTrack.FROM_CLASSIC_LUCKY,FightScoreConst.LUCKY_CLASSIC});
					player.setAttribute(PublicConst.LUCKY_DRAW_CLASSIC,
						activity.getId()+","+(count+1));
					// 发送change消息
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.GEMS_ADD_SOMETHING,this,player,
						activity.getGems());
					// 活动日志采集
					String sidnum=getSidNum(data);
					ActivityLogMemCache.getInstance().collectAlog(
						activity.getId(),sidnum,player.getId(),
						activity.getGems());
				}

			}

		}
		//删除玩家
		else  if(type==PublicConst.DELETE_PLAYER)
		{
			User user=objectFactory.getUserDBAccess().load(
				player.getUser_id()+"");
			if(user==null) throw new DataAccessException(0,"user is null");
			if(user.getUserType()==User.GUEST) throw new DataAccessException(0,"guest  can not delete");
			int timeNow=TimeKit.getSecondTime();
			data.clear();
			data.writeByte(SET_USER_STATE);
			data.writeUTF(user.getUserAccount());
			data.writeInt(timeNow);
			//大区
			data.writeInt(UserToCenterPort.AREA_ID);
			data=sendHttpData(data);
			if(data==null)
				throw new DataAccessException(0,"center is close");
			user.setDeleteTime(timeNow);
			objectFactory.getUserDBAccess().save(user);
			data.clear();
			data.writeBoolean(true);
		}
		// 帝国援军
		else if(type==PublicConst.EXTRA_GIFT_AWARD)
		{
			int[] infos=player.getExtraAwardInfo();
			if(infos!=null)
			{
				Award award=(Award)Award.factory.getSample(infos[1]);
				if(infos[0]==0&&award!=null)
				{
					player.setNextExtraAward();
					award.awardLenth(data,player,objectFactory,null,null);
					JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.EMPIRE_RELIEF);
				}
			}
			data.clear();
			player.showByteWriteExtraGift(data);
		}
		//解锁绑定账号按钮
		else if(type==PublicConst.BINDING_ACCOUNT)
		{
			data.clear();
			User user=objectFactory.getUserDBAccess().load(
				player.getUser_id()+"");
			if(user==null) throw new DataAccessException(0,"user is null");
			if(user.getUserType()!=User.USER)
				throw new DataAccessException(0,"user is not binding");
			//判断当前玩家绑定账号的时间
			String time=player.getAttributes(PublicConst.ACCOUNT_TIME);
			if(time==null || time.length()==0)
					data.writeInt(0);
			else
			{
				int needtime=TimeKit.getSecondTime()-TextKit.parseInt(time);
				if(needtime>EMAIL_SEND_TIME)
				{
					String info=player.getAttributes(PublicConst.ACCOUNT_INFO);
					if(info!=null&&info.length()!=0)	
					{
						String ac_info[]=info.split(",");
						if(TextKit.parseInt(ac_info[0])==0)
							throw new DataAccessException(0,"player is open deblocking");
					}
				}
				else
					data.writeInt(EMAIL_SEND_TIME-needtime);
			}
		}
		/**发送邮件**/
		else if(type==PublicConst.SENDING_EMAIL)
		{
			User user=objectFactory.getUserDBAccess().load(
				player.getUser_id()+"");
			if(user==null) throw new DataAccessException(0,"user is null");
			String infos=player.getAttributes(PublicConst.ACCOUNT_INFO);
			// 是否需要去除玩家发送的次数
			if(infos!=null&&infos.split(",").length==4)
			{
				if(!SeaBackKit.isSameDay(TimeKit.getSecondTime(),
					TextKit.parseInt(infos.split(",")[1])))
					infos=getString(infos,3);
				if(TextKit.parseInt(infos.split(",")[3])>DAY_SEND_LENGTH)
					throw new DataAccessException(0,"email times out");
			}
			String deviceId=data.readUTF();
			int result=passwordManager.sendMail(user,SEND_MAIL_LENGTH,
				deviceId);
			if(result==0)
				throw new DataAccessException(0,"email send fail");
			StringBuffer info=new StringBuffer();
			info.append("0,").append(TimeKit.getSecondTime()).append(",")
				.append(result).append(",");
			if(infos==null||infos.length()==0||infos.split(",").length!=4)
				info.append("1");
			else
				info.append(TextKit.parseInt(infos.split(",")[3])+1);
			player.setAttribute(PublicConst.ACCOUNT_INFO,info.toString());
		}
		/**验证码**/
		else if(type==PublicConst.COM_CODE)
		{
			String code=data.readUTF();
			data.clear();
			String infos=player.getAttributes(PublicConst.ACCOUNT_INFO);
			if(infos==null||infos.length()==0)
				throw new DataAccessException(0,"please send email again");
			if(TextKit.parseInt(infos.split(",")[0])==1)
				throw new DataAccessException(0,"account aleady deblock");
			String[] info=infos.split(",");
			if((TimeKit.getSecondTime()-TextKit.parseInt(info[1]))>UCODE_TIME)
				throw new DataAccessException(0,"codeinfo is invalid");
			if(code.equals(info[2]))
			{
				player.setAttribute(PublicConst.ACCOUNT_INFO,"1");
				data.writeBoolean(true);
			}
			else
				throw new DataAccessException(0,"codeinfo is incorrect");
		}
		/**去除解锁**/
		else if(type==PublicConst.LOCK_ACCOUNT)
		{
			data.clear();
			String info=player.getAttributes(PublicConst.ACCOUNT_INFO);
			if(info==null||info.length()==0
				||TextKit.parseInt(info.split(",")[0])==0)
				throw new DataAccessException(0,"account has unlock");
			info=getString(info,0);
			player.setAttribute(PublicConst.ACCOUNT_INFO,info);
			data.writeBoolean(true);
		}
		// 设置显示需要的统御升级幸运值
		else if(type==PublicConst.SET_COMMAND_LUCKY)
		{
			int lucky=data.readInt();
			data.clear();
			player.setAttribute(PublicConst.COMMAND_UP_LUCKY,lucky+"");
		}
		// 新手引导中切换前台进程时(玩家在新手引导中流失)
		else if(type==PublicConst.NEW_TASK_BG)
		{
			// 新手引导跳过步数记录
			String follow=data.readUTF();
			if(player.getAttributes(PublicConst.NEW_PLAYER_AWARD)==null)
				player.setAttribute(PublicConst.CURRENT_NEW_FOLLOW,follow);
		}
		else if(type==PublicConst.MEALTIME_ENERGY)
		{
			data.clear();
			int energy=MealTimeManager.getInstance().checkPlayerMealEnergy(player);
			if(energy<=0)
				throw new DataAccessException(0,"mealtime energy calming down");
		}
		else if(type==PublicConst.BUY_GROWTH_PLAN)
		{
			data.clear();
			String msg=planManager.buyPrivatePlan(player,objectFactory);
			if(msg!=null)
				throw new DataAccessException(0,msg);
		}
		else if(type==PublicConst.GET_PRIVATE_GROWTH)
		{
			int value=data.readUnsignedByte();
			String msg=planManager.openPrivateAward(player,value,objectFactory);
			if(msg!=null)
				throw new DataAccessException(0,msg);
			data.clear();
			data.writeByte(value);
		}
		else if(type==PublicConst.GET_SERVER_GROWTH)
		{
			int value=data.readInt();
			String msg=planManager.openServerAward(player,value,objectFactory);
			if(msg!=null)
				throw new DataAccessException(0,msg);
			data.clear();
			data.writeInt(value);
		}
/*		else if(type==PublicConst.GET_INC_PROSPERTITY_INFO)
		{// 获取需要增加的繁荣度和需要消耗的宝石
			data.clear();
			int prosperity=player.getIsland().gotProsperityInfo(TimeKit.getSecondTime());
			int needGems=player.getProsperityInfo()[2]-prosperity;
			if(needGems>=Player.PROSPERITY_MAX_GEMS)
				data.writeShort(Player.PROSPERITY_MAX_GEMS);
			else
				data.writeShort(needGems);
		}*/
		else if(type==PublicConst.INC_PROSPERTITY){// 宝石增加繁荣度 port 1010 type 65
			int useGems = data.readShort();//使用的宝石数
			if(!Resources.checkGems(useGems,player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			data.clear();
			int prosperity=player.getIsland().gotProsperityInfo(TimeKit.getSecondTime());
			int needGems=player.getProsperityInfo()[2]-prosperity;
			if(needGems>=Player.PROSPERITY_MAX_GEMS)
				needGems = Player.PROSPERITY_MAX_GEMS;
			if(needGems>useGems)//需要更多的宝石才提示
				throw new DataAccessException(0,"inc prospertity need more gems");
			// 扣费
			Resources.reduceGems(needGems,player.getResources(),player);
			// 宝石消费记录
			objectFactory.createGemTrack(GemsTrack.BUY_PROSPERITY,
				player.getId(),needGems,TimeKit.getSecondTime(),
				Resources.getGems(player.getResources()));
			player.getProsperityInfo()[0] = player.getProsperityInfo()[2];//把繁荣度设置为最大
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				needGems);
			JBackKit.sendResetProsperity(player);//刷新前台
		}else if (type == PublicConst.PLAYER_CHANGE_HEAD) {//修改玩家头像 type 69
			int headSid = data.readInt();
			if (headSid > 0) {
				player.changeCurrentHead(headSid);
				data.clear();
				data.writeInt(player.getCurrentHeadSid());
			}
		} 
		else if (type == PublicConst.PLAYER_CHANGE_BORDER) {//修改玩家头像边框 type 70
			int borderId = data.readInt();
			if (borderId >= 0) {
				player.changeHeadBorder(borderId);
				data.clear();
				data.writeInt(player.getAttrHeadBorder());
			}
		} 
		/**设置关卡buff**/
		else if(type==PublicConst.SET_POINT_BUFF)
		{
			int sid=data.readShort();
			int pointType =-1;
			for(int i=0;i<PublicConst.SHOW_SIDS.length;i++)
			{
				if(sid==PublicConst.SHOW_SIDS[i])
				{
					pointType=i;
					break;
				}
			}
			if(type<0) throw new DataAccessException(0,"pointbuff is not exist");
			if(player.getPointBuff().length<pointType)
				throw new DataAccessException(0,"pointbuff is not open");
			if(player.getPointBuff()[pointType]==Chapter.USE_STATE)
				throw new DataAccessException(0,"point buff is open");
			player.usePointBuff(pointType,objectFactory);
		}
		return data;
	}
	/***
	 * 
	 * @return
	 */
	public String isAbleMouthCard(Player player)
	{
		String etime=player.getAttributes("endtime");
		if(etime==null) return "No Monthly ticket ";//月票没有
		int timenow=TimeKit.getSecondTime();
		int endtime=Integer.parseInt(etime);
		if(endtime<=timenow) return  "Monthly ticket has expired ";//月票已到期
		int awardtime=player.getAttributes("awardtime")==null?0:Integer.parseInt(player.getAttributes("awardtime"));
		if(SeaBackKit.isSameDay(awardtime,timenow))  return "Today has brought";//今天已经领取
		return null;
	}
	
	/**
	 * 获取抽奖奖励
	 * 
	 * @param type
	 * @param data
	 * @return
	 */
	private ByteBuffer getLotto(Player player,int type,ByteBuffer data)
	{
		String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"lotto_special_prop");
		message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
		// 抽奖活动中需要提示的道具
		int[] lottoSid=SeaBackKit.getLuckySids();
//		if(type==PublicConst.LOW_LOTTO||type==PublicConst.HIGH_LOTTO)
//		{
//			int length=data.readByte();
//			int position=data.readByte();
//			int awardSid=0;
//			if(type==PublicConst.LOW_LOTTO)
//				awardSid=player.getBasicLotto()[0];
//			else
//				awardSid=player.getBasicLotto()[1];
//			if(awardSid!=0)
//			{
//				if(type==PublicConst.LOW_LOTTO)
//					player.setLowLotto(0);
//				else
//					player.setHighLotto(0);
//				startLotto(type,player,data,awardSid,lottoSid,message,
//					length,position);
//			}
//			else
//			{
//				throw new DataAccessException(0,"lotto_count_limit");
//			}
//		}
//		else if(type==PublicConst.LOTTO_STATE)
//		{
			int lottoType=type;
//			int lottoType=data.readByte();
//			data.clear();
//			data.writeByte(lottoType);
			int awardSid=0;
			// 免费抽奖加入强制间隔时间
			if(lottoType==PublicConst.FREE_LOTTO)
			{
				int now=TimeKit.getSecondTime();
				int count=player.getLottoCount(PublicConst.LOTTO_FREE);
				if(SeaBackKit.getFreeLottoTime(player,now)>0)
				{
					throw new DataAccessException(0,"lotto_time_limit");
				}
				else if(count<PublicConst.LOTTO_MAX_1
					&&player.incrLottoCount(PublicConst.LOTTO_FREE,1))
				{
					player.setAttribute(PublicConst.LAST_FREE_LOTTO,now+"");
					awardSid=51002;
				}
				else
					throw new DataAccessException(0,"lotto_count_limit");
					
			}
			else if(lottoType==PublicConst.LOW_LOTTO)
			{
				boolean isUseProp=data.readBoolean();
				if((player.getBundle().getCountBySid(SILVER_WHEEL)>0&&player
					.getBundle().decrProp(SILVER_WHEEL,1)))
				{
					awardSid=51002;
//					JBackKit.sendResetBunld(player);
				}
				// 如果前台提示的是使用物品,那么提示玩家物品数量不正常
				else if(isUseProp)
				{
					throw new DataAccessException(0,"not enough prop");
				}
				else if(Resources.getGems(player.getResources())>=PublicConst.LOTTO_NEED_GEM_2
					&&Resources.reduceGems(PublicConst.LOTTO_NEED_GEM_2,
						player.getResources(),player))
				{
					// 宝石消费记录
					objectFactory.createGemTrack(GemsTrack.LOTTO,
						player.getId(),PublicConst.LOTTO_NEED_GEM_2,
						PublicConst.LOTTO_ADVANCE,
						Resources.getGems(player.getResources()));
					// 发送change消息
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.GEMS_ADD_SOMETHING,this,player,
						PublicConst.LOTTO_NEED_GEM_2);
					awardSid=51002;
				}
				if(awardSid!=0)
				{
//					player.setLowLotto(awardSid);
				}
				else
				{
					throw new DataAccessException(0,"lotto_gem_not_enough");
				}
			}
			else
			{
				boolean isUseProp=data.readBoolean();
				int goldenWheel=player.getBundle().getCountBySid(
					GOLDEN_WHEEL);
				if(goldenWheel>=1
					&&player.getBundle().decrProp(GOLDEN_WHEEL,1))
				{
					awardSid=51003;
//					JBackKit.sendResetBunld(player);
//					player.setHighLotto(awardSid);
				}
				// 如果前台提示的是使用物品,那么提示玩家物品数量不正常
				else if(isUseProp)
				{
					throw new DataAccessException(0,"not enough prop");
				}
				else if(Resources.getGems(player.getResources())>=PublicConst.LOTTO_NEED_GEM_3
					&&Resources.reduceGems(PublicConst.LOTTO_NEED_GEM_3,
						player.getResources(),player))
				{
					awardSid=51003;
					// 宝石消费记录
					objectFactory.createGemTrack(GemsTrack.LOTTO,
						player.getId(),PublicConst.LOTTO_NEED_GEM_3,
						PublicConst.LOTTO_LUXURY,
						Resources.getGems(player.getResources()));
					// 发送change消息
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.GEMS_ADD_SOMETHING,this,player,
						PublicConst.LOTTO_NEED_GEM_3);
//					player.setHighLotto(awardSid);
				}
				else
				{
					throw new DataAccessException(0,"lotto_gem_not_enough");
				}
			}
			data.clear();
			// TODO 兼容代码
			if(awardSid!=0)
			{
				if(lottoType==PublicConst.FREE_LOTTO)
				{
					// 新军需乐透当前免费次数
					data.writeByte(player.getLottoCount(PublicConst.LOTTO_FREE));
					// 免费强制间隔时间
					data.writeInt(SeaBackKit.getFreeLottoTime(player,TimeKit.getSecondTime()));
				}
				startLotto(type,player,data,awardSid,lottoSid,message,
					0,0);
				JBackKit.sendResetBunld(player);
			}
			else
			{
				throw new DataAccessException(0,"lotto_count_limit");
			}
//		}
		return data;
	}

	/** 计算某个技能总共消费的荣誉勋章 */
	public int skillNums(Skill skill)
	{
		int needNum=0;
		int size=skill.getLevel()-1;
		for(int i=size;i>=0;i--)
		{
			skill.setLevel(i);
			needNum+=skill.getNeedLevelPropNum();
		}
		return needNum;
	}
	
	/**
	 * 添加好友申请
	 * @param player 自己
	 * @param friendId 好友ID
 	 * @return
	 */
	public String addApplyFriend(Player player,Player friend){
		
		if(friend==null || TextKit.parseInt(friend
			.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE) return "player is null";//检索ID是否存在
		int friendId = friend.getId();
		IntKeyHashMap friendMap = player.getFriendInfo().getFriends();
		if(friendMap.size()>=PublicConst.MAX_FRIEND_LIST_COUNT) return "friend_count_error_friends_list";//检索是否好友满员
		if(friendId==player.getId())  return "add_player_is_yourself";//不能添加自己
		if(player.getFriendInfo().getBlackList().get(friend.getId())!=null) return "is_exists_in_black_list";//不能添加黑名单里面的人
		if(friendMap.get(friendId)!=null) return "is_exists_in_friends_list";//是否已经存在
		player.getFriendInfo().applyFriend(player,friend,objectFactory); 
		return null;
	}
	
	/**
	 * 添加好友
	 * @param player
	 * @param friend
	 * @return
	 */
	public String addFriend(Player player,Player friend,int eventId,boolean agree){
		if(agree){
		if(friend==null || TextKit.parseInt(friend
			.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE) return "player is null";//检索ID是否存在
		int friendId = friend.getId();
		IntKeyHashMap friendMap = player.getFriendInfo().getFriends();
		if(friendMap.size()>=PublicConst.MAX_FRIEND_LIST_COUNT) return "friend_count_error_friends_list";//检索是否好友满员
		if(friendId==player.getId())  return "add_player_is_yourself";//不能添加自己
		if(friendMap.get(friendId)!=null) return "is_exists_in_friends_list";//是否已经存在
		if(player.getFriendInfo().getBlackList().get(friend.getId())!=null) return "is_exists_in_black_list";//不能添加黑名单里面的人
		if(friend.getFriendInfo().getBlackList().get(player.getId())!=null) return "your request has been rejected";//不能添加黑名单里面的人
		}
		player.getFriendInfo().addFriend(player,friend,eventId,agree,objectFactory);
		return null;
	}
	
	
	
	/**
	 * 添加到黑名单
	 * @param player 自己
	 * @param blackId 黑名单ID
	 * @return
	 */
	public String addBlackList(Player player,int blackId){
		Player friend = objectFactory.getPlayerById(blackId);
		if(friend==null || TextKit.parseInt(friend
			.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE) return "player is null";//检索ID是否存在
		IntKeyHashMap blackMap = player.getFriendInfo().getBlackList();
		if(blackMap.size()>=PublicConst.MAX_FRIEND_LIST_COUNT) return "friend_count_error_black_list";//检索是否黑名单满员
		if(blackId==player.getId()) return "add_player_is_yourself";//不能添加自己
		if(blackMap.get(blackId)!=null) return "is_exists_in_black_list";//是否已经存在
		if(player.getFriendInfo().getFriends().get(blackId)!=null)player.getFriendInfo().reMoveFriend(player,friend,objectFactory);//删除好友
		blackMap.put(blackId,friend);//添加黑名单
		
		return null;
	}
	
	public String reMoveBlackList(Player player,int blackId){
		IntKeyHashMap blackList = player.getFriendInfo().getBlackList();
		blackList.remove(blackId);
		return null;
	}
	

	/**
	 * 添加一个好友
	 * 
	 * @param player
	 * @param friendName
	 * @return
	 */
	@Deprecated
	private String addFriend(Player player,String friendName,String listKey,
		int maxCount)
	{
		if(friendName==null||friendName.length()==0)
			return "friend_player_not_exists";
		Player friend=objectFactory.getPlayerByName(friendName,false);
		if(friend==null || TextKit.parseInt(friend
			.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE) return "player is null";
		String[] friends=getFriendList(player,listKey);
		if(friends.length>=maxCount) return "friend_count_error_"+listKey;
		if(player.getName().equals(friendName))
			return "can_not_add_myself_"+listKey;
		String strFriends=player.getAttributes(listKey);
		// 添加时从另一个列表移除，并判断是否重复添加
		if(PublicConst.FRIENDS_LIST.equals(listKey))
		{
			removeFriend(player,friendName,PublicConst.BLACK_LIST);
			if(SeaBackKit.isInFriendList(player,friendName))
				return "is_exists_in_"+listKey;
		}
		else
		{
			removeFriend(player,friendName,PublicConst.FRIENDS_LIST);
			if(SeaBackKit.isInFriendList(player,friendName))
				return "is_exists_in_"+listKey;
		}
		if(strFriends==null||strFriends.length()==0)
			strFriends=friendName;
		else
			strFriends=strFriends+","+friendName;
		player.setAttribute(listKey,strFriends);
		return null;
	}

	@Deprecated
	private String removeFriend(Player player,String friendName,
		String listKey)
	{
		String[] friends=getFriendList(player,listKey);
		boolean isHave=false;
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<friends.length;i++)
		{
			if(friends[i].equals(friendName))
			{
				isHave=true;
			}
			else
			{
				if(sb.length()==0)
					sb.append(friends[i]);
				else
					sb.append(",").append(friends[i]);
			}
		}
		if(!isHave)
			return "friend_not_exists_"+listKey;
		else
			player.setAttribute(listKey,sb.toString());
		return null;
	}

	private String[] getFriendList(Player player,String listKey)
	{
		String[] strs={};
		if(player==null) return strs;
		String friendStr=player.getAttributes(listKey);
		if(friendStr==null) return strs;
		strs=TextKit.split(friendStr,",");
		return strs;
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

	/** 联盟广播 */
	public void sendAllAlliancePlayers(ByteBuffer data,Alliance alliance,
		int top)
	{
		data.setTop(data.offset());
		data.writeShort(AlliancePort.ALLIANCE_PORT);
		data.setTop(top);
		SessionMap smap=objectFactory.getDsmanager().getSessionMap();
		Session[] sessions=smap.getSessions();
		Player player=null;
		Connect con=null;
		IntList list=alliance.getPlayerList();
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					for(int j=0;j<list.size();j++)
					{
						if(player.getId()==list.get(j))
						{
							con.send(data);
						}
					}
				}
			}
		}
	}
	
	/** 获取幸运抽奖物品 （物品日志特殊需求）*/
	public String getSidNum(ByteBuffer data)
	{
		int offset=data.offset();
		data.readUnsignedByte();
		data.readUnsignedByte();
		int num=data.readInt();
		int sid=data.readUnsignedShort();
		data.setOffset(offset);
		return sid+"x"+num;
	
	}
	
	/** 发放新手礼包 */
	public void awardNewComer(Player player){
		player.setAttribute(PublicConst.NEW_FOLLOW_PLAYER,null);
		String newPlayerAward=player.getAttributes(PublicConst.NEW_PLAYER_AWARD);
		if(newPlayerAward==null||"".equals(newPlayerAward)){
			/** 送1个2级潜艇,2级巡洋舰 */
			Award award=(Award)Award.factory.getSample(PublicConst.NEW_PLAYER_AWARD_SID);	
			award.awardLenth(new ByteBuffer(),player,null,null,null);
			JBackKit.sendResetTroops(player);
			JBackKit.sendFightScore(player,getObjectFactory(),true,FightScoreConst.NEW_PLAYER_AWARD);
			if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER_HOLD)!=null)
			{
				player.setAttribute(PublicConst.NEW_FOLLOW_PLAYER_HOLD,null);
				NpcIsland island=(NpcIsland)NpcIsland.factory
								.getSample(FightPort.NEW_PLAYER_ATT_ISLAND);
				int[] res=new int[Player.RESOURCES_SIZE];
				island.forceSetResource(res,FightPort.NEW_PLAYER_HOLD_RESOURCE);
				// 回归资源
				Resources.addResources(player.getResources(),res,player);
			}
			player.setAttribute(PublicConst.NEW_PLAYER_AWARD,"Y");
		}
		// 新手引导完成后推送部分任务
		player.getTaskManager().pushNextTask();
	}
	
	private void startLotto(int lottoType,Player player,ByteBuffer data,
		int awardSid,int[] lottoSid,String message,int length,int position)
	{
		Award award=(Award)Award.factory.newSample(awardSid);
		int equipReason=EquipmentTrack.FROM_LOW_LOTTO;
		if(lottoType==PublicConst.HIGH_LOTTO)
		{
			equipReason=EquipmentTrack.FROM_HIGH_LOTTO;
		}
		award.awardLenth(data,player,objectFactory,message,lottoSid,
			new int[]{equipReason,FightScoreConst.LOTTO_STATE});
//		data.readByte();// 跳过奖励品种类数量
//		data.readByte();// 跳过奖励品种类
//		int num=data.readInt();// 奖励品数量
//		int sid=data.readUnsignedShort();// 奖励品
//		data.clear();
//		data.writeByte(position);
//		data.writeByte(lottoType);
//		data.writeByte(length);
//		int[] props=award.getRandomProps();
//		//随机物品数组进行随机填充，增强前台显示效果
//		int seed=MathKit.randomValue(0,props.length/3)*3;
//		for(int i=0;i<length;i++){
//			if(i+1==position)
//			{
//				data.writeByte(num);
//				data.writeShort(sid);
//				continue;
//			}
//			seed=(seed+3)%props.length;
//			if(props[seed]==sid){
//				seed=(seed+3)%props.length;
//			}
//			data.writeByte(props[seed+1]);
//			data.writeShort(props[seed]);
//		}
	}
	
	public AutoUpBuildManager getAutoUpBuilding()
	{
		return autoUpBuilding;
	}
	
	public void setAutoUpBuilding(AutoUpBuildManager autoUpBuilding)
	{
		this.autoUpBuilding=autoUpBuilding;
	}
	
	public GameDataMemCache getGameDataMemCache()
	{
		return gameDataMemCache;
	}
	
	public void setGameDataMemCache(GameDataMemCache gameDataMemCache)
	{
		this.gameDataMemCache=gameDataMemCache;
	}
	
	public void setPasswordManager(PasswordManager passwordManager)
	{
		this.passwordManager=passwordManager;
	}
	public ConfigManager getConfigManager()
	{
		return configManager;
	}
	
	public void setConfigManager(ConfigManager configManager)
	{
		this.configManager=configManager;
	}
//	public void addPlat(int pid,String platname)
//	{
//		platMap.put(pid,platname);
//	}
//	public void addAddress(int pid,String platname,String address,String share)
//	{
//		platMap.put(pid,platname);
//		addressMap.put(pid,address);
//		shareMap.put(pid,share);
//	}
	
	
	public GrowthPlanManager getPlanManager()
	{
		return planManager;
	}
	
	public void setPlanManager(GrowthPlanManager planManager)
	{
		this.planManager=planManager;
	}
	/**向中心发送消息**/
	public ByteBuffer sendHttpData(ByteBuffer data)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// 设置port
		map.put("port","1");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,
				null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
	
	/**修改绑定的状态  返回字符串**/
	public  String  getString(String  str,int index)
	{
		if(str==null||str.length()==0) return "";
		String[] strs=str.split(",");
		if(index<strs.length) strs[index]="0";
		String endstr=null;
		for(int k=0;k<strs.length;k++)
		{
			if(endstr==null)
				endstr=strs[k];
			else
				endstr+=","+strs[k];
		}
		return endstr;
	}
	
	/** 设置定时器 */
	public void init()
	{
		checkTime=TimeKit.getSecondTime();
		TimerCenter.getMinuteTimer().add(
			new TimerEvent(this,"playerAtt",5*1000));
	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		if(!SeaBackKit.isSameDay(checkTime,TimeKit.getSecondTime()))
		{
			JBackKit.sendOnLinePlayerFrindInfo(objectFactory);
		}
		checkTime=TimeKit.getSecondTime();
	}

}
