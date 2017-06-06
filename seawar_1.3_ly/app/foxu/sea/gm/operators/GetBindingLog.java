package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.bind.BindingTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 获取绑定日志
 * 
 * @author Alan
 * 
 */
public class GetBindingLog extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{

		try
		{
			String pname=params.get("pname");
			String stime=params.get("stime");
			String etime=params.get("etime");
			int sTime=SeaBackKit.parseFormatTime(stime);
			int eTime=SeaBackKit.parseFormatTime(etime);
			if(sTime>eTime) return GMConstant.ERR_PARAMATER_ERROR;
			CreatObjectFactory objectFactory=info.getObjectFactory();
			Player player=objectFactory.getPlayerByName(pname,false);
			if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
			BindingTrack[] tracks=objectFactory.getBindingMemCache()
				.loadTracks(BindingTrack.ALL,BindingTrack.ALL,
					BindingTrack.ALL,BindingTrack.ALL,player.getId(),sTime,
					eTime);
			JSONObject json=new JSONObject();
			json.put("pname",player.getName());
			jsonArray.put(json);
			if(tracks!=null)
			{
				for(int i=0;i<tracks.length;i++)

				{
					json=new JSONObject();
					json.put("bindType",tracks[i].getBindType());
					json.put("trackType",tracks[i].getTrackType());
					json.put("actionType",tracks[i].getActionType());
					json.put("uid",tracks[i].getUid());
					json.put("pid",tracks[i].getPid());
					json.put("operateInfo",tracks[i].getOperateInfo());
					json.put("lastRecord",tracks[i].getLastRecord());
					json.put("currentRecord",tracks[i].getCurrentRecord());
					json.put("time",SeaBackKit.formatDataTime(tracks[i].getTime()));
					jsonArray.put(json);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}
		return 0;
	}
}
