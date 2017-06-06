package foxu.sea.gm;

/**
 * 错误定义
 * 
 * @author comeback
 * 
 */
public interface GMConstant
{

	public static final int ERR_SUCCESS=0,
					ERR_PRIVILEGE_ERROR=1,
					ERR_PRIVILEGE_RANGE_ERROR=2,
					ERR_ACCOUNT_EXISTS=3,
					ERR_COMMAND_NOT_EXISTS=4,
					ERR_NOT_DO=5,
					ERR_PASSWORD_ERROR=7,
					ERR_NEW_PASSWORD_ERROR=8,
					ERR_IS_GUEST_ACCOUNT=9,
					ERR_ACCOUNT_NOT_EXISTS=10,
					ERR_GAME_CENTER_COMUNICATION_ERROR=11,
					ERR_PARAMATER_ERROR=12,
					ERR_TOO_MANY_PLAYERS=13,
					ERR_ACCOUNT_FULL=14,
					ERR_VLAUE_ERROR=15,// 输入数量有误
					ERR_VIPLEVEL_LIMIT=16,
					ERR_PRO_IS_NULL=17,// 输入的打折商品为空
					ERR_PRO_IS_ERRO=18,// 输入的打折商品有误
					ERR_PRO_NUM_IS_ERRO=19,// 输入的打折商品数量有误
					ERR_TITLE_NULL=20,// 标题为空
					ERR_CONTENT_NULL=21,// 内容为空
					ERR_BTNNAME_NULL=22,// 按钮名称
					ERR_TIME_ERRO=23,// 时间错误
					ERR_AWARD_ERRO=24,// 奖励错误
					ERR_INTRODUCTION_ERRO=25,// 简介错误
					ERR_ANNOUNCE_IS_NULL=26,// 公告为空
					ERR_DAY_SID_IS_ERRO=27,// 每日折扣商品的sid错误
					ERR_DAY_PRO_ABOUT_LENGTH=28,// 输入的每日折扣活动的商品数量大于上限
					ERR_TITLE_LENGTH_ERRO=29,//标题长度不对
					ERR_CONTENT_LENGTH_ERRO=30,//内容长度不对
					ERR_INTODUCTION_LENGTH_ERRO=31,//简介长度不对
					ERR_BTNNAME_LENGTH_ERRO=32,//按钮长度不对
					ERR_PROINFO_IS_NULL=33,//物品为空
					ERR_PRO_ERRO_LENGTH=34,//物品的长度不对
					ERR_PRO_IS_NOT_PRO=35,//配置的物品中有的物品sid不对
					ERR_PRO_IS_SPACE=36,//在物品配置中有空格
					ERR_CONTENET_IS_NULL=37,//输入的玩家名称或者是联盟名称为空
					ERR_PLAYER_NO_ALLIANCE=38,//该玩家没有联盟
					ERR_ADDRESS_IS_ERROR=39,//输入地址不对
					ERR_WORLD_ADDRESS_ERROR=40,//当前地点不是世界boss
					ERR_WORLD_HAVE_BLOOD =41,//当前地点不是世界boss
					ERR_AWARD_SHIPS_ERRO=42,//幸运抽奖中的船只配置不对
					ERR_AWARD_SHIPS_START=43,//幸运抽奖中的星石头配置不对
					ERR_AWARD_SHIPS_PRO=44,//幸运抽奖中的物品头配置不对
					ERR_AWARD_SHIPS_EQU=45,//幸运抽奖中的装备头配置不对
					// 玩家
					ERR_PLAYER_NAME_NULL=201,
					ERR_PLAYER_NOT_EXISTS=202,
					ERR_PLAYER_ALREADY_LINKED=203,
					ERR_ACCOUNT_NULL=204,
					ERR_USER_NOT_EXISTS=205,
					ERR_ACCOUNT_ERROR=206,
					ERR_ACCOUNT_ALREADY_LINKED=207,
					ERR_PWD_RECORD_NOT_EXISTS=208,
					ERR_COVER_PLAYERNAME=209,//错误的修复玩家名称
					ERR_MODIFAY_LENGHT=210,//要修改的玩家长度不一致
					ERR_PLAYER_IS_EXSITS=211,//要修改玩家的名称已经存在
					// 联盟
					ERR_ALLIANCE_NOT_EXISTS=401,
					ERR_ALLIANCE_SKILL_NOT_EXISTS=402,
					ERR_ALLIANCE_LEVEL_ERROR=403,
					ERR_ALLIANCE_EXISTS=404,
					ERR_ALLIANCE_NAME_IS_NULL=405,
					ERR_ALLIANCE_LENGTH_WRONG=406,//联盟名称长度
					ERR_ALLIANCE_IS_UNVALID=407,//联盟名称不可用
					ERR_ALLIANCE_NAME_USED=408,//名称已经拥有
					ERR_ALLIANCE_EVENT_NULL=409,//联盟事件为空
					ERR_ALLIANCEFIGHT_IS_NULL=410,//联盟战为空
					ERR_ALLIANCE_FIGHT_IS_OVER=411,//在竞标或者新联盟战已经开启无法解散联盟
					ERR_JOIN_ALLAINCE_FIGHT=412,//玩家已经参与了联盟战
					ERR_BATTLE_FIGHT_IS_NULL=415,//所有的岛屿不存在联盟战
					
