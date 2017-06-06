package foxu.cross.goalleague;

import mustang.io.ByteBuffer;
import mustang.set.IntList;
import foxu.cross.war.CrossWarPlayer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.FightScene;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.award.Award;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

/**
 * 联赛战斗管理器
 * 
 * @author Alan
 * 
 */
public class LeagueFightManager
{
	public static final int LEAGUE_FIGHT_AWARD=65534;
	/** 积分公式常量基准值 */
	public static float[] FIGHT_GOAL_SUCCESS_BASE={80f,90f,100f};
	/** 积分公式常量 */
	public static float FIGHT_GOAL_FAIL=15f,
					FIGHT_GOAL_SUCCESS_BASE_PERCENT=0.9f,
					FIGHT_GOAL_SUCCESS_TARGET=5f,FIGHT_GOAL_MAX=150f;

	/**
	 * 申请挑战
	 * 
	 * @param player
	 * @param attackName
	 * @return
	 */
	public String applyFight(Player player,int targetId,int time,
		ClientLeagueManager clm,ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		if(player.getLevel()<ServerLeagueManager.COPY_LIMIT_LV)
			return "level_not_enough";
		CrossWarPlayer beAttackPlayer=clm.getCrossWarPlayer(targetId);
		LeaguePlayer attacker=clm.getLeaguePlayer(player);
		if(beAttackPlayer==null) return "league target not exist";
		// 是否匹配
		String msg=clm.checkTargetCanFight(targetId,attacker);
		if(msg!=null) return msg;
		attacker.setPlayer(null);
		attacker.initCrossAttrs(player);
		FleetGroup fleetGroup=attacker.createFleetGroup(attacker.getAttacklist());
		if(fleetGroup==null) return "not set fleet yet";
		FleetGroup beAttackGroup=beAttackPlayer
			.createFleetGroup(beAttackPlayer.getAttacklist());
		// 创建战斗
		FightScene scene=FightSceneFactory.factory.create(fleetGroup,
			beAttackGroup);
		// 开始战斗
		FightShowEventRecord record=FightSceneFactory.factory.fight(scene,
			null);
		// 增加今天的挑战次数
		attacker.incrTodayBattleCount();
		// 如果进攻方胜利
		boolean isSuccess=scene.getSuccessTeam()==0;
		// 需要首先判断,否则如果本次为列表最后一次则会出现找不到目标位置的情况
		int baseIndex=clm.getChallegeTargetIndex(targetId,player);
		if(isSuccess)
		{
			attacker.resetTargetStaut(targetId,
				LeaguePlayer.CHALLENGE_SUCCESS);
			clm.getCurrentChallengeList(player);
			JBackKit.sendCrossLeagueChallengeList(player,clm);
		}
		int goal=getFightLeagueGoal(baseIndex,attacker.getFightscore(),
			beAttackPlayer.getFightscore(),isSuccess);
		int awardSid=getAwardSid(player.getFightScore()
			-beAttackPlayer.getFightscore());
		Award award=(Award)Award.factory.newSample(awardSid);
		award.setLeagueGoal(goal);
		// 同等数量代币
		award.setLeagueCoin(goal);
		award.awardLenth(data,player,objectFactory,null,null);
		JBackKit.sendCrossLeaguePlayerInfo(objectFactory,player,clm,time);
		// 战斗结果
		data.writeBoolean(isSuccess);
		// 计算战斗
		SeaBackKit.conFightRecord(data,record.getRecord(),player.getName(),
			player.getLevel(),beAttackPlayer.getName(),
			beAttackPlayer.getLevel(),PublicConst.FIGHT_TYPE_20,attacker.getPlayer(),
			beAttackPlayer.getPlayer(),fleetGroup,beAttackGroup,true,null,
			null);
		return null;
	}
	/**
	 * 部署舰队
	 * 
	 * @param player
	 * @param list 包含位置，船只SID，船只数量的一个舰队列表
	 * @param length 列表中的舰队数量
	 * @return
	 */
	public String deployFleet(Player player,ClientLeagueManager clm,
		ByteBuffer data,CreatObjectFactory objectFactory)
	{
		IntList list=new IntList();
		// 检查舰队是否可设置
		FleetGroup mainGroup=player.getIsland().getMainGroup();
		ByteBuffer tempData=(ByteBuffer)data.clone();
		int length=tempData.readUnsignedByte();
		String err=SeaBackKit.checkShipNumLimit(list,length,tempData,player,
			mainGroup,0);
		if(err!=null) return err;
		LeaguePlayer lp=clm.getLeaguePlayer(player);
		list.clear();
		// 战力计算需要将船只列表变为2位1组,不要坑位信息
		IntList tempList=new IntList();
		length=data.readUnsignedByte();
		for(int i=0;i<length;i++)
		{
			int location=data.readUnsignedByte();
			int shipSid=data.readUnsignedShort();
			int num=data.readUnsignedShort();
			tempList.add(shipSid);
			tempList.add(num);
			list.add(shipSid);
			list.add(num);
			list.add(location);
		}
		lp.setAttacklist(list);
		lp.setFightscore(SeaBackKit.getPlayerFightScroe(player,
			objectFactory,tempList,null,true));
		return null;
	}

	/** 获取战斗积分 */
	public static int getFightLeagueGoal(int baseIndex,float attackerFS,
		int targetFS,boolean isSuccess)
	{

		int goal=(int)FIGHT_GOAL_FAIL;
		if(isSuccess&&attackerFS>0)
		{
			// 暂时取消基础值
			// 100*[对方战力/(自己战力*0.9)]+15
			goal=Math
				.round(FIGHT_GOAL_SUCCESS_BASE[FIGHT_GOAL_SUCCESS_BASE.length-1]
					*(targetFS*FIGHT_GOAL_SUCCESS_BASE_PERCENT)
					/attackerFS
					+FIGHT_GOAL_FAIL);
			goal=goal>0?goal:0;
		}
		return goal>(int)FIGHT_GOAL_MAX?(int)FIGHT_GOAL_MAX:goal;
	}

	public int getAwardSid(int fightScoreDistance)
	{
		return LEAGUE_FIGHT_AWARD;
	}
}
