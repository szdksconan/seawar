package foxu.sea.equipment;

/**
 * 装备日志
 * @author Alan
 *
 */
public class EquipmentTrack
{
	/** 日志类型 */
	public static final int ADD=0,REDUCE=1;
	/**
	 *  装备变动原因 
	 *  FROM_CHECK_POINT=0 关卡掉落, FROM_EQUIP_BOX=1 装备宝箱,
	 *	FROM_GM_ADD=2 GM添加, FROM_NPCISLAND=3 npc岛屿掉落, FROM_COMBINE=4 进阶材料合成, 
	 *	FROM_QUALITY_UP=5 进阶装备, FROM_FOLLOW=6 新手引导, FROM_BOSS=7 攻打boss,
	 *	FROM_ARENA=8 竞技场, FROM_ALLIANCE=9 联盟捐献, FROM_ANNOUNCEMENT=10 公告奖励,
	 *	FROM_EXCHANGECODE=11 兑换码, FROM_DAY_AWARD=12 每日登陆, FROM_ONLINE=13 在线奖励,
	 *	FROM_VIP_LIMIT=14 vip限量礼包, FROM_TASK=15 任务奖励, FROM_CLASSIC_LUCKY=16 神秘乐透,
	 *	FROM_CIRCLE_LUCKY=17 幸运轮盘, FROM_LOW_LOTTO=18 低级军需, FROM_HIGH_LOTTO=19 高级军需,
	 *	FROM_ACHIEVE=20 成就,FROM_VARIBLE=21 天降好礼,FROM_ARMS=22 军备航线,FROM_FIRST_PAY=23 首充奖励,
	 *	FROM_TOTAL_BUY=24 累计充值,FROM_APP_GRADE=25 应用评分 FROM_APP_SHARE=26 应用分享
	 *	FROM_RANK_AWARD=27 排名奖励活动 FROM_JIGSAW=28 拼图活动 FROM_CROSSWAR跨服战 FROM_WELFARE新兵福利,
	 *	ALLIANCE_CHEST_AWARD=31联盟宝箱,ALLIANCE_LUCKY_POINT_AWARD=32联盟幸运积分宝箱, 
	 *	LOGIN_REWARD=33登陆有奖活动,PAY_RELAY=34充值接力,QUESTIONNAIRE=35调查问卷
	 *	RROM_WAR_MANNIAC战争狂人 FROM_COMRADE战友系统 FROM_SHIPPING_LUCKY=38通商航运抽奖
	 *  FROM_LUCKY_EXPLORED=39,幸运探险活动 FROM_ROB_LUCKY=40 全名抢"节"活动,FROM_GROWTH_PLAN=41成长计划
	 *	<p>
	 *	INTO_INCR_EXP=100 增加经验, INTO_QUALITY_UP=101 装备进阶消耗, INTO_SALE=102 出售, 
	 *	INTO_COMBINE=103 进阶材料合成消耗 ,
	 */
	public static final int 
					// 获取类型
					FROM_CHECK_POINT=0,FROM_EQUIP_BOX=1,
					FROM_GM_ADD=2,FROM_NPCISLAND=3,FROM_COMBINE=4,
					FROM_QUALITY_UP=5,FROM_FOLLOW=6,FROM_BOSS=7,
					FROM_ARENA=8,FROM_ALLIANCE=9,FROM_ANNOUNCEMENT=10,
					FROM_EXCHANGECODE=11,FROM_DAY_AWARD=12,FROM_ONLINE=13,
					FROM_VIP_LIMIT=14,FROM_TASK=15,FROM_CLASSIC_LUCKY=16,
					FROM_CIRCLE_LUCKY=17,FROM_LOW_LOTTO=18,FROM_HIGH_LOTTO=19,
					FROM_ACHIEVE=20,FROM_VARIBLE=21,FROM_ARMS=22,FROM_FIRST_PAY=23,
					FROM_TOTAL_BUY=24,FROM_APP_GRADE=25,FROM_APP_SHARE=26,
					FROM_RANK_AWARD=27,FROM_JIGSAW=28,FROM_CROSSWAR=29,FROM_WELFARE=30,
					ALLIANCE_CHEST_AWARD=31,ALLIANCE_LUCKY_POINT_AWARD=32,
					LOGIN_REWARD=33,PAY_RELAY=34,QUESTIONNAIRE=35,
					RROM_WAR_MANNIAC=36,FROM_COMRADE=37,FROM_SHIPPING_LUCKY=38,
					FROM_LUCKY_EXPLORED=39,FROM_ROB_LUCKY=40,FROM_GROWTH_PLAN=41,
					FROM_SELLING_PACK1=42,FROM_SELLING_PACK2=43,FROM_SELLING_PACK3=44,
					FROM_INTIMACY_LUCKY = 45,
					// 消耗类型
					INTO_INCR_EXP=100,INTO_QUALITY_UP=101,INTO_SALE=102,
					INTO_COMBINE=103;

	/** 记录ID */
	int id;
	/** 日志类型 */
	int type;
	/** 装备变动原因 */
	int reason;
	/** 玩家ID */
	int playerId;
	/** 装备sid */
	int equipSid;
	/** 装备数量 */
	int num;
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
	/** 当前剩余 */
	int nowLeft;
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type=type;
	}
	
	public int getReason()
	{
		return reason;
	}
	
	public void setReason(int reason)
	{
		this.reason=reason;
	}
	
	public int getPlayerId()
	{
		return playerId;
	}
	
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}
	
	public int getEquipSid()
	{
		return equipSid;
	}
	
	public void setEquipSid(int equipSid)
	{
		this.equipSid=equipSid;
	}
	
	public int getNum()
	{
		return num;
	}
	
	public void setNum(int num)
	{
		this.num=num;
	}
	
	public int getCreateAt()
	{
		return createAt;
	}
	
	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}
	
	public int getItem_id()
	{
		return item_id;
	}
	
	public void setItem_id(int item_id)
	{
		this.item_id=item_id;
	}
	
	public int getYear()
	{
		return year;
	}
	
	public void setYear(int year)
	{
		this.year=year;
	}
	
	public int getMonth()
	{
		return month;
	}
	
	public void setMonth(int month)
	{
		this.month=month;
	}
	
	public int getDay()
	{
		return day;
	}
	
	public void setDay(int day)
	{
		this.day=day;
	}
	
	public int getNowLeft()
	{
		return nowLeft;
	}
	
	public void setNowLeft(int nowLeft)
	{
		this.nowLeft=nowLeft;
	}
	

}
