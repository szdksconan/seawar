package foxu.sea.achieve;

import mustang.set.IntList;
import mustang.util.Sample;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.Resources;
import foxu.sea.Ship;
import foxu.sea.builds.Build;
import foxu.sea.builds.Island;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.checkpoint.SelfCheckPoint;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;

/**
 * �ɾ����ݲɼ�������
 * 
 * @author yw
 * 
 */
public class AchieveCollect
{

	/** �ж��Ƿ������ֵ */
	public static boolean canAddValue(int atrKey,Player player,Object other)
	{
		Object obj=AchieveManager.instance.key_sid.get(atrKey);
		if(obj==null) return false;
		IntList sids=(IntList)obj;
		for(int i=0;i<sids.size();i++)
		{
			int sid=sids.get(i);
			Sample wildment=Achievement.factory.getSample(sid);
			if(wildment instanceof WildAchievement)
			{
				if(((WildAchievement)wildment).canAddValue((NpcIsland)other,
					player)) return true;
			}
			else if(wildment instanceof KillShipAchievement)
			{
				if(((KillShipAchievement)wildment).canAddValue((Ship)other,
					player)) return true;
			}
			else if(wildment instanceof ArenaRankAchievement)
			{
				if(((ArenaRankAchievement)wildment).canAddValue((Integer)other,
					player)) return true;
			}
			else if(wildment instanceof Achievement)
			{
				if(((Achievement)wildment).canAddValue(player)) return true;
			}
		}
		return false;

	}

	/** �����ȼ� */
	public static void buildLevel(PlayerBuild playerBuild,Island island,
		Player player)
	{
		if(playerBuild==null) return;
		if(playerBuild.getBuildType()==Build.BUILD_DIRECTOR)
		{
			AchieveManager.instance.pushAchieveValue(
				Achievement.DIRECTOR_BUILD,playerBuild.getBuildLevel(),
				player);
		}
		else if(playerBuild.getBuildType()==Build.BUILD_SHIP)
		{
			PlayerBuild build=island.getBuildByType(Build.BUILD_SHIP,null);
			AchieveManager.instance.pushAchieveValue(Achievement.SHIP_BUILD,
				build.getBuildLevel(),player);

		}
		else if(playerBuild.getBuildType()==Build.BUILD_RESEARCH)
		{
			AchieveManager.instance.pushAchieveValue(Achievement.TECH_BUILD,
				playerBuild.getBuildLevel(),player);
		}

	}

	/** �����ȼ� */
	public static void honorLevel(int level,Player player)
	{
		AchieveManager.instance.pushAchieveValue(Achievement.HONOR_LEVEL,
			level,player);
	}

	/** ��Դ */
	public static void resourceStock(Player player)
	{
		if(player==null) return;
		long[] resource=player.getResources();
		AchieveManager.instance.pushAchieveValue(Achievement.MONEY,
			resource[Resources.MONEY],player);
		AchieveManager.instance.pushAchieveValue(Achievement.METAL,
			resource[Resources.METAL],player);
		AchieveManager.instance.pushAchieveValue(Achievement.OIL,
			resource[Resources.OIL],player);
		AchieveManager.instance.pushAchieveValue(Achievement.SILICA,
			resource[Resources.SILICON],player);
		AchieveManager.instance.pushAchieveValue(Achievement.URANIUM,
			resource[Resources.URANIUM],player);
	}
	/** �½� */
	public static void chapterLevel(Player player)
	{
		SelfCheckPoint selfPoint=player.getSelfCheckPoint();
		CheckPoint can=(CheckPoint)CheckPoint.factory.getSample(selfPoint
			.getCheckPointSid());
		int chapter=can.getChapter();
		if(can.getNextSid()!=can.getSid())
		{
			chapter--;
		}
		AchieveManager.instance.pushAchieveValue(Achievement.CHAPTER,
			chapter,player);
	}

	/** ����Ұ�� */
	public static void attackNpc(NpcIsland beIsland,Player player)
	{
		if(!canAddValue(Achievement.WILD,player,beIsland)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.WILD,1,player);
	}