					// 建筑、船只、科技和物品
					ERR_SHIP_IS_NULL=501,ERR_PROP_IS_NULL=502,
					ERR_BUNDLE_IS_FULL=503,ERR_BUILD_IS_NULL=504,
					ERR_BUILD_LEVEL_LIMIT=505,ERR_DIRECTOR_LEVEL_ERROR=506,
					ERR_BUILD_IS_LEVELING=507,ERR_BUILD_FAILED=508,
					ERR_BUILD_INDEX_ERROR=509,ERR_SCIENCE_IS_LEVELING=510,
					TSTARTTIME=511,ERR_CODE_CONTENT_NULL=515,
					ERR_PLATFORM_NULL=516,ERR_CODE_NUM_NULL=517,
					ERR_CODE_GOAL_NULL=518,ERR_CODE_TIMES_NULL=519,
					ERR_CARD_DAYS_NULL=520,
					ERR_ANNOUNCE_NULL=521,
					ERR_BUFF_IS_NULL=522,//BUFF为空
					ERR_BUILD_IS_LEVEL_ERRO=523,//指挥中心等级不足
					ERR_LEVELABILITY_NULL=524,//ability为空
					ERR_PLAYER_NOT_UP=525,//当前玩家不能升级
					ERR_SHIP_LEVEL_ERRO=526,//当前船舰的等级高于玩家的等级
					ERR_ADDRESS_NULL=527,//当前的网页不能为空
					ERR_LEVEL_PLAYER_ERRO=528,//当前的等级设置错误
					// 定时时间为错的
					TSTARTTIME_ERRO=512,CHECK_POINT_NULL=513,
					CHECK_POINT_ERRO=514,

					// 活动
					ERR_ACTIVITY_NOT_EXISTS=601,ERR_ACTIVITY_MAX_PAGE=602,
					ERR_ACTIVITY_MIN_PAGE=603,
					ERR_ACTIVITY_AWARD_IS_NULL=604,//奖励包为空
					ERR_ACTIVITY_AWARD_LENGTH_ERRO=605,//奖励包的长度不对
					ERR_ACTIVITY_AWARD_PROBABILITY_ERRO=606,//奖励包的概率不对
					ERR_ACTIVITY_PRO_LENGTH_ERRO=607,//输入的每日折扣物品的长度不对
					ERR_SCORE_REASON_IS_NULL=608,//评分活动的原因为空
					ERR_SCORE_URL_IS_NULL=609,//URL地址不能为空
					ERR_SCORE_ID_IS_NULL=610,//id不能为空
					ERR_BINDING_OPNE=611,//请先关闭绑定，在开启跳转
					ERR_JUMP_ADDRESS_OPEN=612,//请先关闭跳转，然后在开启绑定
					ERR_BINDING_OPEN=613,//当前平台下的绑定状态已经开启了
					
					ERR_UNKNOWN=-1,
					ERR_DELETE_ERRO=701,//删除失败
					ERR_RECOVER_ERRO=702,//修复失败
					ERR_PLAYER_IS_MASTER=703,//是会长不能删
					ERR_REASON_IS_NULL=704,//建议的原因为空
	
