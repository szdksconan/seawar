package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.CoinsTrack;

/***
 * 查询军魂日志
 * 
 * @author lihon
 *
 */
public class Viewcoins extends GMOperator
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
		CoinsTrack[] tracks=null;
		if(type==ADD)
		{
			tracks=factory.getCoinsMemCache().loadTracks(player.getId(),
				startTime,endTime,CoinsTrack.ADD);
		}
		else if(type==RECUDE)
		{
			tracks=factory.getCoinsMemCache().loadTracks(player.getId(),
				startTime,endTime,CoinsTrack.DESC);
		}
		else
		{
			tracks=factory.getCoinsMemCache().loadTracks(player.getId(),
				startTime,endTime);
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
