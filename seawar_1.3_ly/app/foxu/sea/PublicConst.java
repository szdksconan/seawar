package foxu.sea;


/** 公用常量类 */
public class PublicConst
{
	/** 是否是跨服中心 */
	public static boolean crossServer;
	/** gm单次添加物品上限 */
	public static int ADD_PROP_LIMIT=30;
	/** 新手引导最大步数 */
	public static final int NEW_PLAYER_MARK_MAX=100;
	/** 新手引导奖励 */
	public static final int NEW_PLAYER_AWARD_SID=59168;
	/** platid 与前端ios,android分类 */
	public static final int[] IOS={0,6,7,16,20,22};
	/** 玩家船只sid */
	public static final int SHIP_FOR_SID[]={
		10001,10002,10003,10004,10005,10006,10007,10008,
		10011,10012,10013,10014,10015,10016,10017,10018,
		10021,10022,10023,10024,10025,10026,10027,10028,
		10031,10032,10033,10034,10035,10036,10037,10038};
	/** 限制用户登录的开关 */
	public static String LOGIN_LIMIT;
	/** 是否处于维护状态 */
	public static boolean READY=false;
	/**系统在联盟战中对战默认是星期天 欧美：联盟战的对战时间 是星期6 需要设置成true**/
	public static boolean READY_SATURDAY=false;
	/**邀请码,兑换码,制造车间,军需乐透,日本第三方支付,修改账号 开启的开关 */
	public static boolean[] SWITCH_STATE={true,true,false,true,false,false,true};
	/** 禁用充值货币  */
	public static String[] VALID_CURRENCY={"USD","CAD","MXN","NZD","JPY","SGD","IDR","INR","RUB","TRY","ILS","ZAR","SAR","none","cheat"};
	/** 限制时允许登录的用户id */
	public static int USER_ID_LIMIT[];
	/**第三方支付的方式开关*/
	public static boolean RECHANGE_STATLE=false;
	/** 每次宝石捐赠的系数 */
	public static final int ALLIANCE_GIVE_GEMS=5;
	/** 每种资源每天最大捐献次数 */
	public static final int ALLIANCE_MAX_VALUE=6;
	public static final int ALLIANCE_GIVE_TIME_VALUE_FORE[]={0,0,0,0,0,0};
	/** 默认捐献次数 */
	public static final int ALLIANCE_GIVE_TIME_VALUE[]={0,0,0,0,0,0,0};
	/** 一天的秒数 */
	public final static int DAY_SEC=60*60*24;
	/** 一小时的秒数 */
	public final static int HOUR_SEC=60*60;
	/** 一分钟的秒数 */
	public final static int MIN_SEC=60;
	/** 联盟事件默认取的条数 */
	public final static int DEFAOULT_ALLIANCE_SIZE=20;
	/** 制造车间限制的sid */
	public static int WORKSHOP_PROSID[];
	/** 需要记录物品生产日志的建筑sid */
	public static int[] PRODUCE_LOG_SIDS;
	/**制造车间限制的限制的状态**/
	public static boolean WORDSHOP_LIMIT_STATE=true;
	/** 服务器语言版本 */
	public static int SERVER_LOCALE;

