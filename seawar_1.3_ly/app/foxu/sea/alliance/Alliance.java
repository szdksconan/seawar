package foxu.sea.alliance;

import java.util.ArrayList;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.set.AttributeList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.FightScene;
import foxu.sea.AttrAdjustment;
import foxu.sea.InterTransltor;
import foxu.sea.IslandLocationSave;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Service;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.DonateRank;
import foxu.sea.alliance.alliancebattle.MaterialValue;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.AllianceSkill;
import foxu.sea.fight.Skill;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.port.AlliancePort;

/**
 * 联盟 author:icetiger
 */
public class Alliance
{

	/** 会长自动移交时间为7天 */
	public static final int AUTO_TIME=60*60*24*7;
	/** 联盟事件存储暂定200条 */
	public static final int EVENT_SIZE=200;
	/** 申请人数 */
	public static final int APPLICATION_MAX_NUM=30;

	public static final int MILITARY_RANK1=0,// 普通成员
					MILITARY_RANK2=1,// 联盟副会长
					MILITARY_RANK3=2;// 联盟会长
	
	/** 联盟物资等资源的长度 **/
	public static final int MATERIAL_LENGHT=3;
	/** 设置联盟敌对的长度 **/
	public static final int HOSTILE_LENGTH=5;
	/** 需要满级检测的技能sid */
	public static int[] skills2check;
	/** 联盟id 递增 */
	int id;
	/** 联盟当前经验 */
	int allianceExp;
	/** 会长玩家id */
	int masterPlayerId;
	/** 联盟总战力 */
	int allFightScore;
	/** 联盟等级 默认1级 */
	int allianceLevel=1;
	/** 公会创建时间 */
	int create_at;
	/** 联盟名字 */
	String name="";
	/** 联盟介绍 */
	String description="";
	/** 联盟公告 */
	String announcement="";
	/** 副会长列表 玩家id */
	IntList vicePlayers=new IntList();
	/** 成员列表 玩家id */
	IntList playerList=new IntList();
	/** 申请玩家列表 */
	IntList applicationList=new IntList();
	/** 事件列表 */
	ArrayList<AllianceEvent> eventList=new ArrayList<AllianceEvent>();
	/** 联盟技能 */
	ObjectArray allianSkills=new ObjectArray();
	/** 属性表 */
	AttributeList attributes=new AttributeList();

	/** 联盟boss战 */
	AllianceBoss boss=new AllianceBoss();

	/** 当前联盟排名 */
	int rankNum;
	/** 自己联盟排名的更新时间 */
	int rankTime;
	/** 联盟敌对 **/
	String hostile;
	/** 联盟物资 **/
	long material;
	/** 科技点 **/
	long sciencepoint;
	/** 竞标sid **/
	int betBattleIsland;
	/** 联盟物资捐献记录 **/
	IntKeyHashMap  materialValue=new IntKeyHashMap();
	/**联盟资源捐献排行榜**/
	IntKeyHashMap  giveValue=new IntKeyHashMap();
	/** 收藏的坐标 */
	ArrayList<IslandLocationSave> locationSaveList=new ArrayList<IslandLocationSave>();

	/** 联盟宝箱幸运积分 */
	int luckyPoints;

	/** 是否开启自动加入 0否 1是 */
	int autoJoin=0;
	/** 入盟条件 等级 */
	int joinPlayerLevel=0;
	/** 入盟条件 战力 */
	int joinFightScore=0;
	/**幸运抽奖的时间**/
	int luckyCreateAt;
	/**联盟旗帜**/
	AllianceFlag flag=new AllianceFlag();
	public ArrayList<IslandLocationSave> getLocationSaveList()
	{
		return locationSaveList;
	}

	public void bytesWriteBoss(ByteBuffer data)
	{
		boss.bytesWrite(data);
	}

	public void bytesReadBoss(ByteBuffer data)
	{
		boss.bytesRead(data);
	}

	public void showBytesWriteBoss(ByteBuffer data,int playerId)
	{
		boss.showBytesWrite(data,playerId);
	}

	/** 查看公会是否有这个人 */
	public boolean isHavePlayer(int playerId)
	{
		for(int i=0;i<playerList.size();i++)
		{
			if(playerId==playerList.get(i)) return true;
		}
		return false;
	}

	/** 获得事件的总页数 */
	public int getEventPageSize()
	{
		if(eventList.size()%AlliancePort.MAX_EVENT_SIZE==0)
		{
			return eventList.size()/AlliancePort.MAX_EVENT_SIZE;
		}
		else
		{
			return eventList.size()/AlliancePort.MAX_EVENT_SIZE+1;
		}
	}