	/** ������� */
	public static void attackPlayer(Player player)
	{
		if(!canAddValue(Achievement.ATTACK_PLAYR,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.ATTACK_PLAYR,1,
			player);
	}

	/** ������ҵĴ� */
	public static void perishShip(FleetGroup fgroup,Player player)
	{
		Fleet[] fleets=fgroup.getArray();
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			int lost=fleets[i].lostNum();
			if(lost<=0) continue;
			Ship ship=fleets[i].getShip();
			if(ship.getPlayerType()==Ship.BATTLE_SHIP)
			{
				if(!canAddValue(Achievement.BATTLE_SHIP,player,ship))
					continue;
				AchieveManager.instance.pushAchieveValue(
					Achievement.BATTLE_SHIP,lost,player);
			}
			else if(ship.getPlayerType()==Ship.SUBMARINE_SHIP)
			{
				if(!canAddValue(Achievement.SUBMARINE,player,ship)) continue;
				AchieveManager.instance.pushAchieveValue(
					Achievement.SUBMARINE,lost,player);
			}
			else if(ship.getPlayerType()==Ship.CRUISER_SHIP)
			{
				if(!canAddValue(Achievement.CRUISER,player,ship)) continue;
				AchieveManager.instance.pushAchieveValue(
					Achievement.CRUISER,lost,player);
			}
			else if(ship.getPlayerType()==Ship.AIRCRAFT_SHIP)
			{
				if(!canAddValue(Achievement.CARRIER,player,ship)) continue;
				AchieveManager.instance.pushAchieveValue(
					Achievement.CARRIER,lost,player);
			}
		}

	}
	
	/**
	 * �¿�����
	 */
	public static void mouthCard(int addValue,Player player){
		//if(!canAddValue(Achievement.MOUTH_CARD,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.MOUTH_CARD,
			1,player);
	}

	/** �ۼƳ�ֵ */
	public static void gemsStock(int addValue,Player player)
	{
		if(!canAddValue(Achievement.RECHARGE,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.RECHARGE,
			addValue,player);
	}

	/** �ﶨ�˺� */
	public static void bindUser(Player player)
	{
		AchieveManager.instance.pushAchieveValue(Achievement.BIND,1,player);
	}

	/** ������� */
	public static void inviteUser(Player player)
	{
		if(!canAddValue(Achievement.INVITE,player,null)) return;
		AchieveManager.instance
			.pushAchieveValue(Achievement.INVITE,1,player);
	}

	/** ������½ */
	public static void seriesLogin(int value,Player player)
	{
		AchieveManager.instance.pushAchieveValue(Achievement.SERIERS_LOGIN,
			value,player);
	}
	/** ��Ϸ���� */
	public static void shareGame(Player player)
	{
		if(!canAddValue(Achievement.SHARE_GAME,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.SHARE_GAME,1,
			player);
	}

	/** ����ս�� */
	public static void shareFightData(Player player)
	{
		if(!canAddValue(Achievement.SHARE_FIGHT,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.SHARE_FIGHT,1,
			player);
	}

	/** ��ҵȼ� */
	public static void playerLevel(Player player)
	{
		AchieveManager.instance.pushAchieveValue(Achievement.PLAYER_LEVEL,
			player.getLevel(),player);
	}

	/** ͳ���ȼ� */
	public static void commandLevel(Player player)
	{
		AchieveManager.instance.pushAchieveValue(Achievement.COMMAND_LEVEL,
			player.getCommanderLevel(),player);
	}
	
	/** �˹��� */
	public static void allianceOffer(int addValue,Player player)
	{
		if(!canAddValue(Achievement.ALLIANCE_OFFER,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.ALLIANCE_OFFER,
			addValue,player);
	}
	
	/** ÿ���˹��� */
	public static void allianceOfferForOneDay(int addValue,Player player)
	{	
		if(!canAddValue(Achievement.ALLIANCE_OFFER_ONE_DAY,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.ALLIANCE_OFFER_ONE_DAY,
			addValue,player);
	}
	
	/**��������*/
	public static void honorScore(int addValue,Player player){
		AchieveManager.instance.pushAchieveValue(Achievement.HONOR_SCORE,
			addValue,player);
	}
	
	/** ���������� */
	public static void arenaRank(int addValue,Player player){
		if(!canAddValue(Achievement.ARENA_RANK,player,addValue)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.ARENA_RANK,
			1,player);
	}
	
	/**ս��*/
	public static void FightScore(long addValue,Player player){
		AchieveManager.instance.pushAchieveValue(Achievement.FIGHT_SCORE,
			addValue,player);
	}

	/** ����Boss */
	public static void attackBoss(Player player)
	{
		if(!canAddValue(Achievement.ATTACK_BOSS,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.ATTACK_BOSS,1,
			player);
	}

	/** ��ɱBoss */
	public static void killBoss(Player player)
	{
		if(!canAddValue(Achievement.KILL_BOSS,player,null)) return;
		AchieveManager.instance.pushAchieveValue(Achievement.KILL_BOSS,1,
			player);
	}

}
