package foxu.sea;


/** ���ó����� */
public class PublicConst
{
	/** �Ƿ��ǿ������ */
	public static boolean crossServer;
	/** gm���������Ʒ���� */
	public static int ADD_PROP_LIMIT=30;
	/** ������������� */
	public static final int NEW_PLAYER_MARK_MAX=100;
	/** ������������ */
	public static final int NEW_PLAYER_AWARD_SID=59168;
	/** platid ��ǰ��ios,android���� */
	public static final int[] IOS={0,6,7,16,20,22};
	/** ��Ҵ�ֻsid */
	public static final int SHIP_FOR_SID[]={
		10001,10002,10003,10004,10005,10006,10007,10008,
		10011,10012,10013,10014,10015,10016,10017,10018,
		10021,10022,10023,10024,10025,10026,10027,10028,
		10031,10032,10033,10034,10035,10036,10037,10038};
	/** �����û���¼�Ŀ��� */
	public static String LOGIN_LIMIT;
	/** �Ƿ���ά��״̬ */
	public static boolean READY=false;
	/**ϵͳ������ս�ж�սĬ���������� ŷ��������ս�Ķ�սʱ�� ������6 ��Ҫ���ó�true**/
	public static boolean READY_SATURDAY=false;
	/**������,�һ���,���쳵��,������͸,�ձ�������֧��,�޸��˺� �����Ŀ��� */
	public static boolean[] SWITCH_STATE={true,true,false,true,false,false,true};
	/** ���ó�ֵ����  */
	public static String[] VALID_CURRENCY={"USD","CAD","MXN","NZD","JPY","SGD","IDR","INR","RUB","TRY","ILS","ZAR","SAR","none","cheat"};
	/** ����ʱ�����¼���û�id */
	public static int USER_ID_LIMIT[];
	/**������֧���ķ�ʽ����*/
	public static boolean RECHANGE_STATLE=false;
	/** ÿ�α�ʯ������ϵ�� */
	public static final int ALLIANCE_GIVE_GEMS=5;
	/** ÿ����Դÿ�������״��� */
	public static final int ALLIANCE_MAX_VALUE=6;
	public static final int ALLIANCE_GIVE_TIME_VALUE_FORE[]={0,0,0,0,0,0};
	/** Ĭ�Ͼ��״��� */
	public static final int ALLIANCE_GIVE_TIME_VALUE[]={0,0,0,0,0,0,0};
	/** һ������� */
	public final static int DAY_SEC=60*60*24;
	/** һСʱ������ */
	public final static int HOUR_SEC=60*60;
	/** һ���ӵ����� */
	public final static int MIN_SEC=60;
	/** �����¼�Ĭ��ȡ������ */
	public final static int DEFAOULT_ALLIANCE_SIZE=20;
	/** ���쳵�����Ƶ�sid */
	public static int WORKSHOP_PROSID[];
	/** ��Ҫ��¼��Ʒ������־�Ľ���sid */
	public static int[] PRODUCE_LOG_SIDS;
	/**���쳵�����Ƶ����Ƶ�״̬**/
	public static boolean WORDSHOP_LIMIT_STATE=true;
	/** ���������԰汾 */
	public static int SERVER_LOCALE;

