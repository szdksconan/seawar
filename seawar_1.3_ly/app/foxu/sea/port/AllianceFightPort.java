package foxu.sea.port;

import foxu.ds.PlayerKit;
import foxu.sea.ContextVarManager;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.kit.SeaBackKit;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;

/**
 * 盟战端口
 * 
 * @author yw
 * 
 */
public class AllianceFightPort extends AccessPort
{

	/**
	 * DINATE捐献,GET_EVNET获取本盟事件,GET_ALL_EVENT获取占领事件,FIRE出征,RETREAT撤离,
	 * GET_RANK获取捐献排名,GET_RANK_DETAIL获取捐献详细,GET_RECORD获取捐献记录,
	 * SET_REINFORCE设置自动出兵,GET_FLEET获取库存舰船+据点信息,UP_SHIP升级船只,
	 * CLEAR_CD秒CD GET_FIGHT获取战报 GET_GROUND_FLEET 获取据点部队 REINFORCE补兵
	 * */
	public static final int DINATE=1,GET_EVNET=2,GET_ALL_EVENT=3,FIRE=4,
					RETREAT=5,GET_RANK=6,GET_RANK_DETAIL=7,GET_RECORD=8,
					SET_REINFORCE=9,GET_FLEET=10,UP_SHIP=11,CLEAR_CD=12,
									GET_FIGHT=13,GET_GROUND_FLEET=14,REINFORCE=15;//GET_UPSHIP=16;

	AllianceFightManager aManager;

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		String check=checkAlevel(player);
		if(check!=null)
		{
			throw new DataAccessException(0,check);
		}
		int type=data.readUnsignedByte();
		String result=null;
		switch(type)
		{
			case DINATE:
				String checkString=SeaBackKit.checkChatOpen(
					ContextVarManager.ALLIANCE_SHIP_DONATE_LEVEL_LIMIT,player,
					"alliance_ship_donate_level_limit");
				if(checkString!=null)
					throw new DataAccessException(0,checkString);
				result=aManager.dinateShip(player,data);
				break;
			case GET_EVNET:
				result=aManager.getFightEvent(player,data);
				break;
			case GET_ALL_EVENT:
				result=aManager.getAllEvent(data);
				break;
			case FIRE:
				result=aManager.occupyBattleGround(player,data);
				break;
			case RETREAT:
				result=aManager.retreatGround(player,data);
				break;
			case GET_RANK:
				result=aManager.getRanksByPage(player,data);
				break;
			case GET_RANK_DETAIL:
				result=aManager.getDiantionByName(player,data);
				break;
			case GET_RECORD:
				result=aManager.getRecordByPage(player,data);
				break;
			case SET_REINFORCE:
				result=aManager.setRecruit(player,data);
				break;
//			case GET_FLEET:
//				result=aManager.getStockFleet(player,data);
//				break;
			case UP_SHIP:
				result=aManager.upShip(player,data);
				break;
			case CLEAR_CD:
				result=aManager.clearCD(player,data);
				break;
			case GET_FIGHT:
				result=aManager.getFightData(player,data);
				break;
			case GET_GROUND_FLEET:
				result=aManager.getGroundFleet(player,data);
				break;
			case REINFORCE:
				result=aManager.doRecruit(player,data);
				break;
//			case GET_UPSHIP:
//				result=aManager.getUpShip(player,data);
//				break;
			default:
				break;
		}
		if(result!=null)
		{
			throw new DataAccessException(0,result);
		}
		return data;
	}

	/** 检测联盟等级 */
	public String checkAlevel(Player player)
	{
		String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(aid==null||aid.equals(""))return "no alliance";
		int id=Integer.parseInt(aid);
		Alliance alliance=(Alliance)aManager.getAlliance(id);
		if(alliance==null)return "no alliance";
//		if(alliance.getAllianceLevel()<AllianceFight.LEVEL_LIMIT)return "alliance limit level 10";
		return null;
	}
	public AllianceFightManager getAllianceFightManager()
	{
		return aManager;
	}

	public void setAllianceFightManager(
		AllianceFightManager allianceFightManager)
	{
		aManager=allianceFightManager;
	}

}
