package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.builds.produce.ProducePropTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 获取建筑生产物品日志
 * 
 * @author Alan
 */
public class ViewProduceProp extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		CreatObjectFactory factory=info.getObjectFactory();
		int buildSid=TextKit.parseInt(params.get("type"));
		String name=params.get("name");
		Player player=factory.getPlayerByName(name,false);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		String stime=params.get("stime");
		String etime=params.get("etime");
		int startTime=SeaBackKit.parseFormatTime(stime);
		int endTime=SeaBackKit.parseFormatTime(etime);
		ProducePropTrack[] tracks=factory.getProduceTrackMemCache()
			.loadTracks(player.getId(),buildSid,startTime,endTime);
		try
		{
			JSONObject json=new JSONObject();
			json.put("pname",player.getName());
			json.put("buildsid",buildSid);
			jsonArray.put(json);
			if(tracks!=null)
			{
				for(int i=0;i<tracks.length;i++)
				{

					json=new JSONObject();
					json.put("state",tracks[i].getType());
					json.put("id",tracks[i].getProductId());
					json.put("prop",tracks[i].getPropSid());
					json.put("num",tracks[i].getNum());
					json.put("time",tracks[i].getCreateAt());
					json.put("needtime",tracks[i].getNeedTime());
					json.put("buildindex",tracks[i].getBuildIndex());
					json.put("buildlevel",tracks[i].getBuildLv());
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