	/** �������ȼ����� */
	public static int MAX_PLAYER_LEVEL=80,// �����ҵȼ�
					MAX_HONOR_LEVEL=80,// ��������ȼ�
					MAX_ALLIANCE_LEVEL=80,// ������˵ȼ�
					MAX_BUILD_LEVEL=80,// ������ȼ�
					MAX_SKILL_LEVEL=80;// ����ܵȼ�
	/**
	 * player attributes���Գ��� �����������Ϊkey valueΪ���״���
	 * ALLIANCE_DEFNDָ�����ص�Э�������¼�id
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
					DAILY_GEM_COUNT="daily_gem_count", // ÿ����ȡ�ı�ʯ����
					NEW_DAILY_GEM_COUNT="new_daily_gem_count", // ��ÿ����ȡ�ı�ʯ����
					FRIENDS_LIST="friends_list",// �����б�
					BLACK_LIST="black_list", // �������б�
					LIMIT_SALE_RECORD="limit_sale_record",//��ʱ��Ʒ��¼
					UPCOMMANDER_FAILURE="upcommander_failure",//����ͳ��ʧ�ܼ�¼��X��
					LUCKY_DRAW="lucky_draw",//��ʱ�齱
					LUCKY_DRAW_CLASSIC="lucky_draw_classic",//�����޶�����ʱ�齱
					FP_AWARD="fp_award",
					SERIES_LOGIN="series_login",//������½����	
					VARIBLE_PACKAGE="varible_package",//����������콵����
					ALLIANCE_INVITATION_RECORD="alliance_invitation_record",
					DAY_TASK_UPDATE="day_task_update",//ÿ������ˢ��ʱ��
					BUILD_AUTO_LEVEL_UP="auto_level_up",
					END_TIME="endtime",
					AWARD_TIME="awardtime",
					VIP_POINT="vip_point",
					NEW_PLAYER_AWARD="new_player_award",//�������
					DATE_VITALITY="date_vitality",//��Ծ��
					BASIC_LOTTO="basic_lotto",//������͸
					BASIC_LOTTO_FOLLOW="basic_lotto_follow",//��������
					ONLINE_LUCKY_AWARD="online_lucky_award",//�������ؽ���
					EQUIP_SYS_FOLLOW="equip_sys_follow",//װ��ϵͳ����
					FORBID_CHAT="forbid_chat",//�������
					ALLIANCE_TRANSFER_TIME="alliance_transfer_time",//�Զ��ƽ��᳤�ļ��ʱ��
					PLAYER_DELETE_FLAG="player_delete_flag",//���ɾ��״̬
					VIP_LIMIT_SALE_RECORD="vip_limit_sale_record",//vip��������״̬
					DATE_VITALITY_STORE="date_vitality_store",//��Ծ������洢���,�����Ծ������־û�����
					PLAYER_LOGIN_TIME="player_login_time",//��ҵĵ�¼ʱ��
					RES_TO_LONG="res_to_long",//��Դתlong ���(��Ӱ�����ݴ洢����ȡ)
					DEFAULT_PUSH_MARK="default_push_mark",//ÿ���ۿۺ����߽���Ĭ�ϴ򿪵����ͱ��
					ADD_INIT_PUSH="add_init_push",//���Ĭ�ϴ򿪵����Ϳ��ر��(��","�ָ�,�ɴ洢���)
					BOSS_FIGHT_ID="boss_fight_id",//������boss�¼�ID
					PLAYER_BTIME="player_btime",//�򿪰󶨵�ʱ��
					PLAYER_URL_TIME="player_url_time",//����תҳ���ʱ��
					VAR_PLAYER_INFO="var_player_info",
					ACCOUNT_INFO="account_info",//�޸��˺ŵ���Ϣ 1:�Ƿ����2:�ʼ��Ŀ�ʼʱ��3:��֤��4:����
					ACCOUNT_TIME="account_time",//�޸��˺ż�¼��ʱ��
					CREAT_NAME="creat_name",//����Ƿ��� �������ֱ��
					MEAL_TIME_PUSH="meal_time_push",//��ʱ����
					ONLINE_TIME="online_time",//ĳ������ʱ��
					EXTRA_TROOPS_GIFT="extra_troops_gift",// �۹�Ԯ��������ʶ
					ATTACK_NIAN_TIME="attack_nian_time",//�������޳�ʱ
					NEW_FOLLOW_PLAYER="new_follow_player",// �������������
					NEW_FOLLOW_PLAYER_HOLD="new_follow_player_hold",// ��������פ���¼���ɱ��
					COMMAND_UP_LUCKY="command_up_lucky",// ǰ̨��ʾʹ�õ�ͳ����������ֵ
					CURRENT_NEW_FOLLOW="current_new_follow",// �洢
					LAST_FREE_LOTTO="last_free_lotto",// �������Ѿ�����͸ʱ��
					ALLIANCE_GIVE_VALUES="alliance_give_values",
				    PLAYER_POINT_VALUE="player_point_value",//��һ���
				    PAYMENT_DEVICES="payment_devices",// ���õĳ�ֵ�豸
				    OFFICER_SYSTEM_GUIDE="officer_system_guide",// ������������
				    OFFICER_FREE_DRAW="officer_free_draw",// ������Ƭ�齱�״����
    				LUCKY_DRAW_SHIPPING="lucky_draw_shipping",//ͨ�̺��˳齱  �id,�齱����,�ϴ�λ��
					LUCKY_DRAW_ROB="lucky_draw_rob",//ȫ����"��"� �id,�齱����,�ϴ�λ��
				    PLAYER_COMRADE_CHARGE="player_comrade_charge",//�����ļ��ֵ��¼
				    OWNED_OFFICERS="owned_officers",// ���ӵ�й��ľ���
				    HEAD_SID = "head_sid",//��ǰͷ��
				    HEAD_BORDER = "head_border",//��ǰ�߿�
				    HEAD_INFO = "head_info",// ���ͷ����Ϣ;
					HEAD_TO_ACHIEVEMENT = "completeAchieveHead",// ����ɳɾ����ͷ�񣨱�ʶ����ͷ���ܣ�����ɳɾ��Ƿ���ͷ��
					HEAD_SIGN="HEAD_1";//ͷ��汾��ʶ �����³ɾͼ���������¼���ĳɾ��Ѿ���� ��Ҫ�Զ�������ж�����
	/** �������� */
	public static final String ALLIANCE_CREATE_TIME="alliance_create_time",
						ALLIANCE_JOIN_LEVEL_BOOL="alliance_join_level_bool",
						ALLIANCE_JOIN_SCORE_BOOL="alliance_join_score_bool";
	