					ERR_NAME_LENGTH=801,//角色姓名长度超过限制
					ERR_NAME_UNVALID=802,//当前这个名称不可用
					ERR_NAME_BEEN_USED=803,//名称被使用
					ERR_OFFCERSCARCITY=804,//稀有度不存在
					ERR_OFFCER_LIMIT_LENGTH=805;//数量限制不对
	// 表格的标题
	public static final String
	// 包裹信息
					SID="sid",
					NAME="name",
					COUNT="count",
					// 运营数据
					PLAT="plat",
					DATE="date",
					NEW_USER="new_user",
					NEW_UDID="new_udid",
					DAU="dau",
					RECHARGE="recharge",
					RECHARGE_USER="recharge_user",
					MAU="mau",
					TOP_ONLINE="top_online",
					DAY_RETENTION="day_retention",
					THDAY_RETENTION="thday_retention",
					WEEK_RETENTION="week_retention",
					DBWEEK_RETENTION="dbweek_retention",
					MONTH_RETENTION="month_retention",
					DBMONTH_RETENTION="dbmonth_retention",
					TOTAL_RETENTION="total_retention",
					ARPU="arpu",
					ARPPU="arppu",
					TOTAL_USER="total_user",
					TOTAL_RECHARGE_USER="tatal_rec_user",
					PAY_RATE="pay_rate",
					TOTAL_RECHARGE="total_recharge",
					TOTAL_ARPU="total_arpu",
					TOTAL_ARPPU="total_arppu",
					DAY_TURNOVER="day_turnover",
					WEEK_TURNOVER="week_turnover",
					ONLINE="online",
					ARPU1="arpu1",
					ARPU3="arpu3",
					ARPU7="arpu7",
					ARPU14="arpu14",
					ARPU30="arpu30",
					ARPU60="arpu60",
					DAU_PAY_RATE="dau_pay_rate",
					LOGIN_COUNT="login_count",
					ONLINE_TIME="online_time",

