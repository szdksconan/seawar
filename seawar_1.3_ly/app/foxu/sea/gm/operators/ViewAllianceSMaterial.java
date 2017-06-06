package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.SciencePointTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 
 * @author lhj 查询联盟的物资信息和科技点信息
 * 
 */
public class ViewAllianceSMaterial extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String allianceName=params.get("alliance_name");
			if(allianceName==null||allianceName.trim().length()==0)
				return GMConstant.ERR_ALLIANCE_NAME_IS_NULL;
			CreatObjectFactory factory=info.getObjectFactory();
			Alliance alliance=factory.getAllianceMemCache().loadByName(
				allianceName,false);
			if(alliance==null) return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
			String styles=params.get("style");
			String types=params.get("type");
			String startTime=params.get("stime");
			String endTime=params.get("etime");
			int stime=SeaBackKit.parseFormatTime(startTime);
			int etime=SeaBackKit.parseFormatTime(endTime);
			int style=TextKit.parseInt(styles);
			int type=TextKit.parseInt(types);
			SciencePointTrack[] tracks=getLog(stime,etime,type,style,
				factory,alliance);
			JSONObject json=new JSONObject();
			json.put("allianceName",alliance.getName());
			json.put("style",styles);
			jsonArray.put(json);
			if(tracks!=null)
			{
				for(int i=0;i<tracks.length;i++)
				{
					json=new JSONObject();
					Player player=factory.getPlayerById(tracks[i]
						.getPlayerId());
					if(player!=null)
						json.put("playerName",player.getName());
					else
						json.put("playerName","");
					json.put("time",tracks[i].getCreateAt());
					json.put("num",tracks[i].getNum());
					json.put("state",tracks[i].getState());
					json.put("type",tracks[i].getType());
					json.put("nowleft",tracks[i].getNowLeft());
					json.put("extra",tracks[i].getExtra());
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

	/** 获取当前的日志记录 **/
	public SciencePointTrack[] getLog(int stime,int etime,int type,
		int style,CreatObjectFactory factory,Alliance alliance)
	{
		SciencePointTrack[] track=null;
		if(type==2)
		{
			if(style==SciencePointTrack.MATERIAL)
			{
				track=factory.getSciencePointMemCache().loadTracks(
					alliance.getId(),stime,etime,SciencePointTrack.MATERIAL);
			}
			else
			{
				track=factory.getSciencePointMemCache().loadTracks(
					alliance.getId(),stime,etime,
					SciencePointTrack.SCIENCE_POINT);
			}
		}
		else
		{
			if(style==SciencePointTrack.MATERIAL)
			{
				track=factory.getSciencePointMemCache().loadTracks(
					alliance.getId(),stime,etime,type,
					SciencePointTrack.MATERIAL);
			}
			else
			{
				track=factory.getSciencePointMemCache().loadTracks(
					alliance.getId(),stime,etime,type,
					SciencePointTrack.SCIENCE_POINT);
			}
		}
		return track;
	}

}