	/** ���˵ȼ��������� */
	public static String ALLIANCE_LEVEL_OPEN_SKILL[];
	/**����������**/
	public static int[] ALLIANCE_COMBO_SKILL={521,522,523,524};
	/** ������������ */
	public static int ALLIANCE_LEVEL_EXP[];
	/** ������������ */
	public static int ALLIANCE_LEVEL_NUMS[];
	/** ����ÿ����Դ������ */
	public static int ALLIANCE_RESOURCE_COST[];
	/** ���˾��׽���Ʒsid */
	public static int ALLIANCE_AWARD_SID[];
	/** ����BOSS������ȡ�ľ��׶����� */
	public static int ALLIANCE_GIVE_VALUE_LIMIT[];
	/** ���˵ȼ���Ӧ�����ղ��� */
	public static int ALLIANCE_ISLANDS_LOCATION[];
	/** ������ */
	public static int SCOUT_MONEYCOST[];
	/** VIP��Ӧ��ÿ����������Ĵ��� */
	public static int RESET_TASK_NUM[];
	/** ���ɹ���Ľ���λ����* */
	public static int VIP_LEVEL_FOR_BUILD_DEQUE[];
	/** ����ͳ˧�ȼ��ĳɹ��� */
	public static int COMMANDER_SUCCESS[];
	/** ��ҵȼ���Ӧ���˱��ظ��� */
	public static int PLAYER_RESET_HURT_SHIPS[];
	/** ��Ҿ������ */
	public static int MILITARY_RANK_LEVEL[];
	/** ��Ҿ��ν�Ǯ */
	public static int MILITARY_RANK_MONEY[];
	/** �����ȡ���� */
	public static int MILITARY_RANK_HONOR[];
	/** �̵������Ʒsid */
	public static int SHOP_SELL_SIDS[];
	/** �����̵������Ʒsid */
	public static int ALLIANCE_SHOP_SELL_SIDS[];
	/**��ʱ��Ʒ��sid*/
	public static int LIMIT_SHOP_SIDS[];
	/** ���̵�ɹ�����Ʒ */
	public static int OTHER_SELL_SIDS[];
	/**�����̵��Ӧ����Ʒsid   ϡ�ж�   ����  ��������**/
	public static int OFFCER_SHOP_SIDS[];
	/**�����̵����������   ���� ����**/
	public static int OFFCER_SHOP_NUM_LIMIT[];
	/**�����̵������(��ʯ��2������)   ����  ����  1�Ǳ�ʯ���� 2  2�����ҹ���**/
	public static int OFFCER_SHOP_TYPE_LIMIT[];
	/** ͳ˧�ȼ��Ķ���������� */
	public static int COMMANDER_TROOPS[];
	/** ��ҵȼ���Ӧ�Ĵ������� */
	public static int PLAYER_LEVEL_TROOPS[];
	/** ������λ���ѱ�ʯ */
	public static int VIP_LEVEL_FOR_BUILD_QUEUE_BUY_COST[];
	/** vip��Ӧ��ÿ�չ����������� */
	public static int VIP_LEVEL_FOR_ENERGY_BUY_TIME[];
	/** vip��Ӧ�ĵ����ղ��� */
	public static int VIP_LEVEL_FOR_ISLAND_LOCATION[];
	/** ÿ��index��Ӧ�Ľ���type ����һ�Զ��ϵ ð�ŷֿ� */
	public static String INDEX_FOR_BUILD_TYPE[];
	/** ÿ��ָ�����Ķ�Ӧ���¿�������λ */
	public static String INDEX_0_LEVEL_OPEN_INDEX[];
	/** ������������Ҫ�Ľ�� */
	public static int ALLIANCE_CREATE_MONEY=100000;
	/** ����ͳ˧����Ҫ�ĵ���sid */
	public static int COMMANDER_LEVEL_UP_SID=2022;
	/**ϵ��A*/
	public static int A=3;
	/** ϵ��B */
	public static int B=8;
	/** ����������Ҫ�ĵ���sid */
	public static int UP_SKILL_PROP_SID=2021;
	/** ������ټ�����Ҫ�ı�ʯ */
	public static int CLEAR_SKILL_GEMS=28;
	/** ���ֽ����Ľ�Ұ�sid�ͱ�ʯ���� */
	public static int PROP_SID=5,GEMS=30;
	/** ����ÿ������Ҫ���ѵı�ʯ */
	public static int RESET_TASK_GEMS=28;
	/** ÿ���������ű�ʯ */
	public static int HONOR_DAY_GET=5;
	/** ���˵����ղ����� */
	public static int ALLIANCE_LOCATION_MAX=40;
	/**
	 * ����ͨ������
	 * BUILD_ADD_TYPE=1�¼�һ��������BUILD_LEVEL_UP=2����������CANCLE_BUILD_OR_LEVELUP
	 * =3ȡ��������������� SHIPS_PRODUCE=4��ֻ���� CANLE_SHIP_OR_PROP_PRODUCE=5ȡ����ֻ����
	 * DELETE_BUILD_TYPE=6��������SHIP_STRENGTH=16����ǿ��UP_SHIP=17��������
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
	 * ����˿�ͨ�ų��� REPORT_TASK_TYPE=1�ر����� EXCHANGE_AWARD_TASK=2�����һ�����
	 * NEW_GUIDE_TASK=3��������������� FREE_RESET_TASK���ˢ��ÿ������
	 */
	public static int TASK_REPORT_TYPE=1,TASK_EXCHANGE_AWARD=2,
					TASK_NEW_GUIDE=3,TASK_DAY_RANDOM_CHOOSE=4,
					TASK_DAY_RANDOM=5,TASK_DAY_GEMS_RANDOM=6,GEMS_FINISH=7,
					GIVE_UP=8,RESET_DAY_TASK=9,FREE_RESET_TASK=10;