	/** 服务器等级限制 */
	public static int MAX_PLAYER_LEVEL=80,// 最大玩家等级
					MAX_HONOR_LEVEL=80,// 最大声望等级
					MAX_ALLIANCE_LEVEL=80,// 最大联盟等级
					MAX_BUILD_LEVEL=80,// 最大建筑等级
					MAX_SKILL_LEVEL=80;// 最大技能等级
	/**
	 * player attributes属性常量 已用玩家名字为key value为捐献次数
	 * ALLIANCE_DEFND指定防守的协防舰队事件id
	 */
	public final static String PLAYER_GM="gm_player",
					REST_DAY_TASK="reset_task",ALLIANCE_ID="alliance_id",
					ALLIANCE_APPLICATION="alliance_application_name",
					ALLIANCE_GIVE_TIMES="alliance_give_times",
					ALLIANCE_GIVE_VALUE_PLAYER="alliance_give_value",
					ALLIANCE_JOIN_TIME="alliance_join_time",
					ALLIANCE_BOSS_FIGHT="alliance_boss_attack",
					FIGHT_PUSH_TIME="fight_push_time",
					ALLIANCE_DEFND_ATT="alliance_defend",
					LOTTO_COUNT="lotto_count",TAKE_OVER="take_over",
					ATTACK_BOSS_TIME="attack_boss_time",
					DAILY_GEM_COUNT="daily_gem_count", // 每日领取的宝石数量
					NEW_DAILY_GEM_COUNT="new_daily_gem_count", // 新每日领取的宝石数量
					FRIENDS_LIST="friends_list",// 好友列表
					BLACK_LIST="black_list", // 黑名单列表
					LIMIT_SALE_RECORD="limit_sale_record",//限时商品记录
					UPCOMMANDER_FAILURE="upcommander_failure",//升级统御失败记录（X）
					LUCKY_DRAW="lucky_draw",//限时抽奖
					LUCKY_DRAW_CLASSIC="lucky_draw_classic",//经典无动画限时抽奖
					FP_AWARD="fp_award",
					SERIES_LOGIN="series_login",//连续登陆天数	
					VARIBLE_PACKAGE="varible_package",//各种礼包（天降好礼）
					ALLIANCE_INVITATION_RECORD="alliance_invitation_record",
					DAY_TASK_UPDATE="day_task_update",//每日任务刷新时间
					BUILD_AUTO_LEVEL_UP="auto_level_up",
					END_TIME="endtime",
					AWARD_TIME="awardtime",
					VIP_POINT="vip_point",
					NEW_PLAYER_AWARD="new_player_award",//新手礼包
					DATE_VITALITY="date_vitality",//活跃度
					BASIC_LOTTO="basic_lotto",//军需乐透
					BASIC_LOTTO_FOLLOW="basic_lotto_follow",//军需引导
					ONLINE_LUCKY_AWARD="online_lucky_award",//在线神秘奖励
					EQUIP_SYS_FOLLOW="equip_sys_follow",//装备系统引导
					FORBID_CHAT="forbid_chat",//另类禁言
					ALLIANCE_TRANSFER_TIME="alliance_transfer_time",//自动移交会长的检测时间
					PLAYER_DELETE_FLAG="player_delete_flag",//玩家删除状态
					VIP_LIMIT_SALE_RECORD="vip_limit_sale_record",//vip限量奖励状态
					DATE_VITALITY_STORE="date_vitality_store",//活跃度任务存储标记,解决活跃度任务持久化问题
					PLAYER_LOGIN_TIME="player_login_time",//玩家的登录时间
					RES_TO_LONG="res_to_long",//资源转long 标记(会影响数据存储，读取)
					DEFAULT_PUSH_MARK="default_push_mark",//每日折扣和在线奖励默认打开的推送标记
					ADD_INIT_PUSH="add_init_push",//添加默认打开的推送开关标记(以","分隔,可存储多个)
					BOSS_FIGHT_ID="boss_fight_id",//打世界boss事件ID
					PLAYER_BTIME="player_btime",//打开绑定的时间
					PLAYER_URL_TIME="player_url_time",//打开跳转页面的时间
					VAR_PLAYER_INFO="var_player_info",
					ACCOUNT_INFO="account_info",//修改账号的信息 1:是否解锁2:邮件的开始时间3:验证码4:次数
					ACCOUNT_TIME="account_time",//修改账号记录的时间
					CREAT_NAME="creat_name",//玩家是否已 创建名字标记
					MEAL_TIME_PUSH="meal_time_push",//饭时活动标记
					ONLINE_TIME="online_time",//某天在线时间
					EXTRA_TROOPS_GIFT="extra_troops_gift",// 帝国援军奖励标识
					ATTACK_NIAN_TIME="attack_nian_time",//攻击年兽超时
					NEW_FOLLOW_PLAYER="new_follow_player",// 新引导新手玩家
					NEW_FOLLOW_PLAYER_HOLD="new_follow_player_hold",// 新手引导驻守事件完成标记
					COMMAND_UP_LUCKY="command_up_lucky",// 前台显示使用的统御升级幸运值
					CURRENT_NEW_FOLLOW="current_new_follow",// 存储
					LAST_FREE_LOTTO="last_free_lotto",// 最近的免费军需乐透时间
					ALLIANCE_GIVE_VALUES="alliance_give_values",
				    PLAYER_POINT_VALUE="player_point_value",//玩家积分
				    PAYMENT_DEVICES="payment_devices",// 在用的充值设备
				    OFFICER_SYSTEM_GUIDE="officer_system_guide",// 军官新手引导
				    OFFICER_FREE_DRAW="officer_free_draw",// 军官碎片抽奖首次免费
    				LUCKY_DRAW_SHIPPING="lucky_draw_shipping",//通商航运抽奖  活动id,抽奖次数,上次位置
					LUCKY_DRAW_ROB="lucky_draw_rob",//全民抢"节"活动 活动id,抽奖次数,上次位置
				    PLAYER_COMRADE_CHARGE="player_comrade_charge",//玩家招募充值记录
				    OWNED_OFFICERS="owned_officers",// 玩家拥有过的军官
				    HEAD_SID = "head_sid",//当前头像
				    HEAD_BORDER = "head_border",//当前边框
				    HEAD_INFO = "head_info",// 玩家头像信息;
					HEAD_TO_ACHIEVEMENT = "completeAchieveHead",// 已完成成就添加头像（标识更新头像功能，已完成成就是否获得头像）
					HEAD_SIGN="HEAD_1";//头像版本标识 用于新成就加入且这个新加入的成就已经完成 需要自动激活的判断条件
	/** 联盟属性 */
	public static final String ALLIANCE_CREATE_TIME="alliance_create_time",
						ALLIANCE_JOIN_LEVEL_BOOL="alliance_join_level_bool",
						ALLIANCE_JOIN_SCORE_BOOL="alliance_join_score_bool";
	
