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
 * ����ս��������
 * 
 * @author Alan
 * 
 */
public class LeagueFightManager
{
	public static final int LEAGUE_FIGHT_AWARD=65534;
	/** ���ֹ�ʽ������׼ֵ */
	public static float[] FIGHT_GOAL_SUCCESS_BASE={80f,90f,100f};
	/** ���ֹ�ʽ���� */
	public static float FIGHT_GOAL_FAIL=15f,
					FIGHT_GOAL_SUCCESS_BASE_PERCENT=0.9f,
					FIGHT_GOAL_SUCCESS_TARGET=5f,FIGHT_GOAL_MAX=150f;

	/**
	 * ������ս
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
		// �Ƿ�ƥ��
		String msg=clm.checkTargetCanFight(targetId,attacker);
		if(msg!=null) return msg;
		attacker.setPlayer(null);
		attacker.initCrossAttrs(player);
		FleetGroup fleetGroup=attacker.createFleetGroup(attacker.getAttacklist());
		if(fleetGroup==null) return "not set fleet yet";
		FleetGroup beAttackGroup=beAttackPlayer
			.createFleetGroup(beAttackPlayer.getAttacklist());
		// ����ս��
		FightScene scene=FightSceneFactory.factory.create(fleetGroup,
			beAttackGroup);
		// ��ʼս��
		FightShowEventRecord record=FightSceneFactory.factory.fight(scene,
			null);
		// ���ӽ������ս����
		attacker.incrTodayBattleCount();
		// ���������ʤ��
		boolean isSuccess=scene.getSuccessTeam()==0;
		// ��Ҫ�����ж�,�����������Ϊ�б����һ���������Ҳ���Ŀ��λ�õ����
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
		// ͬ����������
		award.setLeagueCoin(goal);
		award.awardLenth(data,player,objectFactory,null,null);
		JBackKit.sendCrossLeaguePlayerInfo(objectFactory,player,clm,time);
		// ս�����
		data.writeBoolean(isSuccess);
		// ����ս��
		SeaBackKit.conFightRecord(data,record.getRecord(),player.getName(),
			player.getLevel(),beAttackPlayer.getName(),
			beAttackPlayer.getLevel(),PublicConst.FIGHT_TYPE_20,attacker.getPlayer(),
			beAttackPlayer.getPlayer(),fleetGroup,beAttackGroup,true,null,
			null);
		return null;
	}
	/**
	 * ���𽢶�
	 * 
	 * @param player
	 * @param list ����λ�ã���ֻSID����ֻ������һ�������б�
	 * @param length �б��еĽ�������
	 * @return
	 */
	public String deployFleet(Player player,ClientLeagueManager clm,
		ByteBuffer data,CreatObjectFactory objectFactory)
	{
		IntList list=new IntList();
		// ��齢���Ƿ������
		FleetGroup mainGroup=player.getIsland().getMainGroup();
		ByteBuffer tempData=(ByteBuffer)data.clone();
		int length=tempData.readUnsignedByte();
		String err=SeaBackKit.checkShipNumLimit(list,length,tempData,player,
			mainGroup,0);
		if(err!=null) return err;
		LeaguePlayer lp=clm.getLeaguePlayer(player);
		list.clear();
		// ս��������Ҫ����ֻ�б��Ϊ2λ1��,��Ҫ��λ��Ϣ
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

	/** ��ȡս������ */
	public static int getFightLeagueGoal(int baseIndex,float attackerFS,
		int targetFS,boolean isSuccess)
	{

		int goal=(int)FIGHT_GOAL_FAIL;
		if(isSuccess&&attackerFS>0)
		{
			// ��ʱȡ������ֵ
			// 100*[�Է�ս��/(�Լ�ս��*0.9)]+15
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