	/** �����¼����� */
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

	/** ��Ʒʹ�ö˿�ͨ������USE_PROP=1ʹ����Ʒ��BUY_PROP=2������Ʒ */
	public static int USE_PROP=1,BUY_PROP=2,BUY_PROP_AND_USE=3,CODE_PROP=4,DATE_OFF_PROP=5,VIP_LIMIT_SALE=6,
					BUY_ALLIANCE_PROP=7,PHYSICAL_REWARDS=8;

	/**CODE_TYPE=1 �һ����ʶ  COMMARDE_TYPE=2 ��ļ��ʶ **/
	public static int CODE_TYPE=1,COMMARDE_TYPE=2;
	
	/**
	 * ������Զ˿����ͳ���PLAYER_TYPE_UP=1��Ҿ��εȼ�������COMMAND_LEVEL_UP=2���ͳ˧�ȼ�����
	 * BUY_DEQUEN=5������λ GET_INVETD_AWARD=12��¼ DETELT_PLAYER=51ɾ�����
	 * MODIFYNAME=52�޸����� BINDING_ACCOUNT=53 �������˺� SENDING_EMAIL=54 ��������
	 * COM_CODE=55  ��֤��֤�� EXTRA_GIFT_AWARD=56 ��ȡ�۹�Ԯ�� LOCK_ACCOUNT=57 ȥ�������˺�
	 * SET_COMMAND_LUCKY=58 ����ǰ̨ͳ������ֵ,NEW_TASK_BG=59 ����������������,FREE_LOTTO=60 ��Ѿ���,
	 * MEALTIME_ENERGY=61 ��ȡ����,GROWTH_PLAN=62,�ɳ��ƻ�GET_PRIVATE_GROWTH=63,��ȡ���˳ɳ���Ϣ
	 * GET_SERVER_GROWTH=64 ��ȡ��ȫ���ɳ���Ϣ,INC_PROSPERTITY=68 �ָ����ٶ�,SET_POINT_BUFF=69���ùؿ�buff,PLAYER_CHANGE_HEAD=71�޸�ͷ��
	 * ,PLAYER_CHANGE_BORDER=70 �޸�ͷ��߿� GET_INTIMACY_LUCKY=71 ���ܶȳ齱,GIVE_INTIMACY=72�������ܶ�,RECEVIED_INTIMACY=73��ȡ���ܶ�
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

	/** ս���˿ڳ��� */
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
	
	/** �齱���� */
	public static final int LOTTO_FREE=1,LOTTO_ADVANCE=2,LOTTO_LUXURY=3;
	/** �齱������ */
	public static int LOTTO_MAX_1=5,LOTTO_MAX_2=5;
	/** �齱���ĵı�ʯ�� */
	public static int LOTTO_NEED_GEM_1=0,LOTTO_NEED_GEM_2=10,
					LOTTO_NEED_GEM_3=50;
	/** ��ѳ齱ǿ�Ƽ��ʱ��(��) */
	public static int LOTTO_FREE_CIRCLE;

	/** ���������޺ͺ��������� */
	public static final int MAX_FRIEND_LIST_COUNT=20,
					MAX_BLACK_LIST_COUNT=30;

	/** ���ó���BUILD_TYPE=1000�����¼� */
	public static int EVENT_BUILD_TYPE=1000;

	/** ǰ̨�˿ڳ��� */
	public static final int MESSAGE_PORT=2002;

	/** ��ʯ�������� 1����1�� */
	public static final int GEMS_SPEED=60,BUILD_SPEED_UP=1,
					PRODUCE_SPEED_UP=2,FIGHT_MOVE_UP=3;
	/** ��ʯÿ��λ�ٶ����ĵ����� */
	public static int GEMS_PER_UNIT_SPEED=1;

	/** ÿ�������ˢ����Ҫ��ʯ */
	public static final int TASK_DAY_NEED_GEMS=8;

	/**
	 * ATTACK=100����,DEFENCE=101����,ACCURATE=102��׼,AVOID=103�ر�,SHIP_NUM=104��������
	 * ,HP=105����,CRITICAL_HIT=106����, CRITICAL_HIT_RESIST=107�����ֿ�,
	 * EQUIP_ATTACK=120��װ����ȡ�ļӳ�
	 * EXTRA_SHIP=200 ���������,EXTRA_SPEED=201 ���⺽��,EXTRA_CARRY=202 ��������
	 */
	public static final int ATTACK=100,DEFENCE=101,ACCURATE=102,AVOID=103,
					SHIP_NUM=104,FLEET_HP=105,CRITICAL_HIT=106,
					CRITICAL_HIT_RESIST=107,SHIP_HP=108,COUNTER_COMBO_HIT=109,
					EQUIP_ATTACK=120,EQUIP_DEFENCE=121,
					EQUIP_ACCURATE=122,EQUIP_AVOID=123,
					EQUIP_CRITICAL=126,EQUIP_CRITICAL_RESIST=127,EQUIP_HP=128,
					EXTRA_SHIP=200,EXTRA_SPEED=201,EXTRA_CARRY=202;

