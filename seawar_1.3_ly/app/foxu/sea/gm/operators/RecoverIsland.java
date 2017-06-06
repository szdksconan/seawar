package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.set.ArrayList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.event.FightEvent;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;

/**
 * 回收玩家岛屿
 * @author yw
 *
 */
public class RecoverIsland extends GetPlayerInfo
{
	CreatObjectFactory objectFactory;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerInfo=params.get("player_info");
		String infoType=params.get("info_type");
		if(playerInfo==null||infoType==null||playerInfo.length()==0||infoType.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		
		objectFactory=info.getObjectFactory();
		Player[] players=null;
		if("1".equals(infoType))
			players=getPlayersByName(playerInfo,objectFactory);
		else if("2".equals(infoType))
			players=getPlayersByAccount(playerInfo,objectFactory);
		else if("3".equals(infoType))
			players=getPlayersByUDID(playerInfo,objectFactory);
		else
			return GMConstant.ERR_UNKNOWN;
		for(int i=0;i<players.length;i++)
		{
			NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(players[i].getId());
			if(island!=null)
			{
				ArrayList list=objectFactory.getEventCache().getFightEventListById(island.getIndex());
				clearEvent(list,players[i],objectFactory);
				objectFactory.getIslandCache().removePlayerIslandMap(players[i].getId());
				island.setPlayerId(0);
				objectFactory.getIslandCache().getDbaccess().save(island);
				objectFactory.getIslandCache().putSpaceIsland(island);
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
	
	public void clearEvent(ArrayList list,Player player,CreatObjectFactory factory)
	{
		if(list==null)return;
		for(int i=0;i<list.size();i++)
		{
			FightEvent event=(FightEvent)list.get(i);
			if(event==null) continue;
			if(event.getPlayerId()==player.getId())
			{
				int bindex=event.getAttackIslandIndex();
				NpcIsland beisland=factory.getIslandByIndex(bindex+"");
				ArrayList belist=factory.getEventCache().getFightEventListById(bindex);
				Player beplayer=factory.getPlayerById(beisland.getPlayerId());
				if(belist!=null&&beplayer!=null)
				{
					belist.remove(event);
					JBackKit.deleteFightEvent(beplayer,event);
				}
				if(event.getFleetGroup()!=null)
				{
					event.getFleetGroup().cancel(player,false);
				}
				event.setDelete(FightEvent.DELETE_TYPE);
				// 加入改变列表 等待储存
				factory.getEventCache().load(event.getId()+"");
				JBackKit.deleteFightEvent(player,event);
			}
		}
		list.clear();
	}

}
