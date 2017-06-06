package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.builds.Build;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.Island;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.recruit.RecruitKit;

/**
 * 设置建筑等级
 * @author comeback
 *
 */
public class BuildLevel extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String sidStr=params.get("sid");
		String indexStr=params.get("index");
		String levelStr=params.get("level");
		int index=Integer.parseInt(indexStr);
		int sid=Integer.parseInt(sidStr);
		int level=Integer.parseInt(levelStr);
		
		if(level<=0||level>PublicConst.MAX_BUILD_LEVEL)
			return GMConstant.ERR_PARAMATER_ERROR;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds.getBuilds());
		// 如果没有建筑或者建筑SID不同，尝试修建一个
		if(checkBuild==null||(sid>0 && checkBuild.getSid()!=sid))
			return constructBuild(player,sid,index,level);
		// 检查建筑
//		if(checkBuild.getBuildLevel()>=checkBuild.getMaxLevel())
//			return GMConstant.ERR_BUILD_LEVEL_LIMIT;

		// 检查指挥中心
		if(!isEnoughDirector(player,level)
			&&index!=BuildInfo.INDEX_0)
			return GMConstant.ERR_DIRECTOR_LEVEL_ERROR;
		// 检查是否正在升级
		if(builds.checkNowBuildingByIndex(index))
			return GMConstant.ERR_BUILD_IS_LEVELING;
		checkBuild.setBuildLevel(level);
		player.getTaskManager().checkTastEvent(null);
		//成就信息采集
		AchieveCollect.buildLevel(checkBuild,builds,player);
		//新兵福利
		RecruitKit.pushTaskBuild(checkBuild,player);
		return GMConstant.ERR_SUCCESS;
	}

	private int constructBuild(Player player,int sid,int index,int level)
	{
		if(!player.isPlayerHaveIndex(index))
			return GMConstant.ERR_BUILD_INDEX_ERROR;
		PlayerBuild playerBuild=(PlayerBuild)Build.factory
						.newSample(sid);
		if(playerBuild==null)
			return GMConstant.ERR_BUILD_IS_NULL;
		if(!player.isBuildThisType(String.valueOf(playerBuild.getBuildType()),index))
			return GMConstant.ERR_BUILD_INDEX_ERROR;
		
		PlayerBuild oldBuild=player.getIsland().getBuildByIndex(index,player.getIsland().getBuilds());
		if(oldBuild!=null)
			player.getIsland().deleteBuild(index);
		playerBuild.setIndex(index);
		playerBuild.setBuildLevel(level);
		playerBuild.init(TimeKit.getSecondTime());
		Island builds=player.getIsland();
		// true为成功 也马上在建筑里面加上
		boolean bool=builds.getBuilds().add(playerBuild);
		//boolean bool=builds.addBuild(playerBuild);
		//成就数据采集
		AchieveCollect.buildLevel(playerBuild,builds,player);
		//新兵福利
		RecruitKit.pushTaskBuild(playerBuild,player);
		if(bool)
			return GMConstant.ERR_SUCCESS;
		return GMConstant.ERR_UNKNOWN;
	}
	
	/** 指挥所等级是否足够 */
	private boolean isEnoughDirector(Player player,int level)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(BuildInfo.INDEX_0,
			builds.getBuilds());
		if(checkBuild==null) return false;
		return checkBuild.getBuildLevel()>=level;
	}
}
