package foxu.sea.gm;

/**
 * ������
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
					ERR_VLAUE_ERROR=15,// ������������
					ERR_VIPLEVEL_LIMIT=16,
					ERR_PRO_IS_NULL=17,// ����Ĵ�����ƷΪ��
					ERR_PRO_IS_ERRO=18,// ����Ĵ�����Ʒ����
					ERR_PRO_NUM_IS_ERRO=19,// ����Ĵ�����Ʒ��������
					ERR_TITLE_NULL=20,// ����Ϊ��
					ERR_CONTENT_NULL=21,// ����Ϊ��
					ERR_BTNNAME_NULL=22,// ��ť����
					ERR_TIME_ERRO=23,// ʱ�����
					ERR_AWARD_ERRO=24,// ��������
					ERR_INTRODUCTION_ERRO=25,// ������
					ERR_ANNOUNCE_IS_NULL=26,// ����Ϊ��
					ERR_DAY_SID_IS_ERRO=27,// ÿ���ۿ���Ʒ��sid����
					ERR_DAY_PRO_ABOUT_LENGTH=28,// �����ÿ���ۿۻ����Ʒ������������
					ERR_TITLE_LENGTH_ERRO=29,//���ⳤ�Ȳ���
					ERR_CONTENT_LENGTH_ERRO=30,//���ݳ��Ȳ���
					ERR_INTODUCTION_LENGTH_ERRO=31,//��鳤�Ȳ���
					ERR_BTNNAME_LENGTH_ERRO=32,//��ť���Ȳ���
					ERR_PROINFO_IS_NULL=33,//��ƷΪ��
					ERR_PRO_ERRO_LENGTH=34,//��Ʒ�ĳ��Ȳ���
					ERR_PRO_IS_NOT_PRO=35,//���õ���Ʒ���е���Ʒsid����
					ERR_PRO_IS_SPACE=36,//����Ʒ�������пո�
					ERR_CONTENET_IS_NULL=37,//�����������ƻ�������������Ϊ��
					ERR_PLAYER_NO_ALLIANCE=38,//�����û������
					ERR_ADDRESS_IS_ERROR=39,//�����ַ����
					ERR_WORLD_ADDRESS_ERROR=40,//��ǰ�ص㲻������boss
					ERR_WORLD_HAVE_BLOOD =41,//��ǰ�ص㲻������boss
					ERR_AWARD_SHIPS_ERRO=42,//���˳齱�еĴ�ֻ���ò���
					ERR_AWARD_SHIPS_START=43,//���˳齱�е���ʯͷ���ò���
					ERR_AWARD_SHIPS_PRO=44,//���˳齱�е���Ʒͷ���ò���
					ERR_AWARD_SHIPS_EQU=45,//���˳齱�е�װ��ͷ���ò���
					// ���
					ERR_PLAYER_NAME_NULL=201,
					ERR_PLAYER_NOT_EXISTS=202,
					ERR_PLAYER_ALREADY_LINKED=203,
					ERR_ACCOUNT_NULL=204,
					ERR_USER_NOT_EXISTS=205,
					ERR_ACCOUNT_ERROR=206,
					ERR_ACCOUNT_ALREADY_LINKED=207,
					ERR_PWD_RECORD_NOT_EXISTS=208,
					ERR_COVER_PLAYERNAME=209,//������޸��������
					ERR_MODIFAY_LENGHT=210,//Ҫ�޸ĵ���ҳ��Ȳ�һ��
					ERR_PLAYER_IS_EXSITS=211,//Ҫ�޸���ҵ������Ѿ�����
					// ����
					ERR_ALLIANCE_NOT_EXISTS=401,
					ERR_ALLIANCE_SKILL_NOT_EXISTS=402,
					ERR_ALLIANCE_LEVEL_ERROR=403,
					ERR_ALLIANCE_EXISTS=404,
					ERR_ALLIANCE_NAME_IS_NULL=405,
					ERR_ALLIANCE_LENGTH_WRONG=406,//�������Ƴ���
					ERR_ALLIANCE_IS_UNVALID=407,//�������Ʋ�����
					ERR_ALLIANCE_NAME_USED=408,//�����Ѿ�ӵ��
					ERR_ALLIANCE_EVENT_NULL=409,//�����¼�Ϊ��
					ERR_ALLIANCEFIGHT_IS_NULL=410,//����սΪ��
					ERR_ALLIANCE_FIGHT_IS_OVER=411,//�ھ������������ս�Ѿ������޷���ɢ����
					ERR_JOIN_ALLAINCE_FIGHT=412,//����Ѿ�����������ս
					ERR_BATTLE_FIGHT_IS_NULL=415,//���еĵ��첻��������ս
					
					// ��������ֻ���Ƽ�����Ʒ
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
					ERR_BUFF_IS_NULL=522,//BUFFΪ��
					ERR_BUILD_IS_LEVEL_ERRO=523,//ָ�����ĵȼ�����
					ERR_LEVELABILITY_NULL=524,//abilityΪ��
					ERR_PLAYER_NOT_UP=525,//��ǰ��Ҳ�������
					ERR_SHIP_LEVEL_ERRO=526,//��ǰ�����ĵȼ�������ҵĵȼ�
					ERR_ADDRESS_NULL=527,//��ǰ����ҳ����Ϊ��
					ERR_LEVEL_PLAYER_ERRO=528,//��ǰ�ĵȼ����ô���
					// ��ʱʱ��Ϊ���
					TSTARTTIME_ERRO=512,CHECK_POINT_NULL=513,
					CHECK_POINT_ERRO=514,

					// �
					ERR_ACTIVITY_NOT_EXISTS=601,ERR_ACTIVITY_MAX_PAGE=602,
					ERR_ACTIVITY_MIN_PAGE=603,
					ERR_ACTIVITY_AWARD_IS_NULL=604,//������Ϊ��
					ERR_ACTIVITY_AWARD_LENGTH_ERRO=605,//�������ĳ��Ȳ���
					ERR_ACTIVITY_AWARD_PROBABILITY_ERRO=606,//�������ĸ��ʲ���
					ERR_ACTIVITY_PRO_LENGTH_ERRO=607,//�����ÿ���ۿ���Ʒ�ĳ��Ȳ���
					ERR_SCORE_REASON_IS_NULL=608,//���ֻ��ԭ��Ϊ��
					ERR_SCORE_URL_IS_NULL=609,//URL��ַ����Ϊ��
					ERR_SCORE_ID_IS_NULL=610,//id����Ϊ��
					ERR_BINDING_OPNE=611,//���ȹرհ󶨣��ڿ�����ת
					ERR_JUMP_ADDRESS_OPEN=612,//���ȹر���ת��Ȼ���ڿ�����
					ERR_BINDING_OPEN=613,//��ǰƽ̨�µİ�״̬�Ѿ�������
					
					ERR_UNKNOWN=-1,
					ERR_DELETE_ERRO=701,//ɾ��ʧ��
					ERR_RECOVER_ERRO=702,//�޸�ʧ��
					ERR_PLAYER_IS_MASTER=703,//�ǻ᳤����ɾ
					ERR_REASON_IS_NULL=704,//�����ԭ��Ϊ��
	
					ERR_NAME_LENGTH=801,//��ɫ�������ȳ�������
					ERR_NAME_UNVALID=802,//��ǰ������Ʋ�����
					ERR_NAME_BEEN_USED=803,//���Ʊ�ʹ��
					ERR_OFFCERSCARCITY=804,//ϡ�жȲ�����
					ERR_OFFCER_LIMIT_LENGTH=805;//�������Ʋ���
	// ���ı���
	public static final String
	// ������Ϣ
					SID="sid",
					NAME="name",
					COUNT="count",
					// ��Ӫ����
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

					// ��ֵ����
					RANKING="rank",
					PLAYER_NAME="player_name",
					RECHARGE_GEMS="recharge_gems",
					RECHARGE_RMB="recharge_rmb",
					UPDATE_TIME="update_time",
					RONGYU_ZHANG="rongyu_zhang",
					// ��һ�����Ϣ
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
					//��ǰ����
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
					CONTRIBUTION="contribution",	//���׶�			
					GUILD_POSITION="guild_position",
					LOCATION="location",
					DELETE_STATE="deletestate",
					// ��ҽ�����Ϣ
					// NAME="name",
					INDEX="index",
					// LEVEL="level"
					// ��ֻ��Ϣ
					// COUNT="count",
					// �رշ�����
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
					// ��ֻ��־
					EVENT_ID="event_id",
					EVENT_TYPE="event_type",
					STATE="state",TIME="time",
					EXTRA_INFO="extra_info",
					EVENT_INFO="event_info",
					EVENT_SHIPS="event_ships",
					BROKEN_SHIPS="broken_ships",SHIPS="ships",
					// ��ս��ֻ��־
					GROUND_SHIPS="ground_ships",CHANGE_SHIPS="change_ships",
					// ��ʯ��־
					TYPE="type",
					// GEMS="gems",
					NOW_GEMS="now_gems",
					ITEM_ID="item_id",
					// �ӹ��˺�
					// ACCOUNT="account",
					PASSWORD="password",
					// ���ʯ����
					MAX_GEMS="max_gems",
					// ϵͳ�ʼ�
					TITLE="title",
					// ID="id",
					CONTENT="content",
					PROP_STATE="prop_state",
					MAIL_STATE="mail_state",START_TIME="starttime",// ��ʼʱ��
					END_TIME="endtime",INTRODUCTION="introduction",// ���
					PERMANNENT="permanent",// ��ʱ
					ANNOUNCESTATE="announcestate",// ����״̬
					ANNOUNCEFLAG="announceflag",// �����޸ĵ���Щ����
					ANNOUNCEEDITOR="announceeditor",// �Ƿ���Ա༭
					AWARD="award",// ����
					BTNNAMES="btnnames",// ��ť����
					ANNREADPLAYER="annreadplayer",//�����Ķ������
					ANNAWARDPLAYER="annawardplayer",//�����콱�����
					MSG="msg",
					//����
					EXP="exp",
					//����
					ALLIANCE="alliance",
					//������Ϣ
					ALLIANCEINFO="allianceinfo",
					//��������
					ALLIANCE_RANKNUM="alliance_ranknum",
					//���˵ȼ�
					ALLIANCE_LEVEL="alliance_level",
					//���˴�����
					ALLIANCE_MASTERNAME="alliance_mastername",
					//���˾���
					ALLIANCE_EXP="alliance_exp",
					//����ս����
					ALLIANCE_SKILL="alliance_skill",
					ALLIANCE_PLAYER="alliance_player",
					ALLIANCE_FIGHTSCORE="alliance_fightscore",	
					//�����������
					ALLIANCE_PLAYER_NUM="alliance_player_num",
					//��Ҹ���ս��������
					FIGHTSCORERANK="fightscorerank",
					//
					PLUNDERRANK="plunderrank",	
					//��������
					HONORSCORERANK="honorscorerank",		

					// �콵������Ϣ
					START="start",END="end",OPEN="open",PROPS="props",
					//��ѯ�������󶨵�״̬��ԭ��
					BINGSTATE="bingstate",BINGREASON="bingreason",
					//�󶨵ĵȼ����ƺͰ󶨵ĵ�ַ
					BINGLEVEL="binglevel",BINGADDRESS="bingaddress",
					//�ǰ󶨻�����תҳ��
					BINGTYPE="bingtype",
					//�������������
					E_PLAYERNAME="e_playername",
					//���������������
					E_PASSIVENAME="e_passivename",
					//���
					BANNED_STATE="banned_state",
					//���˹��׵�
					ALLIANCE_VALUE="alliance_value";
					

	/**
	 * ����
	 */
	public static int MAX_PLAYER_COUNT=300 // ���������������
	;
}