	/** 联盟等级开启技能 */
	public static String ALLIANCE_LEVEL_OPEN_SKILL[];
	/**反连击技能**/
	public static int[] ALLIANCE_COMBO_SKILL={521,522,523,524};
	/** 联盟升级经验 */
	public static int ALLIANCE_LEVEL_EXP[];
	/** 联盟限制人数 */
	public static int ALLIANCE_LEVEL_NUMS[];
	/** 联盟每种资源的消耗 */
	public static int ALLIANCE_RESOURCE_COST[];
	/** 联盟捐献奖励品sid */
	public static int ALLIANCE_AWARD_SID[];
	/** 联盟BOSS箱子领取的捐献度限制 */
	public static int ALLIANCE_GIVE_VALUE_LIMIT[];
	/** 联盟等级对应岛屿收藏数 */
	public static int ALLIANCE_ISLANDS_LOCATION[];
	/** 侦查费用 */
	public static int SCOUT_MONEYCOST[];
	/** VIP对应的每日重置任务的次数 */
	public static int RESET_TASK_NUM[];
	/** 最大可购买的建筑位数量* */
	public static int VIP_LEVEL_FOR_BUILD_DEQUE[];
	/** 升级统帅等级的成功率 */
	public static int COMMANDER_SUCCESS[];
	/** 玩家等级对应的伤兵回复率 */
	public static int PLAYER_RESET_HURT_SHIPS[];
	/** 玩家军衔相关 */
	public static int MILITARY_RANK_LEVEL[];
	/** 玩家军衔金钱 */
	public static int MILITARY_RANK_MONEY[];
	/** 玩家领取声望 */
	public static int MILITARY_RANK_HONOR[];
	/** 商店出售物品sid */
	public static int SHOP_SELL_SIDS[];
	/** 联盟商店出售物品sid */
	public static int ALLIANCE_SHOP_SELL_SIDS[];
	/**限时商品的sid*/
	public static int LIMIT_SHOP_SIDS[];
	/** 非商店可购买商品 */
	public static int OTHER_SELL_SIDS[];
	/**军官商店对应的物品sid   稀有度   概率  数量上限**/
	public static int OFFCER_SHOP_SIDS[];
	/**军官商店的数量设置   概率 数量**/
	public static int OFFCER_SHOP_NUM_LIMIT[];
	/**军官商店的类型(宝石，2级货币)   概率  类型  1是宝石购买 2  2级货币购买**/
	public static int OFFCER_SHOP_TYPE_LIMIT[];
	/** 统帅等级的额外带兵数量 */
	public static int COMMANDER_TROOPS[];
	/** 玩家等级对应的带兵数量 */
	public static int PLAYER_LEVEL_TROOPS[];
	/** 购买建筑位花费宝石 */
	public static int VIP_LEVEL_FOR_BUILD_QUEUE_BUY_COST[];
	/** vip对应的每日购买能量次数 */
	public static int VIP_LEVEL_FOR_ENERGY_BUY_TIME[];
	/** vip对应的岛屿收藏数 */
	public static int VIP_LEVEL_FOR_ISLAND_LOCATION[];
	/** 每个index对应的建筑type 存在一对多关系 冒号分开 */
	public static String INDEX_FOR_BUILD_TYPE[];
	/** 每级指挥中心对应的新开发建筑位 */
	public static String INDEX_0_LEVEL_OPEN_INDEX[];
	/** 创建联盟所需要的金币 */
	public static int ALLIANCE_CREATE_MONEY=100000;
	/** 升级统帅所需要的道具sid */
	public static int COMMANDER_LEVEL_UP_SID=2022;
	/**系数A*/
	public static int A=3;
	/** 系数B */
	public static int B=8;
	/** 提升技能需要的道具sid */
	public static int UP_SKILL_PROP_SID=2021;
	/** 清楚军官技能需要的宝石 */
	public static int CLEAR_SKILL_GEMS=28;
	/** 新手奖励的金币包sid和宝石数量 */
	public static int PROP_SID=5,GEMS=30;
	/** 重置每日任务要花费的宝石 */
	public static int RESET_TASK_GEMS=28;
	/** 每日声望发放宝石 */
	public static int HONOR_DAY_GET=5;
	/** 联盟岛屿收藏上限 */
	public static int ALLIANCE_LOCATION_MAX=40;
	/**
	 * 建筑通信类型
	 * BUILD_ADD_TYPE=1新加一个建筑，BUILD_LEVEL_UP=2建筑升级，CANCLE_BUILD_OR_LEVELUP
	 * =3取消建筑建造或升级 SHIPS_PRODUCE=4船只生产 CANLE_SHIP_OR_PROP_PRODUCE=5取消船只生产
	 * DELETE_BUILD_TYPE=6撤除建筑SHIP_STRENGTH=16舰船强化UP_SHIP=17舰船升级
	 */
	public static final int BUILD_ADD_TYPE=1,BUILD_LEVEL_UP=2,
					CANCLE_BUILD_OR_LEVELUP=3,SHIPS_OR_PROP_PRODUCE=4,
					CANLE_SHIP_OR_PROP_PRODUCE=5,DELETE_BUILD_TYPE=6,
					BUILD_ADD_FINISH=7,BUILD_LEVEL_UP_FINISH=8,
					SHIPS_OR_PROP_PRODUCE_FINISH=9,COMMAND_PRETEND=10,
					COMMAND_FIGHT=11,SCIENCE=12,GEMS_SPEED_TYPE=13,
					PRODUCE_PROPS=14,SHIP_UPGRADE_LEVEL=15,SHIP_STRENGTH=16,UP_SHIP=17,
					AUTO_LEVEL_UP=19,GET_AUTO_ARRAY=20,BUILD_UP_IMMED=21;
	/**
	 * 任务端口通信常量 REPORT_TASK_TYPE=1回报任务 EXCHANGE_AWARD_TASK=2奖励兑换任务
	 * NEW_GUIDE_TASK=3新手引导任务完成 FREE_RESET_TASK免费刷新每日任务
	 */
	public static int TASK_REPORT_TYPE=1,TASK_EXCHANGE_AWARD=2,
					TASK_NEW_GUIDE=3,TASK_DAY_RANDOM_CHOOSE=4,
					TASK_DAY_RANDOM=5,TASK_DAY_GEMS_RANDOM=6,GEMS_FINISH=7,
					GIVE_UP=8,RESET_DAY_TASK=9,FREE_RESET_TASK=10;

	/** 任务事件类型 */
	public static int BUILD_FINISH_TASK_EVENT=1,BUILD_ANY_TASK_EVENT=2,
					SHIP_PRODUCE_TASK_EVENT=3,CHAPTER_STARTS_TASK_EVENT=4,
					PLAYER_LEVEL_ISLAND_EVENT=5,RANK_HONOR_TASK_EVENT=6,
					SCIENCE_LEVEL_UP_EVENT=7,POINT_SUCCESS_TASK_EVENT=8,
					ATTACK_TASK_EVENT=9,GEMS_ADD_SOMETHING=10,TEARPOINT_SUCCESS_TASK_EVENT=11,
					ATTACK_POINT_TASK_EVENT=12,TASK_DAY_TASK_EVENT=13,HONOR_UP_TASK_EVENT=14,
					ATTACK_NPCISLAND_TASK_EVENT=15,ATTACK_BOSS_TASK_EVENT=16,
					ATTACK_PLAYER_TASK_EVENT=17,BUY_ENERGY_TASK_EVENT=18,
					ATTACK_ARENA_TASK_EVENT=19,ALLIANCE_GIVE_TASK_EVENT=20,
					ATTACK_ALLIANCE_TASK_EVENT=21,GET_ALLIANCE_TASK_EVENT=22,
					SKILL_UP_TASK_EVENT=23,COMMAND_UP_TASK_EVENT=24,BIND_ACCOUNT_EVENT=25,
					DAY_TASK_COUNT_EVENT=26,PRODUCE_COUNT_TASK_EVENT=27,ARMS_POINT_EVENT=28,
					TEAR_POINT_EVENT=29,HCITY_POINT_EVENT=30,STAR_STONE_LEVEL_EVENT=31,
					ELITE_POINT_EVENT=32;