	/**
	 * ĳ�����͵�service ADD_RESOURCE_BUFF=1��Դ���� HEAVE_BUFF=13��������
	 * STORE_ADD_BUFF�ֿ���������BUFF Ҳ�����ڿƼ�
	 * FORE_METAL_BUFF������ FORE_OIL_BUFFʯ������ FORE_SILICON_BUFF������
	 * FORE_URANIUM_BUFF������ FORE_MONEY_BUFF���������FORE���ý���ʱ��ǰ ������
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
	 * ������ SHIPS_1ս�н� SHIPS_2Ǳͧ SHIPS_3Ѳ�� SHIPS_4��ĸ SHIPS_5=�վ�����
	 * SHIPS_6=�������� SHIPS_7=������� TRANSPORT_SHIP=128 ���䴬
	 */
	public static final int BATTLE_SHIP=1,SUBMARINE_SHIP=2,CRUISER_SHIP=4,
					AIRCRAFT_SHIP=8,POSITION_AIR=16,POSITION_MISSILE=32,
					POSITION_FIRE=64,TRANSPORT_SHIP=128;
	/** AIR_RAID��Ϯ,ARTILLERY�ڻ�,MISSILE����,TORPEDO����,NUCLEAR�� */
	public static final int AIR_RAID=0,ARTILLERY=1,MISSILE=2,TORPEDO=3,
					NUCLEAR=4;
	/** װ���ṩ�Ķ��⿹����ӳ� */
	public static final int RESIST_AIR_RAID=2000,RESIST_ARTILLERY=2001,RESIST_MISSILE=2002,
					RESIST_TORPEDO=2003,RESIST_NUCLEAR=2004,
					ATTACH_BASE=2005,ATTACH_BATTLE=2006,ATTACH_SUBMARINE=2007,
					ATTACH_CRUISER=2009,ATTACH_AIRCRAFT=2013;
	/** ǰ̨֪ͨ��̨�������� */
	public static final int FORE_TASK_SEND=1;

	/** ���ڣ��վ����أ����������� */
	public static final String POSITION_FIRE_NAME="",
					POSITION_MISSILE_NAME="",POSITION_AIR_NAME="";

	/** ���ڣ��վ����أ�����������sid 10041��10061��10051����index 6,7,8 */
	public static final int POSITION_FIRE_SID=10041,
					POSITION_MISSILE_SID=10051,POSITION_AIR_SID=10061,
					POSITION_FIRE_INDEX=6,POSITION_AIR_INDEX=7,
					POSITION_MISSILE_INDEX=8;

	/** ϵͳ���� AUTO_HOLDĬ��פ�� */
	public static final int AUTO_ADD_MAINGROUP=0,ISLAND_BE_ATTACK=1,
					BUILD_FINISHED=2,ENERY_PUSH_IS_FULL=3,AUTO_HOLD=5,
					DATE_OFF_PUSH=6,ONLINE_AWARD_PUSH=7,PEACE_TIME_PUSH=8,
					MEAL_TIME_ENERGY_PUSH=9,STATIONED_PUSH=10;

	/** ��ֵ��ʯ������Ӧ��VIP�ȼ� */
	public static int GEMS_FOR_VIP_LEVEL[];
	/** ��Ӧvip�ȼ������ĵȴ����� */
	public static int VIP_LEVEL_FOR_DEQUE[];
	/** vip�ȼ���Ӧ��ͬʱ�������� */
	public static int VIP_LEVEL_FOR_BATTLE_DEQUE[];
	/** ���Ի������� */
	public final static int kLanguageEnglish=0,kLanguageChinese=1,
					kLanguageFrench=2,kLanguageItalian=3,kLanguageGerman=4,
					kLanguageSpanish=5,kLanguageRussian=6,kLanguageKorean=7,
					kLanguageChineseHant=8,kLanguageJapness=9,kLanguageArab=12,kLanguageThailand=13,
					kLanguageVietnam=14;
	/** ս������ */
	public final static int FIGHT_TYPE_1=1,// ���ǵĽ��ӹ�����%s(��󵺣�Ұ�ص�������)
					FIGHT_TYPE_2=2,// ���ǵĽ��ӹ�����%s(�������)
					FIGHT_TYPE_3=3,// ���ǵĻ��ر�%s(�������)������
					FIGHT_TYPE_4=4,// ���ǵ�%s(��󵺣�Ұ�ص�������)��%s(�������)������
					FIGHT_TYPE_5=5,// ���������פ�ص�Ұ��
					FIGHT_TYPE_6=6,// �Լ��Ķ��� ����
					FIGHT_TYPE_7=7,// ����սBUFF
					FIGHT_TYPE_8=8,FIGHT_TYPE_9=9,FIGHT_TYPE_10=10,// ����ս��
					FIGHT_TYPE_11=11,FIGHT_TYPE_12=12,FIGHT_TYPE_13=13,// FIGHT_TYPE_13
					FIGHT_TYPE_14=14,// �����յ�
					FIGHT_TYPE_15=15,//����ս
					FIGHT_TYPE_16=16,//
					FIGHT_TYPE_17=17,//����
					FIGHT_TYPE_18=18,//������ս(���ս��) ��ս��
					FIGHT_TYPE_19=19,//������ս(���ս��) ����ս��
					FIGHT_TYPE_20=20;//���������

