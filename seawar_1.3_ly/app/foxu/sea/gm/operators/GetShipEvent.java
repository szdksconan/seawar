package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.Ship;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.shipdata.ShipCheckData;
import foxu.sea.shipdata.ShipDataEvent;

/**
 * 获取船只日志
 * @author comeback
 *
 */
public class GetShipEvent extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String checkTimeStr=params.get("check_time");
		String playerName=params.get("player_name");
		String shipType=params.get("type");
		
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,false);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		
		int day=0;
		if(checkTimeStr!=null && TextKit.valid(checkTimeStr,TextKit.NUMBER)==0)
			day=Integer.parseInt(checkTimeStr);
		// 获取日志
		ShipCheckData[] shipdata=objectFactory.getShipCache()
			.getPlayerDatas(player.getId(),
				SeaBackKit.getSomedayBegin(day*24*3600*1000),shipType);
		// 如果没有日志，直接返回
		if(shipdata==null||shipdata.length==0)
			return GMConstant.ERR_SUCCESS;
		for(int i=0;i<shipdata.length;i++)
		{
			ShipCheckData cdata=(ShipCheckData)shipdata[i];
			if(cdata.getPlayerId()!=player.getId()) continue;
			// 一条日志
			JSONObject jo=new JSONObject();
			try
			{
				// 基本信息
				jo.put(GMConstant.EVENT_ID,cdata.getId());
				jo.put(GMConstant.EVENT_TYPE,cdata.getType());
				jo.put(GMConstant.TIME,cdata.getCreateAt());
				jo.put(GMConstant.EXTRA_INFO,getExtra(cdata,objectFactory));
				// 港口船只
				JSONArray portShip=getShipInfo(cdata.getLeftList());
				jo.put(GMConstant.PORT_SHIPS,portShip);
				// 未修理的船只
				JSONArray hurtShip=getShipInfo(cdata.getHurtList());
				jo.put(GMConstant.BROKEN_SHIPS,hurtShip);
				// 当前所有事件
				JSONArray eventInfo=getEventShipInfo(cdata.getEventList());
				jo.put(GMConstant.EVENT_INFO,eventInfo);
				// 本次事件影响船只
				JSONArray eventShip=getShipInfo(cdata.getList(),cdata.getType());
				jo.put(GMConstant.EVENT_SHIPS,eventShip);
				
				jsonArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return GMConstant.ERR_SUCCESS;
	}

	/**
	 * 构建船只信息
	 * @param list sid,count对
	 * @return
	 */
	private JSONArray getShipInfo(IntList list)
	{
		JSONArray joArray= new JSONArray();
		for(int i=0;i<list.size();i+=2)
		{
			Ship ship=(Ship)Ship.factory.getSample(list.get(i));
			try
			{
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.NAME,ship.getName());
				jo.put(GMConstant.COUNT,list.get(i+1));
				joArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return joArray;
	}
	
	/**
	 * 构建船只信息
	 * @param list sid,count对
	 * @return
	 */
	private JSONArray getShipInfo(IntList list,int type)
	{
		JSONArray joArray=new JSONArray();
		int num=3;
		if(type!=ShipCheckData.ALLIANCE_BATTLE_FIGHT
			&&type!=ShipCheckData.ALLIANCE_BACK_SHIP
			&&type!=ShipCheckData.FIGHT_REST_SHIP) num=2;
		for(int i=0;i<list.size();i+=num)
		{
			Ship ship=(Ship)Ship.factory.getSample(list.get(i));
			try
			{
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.NAME,ship.getName());
				jo.put(GMConstant.COUNT,list.get(i+1));
				joArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return joArray;
	}
	
	/**
	 * 构建所有事件的信息
	 * @param events
	 * @return 返回事件数组
	 */
	private JSONArray getEventShipInfo(ObjectArray events)
	{
		JSONArray joArray=new JSONArray();
		Object[] troops=events.getArray();
		for(int i=0;i<troops.length;i++)
		{
			ShipDataEvent event=(ShipDataEvent)troops[i];
			if(event==null)
				continue;
			try
			{
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.EVENT_ID,event.getEventId());
				IntList ships=event.getShips();
				JSONArray arr=getShipInfo(ships);
				jo.put(GMConstant.SHIPS,arr);
				jo.put(GMConstant.STATE,event.getState());
				jo.put(GMConstant.LOCATION,SeaBackKit.getIslandLocation(event.getIndex()));
				joArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return joArray;
	}
	
	/**设置额外信息**/
	public String getExtra(ShipCheckData cdata,CreatObjectFactory factory)
	{
		if(cdata==null||cdata.getExtra()==null||cdata.getExtra().length()==0)
			return "";
		if(cdata.getType()!=ShipCheckData.FIGHT_SEND_SHIPS)
			return cdata.getExtra();
		String[] extra=TextKit.split(cdata.getExtra(),",");
		if(extra==null) return "";
		if(TextKit.parseInt(extra[0])==NpcIsland.NPC_PLAYER)
		{
			int sx=TextKit.parseInt(extra[2])%600+1;
			int sy=TextKit.parseInt(extra[2])/600+1;
			Player player=factory.getPlayerById(TextKit.parseInt(extra[1]));
			return extra[0]+","+player.getName()+","+sx+","+sy;
		}
		int sx=TextKit.parseInt(extra[1])%600+1;
		int sy=TextKit.parseInt(extra[1])/600+1;
		return extra[0]+","+sx+","+sy;
	}
}
