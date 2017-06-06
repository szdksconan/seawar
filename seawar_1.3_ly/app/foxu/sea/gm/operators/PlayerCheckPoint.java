package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.checkpoint.SelfCheckPoint;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

public class PlayerCheckPoint extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String checkpsid=params.get("checkpsid");
			String playerName=params.get("player_name");
			String checkpoint=params.get("checkpoint");
			if(playerName==null||playerName.length()==0)
				return GMConstant.ERR_PLAYER_NAME_NULL;
			if(checkpoint==null||checkpoint.length()==0)
				return GMConstant.CHECK_POINT_NULL;
			CreatObjectFactory objectFactory=info.getObjectFactory();
			Player player=objectFactory.getPlayerByName(playerName,false);
			if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
			SelfCheckPoint checkPoint=null;
			if(Integer.parseInt(checkpsid)==1)  checkPoint=player.getSelfCheckPoint();
			else checkPoint=player.getHeritagePoint();
				if(CheckPoint.factory.getSample(Integer.parseInt(checkpoint))==null)
					return GMConstant.CHECK_POINT_ERRO;
				int Surpluschecksid=Integer.parseInt(checkpoint)
								-checkPoint.getCheckPointSid();// 就是输入当地额关卡 查看当前输入的sid和关卡的差值
				int sid=checkPoint.getCheckPointSid();
				for(int i=0;i<=Surpluschecksid;i++)
				{
					CheckPoint point=(CheckPoint)CheckPoint.factory.newSample(sid+i);
					checkPoint.addPoint(3,point.getNextSid(),
						point.getChapter()-1,point.getIndex()*2);
				}
			
		}
		catch(Exception e)
		{
			return GMConstant.CHECK_POINT_ERRO;
		}
		return GMConstant.ERR_SUCCESS;
	}

}