	/** ����״̬ */
	public final static int NOT_FIGHT_STATE=1;

	/** ������ÿ�ս��� */
	public static String[] ARENA_DAILY_AWARDS;
	/** ��������ս���� */
	public static int[] ARENA_BATTLE_AWARDS;
	/** ���������� */
	public static int[] ARENA_BATTLE_RANKS={1,5,10,20,30,50,100,200,500,
		1000,2000,5000,10000,20000,50000,99999999};
	/** ͳ������ʧ�ܹ黹��Ϸ�� 5000 */
	public static int BACK_MONEY=5000;
	/** ͳ����-������*/
	public static int COMMANDER_NUM=10;
	/** ��ͳ���ž��� ��ξ */
	public static int COMMANDER_OPEN_LEVEL=8;
	/** ��ͳ ͳ����-����*/
	public static int[] CLEVEL_CNUM={1,2,3,4,5,6,7,8};
	
	/** ˺����վ������� */
	public static int TEAR_ENERGY=5;
	/** ˺����ո������ô�������  */
	public static int TEAR_PAYCOUNT_MAX=1;
	/** ˺����� �������ı�ʯ */
	public static int TEAR_CLEAR_GEMS=5;
	/** ˺����ճ�ʼ�ؿ�������������������ʼSID��ͬ������������ÿ�������Լ�����ʼSID��*/
	public static int[] TEAR_CHECK_SID={14001};
	 /**���Ͻ��ӳ�ʼ�ؿ�*/
	public static int HERITAGECITY_CHECK_SID=15001;
	/**�������ߵĳ�ʼ�ؿ�**/
	public static int ARMS_CHECK_SID=14051;
	/** ��ʯ�о�Ժ ���� ָ�����ĵȼ� */
	public static int STAR_STONE_CENTER_LVL=30;
	/** ��¼���� */
	public static int[] DAYAWARD;
	/** ��ʱ�齱���� */
	public static int[] LUCKYAWARD;
	/** �׳����awardSid */
	public static int F_PAY_AWARD[];
	/** ��ս������-�˵ȼ�*/
	public static int[] AFIGHT_SHIP;
	
	/** �ؿ�BUFF�ӳ�����-�ؿ�sid*/
	public static int[] SIDS={1,3,5,7,9,11,13};
	/** �ؿ�BUFF�ӳ�����-��ʾsid��ϵ  */
	public static int[] SHOW_SIDS={401,402,403,404,405,406,407};
	/** ������͸�齱��ʾ(��Ϊ�����ֶ��޸�,��ʹ��SeaBackKit.getLuckySids�������м���) */
	public static int[] LUCKY_SIDS;
	/** VIP���������� */
	public static int[] VIP_LIMIT_AWARD;
	/** ÿ���ۿ������Ʒ */
	public static String[] DATE_PRICE_OFF;
	/** ÿ���ۿ��̵꿪��ʱ�� */
	public static float[] DATE_PRICE_OPEN;
	/** �۹�Ԯ��������ʱ�� */
	public static int[] EXTRA_GIFT_AWARDS;
	/** ʹ��ʱ���[LUCKY_SIDS]����Ʒ��Ҫ��ʾ����Ʒ */
	public static int[] PROP_USE_TIPS;
	public static int MOUTHCARDDAYS=29;
	public static int MOUTHCARDAWARD=50;
	public static int VIPPOINT_NUM=100;//�¿�����vip�ɳ�����
	/** ��ʬ�û����� */
	public static int DEAD_LEVEL=2,DEAD_DAY=2;
	/** ÿ������ˢ��CD ��hour��*/
	public static int DAY_TASK_UPDATE_CD=4;
	public static int PLAYER_CODE_EXCHANGE=7;
	public static int CODE_EXCHANGE_ERRO=0;
	/** �������ʵ�������ı�ʯ��ϵ�� */
	public static float RESET_VOID_LAB;
	public final static int CODEID_IS_ERRO=0;
	public final static int CODEID_IS_USERED=-1;
	public final static int CODEID_IS_LIMIT=-2;
	public final static int PROP_NUM=1;
	public final static int DELETE_STATE=2,DELETE_USER_STATE=0;
	public final static int RECOVER_PLAYER_STATE=0;
	public final static int LOAD_ALL_GEM_COST=-1;
	public final static int AWARD_LENGTH=3;//���س齱�������ķ����ƽ������
	public final static int AWARD_TOTAL_LENGTH=10000;//���س齱 ����
	public final static int AWARD_TOTAL_ALLLENGTH=14;//�������̵ĳ���Ϊ14
	/**��̨���ð��˺ŵ�״̬**/
	public static int GAME_BINDING=0;
	/**�󶨵�ԭ��**/
	public static String	BINDING_REASON=" ";
	/**�󶨿�����ʱ��**/
	public static int START_BINDING_TIME=0;
	/**��̨������תҳ���״̬**/
	public static int JUMP_ADDRESS=0;
	/**Ҫ������ת��ƽ̨**/
	public static String BINDING_PLATID="";
	/**��ת�ĵ�ַ**/
	public static String[] URL_ADDRESS;
	/**��תҳ���ԭ��**/
	public static String	JUMP_REASON="";
	public static int JUMP_LEVEL=0;//�ȼ�����
	public static int JUMP_TIME=0;//��ת������ʱ��
//	public static String BINDING_ADDRESS="";//��ַ

