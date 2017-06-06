package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.equipment.Equipment;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

public class AddEquip extends GMOperator
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
		Equipment equ=(Equipment)Equipment.factory.newSample(sid);
		if(equ!=null)
		{
			// if(player.getEquips().getCapacity()>=player.getEquips()
			// .getCount()+num)
			if(player.getEquips().getEquNum(equ.getShipType())>=(player
				.getEquips().getTypeNum(equ.getShipType())+num))
			{
				for(int i=0;i<num;i++)
				{
					Equipment equip=(Equipment)Equipment.factory
						.newSample(sid);
					if(equip==null) return GMConstant.ERR_PROP_IS_NULL;
					equip.addExp(exp);
					String msg=player.getEquips().addEquipment(equip,0);
					if(msg!=null) return GMConstant.ERR_PROP_IS_NULL;
				}
			}
			else
				return GMConstant.ERR_BUNDLE_IS_FULL;
		}
		else
		{
			String msg=player.getEquips().incrQualityStuff(sid,num);
			if(msg!=null) return GMConstant.ERR_PROP_IS_NULL;
		}
		SeaBackKit.createEquipTrackByAutoLeft(sid,num,
			EquipmentTrack.FROM_GM_ADD,EquipmentTrack.ADD,0,player,
			objectFactory);
		JBackKit.sendEquipInfo(player);
		return GMConstant.ERR_SUCCESS;
	}
}