	/** 物品使用端口通信类型USE_PROP=1使用物品，BUY_PROP=2购买物品 */
	public static int USE_PROP=1,BUY_PROP=2,BUY_PROP_AND_USE=3,CODE_PROP=4,DATE_OFF_PROP=5,VIP_LIMIT_SALE=6,
					BUY_ALLIANCE_PROP=7,PHYSICAL_REWARDS=8;

	/**CODE_TYPE=1 兑换码标识  COMMARDE_TYPE=2 招募标识 **/
	public static int CODE_TYPE=1,COMMARDE_TYPE=2;
	
	/**
	 * 玩家属性端口类型常量PLAYER_TYPE_UP=1玩家军衔等级提升，COMMAND_LEVEL_UP=2玩家统帅等级提升
	 * BUY_DEQUEN=5购买建筑位 GET_INVETD_AWARD=12登录 DETELT_PLAYER=51删除玩家
	 * MODIFYNAME=52修改名称 BINDING_ACCOUNT=53 解锁绑定账号 SENDING_EMAIL=54 发送邮箱
	 * COM_CODE=55  验证验证码 EXTRA_GIFT_AWARD=56 领取帝国援军 LOCK_ACCOUNT=57 去除解锁账号
	 * SET_COMMAND_LUCKY=58 设置前台统御幸运值,NEW_TASK_BG=59 新手任务跳出步骤,FREE_LOTTO=60 免费军需,
	 * MEALTIME_ENERGY=61 领取能量,GROWTH_PLAN=62,成长计划GET_PRIVATE_GROWTH=63,获取个人成长信息
	 * GET_SERVER_GROWTH=64 获取个全服成长信息,INC_PROSPERTITY=68 恢复繁荣度,SET_POINT_BUFF=69设置关卡buff,PLAYER_CHANGE_HEAD=71修改头像
	 * ,PLAYER_CHANGE_BORDER=70 修改头像边框 GET_INTIMACY_LUCKY=71 亲密度抽奖,GIVE_INTIMACY=72赠送亲密度,RECEVIED_INTIMACY=73领取亲密度
	 */
	public static int PLAYER_TYPE_UP=1,COMMAND_LEVEL_UP=2,HONOR_GET_DAY=3,
					BUY_ACTIVES_DAY=4,BUY_DEQUEN=5,BUY_AND_USE_COMMAND=6,
					DEVICE_TOKEN=7,SAVE_ISLAND_LOCATION=8,
					CHANGE_ISLAND_LOCATION=9,NEW_PLAYER_TASK=10,
					BUY_PRODUCE_DEQUEN=11,GET_INVETD_AWARD=12,
					BE_GET_INVETD_GEMS=13,PRODUCE_URL=14,SERVER_VIERSION=15,
					JUMP_NEW_TASK=16,GET_PLAYER_INFO=17,CLEAR_SKILLS=18,
					LOTTO_STATE=19,LOW_LOTTO=20,HIGH_LOTTO=21,
					ADD_APPLY_FRIEND=22,REMOVE_FRIEND=23,
					ADD_BLACK=24,REMOVE_BLACK=25,ALLIANCE_SAVE_LOCATION=26,
					ALLIANCE_CHANGE_LOCATION=27,FIRST_PAY_AWARD=28,
					LUCKY_AWARD=29,ALLIANCE_INVITATION=30,
					ALLIANCE_INVITATION_ACCEPT=31,
					ALLIANCE_INVITATION_REFUSE=32,CHANGE_PLAYER=33,
					PLAYER_CARDAWARD=35,VITALITY_AWARD=36,
					GET_LUCKY_AWARD=37,SET_LOTTO_FOLLOW=38,
					RESET_VITALITY=39,QUALITY_STUFF=40,SALE_EQUIP=41,
					WARE_EQUIP=42,ENLARGE_EQUIPLIST=43,EQUIP_LEVEL_UP=44,
					EQUIP_QUALITY_UP=45,SET_EQUIP_FOLLOW=46,OFF_EQUIP=47,
					ONLINE_LUCKY=48,LUCKY_AWARD_CLASSIC=49,
					GET_ONLINE_LUCKY=50,DELETE_PLAYER=51,MODIFYNAME=52,
					BINDING_ACCOUNT=53,SENDING_EMAIL=54,COM_CODE=55,
					EXTRA_GIFT_AWARD=56,LOCK_ACCOUNT=57,
					SET_COMMAND_LUCKY=58,NEW_TASK_BG=59,FREE_LOTTO=60,
					MEALTIME_ENERGY=61,BUY_GROWTH_PLAN=62,
					GET_PRIVATE_GROWTH=63,GET_SERVER_GROWTH=64,
					INC_PROSPERTITY=68,SET_POINT_BUFF=69,
					PLAYER_CHANGE_HEAD=71,PLAYER_CHANGE_BORDER=70,
					GET_INTIMACY_LUCKY=72,GIVE_INTIMACY=73,
					RECEVIED_INTIMACY=74,ADD_FRIEND=75,REFRESH_BUILDING=1000;

