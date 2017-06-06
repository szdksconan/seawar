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
		String alliance_name=params.get("alliance_name");//�õ���ǰ��������������
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
		//���˵Ļ�����Ϣ
		JSONObject joAlliance=new JSONObject();
		try
		{
			//���˻�����Ϣ
			JSONObject alliancebaseinfo=this.getAllianceinfomation(alliance,objectFactory);
			joAlliance.put(GMConstant.ALLIANCEINFO,alliancebaseinfo);
			//���˼���
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
	 * ��ǰ������������
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
	 *���˵Ļ�����Ϣ
	 */
	private JSONObject getAllianceinfomation(Alliance alliance,CreatObjectFactory objectFactory)
	{
		JSONObject jo=new JSONObject();
		try
		{
			jo.put(GMConstant.ALLIANCE_NAME,alliance.getName());//��������
			jo.put(GMConstant.ALLIANCE_RANKNUM,alliance.getRankNum());//���˵�����
			jo.put(GMConstant.ALLIANCE_LEVEL,alliance.getAllianceLevel());//���˵ĵȼ�
			jo.put(GMConstant.ALLIANCE_MASTERNAME,alliance.getMasterName(objectFactory));//���˵Ļ᳤
			jo.put(GMConstant.ALLIANCE_EXP,alliance.getAllianceExp());//���˵ľ���
			jo.put(GMConstant.ALLIANCE_FIGHTSCORE,alliance.getAllFightScore());//���˵�ս����
			jo.put(GMConstant.ALLIANCE_PLAYER_NUM,alliance.getPlayerList().size()+alliance.getVicePlayers().size());//���˵�����
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jo;
	}
	/*
	 * �õ����˼���
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
		Player player=objectFactory.getPlayerById(playersid);//�õ���ǰ�����
		try
		{
			//�������
			jo.put(GMConstant.NAME,player.getName());
			//id
			jo.put(GMConstant.ID,player.getId());
			// ս��
			jo.put(GMConstant.POWER,player.getFightScore());
			// ��ҵȼ�
			jo.put(GMConstant.LEVEL,player.getLevel());
			// ����¼ʱ��
			jo.put(GMConstant.LOGIN_TIME,player.getUpdateTime());
			//���׶�	
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
							//���᳤λ��
							jo.put(GMConstant.GUILD_POSITION,"vicepresident");
							continue;
						}
						if(playersid==alliance.getMasterPlayerId())
						{
							//�᳤λ��
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
					//�᳤λ��
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

