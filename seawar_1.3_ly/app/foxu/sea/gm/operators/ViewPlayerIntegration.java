package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.alliance.alliancebattle.IntegrationTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/***
 * 查询玩家积分日志
 * 
 * @author lhj
 * 
 */
public class ViewPlayerIntegration extends GMOperator
{

	public static final int ADD=2,RECUDE=3;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{

		String playerName=params.get("pname");
		CreatObjectFactory factory=info.getObjectFactory();
		Player player=factory.getPlayerByName(playerName,false);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		String types=params.get("type");
		int type=TextKit.parseInt(types);
		String stime=params.get("stime");
		String etime=params.get("etime");
		int startTime=SeaBackKit.parseFormatTime(stime);
		int endTime=SeaBackKit.parseFormatTime(etime);
		IntegrationTrack[] tracks=null;
		if(type==ADD)
		{
			tracks=factory.getIntegrationTrackMemCache().loadTracks(
				player.getId(),startTime,endTime,IntegrationTrack.ADD);
		}
		else if(type==RECUDE)
		{
			tracks=factory.getIntegrationTrackMemCache().loadTracks(
				player.getId(),startTime,endTime,IntegrationTrack.REDUCE);
		}
		else
		{
			tracks=factory.getIntegrationTrackMemCache().loadTracks(
				player.getId(),startTime,endTime);
		}

		try
		{
			JSONObject json=new JSONObject();
			json.put("pname",player.getName());
			jsonArray.put(json);
			if(tracks!=null)
			{
				for(int i=0;i<tracks.length;i++)
				{
					json=new JSONObject();
					json.put("type",tracks[i].getType());
					json.put("prop",tracks[i].getPropSid());
					json.put("time",tracks[i].getCreateAt());
					json.put("state",tracks[i].getState());
					json.put("num",tracks[i].getNum());
					json.put("nowleft",tracks[i].getNowLeft());
					jsonArray.put(json);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}

		return GMConstant.ERR_SUCCESS;
	}

}
