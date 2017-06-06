package foxu.sea.arena;

import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;

/**
 * ���������࣬��������;�̬����
 * 
 * @author comeback
 * 
 */
public final class ArenaHelper
{

	/** ���а�ÿ�λ�ȡ��ҳ�������� */
	public static final int RANK_COUNT_PER_PAGE=20;

	/** ��ս���ʱ�� */
	public static final int BATTLE_INTERAL_TIME=10*60;

	public static final int DAILY_AWARD_MAX_RANKING=100;
	
	/** ɾ������ʱ�� */
	public static final int REMOVE_REPORT_TIME=24*3600;
	
	/** ����������� */
	public static final int MAX_REPORT_COUNT=10;
	
	/** ��ս�������ϵ�� */
	public static final float[] RANKING_RATE={0.98f,0.96f,0.94f,0.92f,0.9f};
	
	/** ��ս������VIP�ȼ���Ӧ��ϵ */
	public static int[] COUNT_VIP_LEVEL={5,6,7,8,9,10,11,12,13,14};

	/**
	 * ͨѶЭ��
	 */
	public static final int GET_MAIN_INFO=1,GET_RANK_INFO=2,GET_AWARD=3,
					APPLY_FIGHT=4,DEPLOY_FLEET=5,SPEED_UP=6,GET_REPORT=14,
					CLEAR_REPORTS=15,
					GET_REPORT_COUNT=17,GET_RETPORT_CONTENT=16,REMOVE_REPORT=18,
					VIEW_AWARD=19;

	/**
	 * ����ָ�������ģ���k�����ֵ�����,ǰ5�����⴦��
	 * 
	 * @param ranking
	 * @param k ��k������,k�ķ�Χ[1,5]
	 * @return
	 */
	public static int calculateRival(int ranking,int k)
	{
		int rivalRanking=0;
		if(ranking<=5)
		{
			if(ranking<=k) return k+1;
			return k;
		}
		// 6-k ��ȡ���θߵ�
		k=6-k;
		// +0.9f ����ȡ������
		rivalRanking=(int)(RANKING_RATE[k-1]*ranking +0.9f)-k;
		return rivalRanking;
	}

	/**
	 * ��ָ����ҵ�������Ϣд��bytebuffer
	 * 
	 * @param gladiator
	 * @param data
	 */
	public static void writeRanking(SeawarGladiator gladiator,
		ByteBuffer data,CreatObjectFactory objectFactory)
	{
		Player player=objectFactory.getPlayerById(gladiator.getPlayerId());
		data.writeInt(gladiator.getRanking());
		data.writeUTF(player.getName());
		data.writeByte(player.getLevel());
		data.writeInt(player.getFightScore());
	}

	/**
	 * �������1�Ƿ�����ս����2
	 * 
	 * @param ranking1
	 * @param ranking2
	 * @return
	 */
	public static boolean checkBattleRanking(int ranking1,int ranking2)
	{
		for(int i=1;i<=5;i++)
		{
			int ranking=ArenaHelper.calculateRival(ranking1,i);
			if(ranking==ranking2) return true;
		}
		return false;
	}

	/**
	 * Ϊָ������Ҵ�������Ⱥ
	 * 
	 * @param gladiator
	 * @param objectFactory
	 * @return
	 */
	public static FleetGroup createFleetGroup(SeawarGladiator gladiator,
		CreatObjectFactory objectFactory)
	{
		FleetGroup group=new FleetGroup();
		Player player=objectFactory.getPlayerById(gladiator.getPlayerId());
		group.getOfficerFleetAttr().initOfficers(player);
		for(int i=0;i<SeawarGladiator.FLEET_MAX_COUNT;i++)
		{
			int sid=gladiator.getShipSidByIndex(i);
			int count=gladiator.getShipCountByIndex(i);
			if(sid>0&&count>0)
			{
				Ship ship=(Ship)Ship.factory.newSample(sid);
				Fleet fleet=new Fleet(ship,count);
				fleet.setLocation(i);
				fleet.setPlayter(player);
				group.setFleet(i,fleet);
			}

		}
		return group;
	}

	/**
	 * ����ָ����������ս������
	 * 
	 * @param ranking ��ս�����������
	 * @return
	 */
	public static int getAwardSid(int ranking)
	{
		if(PublicConst.ARENA_BATTLE_AWARDS.length!=PublicConst.ARENA_BATTLE_RANKS.length)
		{
			return PublicConst.ARENA_BATTLE_AWARDS[PublicConst.ARENA_BATTLE_AWARDS.length-1];
		}
		int sid=0;
		for(int i=PublicConst.ARENA_BATTLE_RANKS.length-1;i>=0;i--)
		{
			if(ranking<PublicConst.ARENA_BATTLE_RANKS[i])
			{
				sid=PublicConst.ARENA_BATTLE_AWARDS[i];
				break;
			}
		}
		return sid;
	}

	/**
	 * ��ȡָ����ҵ�ÿ�������ս����
	 * 
	 * @param player
	 * @return
	 */
	public static int getMaxBattleCount(Player player)
	{
		int vipLevel=player.getUser_state();

		if(vipLevel>=COUNT_VIP_LEVEL.length)
			vipLevel=COUNT_VIP_LEVEL.length-1;
		return COUNT_VIP_LEVEL[vipLevel];
	}

}