	/** 获取某个联盟技能的经验和等级 */
	public int[] getSkillExp(int skillSid)
	{
		Object object[]=allianSkills.getArray();
		int exp[]=new int[2];
		for(int i=0;i<object.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)object[i];
			if(skill==null) continue;
			if(skill.getSid()==skillSid)
			{
				exp[0]=skill.getNowExp();
				exp[1]=skill.getLevel();
				break;
			}
		}
		return exp;
	}
	/** 增加技能经验值 */
	public int incrSkillExp(int n,int skillSid)
	{
		Object object[]=allianSkills.getArray();
		int addLevel=0;
		for(int i=0;i<object.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)object[i];
			if(skill==null) continue;
			if(skill.getSid()==skillSid)
			{
				addLevel=skill.incrExp(n,allianceLevel);
				return addLevel;
			}
		}
		AllianceSkill skill=(AllianceSkill)FightScene.abilityFactory
			.newSample(skillSid);
		if(skill!=null)
		{
			addLevel=skill.incrExp(n,allianceLevel);
			allianSkills.add(skill);
		}
		return addLevel;
	}

	/** 增加经验值 */
	public int incrExp(int n)
	{
		if(n<=0||allianceExp+n<0) return 0;// 越界判定
		if(getAllianceLevel()>=PublicConst.MAX_ALLIANCE_LEVEL) return 0;
		allianceExp+=n;
		int addLevel=flashLevel();
		return addLevel;
	}
	/** 随机获取一个等级未满的技能 */
	public int getRandomSkill()
	{
		IntList sids=new IntList();
		for(int i=0;i<PublicConst.ALLIANCE_LEVEL_OPEN_SKILL.length;i++)
		{
			String[] strs=PublicConst.ALLIANCE_LEVEL_OPEN_SKILL[i].split(":");
			if(allianceLevel>=Integer.parseInt(strs[0])
				&&!SeaBackKit.isContainValue(
					PublicConst.ALLIANCE_COMBO_SKILL,
					Integer.parseInt(strs[1])))
			{
				sids.add(Integer.parseInt(strs[1]));
			}
		}
		int index=MathKit.randomValue(0,sids.size());
		AllianceSkill skill=null;
		for(int i=index;i<sids.size();i++)
		{
			skill=(AllianceSkill)FightScene.abilityFactory.getSample(sids.get(i));
			int[] explevel=getSkillExp(sids.get(i));
			if(explevel[1]>=skill.getExperience().length)continue;
			if(explevel[1]==allianceLevel&&explevel[0]==skill.getExperience()[explevel[1]]-1)continue;
			return sids.get(i);
		}
		for(int i=0;i<index;i++)
		{
			skill=(AllianceSkill)FightScene.abilityFactory.getSample(sids.get(i));
			int[] explevel=getSkillExp(sids.get(i));
			if(explevel[1]>=skill.getExperience().length)continue;
			if(explevel[1]==allianceLevel&&explevel[0]==skill.getExperience()[explevel[1]]-1)continue;
			return sids.get(i);
		}
		return 0;
	}
	/** 刷新等级 */
	public int flashLevel()
	{
		if(getAllianceLevel()<1) setAllianceLevel(1);
		if(getAllianceLevel()>PublicConst.MAX_ALLIANCE_LEVEL)
			setAllianceLevel(PublicConst.MAX_ALLIANCE_LEVEL);
		int addLevel=0;
		int length=PublicConst.MAX_ALLIANCE_LEVEL<PublicConst.ALLIANCE_LEVEL_EXP.length
			?PublicConst.MAX_ALLIANCE_LEVEL:PublicConst.ALLIANCE_LEVEL_EXP.length;
		for(int i=getAllianceLevel()-1;i<length;i++)
		{
			if(allianceExp>=PublicConst.ALLIANCE_LEVEL_EXP[i])
			{
				addLevel++;
				allianceExp-=PublicConst.ALLIANCE_LEVEL_EXP[i];
				continue;
			}
			break;
		}
		if(allianceExp>PublicConst.ALLIANCE_LEVEL_EXP[length-1])
			allianceExp=PublicConst.ALLIANCE_LEVEL_EXP[length-1];
		setAllianceLevel(getAllianceLevel()+addLevel);
		return addLevel;
	}

	/** 计算自动移交会长 */
	public void autoChangeMaster(CreatObjectFactory objectFactory)
	{
		// if(masterPlayerId==0) return;
		// Player master=objectFactory.getPlayerById(masterPlayerId);
		// // 找到user的登录时间
		// User user=objectFactory.getUserDBAccess().load(
		// master.getUser_id()+"");
		// if(user!=null)
		// {
		// int loginTime=user.getLoginTime();
		// if((TimeKit.getSecondTime()-loginTime)>=AUTO_TIME)
		// {
		// // 更换会长
		// }
		// }
	}

	/** 找到贡献度最高的且7天之内登录过的玩家id 3天登录 */
	public int findPlayerContribution()
	{
		return 0;
	}

	/** 添加一个副会长 */
	public void addVicePlayer(int playerId)
	{
		if(vicePlayers.size()>=100) return;
		for(int i=0;i<vicePlayers.size();i++)
		{
			if(vicePlayers.get(i)==playerId) return;
		}
		vicePlayers.add(playerId);
	}

	/** 移除一个副会长 */
	public void removeVicePlayer(int playerId)
	{
		vicePlayers.remove(playerId);
	}

	/** 为某个玩家添加联盟技能 */
	public void addAllianceSkills(Player player)
	{
		// 添加联盟技能
		player.resetAdjustment();
		// 清空主动技能
		player.getAllianceList().clear();
		Object[] array=getAllianSkills().getArray();
		for(int i=0;i<array.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.isDefenceSkill()) continue;
			skill.setChangeValue(player.getAdjstment());
		}
		// 设置主动技能
		for(int i=0;i<array.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.isAllianceSkill())
			{
				player.getAllianceList().add(skill.getSid());
				player.getAllianceList().add(skill.getLevel());
			}
		}
	}

	/** 某玩家是否在申请列表内 */
	public boolean isApplication(int playerId)
	{
		for(int i=0;i<applicationList.size();i++)
		{
			if(playerId==applicationList.get(i)) return true;
		}
		return false;
	}

	/** 是否是管理 */
	public boolean isMaster(int playerId)
	{
		if(playerId==masterPlayerId) return true;
		for(int i=0;i<vicePlayers.size();i++)
		{
			if(playerId==vicePlayers.get(i))
			{
				return true;
			}
		}
		return false;
	}

	/** 联盟技能升级 重新挂载玩家技能 */
	public void flushAllianceSkill(CreatObjectFactory objectFactory)
	{
		Player player=null;
		// 清除联盟所有人的技能加成
		for(int i=0;i<playerList.size();i++)
		{
			player=objectFactory.getPlayerById(playerList.get(i));
			if(player!=null)
			{
				addAllianceSkills(player);
			}
		}
	}

	/** 联盟解散 */
	public void dismiss(CreatObjectFactory objectFactory)
	{
		Player player=null;
		// 清除联盟所有人的技能加成
		for(int i=0;i<playerList.size();i++)
		{
			player=objectFactory.getPlayerById(playerList.get(i));
			if(player!=null)
			{
				clearAllianceDefence(player,objectFactory);
				player.setAttribute(PublicConst.ALLIANCE_ID,null);
				player.getAllianceList().clear();
				player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,
					null);
				player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
				player.resetAdjustment();
				// 发送邮件
				String title=InterTransltor.getInstance().getTransByKey(
					player.getLocale(),"dismiss_alliance_title");
				String content=InterTransltor.getInstance().getTransByKey(
					player.getLocale(),"dismiss_alliance_content");
				content=TextKit.replace(content,"%",
					SeaBackKit.formatDataTime(TimeKit.getSecondTime()));
				MessageKit.sendSystemMessages(player,objectFactory,content,
					title);
			}
		}
		//如果在一阶段的情况下  验证是否有占领岛屿
		if(getBetBattleIsland()!=0)
		{
			BattleIsland island=(BattleIsland)objectFactory
				.getBattleIslandMemCache().load(betBattleIsland,true);
			if(island!=null) island.setChangeRankValue(id);
		}
		allFightScore=0;
		masterPlayerId=0;
		vicePlayers.clear();
		playerList.clear();
		applicationList.clear();
		eventList.clear();
		allianSkills.clear();
		materialValue.clear();
		giveValue.clear();
		ActivityContainer.getInstance().clearActivityInfo(getId());
		objectFactory.getAllianceMemCache().deleteCache(this);
		objectFactory.getAllianceMemCache().getDbaccess().delete(this);
	}

	/** 新增一个申请者 */
	public void addApllication(int playerId)
	{
		applicationList.add(playerId);
	}

	/** 移除一个申请者 */
	public void removeApllication(int playerId)
	{
		applicationList.remove(playerId);
	}

	/** 移除一个成员 */
	public void removePlayerId(int playerId)
	{
		playerList.remove(playerId);
		vicePlayers.remove(playerId);
	}

	/** 给联盟所有成员添加一个buff */
	public void addPlayerServices(CreatObjectFactory objectFactory,
		int serviceSid,int serviceTime)
	{
		Player player=null;
		for(int i=0;i<playerList.size();i++)
		{
			player=objectFactory.getPlayerById(playerList.get(i));
			if(player==null) continue;
			Service servicePlayer=(Service)Service.factory.newSample(serviceSid);
			servicePlayer.setServiceTime(serviceTime);
			player.addService(servicePlayer,TimeKit.getSecondTime());
			JBackKit.sendResetService(player);
		}
	}

	/** 总战力push */
	public void pushAllFightScore(CreatObjectFactory objectFactory)
	{
		Player player=null;
		allFightScore=0;
		for(int i=0;i<playerList.size();i++)
		{
			player=objectFactory.getPlayerById(playerList.get(i));
			if(player==null) continue;
			allFightScore+=player.getFightScore();
		}
	}

	/** 成员总数 */
	public int playersNum()
	{
		return playerList.size();
	}

	/** 添加一个人员 */
	public void addPlayer(int playerId)
	{
		if(playerList.contain(playerId))return;
		playerList.add(playerId);
	}

	/** 是否在联盟成员里面 */
	public boolean inAlliance(int playerId)
	{
		return playerList.contain(playerId);
	}

	/** 获取某个属性 */
	public String getAttributes(String key)
	{
		return attributes.get(key);
	}

	/** 获取全部属性 */
	public String[] getAttributes()
	{
		return attributes!=null?attributes.getArray():AttributeList.NULL;
	}

	/** 添加一个事件 */
	public void addEvent(AllianceEvent event)
	{
		if(eventList.size()>=EVENT_SIZE)
		{
			for(int i=eventList.size()-1;i>=0;i--)
			{
				if(eventList.get(i)==null) continue;
				eventList.remove(i);
				break;
			}
		}
		eventList.add(0,event);
	}

	/** 设置指定属性的值 */
	public void setAttribute(String key,String value)
	{
		attributes.set(key,value);
	}

	/** 获得会长的名称 */
	public String getMasterName(CreatObjectFactory factoy)
	{
		Player master=factoy.getPlayerById(masterPlayerId);
		if(master==null) return "";
		return master.getName();
	}
	/** 获取技能修正值 */
	public AttrAdjustment getAdjustment()
	{
		Object[] array=getAllianSkills().getArray();
		AttrAdjustment adjustment=new AttrAdjustment();
		for(int i=0;i<array.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.isDefenceSkill()) continue;
			skill.setChangeValue(adjustment);
		}
		return adjustment;
	}
	
	/** 获取主动技能 */
	public IntList getSkillList()
	{
		Object[] array=getAllianSkills().getArray();
		IntList list=new IntList();
		// 设置主动技能
		for(int i=0;i<array.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.isAllianceSkill())
			{
				list.add(skill.getSid());
				list.add(skill.getLevel());
			}
		}
		return list;
	}
	/** 获取当前带兵量 */
	public int getShipMax()
	{
		return PublicConst.AFIGHT_SHIP[allianceLevel-1];
	}
	/** 清除联盟协防(协防舰队返航) */
	public void clearAllianceDefence(Player player,CreatObjectFactory objectFactory)
	{
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		mustang.set.ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null||fightEventList.size()<=0) return;
		for(int i=0;i<fightEventList.size();i++)
		{
			if(fightEventList.get(i)==null) continue;
			FightEvent event=(FightEvent)fightEventList.get(i);
			if(event.getType()==FightEvent.ATTACK_HOLD
				&&event.getEventState()!=FightEvent.RETRUN_BACK)
			{
				NpcIsland beIsland=objectFactory.getIslandCache().loadOnly(
					event.getAttackIslandIndex()+"");
				if(beIsland.getPlayerId()!=0)// 协防
				{
					// todo
					int now=TimeKit.getSecondTime();
					int needTime=FightKit.needTime(
						event.getAttackIslandIndex(),
						event.getSourceIslandIndex());
					// 航行中的协防舰队返航，避免造成误伤
					if(event.getEventState()==FightEvent.ATTACK)
					{
						int leftTime=event.getCreatAt()+event.getNeedTime()
							-now;
						// 如果还没到目的地，则在当前地点返回，用时与出发方向花费时间相同
						if(leftTime>0) needTime=event.getNeedTime()-leftTime;
						event.setNeedTimeDB(needTime);
					}
					else
					{
						Player aPlayer=objectFactory.getPlayerById(event.getPlayerId());
						event.setNeedTime(needTime,aPlayer,now);
					}
					event.setCreatAt(now);
					event.setEventState(FightEvent.RETRUN_BACK);
					JBackKit.sendFightEvent(player,event,objectFactory);
					JBackKit.deleteFightEvent(player,event);
					JBackKit.sendFightEvent(
						objectFactory.getPlayerById(event.getPlayerId()),
						event,objectFactory);
				}
			}
		}
	}
	
	public boolean isLevelAndSkillsMax()
	{
		boolean isMax=true;
		if(getAllianceLevel()<PublicConst.MAX_ALLIANCE_LEVEL)
			return false;
		// 检测技能等级
		Object[] skills=getAllianSkills().getArray();
		if(skills==null||skills.length<=0)
			isMax=false;
		else
		{
			IntKeyHashMap typeMap=new IntKeyHashMap();
			for(int i=0;i<skills.length;i++)
			{
				Skill skill=(Skill)skills[i];
				if(typeMap.get(skill.getSid())!=null) continue;
				if(skill.getLevel()<PublicConst.MAX_SKILL_LEVEL)
				{
					isMax=false;
					break;
				}
				typeMap.put(skill.getSid(),"");
			}
			// 如果所持技能都达到满级，再判定是否包含了所有可升级的技能
			if(isMax)
			{
				for(int i=0;i<skills2check.length;i++)
				{
					if(typeMap.get(skills2check[i])==null)
					{
						isMax=false;
						break;
					}
				}
			}
		}
		return true;
	}
	
	/**添加一个联盟敌对**/
	public void  addHostile(int id)
	{
		if(hostile==null || hostile.length()==0)
		{
			hostile=id+"";
			return ;
		}
		hostile+=","+id;
	}
	/**移除一个敌对**/
	public void  removeHostile(int id)
	{
		if(hostile==null || hostile.length()==0)
			return;
		String info[]=hostile.split(",");
		if(info.length==0)
		{	
			hostile="";
			return;
		}
		String newHost="";
		for(int i=0;i<info.length;i++)
		{
			if(TextKit.parseInt(info[i])!=id)
			{
				if(newHost==null ||newHost.length()==0)
					newHost=info[i];
				else
					newHost+=","+info[i];
			}
		}
		hostile=newHost;
	}
	
	/** 前台序列化 */
	public void showBytesWrite(ByteBuffer data,Player player,
		CreatObjectFactory factoy)
	{
		//初始化玩家的捐献
		initGiveValue(factoy);
		data.writeInt(id);
		Player master=factoy.getPlayerById(masterPlayerId);
		data.writeUTF(master.getName());
		data.writeInt(allFightScore);
		data.writeByte(allianceLevel);
		data.writeInt(create_at);
		data.writeUTF(name);
		data.writeUTF(description);
		data.writeUTF(announcement);
		data.writeInt(allianceExp);
		showBytesWritePlayerList(data,factoy);
		// showBytesWriteEventList(data);
		showbytesWriteSkills(data);
		// 某个玩家玩家当天的捐献次数
		int[] give=new int[6];
		DonateRank rank=(DonateRank)giveValue.get(player.getId());
		if(rank!=null)
			give=rank.getDonaterecord();
		for(int i=0;i<give.length;i++)
		{
			data.writeByte(give[i]);
		}
		data.writeByte(applicationList.size());
		Player appPlayer=null;
		for(int i=0;i<applicationList.size();i++)
		{
			appPlayer=factoy.getPlayerById(applicationList.get(i));
			if(appPlayer!=null)
			{
				data.writeInt(appPlayer.getId());
				data.writeUTF(appPlayer.getName());
				data.writeByte(appPlayer.getLevel());
				data.writeInt(appPlayer.getFightScore());
			}
		}
		showBytesWriteBoss(data,player.getId());
		//序列化联盟敌对
		showBytesWriteHostile(data,factoy);
		// 序列化入盟设置
		data.writeByte(autoJoin);
		data.writeShort(joinPlayerLevel);
		data.writeInt(joinFightScore);
		boolean isJoinPlayerLevel=Boolean.parseBoolean(attributes
			.get(PublicConst.ALLIANCE_JOIN_LEVEL_BOOL));
		boolean isJoinPlayerScore=Boolean.parseBoolean(attributes
			.get(PublicConst.ALLIANCE_JOIN_SCORE_BOOL));
		data.writeBoolean(isJoinPlayerLevel);
		data.writeBoolean(isJoinPlayerScore);
		flag.showBytesWriteFlagsInfo(data);
		
		bytesWriteLocationSaveList(data);

	}

	/** 序列化成员 */
	public void showBytesWritePlayerList(ByteBuffer data,
		CreatObjectFactory factoy)
	{
		data.writeByte(playerList.size());
		Player player=null;
		int rank=MILITARY_RANK1;
		int nowTime=TimeKit.getSecondTime();
		for(int i=0;i<playerList.size();i++)
		{
			int playerId=playerList.get(i);
			player=factoy.getPlayerById(playerId);
			data.writeInt(playerId);
			data.writeUTF(player.getName());
			data.writeByte(player.getLevel());
			rank=getPosition(playerId);
			data.writeByte(rank);
			data.writeInt(player.getFightScore());
			data.writeShort((nowTime-player.getUpdateTime())
				/PublicConst.DAY_SEC);
			DonateRank drank=(DonateRank)giveValue.get(playerId);
			if(drank!=null)
			{
				drank.flushValue();
				data.writeShort(drank.getDayValue());
				data.writeShort(drank.getWeekValue());
				data.writeShort(drank.getMouthValue());
				data.writeInt((int)drank.getTotleValue());
				continue;
			}
			data.writeShort(0);
			data.writeShort(0);
			data.writeShort(0);
			data.writeInt(0);
		}
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(masterPlayerId);
		data.writeInt(allFightScore);
		data.writeByte(allianceLevel);
		data.writeInt(create_at);
		data.writeUTF(name);
		data.writeUTF(description);
		data.writeUTF(announcement);
		data.writeInt(allianceExp);
		bytesWriteVicePlayersList(data);
		bytesWritePlayerList(data);
		bytesWriteEventList(data);
		bytesWriteSkills(data);
		bytesWriteAttributes(data);
		bytesWriteApplicationList(data);
		bytesWriteBoss(data);
		bytesWriteLocationSaveList(data);
		data.writeInt(luckyPoints);
		data.writeInt(autoJoin);
		data.writeInt(joinPlayerLevel);
		data.writeInt(joinFightScore);
		data.writeInt(luckyCreateAt);

	}

	/** 将对象的域序列化到字节缓存中 */
	public Object bytesRead(ByteBuffer data)
	{
		id=data.readInt();
		masterPlayerId=data.readInt();
		allFightScore=data.readInt();
		allianceLevel=data.readUnsignedByte();
		create_at=data.readInt();
		name=data.readUTF();
		description=data.readUTF();
		announcement=data.readUTF();
		allianceExp=data.readInt();
		bytesReadVicePlayersList(data);
		bytesReadPlayerList(data);
		bytesReadEventList(data);
		bytesReadSkills(data);
		bytesReadAttributes(data);
		bytesReadApplicationList(data);
		bytesReadBoss(data);
		bytesReadLocationSaveList(data);
		luckyPoints=data.readInt();
		autoJoin=data.readInt();
		joinPlayerLevel=data.readInt();
		joinFightScore=data.readInt();
		luckyCreateAt=data.readInt();
		return this;
	}

	public void bytesWriteDes(ByteBuffer data)
	{
		data.writeUTF(description);
	}

	public void bytesWriteAnn(ByteBuffer data)
	{
		data.writeUTF(announcement);
	}

	public void bytesWriteApplicationList(ByteBuffer data)
	{
		if(applicationList==null||applicationList.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(applicationList.size());
			for(int i=0;i<applicationList.size();i++)
			{
				data.writeInt(applicationList.get(i));
			}
		}
	}

	public void bytesReadDes(ByteBuffer data)
	{
		description=data.readUTF();
	}

	public void bytesReadAnn(ByteBuffer data)
	{
		announcement=data.readUTF();
	}

	public void bytesReadApplicationList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		applicationList.clear();
		for(int i=0;i<length;i++)
		{
			applicationList.add(data.readInt());
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadAttributes(ByteBuffer data)
	{
		String[] array=new String[data.readUnsignedShort()*2];
		for(int i=0;i<array.length;i++)
		{
			array[i]=data.readUTF();
		}
		attributes=new AttributeList(array);
		return this;
	}

	/** 将手上东西的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteAttributes(ByteBuffer data)
	{
		if(attributes!=null)
		{
			String[] array=attributes.getArray();
			data.writeShort(array.length/2);
			for(int i=0;i<array.length;i++)
				data.writeUTF(array[i]);
		}
		else
		{
			data.writeShort(0);
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadSkills(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			temp[i]=Skill.bytesReadAbility(data,FightScene.abilityFactory);
		}
		allianSkills=new ObjectArray(temp);
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteSkills(ByteBuffer data)
	{
		if(allianSkills!=null&&allianSkills.size()>0)
		{
			int len=0;
			int top=data.top();
			Object[] array=allianSkills.getArray();
			data.writeByte(len);
			for(int i=0;i<array.length;i++)
			{
				((AllianceSkill)array[i]).bytesWrite(data);
				len++;
			}
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(len);
			data.setTop(newTop);
		}
		else
		{
			data.writeByte(0);
		}
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void showbytesWriteSkills(ByteBuffer data)
	{
		if(allianSkills!=null&&allianSkills.size()>0)
		{
			int len=0;
			int top=data.top();
			Object[] array=allianSkills.getArray();
			data.writeByte(len);
			for(int i=0;i<array.length;i++)
			{
				if(((AllianceSkill)array[i]).isEffectEnemySkill()) continue;
				((AllianceSkill)array[i]).bytesWrite(data);
				len++;
			}
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(len);
			data.setTop(newTop);
		}
		else
		{
			data.writeByte(0);
		}
	}
	
	public void bytesWriteVicePlayersList(ByteBuffer data)
	{
		if(vicePlayers==null||vicePlayers.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(vicePlayers.size());
			for(int i=0;i<vicePlayers.size();i++)
			{
				data.writeInt(vicePlayers.get(i));
			}
		}
	}

	public void bytesReadVicePlayersList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		vicePlayers.clear();
		for(int i=0;i<length;i++)
		{
			vicePlayers.add(data.readInt());
		}
	}
	
	public void bytesWriteLocationSaveList(ByteBuffer data)
	{
		if(locationSaveList==null||locationSaveList.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(locationSaveList.size());
			IslandLocationSave location=null;
			for(int i=0;i<locationSaveList.size();i++)
			{
				location=locationSaveList.get(i);
				location.bytesWrite(data);
			}
		}
	}
	
	public void bytesReadLocationSaveList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		locationSaveList.clear();
		IslandLocationSave location=null;
		for(int i=0;i<length;i++)
		{
			location=new IslandLocationSave();
			location.bytesRead(data);
			locationSaveList.add(location);
		}
	}

	/** 默认最多发20条 */
	public void showBytesWriteEventList(ByteBuffer data)
	{
		if(eventList==null||eventList.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			int length=PublicConst.DEFAOULT_ALLIANCE_SIZE;
			if(eventList.size()>=PublicConst.DEFAOULT_ALLIANCE_SIZE)
			{
				data.writeByte(PublicConst.DEFAOULT_ALLIANCE_SIZE);
			}
			else
			{
				length=eventList.size();
				data.writeByte(eventList.size());
			}
			for(int i=0;i<length;i++)
			{
				AllianceEvent event=eventList.get(i);
				event.showBytesWrite(data);
			}
		}

	}

	public void bytesWriteEventList(ByteBuffer data)
	{
		if(eventList==null||eventList.size()<=0)
		{
			data.writeShort(eventList.size());
		}
		else
		{
			data.writeShort(eventList.size());
			for(int i=0;i<eventList.size();i++)
			{
				AllianceEvent event=eventList.get(i);
				event.bytesWrite(data);
			}
		}
	}

	public void bytesReadEventList(ByteBuffer data)
	{
		int length=data.readUnsignedShort();
		eventList.clear();
		for(int i=0;i<length;i++)
		{
			AllianceEvent event=new AllianceEvent();
			event.bytesRead(data);
			eventList.add(event);
		}
	}

	public void bytesWritePlayerList(ByteBuffer data)
	{
		if(playerList==null||playerList.size()<=0)
		{
			data.writeShort(0);
		}
		else
		{
			data.writeShort(playerList.size());
			for(int i=0;i<playerList.size();i++)
			{
				data.writeInt(playerList.get(i));
			}
		}
	}

	public void bytesReadPlayerList(ByteBuffer data)
	{
		int length=data.readUnsignedShort();
		playerList.clear();
		for(int i=0;i<length;i++)
		{
			playerList.add(data.readInt());
		}
	}

	/**序列化敌对联盟**/
	public void showBytesWriteHostile(ByteBuffer data,CreatObjectFactory factory)
	{
		if(hostile==null || hostile.length()==0)
		{
			data.writeByte(0);
			return;
		}
		String[]  hostiles=hostile.split(",");
		int top=data.top();
		data.writeByte(hostiles.length);
		//当前敌对联盟的长度
		int end=hostiles.length;
		for(int i=0;i<hostiles.length;i++)
		{
			int aid=TextKit.parseInt(hostiles[i]);
			Alliance alliance=factory.getAlliance(aid,false);
			if(alliance==null)
					end-=1;
			else
			{
				data.writeInt(aid);
				data.writeUTF(alliance.getName());
				data.writeByte(alliance.playersNum());
				data.writeInt(alliance.getAllFightScore());
				alliance.getFlag().showBytesWriteAllianceFlag(data);
			}
		}
		if(end!=hostiles.length)
		{
			int now_top=data.top();
			data.setTop(top);
			data.writeByte(end);
			data.setTop(now_top);
		}

	}

	/** 增加幸运点数 */
	public synchronized void addLuckyPoints()
	{
		this.luckyPoints++;
	}
	public int getId()
	{
		return id;
	}

	/**增加物资**/
	public void addMaterial(int num)
	{
		if(num<0) return ;
		material+=num;
		if(material<0) material=Long.MAX_VALUE;
	}
	
	/**扣除联盟物资**/
	public void reduceMaterial(int num)
	{
		if(num<0) return ;
		material-=num;
		if(material<0) material=0;
	}
	
	/**增加联盟科技点**/
	public void addSciencepoint(int num)
	{
		if(num<0) return ;
		sciencepoint+=num;
		if(sciencepoint<0) sciencepoint=Long.MAX_VALUE;
	}
	
	/**扣除联盟科技点**/
	public void reduceSciencepoint(int num)
	{
		if(num<0) return ;
		sciencepoint-=num;
		if(sciencepoint<0) sciencepoint=0;
	}
	
	/** 判断下当前的物资是否足够 **/
	public boolean enoughMaterial(int num)
	{
			if(num<0) return false;
			if(material<num) return false;
			return true;
	}
	
	/** 判断下当前科技点是否足够 **/
	public boolean enoughSciencepoint(int num)
	{
			if(num<0) return false;
			if(sciencepoint<num) return false;
			return true;
	}
	
	/** 清除联盟战信息 **/
	public void clear()
	{
		setBetBattleIsland(0);
	}

	public void setId(int allianceId)
	{
		this.id=allianceId;
	}

	public int getAllianceLevel()
	{
		return allianceLevel;
	}

	public void setAllianceLevel(int allianceLevel)
	{
		this.allianceLevel=allianceLevel;
	}

	public String getAnnouncement()
	{
		return announcement;
	}

	public void setAnnouncement(String announcement)
	{
		announcement=ChatMessage.filerText(announcement);
		this.announcement=announcement;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		description=ChatMessage.filerText(description);
		this.description=description;
	}

	public ArrayList<AllianceEvent> getEventList()
	{
		return eventList;
	}

	public void setEventList(ArrayList<AllianceEvent> eventList)
	{
		this.eventList=eventList;
	}

	public int getMasterPlayerId()
	{
		return masterPlayerId;
	}

	public void setMasterPlayerId(int masterPlayerId)
	{
		this.masterPlayerId=masterPlayerId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}

	public IntList getPlayerList()
	{
		return playerList;
	}

	public void setPlayerList(IntList playerList)
	{
		this.playerList=playerList;
	}

	public int getCreate_at()
	{
		return create_at;
	}

	public void setCreate_at(int create_at)
	{
		this.create_at=create_at;
	}

	public int getAllFightScore()
	{
		return allFightScore;
	}

	public void setAllFightScore(int allFightScore)
	{
		this.allFightScore=allFightScore;
	}

	public IntList getVicePlayers()
	{
		return vicePlayers;
	}

	public void setVicePlayers(IntList vicePlayers)
	{
		this.vicePlayers=vicePlayers;
	}

	public void setAttributes(AttributeList attributes)
	{
		this.attributes=attributes;
	}

	public int getAllianceExp()
	{
		return allianceExp;
	}

	public void setAllianceExp(int allianceExp)
	{
		this.allianceExp=allianceExp;
	}

	public int getRankTime()
	{
		return rankTime;
	}

	public void setRankTime(int rankTime)
	{
		this.rankTime=rankTime;
	}

	public int getRankNum()
	{
		return rankNum;
	}

	public void setRankNum(int rankNum)
	{
		this.rankNum=rankNum;
	}

	public IntList getApplicationList()
	{
		return applicationList;
	}

	public void setApplicationList(IntList applicationList)
	{
		this.applicationList=applicationList;
	}

	public ObjectArray getAllianSkills()
	{
		return allianSkills;
	}

	public void setAllianSkills(ObjectArray allianSkills)
	{
		this.allianSkills=allianSkills;
	}

	public AllianceBoss getBoss()
	{
		return boss;
	}

	public void setBoss(AllianceBoss boss)
	{
		this.boss=boss;
	}

	public String getHostile()
	{
		return hostile;
	}

	public void setHostile(String hostile)
	{
		this.hostile=hostile;
	}

	public long getMaterial()
	{
		return material;
	}

	public void setMaterial(long material)
	{
		this.material=material;
	}

	public long getSciencepoint()
	{
		return sciencepoint;
	}

	public void setSciencepoint(long sciencepoint)
	{
		this.sciencepoint=sciencepoint;
	}

	public int getBetBattleIsland()
	{
		return betBattleIsland;
	}

	
	public void setBetBattleIsland(int betBattleIsland)
	{
		this.betBattleIsland=betBattleIsland;
	}

	/**序列化**/
	public void showBytesWriteWarResrouce(ByteBuffer data,int integral,Player p,Alliance alliance)
	{
		data.writeLong(material);
		data.writeLong(sciencepoint);
		data.writeInt(integral);
		MaterialValue mvlaue=(MaterialValue)alliance.getMaterialValue().get(p.getId());
		if(mvlaue==null) 
			data.writeByte(0);
		else
			data.writeByte(mvlaue.getTimes());
	}

	/**序列化物资数据**/
	public void showByteWriteMaterialRank(int type,CreatObjectFactory factory,ByteBuffer data)
	{
		if(materialValue==null||materialValue.size()==0)
		{
			data.writeShort(0);
			return;
		}
		Object[] objects=materialValue.valueArray();
		data.writeShort(materialValue.size());
		for(int i=0;i<objects.length;i++)
		{
			MaterialValue mValue=(MaterialValue)objects[i];
			int rank=getPosition(mValue.getPlayerId());
			mValue.showByteWrite(data,factory,type,rank);
		}
	}
	
	public IntKeyHashMap getMaterialValue()
	{
		return materialValue;
	}

	
	public void setMaterialValue(IntKeyHashMap materialValue)
	{
		this.materialValue=materialValue;
	}
	
	/** 物资序列化 */
	public void bytesWriteMaterialValue(ByteBuffer data)
	{
		if(materialValue==null||materialValue.size()==0)
		{
			data.writeInt(0);
			return;
		}
		Object[] objects=materialValue.valueArray();
		data.writeInt(materialValue.size());
		for(int i=0;i<objects.length;i++)
		{
			MaterialValue mValue=(MaterialValue)objects[i];
			mValue.bytesWrite(data);
		}
	}

	/** 捐献序列化 */
	public void bytesWriteGiveValue(ByteBuffer data)
	{
		if(giveValue==null||giveValue.size()==0)
		{
			data.writeInt(0);
			return;
		}
		Object[] objects=giveValue.valueArray();
		data.writeInt(giveValue.size());
		for(int i=0;i<objects.length;i++)
		{
			DonateRank rank=(DonateRank)objects[i];
			rank.bytesWrite(data);
		}
	}
	
	/**读物资**/
	public void bytesReadMaterialValue(ByteBuffer data)
	{
		int le=data.readInt();
		for(int i=0;i<le;i++)
		{
			MaterialValue mValue=new MaterialValue();
			mValue.bytesRead(data);
			materialValue.put(mValue.getPlayerId(),mValue);
		}
	}
	/**读捐献**/
	public void bytesReadGiveValue(ByteBuffer data)
	{
		int le=data.readInt();
		for(int i=0;i<le;i++)
		{
			DonateRank rank=new DonateRank();
			rank.bytesRead(data);
			giveValue.put(rank.getPlayerId(),rank);
		}
	}
	
	/** 增加玩家联盟物资的记录 **/
	public boolean addPlayerMaterialValue(int playerId,int num)
	{
		if(num<=0) return false;
		MaterialValue mValue=null;
		if(materialValue.get(playerId)!=null)
		{
			mValue=(MaterialValue)materialValue.get(playerId);
			return mValue.addMaterialValue(num);
		}
		mValue=new MaterialValue();
		mValue.setPlayerId(playerId);
		mValue.addMaterialValue(num);
		materialValue.put(playerId,mValue);
		return false;
	}

	/** 获取玩家物资捐献次数 **/
	public int getPlayerGiveTimes(int playerId)
	{
		if(materialValue.get(playerId)!=null)
		{
			MaterialValue mValue=(MaterialValue)materialValue.get(playerId);
			return mValue.getTimes();
		}
		return 0;
	}

	/** 获取玩家物资捐献数量 **/
	public long getPlayerGiveMaterial(int playerId)
	{
		if(materialValue.get(playerId)!=null)
		{
			MaterialValue mValue=(MaterialValue)materialValue.get(playerId);
			return mValue.getTotleValue();
		}
		return 0;
	}

	
	public IntKeyHashMap getGiveValue()
	{
		return giveValue;
	}

	
	public void setGiveValue(IntKeyHashMap giveValue)
	{
		this.giveValue=giveValue;
	}
	
	/**联盟设置捐献记录**/
	public  void setPlayerGiveValue(Player player,CreatObjectFactory factory)
	{
		String str=player.getAttributes(PublicConst.ALLIANCE_GIVE_VALUES);
		if(str==null || str.length()==0) return;
		String[] values=str.split(",");
		int votetime=TextKit.parseInt(values[1]);
		int timeNow=TimeKit.getSecondTime();
		// 物资
		MaterialValue value=new MaterialValue();
		value.setPlayerId(player.getId());
		if(votetime!=0&&SeaBackKit.isSameDay(votetime,timeNow))
		{
			value.setTimes(TextKit.parseInt(values[0]));
			value.setValueTime(votetime);
		}
		materialValue.put(player.getId(),value);
		int rank1=0;
		// 职务
		if(player.getId()==masterPlayerId)
		{
			rank1=MILITARY_RANK3;
		}
		for(int j=0;j<vicePlayers.size();j++)
		{
			if(player.getId()==vicePlayers.get(j))
			{
				rank1=MILITARY_RANK2;
				break;
			}
		}
		JBackKit.sendPlayerMaterialRank(playerList,factory,player,0,rank1);
		votetime=TextKit.parseInt(values[8]);
		// 联盟捐献
		DonateRank rank=new DonateRank();
		rank.setPlayerId(player.getId());
		if(votetime!=0&&SeaBackKit.isSameDay(votetime,timeNow))
		{
			int[] giveValues=new int[6];
			for(int i=2;i<values.length-1;i++)
			{
				giveValues[i-2]=TextKit.parseInt(values[i]);
			}
			rank.setDonaterecord(giveValues);
			rank.setValueTime(votetime);
		}
		giveValue.put(player.getId(),rank);
	}
	
	public int getLuckyPoints()
	{
		return luckyPoints;
	}

	public void setLuckyPoints(int luckyPoints)
	{
		this.luckyPoints=luckyPoints;
	}

	public int getAutoJoin()
	{
		return autoJoin;
	}

	public void setAutoJoin(int autoJoin)
	{
		this.autoJoin=autoJoin;
	}

	public int getJoinPlayerLevel()
	{
		return joinPlayerLevel;
	}

	public void setJoinPlayerLevel(int joinPlayerLevel)
	{
		this.joinPlayerLevel=joinPlayerLevel;
	}

	public int getJoinFightScore()
	{
		return joinFightScore;
	}

	public void setJoinFightScore(int joinFightScore)
	{
		this.joinFightScore=joinFightScore;
	}

	
	public int getLuckyCreateAt()
	{
		return luckyCreateAt;
	}

	
	public void setLuckyCreateAt(int luckyCreateAt)
	{
		this.luckyCreateAt=luckyCreateAt;
	}

	/** 添加捐献排行榜 **/
	public  void initGiveValue(CreatObjectFactory factory)
	{
		if(giveValue.size()!=0)
			return;
		for(int i=0;i<playerList.size();i++)
		{
			Player player=factory.getPlayerById(playerList.get(i));
			if(player==null) continue;
			DonateRank rank=new DonateRank();
			String str=player.getAttributes(PublicConst.ALLIANCE_GIVE_TIMES);
			//设置进入联盟的捐献值
			if(str!=null)
			{
				rank.setValueTime(0);
				rank.setDonaterecord(player.todayAlliance());
			}
			String  gives=player.getAttributes(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER);
			if(gives!=null)
			rank.setTotleValue(TextKit.parseInt(gives));
			rank.setPlayerId(playerList.get(i));
			rank.flushValue();
			giveValue.put(playerList.get(i),rank);
			MaterialValue value=new MaterialValue();
			value.setPlayerId(playerList.get(i));
			materialValue.put(playerList.get(i),value);
		}
	}
	/**移除捐献排行**/
	public void removeGiveRank(int playerId)
	{
		giveValue.remove(playerId);
	}
	
	/**移除物资排行**/
	public void removeMaterialRank(int playerId)
	{
		materialValue.remove(playerId);
	}
	
	/**获取职位**/
	public int getPosition(int playerId)
	{
		int rank1=0;
		// 职务
		if(playerId==masterPlayerId)
		{
			rank1=MILITARY_RANK3;
		}
		for(int j=0;j<vicePlayers.size();j++)
		{
			if(playerId==vicePlayers.get(j))
			{
				rank1=MILITARY_RANK2;
				return rank1;
			}
		}
		return rank1;
	}

	
	public AllianceFlag getFlag()
	{
		return flag;
	}

	
	public void setFlag(AllianceFlag flag)
	{
		this.flag=flag;
	}
}
