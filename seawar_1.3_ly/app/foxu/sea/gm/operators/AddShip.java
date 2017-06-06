package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.set.IntList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.Role;
import foxu.sea.Ship;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.shipdata.ShipCheckData;

/**
 * 增加船只
 * @author comeback
 *
 */
public class AddShip extends GMOperator
{
	
	/** 船舰类型和等级对应的sid */
	public final static int SHIPS_SIDS[][]={
				{10001,10002,10003,10004,10005,10006,10007,10008,10009},
				{10011,10012,10013,10014,10015,10016,10017,10018,10019},
				{10021,10022,10023,10024,10025,10026,10027,10028,10029},
				{10031,10032,10033,10034,10035,10036,10037,10038,10039},
				{10071,10072,10073,10074,10075}
				};
	
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		String shipType=params.get("ship_type");
		if(shipType==null||shipType.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		String shipLevel=params.get("ship_level");
		if(shipLevel==null||shipLevel.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		String countStr=params.get("count");
		if(countStr==null||countStr.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		int sid=SHIPS_SIDS[Integer.parseInt(shipType)-1][Integer
			.parseInt(shipLevel)-1];
		Ship ship=(Ship)Role.factory.newSample(sid);
		if(ship==null) 
			return GMConstant.ERR_SHIP_IS_NULL;
		int count=Integer.parseInt(countStr);
		
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		if(count>0)
			player.getIsland().addTroop(sid,count,
				player.getIsland().getTroops());
		else
		{
			count=Math.abs(count);
			player.getIsland().reduceTroop(sid,count,
				player.getIsland().getTroops());
		}
		JBackKit.sendResetTroops(player);
		// 船只日志
		IntList fightlist=new IntList();
		fightlist.add(sid);
		fightlist.add(count);
		objectFactory.addShipTrack(0,ShipCheckData.GM_SEND,player,
			fightlist,null,false);
		return GMConstant.ERR_SUCCESS;
	}

}
