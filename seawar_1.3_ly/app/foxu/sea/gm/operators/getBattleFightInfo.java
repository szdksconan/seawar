package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.AllianceFightRecordMemCache;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.AllianceFightRecordTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/****
 * 
 * 获取联盟战的信息
 * 
 * @author lhj
 * 
 */
public class getBattleFightInfo extends GMOperator
{

	/**
	 * FIND_ALL=0 全部查询 FIND_BY_ALLIANCE=1 联盟查询 FINAD_BY_PLAYER=2 玩家查询
	 * RESLUT_SELECT=3 查询结果
	 * **/
	public static final int FIND_ALL=0,FIND_BY_ALLIANCE=1,FINAD_BY_PLAYER=2,
					RESLUT_SELECT=3;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{

		try
		{
			String types=params.get("fightType");
			String stime=params.get("stime");
			String etime=params.get("etime");
			int startTime=SeaBackKit.parseFormatTime(stime);
			int endTime=SeaBackKit.parseFormatTime(etime);
			String typeSql="";
			int type=TextKit.parseInt(types);
			String island=params.get("island");
			CreatObjectFactory factory=info.getObjectFactory();
			AllianceFightRecordMemCache cache=factory
				.getAllianceFightRecordMemCache();
			if(type==FIND_ALL)
			{
				typeSql="   and   type=1";
			}
			else if(type==RESLUT_SELECT)
			{
				String state=params.get("state");
				typeSql="   and   state="+TextKit.parseInt(state)
					+" and  battleisland="+TextKit.parseInt(island);
				endTime+=7*PublicConst.DAY_SEC;
			}
			AllianceFightRecordTrack[] tracks=cache.loadTracks(startTime,
				endTime,typeSql);
			if(tracks==null||tracks.length==0)
			{
				return GMConstant.ERR_BATTLE_FIGHT_IS_NULL;
			}
			JSONObject json=new JSONObject();
			json.put("selectType",type);
			if(type==FIND_ALL)
			{
				jsonArray.put(json);
				for(int i=0;i<tracks.length;i++)
				{
					json=new JSONObject();
					json.put("stime",tracks[i].getStime());
					json.put("etime",tracks[i].getEtime());
					json.put("state",tracks[i].getState());
					jsonArray.put(json);
				}
			}
			else if(type==RESLUT_SELECT)
			{
				json.put("island",TextKit.parseInt(island));
				jsonArray.put(json);
				if(tracks!=null)
				{
					for(int i=0;i<tracks.length;i++)
					{
						json=new JSONObject();
						json.put("stage",tracks[i].getStage());
						json.put("rankvalue",
							getRankValue(tracks[i].getRankvalue(),factory));
						json.put("createAt",tracks[i].getCreateAt());
						if(tracks[i].getAllianceId()==0)
							json.put("allianceName","");
						else
						{
							Alliance alliance=factory.getAlliance(
								tracks[i].getAllianceId(),false);
							if(alliance==null)
								json.put("allianceName","");
							else
								json.put("allianceName",alliance.getName());
						}
						json.put("players",
							getPlayersInfo(tracks[i].getPlayers(),factory));
						jsonArray.put(json);
					}
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

	/** 获取排行榜信息 **/
	public String getRankValue(String str,CreatObjectFactory factory)
	{
		StringBuffer sb=new StringBuffer();
		String[] strs=TextKit.split(str,":");
		for(int i=0;i<strs.length;i+=2)
		{
			if(TextKit.parseInt(strs[i])==0) continue;
			Alliance alliance=factory.getAlliance(TextKit.parseInt(strs[i]),
				false);
			if(alliance==null) continue;
			if(sb.length()==0)
				sb.append(alliance.getName()+"-"+strs[i+1]);
			else
				sb.append("---"+alliance.getName()+"-"+strs[i+1]);
		}
		return sb.toString();
	}

	/** 获取当前玩家报名信息 **/
	public String getPlayersInfo(String str,CreatObjectFactory factory)
	{
		StringBuffer sb=new StringBuffer();
		String[] strs=TextKit.split(str,":");
		for(int i=0;i<strs.length;i+=2)
		{
			Player player=factory.getPlayerById(TextKit.parseInt(strs[i]));
			if(player==null) continue;
			Alliance alliance=factory.getAlliance(
				TextKit.parseInt(strs[i+1]),false);
			if(alliance==null) continue;
			if(sb.length()==0)
				sb.append(player.getName()+"("+alliance.getName()+")");
			else
				sb.append("-"+player.getName()+"("+alliance.getName()+")");
		}
		return sb.toString();
	}
}
