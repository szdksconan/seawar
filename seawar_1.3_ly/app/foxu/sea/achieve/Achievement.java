package foxu.sea.achieve;

import foxu.sea.Player;
import mustang.util.Sample;
import mustang.util.SampleFactory;


/**
 * 成就
 * @author yw
 *
 */
public class Achievement extends Sample
{
	
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	
	/** 属性KEY <=500当前属性  501-999单次最高属性    >=1000累计属性*/
	
	/** 基础 1-100 501-600 1000-1100 
	DIRECTOR_BUILD 指挥中心,SHIP_BUILD 船坞,TECH_BUILD 科研中心,
	HONOR_LEVEL 声望,MONEY 金币,METAL 铁矿,OIL 石油,SILICA 硅矿,URANIUM 铀矿
	*/
	public static final int DIRECTOR_BUILD=1,SHIP_BUILD=2,TECH_BUILD=3,
					HONOR_LEVEL=4,MONEY=5,METAL=6,OIL=7,SILICA=8,URANIUM=9;
	/** 军事 101-200 601-700 1101-1200 
 	CHAPTER 章节,WILD 野地,ATTACK_PLAYR 攻打玩家,BATTLE_SHIP 消灭玩家的战列舰,
	SUBMARINE 消灭玩家的战潜艇,CRUISER 消灭玩家的巡洋舰,CARRIER 消灭玩家的航母
	 */
	public static final int CHAPTER=601,WILD=1101,ATTACK_PLAYR=1102,BATTLE_SHIP=1103,
					SUBMARINE=1104,CRUISER=1105,CARRIER=1106;
	/** 荣誉 201-300 701-800 1201-1300 
	RECHARGE 充值,BIND 绑定账号,SERIERS_LOGIN 连续登陆,
	SHARE_GAME 分享游戏,SHARE_FIGHT 分享战报,PLAYER_LEVEL 玩家等级,
	COMMAND_LEVEL 统御等级,INVITE 邀请玩家,ALLIANCE_OFFER 盟贡献点,
	ATTACK_BOSS 攻击Boss,KILL_BOSS 击杀Boss,ALLIANCE_OFFER_ONE_DAY 每日盟贡献点,
	ARENA_RANK=603环球排名,MOUTH_CARD=604月卡，HONOR_SCORE=605荣誉值，FIGHT_SCORE=606战力
	 */
	public static final int RECHARGE=1201,BIND=201,SERIERS_LOGIN=202,
					SHARE_GAME=1202,SHARE_FIGHT=1203,PLAYER_LEVEL=203,
					COMMAND_LEVEL=204,INVITE=1204,ALLIANCE_OFFER=1205,
					ATTACK_BOSS=1206,KILL_BOSS=1207,ALLIANCE_OFFER_ONE_DAY=602,
					ARENA_RANK=603,MOUTH_CARD=604,HONOR_SCORE=605,FIGHT_SCORE=606;
	/** 属性分界值 */
	public static final int ATR_TYPE1=500,ATR_TYPE2=1000;
	/** 基本分类常量 */
	public static final int BASE=1,ARMY=2,HONOR=3,OTHER=4;
	
	/** 属性key值 */
	int atrKey;
	/** 需要完成值 */
	int[] needValue;
	 /** 对应奖励Sid */
	int[] awardSids;
	/** 各阶段总积分 */
	int[] score={1,2,3,4,5};
	/** 对应的奖励头像 */
	int[] headSids;
	/** 所属分类 */
	int baseType;
	/** 排序值 */
	int sort;
	/** 领奖清零 */
	boolean awardClear;
	/** 满值采集 */
	boolean fullCollect=true;
	
	public boolean canAddValue(Player player)
	{
		int progress=player.getAchieveProgress(getSid());
		long cvalue=player.getAchieveValue(atrKey);
		if(!fullCollect)
		{
			if(progress>=needValue.length||cvalue>=needValue[progress])
				return false;
		}
		return true;
	}
	public int getNeedValue(int progress)
	{
		if(progress>=needValue.length) return needValue[needValue.length-1];
		return needValue[progress];
	}
	public int getAwardSid(int progress)
	{
		if(progress>=awardSids.length) return awardSids[awardSids.length-1];
		return awardSids[progress];
	}
	public int computeScore(int pg,long cv)
	{
		if(pg>=needValue.length) return score[score.length-1];
		if(cv<needValue[pg]) pg--;
		if(pg<0) return 0;
		return score[pg];
	}
	public int getMaxScore()
	{
		return score[score.length-1];
	}
	/** 获取成就增量积分 */
	public int getAddScore(int pg)
	{
		if(pg==0)return score[pg];
		return score[pg]-score[pg-1];
	}
	
	public int getAtr_key()
	{
		return atrKey;
	}
	
	public void setAtr_key(int atr_key)
	{
		this.atrKey=atr_key;
	}
	
	public int[] getNeedValue()
	{
		return needValue;
	}
	
	public void setNeedValue(int[] needValue)
	{
		this.needValue=needValue;
	}
	
	public int[] getAwardSid()
	{
		return awardSids;
	}
	
	public void setAwardSid(int[] awardSid)
	{
		this.awardSids=awardSid;
	}
	
	public int[] getHeadSids() {
		return headSids;
	}

	public void setHeadSids(int[] headSids) {
		this.headSids = headSids;
	}

	public int getHeadSid(int progress) {
		if (headSids == null) {
			return -1;
		}
		if (progress >= headSids.length) {
			return headSids[headSids.length - 1];
		}
		return headSids[progress];
	}

	public int getAchieveType() {
		return baseType;
	}
	
}
