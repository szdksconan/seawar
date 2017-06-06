package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.Comparator;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Science;
import foxu.sea.Ship;
import foxu.sea.User;
import foxu.sea.alliance.Alliance;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.Troop;
import foxu.sea.builds.produce.ScienceProduce;
import foxu.sea.fight.Skill;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.proplist.PropList;


public class GetPlayerInfo extends GMOperator
{


	CreatObjectFactory objectFactory;

	public int operate(String user,Map<String,String> params,JSONArray array,ServerInfo info)
	{
		String playerInfo=params.get("player_info");
		String infoType=params.get("info_type");
		if(playerInfo==null||infoType==null||playerInfo.length()==0||infoType.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
			
		 objectFactory=info.getObjectFactory();
		Player[] players=null;
		if("1".equals(infoType))
			players=getPlayersByName(playerInfo,objectFactory);
		else if("2".equals(infoType))
			players=getPlayersByAccount(playerInfo,objectFactory);
		else if("3".equals(infoType))
			players=getPlayersByUDID(playerInfo,objectFactory);
		else if("4".equals(infoType))
			players=getPlayersById(playerInfo,objectFactory);
		else
			return GMConstant.ERR_UNKNOWN;
		if(players==null||players.length==0)
		{
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		}
		for(int i=0;i<players.length;i++)
		{
			Player player=players[i];
			try
			{
				JSONObject joPlayer=new JSONObject();
				// 基本信息
				JSONObject baseInfo=this.getPlayerBaseInfo(player);
				joPlayer.put(GMConstant.BASE_INFO,baseInfo);
				// 建筑信息
				JSONArray buildInfo=this.getPlayerBuildInfo(player);
				joPlayer.put(GMConstant.BUILD_INFO,buildInfo);
				// 科技信息
				JSONArray scienceInfo=this.getPlayerScienceInfo(player);
				joPlayer.put(GMConstant.SCIENCE_INFO,scienceInfo);
				// 技能信息
				JSONArray skillInfo=this.getPlayerSkillInfo(player);
				joPlayer.put(GMConstant.SKILL_INFO,skillInfo);
				// 港口船只
				JSONArray portShip=this.getPlayerPortShipInfo(player);
				joPlayer.put(GMConstant.PORT_SHIPS,portShip);
				// 物品信息
				JSONArray propInfo=this.getPlayerBundleInfo(player);
				joPlayer.put(GMConstant.BUNDLE_INFO,propInfo);
				// 星石研究院
				JSONArray shipInfo=this.getShipInfo(player);
				joPlayer.put(GMConstant.SHIP_INFO,shipInfo);
				array.put(joPlayer);
			}
			catch(Exception e)
			{
				 e.printStackTrace();
				return GMConstant.ERR_UNKNOWN;
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
	
	/**
	 *  通过名字获取player
	 * @param name
	 * @param objectFactory
	 * @return
	 */
	public Player[] getPlayersByName(String name,CreatObjectFactory objectFactory)
	{
		Player player=objectFactory.getPlayerByName(name,false);
		if(player!=null)
		{
			return new Player[]{player};
		}
		return null;
	}
	
	/**
	 * 通过账号查询玩家
	 * @param account
	 * @param objectFactory
	 * @return
	 */
	public Player[] getPlayersByAccount(String account,CreatObjectFactory objectFactory)
	{
		if(account==null||account.length()==0)
			return null;
		String sql="SELECT * FROM players WHERE players.user_id=(SELECT id FROM users WHERE users.user_account='"+account+"')";
		Player[] objs=(Player[])objectFactory.getPlayerCache().getDbaccess().loadBySql(sql);
		
		return objs;
	}
	
	/**
	 *  通过名字获取player
	 * @param name
	 * @param objectFactory
	 * @return
	 */
	public Player[] getPlayersByUDID(String udid,CreatObjectFactory objectFactory)
	{
		if(udid==null)
			return null;
		//String sql="SELECT * FROM players WHERE players.user_id=(SELECT id FROM users WHERE users.login_udid='"+udid+"')";
		//Player[] objs=(Player[])objectFactory.getPlayerCache().getDbaccess().loadBySql(sql);
		Player[] players=null;
		// load user
		String sql="SELECT * FROM users WHERE login_udid='"+udid+"' or udid='"+udid+"'";
		User[] users=objectFactory.getUserDBAccess().loadBySql(sql);
		if(users==null||users.length==0)
			return null;
		ArrayList list=new ArrayList();
		// load every player
		for(int i=0;i<users.length;i++)
		{
			sql="SELECT * FROM players WHERE user_id="+users[i].getId();
			players=(Player[])objectFactory.getPlayerCache().getDbaccess().loadBySql(sql);
			if(players==null||players.length==0)
				continue;
			for(int k=0;k<players.length;k++)
				list.add(players[k]);
		}
		if(list.size()==0)
			return null;
		players=new Player[list.size()];
		list.toArray(players);
		return players;
	}

	/**
	 *  通过角色id获取player
	 * @param id
	 * @param objectFactory
	 * @return
	 */
	public Player[] getPlayersById(String id,CreatObjectFactory objectFactory)
	{
		Player player=objectFactory.getPlayerById(TextKit.parseInt(id));
		if(player!=null)
		{
			return new Player[]{player};
		}
		return null;
	}
	
	/**
	 * 组装玩家基本信息
	 * @param player
	 * @return
	 */
	private JSONObject getPlayerBaseInfo(Player player)
	{
		JSONObject jo=new JSONObject();
		try
		{
			// 玩家名字
			jo.put(GMConstant.NAME,player.getName());
			// 玩家ID
			jo.put(GMConstant.ID,player.getId());
			// 玩家等级
			jo.put(GMConstant.LEVEL,player.getLevel());
			// 当前经验值
			jo.put(GMConstant.EXPERIENCE,player.getExperience());
			// vip等级
			jo.put(GMConstant.VIP_LEVEL,player.getUser_state());
			
			jo.put(GMConstant.MOUTH_CARD,getMouthDays(player));
			//能力值
			jo.put(GMConstant.ENERGY_VALUE,player.getIsland().gotEnergy(TimeKit.getSecondTime()));

			// 统御
			jo.put(GMConstant.COMMAND_LEVEL,player.getCommanderLevel());
			// 声望
			jo.put(GMConstant.CREDIT,player.getHonor()[Player.HONOR_LEVEL_INDEX]);
			// 军衔
			jo.put(GMConstant.MILITARY_RANK,player.getPlayerType());
			// 荣誉
			jo.put(GMConstant.HONOR,player.getHonorScore());
			// 战力
			jo.put(GMConstant.POWER,player.getFightScore());
			// 个人战斗力排行
			jo.put(GMConstant.FIGHTSCORERANK,player.getFightScoreRank());
			// 荣誉排行
			jo.put(GMConstant.HONORSCORERANK,player.getHonorScoreRank());
			//
			jo.put(GMConstant.PLUNDERRANK,player.getPlunderRank());
			// 关卡星数
			// 能量
		//	jo.put(GMConstant.ENERGY,player.getActives()[0]);
			// 删除玩家的状态
			String dState=player
				.getAttributes(PublicConst.PLAYER_DELETE_FLAG);
			if(dState!=null
				&&TextKit.parseInt(dState)==PublicConst.DELETE_STATE)
				jo.put(GMConstant.DELETE_STATE,"true");
			else
				jo.put(GMConstant.DELETE_STATE,"false");
			// 充值宝石
			long maxgems=player.getResources()[Resources.MAXGEMS];
			int dailygems=player.getDailyGemsCount();
			int newDailygems=player.getNewDailyGemsCount();
			jo.put(GMConstant.RECHARGE_GEMS,(maxgems-dailygems)+"("+(dailygems+newDailygems)+")");
			// 当前宝石
			jo.put(GMConstant.GEMS,Resources.getGems(player.getResources()));
			// 当前充值宝石
			jo.put(GMConstant.PGEMS,player.getResources()[Resources.GEMS]);
			// 当前系统宝石
			jo.put(GMConstant.SGEMS,player.getResources()[Resources.SGEMS]);
			// 金
			jo.put(GMConstant.MONEY,player.getResources()[Resources.MONEY]);
			// 铁
			jo.put(GMConstant.METAL,player.getResources()[Resources.METAL]);
			// 油
			jo.put(GMConstant.OIL,player.getResources()[Resources.OIL]);
			// 硅
			jo.put(GMConstant.SILICON,player.getResources()[Resources.SILICON]);
			// 铀
			jo.put(GMConstant.URANIUM,player.getResources()[Resources.URANIUM]);
			// 联盟
			String name="";
			Alliance alliance=(Alliance)objectFactory
				.getAllianceMemCache().loadOnly(
					player.getAttributes(PublicConst.ALLIANCE_ID));
			if(alliance!=null) name=alliance.getName();
			jo.put(GMConstant.ALLIANCE_NAME,name);
			// 账号
			User user=objectFactory.getUserDBAccess().load(String.valueOf(
				player.getUser_id()));
			//封号状态
			int banned=user==null?0:user.getBanned(); 
			jo.put(GMConstant.BANNED_STATE,banned!=0?"true":"false");
			
			jo.put(GMConstant.ACCOUNT,user!=null?user.getUserAccount():"");
			// create udid
			jo.put(GMConstant.CREATE_UDID,user!=null?user.getCreateUdid():"");
			// create udid
			jo.put(GMConstant.LOGIN_UDID,user!=null?user.getLoginUdid():"");
			jo.put(GMConstant.PAY_UDID,user!=null?user.getPayUdid():"");
			// 创建时间
			jo.put(GMConstant.CREATE_TIME,player.getCreateTime());
			// 最后登录时间
			jo.put(GMConstant.LOGIN_TIME,player.getUpdateTime());
			// 位置
			int islandIndex=-1;
			NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(player.getId());
			if(island!=null)
			{
				islandIndex=island.getIndex();
			}
			jo.put(GMConstant.LOCATION,islandIndex>=0?SeaBackKit.getIslandLocation(islandIndex):"0,0");
			boolean online=false;
			Session session=(Session)player.getSource();
			if(session!=null)
			{
				Connect c=session.getConnect();
				if(c!=null) online=c.isActive();
			}
			jo.put(GMConstant.ONLINE,online);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jo;
	}
	
	/**
	 * 组装玩家的建筑信息(位置，名称，等级)
	 * @param player
	 * @return
	 */
	private JSONArray getPlayerBuildInfo(Player player)
	{
		JSONArray joArray=new JSONArray();
		// 玩家建筑
		Object[] builds=player.getIsland().getBuildArray();
		SetKit.sort(builds,new BuildComparator());
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild b=(PlayerBuild)builds[i];
			if(b==null)
				continue;
			JSONObject jo=new JSONObject();
			// 名字
			String strName=b.getBuildName();
			// 位置
			int index=b.getIndex();
			// 等级
			int level=b.getBuildLevel();
			try
			{
				jo.put(GMConstant.NAME,strName);
				jo.put(GMConstant.INDEX,index);
				jo.put(GMConstant.LEVEL,level);
				joArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return joArray;
	}
	
	/**
	 * 获取玩家技能信息
	 * @param player
	 * @return
	 */
	private JSONArray getPlayerSkillInfo(Player player)
	{
		JSONArray joArray=new JSONArray();
		// 玩家技能
		Object object[]=player.getSkills().toArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			Skill skill=(Skill)object[i];
			int sid=skill.getSid();
			String name=skill.getName();
			int level=skill.getLevel();
			try
			{
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.NAME,name);
				jo.put(GMConstant.SID,sid);
				jo.put(GMConstant.LEVEL,level);
				joArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return joArray;
	}
	
	/**
	 * 玩家科技信息
	 * @param player
	 * @return
	 */
	private JSONArray getPlayerScienceInfo(Player player)
	{
		JSONArray joArray=new JSONArray();
		PlayerBuild buildscice=player.getIsland().getBuildByIndex(
			BuildInfo.INDEX_2,player.getIsland().getBuilds());
		if(buildscice!=null)
		{
			ScienceProduce sp=(ScienceProduce)buildscice
				.getProduce();
			Science science[]=sp.getAllScience();
			for(int i=0;i<science.length;i++)
			{
				// 科技名字替换
				String name=science[i].getName();
				int level=science[i].getLevel();
				try
				{
					JSONObject jo=new JSONObject();
					jo.put(GMConstant.NAME,name);
					jo.put(GMConstant.LEVEL,level);
					joArray.put(jo);
				}
				catch(JSONException e)
				{
				}
			}
		}
		return joArray;
	}
	
	
	private  int getMouthDays(Player player)
	{
		//判断是否可以领取
		String etime=player.getAttributes(PublicConst.END_TIME);
		int timenow=TimeKit.getSecondTime();
		int endtime=etime==null?0:Integer.parseInt(etime);
		int abletime=0;
		if(endtime>timenow)// 判断当前的月卡是否还有时间
		{
			abletime=(endtime-timenow)/PublicConst.DAY_SEC+1;
		}
		return  abletime;
	}
	/**
	 * 获取玩家港口的船只
	 * @param player
	 * @return
	 */
	private JSONArray getPlayerPortShipInfo(Player player)
	{
		JSONArray joArray=new JSONArray();
		Object troops[]=player.getIsland().getTroops().getArray();
		for(int i=0;i<troops.length;i++)
		{
			if(troops[i]==null)
				continue;
			Troop troop=(Troop)troops[i];
			Ship ship=(Ship)Ship.factory.getSample(troop
				.getShipSid());
			String name=ship.getName();
			int num=troop.getNum();
			try
			{
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.NAME,name);
				jo.put(GMConstant.COUNT,num);
				joArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return joArray;
	}
	
	/**
	 * 获取包裹信息
	 * @param player
	 * @return
	 */
	private JSONArray getPlayerBundleInfo(Player player)
	{
		PropList propList=player.getBundle();
		Prop[] props=propList.getProps();
		JSONArray jsonArray=new JSONArray();
		for(int i=0;i<props.length;i++)
		{
			if(props[i]==null)
				continue;
			try
			{
				Prop prop=props[i];
				int count=1;
				if(prop instanceof NormalProp)
					count=((NormalProp)prop).getCount();
				JSONObject jo=new JSONObject();
				// 物品sid
				jo.put(GMConstant.SID,prop.getSid());
				// 物品名称
				jo.put(GMConstant.NAME,prop.getName());
				// 物品数量
				jo.put(GMConstant.COUNT,count);
				jsonArray.put(jo);
			}
			catch(JSONException je)
			{
			}
		}
		
		return jsonArray;
	}

	/**
	 * 星石头研究院
	 * 
	 * @param player
	 * @return
	 */
	private JSONArray getShipInfo(Player player)
	{
		int[] shipLevel=player.getShipLevel();
		JSONArray jsonArray=new JSONArray();
		if(shipLevel!=null)
		{
			for(int i=0;i<shipLevel.length;i++)
			{
				JSONObject jo=new JSONObject();
				try
				{
					jo.put(GMConstant.SID,shipLevel[i]>>>16);
					jo.put(GMConstant.NAME,"ship_skill_"+(shipLevel[i]>>>16)
						+"");
					jo.put(GMConstant.LEVEL,(shipLevel[i]<<16)>>>16);
					jsonArray.put(jo);
				}
				catch(JSONException e)
				{
				}
			}
		}
		return jsonArray;
	}
}

/**
 * 建筑比较器，按建筑位置排序
 * @author comeback
 *
 */
class BuildComparator implements Comparator
{

	public int compare(Object arg0,Object arg1)
	{
		if(arg0==null&&arg1==null)
			return COMP_EQUAL;
		if(arg0==null)
			return COMP_LESS;
		if(arg1==null)
			return COMP_GRTR;
		PlayerBuild build1=(PlayerBuild)arg0;
		PlayerBuild build2=(PlayerBuild)arg1;
		if(build1.getIndex()>build2.getIndex())
			return COMP_LESS;
		if(build1.getIndex()<build2.getIndex())
			return COMP_GRTR;
		return COMP_EQUAL;
	}
	
}