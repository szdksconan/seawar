package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Science;
import foxu.sea.builds.Build;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.produce.ScienceProduce;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.recruit.RecruitDayTask;
import foxu.sea.recruit.RecruitKit;

/**
 * 设置科技等级
 * 
 * @author comeback
 *
 */
public class ScienceLevel extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String sidStr=params.get("sid");
		String levelStr=params.get("level");
		int sid=Integer.parseInt(sidStr);
		int level=Integer.parseInt(levelStr);
		
		if(level<=0||level>PublicConst.MAX_PLAYER_LEVEL)
			return GMConstant.ERR_PARAMATER_ERROR;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		
		PlayerBuild build=(PlayerBuild)player.getIsland()
			.getBuildByType(Build.BUILD_RESEARCH,null);
		if(build==null)
			return GMConstant.ERR_BUILD_IS_NULL;
		ScienceProduce produce=(ScienceProduce)build.getProduce();
		// 如果科技存在，直接设置等级
		Science science=produce.getScienceBySid(sid);
		if(science!=null)
		{
			if(produce.checkProduceSid(player,sid))
				return GMConstant.ERR_SCIENCE_IS_LEVELING;
			if(level>build.getBuildLevel())level=build.getBuildLevel();
			science.setLevel(level);
			// 新兵福利
			RecruitKit.pushTask(RecruitDayTask.SCIENCE_LV,level,player,true);
			return GMConstant.ERR_SUCCESS;
		}
		return researchScience(player,build,sid,level);
	}
	
	private int researchScience(Player player,PlayerBuild scienceBuild,int sid,int level)
	{
		if(scienceBuild.getBuildLevel()<=0)
			return GMConstant.ERR_BUILD_IS_NULL;
		if(!scienceBuild.checkLevelPropSid(sid))
			return GMConstant.ERR_BUILD_LEVEL_LIMIT;
		ScienceProduce produce=(ScienceProduce)scienceBuild.getProduce();
		Science science=(Science)Science.factory.newSample(sid);
		if(science==null)
			return GMConstant.ERR_PARAMATER_ERROR;
		if(level>scienceBuild.getBuildLevel())level=scienceBuild.getBuildLevel();
		science.setLevel(level);
		produce.addScience(science);
		// 新兵福利
		RecruitKit.pushTask(RecruitDayTask.SCIENCE_LV,level,player,true);
		return GMConstant.ERR_SUCCESS;
	}
}
