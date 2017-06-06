package foxu.sea.gems;

/** 宝石消费追踪 */
public class GemsTrack
{

	/**
	 * BUILD_SPEED_UP=0建筑加速，PRODUCE_SPEED_UP=1生产产品加速 OTHER=6宝石刷新随机 立即完成随机
	 * GEMS_PAY=7宝石充值 GEMS_AWARD=8由系统或者GM送给玩家做奖励的宝石 FIGHT_EVENT_UP战斗事件加速
	 * GEMS_DAY_SEND=13每日宝石领取ALLIANCE_GIVE联盟捐献 LOTTO 抽奖,MOVE_ISLAND
	 * 搬迁岛屿,ARENA 环球军演 CLEAR_TEAR 重置撕裂虚空,SUBMIT_ORDER=17 提交订单,CANCEL_ORDER=18
	 * 取消订单,AWARD=22幸运转盘MOUTHCARD=24月卡,MOUTHCARD_GET=25月卡领取
	 * EQUIP_CAPACITY=26装备仓库扩展VIP_LIMIT_AWARD
	 * =27vip限购奖励包CLEAR_ARMS_POINT=28重置军备航线SWEEP_ARMS_POINT=29
	 * 扫荡消耗宝石AWARD_LETO=30 神秘乐透 MODIFY_USERACCOUNT=31 CONSUME_GEMS=32 累计消费返利
	 * JIGSAW_ACTIVITY=33 拼图活动,CROSSWAR_BET 跨服战押注,WIN_BET跨服赢取押注
	 * DRAW_OFFICER_FRAG=36军官碎片抽奖 MAKE_OFFICER_BOOK=37
	 * 编撰军官书籍,OFFICER_RANK_UP=38 军衔提升,RESET_VOID_LAB=39 重置星石,RECRUIT_HALF=40
	 * 新兵福利半价
	 * ALLIANCE_VALUE =41 联盟捐献物资  LOGIN_REWARD=42登陆有礼活动时间找回
	 * LUCKY_DRAW_SHIPPING=43 通商航运抽奖 LUCKY_EXPLORED=44 幸运探险活动 LUCKY_DRAW_ROB=45 全民抢"节"
	 * BUY_GROWTH_PLAN=48,购买成长计划 GROWTH_PLAN=47
	 * SWEEP_ELITE_POINT =46 扫荡精英战场 OFFICER_SHOP=49 军官商店消费 OFFICER_FLUSH=50 购买繁荣度 BUY_PROSPERITY=51
	 * 热销大礼包  SELLING_P_ONE=53 SELLING_P_TWO=54 SELLING_P_THREE=55
	 * FIGHT_GEM_ISLAND=52 攻打宝石岛屿胜利返航  BUY_ALLIANCE_FLAG=58 购买联盟旗子消费的宝石
	 */
	// 支出
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
					// 收入
					GEMS_PAY=7,GEMS_AWARD=8,GM_SEND=9,INVITE=11,
					GEMS_DAY_SEND=12,SERVER_AWARD=20,THIRD_GEMS_PAY=23,
					MOUTHCARD=24,MOUTHCARD_GET=25,CONSUME_GEMS=32,
					JIGSAW_ACTIVITY=33,WIN_BET=35,GROWTH_PLAN=47,
					FIGHT_GEM_ISLAND=52,SELLING_P_ONE=53,SELLING_P_TWO=54,SELLING_P_THREE=55,
					// 不影响
					SUBMIT_ORDER=18,CANCEL_ORDER=19;

	/** 记录ID */
	int id;
	/** 消费类型 */
	int type;
	/** 玩家ID */
	int playerId;
	/** 消费宝石数量 */
	int gems;
	/** 创建时间 */
	int createAt;
	/** 物品ID */
	int item_id;
	/** 年 */
	int year;
	/** 月 */
	int month;
	/** 日 */
	int day;
	/** 当前剩余宝石 */
	long nowGems;

	/**
	 * @return createAt
	 */
	public int getCreateAt()
	{
		return createAt;
	}

	/**
	 * @param createAt 要设置的 createAt
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
	 * @param gems 要设置的 gems
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
	 * @param playerId 要设置的 playerId
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
	 * @param type 要设置的 type
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
	 * @param id 要设置的 id
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
	 * @param item_id 要设置的 item_id
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
	 * @param day 要设置的 day
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
	 * @param month 要设置的 month
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
	 * @param year 要设置的 year
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
	 * @param nowGems 要设置的 nowGems
	 */
	public void setNowGems(long nowGems)
	{
		this.nowGems=nowGems;
	}

}
