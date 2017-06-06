package foxu.sea.webgm.opertrator;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.set.IntList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.fight.AllianceSkill;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


public class GetAllianceinfo extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String alliance_name=params.get("alliance_name");//得到当前搜索的联盟名称
		if(alliance_name==null)
		{
			return GMConstant.ERR_ALLIANCE_NAME_IS_NULL;
		}
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Alliance alliance=getcurAlliance(objectFactory,alliance_name);
		if(alliance==null)
		{
			return GMConstant.ERR_ALLIANCE_EXISTS;
		}
		//联盟的基本信息
		JSONObject joAlliance=new JSONObject();
		try
		{
			//联盟基本信息
			JSONObject alliancebaseinfo=this.getAllianceinfomation(alliance,objectFactory);
			joAlliance.put(GMConstant.ALLIANCEINFO,alliancebaseinfo);
			//联盟技能
			JSONArray allianceskill=this.getAllianceskill(alliance);
			joAlliance.put(GMConstant.ALLIANCE_SKILL,allianceskill);
			JSONArray allianceplayer=this.getAlliancePlayerinfo(alliance,objectFactory);
			joAlliance.put(GMConstant.ALLIANCE_PLAYER,allianceplayer);
			jsonArray.put(joAlliance);
		}
		catch(JSONException e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}
	/*
	 * 当前的搜索的联盟
	 */
	private Alliance getcurAlliance(CreatObjectFactory objectFactory,String alliance_name)
	{
		Alliance alliance=null;
		Object[] obj=objectFactory.getAllianceMemCache().getCacheMap().valueArray();
		for(int i=0;i<obj.length;i++)
		{
			AllianceSave  alliancesave=(AllianceSave)obj[i];
			alliance=alliancesave.getData();
			if(alliance.getName().equals(alliance_name))
			{
				return alliance;
			}
		}
		return null;
	}
	/**
	 *联盟的基本信息
	 */
	private JSONObject getAllianceinfomation(Alliance alliance,CreatObjectFactory objectFactory)
	{
		JSONObject jo=new JSONObject();
		try
		{
			jo.put(GMConstant.ALLIANCE_NAME,alliance.getName());//联盟名称
			jo.put(GMConstant.ALLIANCE_RANKNUM,alliance.getRankNum());//联盟的排名
			jo.put(GMConstant.ALLIANCE_LEVEL,alliance.getAllianceLevel());//联盟的等级
			jo.put(GMConstant.ALLIANCE_MASTERNAME,alliance.getMasterName(objectFactory));//联盟的会长
			jo.put(GMConstant.ALLIANCE_EXP,alliance.getAllianceExp());//联盟的经验
			jo.put(GMConstant.ALLIANCE_FIGHTSCORE,alliance.getAllFightScore());//联盟的战斗力
			jo.put(GMConstant.ALLIANCE_PLAYER_NUM,alliance.getPlayerList().size()+alliance.getVicePlayers().size());//联盟的人数
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jo;
	}
	/*
	 * 得到联盟技能
	 */
	private JSONArray getAllianceskill(Alliance alliance)
	{
		JSONArray joArray=new JSONArray();
		Object[] array=alliance.getAllianSkills().toArray();
		for(int i=0;i<array.length;i++)
		{
			if(array[i]==null) continue;
			JSONObject jo=new JSONObject();
			AllianceSkill skill=(AllianceSkill)array[i];
		    Integer sid=skill.getSid();
		    Integer level=skill.getLevel();
		    Integer c_exp=skill.getNowExp();
		    try
			{
				jo.put(GMConstant.SID,sid);
				jo.put(GMConstant.LEVEL,level);
				jo.put(GMConstant.C_EXP,c_exp);
				joArray.put(jo);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		return joArray;
	}
	
	private JSONArray getAlliancePlayerinfo(Alliance alliance,CreatObjectFactory objectFactory)
	{
		JSONArray joArray=new JSONArray();
		
		IntList array=alliance.getPlayerList();
		IntList arry=alliance.getVicePlayers();
		for(int i=0;i<array.size();i++)
		{
			Integer playersid=array.get(i);
			if(playersid==null) continue;
			JSONObject jo=new JSONObject();
			jo=this.getplayer_infomation(objectFactory,playersid,arry,alliance);
			joArray.put(jo);
		}
		return joArray;
	}
	private JSONObject getplayer_infomation(CreatObjectFactory objectFactory,Integer playersid,IntList arry,Alliance alliance)
	{
		JSONObject jo=new JSONObject();
		Player player=objectFactory.getPlayerById(playersid);//得到当前的玩家
		try
		{
			//玩家名称
			jo.put(GMConstant.NAME,player.getName());
			//id
			jo.put(GMConstant.ID,player.getId());
			// 战力
			jo.put(GMConstant.POWER,player.getFightScore());
			// 玩家等级
			jo.put(GMConstant.LEVEL,player.getLevel());
			// 最后登录时间
			jo.put(GMConstant.LOGIN_TIME,player.getUpdateTime());
			//贡献度	
			String alreadyGive=player
							.getAttributes(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER);
			if(alreadyGive==null || alreadyGive=="")
			{
				alreadyGive="0";
			}
			jo.put(GMConstant.CONTRIBUTION,Integer.parseInt(alreadyGive));
			if(arry.size()>0)
			{
					for(int i=0;i<arry.size();i++)
					{
						if(playersid==arry.get(i))
						{
							//副会长位置
							jo.put(GMConstant.GUILD_POSITION,"vicepresident");
							continue;
						}
						if(playersid==alliance.getMasterPlayerId())
						{
							//会长位置
							jo.put(GMConstant.GUILD_POSITION,"president");
							continue;
						}
						else
						{
							jo.put(GMConstant.GUILD_POSITION,"members");
						}
					}
			}	
			else
			{
				if(playersid==alliance.getMasterPlayerId())
				{
					//会长位置
					jo.put(GMConstant.GUILD_POSITION,"president");
				}
				else
				{
					jo.put(GMConstant.GUILD_POSITION,"members");
				}
			}
		
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jo;
	}
}

