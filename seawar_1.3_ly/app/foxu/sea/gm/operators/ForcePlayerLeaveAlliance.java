package foxu.sea.gm.operators;

import java.util.Map;

import mustang.util.TimeKit;
import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.alliance.alliancebattle.AllianceBattleFight;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/***
 * ǿ��������˳�����
 * 
 * @author lhj
 * 
 */
public class ForcePlayerLeaveAlliance extends GMOperator
{

	/** ��ս */
	AllianceFightManager afightManager;
	/**������ս**/
	AllianceBattleFight bFight;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.loadOnly(player.getAttributes(PublicConst.ALLIANCE_ID));
		if(alliance==null)
		{
			return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
		}
		if(checkLeaveAlliance(alliance,player.getId()))
			return GMConstant.ERR_JOIN_ALLAINCE_FIGHT;
		if(alliance.getMasterPlayerId()==player.getId())
			return GMConstant.ERR_PLAYER_IS_MASTER;
		if(!alliance.isHavePlayer(player.getId()))
			return GMConstant.ERR_SUCCESS;
		// �����¼�
		AllianceEvent event=new AllianceEvent(
			AllianceEvent.ALLIANCE_EVENT_PLAYER_LEFT,player.getName(),
			player.getName(),"",TimeKit.getSecondTime());
		alliance.addEvent(event);
		// ��ս���
		afightManager.exitAlliance(player,alliance.getId());
		alliance.removePlayerId(player.getId());
		player.setAttribute(PublicConst.ALLIANCE_ID,null);
		player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
		player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
		player.getAllianceList().clear(); 
		player.resetAdjustment();
		//��¼��ǰ��ҵ�����ֵ
		SeaBackKit.leaveAllianceRecord(player,alliance);
		//�Ƴ���ҵ�����������Ϣ
		SeaBackKit.removeAllianceRank(player,alliance);
		return GMConstant.ERR_SUCCESS;
	}

	/**��֤����Ƿ����������ս**/
	public boolean checkLeaveAlliance(Alliance alliance,int playerId)
	{
		if(alliance.getBetBattleIsland()==0) return false;
		BattleIsland battleIsland=bFight.getBattleIslandById(alliance.getBetBattleIsland(),false);
		if(battleIsland==null) return false;
		if(bFight.getAllianceStage().getStage()<Stage.STAGE_THREE)
			return false;
		return battleIsland.isHavePlayer(playerId);
	}
	
	public void setAfightManager(AllianceFightManager afightManager)
	{
		this.afightManager=afightManager;
	}

	
	public void setbFight(AllianceBattleFight bFight)
	{
		this.bFight=bFight;
	}

	
	
}
