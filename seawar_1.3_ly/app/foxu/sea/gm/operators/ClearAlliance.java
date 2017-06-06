package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.set.IntList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.AllianceBattleFight;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.AlliancePort;

/***
 * gm工具清除联盟
 * 
 * @author lhj
 * 
 */
public class ClearAlliance extends GMOperator
{

	/** 盟战管理器 **/
	AllianceFightManager afightManager;
	/**新的联盟战**/
	AllianceBattleFight bFight;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String alliance_name=params.get("alliance_name");
		Alliance alliance=info.getObjectFactory().getAllianceMemCache()
			.loadByName(alliance_name,false);
		if(alliance==null)
		{
			return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
		}
		if(checkDisAlliance(alliance))
		{
			return GMConstant.ERR_ALLIANCE_FIGHT_IS_OVER;
		}
		setAllAllianceGiveValue(alliance,info.getObjectFactory());
		alliance.dismiss(info.getObjectFactory());
		AlliancePort alliancePort=(AlliancePort)BackKit.getContext().get(
			"alliancePort");
		alliancePort.flushFileds(true);
		afightManager.dismissAlliance(alliance);
		return GMConstant.ERR_SUCCESS;
	}

	public void setAfightManager(AllianceFightManager afightManager)
	{
		this.afightManager=afightManager;
	}
	
	/**解散联盟需要验证是否可以解散**/
	public boolean checkDisAlliance(Alliance alliance)
	{
		if(alliance.getBetBattleIsland()==0) return false;
		BattleIsland battleIsland=bFight.getBattleIslandById(alliance
			.getBetBattleIsland(),false);
		if(battleIsland==null) return false;
		int stage=bFight.getAllianceStage().getStage();
		if(stage==Stage.STAGE_ONE) return false;
		return true;
	}
	
	/**设置全联盟的捐献值**/
	public void setAllAllianceGiveValue(Alliance alliance,CreatObjectFactory factory)
	{
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=factory.getPlayerById(list.get(i));
			if(player==null) continue;
			SeaBackKit.leaveAllianceRecord(player,alliance);
		}
	}

	
	public void setbFight(AllianceBattleFight bFight)
	{
		this.bFight=bFight;
	}
	
	
}
