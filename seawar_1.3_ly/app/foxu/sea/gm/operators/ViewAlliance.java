package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.DonateRank;
import foxu.sea.fight.AllianceSkill;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 盟信息查询
 * @author alan
 *
 */
public class ViewAlliance extends GMOperator
{

	int pageSize=20;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String viewType=params.get("range");
		String page=params.get("page");
		int pageNum=1;
		if(page!=null&&page.matches("\\d")) pageNum=Integer.parseInt(page);
		JSONObject type=new JSONObject();
		jsonArray.put(type);
		if("0".equals(viewType))
		{
			try
			{
				type.put("type",0);
				if(pageNum<1) pageNum=1;
				String sqlLength="SELECT COUNT(1) FROM alliances";
				int length=Integer.parseInt(info.getObjectFactory()
					.getAllianceMemCache().getDbaccess().loadSql(sqlLength).getArray()[0].getValue()
					.toString());
				int pages=length/pageSize;
				if(pages%pageSize!=0)	pages++;
				String sql="SELECT * FROM alliances ORDER BY allFightScore DESC LIMIT "
					+(pageNum-1)*pageSize+","+pageSize;
				Alliance[] alliances=(Alliance[])info.getObjectFactory()
					.getAllianceMemCache().getDbaccess().loadBySql(sql);
				if(alliances.length<=0&&pageNum>1)	--pageNum;
				type.put("page",pageNum);
				type.put("maxpage",pages);
				for(int i=0;i<alliances.length;i++)
				{
					JSONObject allianceJson=new JSONObject();
					allianceJson.put("name",alliances[i].getName());
					allianceJson.put("master",
						alliances[i].getMasterName(info.getObjectFactory()));
					allianceJson
						.put("level",alliances[i].getAllianceLevel());
					allianceJson.put("num",alliances[i].playersNum());
					allianceJson.put("maxnum",
						PublicConst.ALLIANCE_LEVEL_NUMS[alliances[i]
							.getAllianceLevel()-1]);
					allianceJson
						.put("fight",alliances[i].getAllFightScore());
					allianceJson.put("rank",(pageNum-1)*pageSize+(i+1));
					jsonArray.put(allianceJson);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else if("2".equals(viewType))
		{
			try
			{
				type.put("type",2);
				String allianceName=params.get("name");
				Alliance alliance=info.getObjectFactory()
					.getAllianceMemCache().loadByName(allianceName,false);
				if(alliance==null)
					return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
				JSONObject aName=new JSONObject();
				aName.put("name",alliance.getName());
				aName.put("level",alliance.getAllianceLevel());
				jsonArray.put(aName);
				JSONArray allianceSkills=new JSONArray();
				ObjectArray skills=alliance.getAllianSkills();
				for(int i=0;i<skills.size();i++)
				{
					AllianceSkill skill=(AllianceSkill)skills.getArray()[i];
					JSONObject skillJson=new JSONObject();

					skillJson.put("sid",skill.getSid());
					skillJson.put("level",skill.getLevel());
					skillJson.put("nowexp",skill.getNowExp());
					// 如果技能到达最高级，升级经验设为0
					int levelexp=skill.getLevel()>=PublicConst.MAX_ALLIANCE_LEVEL
						?0:skill.getExperience()[skill.getLevel()];
					skillJson.put("levelexp",levelexp);
					allianceSkills.put(skillJson);
				}
				jsonArray.put(allianceSkills);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				type.put("type",1);
				String allianceName=params.get("name");
				Alliance alliance=info.getObjectFactory()
					.getAllianceMemCache().loadByName(allianceName,false);
				if(alliance==null)
					return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
				type.put("name",alliance.getName());
				type.put("level",alliance.getAllianceLevel());
				IntList players=alliance.getPlayerList();
				JSONArray alliancePlayers=new JSONArray();
				int pages=players.size()/pageSize;
				if(pages*pageSize<players.size())
				{
					// 多出记录不足一页的补一页
					pages++;
				}
				if(pageNum>pages){
					pageNum=pages;
				}
				if(pageNum<1)	pageNum=1;
				type.put("page",pageNum);
				type.put("maxpage",pages);
				int start=(pageNum-1)*pageSize;
				int len=start+pageSize>players.size()?players.size()-start
					:pageSize;
				for(int i=start;i<start+len;i++)
				{
					JSONObject playerJson=new JSONObject();
					Player player=info.getObjectFactory().getPlayerById(
						players.get(i));
					int militaryRank=Alliance.MILITARY_RANK1;
					if(player.getId()==alliance.getMasterPlayerId())
					{
						militaryRank=Alliance.MILITARY_RANK3;
					}
					for(int j=0;j<alliance.getVicePlayers().size();j++)
					{
						if(player.getId()==alliance.getVicePlayers().get(j))
						{
							militaryRank=Alliance.MILITARY_RANK2;
							break;
						}
					}
					playerJson.put("position",militaryRank);
					playerJson.put("name",player.getName());
					playerJson.put("level",player.getLevel());
					playerJson.put("fight",player.getFightScore());
					int giveNum=0;
					DonateRank drank=(DonateRank)alliance.getGiveValue().get(player.getId());
					if(drank!=null)
					{
						giveNum=(int)drank.getTotleValue();
					}
					playerJson.put("give",giveNum);
					playerJson.put("lasttime",player.getUpdateTime());
					alliancePlayers.put(playerJson);
				}
				jsonArray.put(alliancePlayers);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}
		return GMConstant.ERR_SUCCESS;
	}
}