	/** 战斗端口常量 */
	public static int SET_MAIN_GROUP=1,FIGHT_CHECK_POINT=2,WORLD_FIGHT=3,
					WORLD_FIGHT_VIEW=4,VIEW_ISLAND_INFO=5,REPARI_SHIPS=6,
					GET_FIGHT_EVENT=7,EVENT_PUSH=8,SHIP_RRETURN_BACK=9,
					ALLIANCE_DEFEND=10,ALLIANCE_DEFEND_BACK=11,
					CHOOSE_FIGHT_EVENT=12,CANCEL_EVENT=13,FIGHT_TEAR_POINT=14,
					CLEAR_TEAR_POINT=15,GET_TEAR_POINT=16,GET_COMBINDED_POINT=17,
					 GET_RANDOM=18,GET_TIMENOW=19,SET_FLEET=20,FIGHT_ARMS_ROUTE=21,
					 CLEAR_ARMS_POINT=22,SWEEP_POINT=23,CHECK_SWEEP=24,SET_FORMATION=25,
					GET_FORMATION=26,CHECK_POINT_CHEST=27,FIGHT_ELITE_POINT=28,
					SWEEP_ELITE_POINT=29,COMBINED_POINT_CHEST=30,CHECK_GEMS_BUFF=31;
	
	/** 抽奖类型 */
	public static final int LOTTO_FREE=1,LOTTO_ADVANCE=2,LOTTO_LUXURY=3;
	/** 抽奖最大次数 */
	public static int LOTTO_MAX_1=5,LOTTO_MAX_2=5;
	/** 抽奖消耗的宝石数 */
	public static int LOTTO_NEED_GEM_1=0,LOTTO_NEED_GEM_2=10,
					LOTTO_NEED_GEM_3=50;
	/** 免费抽奖强制间隔时间(秒) */
	public static int LOTTO_FREE_CIRCLE;

	/** 好友数上限和黑名单上限 */
	public static final int MAX_FRIEND_LIST_COUNT=20,
					MAX_BLACK_LIST_COUNT=30;

	/** 公用常量BUILD_TYPE=1000建筑事件 */
	public static int EVENT_BUILD_TYPE=1000;

	/** 前台端口常量 */
	public static final int MESSAGE_PORT=2002;

	/** 宝石加速消耗 1分钟1个 */
	public static final int GEMS_SPEED=60,BUILD_SPEED_UP=1,
					PRODUCE_SPEED_UP=2,FIGHT_MOVE_UP=3;
	/** 宝石每单位速度消耗的数量 */
	public static int GEMS_PER_UNIT_SPEED=1;

	/** 每日任务的刷新需要宝石 */
	public static final int TASK_DAY_NEED_GEMS=8;

	/**
	 * ATTACK=100攻击,DEFENCE=101防御,ACCURATE=102精准,AVOID=103回避,SHIP_NUM=104舰船数量
	 * ,HP=105生命,CRITICAL_HIT=106暴击, CRITICAL_HIT_RESIST=107暴击抵抗,
	 * EQUIP_ATTACK=120从装备获取的加成
	 * EXTRA_SHIP=200 额外带兵量,EXTRA_SPEED=201 额外航速,EXTRA_CARRY=202 额外载重
	 */
	public static final int ATTACK=100,DEFENCE=101,ACCURATE=102,AVOID=103,
					SHIP_NUM=104,FLEET_HP=105,CRITICAL_HIT=106,
					CRITICAL_HIT_RESIST=107,SHIP_HP=108,COUNTER_COMBO_HIT=109,
					EQUIP_ATTACK=120,EQUIP_DEFENCE=121,
					EQUIP_ACCURATE=122,EQUIP_AVOID=123,
					EQUIP_CRITICAL=126,EQUIP_CRITICAL_RESIST=127,EQUIP_HP=128,
					EXTRA_SHIP=200,EXTRA_SPEED=201,EXTRA_CARRY=202;

	/**
	 * 某种类型的service ADD_RESOURCE_BUFF=1资源增加 HEAVE_BUFF=13载重增加
	 * STORE_ADD_BUFF仓库容量增加BUFF 也适用于科技
	 * FORE_METAL_BUFF铁增产 FORE_OIL_BUFF石油增产 FORE_SILICON_BUFF硅增产
	 * FORE_URANIUM_BUFF铀增产 FORE_MONEY_BUFF金币增产（FORE设置结束时间前 持续）
	 */
	public static final int ADD_METAL_BUFF=201,ADD_OIL_BUFF=202,
					ADD_SILICON_BUFF=203,ADD_URANIUM_BUFF=204,
					ADD_MONEY_BUFF=205,REDUCE_HURT_BUFF=206,
					ADD_HURT_BUFF=207,ADD_SPREED_BUFF=208,
					NOT_FIGHT_BUFF=209,EXP_ADD=210,MONEY_BUFF=211,
					BUILD_BUFF=212,HEAVE_BUFF=213,STORE_ADD_BUFF=214,
					ADD_ACCURATE_BUFF=215,ADD_AVOID_BUFF=216,
					ADD_CRITICAL_BUFF=217,ADD_CRITICAL_RESIST=218,
					FORE_METAL_BUFF=219,FORE_OIL_BUFF=220,FORE_SILICON_BUFF=221,
					FORE_URANIUM_BUFF=222,FORE_MONEY_BUFF=223,AUTO_BUILD_BUFF=224;
	/**
	 * 船类型 SHIPS_1战列舰 SHIPS_2潜艇 SHIPS_3巡洋舰 SHIPS_4航母 SHIPS_5=空军基地
	 * SHIPS_6=导弹基地 SHIPS_7=火炮阵地 TRANSPORT_SHIP=128 运输船
	 */
	public static final int BATTLE_SHIP=1,SUBMARINE_SHIP=2,CRUISER_SHIP=4,
					AIRCRAFT_SHIP=8,POSITION_AIR=16,POSITION_MISSILE=32,
					POSITION_FIRE=64,TRANSPORT_SHIP=128;
	/** AIR_RAID空袭,ARTILLERY炮火,MISSILE导弹,TORPEDO鱼雷,NUCLEAR核 */
	public static final int AIR_RAID=0,ARTILLERY=1,MISSILE=2,TORPEDO=3,
					NUCLEAR=4;
	/** 装备提供的额外抗性与加成 */
	public static final int RESIST_AIR_RAID=2000,RESIST_ARTILLERY=2001,RESIST_MISSILE=2002,
					RESIST_TORPEDO=2003,RESIST_NUCLEAR=2004,
					ATTACH_BASE=2005,ATTACH_BATTLE=2006,ATTACH_SUBMARINE=2007,
					ATTACH_CRUISER=2009,ATTACH_AIRCRAFT=2013;
	/** 前台通知后台推送任务 */
	public static final int FORE_TASK_SEND=1;

