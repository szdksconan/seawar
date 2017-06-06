package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gems.GemsTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 宝石日志
 * @author comeback
 *
 */
public class GemsLog extends GMOperator
{
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String yearStr=params.get("year");
		String monthStr=params.get("month");
		String dayStr=params.get("day");
		String types=params.get("type");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,false);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		int year=Integer.parseInt(yearStr);
		int month=Integer.parseInt(monthStr);
		int day=Integer.parseInt(dayStr);
		// 加载数据库中的部分
		GemsTrack tracks[]=objectFactory.getGemsTrackMemCache()
				.loadTracks(player.getId(),year,month,day,TextKit.parseInt(types));
		if(tracks==null||tracks.length==0)
			return GMConstant.ERR_SUCCESS;
		for(int i=0;i<tracks.length;i++)
		{
			JSONObject jo=createGemsTrack(tracks[i]);
			if(jo!=null)
				jsonArray.put(jo);
		}
		// 内存中的部分
		Object[] objs=objectFactory.getGemsTrackMemCache()
			.getList().valueArray();
		for(int i=objs.length-1;i>=0;i--)
		{
			GemsTrack track=(GemsTrack)objs[i];
			if(track.getPlayerId()!=player.getId()) continue;
			JSONObject jo=createGemsTrack(track);
			if(jo!=null)
				jsonArray.put(jo);
		}
		
		return GMConstant.ERR_SUCCESS;
	}
	
	private JSONObject createGemsTrack(GemsTrack track)
	{
		try
		{
			JSONObject jo=new JSONObject();
			jo.put(GMConstant.ID,track.getId());
			jo.put(GMConstant.TYPE,track.getType());
			jo.put(GMConstant.GEMS,track.getGems());
			jo.put(GMConstant.NOW_GEMS,track.getNowGems());
			jo.put(GMConstant.ITEM_ID,track.getItem_id());
			jo.put(GMConstant.TIME,track.getCreateAt());
			return jo;
		}
		catch(JSONException e)
		{
			return null;
		}
		
	}

}