					// 充值排名
					RANKING="rank",
					PLAYER_NAME="player_name",
					RECHARGE_GEMS="recharge_gems",
					RECHARGE_RMB="recharge_rmb",
					UPDATE_TIME="update_time",
					RONGYU_ZHANG="rongyu_zhang",
					// 玩家基本信息
					BASE_INFO="base_info",
					BUILD_INFO="build_info",
					SCIENCE_INFO="science_info",
					SKILL_INFO="skill_info",
					PORT_SHIPS="port_ships",
					BUNDLE_INFO="bundle_info",
					SHIP_INFO="ship_info",
					ID="id",
					// NAME="name",
					LEVEL="level",
					//当前经验
					C_EXP="c_exp",
					EXPERIENCE="experience",
					VIP_LEVEL="vip_level",
					MOUTH_CARD="mouth_card",
					COMMAND_LEVEL="command_level",
					ENERGY_VALUE="energy_value" ,
					CREDIT="credit",
					MILITARY_RANK="military_rank",
					HONOR="honor",
					POWER="power",
					GEMS="gems",
					PGEMS="pgems",
					SGEMS="sgems",
					ENERGY="energy",
					// RECHARGE_GEMS="recharge_gems"
					MONEY="money",
					METAL="metal",
					OIL="oil",
					SILICON="silicon",
					URANIUM="uranium",
					ALLIANCE_NAME="alliance_name",
					ACCOUNT="account",
					CREATE_UDID="create_udid",
					LOGIN_UDID="login_udid",
					PAY_UDID="pdid",
					CREATE_TIME="create_time",
					LOGIN_TIME="login_time",
					CONTRIBUTION="contribution",	//贡献度			
					GUILD_POSITION="guild_position",
					LOCATION="location",
					DELETE_STATE="deletestate",
					// 玩家建筑信息
					// NAME="name",
					INDEX="index",
					// LEVEL="level"
					// 船只信息
					// COUNT="count",
					// 关闭服务器
					SERVER_ID="server_id",
					CLOSE_PORT="close_port",
					// ONLINE="online",
					UNSAVED_PLAYER="unsaved_player",
					UNSAVED_EVENT="unsaved_event",
					UNSAVED_ISLAND="unsaved_island",
					UNSAVED_MESSAGE="unsaved_message",
					UNSAVED_ALLIANCE="unsaved_alliance",
					UNSAVED_GEMSTRACK="unsaved_gemstrack",
					UNSAVED_GAMEDATA="unsaved_gamedata",
					UNSAVED_SHIPLOG="unsaved_shiplog",
					UNSAVED_ARENA="unsaved_arena",
					UNSAVED_WORLDBOSS="unsaved_worldboss",
					UNSAVED_AFIGHT="unsaved_afight",
					UNSAVED_AFIGHTEVENT="unsaved_afightevent",
					UNSAVED_BATTLEGROUND="unsaved_battleground",
					UNSAVED_ACTIVITYLOG="unsaved_activitylog",
					// 船只日志
					EVENT_ID="event_id",
					EVENT_TYPE="event_type",
					STATE="state",TIME="time",
					EXTRA_INFO="extra_info",
					EVENT_INFO="event_info",
					EVENT_SHIPS="event_ships",
					BROKEN_SHIPS="broken_ships",SHIPS="ships",
					// 盟战船只日志
					GROUND_SHIPS="ground_ships",CHANGE_SHIPS="change_ships",
					// 宝石日志
					TYPE="type",
					// GEMS="gems",
					NOW_GEMS="now_gems",
					ITEM_ID="item_id",
					// 接管账号
					// ACCOUNT="account",
					PASSWORD="password",
					// 最大宝石数量
					MAX_GEMS="max_gems",
					// 系统邮件
					TITLE="title",
					// ID="id",
					CONTENT="content",
					PROP_STATE="prop_state",
					MAIL_STATE="mail_state",START_TIME="starttime",// 开始时间
					END_TIME="endtime",INTRODUCTION="introduction",// 简介
					PERMANNENT="permanent",// 定时
					ANNOUNCESTATE="announcestate",// 公告状态
					ANNOUNCEFLAG="announceflag",// 公告修改的哪些内容
					ANNOUNCEEDITOR="announceeditor",// 是否可以编辑
					AWARD="award",// 奖励
					BTNNAMES="btnnames",// 按钮名称
					ANNREADPLAYER="annreadplayer",//可以阅读的玩家
					ANNAWARDPLAYER="annawardplayer",//可以领奖的玩家
					MSG="msg",
					//经验
					EXP="exp",
					//联盟
					ALLIANCE="alliance",
					//联盟信息
					ALLIANCEINFO="allianceinfo",
					//联盟排名
					ALLIANCE_RANKNUM="alliance_ranknum",
					//联盟等级
					ALLIANCE_LEVEL="alliance_level",
					//联盟创建人
					ALLIANCE_MASTERNAME="alliance_mastername",
					//联盟经验
					ALLIANCE_EXP="alliance_exp",
					//联盟战斗力
					ALLIANCE_SKILL="alliance_skill",
					ALLIANCE_PLAYER="alliance_player",
					ALLIANCE_FIGHTSCORE="alliance_fightscore",	
					//联盟玩家人数
					ALLIANCE_PLAYER_NUM="alliance_player_num",
					//玩家个人战斗力排行
					FIGHTSCORERANK="fightscorerank",
					//
					PLUNDERRANK="plunderrank",	
					//荣誉排行
					HONORSCORERANK="honorscorerank",		

					// 天降好礼信息
					START="start",END="end",OPEN="open",PROPS="props",
					//查询服务器绑定的状态和原因
					BINGSTATE="bingstate",BINGREASON="bingreason",
					//绑定的等级限制和绑定的地址
					BINGLEVEL="binglevel",BINGADDRESS="bingaddress",
					//是绑定还是跳转页面
					BINGTYPE="bingtype",
					//触发方玩家名字
					E_PLAYERNAME="e_playername",
					//被动方的玩家名称
					E_PASSIVENAME="e_passivename",
					//封号
					BANNED_STATE="banned_state",
					//联盟贡献点
					ALLIANCE_VALUE="alliance_value";
					

	/**
	 * 常量
	 */
	public static int MAX_PLAYER_COUNT=300 // 单次最大处理的玩家数
	;
}