	/** 火炮，空军基地，导弹的名字 */
	public static final String POSITION_FIRE_NAME="",
					POSITION_MISSILE_NAME="",POSITION_AIR_NAME="";

	/** 火炮，空军基地，导弹，兵力sid 10041，10061，10051，和index 6,7,8 */
	public static final int POSITION_FIRE_SID=10041,
					POSITION_MISSILE_SID=10051,POSITION_AIR_SID=10061,
					POSITION_FIRE_INDEX=6,POSITION_AIR_INDEX=7,
					POSITION_MISSILE_INDEX=8;

	/** 系统设置 AUTO_HOLD默认驻守 */
	public static final int AUTO_ADD_MAINGROUP=0,ISLAND_BE_ATTACK=1,
					BUILD_FINISHED=2,ENERY_PUSH_IS_FULL=3,AUTO_HOLD=5,
					DATE_OFF_PUSH=6,ONLINE_AWARD_PUSH=7,PEACE_TIME_PUSH=8,
					MEAL_TIME_ENERGY_PUSH=9,STATIONED_PUSH=10;

	/** 充值宝石数量对应的VIP等级 */
	public static int GEMS_FOR_VIP_LEVEL[];
	/** 对应vip等级开发的等待队列 */
	public static int VIP_LEVEL_FOR_DEQUE[];
	/** vip等级对应的同时出兵数量 */
	public static int VIP_LEVEL_FOR_BATTLE_DEQUE[];
	/** 语言环境常量 */
	public final static int kLanguageEnglish=0,kLanguageChinese=1,
					kLanguageFrench=2,kLanguageItalian=3,kLanguageGerman=4,
					kLanguageSpanish=5,kLanguageRussian=6,kLanguageKorean=7,
					kLanguageChineseHant=8,kLanguageJapness=9,kLanguageArab=12,kLanguageThailand=13,
					kLanguageVietnam=14;
	/** 战斗常量 */
	public final static int FIGHT_TYPE_1=1,// 我们的舰队攻击了%s(硅矿岛，野地岛屿名字)
					FIGHT_TYPE_2=2,// 我们的舰队攻击了%s(玩家名字)
					FIGHT_TYPE_3=3,// 我们的基地被%s(玩家名字)攻击了
					FIGHT_TYPE_4=4,// 我们的%s(硅矿岛，野地岛屿名字)被%s(玩家名字)攻击了
					FIGHT_TYPE_5=5,// 攻击了玩家驻守的野地
					FIGHT_TYPE_6=6,// 自己的队伍 返航
					FIGHT_TYPE_7=7,// 有免战BUFF
					FIGHT_TYPE_8=8,FIGHT_TYPE_9=9,FIGHT_TYPE_10=10,// 联盟战斗
					FIGHT_TYPE_11=11,FIGHT_TYPE_12=12,FIGHT_TYPE_13=13,// FIGHT_TYPE_13
					FIGHT_TYPE_14=14,// 攻击空岛
					FIGHT_TYPE_15=15,//联盟战
					FIGHT_TYPE_16=16,//
					FIGHT_TYPE_17=17,//年兽
					FIGHT_TYPE_18=18,//新联盟战(玩家战斗) 挑战方
					FIGHT_TYPE_19=19,//新联盟战(玩家战斗) 被挑战方
					FIGHT_TYPE_20=20;//跨服积分赛

	/** 岛屿状态 */
	public final static int NOT_FIGHT_STATE=1;

	/** 竞技场每日奖励 */
	public static String[] ARENA_DAILY_AWARDS;
	/** 竞技场挑战奖励 */
	public static int[] ARENA_BATTLE_AWARDS;
	/** 竞技场名次 */
	public static int[] ARENA_BATTLE_RANKS={1,5,10,20,30,50,100,200,500,
		1000,2000,5000,10000,20000,50000,99999999};
	/** 统御升级失败归还游戏币 5000 */
	public static int BACK_MONEY=5000;
	/** 统御书-带兵数*/
	public static int COMMANDER_NUM=10;
	/** 神统开放军衔 上尉 */
	public static int COMMANDER_OPEN_LEVEL=8;
	/** 神统 统御书-军衔*/
	public static int[] CLEVEL_CNUM={1,2,3,4,5,6,7,8};
	
	/** 撕裂虚空精力消耗 */
	public static int TEAR_ENERGY=5;
	/** 撕裂虚空付费重置次数上限  */
	public static int TEAR_PAYCOUNT_MAX=1;
	/** 撕裂虚空 重置消耗宝石 */
	public static int TEAR_CLEAR_GEMS=5;
	/** 撕裂虚空初始关卡（若连续，则所有起始SID相同，若不连续则每章配置自己的起始SID）*/
	public static int[] TEAR_CHECK_SID={14001};
	 /**联合舰队初始关卡*/
	public static int HERITAGECITY_CHECK_SID=15001;
	/**军备航线的初始关卡**/
	public static int ARMS_CHECK_SID=14051;
	/** 星石研究院 依赖 指挥中心等级 */
	public static int STAR_STONE_CENTER_LVL=30;
	/** 登录奖励 */
	public static int[] DAYAWARD;
	/** 限时抽奖奖励 */
	public static int[] LUCKYAWARD;
	/** 首冲礼包awardSid */
	public static int F_PAY_AWARD[];
	/** 盟战带兵量-盟等级*/
	public static int[] AFIGHT_SHIP;
	
