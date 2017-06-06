package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;


public class AddMouthCard extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String days=params.get("days");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		if(days==null || days.length()==0)
			return GMConstant.ERR_CARD_DAYS_NULL;
		int lastendtime=0;
		int endtime=0;
		lastendtime=player.getAttributes(PublicConst.END_TIME)==null?0:Integer
			.parseInt(player.getAttributes(PublicConst.END_TIME));
		if(lastendtime>TimeKit.getSecondTime())
			endtime=lastendtime
				+(Integer.parseInt(days)*PublicConst.DAY_SEC);
		else
		{
			lastendtime=(SeaBackKit.getTimesnight()-TimeKit
				.getSecondTime());// 获取当天到凌晨的系统时间
			endtime=TimeKit.getSecondTime()
				+((Integer.parseInt(days)-1)*PublicConst.DAY_SEC)
				+lastendtime;
		}
		if(endtime<=TimeKit.getSecondTime())
				endtime=TimeKit.getSecondTime();
//		int vp=player.getAttributes(PublicConst.VIP_POINT)==null?0:Integer.parseInt(player.getAttributes(PublicConst.VIP_POINT));
//		player.setAttribute(PublicConst.VIP_POINT,String.valueOf(vp+PublicConst.VIPPOINT_NUM));
		player.setAttribute(PublicConst.END_TIME,String.valueOf(endtime));
		AchieveCollect.mouthCard(1,player);
		JBackKit.sendMouthCard(player);
		JBackKit.sendResetResources(player);
		
		return 0;
	}

}
