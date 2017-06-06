package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.recruit.RecruitDayTask;
import foxu.sea.recruit.RecruitKit;


/***
 * �������ͳ���ȼ�
 * @author lhj
 *
 */
public class SetPlayerCommand extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		String command=params.get("command_level");
		int command_level=TextKit.parseInt(command);
		if(command_level>PublicConst.MAX_PLAYER_LEVEL)
				command_level=PublicConst.MAX_ALLIANCE_LEVEL;
		player.setPlayerCommonLevel(command_level);
		//�ɾ����ݲɼ�
		AchieveCollect.commandLevel(player);
		// �±�����
		RecruitKit.pushTask(RecruitDayTask.COMMAND_LV,
			player.getCommanderLevel(),player,true);
		return 0;
	}

}
