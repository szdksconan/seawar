package foxu.sea;


import mustang.back.SessionMap;
import mustang.event.ChangeListenerList;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.AttributeList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.text.TextKit;
import mustang.util.Sample;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.MessageGameDBAccess;
import foxu.dcaccess.mem.ActivityLogMemCache;
import foxu.fight.FightScene;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.achieve.AchieveData;
import foxu.sea.achieve.Achievement;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.ConsumeGemsActivity;
import foxu.sea.activity.ConsumeGemsRecord;
import foxu.sea.activity.DatePriceOffActivity;
import foxu.sea.activity.DiscountActivity;
import foxu.sea.activity.DoubleGemsAcitivity;
import foxu.sea.activity.LimitSaleActivity;
import foxu.sea.activity.TotalBuyActivity;
import foxu.sea.activity.VaribleAwardActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.alliance.chest.AllianceChest;
import foxu.sea.award.Award;
import foxu.sea.bind.TelBindingManager;
import foxu.sea.builds.Build;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.Island;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.produce.ScienceProduce;
import foxu.sea.checkpoint.ArmsRoutePoint;
import foxu.sea.checkpoint.Chapter;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.checkpoint.ElitePoint;
import foxu.sea.checkpoint.HCityCheckPoint;
import foxu.sea.checkpoint.SelfCheckPoint;
import foxu.sea.checkpoint.TearCheckPoint;
import foxu.sea.comrade.Comrade;
import foxu.sea.comrade.ComradeHandler;
import foxu.sea.comrade.ComradeTask;
import foxu.sea.equipment.EquipList;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.Skill;
import foxu.sea.growth.GrowthPlan;
import foxu.sea.head.HeadData;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.OfficerCenter;
import foxu.sea.port.AlliancePort;
import foxu.sea.port.PlayerAttPort;
import foxu.sea.proplist.Prop;
import foxu.sea.proplist.PropList;
import foxu.sea.recruit.RecruitRecord;
import foxu.sea.task.TaskEventExecute;
import foxu.sea.task.TaskManager;

/**
 * 玩家类 author:icetiger
 */
public class Player extends Role
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(MessageGameDBAccess.class);
	/** 平台 */
	public static final int PLAT_IOS=0,PLAT_ANDROID=1,PLAT_INTOUCH=2;
	
	public static final int MAX_INVITER_NUM=44;
	/** 荣誉积分初始为1000 */
	public static final int HONOR_SCORE=1000;
	/** 常量 */
	public static final int HONOR_INDEX=0,HONOR_LEVEL_INDEX=1;
	/** 常量 */
	public static final int ENERGY_INDEX=0,ENERGY_TIME_INDEX=1,
					ENERGY_BUY_TIME=2;
	/** 最大活力值 每小时回复 */
	public static final int MAX_ENERGY=20,ENERGY_TIME=60*30;
	/** 繁荣度 恢复间隔*/
	public static int PROSPERITY_TIME ;
	/** 繁荣度 等级划分 [需求繁荣度 产量加成 战力加成]*/
	public static int[] PROSPERITY_lV_BUFF;
	/** 繁荣度战斗BUFF 前端BUFF显示 SID 仅作显示*/
	public static int[] PROSPERITY_BUFF_SID;
	/** 恢复繁荣度最多需要的宝石数 */
	public static final int PROSPERITY_MAX_GEMS =200;
	/** 每日建议次数为5次 存入属性常量列表里面 */
	public static final int ADVICE_TIME_DAY=5;
	/** 玩家最大等级 */
	// public final static int MAX_LEVEL=70;
