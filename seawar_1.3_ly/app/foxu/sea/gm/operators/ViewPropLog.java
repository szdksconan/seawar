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
import foxu.sea.proplist.PropTrack;

/**
 * 查看物品日志
 * 
 * @author Alan
 */
public class ViewPropLog extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		CreatObjectFactory factory=info.getObjectFactory();
		String name=params.get("name");
		Player player=factory.getPlayerByName(name,false);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		int type=TextKit.parseInt(params.get("type"));
		String stime=params.get("stime");
		String etime=params.get("etime");
		int startTime=SeaBackKit.parseFormatTime(stime);
		int endTime=SeaBackKit.parseFormatTime(etime);
		PropTrack[] tracks=null;
		if(type==1)
			tracks=factory.getPropTrackMemCache().loadTracks(player.getId(),
				startTime,endTime);
		else
		{
			int propSid=TextKit.parseInt(params.get("propSid"));
			tracks=factory.getPropTrackMemCache().loadTracks(player.getId(),
				propSid,startTime,endTime);
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
					json.put("invokeNum",tracks[i].getInvokeNum());
					json.put("leftNum",tracks[i].getLeftNum());
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
