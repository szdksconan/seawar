package foxu.sea.officer;

/**
 * 军官日志
 * @author Alan
 *
 */
public class OfficerTrack
{
	/** 日志类型 */
	public static final int ADD=0,REDUCE=1;
	/**
	 *  军官变动原因 
	 *  FROM_CHECK_POINT=0 关卡掉落, FROM_EQUIP_BOX=1 装备宝箱,
	 *	FROM_GM_ADD=2 GM添加, FROM_NPCISLAND=3 npc岛屿掉落, FROM_COMBINE=4 进阶材料合成, 
	 *	FROM_QUALITY_UP=5 进阶装备, FROM_FOLLOW=6 新手引导, FROM_BOSS=7 攻打boss,
	 *	FROM_ARENA=8 竞技场, FROM_ALLIANCE=9 联盟捐献, FROM_ANNOUNCEMENT=10 公告奖励,
	 *	FROM_EXCHANGECODE=11 兑换码, FROM_DAY_AWARD=12 每日登陆, FROM_ONLINE=13 在线奖励,
	 *	FROM_VIP_LIMIT=14 vip限量礼包, FROM_TASK=15 任务奖励, FROM_CLASSIC_LUCKY=16 神秘乐透,
	 *	FROM_CIRCLE_LUCKY=17 幸运轮盘, FROM_LOW_LOTTO=18 低级军需, FROM_HIGH_LOTTO=19 高级军需,
	 *	FROM_ACHIEVE=20 成就,FROM_VARIBLE=21 天降好礼,FROM_ARMS=22 军备航线,FROM_FIRST_PAY=23 首充奖励,
	 *	FROM_TOTAL_BUY=24 累计充值,FROM_APP_GRADE=25 应用评分 FROM_APP_SHARE=26 应用分享
	 *	FROM_RANK_AWARD=27 排名奖励活动 FROM_JIGSAW=28 拼图活动 FROM_CROSSWAR跨服战 FROM_WELFARE新兵福利
	 *	RROM_WAR_MANNIAC战争狂人 FROM_COMRADE战友系统 FROM_FRAG_COMBINE=1000 碎片合成,FROM_FRAG_DRAW=1001 碎片抽奖,
	 *	FROM_GM_ADD=1002 GM添加    FROM_ELITE=1003 精英战场 FROM_OFFICER_SHOP=1004军官商店
	 *	<p>
	 *	INTO_UP_RANK=2000 军衔突破,INTO_OFFICER_COMBINE=2001 军官合成,INTO_OFFICER_RESET=2002 制作书籍
	 *	INTO_OFFICER_DISBAND=2003 遣散军官
	 */
	public static final int 
					// 获取类型(获取来自于物品奖励时,沿用装备日志原因)
					FROM_FRAG_COMBINE=1000,FROM_FRAG_DRAW=1001,FROM_GM_ADD=1002,FROM_ELITE=1003,
					FROM_OFFICER_SHOP=1004,
					// 消耗类型
					INTO_UP_RANK=2000,INTO_OFFICER_COMBINE=2001,INTO_OFFICER_RESET=2002,
					INTO_OFFICER_DISBAND=2003;

	/** 记录ID */
	int id;
	/** 日志类型 */
	int type;
	/** 装备变动原因 */
	int reason;
	/** 玩家ID */
	int playerId;
	/** 装备sid */
	int officerSid;
	/** 装备数量 */
	int num;
	/** 创建时间 */
	int createAt;
	/** 物品ID */
	int item_id;
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
	
	public int getOfficerSid()
	{
		return officerSid;
	}
	
	public void setOfficerSid(int officerSid)
	{
		this.officerSid=officerSid;
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
	
	public int getNowLeft()
	{
		return nowLeft;
	}
	
	public void setNowLeft(int nowLeft)
	{
		this.nowLeft=nowLeft;
	}
	

}