	/** 关卡BUFF加成类型-关卡sid*/
	public static int[] SIDS={1,3,5,7,9,11,13};
	/** 关卡BUFF加成类型-显示sid关系  */
	public static int[] SHOW_SIDS={401,402,403,404,405,406,407};
	/** 军事乐透抽奖提示(因为可以手动修改,请使用SeaBackKit.getLuckySids方法进行兼容) */
	public static int[] LUCKY_SIDS;
	/** VIP限量购买奖励 */
	public static int[] VIP_LIMIT_AWARD;
	/** 每日折扣随机商品 */
	public static String[] DATE_PRICE_OFF;
	/** 每日折扣商店开启时间 */
	public static float[] DATE_PRICE_OPEN;
	/** 帝国援军奖励、时间 */
	public static int[] EXTRA_GIFT_AWARDS;
	/** 使用时获得[LUCKY_SIDS]奖励品需要提示的物品 */
	public static int[] PROP_USE_TIPS;
	public static int MOUTHCARDDAYS=29;
	public static int MOUTHCARDAWARD=50;
	public static int VIPPOINT_NUM=100;//月卡增加vip成长点数
	/** 僵尸用户定义 */
	public static int DEAD_LEVEL=2,DEAD_DAY=2;
	/** 每日任务刷新CD （hour）*/
	public static int DAY_TASK_UPDATE_CD=4;
	public static int PLAYER_CODE_EXCHANGE=7;
	public static int CODE_EXCHANGE_ERRO=0;
	/** 重置虚空实验室消耗宝石的系数 */
	public static float RESET_VOID_LAB;
	public final static int CODEID_IS_ERRO=0;
	public final static int CODEID_IS_USERED=-1;
	public final static int CODEID_IS_LIMIT=-2;
	public final static int PROP_NUM=1;
	public final static int DELETE_STATE=2,DELETE_USER_STATE=0;
	public final static int RECOVER_PLAYER_STATE=0;
	public final static int LOAD_ALL_GEM_COST=-1;
	public final static int AWARD_LENGTH=3;//神秘抽奖奖励包的分配的平均长度
	public final static int AWARD_TOTAL_LENGTH=10000;//神秘抽奖 概率
	public final static int AWARD_TOTAL_ALLLENGTH=14;//幸运轮盘的长度为14
	/**后台设置绑定账号的状态**/
	public static int GAME_BINDING=0;
	/**绑定的原因**/
	public static String	BINDING_REASON=" ";
	/**绑定开启的时间**/
	public static int START_BINDING_TIME=0;
	/**后台设置跳转页面的状态**/
	public static int JUMP_ADDRESS=0;
	/**要设置跳转的平台**/
	public static String BINDING_PLATID="";
	/**跳转的地址**/
	public static String[] URL_ADDRESS;
	/**跳转页面的原因**/
	public static String	JUMP_REASON="";
	public static int JUMP_LEVEL=0;//等级限制
	public static int JUMP_TIME=0;//跳转开启的时间
//	public static String BINDING_ADDRESS="";//网址

	/**账号绑定标识 0:关闭 1:建议绑定 2：强制绑定  GM工具强制绑定**/
	public final static int COMMON_BINDING=0;
	public final static int ADVISE_BINDING=1;
	public final static int FORCE_BINDING=2;
	public final static int GM_FORCE_BINGD=3;
	/**每次扫荡的消耗的宝石数量**/
	public final static int ARMS_SWEEP_COST_GEMS=1;
	/**每次扫荡的消耗的宝石数量**/
	public final static int ELITE_SWEEP_COST_GEMS=1;
	/**每次更换账号的宝石数量**/
	public static int MODIFY_A_COST_GEMS;
	public static  String SHIELD_WORD;
	/**屏蔽玩家名称**/
	public static String NONEPLAYER_NAME;
	/** 聊天消息广播按语言分组 */
	public static boolean CHAT_GROUP_LOCALE=false;
	/** 配件战力系数 */
	public static float EQUIP_RATIO;
	/** 开放阵型收藏对应的vip等级  */
	public static int[] FORMATION_VIP;
	/**每次捐献所获取的  宝石 wuzi  积分    在配置的时候 免费的也需要配置**/
	public static int ALLIANCE_VALUES[]={0,5,10,10,5,10,20,5,10,30,5,10,40,5,10,50,5,10};
	/**每次联盟捐献物资的时候  如果是免费的话 每次增加联盟科技点**/
	public static int ADD_SCIENCEPOINT=10;
	/** 军官系统开放的最高级数  */
	public static int OFFICER_MAX_LV;
	/** 军官军衔对应最高级数  */
	public static int[] OFFICER_RANK_LV;
	/** 碎片抽奖强制间隔时间 */
	public static int FREE_DRAW_LIMIT_TIME;
	/** 军官碎片低级抽奖免费次数 */
	public static int OFFICER_FRAG_LOW_FREE;
	/** 军官碎片低级抽奖消耗道具 */
	public static int[] OFFICER_FRAG_LOW_PROP;
	/** 军官碎片低级抽奖概率,稀有值位置对应的概率 */
	public static String[] OFFICER_FRAG_LOW;
	/** 军官碎片高级抽奖消耗宝石 */
	public static int[] OFFICER_FRAG_HIGH_GEMS;
	/** 军官碎片高级抽奖概率,稀有值位置对应的概率 */
	public static String[] OFFICER_FRAG_HIGH;
	/** 军官碎片高级抽奖保底碎片组 */
	public static int[] OFFICER_FRAG_HIGH_MIN;;
	/** 军官碎片引导抽奖产出 */
	public static int[] OFFICER_FRAG_GUIDE_DRAW;
	/** 图书馆阅读时间配置{功勋,玩家限制级数,冷却时间,前台sid,...} */
	public static int[] OFFICER_LIB_READ;
	/** 制作书籍百分比配置{percent,metal,oil,silicon,uranium,money,gems...} */
	public static int[] OFFICER_LIB_WRITE;
	/** 军官影响技能概率——类型对应的技能sid{连击,燃烧弹,...} */
	public static String[] SKILL_TYPE_SIDS;
	/** 军官经验池上限(军官中心获取值限制) */
	public static long[] OFFICER_FEATS_MAX;
	/** 玩家战斗胜利时增加的军官功勋 */
	public static int PLAYER_FIGHT_SUCCESS_FEATS;
	/** 玩家战斗失败时增加的军官功勋 */
	public static int PLAYER_FIGHT_FAIL_FEATS;
	/** 战报演示数据版本(程序控制,战报解析有变动时须与前台对应修改)  */
	public static int FIGHT_RECORD_VERSION=3;
	
