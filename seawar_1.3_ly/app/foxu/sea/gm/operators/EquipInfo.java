package foxu.sea.gm.operators;

import java.util.Map;

import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.sea.Player;
import foxu.sea.equipment.EquipList;
import foxu.sea.equipment.Equipment;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 查询玩家装备信息
 * 
 * @author Alan
 */
public class EquipInfo extends GMOperator
{

	public static final int ALL_EQUIP=1,EQUIPED_WARE=2,ALL_COUNT=3;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String name=params.get("name");
		int type=TextKit.parseInt(params.get("type"));
		Player player=info.getObjectFactory().getPlayerByName(name,false);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		if(type==ALL_EQUIP)
		{
			return printEquip(player,jsonArray,false);
		}
		else if(type==EQUIPED_WARE)
		{
			return printEquip(player,jsonArray,true);
		}
		else if(type==ALL_COUNT)
		{
			return printEquipNum(player,jsonArray);
		}
		return GMConstant.ERR_SUCCESS;
	}
	/** 全部装备详细 */
	public int printEquip(Player player,JSONArray jsonArray,
		boolean justEquiped)
	{
		// 从player中获取装备系统
		EquipList equipList=player.getEquips();
		// 从装备系统中获取装备与经验道具
		ArrayList pequips=equipList.getEquips();
		if(pequips.size()>0)
		{
			// 打印
			for(int j=0;j<pequips.size();j++)
			{
				Equipment equip=(Equipment)pequips.get(j);
				if(justEquiped&&!equip.isEquiped()) continue;
				if(!equip.isUpgr()) continue;
				int nextLvExp=0;
				int currentLvExp=0;
				if(equip.getLevel()>1)
				{
					// 升级到当前等级的总经验
					int lastLvExp=equip.getLevelExps()[equip.getLevel()-2];
					// 升级到下一级的总经验除去当前等级所需的总经验即当前级数所需的升级经验
					// 如果当前级数未到最高
					if(equip.getLevel()-1<equip.getLevelExps().length)
					{
						nextLvExp=equip.getLevelExps()[equip.getLevel()-1]
							-lastLvExp;
						currentLvExp=equip.getExp()-lastLvExp;
					}
				}
				else
				{
					nextLvExp=equip.getLevelExps()[0];
					currentLvExp=equip.getExp();
				}
				try
				{
					JSONObject json=new JSONObject();
					json.put("uid",equip.getUid());
					json.put("sid",equip.getSid());
					json.put("lv",equip.getLevel());
					json.put("nowexp",currentLvExp);
					json.put("nextlvexp",nextLvExp);
					json.put("state",equip.isEquiped());
					json.put("totalexp",equip.getExp());
					json.put("count","--");
					jsonArray.put(json);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return GMConstant.ERR_UNKNOWN;
				}
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
	/** 全部装备统计 */
	public int printEquipNum(Player player,JSONArray jsonArray)
	{
		// 从player中获取装备系统
		EquipList equipList=player.getEquips();
		// 从装备系统中获取装备与经验道具
		ArrayList pequips=equipList.getEquips();
		// 从装备系统中获取进阶材料
		IntKeyHashMap pstuffs=equipList.getQualityMap();
		// 装入数据
		IntKeyHashMap infos=new IntKeyHashMap();
		for(int i=0;i<pequips.size();i++)
		{
			Equipment equip=(Equipment)pequips.get(i);
			Integer num=1;
			if(infos.get(equip.getSid())!=null)
			{
				num=(Integer)infos.get(equip.getSid())+1;
			}
			infos.put(equip.getSid(),num);
		}
		int[] sids=pstuffs.keyArray();
		for(int i=0;i<sids.length;i++)
		{
			Integer num=(Integer)pstuffs.get(sids[i]);
			infos.put(sids[i],num);
		}
		// 打印
		sids=infos.keyArray();
		for(int i=0;i<sids.length;i++)
		{
			try
			{
				JSONObject json=new JSONObject();
				json.put("uid","--");
				json.put("sid",sids[i]);
				json.put("lv","--");
				json.put("nowexp","--");
				json.put("nextlvexp","--");
				json.put("state","--");
				json.put("totalexp","--");
				json.put("count",(Integer)infos.get(sids[i]));
				jsonArray.put(json);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return GMConstant.ERR_UNKNOWN;
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
}