//	/** 玩家最大声望等级 */
//	public final static int MAX_HONOR_LEVEL=70;
	/** 精力活,跃度等 */
	public final static int ACTIVES_SIZE=3,RESOURCES_SIZE=7,HONOR_SIZE=2;
	/** 包裹 */
	public final static int BUNDLE_SIZE=200;
	/** 玩家升级经验数组 */
	public static long[] level_exps=null;
	/** 玩家声望升级经验数组 */
	public static int[] honor_exps=null;
	/** 默认资源 */
	public static String KEY_ID="id";
	/** 到达特定级数推送信息 */
	public static int[] level2push=null;
	/** 需要满级检测的建筑sid */
	public static int[] builds2check;
	/** 需要满级检测的科技sid */
	public static int[] sciences2check;
	/** 需要满级检测的技能sid */
	public static int[] skills2check;
	/** 状态改变事件监听器列表 */
	static ChangeListenerList changeListenerList=new ChangeListenerList();
	/** 经验 */
	long experience;
	/** 唯一ID */
	int id;
	/** vip */
	int user_state;
	/** 账号id */
	int user_id;
	/** 每日奖励 目前是声望领取 左16天数 右16表示领了多少次 0为没有领取 */
	int reward;
	/** 引导任务到第个新手任务 */
	int taskMark;
	/** 玩家做的新手引导步数 */
	int playerTaskMark;
	/** 包裹的大小 */
	int bundleSize=BUNDLE_SIZE;
	/** 本地语言 */
	int locale;
	/** 繁荣度相关信息   指数、checkTime、繁荣度max值、繁荣度等级*/
	int[] prosperityInfo;
	/** actives 活跃度 活跃度的回复时间 */
	int[] actives;
	/** *资源和宝石 */
	long resources[];
	/** 声望，声望等级默认1级 */
	int honor[];
	/** 邀请玩家ID 是否领取奖励 不等于0为领取 */
	int[] inviter_id={};
	/** 邀请我的玩家的id */
	int inveted;
	/** 创建的时候记录 udid */
	String udid;
	/** 玩家头像style */
	int style;
	/** 属性表 */
	AttributeList attributes;
	/** 任务类 */
	TaskManager taskManager=new TaskManager();
	/** 成就 */
	AchieveData achieveData=new AchieveData();
	/** 岛屿类 */
	Island island=new Island();
	/** 包裹 */
	PropList bundle=new PropList();
	/** 装备 */
	EquipList equips=new EquipList();
	/** 拥有的服务 */
	ObjectArray service=new ObjectArray();
	/** 关卡 */
	SelfCheckPoint selfCheckPoint=new SelfCheckPoint();
	/** 撕裂虚空关卡 */
	TearCheckPoint tearCheckPoint=new TearCheckPoint();
	/** 联合舰队关卡 */
	HCityCheckPoint heritagePoint=new HCityCheckPoint();
	/**军备航线**/
	ArmsRoutePoint armsroutePoint=new ArmsRoutePoint();
	/**精英战场**/
	ElitePoint elitePoint=new ElitePoint();
	/** 创建时间 */
	int createTime;
	/** 禁言时间 */
	int muteTime;
	/** 更新时间 */
	int updateTime;
	/** 下线时间  */
	int exitTime;
	/** 在线时间记录时刻*/
	int saveTime;
	/** 在线时间*/
	int onlineTime;
	/** 统帅等级 */
	int commanderLevel;
	/** 掠夺别人的资源量 改为星数排行 */
	long plunderResource;
	/** 荣誉积分 */
	int honorScore=HONOR_SCORE;
	/** 最高战斗力 */
	int fightScore;
	/** 成就积分 */
	int achieveScore;
	/** 玩家技能 */
	ObjectArray skills=new ObjectArray();
	/** 玩家deviceToken */
	String deviceToken;
	/** 玩家收藏的坐标 */
	ArrayList locationSaveList=new ArrayList();

	/** 属性修正值集合 */
	AttrAdjustment adjustment;
	/** 自己的掠夺资源排行 6小时更新一次 */
	int plunderRank;
	/** 自己的最高战斗力排行 6小时更新一次 */
	int fightScoreRank;
	/** 自己的荣誉排行 */
	int honorScoreRank;
	/** 成就积分排名 */
	int achieveScoreRank;
	/** 上一次更新排行的时间 */
	int lastRankTime;
	/** 上一次侦查时间 */
	int fightViewTime;
	/** 联盟技能 */
	IntList allianceList=new IntList();
	// /** 引导进度 */
	// int[] guideMark;
	/** 舰船等级技能 */
	int[] shipLevel;
	/** 关卡BUFF 等级 */
	int[] pointBuff;
	/** 创建ip */
	String createIp;
	/** 绑定ip */
	String bindIp;
	/** 登陆ip */
	String loginIp;
	/** 随机数产生sid为key random为value */
	/** 临时变量 */
	IntKeyHashMap getrandom=new IntKeyHashMap();
	/** 登陆日志时间 */
	int loginLogTime;
	/** 平台来源 (默认)*/
	int platid=PLAT_IOS;//PLAT_ANDROID
	/** (来自)地区*/
	String area;
	/** 登陆设备号  */
	String loginUid;
	/** 来源平台名 */
	String plat;
	/** 累计消费活动记录 */
	IntKeyHashMap consumeGems=new IntKeyHashMap();
	/** 产品事件id记录 */
	IntKeyHashMap productRecord=new IntKeyHashMap();
	/** 活跃度属性同步锁 */
	Object vitalityLock=new Object();
	/** 军需乐透属性同步锁 */
	Object lottoLock=new Object();
	/** 客户端包名 */
	String bundleId;
	/**删除状态**/
	int deleteTime;
	/** 设备越狱标识,1代表越狱设备 */
	int escapeDevice;
	/** 玩家收藏的阵型 */
	ArrayList formationList=new ArrayList();
	/** 跨服战 押注map crossid-bet（0为是否押注，1为是否参与,2为止步几强）*/
	IntKeyHashMap betmap=new IntKeyHashMap();
	/** 玩家军官中心 */
	OfficerCenter officers=new OfficerCenter(this);
	/** 新兵福利记录 */
	RecruitRecord recruit=new RecruitRecord();
	/** 战友系统 */
	Comrade comrade=new Comrade();
	/** 邮件附件领取记录 */
	IntList annex=new IntList();
	/** 联盟宝箱信息 */
	AllianceChest allianceChest=new AllianceChest();
	/** 联盟商品兑换次数  key:物品sid value:已兑换次数 */
	IntKeyHashMap propExchangeNum=new IntKeyHashMap();
	/** 成长计划信息 */
	GrowthPlan growthPlan=new GrowthPlan();
	/** 玩家头像信息 */
	private IntKeyHashMap headInfoMap = new IntKeyHashMap();
	/** 好友模块信息 */
	private FriendInfo friendInfo = new FriendInfo();
	// 当前头像sid
	private int currentHeadSid = 0;
	
	/* methods */
	/** 获取某个属性 */
	public String getAttributes(String key)
	{
		return attributes.get(key);
	}

	/** 重设改变值 */
	public void resetAdjustment()
	{
		if(adjustment==null)
		{
			adjustment=new AttrAdjustment();
		}
		else
		{
			adjustment.clear();
		}
		// 重设科技改变值
		PlayerBuild build=island.getBuildByIndex(BuildInfo.INDEX_2,
			island.getBuilds());
		if(build!=null)
		{
			ScienceProduce sp=(ScienceProduce)build.getProduce();
			sp.putAdjustment(adjustment);
		}
		Object[] objs=service.getArray();
		for(int i=objs.length-1;i>=0;i--)
		{
			Service s=(Service)objs[i];
			s.setChangeValue(adjustment);
		}
		// 技能
		Object[] array=skills.getArray();
		for(int i=0;i<array.length;i++)
		{
			Skill skill=(Skill)array[i];
			skill.setChangeValue(adjustment);
		}
		// 重设关卡加成
		setPointValue();
		//装备
		equips.resetChangeValue(adjustment);
	}
	/** 跨服属性值 */
	public void crossWriteAdjustment(ByteBuffer data)
	{
		AttrAdjustment adjustment=new AttrAdjustment();
		// 重设科技改变值
		PlayerBuild build=island.getBuildByIndex(BuildInfo.INDEX_2,
			island.getBuilds());
		if(build!=null)
		{
			ScienceProduce sp=(ScienceProduce)build.getProduce();
			sp.putAdjustment(adjustment);
		}
		// 技能
		Object[] array=skills.getArray();
		for(int i=0;i<array.length;i++)
		{
			Skill skill=(Skill)array[i];
			skill.setChangeValue(adjustment);
		}
		// 重设关卡加成
		setPointValue();
		//装备
		equips.resetChangeValue(adjustment);
		//序列化
		adjustment.crossBytesWrite(data);
	}
	
	/** 设置关卡加成 */
	public void setPointValue()
	{
		int level=getPointBuffLv(Chapter.ATTACK);
		if(level>0)
		{
			Chapter chapter=(Chapter)Chapter.factory
				.getSample(PublicConst.SIDS[Chapter.ATTACK]);
			chapter.setChangeValue(adjustment,PublicConst.ATTACK,level);
		}
		level=getPointBuffLv(Chapter.HP);
		if(level>0)
		{
			Chapter chapter=(Chapter)Chapter.factory
				.getSample(PublicConst.SIDS[Chapter.HP]);
			chapter.setChangeValue(adjustment,PublicConst.SHIP_HP,level);
		}

	}

	/** 获得属性修正值集合 */
	public AttrAdjustment getAdjstment()
	{
		if(adjustment==null) resetAdjustment();
		return adjustment;
	}
	/** 设置属性修正值集合 */
	public void setAdjstment(AttrAdjustment adjustment)
	{
		this.adjustment=adjustment;
	}
	/** 获取岛屿等级 */
	public int getIsLandLevel()
	{
		if(island.getIslandLevel()<=0) return 1;
		return island.getIslandLevel();
	}

	/** 玩家是否迎战 */
	public boolean isFight()
	{
		// PlayerBuild build=island.getBuildByIndex(BuildInfo.INDEX_0,island
		// .getBuilds());
		// CommandProduce produce=(CommandProduce)build.getProduce();
		// return produce.isFight();
		return true;
	}

	/** 减少精力值 */
	public void reDuceEnergy()
	{
		if(actives[ENERGY_INDEX]>=MAX_ENERGY)
		{
			actives[ENERGY_TIME_INDEX]=TimeKit.getSecondTime();
		}
		actives[ENERGY_INDEX]--;
		if(actives[ENERGY_INDEX]<0) actives[ENERGY_INDEX]=0;
	}
	/** 减少精力值 */
	public void reduceEnergyN(int n)
	{
		if(n<0) return;
		if(actives[ENERGY_INDEX]>=MAX_ENERGY)
		{
			actives[ENERGY_TIME_INDEX]=TimeKit.getSecondTime();
		}
		actives[ENERGY_INDEX]-=n;
		if(actives[ENERGY_INDEX]<0) actives[ENERGY_INDEX]=0;
	}

	/** 增加精力值 购买增加 可以超过上限 */
	public void addEnergy(int energy)
	{
		actives[ENERGY_INDEX]+=energy;
	}

	/** 自动补充主力舰队 */
	public void autoAddMainGroup()
	{
		island.autoAddMainGroup();
	}

	/** 获得今天购买了多少次能量 */
	public int getTodayBuyTimes()
	{
		int nowDay=SeaBackKit.getDayOfYear();
		int times[]=SeaBackKit.get2ShortInInt(actives[ENERGY_BUY_TIME]);
		// 是今天
		if(times[0]==nowDay) return times[1];
		return 0;
	}

	/** 获得当天重置任务的次数 */
	public int getResetTaskNum()
	{
		String times=getAttributes(PublicConst.REST_DAY_TASK);
		if(times==null||times.equals("")) return 0;
		int dayAndTime=Integer.parseInt(times);
		// 这一年的第几天
		int dayOfYear=SeaBackKit.getDayOfYear();
		int dayAndTimes[]=SeaBackKit.get2ShortInInt(dayAndTime);
		if(dayAndTimes[0]!=dayOfYear)
		{
			return 0;
		}
		return dayAndTimes[1];
	}

	/** 获取今天该领第几天 */
	public int getTodayRewardTime()
	{
		int nowDay=SeaBackKit.getDayOfYear();
		int times[]=SeaBackKit.get2ShortInInt(reward);
		if(nowDay-times[0]>1) return 0;
		// 是今天
		times[1]=times[1]%PublicConst.DAYAWARD.length;
		//if(times[1]<0) times[1]=0;
		if(times[0]==nowDay)
		{
			return times[1]-1<0?PublicConst.DAYAWARD.length-1:times[1]-1;
		}
		return times[1];
	}
	/** 获得今天领没领 */
	public int getTodayReward()
	{
		int nowDay=SeaBackKit.getDayOfYear();
		int times[]=SeaBackKit.get2ShortInInt(reward);
		// 是今天
		if(times[0]==nowDay) return 1;
		return 0;
	}
	/** 增加一次能量购买次数 */
	public void addBuyActivesTime()
	{
		int nowDay=SeaBackKit.getDayOfYear();
		int times[]=SeaBackKit.get2ShortInInt(actives[ENERGY_BUY_TIME]);
		int buyTime;
		// 是今天
		if(times[0]==nowDay)
		{
			times[1]++;
			buyTime=SeaBackKit.put2ShortInInt(times[0],times[1]);
		}
		else
		{
			times[0]=nowDay;
			times[1]=1;
			buyTime=SeaBackKit.put2ShortInInt(times[0],times[1]);
		}
		actives[ENERGY_BUY_TIME]=buyTime;
	}

	/** 获取玩家可以建筑的候补队列数量 */
	public int getPlayerDequeNum()
	{
		return island.getProduceNum();
	}

	/** 升级某个技能 */
	public void skillUpLevel(int skillSid)
	{
		Object object[]=skills.toArray();
		for(int i=0;i<object.length;i++)
		{
			Skill skill=(Skill)object[i];
			if(skill.getSid()==skillSid)
			{
				skill.setLevel(skill.getLevel()+1);
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.SKILL_UP_TASK_EVENT,null,this,null);
				return;
			}
		}
		Skill skill=(Skill)FightScene.abilityFactory.newSample(skillSid);
		skill.setLevel(1);
		skills.add(skill);
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.SKILL_UP_TASK_EVENT,null,this,null);
	}

	/** 获得某个技能 */
	public Skill getSkillBySid(int sid)
	{
		Object object[]=skills.toArray();
		for(int i=0;i<object.length;i++)
		{
			Skill skill=(Skill)object[i];
			if(skill.getSid()==sid)
			{
				return skill;
			}
		}
		return null;
	}

	/**
	 * 检查物品是否足够 propSid 物品sid num 物品数量
	 */
	public boolean checkPropEnough(int propSid,int num)
	{
		if(propSid==0) return true;
		int haveNum=bundle.getCountBySid(propSid);
		return haveNum>=num;
	}

	/** 获得当前升级经验 */
	public long getNowLevelUpExp()
	{
		if(level>level_exps.length) return 0;
		long needExp=level_exps[level-1];
		long have=needExp-experience;
		if(level==level_exps.length) return (have-1);
		return have;
	}

	/** 获得总经验 */
	public long getTotalExperience()
	{
		long total=0;
		for(int i=0;i<level-1;i++)
		{
			total+=level_exps[i];
		}
		total+=experience;
		return total;
	}

	/** 刷新等级 */
	public int flashLevel()
	{
		if(level<1) level=1;
		if(level>PublicConst.MAX_PLAYER_LEVEL)
			level=PublicConst.MAX_PLAYER_LEVEL;
		int addLevel=0;
		int length=PublicConst.MAX_PLAYER_LEVEL-1<Player.level_exps.length
			?PublicConst.MAX_PLAYER_LEVEL-1:Player.level_exps.length;
		for(int i=level-1;i<length;i++)
		{
			if(experience>=Player.level_exps[i])
			{
				addLevel++;
				experience-=Player.level_exps[i];
				continue;
			}
			break;
		}
		if(experience>Player.level_exps[length-1])
			experience=Player.level_exps[length-1];
		setLevel(level+addLevel);
		if(addLevel>0&&level2push!=null&&level2push.length>0)
			for(int i=0;i<level2push.length;i++){
				if(level2push[i]==level){
					JBackKit.sendLevel2Push(this,level);
					break;
				}
			}
		if(addLevel>0&&getLevel()==5)
		{
			// 新版本赠送白银船锚
			getBundle().incrProp(
				(Prop)Prop.factory.newSample(PlayerAttPort.SILVER_WHEEL),
				true);
		}
		return addLevel;
	}

	/** 增加经验值 */
	public boolean incrExp(int n,CreatObjectFactory factory)
	{
		if(n<=0||experience+n<0) return false;// 越界判定
		if(level>=PublicConst.MAX_PLAYER_LEVEL) return false;
		// if(n>100000)
		// {
		// log.warn("player_exp_add:"+"name==="+getName()+",n==="+n
		// +",playerLevel="+getLevel()+",========"+experience);
		// }
		experience+=n;
		if(flashLevel()>0)
		{
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.PLAYER_LEVEL_ISLAND_EVENT,this,this,null);
			// 成就数据采集
			AchieveCollect.playerLevel(this);
//			flushGodSend();
			// 战友系统
			ComradeHandler.getInstance().finishTask(this,ComradeTask.LEVEL);
			if(factory!=null)
				JBackKit.sendFightScore(this,factory,true,FightScoreConst.PLAYER_LV_UP);
		}
		// if(n>100000)
		// {
		// log.warn("player_exp_add:"+"name==="+getName()+",n==="+n
		// +",playerLevel="+getLevel()+",========"+experience);
		// }
		return true;
	}

	/** 获得当前声望升级经验 */
	public int getNowHonorUpExp()
	{
		if(honor[HONOR_LEVEL_INDEX]>honor_exps.length) return 0;
		int needExp=honor_exps[honor[HONOR_LEVEL_INDEX]-1];
		int have=needExp-honor[HONOR_INDEX];
		if(honor[HONOR_LEVEL_INDEX]==honor_exps.length) return (have-1);
		return have;
	}

	/** 获得声望总经验 */
	public long getTotalHonorExperience()
	{
		long total=0;
		for(int i=0;i<honor[HONOR_LEVEL_INDEX]-1;i++)
		{
			total+=honor_exps[i];
		}
		total+=honor[HONOR_INDEX];
		return total;
	}

	/** 刷新声望等级 */
	public int flashHonorLevel()
	{
		if(honor[HONOR_LEVEL_INDEX]<0) honor[HONOR_LEVEL_INDEX]=0;
		if(honor[HONOR_LEVEL_INDEX]>PublicConst.MAX_HONOR_LEVEL)
			honor[HONOR_LEVEL_INDEX]=PublicConst.MAX_HONOR_LEVEL;
		int addLevel=0;
		int length=PublicConst.MAX_HONOR_LEVEL-1<honor_exps.length
			?PublicConst.MAX_HONOR_LEVEL-1:honor_exps.length;
		for(int i=honor[HONOR_LEVEL_INDEX]-1;i<length;i++)
		{
			if(honor[HONOR_INDEX]>=honor_exps[i])
			{
				addLevel++;
				honor[HONOR_INDEX]-=honor_exps[i];
				continue;
			}
			break;
		}
		if(honor[HONOR_INDEX]>honor_exps[length-1])
			honor[HONOR_INDEX]=honor_exps[length-1];
		honor[HONOR_LEVEL_INDEX]+=addLevel;
		return addLevel;
	}

	/** 增加声望经验值 */
	public boolean incrHonorExp(int n)
	{
		if(n<=0||honor[HONOR_INDEX]+n<0) return false;// 越界判定
		if(honor[HONOR_LEVEL_INDEX]>=PublicConst.MAX_HONOR_LEVEL) return false;
		// if(n>400)
		// {
		// log.warn("player_honor_add=start:"+"name==="+getName()+",n==="+n
		// +",honorLevel="+honor[HONOR_LEVEL_INDEX]+",========"
		// +honor[HONOR_INDEX]);
		// }
		honor[HONOR_INDEX]+=n;
		if(flashHonorLevel()>0)
		{
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.RANK_HONOR_TASK_EVENT,this,this,null);
			// 成就信息采集
			AchieveCollect.honorLevel(honor[HONOR_LEVEL_INDEX],this);
		}
		// if(n>400)
		// {
		// log.warn("player_honor_add=start:"+"name==="+getName()+",n==="+n
		// +",honorLevel="+honor[HONOR_LEVEL_INDEX]+",========"
		// +honor[HONOR_INDEX]);
		// }
		return true;
	}
	/** 检查资源是否足够 */
	public boolean checkResource(PlayerBuild playerBuild,int buildLevel)
	{
		return Resources.checkResources(
			playerBuild.getLevelMetalCost(buildLevel),
			playerBuild.getLevelOilCost(buildLevel),
			playerBuild.getLevelSiliconCost(buildLevel),
			playerBuild.getLevelUraniumCost(buildLevel),
			playerBuild.getLevelMoneyCost(buildLevel),resources);
	}

	/** 检查升级船只资源是否足够 */
	public boolean checkResourceForUpShips(int shipSid,int shipNum)
	{
		Ship ship=(Ship)Role.factory.getSample(shipSid);
		if(ship==null) return false;
		return Resources.checkResources(ship.getUpgradeResources(),
			resources,shipNum);
	}

	/** 检查造船资源是否足够 船厂 城防 可以配置成船 */
	public boolean checkResourceForShips(int shipSid,int shipNum)
	{
		Ship ship=(Ship)Role.factory.getSample(shipSid);
		if(ship==null) return false;
		return Resources.checkResources(ship.getCostResources(),resources,
			shipNum);
	}

	/** 检查物品资源是否足够 */
	public boolean checkResourceForPorps(int propSid,int propNum)
	{
		Prop prop=(Prop)Prop.factory.getSample(propSid);
		if(prop==null) return false;
		return Resources.checkResources(prop.getCostResources(),resources,
			propNum);
	}

	/** 检查升级科技资源是否足够 */
	public boolean checkResourceForScience(int scienceSid,
		PlayerBuild scienceBuild,Player player)
	{
		Science science=(Science)Science.factory.getSample(scienceSid);
		if(science==null) return false;
		if(!(scienceBuild.getProduce() instanceof ScienceProduce))
			return false;
		ScienceProduce produce=(ScienceProduce)scienceBuild.getProduce();
		Science have=produce.getScienceBySid(scienceSid);
		int level=0;
		if(have!=null) level=have.getLevel();
		// 资源限制
		if(!Resources.checkResources(science.getLevelMetalCost(level),
			science.getLevelOilCost(level),
			science.getLevelSiliconCost(level),
			science.getLevelUraniumCost(level),
			science.getLevelMoneyCost(level),resources))
		{
			JBackKit.sendResetResources(player);
			return false;
		}
		// 物品消耗限制
		int propCostSid=science.getLevelPropSidCost(level);
		return player.checkPropEnough(propCostSid,1);
	}
	/** 该岛屿是否拥有此index */
	public boolean isPlayerHaveIndex(int index)
	{
		return island.isPlayerHaveIndex(index);
	}

	/** 对应位置是否可以建筑该类型建筑 */
	public boolean isBuildThisType(String buildType,int index)
	{
		return island.isBuildThisType(buildType,index);
	}

	/** 获得兵力上限 */
	public int getShipNum()
	{
		int commanderNum=0;
		if(getCommanderLevel()>0)
			commanderNum=PublicConst.COMMANDER_TROOPS[getCommanderLevel()-1];
		int num=PublicConst.PLAYER_LEVEL_TROOPS[getLevel()-1]+commanderNum;
		return num;
	}

	/** 添加一个服务 同类型增加时间 */
	public boolean addService(Service service,int checkTime)
	{
		if(service.getServiceTime()<0) return true;
		Service ser=checkService(service.getServiceType(),checkTime);
		if(ser!=null)
		{
			ser.addTime(service.getServiceTime());
			return true;
		}
		ser=(Service)Service.factory.newSample(service.getSid());
		ser.setServiceTime(service.getServiceTime());
		ser.setEndTime(checkTime+service.getServiceTime());
		this.service.add(ser);
		//重置buff
		SeaBackKit.resetPlayerSkill(this,null);
		return true;
	}
	
	/**服务 同类型减少时间 */
	public boolean reduceService(int type,int reduceTime)
	{
		int timeNow=TimeKit.getSecondTime();
		Service ser=checkService(type,TimeKit.getSecondTime());
		if(ser==null) return true;
		ser.reduceTime(reduceTime);
		if(ser.isOver(timeNow))
		{
			this.service.remove(ser);
		}
		// 重置buff
		SeaBackKit.resetPlayerSkill(this,null);
		return true;
	}
	
	/** 添加永久服务(直到设置结束时间) */
	public void addForeverService(ForeverService service,int checkTime)
	{
		Service ser=checkService(service.getServiceType(),checkTime);
		if(ser!=null)
		{
			service.setEndTime(-1);
		}
		else
		{
			this.service.add(service);
			//重置buff
			SeaBackKit.resetPlayerSkill(this,null);
		}
	}

	/** 根据type一处service */
	public boolean removeService(int serviceType)
	{
		Object services[]=service.getArray();
		for(int i=0;i<services.length;i++)
		{
			if(services[i]==null) continue;
			Service service=(Service)services[i];
			if(service.getServiceType()==serviceType)
			{
				return removeService(service);
			}
		}
		return false;
	}

	/** 移除一个服务 */
	public boolean removeService(Service service)
	{
		/**和平旗移除 特殊处理*/
		if(service.serviceType==PublicConst.NOT_FIGHT_BUFF)
		{
			log.info("NOT_FIGHT_BUFF IS REMOMVE_  ID:"+id+"    TIME="+TimeKit.getSecondTime());
		}
		this.service.remove(service);
		return true;
	}

	/** 检查某个服务是否到期 到期就移除 否则返回 */
	public Service checkService(int type,int checkTime)
	{
		Object[] service=this.service.toArray();
		for(int i=0;i<service.length;i++)
		{
			Service ser=(Service)service[i];
			if(ser.getServiceType()==type)
			{
				if(ser.isOver(checkTime))
				{
					this.service.remove(ser);
					//重置buff
					SeaBackKit.resetPlayerSkill(this,null);
					return null;
				}
				return ser;
			}
		}
		return null;
	}

	/** 检查某个服务是否到期 到期就移除 否则返回 */
	public void checkService(int checkTime)
	{
		Object[] service=this.service.toArray();
		for(int i=0;i<service.length;i++)
		{
			Service ser=(Service)service[i];
			if(ser.isOver(checkTime))
			{
				this.service.remove(ser);
				//重置buff
				SeaBackKit.resetPlayerSkill(this,null);
			}
		}
	}

	/** *根据当前的时间检查服务的状态 如果服务已经结束 返回服务的结束时间 */
	public Service checkSeriveTime(int serviceType,int checkTime)
	{
		Object[] service=this.service.toArray();
		for(int i=0;i<service.length;i++)
		{
			Service ser=(Service)service[i];
			if(ser.getServiceType()==serviceType)
			{
				if(ser.isOver(checkTime))
				{
					this.service.remove(ser);
					SeaBackKit.resetPlayerSkill(this,null);
					return ser;
				}
				return null;
			}
		}
		return null;
	}

	/** *根据当前的时间检查服务的状态 如果服务已经结束 返回服务的结束时间 */
	public Service checkSeriveTime1(int serviceType,int checkTime)
	{
		Object[] service=this.service.toArray();
		for(int i=0;i<service.length;i++)
		{
			Service ser=(Service)service[i];
			if(ser.getServiceType()==serviceType)
			{
				if(ser.isOver(checkTime))
				{
					this.service.remove(ser);
					//重置buff
					SeaBackKit.resetPlayerSkill(this,null);
				}
				return ser;
			}
		}
		return null;
	}

	/** 获得某一种类型的服务 */
	public Service getServiceByType(int type)
	{
		Object[] service=this.service.toArray();
		for(int i=0;i<service.length;i++)
		{
			Service ser=(Service)service[i];
			if(ser.getServiceType()==type) return ser;
		}
		return null;
	}
	/** 获取某个SID的服务 */
	public Service getServiceBySid(int sid)
	{
		Object[] service=this.service.toArray();
		for(int i=0;i<service.length;i++)
		{
			Service ser=(Service)service[i];
			if(ser.getSid()==sid) return ser;
		}
		return null;
	}

	/** 扣除生产建筑的资源 */
	public void reduceResource(PlayerBuild playerBuild,int buildLevel)
	{
		Resources.reduceResources(resources,
			playerBuild.getLevelMetalCost(buildLevel),
			playerBuild.getLevelOilCost(buildLevel),
			playerBuild.getLevelSiliconCost(buildLevel),
			playerBuild.getLevelUraniumCost(buildLevel),
			playerBuild.getLevelMoneyCost(buildLevel),this);
	}
	/** 扣除生产的资源 */
	public void reduceBuidResource(long resources[],int num)
	{
		// resources是getsamle出来 需要clone
		resources=resources.clone();
		for(int i=0;i<resources.length;i++)
		{
			resources[i]*=num;
		}
		Resources.reduceResources(getResources(),resources,this);
	}
	
	/** 扣除生产的资源 */
	public void reduceBuidResource(int resources[],int num)
	{
		// resources是getsamle出来 需要clone
		resources=resources.clone();
		for(int i=0;i<resources.length;i++)
		{
			resources[i]*=num;
		}
		Resources.reduceResources(getResources(),resources,this);
	}
	
	/**
	 * @return actives
	 */
	public int[] getActives()
	{
		return actives;
	}

	/**
	 * @param actives 要设置的 actives
	 */
	public void setActives(int[] actives)
	{
		this.actives=actives;
	}

	/** 获取全部属性 */
	public String[] getAttributes()
	{
		return attributes!=null?attributes.getArray():AttributeList.NULL;
	}

	/**
	 * @param attributes 要设置的 attributes
	 */
	public void setAttributes(AttributeList attributes)
	{
		this.attributes=attributes;
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

	/**
	 * @return inviter_id
	 */
	public int[] getInviter_id()
	{
		return inviter_id;
	}

	/**
	 * @param inviter_id 要设置的 inviter_id
	 */
	public void setInviter_id(int[] inviter_id)
	{
		this.inviter_id=inviter_id;
	}

	/**
	 * @return player_type
	 */
	public int getPlayerType()
	{
		return playerType;
	}

	/**
	 * @param player_type 要设置的 player_type
	 */
	public void setPlayerType(int player_type)
	{
		this.playerType=player_type;
		if(this.playerType>PublicConst.MILITARY_RANK_LEVEL.length)
			this.playerType=PublicConst.MILITARY_RANK_LEVEL.length+1;
	}

	/**
	 * @return reward
	 */
	public int getReward()
	{
		return reward;
	}

	/**
	 * @param reward 要设置的 reward
	 */
	public void setReward(int reward)
	{
		this.reward=reward;
	}

	/**
	 * @return udid
	 */
	public String getUdid()
	{
		return udid;
	}

	/**
	 * @param udid 要设置的 udid
	 */
	public void setUdid(String udid)
	{
		this.udid=udid;
	}

	/**
	 * @return user_id
	 */
	public int getUser_id()
	{
		return user_id;
	}

	/**
	 * @param user_id 要设置的 user_id
	 */
	public void setUser_id(int user_id)
	{
		this.user_id=user_id;
	}

	/**
	 * @return user_state
	 */
	public int getUser_state()
	{
		return user_state;
	}

	/**
	 * @param user_state 要设置的 user_state
	 */
	public void setUser_state(int user_state)
	{
		if(user_state<0||user_state>10) return;
		this.user_state=user_state;
	}

	/**
	 * @return locale
	 */
	public int getLocale()
	{
		return locale;
	}

	/**
	 * @param locale 要设置的 locale
	 */
	public void setLocale(int locale)
	{
		this.locale=locale;
	}

	/**
	 * 得到联合舰队关卡
	 */
	public HCityCheckPoint getHeritagePoint()
	{
		return heritagePoint;
	}

	public void setHeritagePoint(HCityCheckPoint heritagePoint)
	{
		this.heritagePoint=heritagePoint;
	}

	/**军备航线**/
	public ArmsRoutePoint getArmsroutePoint()
	{
		return armsroutePoint;
	}

	
	public void setArmsroutePoint(ArmsRoutePoint armsroutePoint)
	{
		this.armsroutePoint=armsroutePoint;
	}

	/**精英战场**/
	public ElitePoint getElitePoint()
	{
		return elitePoint;
	}

	
	public void setElitePoint(ElitePoint elitePoint)
	{
		this.elitePoint=elitePoint;
	}

	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}
	/**
	 * @param id 要设置的 id
	 */
	public void setId(int id)
	{
		this.id=id;
	}
	public void bytesWriteQuest(ByteBuffer data)
	{
		taskManager.bytesWrite(data);
	}

	public void bytesReadQuest(ByteBuffer data)
	{
		taskManager.bytesRead(data);
	}

	public void bytesWriteBulids(ByteBuffer data)
	{
		island.bytesWrite(data);
	}

	public void bytesReadBulids(ByteBuffer data)
	{
		island.bytesRead(data);
	}
	public void bytesWriteTearCheckPoint(ByteBuffer data)
	{
		tearCheckPoint.bytesWrite(data);
	}

	public void bytesReadTearCheckPoint(ByteBuffer data)
	{
		tearCheckPoint.bytesRead(data);
	}
	public void bytesWriteSelfCheckPoint(ByteBuffer data)
	{
		selfCheckPoint.bytesWrite(data);
		selfCheckPoint.bytesWriteAwardRecord(data);
	}

	public void bytesReadSelfCheckPoint(ByteBuffer data)
	{
		selfCheckPoint.bytesRead(data);
		selfCheckPoint.bytesReadAwardRecord(data);
	}

	public void bytesWriteArmsRoutePoint(ByteBuffer data)
	{
		armsroutePoint.bytesWrite(data);
	}

	public void bytesReadArmsRoutePoint(ByteBuffer data)
	{
		armsroutePoint.bytesRead(data);
	}
	
	public void bytesWriteElitePoint(ByteBuffer data)
	{
		elitePoint.bytesWrite(data);
	}
	
	public void bytesReadElitePoint(ByteBuffer data)
	{
		elitePoint.bytesRead(data);
	}
	public void bytesWriteProduceQueue(ByteBuffer data)
	{
		int top=data.top();
		int buildLen=0;
		data.writeByte(buildLen);
		int[] builds=productRecord.keyArray();
		// 建筑位置
		for(int i=0;i<builds.length;i++)
		{
			IntList indexList=(IntList)productRecord.get(builds[i]);
			if(indexList!=null&&indexList.size()>0)
			{
				buildLen++;
				data.writeByte(builds[i]);
				data.writeByte(indexList.size());
				// 建造队列
				for(int j=0;j<indexList.size();j++)
				{
					// 该队列位置产品id
					data.writeInt(indexList.get(j));
				}
			}
		}
		if(buildLen>0)
		{
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(buildLen);
			data.setTop(newTop);
		}
	}

	public void bytesReadProduceQueue(ByteBuffer data)
	{
		int buildLen=data.readByte();
		for(int i=0;i<buildLen;i++)
		{
			int buildIndex=data.readByte();
			IntList tempList=new IntList();
			int indexLen=data.readByte();
			for(int j=0;j<indexLen;j++)
			{
				tempList.add(data.readInt());
			}
			productRecord.put(buildIndex,tempList);
		}
	}
	
	public void bytesWriteHeritageCityPoint(ByteBuffer data)
	{
		heritagePoint.bytesWrite(data);
	}

	public void bytesReadHeritageCityPoint(ByteBuffer data)
	{
		heritagePoint.bytesRead(data);
	}
	public void bytesWriteLocaitonSave(ByteBuffer data)
	{
		if(locationSaveList!=null&&locationSaveList.size()>0)
		{
			data.writeByte(locationSaveList.size());
			for(int i=0;i<locationSaveList.size();i++)
			{
				((IslandLocationSave)locationSaveList.get(i))
					.bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	public void bytesReadLocaitonSave(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return;
		for(int i=0;i<n;i++)
		{
			IslandLocationSave save=new IslandLocationSave();
			save.bytesRead(data);
			locationSaveList.add(save);
		}
	}
	
	public void bytesReadEquips(ByteBuffer data){
		equips.bytesRead(data);
	}
	
	public void bytesWriteEquips(ByteBuffer data){
		equips.bytesWrite(data);
	}

	public void bytesReadFormations(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		IntList fg=null;
		for(int i=0;i<len;i++)
		{
			fg=new IntList();
			int shipLen=data.readUnsignedByte();
			for(int j=0;j<shipLen;j++)
			{
				fg.add(data.readUnsignedShort());
				fg.add(data.readUnsignedShort());
				fg.add(data.readUnsignedByte());
			}
			formationList.add(fg);
		}
	}
	
	public void bytesWriteFormations(ByteBuffer data)
	{
		int len=0;
		int top=data.top();
		data.writeByte(len);
		IntList fg=null;
		for(int i=0;i<formationList.size();i++)
		{
			fg=(IntList)formationList.get(i);
			if(fg!=null)
			{
				len++;
				data.writeByte(fg.size()/3);
				for(int j=0;j<fg.size();j+=3)
				{
					data.writeShort(fg.get(j));
					data.writeShort(fg.get(j+1));
					data.writeByte(fg.get(j+2));
				}
			}
		}
		int newTop=data.top();
		data.setTop(top);
		data.writeByte(len);
		data.setTop(newTop);
	}
	public void bytesWriteBetmap(ByteBuffer data)
	{
		int[] keys=betmap.keyArray();
		data.writeShort(keys.length);
		for(int i=0;i<keys.length;i++)
		{
			data.writeInt(keys[i]);
			data.writeInt((Integer)betmap.get(keys[i]));
		}
	}
	public void bytesReadBetmap(ByteBuffer data)
	{
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			betmap.put(data.readInt(),data.readInt());
		}
	}
	
	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteBundle(ByteBuffer data)
	{
		if(bundle!=null)
		{
			bundle.bytesWrite(data);
		}
		else
		{
			data.writeShort(bundleSize);
			data.writeShort(0);
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadBundle(ByteBuffer data)
	{
		bundle.bytesRead(data);
		return this;
	}

	public void bytesReadActives(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		actives=new int[length];
		for(int i=0;i<length;i++)
		{
			actives[i]=data.readInt();
		}
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteActives(ByteBuffer data)
	{
		data.writeByte(actives.length);
		for(int i=0;i<actives.length;i++)
		{
			data.writeInt(actives[i]);
		}
	}

	public void showBytesWriteActives(ByteBuffer data,int time)
	{
		data.writeByte(actives.length);
		for(int i=0;i<actives.length;i++)
		{
			if(i==ENERGY_INDEX)
			{
				// 当前精力值
				data.writeInt(actives[i]);
			}
			else if(i==ENERGY_TIME_INDEX)
			{
				// 写入离下一次计算能量的差距时间
				data.writeInt(ENERGY_TIME-time+actives[i]);
			}
			else if(i==ENERGY_BUY_TIME)
			{
				// 今天购买能量的次数
				data.writeInt(getTodayBuyTimes());
			}
		}
	}

	public void showBytesWriteOfficers(ByteBuffer data)
	{
		officers.showBytesWrite(data,this);
	}
	
	public void showBytesWriteTel(ByteBuffer data,CreatObjectFactory factory)
	{
		String[] tel=TelBindingManager.getPlayerBindingTel(this,factory);
		String phone="";
		String zone="";
		if(tel!=null)
		{
			if(tel.length>0&&tel[0]!=null)
				zone=tel[0];
			if(tel.length>1&&tel[1]!=null)
				phone=tel[1];
		}
		data.writeUTF(phone);
		data.writeUTF(zone);
	}
	
	public void bytesReadResources(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		resources=new long[length];
		for(int i=0;i<length;i++)
		{
			resources[i]=data.readLong();
		}
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteResources(ByteBuffer data)
	{
		data.writeByte(resources.length);
		for(int i=0;i<resources.length;i++)
		{
			if(i==Resources.MAXGEMS)
			{
				data.writeLong(getVpPoint());
			}
			else if(i==Resources.GEMS)
			{
				data.writeLong(Resources.getGems(resources));
			}
			else
			{
				data.writeLong(resources[i]);
			}
		}
	}

	public void bytesReadInviter(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		inviter_id=new int[length];
		for(int i=0;i<length;i++)
		{
			inviter_id[i]=data.readInt();
		}
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteInviter(ByteBuffer data)
	{
		data.writeByte(inviter_id.length);
		for(int i=0;i<inviter_id.length;i++)
		{
			data.writeInt(inviter_id[i]);
		}
	}
	/** 前台序列化 关卡BUFF */
	public void showBytesWritePointBuff(ByteBuffer data)
	{
		if(pointBuff==null || pointBuff.length==0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(pointBuff.length);
			for(int i=0;i<pointBuff.length;i++)
			{
				data.writeShort(PublicConst.SHOW_SIDS[i]);
				data.writeByte(pointBuff[i]);
			}
		}
	}

	/** 序列化 舰船等级（技能sid&等级） */
	public void bytesWriteShipLevel(ByteBuffer data)
	{
		if(shipLevel==null)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(shipLevel.length);
			for(int i=0;i<shipLevel.length;i++)
			{
				data.writeInt(shipLevel[i]);
			}
		}
	}

	/** 反序列化 舰船等级（技能sid&等级） */
	public void bytesReadShipLevel(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		if(len>0)
		{
			shipLevel=new int[len];
			for(int i=0;i<len;i++)
			{
				shipLevel[i]=data.readInt();
			}
		}
	}
	/** 序列化给前台成就） */
	public void showBytesAchieveData(ByteBuffer data)
	{
		achieveData.showBytesWrite(data,this);
	}

	/** 序列化 累计消费记录 */
	public void bytesWriteConsumeGemsData(ByteBuffer data)
	{
		int[] aids=consumeGems.keyArray();
		data.writeByte(aids.length);
		for(int i=0;i<aids.length;i++)
		{
			ConsumeGemsRecord record=(ConsumeGemsRecord)consumeGems.get(aids[i]);
			record.bytesWrite(data);
		}
	}

	/** 反序列化 累计消费记录 */
	public void bytesReadConsumeGemsData(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			ConsumeGemsRecord record=ConsumeGemsRecord.bytesRead(data);
			consumeGems.put(record.getAid(),record);
		}
	}
	/** 序列化新兵福利记录 */
	public void bytesWriteRecruit(ByteBuffer data)
	{
		recruit.bytesWrite(data);
	}
	/** 反序列化新兵福利记录 */
	public void bytesReadRecruit(ByteBuffer data)
	{
		recruit.bytesRead(data);
	}
	/** 序列化军官信息 */
	public void bytesWriteOfficers(ByteBuffer data)
	{
		officers.bytesWrite(data);
	}
	/** 反序列化军官信息 */
	public void bytesReadOfficers(ByteBuffer data)
	{
		officers.bytesRead(data);
	}
	/**序列化军官商店**/
	public void bytesWriteOffcerShop(ByteBuffer data)
	{
		officers.bytesWriteShop(data);
	}
	/** 反序列化军官商店 */
	public void bytesReadOffcerShop(ByteBuffer data)
	{
		officers.bytesReadShop(data);
	}
	/** 序列化战友信息 */
	public void bytesWriteComrade(ByteBuffer data)
	{
		comrade.bytesWrite(data);
	}
	/** 反序列化战友信息 */
	public void bytesReadComrade(ByteBuffer data)
	{
		comrade.bytesRead(data);
	}
	/** 序列化邮件附件记录 */
	public void bytesWriteAnnex(ByteBuffer data)
	{
		data.writeShort(annex.size());
		for(int i=0;i<annex.size();i++)
		{
			data.writeInt(annex.get(i));
		}
	}
	/** 反序列化邮件附件记录 */
	public void bytesReadAnnex(ByteBuffer data)
	{
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			annex.add(data.readInt());
		}
	}
	
	/** 序列化联盟宝箱  */
	public void bytesWriteAllianceChest(ByteBuffer data)
	{
		allianceChest.bytesWrite(data);
	}
	/** 反序列化联盟宝箱  */
	public void bytesReadAllianceChest(ByteBuffer data)
	{
		allianceChest.bytesRead(data);
	}
	/** 序列化联盟商品兑换次数  */
	public void bytesWritePropExchangeNum(ByteBuffer data)
	{
		int[] keys=propExchangeNum.keyArray();
		data.writeShort(keys.length);
		for(int i=0;i<keys.length;i++)
		{
			data.writeInt(keys[i]);
			data.writeInt((Integer)propExchangeNum.get(keys[i]));
		}
	}
	/** 反序列化联盟商品兑换次数  */
	public void bytesReadPropExchangeNum(ByteBuffer data)
	{
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			propExchangeNum.put(data.readInt(),data.readInt());
		}
	}
	/** 序列化成长计划  */
	public void bytesWriteGrowthPlan(ByteBuffer data)
	{
		growthPlan.bytesWrite(data);
	}
	/** 反序列化成长计划  */
	public void bytesReadGrowthPlan(ByteBuffer data)
	{
		growthPlan.bytesRead(data);
	}
	/** 序列化给前台 舰船等级（技能sid&等级） */
	public void showBytesWriteShipLevel(ByteBuffer data)
	{
		if(shipLevel==null)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(shipLevel.length);
			for(int i=0;i<shipLevel.length;i++)
			{
				data.writeShort(shipLevel[i]>>>16);
				data.writeByte((shipLevel[i]<<16)>>>16);
			}
		}
	}

	/** 写入GM权限 */
	public void bytesWriteGMPermit(ByteBuffer data)
	{
		String gmPermit=(String)getAttributes(PublicConst.PLAYER_GM);
		if(gmPermit==null||gmPermit.length()==0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(Integer.parseInt(gmPermit));
		}
	}

	public void showBytesWrite(ByteBuffer data,int current,
		CreatObjectFactory factoy)
	{
		super.bytesWrite(data);
		data.writeInt(id);
		data.writeByte(level);
		data.writeLong(experience);
		data.writeByte(playerType);
		data.writeByte(commanderLevel);
		data.writeByte(user_state);
		data.writeByte(getTodayReward());// 今天是否领取，0/1
	    data.writeByte(getTodayRewardTime());// 今天领取登录奖励的次数
		data.writeByte(getLottoCount(PublicConst.LOTTO_FREE));// 今天已经抽取了免费抽奖次数
		data.writeByte(getLottoCount(PublicConst.LOTTO_ADVANCE));// 今天已经抽取的高级抽奖次数
		data.writeBoolean(getBasicLotto()[0]!=0);//低级乐透激活状态
		data.writeBoolean(getBasicLotto()[1]!=0);//高级乐透激活状态
		String followStr=getAttributes(PublicConst.BASIC_LOTTO_FOLLOW);
		int follows=followStr!=null&&!"".equals(followStr)?Integer.valueOf(followStr):0;
		data.writeShort(follows);// 军需引导步骤
		data.writeByte(getResetTaskNum());
		data.writeUTF(name);
		data.writeInt(muteTime-current);
		data.writeShort(getShowTaskMark());
		data.writeInt(inveted);
		data.writeLong(plunderResource);
		data.writeInt(fightScore);
		bytesWriteGMPermit(data);
		data.writeUTF(deviceToken);
		island.showBytesWrite(data,current,factoy);
		showBytesWriteActives(data,current);
		bytesWriteResources(data);
		getVitality(factoy);//任务序列化之前初始化活跃度任务避免状态不一致
		taskManager.showBytesWrite(data,current);
		data.writeInt(getVitality(factoy)[1]);//活跃值
		data.writeInt(SeaBackKit.getTimesnight()-TimeKit.getSecondTime());// 活跃度任务刷新时间
		data.writeByte(getVitality(factoy)[2]);//活跃值奖励领取信息
		bundle.showBytesWrite(data,current);
		showBytesWriteServices(data,current);
		bytesWriteHonor(data);
		selfCheckPoint.showBytesWrite(data);
		tearCheckPoint.showBytesWrite(data);// 添加的
		heritagePoint.showBytesWrite(data);// 添加的
		bytesWriteInviter(data);
		bytesWriteSkills(data);
		showBytesWriteShipLevel(data);// 添加的
		// 商品sid
		data.writeByte(PublicConst.SHOP_SELL_SIDS.length);
		for(int i=0;i<PublicConst.SHOP_SELL_SIDS.length;i++)
		{
			data.writeShort(PublicConst.SHOP_SELL_SIDS[i]);
		}
		// 序列化限时购买
		showByteWriteLimitSale(data);
		// 序列化打折活动
		showByteWriteDiscount(data);
		// 每日折扣
		showByteWriteDatePriceOff(data);
		bytesWriteLocaitonSave(data);
		data.writeInt(honorScore);
		int eventId=0;
		if(getAttributes(PublicConst.ALLIANCE_DEFND_ATT)!=null
			&&!getAttributes(PublicConst.ALLIANCE_DEFND_ATT).equals(""))
		{
			eventId=Integer
				.parseInt(getAttributes(PublicConst.ALLIANCE_DEFND_ATT));
		}
		data.writeInt(eventId);
		// 联盟
		boolean bool=false;
		Alliance alliance=null;
		String alliance_id=getAttributes(PublicConst.ALLIANCE_ID);
		if(alliance_id!=null&&!alliance_id.equals(""))
		{
			alliance=(Alliance)factoy.getAllianceMemCache().loadOnly(
				alliance_id);
			if(alliance!=null&&alliance.getPlayerList().contain(id))
			{
				bool=true;
			}
			else
			{
				setAttribute(PublicConst.ALLIANCE_ID,null);
			}
		}
		//重置buff
		SeaBackKit.resetPlayerSkill(this,factoy);
		if(bool)
		{
			data.writeBoolean(true);
			alliance.showBytesWrite(data,this,factoy);
			AllianceFightManager.amanager.showBytesWrite(
				TextKit.parseInt(alliance_id),data);
			int time=0;
			if(getAttributes(PublicConst.ALLIANCE_BOSS_FIGHT)!=null
				&&!getAttributes(PublicConst.ALLIANCE_BOSS_FIGHT).equals(""))
			{
				int value=Integer
					.parseInt(getAttributes(PublicConst.ALLIANCE_BOSS_FIGHT));
				int nowDay=SeaBackKit.getDayOfYear();
				int day=value>>16;
				if(day==nowDay)
				{
					int nowTime=value<<16>>16;
					time=nowTime;
				}
			}
			data.writeByte(time);
			// 广播联盟人员上线
			ByteBuffer sendData=new ByteBuffer();
			sendData.clear();
			sendData.writeShort(AlliancePort.ALLIANCE_PORT);
			sendData.writeByte(AlliancePort.MEMBER_LOGIN_STATE_CHANGE);
			sendData.writeUTF(getName());
			sendAllAlliancePlayers(sendData,alliance,factoy);
		}
		else
		{
			data.writeBoolean(false);
		}
		factoy.getArenaManager().showBytesWritePlayer(this,data);
		data.writeBoolean(getCanFAward());
		data.writeByte(getFPAward());// 首充领取进度
		data.writeShort(getDrawDay());// 限时抽奖活动已抽次数
		data.writeShort(getClassicDrawDay());// 经典限时抽奖活动已抽次数
		data.writeBoolean(getInvitedStauts(factoy));
		showByteWriteVaribleAward(data);// 天降好礼
		showBytesWritePointBuff(data);// 关卡Buff
		showBytesAchieveData(data);// 成就序列化
		data.writeShort(getDayUpdateCD());//免费 刷新任务CD
		data.writeBoolean(getAutoLevelUp());// 建筑自动升级是否可用
		data.writeBoolean(isAbleadd());
		isAbleMouthCard(data);
		equips.showBytesWrite(data);
		data.writeInt(OnlineLuckyContainer.getInstance().getCountTime(this));// 在线奖励
		showBytesVipLimitAward(data);// vip限购
		showByteWriteTotalBuyAward(data);// 累计充值
		showByteWriteConsumeGems(data);// 累计消费
		showByteWriteDoubleGems(data);// 双倍充值
		armsroutePoint.showBytesWrite(data);//军备航线
		data.writeBoolean(getAttributes(PublicConst.CREAT_NAME)==null);//是否已创建名字
		data.writeByte(MealTimeManager.getInstance().getMealTimeEnergy());
		data.writeBoolean(MealTimeManager.getInstance().checkEnergerState(this));
		showByteWriteExtraGift(data);// 帝国援军
		// 显示统御升级幸运值
		String cmdLucky=getAttributes(PublicConst.COMMAND_UP_LUCKY);
		data.writeInt(cmdLucky==null?0:TextKit.parseInt(cmdLucky));
		data.writeInt(prosperityInfo[0]);//繁荣度指数
		data.writeInt(prosperityInfo[2]);//繁荣度MAX值
		data.writeInt(getAttrHead());//当前头像
		data.writeInt(getAttrHeadBorder());//当前边框
		data.writeShort(headInfoMap.size());//头像个数
		for(int i=0;i< headInfoMap.keyArray().length;i++){
			HeadData dh = (HeadData)headInfoMap.get(headInfoMap.keyArray()[i]);
			data.writeInt(dh.getSid());
			data.writeBoolean(dh.isEnabled());//激活
		}

	}
	
	/** 兼容更多序列化信息,新加序列化添加至此 */
	public void showBytesWrite2(ByteBuffer data,int current,
		CreatObjectFactory factoy)
	{
		showBytesWriteOfficers(data);
		// 新军需乐透当前免费次数
		data.writeByte(getLottoCount(PublicConst.LOTTO_FREE));
		// 最大免费次数
		data.writeByte(PublicConst.LOTTO_MAX_1);
		// 免费强制间隔时间
		data.writeInt(SeaBackKit.getFreeLottoTime(this,current));
		showBytesWriteTel(data,factoy);
	}
	
	/** 累计充值活动序列化 */
	public void showByteWriteTotalBuyAward(ByteBuffer data)
	{
		TotalBuyActivity activity=(TotalBuyActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.TOTALBUYGMES_ID,0);
		if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
		{
			data.writeByte(0);
		}else{
			activity.showByteWrite(data,this);
		}
	}
	/** 打折活动序列化 */
	public void showByteWriteDiscount(ByteBuffer data)
	{
		DiscountActivity activity=(DiscountActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.DISCOUNT_ID,0);
		if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
		{
			data.writeByte(0);
		}
		else
		{
			int[] sids=activity.getPropSids();
			data.writeByte(sids.length);// 打折商品数组长度
			for(int i=0;i<sids.length;i++)
			{
				Prop p=(Prop)Prop.factory.getSample(sids[i]);
				data.writeShort(sids[i]);// 物品SID
				data.writeShort(0);
				data.writeInt(ActivityContainer.getInstance().discountGems(
					sids[i],p.getNeedGems()));// 物品打折后需要的宝石数
			}
		}
	}

	/** 限时商品序列化 */
	public void showByteWriteLimitSale(ByteBuffer data)
	{
		LimitSaleActivity activity=(LimitSaleActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.LIMIT_ID,0);
		if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
		{
			data.writeByte(0);
		}
		else
		{
			IntKeyHashMap map=activity.getSid_num();
			int[] sids=map.keyArray();
			data.writeByte(sids.length);
			for(int i=0;i<sids.length;i++)
			{
				data.writeShort(sids[i]);// 物品SID
				data.writeShort(0);
				data.writeByte(getLimitSaleNum(activity.getId(),
					sids[i]));// 已购数量
				data.writeByte((Integer)map.get(sids[i]));// 限购数量
			}
		}
	}

	/** 天降好礼序列化 */
	public void showByteWriteVaribleAward(ByteBuffer data)
	{
		VaribleAwardActivity activity=(VaribleAwardActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.VARIBLE_AWARD,0);
		activity.showByteWrite(data,this);
	}

	/** 每日折扣序列化 */
	public void showByteWriteDatePriceOff(ByteBuffer data)
	{
		DatePriceOffActivity activity=(DatePriceOffActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.DATE_OFF_ID,0);
		if(activity!=null&&activity.isOpen(TimeKit.getSecondTime()))
		{
			activity.showByteWrite(data,this);
		}
		else
			DatePriceOffActivity.showByteWriteClosed(data);
	}
	
	/** 每档宝石首冲双倍序列化 */
	public void showByteWriteDoubleGems(ByteBuffer data)
	{
		DoubleGemsAcitivity activity=(DoubleGemsAcitivity)ActivityContainer
						.getInstance().getActivity(ActivityContainer.DOUBLE_GMES_ID,0);
		if(activity!=null&&activity.isOpen(TimeKit.getSecondTime()))
		{
			activity.showByteWrite(data,this);
		}
		else
			DoubleGemsAcitivity.showByteWriteClosed(data);
	}
	
	/** 累计消费记录序列化 */
	public void showByteWriteConsumeGems(ByteBuffer data)
	{
		ConsumeGemsActivity.showBytesWrite(this,data);
	}
	
	/** 帝国援军序列化 */
	public void showByteWriteExtraGift(ByteBuffer data)
	{
		int[] infos=getExtraAwardInfo();
		if(infos==null)
			data.writeInt(-1);
		else
		{
			data.writeInt(infos[0]);
			Award award=(Award)Award.factory
							.getSample(infos[1]);
						award.viewAward(data,this);
		}
	}
	
	/** 联盟广播上线 */
	public void sendAllAlliancePlayers(ByteBuffer data,Alliance alliance,
		CreatObjectFactory factoy)
	{
		SessionMap smap=factoy.getDsmanager().getSessionMap();
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

//	/** 增加玩家某种资源今天的捐献次数 */
//	public void addGiveValueForAlliance(int typeIndex)
//	{
//		String str=getAttributes(PublicConst.ALLIANCE_GIVE_TIMES);
//		int nowDay=SeaBackKit.getDayOfYear();
//		int value[]=PublicConst.ALLIANCE_GIVE_TIME_VALUE.clone();
//		value[0]=nowDay;
//		value[typeIndex+1]=1;
//		String strValue=value[0]+","+value[1]+","+value[2]+","+value[3]+","
//			+value[4]+","+value[5]+","+value[6];
//		if(str!=null&&!str.equals(""))
//		{
//			String strs[]=TextKit.split(str,",");
//			int day=Integer.parseInt(strs[0]);
//			if(nowDay==day)
//			{
//				strs[typeIndex+1]=(Integer.parseInt(strs[typeIndex+1])+1)+"";
//				strValue=strs[0]+","+strs[1]+","+strs[2]+","+strs[3]+","
//					+strs[4]+","+strs[5]+","+strs[6];
//			}
//		}
//		setAttribute(PublicConst.ALLIANCE_GIVE_TIMES,strValue);
//		// 增加贡献值
//	}
	/** 计算玩家今天的捐献次数 */
	public int[] todayAlliance()
	{
		String str=getAttributes(PublicConst.ALLIANCE_GIVE_TIMES);
		if(str==null||str.equals(""))
		{
			return PublicConst.ALLIANCE_GIVE_TIME_VALUE_FORE;
		}
		else
		{
			String strs[]=TextKit.split(str,",");
			// 存储的天数
			int nowDay=SeaBackKit.getDayOfYear();
			int day=Integer.parseInt(strs[0]);
			if(nowDay==day)
			{
				int value[]={Integer.parseInt(strs[1]),
					Integer.parseInt(strs[2]),Integer.parseInt(strs[3]),
					Integer.parseInt(strs[4]),Integer.parseInt(strs[5]),
					Integer.parseInt(strs[6])};
				return value;
			}
			else
			{
				return PublicConst.ALLIANCE_GIVE_TIME_VALUE_FORE;
			}
		}
	}
	/** 计算当天捐献点 */
	public int todayAlliancePoints(int[] todayNums)
	{
		int points = 0;
		int[] todayNum = todayNums;
		for(int i=0;i<todayNum.length;i++)
		{
			int subNum = todayNum[i];
			for(int j=0;j<subNum;j++)
				points+=(j+1);
		}
		return points;
	}
	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(id);
		data.writeByte(level);
		data.writeLong(experience);
		data.writeByte(playerType);
		data.writeByte(commanderLevel);
		data.writeByte(user_state);
		data.writeInt(user_id);
		data.writeInt(reward);
		data.writeByte(locale);
		data.writeUTF(name);
		data.writeInt(createTime);
		data.writeInt(muteTime);
		data.writeInt(updateTime);
		data.writeInt(exitTime);
		data.writeInt(saveTime);
		data.writeInt(onlineTime);
		data.writeShort(taskMark);
		data.writeInt(inveted);
		data.writeLong(plunderResource);
		data.writeInt(fightScore);
		data.writeInt(achieveScore);
		data.writeUTF(deviceToken);
		data.writeInt(honorScore);
		data.writeUTF(createIp);
		data.writeUTF(bindIp);
		data.writeUTF(loginIp);
		bytesWriteBulids(data);
		bytesWriteActives(data);
		bytesWriteResources(data);
		bytesWriteInviter(data);
		bytesWriteAttributes(data);
		bytesWriteQuest(data);
		bytesWriteBundle(data);
		bytesWriteServices(data);
		bytesWriteHonor(data);
		bytesWriteSelfCheckPoint(data);
		bytesWriteTearCheckPoint(data);
		bytesWriteHeritageCityPoint(data);
		bytesWriteSkills(data);
		bytesWriteLocaitonSave(data);
		bytesWriteShipLevel(data);
		bytesWritePointBuff(data);
		bytesWriteAchieve(data);
		bytesWriteEquips(data);
	}
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		id=data.readInt();
		level=data.readUnsignedByte();
		experience=data.readLong();
		playerType=data.readUnsignedByte();
		commanderLevel=data.readUnsignedByte();
		user_state=data.readUnsignedByte();
		user_id=data.readInt();
		reward=data.readInt();
		locale=data.readUnsignedByte();
		name=data.readUTF();
		createTime=data.readInt();
		muteTime=data.readInt();
		updateTime=data.readInt();
		exitTime=data.readInt();
		saveTime=data.readInt();
		onlineTime=data.readInt();
		taskMark=data.readUnsignedShort();
		inveted=data.readInt();
		plunderResource=data.readLong();
		fightScore=data.readInt();
		achieveScore=data.readInt();
		deviceToken=data.readUTF();
		honorScore=data.readInt();
		createIp=data.readUTF();
		bindIp=data.readUTF();
		loginIp=data.readUTF();
		bytesReadBulids(data);
		bytesReadActives(data);
		bytesReadResources(data);
		bytesReadInviter(data);
		bytesReadAttributes(data);
		bytesReadQuest(data);
		bytesReadBundle(data);
		bytesReadServices(data);
		bytesReadHonor(data);
		bytesReadSelfCheckPoint(data);
		bytesReadTearCheckPoint(data);
		bytesReadHeritageCityPoint(data);
		bytesReadSkills(data);
		bytesReadLocaitonSave(data);
		bytesReadShipLevel(data);
		bytesReadPointBuff(data);
		bytesReadAchieve(data);
		bytesReadEquips(data);
		return this;
	}
	public void bytesWritePointBuff(ByteBuffer data)
	{
		if(pointBuff==null)
			data.writeByte(0);
		else
		{
			data.writeByte(pointBuff.length);
			for(int i=0;i<pointBuff.length;i++)
			{
				data.writeByte(pointBuff[i]);
			}
		}
	}
	public void bytesReadPointBuff(ByteBuffer data)
	{
		int len=data.readByte();
		pointBuff=new int[len];
		for(int i=0;i<len;i++)
		{
			pointBuff[i]=data.readByte();
		}
	}
	
	public void byteWriteFriendInfo(ByteBuffer data){
		friendInfo.bytesWrite(data);
	}
	
	
	public void byteReadFriendInfo(ByteBuffer data){
		friendInfo.bytesRead(data);
	}

	public void bytesWriteAchieve(ByteBuffer data)
	{
		setAttribute(PublicConst.RES_TO_LONG,"long");
		achieveData.bytesWrite(data);
	}

	public void bytesReadAchieve(ByteBuffer data)
	{
		achieveData.bytesRead(data);
	}

	/**
	 * @return quest
	 */
	public TaskManager getTaskManager()
	{
		return taskManager;
	}

	/**
	 * @param taskManager 要设置的 quest
	 */
	public void setTaskManager(TaskManager taskManager)
	{
		this.taskManager=taskManager;
	}

	/**
	 * @return bundle
	 */
	public PropList getBundle()
	{
		return bundle;
	}

	/**
	 * @param bundle 要设置的 bundle
	 */
	public void setBundle(PropList bundle)
	{
		this.bundle=bundle;
	}

	/**
	 * @return bundleSize
	 */
	public int getBundleSize()
	{
		return bundleSize;
	}

	/**
	 * @param bundleSize 要设置的 bundleSize
	 */
	public void setBundleSize(int bundleSize)
	{
		this.bundleSize=bundleSize;
	}

	public void bytesReadHonor(ByteBuffer data)
	{
		honor[HONOR_INDEX]=data.readInt();
		honor[HONOR_LEVEL_INDEX]=data.readUnsignedByte();
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteHonor(ByteBuffer data)
	{
		data.writeInt(honor[HONOR_INDEX]);
		data.writeByte(honor[HONOR_LEVEL_INDEX]);
	}

	/** 复制方法（主要复制深层次的域变量，如对象、数组等） */
	public Object copy(Object obj)
	{
		Player p=(Player)super.copy(obj);
		p.actives=new int[ACTIVES_SIZE];
		p.bundle=new PropList();
		p.bundle.setSource(p);
		p.bundle.setChangeListener(changeListenerList);
		p.bundle.setLength(BUNDLE_SIZE);
		p.resources=new long[RESOURCES_SIZE];
		p.honor=new int[HONOR_SIZE];
		p.setAttributes(new AttributeList());
		p.taskManager=new TaskManager();
		p.taskManager.setPlayer(p);
		p.island=new Island();
		p.island.setPlayer(p);
		p.service=new ObjectArray();
		p.selfCheckPoint=new SelfCheckPoint();
		p.inviter_id=new int[0];
		p.locationSaveList=new ArrayList();
		p.skills=new ObjectArray();
		p.allianceList=new IntList();
		p.tearCheckPoint=new TearCheckPoint();
		p.achieveData=new AchieveData();
		p.achieveData.setPlayer(p);
		p.heritagePoint=new HCityCheckPoint();
		p.equips=new EquipList();
		p.equips.setPlayer(p);
		p.consumeGems=new IntKeyHashMap();
		p.armsroutePoint=new ArmsRoutePoint();
		p.elitePoint=new ElitePoint();
		p.productRecord=new IntKeyHashMap();
		p.formationList=new ArrayList();
		p.betmap=new IntKeyHashMap();
		p.officers=new OfficerCenter(p);
		p.recruit=new RecruitRecord();
		p.comrade=new Comrade();
		p.annex=new IntList();
		p.allianceChest=new AllianceChest();
		p.propExchangeNum=new IntKeyHashMap();
		p.growthPlan=new GrowthPlan();
		p.headInfoMap = new IntKeyHashMap();
		p.friendInfo = new FriendInfo();
		return p;
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadServices(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			temp[i]=Service.bytesReadService(data);
		}
		service=new ObjectArray(temp);
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteServices(ByteBuffer data)
	{
		if(service!=null&&service.size()>0)
		{
			Object[] array=service.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((Service)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void showBytesWriteServices(ByteBuffer data,int current)
	{
		if(service!=null&&service.size()>0)
		{
			Object[] array=service.getArray();
			int top=data.top();
			int decr=0;
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				if(((Service)array[i]).isOver(TimeKit.getSecondTime()))
				{
					decr++;
					continue;
				}
				((Service)array[i]).showBytesWrite(data,current);
			}
			if(decr>0)
			{
				int ntop=data.top();
				data.setTop(top);
				data.writeByte(array.length-decr);
				data.setTop(ntop);
			}
		}
		else
		{
			data.writeByte(0);
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
		skills=new ObjectArray(temp);
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteSkills(ByteBuffer data)
	{
		if(skills!=null&&skills.size()>0)
		{
			Object[] array=skills.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((Skill)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}
	// /** 反序列化引导进度 */
	// public Object bytesReadGuideMark(ByteBuffer data)
	// {
	// int len=data.readInt();
	// if(len<=0)return this;
	// guideMark=new int[len];
	// for(int i=0;i<len;i++)
	// {
	// guideMark[i]=data.readInt();
	// }
	// return this;
	// }
	// /** 序列化引导进度 */
	// public void bytesWriteGuideMark(ByteBuffer data)
	// {
	// if(guideMark==null)
	// {
	// data.writeInt(0);
	// }
	// else
	// {
	// data.writeInt(guideMark.length);
	// for(int i=0;i<guideMark.length;i++)
	// {
	// data.writeInt(guideMark[i]);
	// }
	//
	// }
	// }

	public void showBytesVipLimitAward(ByteBuffer data)
	{
		int length=PublicConst.VIP_LIMIT_AWARD.length/3;
		data.writeByte(length);
		int limit=0;
		String limitStr=getAttributes(PublicConst.VIP_LIMIT_SALE_RECORD);
		if(limitStr!=null&&!limitStr.equals(""))
			limit=TextKit.parseInt(limitStr);
		for(int i=0;i<PublicConst.VIP_LIMIT_AWARD.length;i+=3)
		{
			Award award=(Award)Award.factory
				.getSample(PublicConst.VIP_LIMIT_AWARD[i]);
			award.viewAward(data,this);
			// 折扣价
			data.writeInt(PublicConst.VIP_LIMIT_AWARD[i+2]);
			// 原价
			data.writeInt(PublicConst.VIP_LIMIT_AWARD[i+1]);
			// 购买状态，低位至高位对应vip等级高低
			int state=limit&(1<<(i/3));
			data.writeBoolean(state!=0);
		}
	}
	
	/** 添加一个邀请者 */
	public void addOneInviter(int inviteId)
	{
		if(inviter_id.length>=MAX_INVITER_NUM) return;
		int[] temp=new int[inviter_id.length+2];
		System.arraycopy(inviter_id,0,temp,0,inviter_id.length);
		inviter_id=temp;
		inviter_id[inviter_id.length-2]=inviteId;
		inviter_id[inviter_id.length-1]=0;
	}

	/**
	 * @return createAt
	 */
	public int getCreateTime()
	{
		return createTime;
	}

	/**
	 * @param createAt 要设置的 createAt
	 */
	public void setCreateTime(int createAt)
	{
		this.createTime=createAt;
	}

	/**
	 * @return muteTime
	 */
	public int getMuteTime()
	{
		return muteTime;
	}

	/**
	 * @param muteTime 要设置的 muteTime
	 */
	public void setMuteTime(int muteTime)
	{
		this.muteTime=muteTime;
	}

	/**
	 * @return updateTime
	 */
	public int getUpdateTime()
	{
		return updateTime;
	}

	/**
	 * @param updateTime 要设置的 updateTime
	 */
	public void setUpdateTime(int updateTime)
	{
		this.updateTime=updateTime;
	}

	/**
	 * @return service
	 */
	public ObjectArray getService()
	{
		return service;
	}

	/** 设置指定属性的值 */
	public void setAttribute(String key,String value)
	{
		if(value==null)
			attributes.remove(key);
		else
			attributes.set(key,value);
	}

	/**
	 * @param service 要设置的 service
	 */
	public void setService(ObjectArray service)
	{
		this.service=service;
	}

	/**
	 * @return island
	 */
	public Island getIsland()
	{
		return island;
	}

	/**
	 * @param island 要设置的 island
	 */
	public void setIsland(Island island)
	{
		this.island=island;
	}

	/**
	 * @return taskMark
	 */
	public int getTaskMark()
	{
		return taskMark;
	}
	
	/**
	 * @return taskMark
	 */
	public int getShowTaskMark()
	{
		String newPlayerAward=getAttributes(PublicConst.NEW_PLAYER_AWARD);
		if(newPlayerAward!=null&&!"".equals(newPlayerAward)) return 100;
		return taskMark;
	}

	/**
	 * @param taskMark 要设置的 taskMark
	 */
	public void setTaskMark(int taskMark)
	{
		if(taskMark>100) taskMark=100;
		this.taskMark=taskMark;
	}

	/**
	 * @return resources
	 */
	public long[] getResources()
	{
		return resources;
	}

	/**
	 * @param resources 要设置的 resources
	 */
	public void setResources(long[] resources)
	{
		this.resources=resources;
	}

	/**
	 * @return selfCheckPoint
	 */
	public SelfCheckPoint getSelfCheckPoint()
	{
		return selfCheckPoint;
	}

	/**
	 * @param selfCheckPoint 要设置的 selfCheckPoint
	 */
	public void setSelfCheckPoint(SelfCheckPoint selfCheckPoint)
	{
		this.selfCheckPoint=selfCheckPoint;
	}
	/**
	 * @return tearCheckPoint
	 */
	public TearCheckPoint getTearCheckPoint()
	{
		return tearCheckPoint;
	}
	/**
	 * @return inveted
	 */
	public int getInveted()
	{
		return inveted;
	}

	/**
	 * @param inveted 要设置的 inveted
	 */
	public void setInveted(int inveted)
	{
		this.inveted=inveted;
	}

	/**
	 * @return honor
	 */
	public int[] getHonor()
	{
		return honor;
	}

	/**
	 * @param honor 要设置的 honor
	 */
	public void setHonor(int[] honor)
	{
		this.honor=honor;
	}
	/**
	 * @return experience
	 */
	public long getExperience()
	{
		return experience;
	}

	/**
	 * @param experience 要设置的 experience
	 */
	public void setExperience(long experience)
	{
		this.experience=experience;
	}

	/**
	 * @return style
	 */
	public int getStyle()
	{
		return style;
	}

	/**
	 * @param style 要设置的 style
	 */
	public void setStyle(int style)
	{
		this.style=style;
	}

	/**
	 * @return commanderLevel
	 */
	public int getCommanderLevel()
	{
		return commanderLevel;
	}

	/**
	 * @param commanderLevel 要设置的 commanderLevel
	 */
	public void setCommanderLevel(int commanderLevel)
	{
		this.commanderLevel=commanderLevel;
	}

	/**
	 * @return fightScore
	 */
	public int getFightScore()
	{
		return fightScore;
	}

	/**
	 * @param fightScore 要设置的 fightScore
	 */
	public void setFightScore(int fightScore)
	{
		this.fightScore=fightScore;
	}

	public int getAchieveScore()
	{
		return achieveScore;
	}

	public void setAchieveScore(int achieveScore)
	{
		this.achieveScore=achieveScore;
	}

	public String getCreateIp()
	{
		return createIp;
	}

	public String getBindIp()
	{
		return bindIp;
	}

	public String getLoginIp()
	{
		return loginIp;
	}
	
	/** 
	 * 获取最后登录的客户端包名
	 * @return
	 */
	public String getBundleId()
	{
		return this.bundleId;
	}
	
	/**
	 * 设置最后登录的客户端包名
	 * @param bundleId
	 */
	public void setBundleId(String bundleId)
	{
		this.bundleId=bundleId;
	}

	/**
	 * @return plunderResource
	 */
	public long getPlunderResource()
	{
		return plunderResource;
	}

	/**
	 * @param plunderResource 要设置的 plunderResource
	 */
	public void setPlunderResource(long plunderResource)
	{
		this.plunderResource=plunderResource;
	}

	/**
	 * @return fightScoreRank
	 */
	public int getFightScoreRank()
	{
		return fightScoreRank;
	}

	/**
	 * @param fightScoreRank 要设置的 fightScoreRank
	 */
	public void setFightScoreRank(int fightScoreRank)
	{
		this.fightScoreRank=fightScoreRank;
	}

	/**
	 * @return plunderRank
	 */
	public int getPlunderRank()
	{
		return plunderRank;
	}

	/**
	 * @param plunderRank 要设置的 plunderRank
	 */
	public void setPlunderRank(int plunderRank)
	{
		this.plunderRank=plunderRank;
	}

	/**
	 * @return lastRankTime
	 */
	public int getLastRankTime()
	{
		return lastRankTime;
	}

	/**
	 * @param lastRankTime 要设置的 lastRankTime
	 */
	public void setLastRankTime(int lastRankTime)
	{
		this.lastRankTime=lastRankTime;
	}

	/**
	 * @return skills
	 */
	public ObjectArray getSkills()
	{
		return skills;
	}

	/**
	 * @param skills 要设置的 skills
	 */
	public void setSkills(ObjectArray skills)
	{
		this.skills=skills;
	}

	/**
	 * @return deviceToken
	 */
	public String getDeviceToken()
	{
		return deviceToken;
	}

	/**
	 * @param deviceToken 要设置的 deviceToken
	 */
	public void setDeviceToken(String deviceToken)
	{
		this.deviceToken=deviceToken;
	}

	/**
	 * @return locationSaveList
	 */
	public ArrayList getLocationSaveList()
	{
		return locationSaveList;
	}

	/**
	 * @param locationSaveList 要设置的 locationSaveList
	 */
	public void setLocationSaveList(ArrayList locationSaveList)
	{
		this.locationSaveList=locationSaveList;
	}

	/**
	 * @return honorScore
	 */
	public int getHonorScore()
	{
		return honorScore;
	}

	/**
	 * @param honorScore 要设置的 honorScore
	 */
	public void setHonorScore(int honorScore)
	{
		this.honorScore=honorScore;
	}

	/** 改变荣誉值 */
	public void changeHonorScore(int score)
	{
		honorScore+=score;
		if(honorScore<0) honorScore=0;
		AchieveCollect.honorScore(honorScore,this);
	}

	/**
	 * @return honorScoreRank
	 */
	public int getHonorScoreRank()
	{
		return honorScoreRank;
	}

	/**
	 * @return 得到随机量
	 */
	public IntKeyHashMap getGetrandom()
	{
		return getrandom;
	}

	/**
	 * @return 设置随机量
	 */
	public void setGetrandom(IntKeyHashMap getrandom)
	{
		this.getrandom=getrandom;
	}

	/**
	 * @param honorScoreRank 要设置的 honorScoreRank
	 */
	public void setHonorScoreRank(int honorScoreRank)
	{
		this.honorScoreRank=honorScoreRank;
	}

	public int getAchieveScoreRank()
	{
		return achieveScoreRank;
	}

	public void setAchieveScoreRank(int achieveScoreRank)
	{
		this.achieveScoreRank=achieveScoreRank;
	}

	public int getPlayerTaskMark()
	{
		return playerTaskMark;
	}

	public void setPlayerTaskMark(int playerTaskMark)
	{
		if(playerTaskMark>100) playerTaskMark=100;
		this.playerTaskMark=playerTaskMark;
	}

	public IntList getAllianceList()
	{
		return allianceList;
	}

	public void setAllianceList(IntList allianceList)
	{
		this.allianceList=allianceList;
	}

	public int[] getPointBuff()
	{
		return pointBuff;
	}

	public void setPointBuff(int[] pointBuff)
	{
		this.pointBuff=pointBuff;
	}

	public ArrayList getFormationList()
	{
		return formationList;
	}
	
	public int getEscapeDevice()
	{
		return escapeDevice;
	}
	
	public void setEscapeDevice(int escapeDevice)
	{
		this.escapeDevice=escapeDevice;
	}
	/**
	 * 获取指定抽奖类型的使用次数
	 * 
	 * @param type
	 * @return 如果今天已经抽过指定类型的奖，则返回已抽次数，否则返回0
	 */
	public int getLottoCount(int type)
	{
		String attr=getAttributes(PublicConst.LOTTO_COUNT);
		if(attr==null||attr.equals("")) return 0;
		int value=Integer.parseInt(attr);
		int day=value>>>16;
		int count1=(value>>>8)&0xff;
		int count2=(value&0xff);
		// 如果记录的不是今天
		if(day!=SeaBackKit.getDayOfYear())
		{
			return 0;
		}
		if(type==PublicConst.LOTTO_FREE)
			return count1;
		else if(type==PublicConst.LOTTO_ADVANCE) return count2;
		return 0;
	}

	public boolean incrLottoCount(int type,int count)
	{
		String attr=getAttributes(PublicConst.LOTTO_COUNT);
		int value=0;
		if(attr!=null) value=Integer.parseInt(attr);
		int day=value>>>16;
		int count1=(value>>>8)&0xff;
		int count2=(value&0xff);
		// 如果记录的不是今天，重新记录
		if(day!=SeaBackKit.getDayOfYear())
		{
			if(type==PublicConst.LOTTO_FREE)
			{
				count1=count;
				count2=0;
			}
			else if(type==PublicConst.LOTTO_ADVANCE)
			{
				count1=0;
				count2=count;
			}
			day=SeaBackKit.getDayOfYear();
		}
		// 如果是今天，在现在的次数上增加
		else
		{
			// 如果增加次数超过最大次数，返回失败
			if(type==PublicConst.LOTTO_FREE
				&&count1+count>PublicConst.LOTTO_MAX_1)
				return false;
			else if(type==PublicConst.LOTTO_ADVANCE
				&&count2+count>PublicConst.LOTTO_MAX_2) return false;
			if(type==PublicConst.LOTTO_FREE)
				count1+=count;
			else if(type==PublicConst.LOTTO_ADVANCE) count2+=count;
		}
		value=day<<16|((count1<<8)&0xff00)|(count2&0xff);
		setAttribute(PublicConst.LOTTO_COUNT,String.valueOf(value));
		return true;
	}

	/**
	 * 增加(新)日常领取宝石数量记录
	 * 
	 * @param count
	 * @return
	 */
	public boolean incrDailyGems(int count)
	{
		int nowGems=getNewDailyGemsCount();
		if(nowGems+count<0) return false;
		nowGems+=count;
		setAttribute(PublicConst.NEW_DAILY_GEM_COUNT,String.valueOf(nowGems));
		return true;
	}

	/**
	 * 获取日常领取宝石数量
	 * 
	 * @return
	 */
	public int getDailyGemsCount()
	{
		String gemsStr=getAttributes(PublicConst.DAILY_GEM_COUNT);
		int nowGems=0;
		if(gemsStr!=null)
		{
			nowGems=Integer.parseInt(gemsStr);
		}
		return nowGems;
	}
	
	/**
	 * 获取新日常领取宝石数量
	 * 
	 * @return
	 */
	public int getNewDailyGemsCount()
	{
		String gemsStr=getAttributes(PublicConst.NEW_DAILY_GEM_COUNT);
		int nowGems=0;
		if(gemsStr!=null)
		{
			nowGems=Integer.parseInt(gemsStr);
		}
		return nowGems;
	}

	/**
	 * 限时商品购买记录
	 * 
	 * @param activeTime
	 * @param sid
	 */
	public void limitSaleRecord(int aid,int sid,int gems)
	{
		String record=getAttributes(PublicConst.LIMIT_SALE_RECORD);
		if(record==null||!record.startsWith(aid+""))
		{
			record=aid+","+sid+"-"+1;
		}
		else
		{
			int saleNum=getLimitSaleNum(aid,sid);
			if(saleNum<=0)
			{
				record=record+","+sid+"-"+1;
			}
			else
			{
				record=TextKit.replace(record,","+sid+"-"+saleNum,","+sid
					+"-"+(saleNum+1));
			}
		}
		setAttribute(PublicConst.LIMIT_SALE_RECORD,record);
		//记录活动日志
		ActivityLogMemCache.getInstance().collectAlog(aid,sid+"",getId(),gems);
		// 刷新前台
		JBackKit.sendLimitSaleFlush(this);
	}
	/** 判定某限购物品能否购买 */
	public boolean canLimitSale(int activeTime,int max,int sid)
	{
		return getLimitSaleNum(activeTime,sid)<max;
	}
	/** 获取某限购物品的已购数量 */
	public int getLimitSaleNum(int activeTime,int sid)
	{
		String record=getAttributes(PublicConst.LIMIT_SALE_RECORD);
		if(record==null||!record.startsWith(activeTime+""))
		{
			return 0;
		}
		else
		{
			int index=record.indexOf(","+sid+"-");
			if(index<0)
			{
				return 0;
			}
			else
			{
				int indexS=record.indexOf("-",index)+1;
				int indexE=record.indexOf(",",indexS);
				if(indexE<0) indexE=record.length();
				return Integer.parseInt(record.substring(indexS,indexE));

			}
		}
	}
	/** 获取某船只技能等级 */
	public int getShipAbilityLevel(int shipSid)
	{
		if(shipLevel==null)
		{
			return 0;
		}
		for(int i=shipLevel.length-1;i>=0;i--)
		{
			if(shipLevel[i]>>>16==shipSid)
			{
				return (shipLevel[i]<<16)>>>16;
			}
		}
		return 0;
	}
	/** 升级舰船技能 */
	public int upShipLevel(int sid)
	{
		if(shipLevel==null)
		{
			shipLevel=new int[1];
			shipLevel[0]=sid<<16;
		}
		int index=-1;
		for(int i=shipLevel.length-1;i>=0;i--)
		{
			if(shipLevel[i]>>>16==sid)
			{
				index=i;
				break;
			}
		}
		int lvl=1;
		if(index<0)
		{
			int[] temp=new int[shipLevel.length+1];
			System.arraycopy(shipLevel,0,temp,0,shipLevel.length);
			shipLevel=temp;
			shipLevel[shipLevel.length-1]=(sid<<16)|1;
		}
		else
		{
			lvl=(shipLevel[index]<<16)>>>16;
			if(lvl<getLevel())
			{
				lvl++;
				shipLevel[index]=shipLevel[index]+1;
			}
		}
		return lvl;
	}
	/** 移除舰船技能 */
	public void removeShipLevel(int sid)
	{
		if(shipLevel==null)
			return;
		for(int i=shipLevel.length-1;i>=0;i--)
		{
			if(shipLevel[i]>>>16==sid)
			{
				int[] temp=new int[shipLevel.length-1];
				// 复制其前的内容
				System.arraycopy(shipLevel,0,temp,0,i);
				// 复制其后的内容
				System.arraycopy(shipLevel,i+1,temp,i,shipLevel.length-(i+1));
				shipLevel=temp;
				break;
			}
		}
	}
	/** 检测该船只等级技能是否可以重置,返回导致不成功的sid */
	public IntList checkShipLevelReset(int sid)
	{
		IntList list=new IntList();
		if(shipLevel!=null)
		{
			for(int i=shipLevel.length-1;i>=0;i--)
			{
				// 遍历所持技能
				LevelAbility ability=(LevelAbility)LevelAbility.factory
					.getSample(shipLevel[i]>>>16);
				if(ability!=null&&getShipAbilityLevel(ability.getSid())>0)
				{
					// 如果该技能的前置技能包含所检测的sid,判断是否有另外的前置满足重置条件
					if(SeaBackKit.isContainValue(ability.preSid,sid))
					{
						// 如果有更多前置,判断其他前置是否符合重置条件
						if(ability.preSid.length>1)
						{
							boolean isOtherFit=false;
							for(int j=0;j<ability.preSid.length;j++)
							{
								if(ability.preSid[j]!=sid
									&&getShipAbilityLevel(ability.preSid[j])>=getShipAbilityLevel(ability
										.getSid()))
								{
									// 有其他前置满足技能存在的条件
									isOtherFit=true;
									break;
								}
							}
							// 如果其他前置都不满足技能存在的条件,则不能进行重置
							if(!isOtherFit) list.add(ability.getSid());
						}
						else
							list.add(ability.getSid());
					}
				}
			}
		}
		return list;
	}
	/** 设定舰船技能 */
	public int setUpShipLevel(int sid,int level)
	{
		if(shipLevel==null)
		{
			shipLevel=new int[1];
			shipLevel[0]=sid<<16;
		}
		int index=-1;
		for(int i=shipLevel.length-1;i>=0;i--)
		{
			if(shipLevel[i]>>>16==sid)
			{
				index=i;
				break;
			}
		}
		int lvl=1;
		if(index<0)
		{
			int[] temp=new int[shipLevel.length+1];
			System.arraycopy(shipLevel,0,temp,0,shipLevel.length);
			shipLevel=temp;
			shipLevel[shipLevel.length-1]=(sid<<16)|level;
		}
		else
		{
				shipLevel[index]=(sid<<16)|level;
//				shipLevel[index]=level;
		}
		if(level<=0)
			removeShipLevel(sid);
		return lvl;
	}
	/** 根据shipSid 获取 舰船等级技能 影响 */
	public float[] getLevelAbilityValue(int shipSid)
	{
		if(shipLevel==null) return null;
		LevelAbility ability=null;
		for(int i=0;i<shipLevel.length;i++)
		{
			ability=(LevelAbility)LevelAbility.factory
				.getSample(shipLevel[i]>>>16);
			if(ability==null) continue;
			if(ability.isEffect(shipSid))
			{
				float[] add=new float[2];
				add[0]=ability.getAddLife((shipLevel[i]<<16)>>>16);
				add[1]=ability.getAddAttack((shipLevel[i]<<16)>>>16);
				return add;
			}
		}
		return null;
	}
	/** 首冲领奖值 */
	public int getFPAward()
	{
		String fp=getAttributes(PublicConst.FP_AWARD);
		if(fp!=null)
		{
			int value=Integer.parseInt(fp);
//			if(value==1)return (int)(Math.pow(2,8)-1);//兼容以前的领取值
			if(value==1)return 15;
			return (value<<16)>>16;
		}
		return 0;
	}
	/** 得到限时抽奖领取次数 */
	public int getDrawDay()
	{
		return getSomeDrawDay(PublicConst.LUCKY_DRAW,ActivityContainer.AWARD_ID);
	}
	/** 得到经典(无动画界面)限时抽奖领取次数 */
	public int getClassicDrawDay()
	{
		return getSomeDrawDay(PublicConst.LUCKY_DRAW_CLASSIC,ActivityContainer.AWARD_CLASSIC_ID);
	}
	
//	/** 获取通商航运已抽奖次数 */
//	public int getShippingDrawDay()
//	{
//		return getSomeDrawDay(PublicConst.LUCKY_DRAW_SHIPPING,
//			ActivityContainer.AWARD_SHIPPING_ID);
//	}
	
//	/** 获取全民抢"节"活动 抽奖次数 */
//	public int getRobFesDrawDay()
//	{
//		return getSomeDrawDay(PublicConst.LUCKY_DRAW_ROB,
//			ActivityContainer.AWARD_ROB_ID);
//	}
	
	private int getSomeDrawDay(String attr,int aSid)
	{
		String draw=getAttributes(attr);
		if(draw==null)
		{
			return 0;
		}
		String[] info=draw.split(",");
		int time=Integer.parseInt(info[0]);
		int count=Integer.parseInt(info[1]);
		Activity acrivity=ActivityContainer.getInstance().getActivity(
			aSid,0);
		if(acrivity==null)return 0;
		int atime=acrivity.getId();
		if(time==atime) return count;
		return 0;
	}
	
//	/**
//	 * 获取通商航运或全民抢"节"上次所在位置
//	 */
//	public int getLastLocation(String attr,int aSid)
//	{
//		String draw=getAttributes(attr);
//		if(draw==null)
//		{
//			//初始位置为0
//			return 0;
//		}
//		String[] info=draw.split(",");
//		int time=Integer.parseInt(info[0]);
//		int location=Integer.parseInt(info[2]);
//		Activity acrivity=ActivityContainer.getInstance().getActivity(
//			aSid,0);
//		if(acrivity==null)return 0;
//		int atime=acrivity.getId();
//		if(time==atime) return location;
//		return 0;
//	}

	/**
	 * 检查指定玩家是否被接管状态
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isTakeOver()
	{
		String str=getAttributes(PublicConst.TAKE_OVER);
		if(str==null) return false;
		String[] strs=TextKit.split(str,"|");
		if(strs.length!=2) return false;
		if(TextKit.valid(strs[1],TextKit.NUMBER)!=0) return false;
		int time=Integer.parseInt(strs[1]);
		// 如果接管已经超时，则清除
		if(TimeKit.getSecondTime()>time)
		{
			setAttribute(PublicConst.TAKE_OVER,null);
			return false;
		}
		return true;
	}
	/** 设置所有关卡加成等级 */
	public void setAllPointBuff()
	{
		if(pointBuff!=null && pointBuff.length>0) return ;
		CheckPoint checkPoint=(CheckPoint)CheckPoint.factory
						.newSample(selfCheckPoint.getCheckPointSid());
		if(checkPoint==null) return;
		int chapter=checkPoint.getChapter();
		Chapter ca=(Chapter)Chapter.factory.getSample(chapter);
		if(ca==null) return ;
		for(int i=0;i<PublicConst.SIDS.length;i++)
		{
			if(chapter>=PublicConst.SIDS[i])
			{
				Chapter ch=(Chapter)Chapter.factory.getSample(PublicConst.SIDS[i]);
				if(selfCheckPoint.checkChapterStar((PublicConst.SIDS[i]-1)))
				 addPointBuff(ch.getType(),Chapter.USE_STATE,null);
			}
		}
	}
	/** 增加关卡BUFF 返回BUFF sid */
	public int addPointBuff(int type,int state,CreatObjectFactory cfactory)
	{
		if(type<0) return 0;
		if(pointBuff!=null && pointBuff.length>=(type+1)) return 0;
		if(pointBuff==null) pointBuff=new int[type+1];
		else if(pointBuff.length<=type)
		{
			int[] temp=new int[type+1];
			System.arraycopy(pointBuff,0,temp,0,pointBuff.length);
			pointBuff=temp;
		}
		if(pointBuff[type]>state) return 0;
		pointBuff[type]=state;
		JBackKit.sendPointBuff(this,type);
		return PublicConst.SHOW_SIDS[type];
	}
	
	/**让buff生效**/
	public void usePointBuff(int type,CreatObjectFactory cfactory)
	{
		pointBuff[type]=Chapter.USE_STATE;
		if(cfactory!=null)
		{
			if(type==Chapter.ATTACK||type==Chapter.HP)
			{
				SeaBackKit.resetPlayerSkill(this,cfactory);
				JBackKit.sendFightScore(this,cfactory,true,FightScoreConst.CHECK_POINT_BUFF);
			}
			JBackKit.sendPointBuff(this,type);
		}
	}
	
	/** 获取关卡buff的状态 */
	public int getPointBuffLv(int type)
	{
		if(pointBuff==null||pointBuff.length<=type) return 0;
		return pointBuff[type]==0?0:3;
	}
	
	/**
	 * 获取前端BUFF加成的SID
	 * @return sid
	 */
	public int getProsperityInfoBuff(){
		int[] prs=getProsperityInfo();
		return PROSPERITY_BUFF_SID[prs[3]];
	}
	
	/** 改变成就 属性值 */
	public boolean changeAchieveValue(int key,long addValue)
	{
		return achieveData.changeAttrValue(key,addValue);
	}
	/** 获取成就属性值 */
	public long getAchieveValue(int key)
	{
		return achieveData.getAchieveValue(key);
	}
	/** 推动成就进度 */
	public int addAchieveProgress(int sid,int max)
	{
		return achieveData.addAchieveProgress(sid,max,this);
	}
	/** 获取成就的当前进度 */
	public int getAchieveProgress(int sid)
	{
		return achieveData.getAchieveProgress(sid);
	}
	/** 成就属性值清零 */
	public void clearAchieveAtr(int atrKey)
	{
		achieveData.clearAttr(atrKey);
	}
	/** 成就属性值清零 */
	public AchieveData getAchieveData()
	{
		return achieveData;
	}
	/** 推算连续登陆天数 */
	public int pushLoginDays()
	{
		String logind=getAttributes(PublicConst.SERIES_LOGIN);
		if(logind==null)
		{
			setAttribute(PublicConst.SERIES_LOGIN,1+"");
			return 1;
		}
		int loginday=Integer.parseInt(logind);
		if(SeaBackKit.isSameDay(updateTime,TimeKit.getSecondTime()))
			return loginday;
		int time1=updateTime+loginday*PublicConst.DAY_SEC;
		int time3=updateTime+PublicConst.DAY_SEC;
		int time2=TimeKit.getSecondTime();
		if(SeaBackKit.isSameDay(time1,time2)||SeaBackKit.isSameDay(time3,time2))
		{
			loginday+=1;
		}
		else
		{
			loginday=1;
		}
		setAttribute(PublicConst.SERIES_LOGIN,loginday+"");
		return loginday;
	}

	/** 增加连续登陆天数 */
	public void addLoginDays()
	{
		int loginday=pushLoginDays();
		updateTime=TimeKit.getSecondTime();
		// 成就数据采集
		AchieveCollect.seriesLogin(loginday,this);
	}
	/** 推算成就积分 */
	public void computeAchieveScore()
	{
		setAchieveScore(achieveData.computeScore());
	}
	/** 增加成就积分 */
	public void addAchieveScore(int add)
	{
		achieveScore+=add;
	}

	public void setCreateIp(String createIp)
	{
		this.createIp=createIp;
	}

	public void setBindIp(String bindIp)
	{
		this.bindIp=bindIp;
	}

	public void setLoginIp(String loginIp)
	{
		this.loginIp=loginIp;
	}

	/**
	 * 添加联盟邀请记录
	 * 
	 * @param allianceId
	 * @param invitePlayerId
	 * @param time
	 */
	public void addInvitedRecord(int allianceId,
		int invitePlayerId,int time)
	{
		String record=allianceId+":"+invitePlayerId+":"+time;
		String recordStr=this
			.getAttributes(PublicConst.ALLIANCE_INVITATION_RECORD);
		// 如果没有记录，直接添加
		if(recordStr==null||"".equals(recordStr))
		{
			this.setAttribute(PublicConst.ALLIANCE_INVITATION_RECORD,record);
		}
		else
		{
			synchronized(recordStr)
			{
				// 查看是否存在旧记录，存在就刷新
				String[] records=recordStr.split(",");
				for(int i=0;i<records.length;i++)
				{
					String[] lastRecord=records[i].split(":");
					if(TextKit.parseInt(lastRecord[0])==allianceId)
					{
						int recordIndex=recordStr.indexOf(records[i]);
						// 将原记录清除，新纪录添加到首部
						int subLength=0;
						int nextIndex=recordIndex+records[i].length();
						// 不是第一条记录
						if(recordIndex>0)
						{
							subLength=recordIndex-1;// 将这条记录前的分隔符排除掉(截取时不保留这条记录前分隔符)
						}
						// 是第一条，不是最后一条
						else if(nextIndex<recordStr.length())
						{
							nextIndex++;// 将这条记录后的分隔符排除掉(新的第一条记录不需要前分隔符)
						}
						recordStr=record+","
							+recordStr.substring(0,subLength)
							+recordStr.substring(nextIndex);
						this.setAttribute(
							PublicConst.ALLIANCE_INVITATION_RECORD,recordStr);
						return;
					}
				}
				// 不存在旧记录，直接首部添加
				if(records.length<100)
				{
					recordStr=record+","+recordStr;
				}
				else
				{
					// 记录满一百，截取前99个，首部添加
					recordStr=record+","
						+recordStr.substring(0,recordStr.lastIndexOf(","));
				}
				this.setAttribute(PublicConst.ALLIANCE_INVITATION_RECORD,
					recordStr);
			}
		}
	}

	/**
	 * 删除一条联盟邀请信息
	 * 
	 * @param allianceId
	 */
	public void delInvitedRecord(int allianceId)
	{
		String recordStr=this
			.getAttributes(PublicConst.ALLIANCE_INVITATION_RECORD);
		String[] records=recordStr.split(",");
		if(recordStr!=null&&!"".equals(recordStr))
		{
			synchronized(recordStr)
			{
				for(int i=0;i<records.length;i++)
				{
					String[] attrs=records[i].split(":");
					if(TextKit.parseInt(attrs[0])==allianceId)
					{
						int recordIndex=recordStr.indexOf(records[i]);
						int subLength=0;
						int nextIndex=recordIndex+records[i].length();
						// 不是第一条记录
						if(recordIndex>0)
						{
							subLength=recordIndex-1;// 将这条记录前的分隔符排除掉(截取时不保留这条记录的前分隔符)
						}
						// 是第一条，不是最后一条
						else if(nextIndex<recordStr.length())
						{
							nextIndex++;// 将这条记录后的分隔符排除掉(新的第一条记录不需要前分隔符)
						}
						// 将原记录清除
						recordStr=recordStr.substring(0,subLength)
							+recordStr.substring(nextIndex);
						this.setAttribute(
							PublicConst.ALLIANCE_INVITATION_RECORD,recordStr);
					}
				}
			}
		}
	}

	/** 清空联盟邀请信息 */
	public synchronized void delInvitedRecords()
	{
		this.setAttribute(PublicConst.ALLIANCE_INVITATION_RECORD,null);
	}
	/** 获取是否有联盟邀请记录 */
	public boolean getInvitedStauts(CreatObjectFactory objectFactory)
	{
		String allianceId=getAttributes(PublicConst.ALLIANCE_ID);
		if(allianceId!=null&&!"".equals(allianceId))
		{
			delInvitedRecords();
			return false;
		}
		String inviteRecord=getAttributes(PublicConst.ALLIANCE_INVITATION_RECORD);
		if(inviteRecord!=null&&!"".equals(inviteRecord))
		{
			String[] invitations=inviteRecord.split(",");
			for(int i=0;i<invitations.length;i++)
			{
				String[] infos=invitations[i].split(":");
				// 如果存在以联盟名称作为记录的信息,就清除,统一采用联盟id
				if(!infos[0].matches("\\d+"))
				{
					delInvitedRecords();
					return false;
				}
				Alliance alliance=((Alliance)objectFactory
					.getAllianceMemCache().loadOnly(infos[0]));
				if(alliance!=null)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**获取联盟邀请人**/
	public String  getAlliancePlayer(int aid)
	{
		String inviteRecord=getAttributes(PublicConst.ALLIANCE_INVITATION_RECORD);
		if(inviteRecord!=null&&!"".equals(inviteRecord))
		{
			String[] invitations=inviteRecord.split(":");
			for(int i=0;i<invitations.length;i+=3)
			{
				if(invitations[i].equals(aid+""))
				{
					return invitations[i+1];
				}
			}
		}
		return null;
	}
	/** 获取能否领取首冲 */
	public boolean getCanFAward()
	{
		if(resources[Resources.MAXGEMS]-getDailyGemsCount()>0
			&&getAttributes(PublicConst.FP_AWARD)!=null)
		{
			int fp_award=getFPAward();
			for(int i=0;i<PublicConst.F_PAY_AWARD.length;i++)
			{
				if((fp_award&(1<<i))==0) return true;
			}
		}
		return false;
	}

	/** 获取每日任务 免费CD时间 */
	public int getDayUpdateCD()
	{
		String upcd=getAttributes(PublicConst.DAY_TASK_UPDATE);
		int uptime=0;
		if(upcd!=null)uptime=Integer.parseInt(upcd);
		uptime+=PublicConst.DAY_TASK_UPDATE_CD*3600;
		//uptime+=30;
		int now=TimeKit.getSecondTime();
		return now>=uptime?0:uptime-now;
	}
	/**获取当前的vip成长点数**/
	public long getVpPoint()
	{
		String vpp=getAttributes(PublicConst.VIP_POINT);
		return resources[Resources.MAXGEMS]+(vpp==null?0:Integer.parseInt(vpp));
	}
	/**检测vip的等级**/
	public void flushVIPlevel()
	{
		   int vipState = 0;
	        // 检查vip等级
		   long vippoint = getVpPoint();
	        for (int i = 0; i < PublicConst.GEMS_FOR_VIP_LEVEL.length; i++)
	        {
	            if (vippoint >= PublicConst.GEMS_FOR_VIP_LEVEL[i])
	                vipState = i + 1;
	        }
	        setUser_state(vipState);
	}
	
	/** 设置建筑自动升级 */
	public void setAutoLevelUp(boolean isAuto)
	{
		this.setAttribute(PublicConst.BUILD_AUTO_LEVEL_UP,isAuto+"");
	}
	/** 获取建筑自动升级 */
	public boolean getAutoLevelUp()
	{
		String auto=this.getAttributes(PublicConst.BUILD_AUTO_LEVEL_UP);
		if(auto!=null&&"false".equals(auto)) return false;
		return true;
	}
	/**增加vip的成长值**/
	public void addGrowthPoint(long num)
	{
		String vpp=getAttributes(PublicConst.VIP_POINT);
		long vppoint=(vpp==null?0:TextKit.parseLong(vpp))+num;
		if(vppoint<0)
		{
			resources[Resources.MAXGEMS]+=vppoint;
			vppoint=0;
		}
		if(resources[Resources.MAXGEMS]<0) resources[Resources.MAXGEMS]=0;
		setAttribute(PublicConst.VIP_POINT,String.valueOf(vppoint));
		flushVIPlevel();//检测vip等级
	}
	/****
	 * 判断当前的vip等级 
	 * 来判断当前的建筑位是否可以购买
	 */
	private boolean  isAbleadd()
	{
		int buildnum=island.getBuildNum();
		if(buildnum>=Island.BUILD_MAX) return false;
		int canBuyNum=PublicConst.VIP_LEVEL_FOR_BUILD_DEQUE[user_state];
		if(canBuyNum<=buildnum) return false;
		return true;
	}
	/**
	 * 初始化月票数据
	 * @param data
	 * @return
	 */
	private ByteBuffer isAbleMouthCard(ByteBuffer data)
	{
		//判断是否可以领取
			String etime=getAttributes(PublicConst.END_TIME);
			int timenow=TimeKit.getSecondTime();
			int endtime=etime==null?0:Integer.parseInt(etime);
			if(endtime<=timenow)// 判断当前的月卡是否还有时间
			{
				data.writeInt(0);
			}
			else
			{
				int abletime=(endtime-timenow)/PublicConst.DAY_SEC+1;
				data.writeInt(abletime);// 天数
				int overplustime=SeaBackKit.getTimesnight()-timenow;
				data.writeInt(overplustime);
				String awardtime=getAttributes(PublicConst.AWARD_TIME);
				int atime=awardtime==null?0:Integer.parseInt(awardtime);
				if(SeaBackKit.isSameDay(atime,timenow))
					data.writeByte(0);// 判断当天是否可以领取宝石
				else
					data.writeByte(1);
			}
			return data;
		}

	
	public int getLoginLogTime()
	{
		return loginLogTime;
	}

	
	public void setLoginLogTime(int loginLogTime)
	{
		this.loginLogTime=loginLogTime;
	}


	/** 获取活跃度奖励状态 */
	public boolean getVitalityAward(int index){
		index--;
		int[] vitalitys=getVitality(null);
		int value=vitalitys[2];
		return (value&(1<<index))!=0;
	}

	/** 设置活跃度奖励状态 */
	public void setVitalityAward(int index){
		synchronized(vitalityLock)
		{
			int[] vitalitys=getVitality(null);
			index--;
			int value=vitalitys[2];
			value=value|(1<<index);
			setAttribute(PublicConst.DATE_VITALITY,vitalitys[0]+":"
				+vitalitys[1]+":"+value);
		}
	}

	public int getPlatid()
	{
		return platid;
	}

	/** 设置活跃度 */
	public void setVitality(int vitality)
	{
		synchronized(vitalityLock)
		{
			int[] vitalitys=getVitality(null);
			vitalitys[1]+=vitality;
			setAttribute(PublicConst.DATE_VITALITY,vitalitys[0]+":"
				+vitalitys[1]+":"+vitalitys[2]);
		}
	}
	
	/** 设置活跃度相关信息 ：日期，活跃值，奖励品领取情况*/
	public int[] getVitality(CreatObjectFactory factoy)
	{
		String vitalityStr=getAttributes(PublicConst.DATE_VITALITY);
		int[] vitality=new int[3];
		vitality[0]=SeaBackKit.getDayOfYear();
		vitality[1]=0;
		vitality[2]=0;
		if(vitalityStr!=null&&!"".equals(vitalityStr))
		{
			String[] vitalitys=vitalityStr.split(":");
			if(vitality[0]==Integer.valueOf(vitalitys[0]))
			{
				vitality[1]=Integer.valueOf(vitalitys[1]);
				vitality[2]=Integer.valueOf(vitalitys[2]);
				// setAttribute(PublicConst.DATE_VITALITY,vitalitys[0]+":"
				// +vitalitys[1]+":"+0);
				return vitality;
			}

		}
		// 初始化每日活跃度
		setAttribute(PublicConst.DATE_VITALITY,SeaBackKit.getDayOfYear()+":"
			+0+":"+0);
		checkSthMax(factoy);
		vitality=getVitality(factoy);
		return vitality;
	}
	
	/** 活跃度初始化，检测建筑，科技，技能，统御，声望是否达到max */
	private void checkSthMax(CreatObjectFactory factoy){
		boolean isMax=true;
		// 检测建筑等级
		Object[] builds=getIsland().getBuildArray();
		if(builds==null||builds.length<=0)
			isMax=false;
		else
		{
			IntKeyHashMap typeMap=new IntKeyHashMap();
			for(int i=0;i<builds.length;i++)
			{
				Build build=(Build)builds[i];
				if(typeMap.get(build.getSid())!=null) continue;
				if(build.getBuildLevel()<build.getMaxLevel())
				{
					isMax=false;
					break;
				}
				typeMap.put(build.getSid(),"");
			}
			// 如果所持建筑都达到满级，再判定是否包含了所有可建造的建筑
			if(isMax)
			{
				for(int i=0;i<builds2check.length;i++){
					if(typeMap.get(builds2check[i])==null){
						isMax=false;
						break;
					}
				}
			}
		}
		if(isMax)
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.BUILD_FINISH_TASK_EVENT,null,this,null);
		// 检测科技等级
		isMax=true;
		Science[] sciences=getIsland().getSciences();
		if(sciences==null||sciences.length<=0)
			isMax=false;
		else
		{
			IntKeyHashMap typeMap=new IntKeyHashMap();
			for(int i=0;i<sciences.length;i++)
			{
				if(typeMap.get(sciences[i].getSid())!=null) continue;
				if(sciences[i].getLevel()<sciences[i].getMaxLevel())
				{
					isMax=false;
					break;
				}
				typeMap.put(sciences[i].getSid(),"");
			}
			// 如果所持科技都达到满级，再判定是否包含了所有可升级的科技
			if(isMax)
			{
				for(int i=0;i<sciences2check.length;i++){
					if(typeMap.get(sciences2check[i])==null){
						isMax=false;
						break;
					}
				}
			}
		}
		if(isMax)
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.SCIENCE_LEVEL_UP_EVENT,null,this,null);
		// 检测技能等级
		isMax=true;
		Object[] skills=getSkills().getArray();
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
				for(int i=0;i<skills2check.length;i++){
					if(typeMap.get(skills2check[i])==null){
						isMax=false;
						break;
					}
				}
			}
		}
		if(isMax)
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.SKILL_UP_TASK_EVENT,null,this,null);
		// 检测统御等级
		if(getCommanderLevel()>=PublicConst.COMMANDER_SUCCESS.length)
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.COMMAND_UP_TASK_EVENT,null,this,null);
		// 检测声望等级
		if(honor[HONOR_LEVEL_INDEX]>=PublicConst.MAX_HONOR_LEVEL)
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.HONOR_UP_TASK_EVENT,null,this,null);
		// 检测联盟
		if(factoy==null)	return;
		int alliance_id=0;
		if(getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			alliance_id=Integer
				.parseInt(getAttributes(PublicConst.ALLIANCE_ID));
			Alliance alliance=(Alliance)factoy.getAllianceMemCache()
				.loadOnly(alliance_id+"");
			if(alliance!=null&&alliance.isLevelAndSkillsMax())
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.ALLIANCE_GIVE_TASK_EVENT,null,this,null);
		}
	}
	
	/** 设置低级乐透打开状态  */
	public void setLowLotto(int num){
		synchronized(lottoLock){
			int[] lottos=getBasicLotto();
			lottos[0]=num;
			setBasicLotto(lottos);
		}
	}
	
	/** 设置高级乐透打开状态  */
	public void setHighLotto(int num){
		synchronized(lottoLock){
			int[] lottos=getBasicLotto();
			lottos[1]=num;
			setBasicLotto(lottos);
		}
	}
	
	/** 设置军需乐透相关信息  */
	public void setBasicLotto(int[] lottos)
	{
		setAttribute(PublicConst.BASIC_LOTTO,lottos[0]+","+lottos[1]);
	}
	
	/** 获取军需乐透相关信息  */
	public int[] getBasicLotto(){
		int[] lottos=new int[2];
		String lottoStr=getAttributes(PublicConst.BASIC_LOTTO);
		if(lottoStr!=null&&!"".equals(lottoStr)){
			String[] lottosStrs=lottoStr.split(",");
			lottos[0]=Integer.valueOf(lottosStrs[0]);//已打开低级乐透
			lottos[1]=Integer.valueOf(lottosStrs[1]);//已打开高级乐透
		}
		else
		{
			setBasicLotto(lottos);
		}
		return lottos;
	}
	
	public void setPlatid(int platid)
	{
		this.platid=platid;
	}

	
	public String getLoginUid()
	{
		return loginUid;
	}

	
	public void setLoginUid(String loginUid)
	{
		this.loginUid=loginUid;
	}

	
	public EquipList getEquips()
	{
		return equips;
	}

	
	public void setEquips(EquipList equips)
	{
		this.equips=equips;
	}

	
	public String getArea()
	{
		return area;
	}

	
	public void setArea(String area)
	{
		this.area=area;
	}

	public String getPlat()
	{
		return plat;
	}

	
	public void setPlat(String plat)
	{
		this.plat=plat;
	}
	
	public int[] getShipLevel()
	{
		return shipLevel;
	}
	
	public void setShipLevel(int[] shipLevel)
	{
		this.shipLevel=shipLevel;
	}
	
	public IntKeyHashMap getConsumeGems()
	{
		return consumeGems;
	}

	
	public void setConsumeGems(IntKeyHashMap consumeGems)
	{
		this.consumeGems=consumeGems;
	}
	
	
	public int getExitTime()
	{
		return exitTime;
	}

	
	public void setExitTime(int exitTime)
	{
		this.exitTime=exitTime;
	}

	
	public int getSaveTime()
	{
		return saveTime;
	}

	
	public void setSaveTime(int saveTime)
	{
		this.saveTime=saveTime;
	}

	/**玩家的删除状态**/
	public int getDeleteTime()
	{
		return deleteTime;
	}

	
	public void setDeleteTime(int deleteTime)
	{
		this.deleteTime=deleteTime;
	}

	public int getOnlineTime()
	{
		return onlineTime;
	}

	
	public void setOnlineTime(int onlineTime)
	{
		this.onlineTime=onlineTime;
	}
	
	public OfficerCenter getOfficers()
	{
		return officers;
	}
	
	public RecruitRecord getRecruit()
	{
		return recruit;
	}
	
	public void setRecruit(RecruitRecord recruit)
	{
		this.recruit=recruit;
	}
	
	public AllianceChest getAllianceChest()
	{
		return allianceChest;
	}

	
	public void setAllianceChest(AllianceChest allianceChest)
	{
		this.allianceChest=allianceChest;
	}
	
	
	public IntKeyHashMap getPropExchangeNum()
	{
		return propExchangeNum;
	}

	
	public void setPropExchangeNum(IntKeyHashMap propExchangeNum)
	{
		this.propExchangeNum=propExchangeNum;
	}

	/** 资源是否转为 long */
	public boolean isResToLong()
	{
		return getAttributes(PublicConst.RES_TO_LONG)!=null;
	}
	
	/** 前端系统 */
	public boolean isIOS()
	{
		return SeaBackKit.isContainValue(PublicConst.IOS,platid);
	}
	
	/** 增加打boss事件id */
	public void  addBossFid(int fid)
	{
		String ids=getAttributes(PublicConst.BOSS_FIGHT_ID);
		if(ids!=null&&ids.contains(","+fid)) return;
		if(ids==null)
			ids=","+fid;
		else
			ids=ids+","+fid;
		setAttribute(PublicConst.BOSS_FIGHT_ID,ids);
	}
	/** 移除打boss事件id */
	public void  removeBossFid(int fid)
	{
		String ids=getAttributes(PublicConst.BOSS_FIGHT_ID);
		if(ids!=null&&ids.contains(","+fid))
		{
			ids=TextKit.replaceAll(ids,","+fid,"");
		}
	}
	/** 判断是否为打boss事件id */
	public boolean isBossFid(int fid)
	{
		String ids=getAttributes(PublicConst.BOSS_FIGHT_ID);
		if(ids!=null&&ids.contains(","+fid))
		{
			return true;
		}
		return false;
	}

	/** 产品事件id记录,key:建筑index,value:IntList(队列位置-事件id) */
	public IntKeyHashMap getProductRecord()
	{
		return productRecord;
	}
	
	/** 设置角色sid */
	public void setRoleSid(int sid)
	{
		super.setSid(sid);
	}
	
	/** 增加当日在线时间  仅 下线时 调用*/
	public void incrOnlineTime()
	{
		int now=TimeKit.getSecondTime();
		int begin=SeaBackKit.getSomedayBegin(0);
		int start=begin>updateTime?begin:updateTime;
		int curTime=now-start;
		if(SeaBackKit.isSameDay(saveTime,now))
		{
			curTime+=onlineTime;
		}
		setExitTime(now);
		setSaveTime(now);
		setOnlineTime(curTime);
		getComrade().setOnline(Comrade.OFF_LINE);
		ComradeHandler.getInstance().flushOnLine(this,Comrade.OFF_LINE);
	}
	/** 获取当日在线时间 */
	public int getCurrOnlineTime()
	{
		int now=TimeKit.getSecondTime();
		int curTime=0;
		if(SeaBackKit.isSameDay(saveTime,now))
		{
			curTime+=onlineTime;
		}
		if(exitTime==0||exitTime>updateTime)// 不在线
		{
			return curTime;
		}
		int begin=SeaBackKit.getSomedayBegin(0);
		int start=begin>updateTime?begin:updateTime;
		return curTime+(now-start);
	}

	
	/** 获取玩家帝国援军奖励信息,
	 *  返回null表示所有奖励已领取完毕
	 *  [0]返回>=0表示倒计时
	 *  [1]表示对应的奖励品
	 */
	public int[] getExtraAwardInfo()
	{
		if(getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
			return null;
		String extra=getAttributes(PublicConst.EXTRA_TROOPS_GIFT);
		if(extra!=null)
		{
			String[] extraInfos=TextKit.split(extra,",");
			int[] info=TextKit.parseIntArray(extraInfos);
			// 如果全部奖励领取完毕
			if(info==null)
				return null;
			int time=TimeKit.getSecondTime();
			info[1]=info[1]<=time?0:(info[1]-time);
			return info=new int[]{info[1],info[0]};
		}
		return null;
	}
	/** 设置下一个帝国援军奖励,如果全部奖励完毕则奖励品id设置为0 */
	public void setNextExtraAward()
	{
		String extra=getAttributes(PublicConst.EXTRA_TROOPS_GIFT);
		int[] awards=PublicConst.EXTRA_GIFT_AWARDS;
		int time=TimeKit.getSecondTime();
		if(extra!=null)
		{
			String[] extraInfos=TextKit.split(extra,",");
			int[] info=TextKit.parseIntArray(extraInfos);
			// 如果全部奖励领取完毕
			if(info[0]==awards[awards.length-2])
				extra=null;
			else
			{
				for(int i=0;i<awards.length;i+=2)
				{
					// 设置下一档
					if(awards[i]==info[0]&&awards.length>i+3)
					{
						extra=awards[i+2]+","+(time+awards[i+3]);
						break;
					}
					extra=null;
				}
			}
		}
		// 新手玩家
		else
			extra=awards[0]+","+(time+awards[1]);
		setAttribute(PublicConst.EXTRA_TROOPS_GIFT,extra);
	}
	
	/**设置玩家的统御等级**/
	public void setPlayerCommonLevel(int level)
	{
		if(level<0) return;
		commanderLevel=level;
	}
	
	/** 设置跨服领奖记录 （1,2,3,4,5,6保留key）:0不可领，1可领，2已领 */
	public void setCrossAwardState(int key,int value)
	{
		betmap.put(key,value);
	}
	
	/**
	 * 获取跨服领奖记录 （1跨服战id,2名次,3押注领奖记录,4参赛领奖记录,5预赛战报标记,6跨服战id
	 * 保留key）:0不可领，1可领，2已领
	 */
	public int getCrossAwardState(int key)
	{
		Integer va=(Integer)betmap.get(key);
		return va==null?0:va;
	}
	
	/** 加入跨服战 */
	public void jionCrossWar(int warid)
	{
		betmap.put(1,warid);
		betmap.put(2,0);
	}
	
	/** 更新n强 */
	public boolean setCrossSn(int warid,int sn)
	{
		Integer wid=(Integer)betmap.get(1);
		if(wid==null||wid!=warid)return false;
		betmap.put(2,sn);
		return true;
	}
	
	/**
	 * 押注
	 * @param warid 跨服战id
	 * @param cid 跨服唯一id
	 * @param gem 押注量
	 */
	public void bet(int warid,int cid,int gem)
	{
		if(gem<=0) return;
		Integer war=(Integer)betmap.get(0);
		if(war==null||war!=warid)
		{
			int[] keys=betmap.keyArray();
			for(int i=0;i<keys.length;i++)
			{
				if(keys[i]==1||keys[i]==2||keys[i]==3||keys[i]==4
					||keys[i]==5||keys[i]==6) continue;
				betmap.remove(keys[i]);
			}
			//System.out.println("-----bet---betmap.put(0,warid);-------");
			betmap.put(0,warid);
		}
		Integer gems=(Integer)betmap.get(cid);
		if(gems==null)
		{
			betmap.put(cid,gem);
			//System.out.println(cid+":---bet-----put(cid,gem)-------:"+gem);
		}
		else
		{
			betmap.put(cid,gems+gem>0?gems+gem:Integer.MAX_VALUE);
			//System.out.println(cid+":-----bet---put(cid,gem)-------:"+(gems+gem>0?gems+gem:Integer.MAX_VALUE));
		}
	}
	/** 检测能否押注（当前版本只有一次对一个人） */
	public boolean canBet(int warid)
	{
		Integer war=(Integer)betmap.get(0);
		if(war!=null&&war==warid) return false;
		return true;
	}
	/** 获取对某个玩家的押注量 */
	public int getBet(int cid)
	{
		if(cid<=0) cid=-1;
		Integer gems=(Integer)betmap.get(cid);
		return gems==null?0:gems;
	}
	/** 获取预赛标记状态 */
	public boolean getPreState(int warid)
	{
		Integer wid=(Integer)betmap.get(6);
		if(wid==null||wid!=warid) return false;
		Integer state=(Integer)betmap.get(5);
		if(state==null||state!=1) return false;
		return true;
	}

	
	public IntKeyHashMap getBetmap()
	{
		return betmap;
	}

	
	public void setBetmap(IntKeyHashMap betmap)
	{
		this.betmap=betmap;
	}
	
	/**判断是否刷新玩家的天降好礼**/
	public  void flushGodSend()
	{
		VaribleAwardActivity activity=(VaribleAwardActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.VARIBLE_AWARD,0);
		int[] aid=activity.getactivityId();
		if(aid==null||aid.length==0) return;
		for(int j=0;j<aid.length;j++)
		{
			String godInfo=attributes.get(PublicConst.VAR_PLAYER_INFO);
			if(godInfo==null||godInfo.length()==0)
			{
				attributes.set(PublicConst.VAR_PLAYER_INFO,aid[j]+","+level
					+","+updateTime);
				continue;
			}
			int index=godInfo.indexOf(String.valueOf(aid[j]));
			if(index==-1)
			{
				attributes.set(PublicConst.VAR_PLAYER_INFO,godInfo+","
					+aid[j]+","+level+","+updateTime);
				continue;
			}
			String[] info=godInfo.split(",");
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<info.length;i+=3)
			{
				if(TextKit.parseInt(info[i])==aid[j])
				{
					if(i!=0)
						sb.append(","+info[i]+","+level+","+info[i+2]);
					else
						sb.append(info[i]+","+level+","+info[i+2]);
					continue;
				}
				if(i!=0)
					sb.append(","+info[i]+","+info[i+1]+","+info[i+2]);
				else
					sb.append(info[i]+","+info[i+1]+","+info[i+2]);
			}
			attributes.set(PublicConst.VAR_PLAYER_INFO,sb.toString());
		}
		// 如果有等级提升 检测 天降好礼
		JBackKit.sendPlayerVaribleAward(this);
	}

	
	/**
	 * 减少繁荣度 1/10
	 * @return 减少的繁荣度
	 */
	public int reduceProsperity()
	{
		int prosperityLast = getProsperityInfo()[0];
		getIsland().gotProsperityInfo(TimeKit.getSecondTime());//取最新的繁荣度信息
		int prosperity = 0;
		synchronized(getProsperityInfo())
		{
			prosperity=getProsperityInfo()[0];
			if(prosperity!=0)
			{	// 向下取整
				prosperity-=(int)Math.floor(prosperity*0.1);
				// 重置繁荣度等级
				int lv=0;
				for(int i=0;i<Player.PROSPERITY_lV_BUFF.length;i+=3)
				{
					if(prosperity<Player.PROSPERITY_lV_BUFF[i]){
						lv=i/3-1;
						break;
					}
					if(lv==0&&i==Player.PROSPERITY_lV_BUFF.length-3){
						lv = (Player.PROSPERITY_lV_BUFF.length-3)/3;
					}
				}
				getProsperityInfo()[0]=prosperity;
				// 重置检查时间
				getProsperityInfo()[1]=TimeKit.getSecondTime();
				getProsperityInfo()[3]=lv;
				JBackKit.sendResetProsperity(this);
			}
		}
		return prosperityLast-prosperity;
	}
	
	
	/**增加个人积分**/
	public void addIntegral(int num)
	{
		String playerPoint=getAttributes(PublicConst.PLAYER_POINT_VALUE);
		if(playerPoint!=null && playerPoint.length()!=0)
		{
			num+=Integer.parseInt(playerPoint);
		}
		if(num<0)num=0;
		setAttribute(PublicConst.PLAYER_POINT_VALUE,num+"");
	}
	/**减少个人积分**/
	public void reduceIntegral(int num)
	{
		String playerPoint=getAttributes(PublicConst.PLAYER_POINT_VALUE);
		if(playerPoint!=null && playerPoint.length()!=0)
		{
			num=Integer.parseInt(playerPoint)-num;
		}
		if(num<0)num=0;
		setAttribute(PublicConst.PLAYER_POINT_VALUE,num+"");
	}
	/**获取个人积分**/
	public int getIntegral()
	{
		String playerPoint=getAttributes(PublicConst.PLAYER_POINT_VALUE);
		if(playerPoint==null||playerPoint.length()==0)
		{
			return 0;
		}
		return TextKit.parseInt(playerPoint);
	}
	/**增加玩家招募的充值记录**/
	public void incComardeCharge(int gems)
	{
		String charge=getAttributes(PublicConst.PLAYER_COMRADE_CHARGE);
		int chargeGems=0;
		if(charge!=null&&charge.length()!=0)
			chargeGems=TextKit.parseInt(charge);
		chargeGems+=gems;
		setAttribute(PublicConst.PLAYER_COMRADE_CHARGE,chargeGems+"");
	}
	/**获取玩家招募以后的充值记录**/
	public int getComardeCharge()
	{
		String charge=getAttributes(PublicConst.PLAYER_COMRADE_CHARGE);
		if(charge!=null&&charge.length()!=0)
			return TextKit.parseInt(charge);
		return 0;
	}
	
	public Comrade getComrade()
	{
		return comrade;
	}

	
	public void setComrade(Comrade comrade)
	{
		this.comrade=comrade;
	}

	
	public IntList getAnnex()
	{
		return annex;
	}

	
	public void setAnnex(IntList annex)
	{
		this.annex=annex;
	}

	
	public GrowthPlan getGrowthPlan()
	{
		return growthPlan;
	}

	
	public void setGrowthPlan(GrowthPlan growthPlan)
	{
		this.growthPlan=growthPlan;
	}

	/** 繁荣度相关信息   指数、checkTime、繁荣度max值、繁荣度等级*/
	public int[] getProsperityInfo()
	{
		if(prosperityInfo==null){
			int[] prosperityInfoNew = new int[4];
			prosperityInfo = prosperityInfoNew;
		}
		return prosperityInfo;
	}

	
	public void setProsperityInfo(int[] prosperityInfo)
	{
		this.prosperityInfo=prosperityInfo;
	}
	
	/**
	 * 头像是否已经激活
	 * 
	 * @param headSid
	 * @return
	 */
	public boolean isHeadEnabled(int headSid) {
		Object data = headInfoMap.get(headSid);
		if (data == null) {
			throw new DataAccessException(0, "headSid not find");
		}
		if (((HeadData) data).isEnabled()) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * 更新玩家拥有头像信息
	 * 
	 * @param headSid
	 */
	public void updateHeadInfo(int headSid, boolean isEnabled, boolean isUse) {
		if(headSid == 0) return;
		Object data = headInfoMap.get(headSid);
		if (data == null) {
			throw new DataAccessException(0, "headSid not find");
		} else {
			((HeadData) data).setEnabled(isEnabled);
			((HeadData) data).setUse(isUse);
		}
		setHeadAttribute();
	}
	
	private void setHeadAttribute() {
		StringBuilder sb = new StringBuilder();
		int[] headSids = headInfoMap.keyArray();
		for (int i = 0; i < headSids.length; i++) {
			HeadData head = (HeadData) headInfoMap.get(headSids[i]);
			sb.append(head.toString());
		}
		attributes.set(PublicConst.HEAD_INFO, sb.toString());
	}
	
	/**
	 * 加载玩家时，初始化头像信息
	 */
	public void parserHead() {
		headInfoMap.clear();
		//获取已经存在的头像
		String headAttribute = attributes.get(PublicConst.HEAD_INFO);
		if (headAttribute != null) {
			String[] parserData = TextKit.split(headAttribute, ";");
			for (int j = 0; j < parserData.length; j++) {
				if (parserData[j].length() > 0) {
					String[] parser = TextKit.split(parserData[j], ",");
					int headSid = TextKit.parseInt(parser[0]);
					boolean isEnabled = TextKit.parseBoolean(parser[1]);
					boolean isUse = TextKit.parseBoolean(parser[2]);
					headInfoMap.put(headSid, new HeadData(headSid, isEnabled, isUse));
				}
			}
		}
		//检查是否有新头像加入
		int[] heads = PublicConst.HEADICON;
		boolean isSave = false;
		for (int i = 0; i < heads.length; i++) {
			int headSid = heads[i];
			if (headSid > 0 && headInfoMap.get(headSid) == null) {//如果头像列表里面没有
				boolean isEnabled = false;
				boolean isUse = false;
				if (headSid == PublicConst.HEADSID_BOY || headSid == PublicConst.HEADSID_GIRL) {//如果是默认头像
					if (getSid() == 1 && headSid == PublicConst.HEADSID_BOY) {
						currentHeadSid = headSid;
						isUse = true;
					} else if (getSid() == 2 && headSid == PublicConst.HEADSID_GIRL) {
						currentHeadSid = headSid;
						isUse = true;
					}
					isEnabled = true;
					setAttribute(PublicConst.HEAD_SID,currentHeadSid+"");
				}
				headInfoMap.put(headSid, new HeadData(headSid, isEnabled, isUse));
				isSave = true;
			}
		}
		if (attributes.get(PublicConst.HEAD_TO_ACHIEVEMENT) == null ) {// 表示更新头像后第一次加载
			Sample[] achievements = Achievement.factory.getSamples();
			for (int i = 0; i < achievements.length; i++) {
				if (achievements[i] != null) {
					Achievement achievement = (Achievement) achievements[i];
					int sid = achievement.getId();// 成就id
					int progress = getAchieveProgress(sid);// 当前成就进度
					int len = achievement.getNeedValue().length;// 总进度长度
					boolean complete = progress >= len;// 是否完成所有进度
					for (int j = 0; j < len; j++) {
						if (!complete && j >= progress) {
							int nvalue = achievement.getNeedValue()[j];// 当前j进度的需求值
							long cvalue = getAchieveValue(achievement.getAtr_key());// 当前完成值
							if (cvalue < nvalue) {
								continue;
							}
						}
						int headSid = achievement.getHeadSid(j);
						if (headSid > 0 && !isHeadEnabled(headSid)) {
							updateHeadInfo(headSid, true, false);
						}
					}
				}
			}
		}
		
		if(!PublicConst.HEAD_SIGN.equals(attributes.get(PublicConst.HEAD_TO_ACHIEVEMENT))){//特殊处理一些 还未出现的成就
		AchieveCollect.honorScore(this.getHonorScore(),this);
		if(this.getAttributes(PublicConst.END_TIME)!=null)
			AchieveCollect.mouthCard(1,this);
		}
		if (isSave) {
			setHeadAttribute();
		}
		
	}
	
	/**
	 * 改变玩家头像
	 * 
	 * @param headSid
	 */
	public boolean changeCurrentHead(int headSid) {
		if (currentHeadSid == headSid) {
			return false;
		} else if (!isHeadEnabled(headSid)) {
			return false;
		}
		updateHeadInfo(currentHeadSid, true, false);
		updateHeadInfo(headSid, true, true);
		currentHeadSid = headSid;
		setAttribute(PublicConst.HEAD_SID,currentHeadSid+"");
		return true;
	}
	
	/**
	 * 改变玩家边框
	 */
	public boolean changeHeadBorder(int borderSid){
		if(borderSid == 0 ){
			setAttribute(PublicConst.HEAD_BORDER,String.valueOf(borderSid));
			return true;
		}
		
		int currentHeadBorderSid = getAttributes(PublicConst.HEAD_BORDER)==null?PublicConst.DEF_HEADBORDER:Integer.parseInt(getAttributes(PublicConst.HEAD_BORDER));
		if (currentHeadBorderSid == borderSid) {
			return false;
		} 
		//检测边框是否激活
		boolean hasnot = true;
		for(int i=0;i<PublicConst.HEADBORDER.length;i+=2){
			if(borderSid == PublicConst.HEADBORDER[i]){
				hasnot = false;
				if(PublicConst.HEADBORDER[i+1]>user_state) throw new DataAccessException(0, "vip lv not enough:");
			}
		}
		
		if(hasnot)
			throw new DataAccessException(0, "headBorder not find");
		
		setAttribute(PublicConst.HEAD_BORDER,String.valueOf(borderSid));
		return true;
	}
	
	public int getCurrentHeadSid() {
		return currentHeadSid;
	}

	public void setCurrentHeadSid(int currentHeadSid) {
		this.currentHeadSid = currentHeadSid;
	}

	
	public FriendInfo getFriendInfo()
	{
		return friendInfo;
	}

	
	public void setFriendInfo(FriendInfo friendInfo)
	{
		this.friendInfo=friendInfo;
	}
	
	public int getAttrHeadBorder(){
		return getAttributes((PublicConst.HEAD_BORDER))==null?PublicConst.DEF_HEADBORDER:Integer.parseInt(getAttributes(PublicConst.HEAD_BORDER));
	}
	
	public int getAttrHead(){
		if(getAttributes((PublicConst.HEAD_SID))==null)
		{
			if(getSid()==1)
			{
				return PublicConst.HEADSID_BOY;
			}
			else
			{
				return PublicConst.HEADSID_GIRL;
			}
		}
		return Integer.parseInt(getAttributes(PublicConst.HEAD_SID));
	}
} 

