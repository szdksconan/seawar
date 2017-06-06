package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerFragment;
import foxu.sea.officer.OfficerManager;
import foxu.sea.officer.OfficerTrack;

/**
 * 增加军官物件
 * @author Alan
 *
 */
public class AddOfficer extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("name");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		String sidStr=params.get("sid");
		String expStr=params.get("exp");
		String numStr=params.get("num");
		if(sidStr==null||!sidStr.matches("\\d+")||expStr==null
			||!expStr.matches("\\d+")||numStr==null||!numStr.matches("\\d+"))
			return GMConstant.ERR_PARAMATER_ERROR;
		int sid=Integer.valueOf(sidStr);
		int exp=Integer.valueOf(expStr);
		int num=Integer.valueOf(numStr);
		if(OfficerManager.factory.getSample(sid) instanceof Officer)
		{
			// if(player.getEquips().getCapacity()>=player.getEquips()
			// .getCount()+num)
			for(int i=0;i<num;i++)
			{
				Officer officer=OfficerManager.getInstance().addOfficer(sid,
					player);
				officer.incrExp(exp);
				int lv=officer.getLevel();
				for(int j=0;j<PublicConst.OFFICER_RANK_LV.length;j++)
				{
					if(PublicConst.OFFICER_RANK_LV[j]>lv)
					{
						officer.setMilitaryRank(j+1);
						break;
					}
				}
				info.getObjectFactory().createOfficerTrack(
					OfficerTrack.ADD,
					OfficerTrack.FROM_GM_ADD,
					player.getId(),
					sid,
					1,
					officer.getId(),
					OfficerManager.getInstance().getOfficerOrFragmentCount(
						player,officer.getSid()));
			}
		}
		else if(OfficerManager.factory.getSample(sid) instanceof OfficerFragment)
		{
			OfficerManager.getInstance()
				.addOfficerOrFragment(player,sid,num);
			info.getObjectFactory().createOfficerTrack(
				OfficerTrack.ADD,
				OfficerTrack.FROM_GM_ADD,
				player.getId(),
				sid,
				num,
				0,
				OfficerManager.getInstance().getOfficerOrFragmentCount(
					player,sid));
		}
		else
			return GMConstant.ERR_PARAMATER_ERROR;
		JBackKit.sendOfficerInfo(player);
		return GMConstant.ERR_SUCCESS;
	}
}
