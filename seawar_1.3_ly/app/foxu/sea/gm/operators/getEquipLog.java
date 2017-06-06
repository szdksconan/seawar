package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

public class getEquipLog extends GMOperator
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
			String type=params.get("type");
			int sTime=SeaBackKit.parseFormatTime(stime);
			int eTime=SeaBackKit.parseFormatTime(etime);
			if(sTime>eTime) return GMConstant.ERR_PARAMATER_ERROR;
			CreatObjectFactory objectFactory=info.getObjectFactory();
			Player player=objectFactory.getPlayerByName(pname,false);
			if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
			EquipmentTrack[] tracks=null;
			if("2".equals(type))
				tracks=objectFactory.getEquipTrackMemCache().loadTracks(
					player.getId(),sTime,eTime,EquipmentTrack.ADD);
			else if("3".equals(type))
				tracks=objectFactory.getEquipTrackMemCache().loadTracks(
					player.getId(),sTime,eTime,EquipmentTrack.REDUCE);
			else
				tracks=objectFactory.getEquipTrackMemCache().loadTracks(
					player.getId(),sTime,eTime);
			JSONObject json=new JSONObject();
			json.put("pname",player.getName());
			jsonArray.put(json);
			if(tracks!=null)
			{
				for(int i=0;i<tracks.length;i++)

				{
					json=new JSONObject();
					json.put("sid",tracks[i].getEquipSid());
					json.put("type",tracks[i].getType());
					json.put("reason",tracks[i].getReason());
					json.put("item",tracks[i].getItem_id());
					json.put("num",tracks[i].getNum());
					json.put("left",tracks[i].getNowLeft());
					json.put("time",tracks[i].getCreateAt());
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
