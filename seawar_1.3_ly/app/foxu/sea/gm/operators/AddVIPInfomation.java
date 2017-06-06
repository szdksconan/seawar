package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/***
 * 修改vip等级和成长值
 * @author lhj
 *
 */
public class AddVIPInfomation extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String num=params.get("value");//vip等级
		String vppointvalue=params.get("vppoint");//vip成长点
		boolean flag=true;
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		if(num!=null&&num.length()!=0)
		{
			flag=false;
			char c=num.charAt(0);
			if(TextKit.valid(c,TextKit.NUMBER))
			{
				int level=Integer.parseInt(num);
				if(level<0||level>=10) return GMConstant.ERR_VIPLEVEL_LIMIT;
				int tarValue=0;
				if(level!=0)
					tarValue=PublicConst.GEMS_FOR_VIP_LEVEL[level-1];
				long currValue=player.getVpPoint();
				player.addGrowthPoint(tarValue-currValue);
			}
		}
		if(vppointvalue!=null&&vppointvalue.length()!=0)
		{
			flag=false;
			player.addGrowthPoint(Integer.parseInt(vppointvalue));
		}
		if(flag) return GMConstant.ERR_VLAUE_ERROR;
		player.flushVIPlevel();
		return GMConstant.ERR_SUCCESS;
	}
}