	/**�˺Ű󶨱�ʶ 0:�ر� 1:����� 2��ǿ�ư�  GM����ǿ�ư�**/
	public final static int COMMON_BINDING=0;
	public final static int ADVISE_BINDING=1;
	public final static int FORCE_BINDING=2;
	public final static int GM_FORCE_BINGD=3;
	/**ÿ��ɨ�������ĵı�ʯ����**/
	public final static int ARMS_SWEEP_COST_GEMS=1;
	/**ÿ��ɨ�������ĵı�ʯ����**/
	public final static int ELITE_SWEEP_COST_GEMS=1;
	/**ÿ�θ����˺ŵı�ʯ����**/
	public static int MODIFY_A_COST_GEMS;
	public static  String SHIELD_WORD;
	/**�����������**/
	public static String NONEPLAYER_NAME;
	/** ������Ϣ�㲥�����Է��� */
	public static boolean CHAT_GROUP_LOCALE=false;
	/** ���ս��ϵ�� */
	public static float EQUIP_RATIO;
	/** ���������ղض�Ӧ��vip�ȼ�  */
	public static int[] FORMATION_VIP;
	/**ÿ�ξ�������ȡ��  ��ʯ wuzi  ����    �����õ�ʱ�� ��ѵ�Ҳ��Ҫ����**/
	public static int ALLIANCE_VALUES[]={0,5,10,10,5,10,20,5,10,30,5,10,40,5,10,50,5,10};
	/**ÿ�����˾������ʵ�ʱ��  �������ѵĻ� ÿ���������˿Ƽ���**/
	public static int ADD_SCIENCEPOINT=10;
	/** ����ϵͳ���ŵ���߼���  */
	public static int OFFICER_MAX_LV;
	/** ���پ��ζ�Ӧ��߼���  */
	public static int[] OFFICER_RANK_LV;
	/** ��Ƭ�齱ǿ�Ƽ��ʱ�� */
	public static int FREE_DRAW_LIMIT_TIME;
	/** ������Ƭ�ͼ��齱��Ѵ��� */
	public static int OFFICER_FRAG_LOW_FREE;
	/** ������Ƭ�ͼ��齱���ĵ��� */
	public static int[] OFFICER_FRAG_LOW_PROP;
	/** ������Ƭ�ͼ��齱����,ϡ��ֵλ�ö�Ӧ�ĸ��� */
	public static String[] OFFICER_FRAG_LOW;
	/** ������Ƭ�߼��齱���ı�ʯ */
	public static int[] OFFICER_FRAG_HIGH_GEMS;
	/** ������Ƭ�߼��齱����,ϡ��ֵλ�ö�Ӧ�ĸ��� */
	public static String[] OFFICER_FRAG_HIGH;
	/** ������Ƭ�߼��齱������Ƭ�� */
	public static int[] OFFICER_FRAG_HIGH_MIN;;
	/** ������Ƭ�����齱���� */
	public static int[] OFFICER_FRAG_GUIDE_DRAW;
	/** ͼ����Ķ�ʱ������{��ѫ,������Ƽ���,��ȴʱ��,ǰ̨sid,...} */
	public static int[] OFFICER_LIB_READ;
	/** �����鼮�ٷֱ�����{percent,metal,oil,silicon,uranium,money,gems...} */
	public static int[] OFFICER_LIB_WRITE;
	/** ����Ӱ�켼�ܸ��ʡ������Ͷ�Ӧ�ļ���sid{����,ȼ�յ�,...} */
	public static String[] SKILL_TYPE_SIDS;
	/** ���پ��������(�������Ļ�ȡֵ����) */
	public static long[] OFFICER_FEATS_MAX;
	/** ���ս��ʤ��ʱ���ӵľ��ٹ�ѫ */
	public static int PLAYER_FIGHT_SUCCESS_FEATS;
	/** ���ս��ʧ��ʱ���ӵľ��ٹ�ѫ */
	public static int PLAYER_FIGHT_FAIL_FEATS;
	/** ս����ʾ���ݰ汾(�������,ս�������б䶯ʱ����ǰ̨��Ӧ�޸�)  */
	public static int FIGHT_RECORD_VERSION=3;
	