	/** 兼容手动设置抽奖提示临时变量 */
	public static int[] manualLuckySids;

	/** 验证器 弹出条件 行为X次后触发 */
	public static int VERTIFY_TRIGGER_COUNT=50;
	/** 验证器 条件重置时间 (秒) */
	public static long VERTIFY_TRIGGER_INTERVAL=60*60;
	/** 验证器 验证时间(秒) */
	public static int VERTIFY_TIME=30;
	/** 验证器 状态 0--关闭，1--开启，2--查询 */
	public static int VERTIFY_STATUS=1;
	/** 验证器 回答最大次数 */
	public static int VERTIFY_MAX_COUNT = 5;
	
	/** 联盟宝箱奖励 */
	public static int[] ALLIANCE_CHEST_AWARD;
	/** 联盟积分奖励 */
	public static int[] ALLIANCE_LUCKY_POINT_AWARD;
	/** 联盟积分奖励对应名次 */
	public static String[] ALLIANCE_LUCKY_POINT_PLACING;
	/** 联盟宝箱最大抽奖次数 */
	public final static int MAX_COUNT=7;
	/** 联盟宝箱宝箱升级几率 */
	public static float ALLIANCE_CHEST_UPGRADE_ODDS;
	/** 联盟宝箱幸运积分几率 */
	public static float ALLIANCE_CHEST_LUCKY_POINT_ODDS;
	/** 联盟宝箱次数与贡献值基数 */
	public static float ALLIANCE_CHEST_COUNT_BASE=5.0f;
	/** 联盟宝箱次数与贡献值系数 */
	public static float ALLIANCE_CHEST_COUNT_COEF=20.0f;
	/** 联盟宝箱积分sid */
	public static int ALLIANCE_CHEST_SID=3093;
	/** 实物奖励物品sid */
	public static int[] PHYSICAL_PROPS;
	/** 成长计划个人宝石数 */
	public static int GROWTH_PLAN_COST;
	/** 成长计划个人奖励{军衔,awardSid} */
	public static int[] GROWTH_PLAN_PRIVATE;
	/** 成长计划全服奖励{参与人数,awardSid} */
	public static int[] GROWTH_PLAN_SERVER;
	
	/** 全民抢"节"活动 奖品种类几率 分别为3种 2种 */
	public static float[] ROB_ACTIVITY_AWARD_ODD={0.05f,0.1f};
	/**j精英战场的跳转次数**/
	public static int FIGHT_ATTACK_LENGTH;
	/**遗迹都是的章节**/
	public static int [] COMBINED_CHAPTER;
	/**遗迹都是的关卡宝箱**/
	public static int[] COMBINED_AWARD;
	/**运输船的sids**/
	public static int [] TRANSPORT_SHIP_SIDS={10071,10072,10073,10074,10075};
	/**宝石岛屿对应的玩家等级和岛屿等级     level -type  从小到大**/
	public static int[] GEMISLAND_LEVEL_TYPE;
	/**宝石岛屿sids  type-sid**/
	public static int[] GEMISLAND_SIDS;
	/**每隔10分钟获取宝石数量**/
	public static int LOWLIMIT_GEMS_TIMES;
	/**从某刻时间-到某刻时间 必须长度为2(以小时为单位)**/
	public static int [] GEMS_ISLAND_LIMIT_TIME;
	/**几个小时换一次buff**/
	public static int ISLAND_BUFF_CHANGE;
	/**gemIsland技能信息 每隔n个小时换buff    **/
	public static int []GEMS_ISLAND_BUFF;
	/**宝石岛每天的活跃人数**/
	public static int CONTROL_ONLINE;
	/**原始宝石岛屿的数量**/
	public static int ORIGINAL_NUM;
	/**是否需要增加宝石岛屿**/
	public static boolean GEMS_ISLAND_CLOSE=true;
	/** 头像sid */
	public static int[] HEADICON ={50001,50002,50003,50004,50005,50006,50007,50008,50009,50010,50011};
	// 初始头像
	public static int HEADSID_BOY = 50001;
	public static int HEADSID_GIRL = 50002;
	/**头像边框*/
	public static int[] HEADBORDER = {51001,0,51002,1,51003,3,51004,5,51005,7,51006,8,51007,9};
	/**默认边框*/
	public static int DEF_HEADBORDER = 51001;
	/** 旗帜 **/
	public static int[] IMAGE;
	/** 图案 **/
	public static int[] COLOUR;
	/** 造型 **/
	public static int[] MODEL;
	/** 跨服积分赛排名奖励(参加竞技场奖励) **/
	public static String[] CROSS_LEAGUE_RANK_AWARD;
	
}