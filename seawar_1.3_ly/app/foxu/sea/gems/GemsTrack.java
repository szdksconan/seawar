package foxu.sea.gems;

/** ��ʯ����׷�� */
public class GemsTrack
{

	/**
	 * BUILD_SPEED_UP=0�������٣�PRODUCE_SPEED_UP=1������Ʒ���� OTHER=6��ʯˢ����� ����������
	 * GEMS_PAY=7��ʯ��ֵ GEMS_AWARD=8��ϵͳ����GM�͸�����������ı�ʯ FIGHT_EVENT_UPս���¼�����
	 * GEMS_DAY_SEND=13ÿ�ձ�ʯ��ȡALLIANCE_GIVE���˾��� LOTTO �齱,MOVE_ISLAND
	 * ��Ǩ����,ARENA ������� CLEAR_TEAR ����˺�����,SUBMIT_ORDER=17 �ύ����,CANCEL_ORDER=18
	 * ȡ������,AWARD=22����ת��MOUTHCARD=24�¿�,MOUTHCARD_GET=25�¿���ȡ
	 * EQUIP_CAPACITY=26װ���ֿ���չVIP_LIMIT_AWARD
	 * =27vip�޹�������CLEAR_ARMS_POINT=28���þ�������SWEEP_ARMS_POINT=29
	 * ɨ�����ı�ʯAWARD_LETO=30 ������͸ MODIFY_USERACCOUNT=31 CONSUME_GEMS=32 �ۼ����ѷ���
	 * JIGSAW_ACTIVITY=33 ƴͼ�,CROSSWAR_BET ���սѺע,WIN_BET���ӮȡѺע
	 * DRAW_OFFICER_FRAG=36������Ƭ�齱 MAKE_OFFICER_BOOK=37
	 * ��׫�����鼮,OFFICER_RANK_UP=38 ��������,RESET_VOID_LAB=39 ������ʯ,RECRUIT_HALF=40
	 * �±��������
	 * ALLIANCE_VALUE =41 ���˾�������  LOGIN_REWARD=42��½����ʱ���һ�
	 * LUCKY_DRAW_SHIPPING=43 ͨ�̺��˳齱 LUCKY_EXPLORED=44 ����̽�ջ LUCKY_DRAW_ROB=45 ȫ����"��"
	 * BUY_GROWTH_PLAN=48,����ɳ��ƻ� GROWTH_PLAN=47
	 * SWEEP_ELITE_POINT =46 ɨ����Ӣս�� OFFICER_SHOP=49 �����̵����� OFFICER_FLUSH=50 �����ٶ� BUY_PROSPERITY=51
	 * ���������  SELLING_P_ONE=53 SELLING_P_TWO=54 SELLING_P_THREE=55
	 * FIGHT_GEM_ISLAND=52 ����ʯ����ʤ������  BUY_ALLIANCE_FLAG=58 ���������������ѵı�ʯ
	 */
	// ֧��
	public static final int BUILD_SPEED_UP=0,PRODUCE_SPEED_UP=1,
					BUY_ENERGY=2,BUY_PROP=3,BUY_BUILD_DEQUEN=4,
					REPARIE_SHIPS=5,OTHER=6,FIGHT_EVENT_UP=10,
					ALLIANCE_GIVE=13,LOTTO=14,MOVE_ISLAND=15,ARENA=16,
					CLEAR_TEAR=17,RESET_SKILL=21,AWARD=22,EQUIP_CAPACITY=26,
					VIP_LIMIT_AWARD=27,CLEAR_ARMS_POINT=28,
					SWEEP_ARMS_POINT=29,AWARD_LETO=30,MODIFY_USERACCOUNT=31,
					CROSSWAR_BET=34,DRAW_OFFICER_FRAG=36,
					MAKE_OFFICER_BOOK=37,
					OFFICER_RANK_UP=38,
					RESET_VOID_LAB=39,	ALLIANCE_VALUE=41,
					RECRUIT_HALF=40,  LOGIN_REWARD=42,LUCKY_DRAW_SHIPPING=43,
					LUCKY_EXPLORED=44,LUCKY_DRAW_ROB=45,SWEEP_ELITE_POINT=46,
					BUY_GROWTH_PLAN=48,OFFICER_SHOP=49,OFFICER_FLUSH=50,BUY_PROSPERITY=51,
					LEAGUE_FLUSH_COUNT=56,LEAGUE_BATTLE_COUNT=57,
					BUY_ALLIANCE_FLAG=58,
					// ����
					GEMS_PAY=7,GEMS_AWARD=8,GM_SEND=9,INVITE=11,
					GEMS_DAY_SEND=12,SERVER_AWARD=20,THIRD_GEMS_PAY=23,
					MOUTHCARD=24,MOUTHCARD_GET=25,CONSUME_GEMS=32,
					JIGSAW_ACTIVITY=33,WIN_BET=35,GROWTH_PLAN=47,
					FIGHT_GEM_ISLAND=52,SELLING_P_ONE=53,SELLING_P_TWO=54,SELLING_P_THREE=55,
					// ��Ӱ��
					SUBMIT_ORDER=18,CANCEL_ORDER=19;

	/** ��¼ID */
	int id;
	/** �������� */
	int type;
	/** ���ID */
	int playerId;
	/** ���ѱ�ʯ���� */
	int gems;
	/** ����ʱ�� */
	int createAt;
	/** ��ƷID */
	int item_id;
	/** �� */
	int year;
	/** �� */
	int month;
	/** �� */
	int day;
	/** ��ǰʣ�౦ʯ */
	long nowGems;

	/**
	 * @return createAt
	 */
	public int getCreateAt()
	{
		return createAt;
	}

	/**
	 * @param createAt Ҫ���õ� createAt
	 */
	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}

	/**
	 * @return gems
	 */
	public int getGems()
	{
		return gems;
	}

	/**
	 * @param gems Ҫ���õ� gems
	 */
	public void setGems(int gems)
	{
		this.gems=gems;
	}

	/**
	 * @return playerId
	 */
	public int getPlayerId()
	{
		return playerId;
	}

	/**
	 * @param playerId Ҫ���õ� playerId
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	/**
	 * @return type
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type Ҫ���õ� type
	 */
	public void setType(int type)
	{
		this.type=type;
	}

	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id Ҫ���õ� id
	 */
	public void setId(int id)
	{
		this.id=id;
	}

	/**
	 * @return item_id
	 */
	public int getItem_id()
	{
		return item_id;
	}

	/**
	 * @param item_id Ҫ���õ� item_id
	 */
	public void setItem_id(int item_id)
	{
		this.item_id=item_id;
	}

	/**
	 * @return day
	 */
	public int getDay()
	{
		return day;
	}

	/**
	 * @param day Ҫ���õ� day
	 */
	public void setDay(int day)
	{
		this.day=day;
	}

	/**
	 * @return month
	 */
	public int getMonth()
	{
		return month;
	}

	/**
	 * @param month Ҫ���õ� month
	 */
	public void setMonth(int month)
	{
		this.month=month;
	}

	/**
	 * @return year
	 */
	public int getYear()
	{
		return year;
	}

	/**
	 * @param year Ҫ���õ� year
	 */
	public void setYear(int year)
	{
		this.year=year;
	}

	/**
	 * @return nowGems
	 */
	public long getNowGems()
	{
		return nowGems;
	}

	/**
	 * @param nowGems Ҫ���õ� nowGems
	 */
	public void setNowGems(long nowGems)
	{
		this.nowGems=nowGems;
	}

}