	/** �����ֶ����ó齱��ʾ��ʱ���� */
	public static int[] manualLuckySids;

	/** ��֤�� �������� ��ΪX�κ󴥷� */
	public static int VERTIFY_TRIGGER_COUNT=50;
	/** ��֤�� ��������ʱ�� (��) */
	public static long VERTIFY_TRIGGER_INTERVAL=60*60;
	/** ��֤�� ��֤ʱ��(��) */
	public static int VERTIFY_TIME=30;
	/** ��֤�� ״̬ 0--�رգ�1--������2--��ѯ */
	public static int VERTIFY_STATUS=1;
	/** ��֤�� �ش������� */
	public static int VERTIFY_MAX_COUNT = 5;
	
	/** ���˱��佱�� */
	public static int[] ALLIANCE_CHEST_AWARD;
	/** ���˻��ֽ��� */
	public static int[] ALLIANCE_LUCKY_POINT_AWARD;
	/** ���˻��ֽ�����Ӧ���� */
	public static String[] ALLIANCE_LUCKY_POINT_PLACING;
	/** ���˱������齱���� */
	public final static int MAX_COUNT=7;
	/** ���˱��䱦���������� */
	public static float ALLIANCE_CHEST_UPGRADE_ODDS;
	/** ���˱������˻��ּ��� */
	public static float ALLIANCE_CHEST_LUCKY_POINT_ODDS;
	/** ���˱�������빱��ֵ���� */
	public static float ALLIANCE_CHEST_COUNT_BASE=5.0f;
	/** ���˱�������빱��ֵϵ�� */
	public static float ALLIANCE_CHEST_COUNT_COEF=20.0f;
	/** ���˱������sid */
	public static int ALLIANCE_CHEST_SID=3093;
	/** ʵ�ｱ����Ʒsid */
	public static int[] PHYSICAL_PROPS;
	/** �ɳ��ƻ����˱�ʯ�� */
	public static int GROWTH_PLAN_COST;
	/** �ɳ��ƻ����˽���{����,awardSid} */
	public static int[] GROWTH_PLAN_PRIVATE;
	/** �ɳ��ƻ�ȫ������{��������,awardSid} */
	public static int[] GROWTH_PLAN_SERVER;
	
	/** ȫ����"��"� ��Ʒ���༸�� �ֱ�Ϊ3�� 2�� */
	public static float[] ROB_ACTIVITY_AWARD_ODD={0.05f,0.1f};
	/**j��Ӣս������ת����**/
	public static int FIGHT_ATTACK_LENGTH;
	/**�ż����ǵ��½�**/
	public static int [] COMBINED_CHAPTER;
	/**�ż����ǵĹؿ�����**/
	public static int[] COMBINED_AWARD;
	/**���䴬��sids**/
	public static int [] TRANSPORT_SHIP_SIDS={10071,10072,10073,10074,10075};
	/**��ʯ�����Ӧ����ҵȼ��͵���ȼ�     level -type  ��С����**/
	public static int[] GEMISLAND_LEVEL_TYPE;
	/**��ʯ����sids  type-sid**/
	public static int[] GEMISLAND_SIDS;
	/**ÿ��10���ӻ�ȡ��ʯ����**/
	public static int LOWLIMIT_GEMS_TIMES;
	/**��ĳ��ʱ��-��ĳ��ʱ�� ���볤��Ϊ2(��СʱΪ��λ)**/
	public static int [] GEMS_ISLAND_LIMIT_TIME;
	/**����Сʱ��һ��buff**/
	public static int ISLAND_BUFF_CHANGE;
	/**gemIsland������Ϣ ÿ��n��Сʱ��buff    **/
	public static int []GEMS_ISLAND_BUFF;
	/**��ʯ��ÿ��Ļ�Ծ����**/
	public static int CONTROL_ONLINE;
	/**ԭʼ��ʯ���������**/
	public static int ORIGINAL_NUM;
	/**�Ƿ���Ҫ���ӱ�ʯ����**/
	public static boolean GEMS_ISLAND_CLOSE=true;
	/** ͷ��sid */
	public static int[] HEADICON ={50001,50002,50003,50004,50005,50006,50007,50008,50009,50010,50011};
	// ��ʼͷ��
	public static int HEADSID_BOY = 50001;
	public static int HEADSID_GIRL = 50002;
	/**ͷ��߿�*/
	public static int[] HEADBORDER = {51001,0,51002,1,51003,3,51004,5,51005,7,51006,8,51007,9};
	/**Ĭ�ϱ߿�*/
	public static int DEF_HEADBORDER = 51001;
	/** ���� **/
	public static int[] IMAGE;
	/** ͼ�� **/
	public static int[] COLOUR;
	/** ���� **/
	public static int[] MODEL;
	/** �����������������(�μӾ���������) **/
	public static String[] CROSS_LEAGUE_RANK_AWARD;
	
}