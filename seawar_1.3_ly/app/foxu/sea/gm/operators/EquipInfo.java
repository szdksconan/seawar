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
 * ��ѯ���װ����Ϣ
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
	/** ȫ��װ����ϸ */
	public int printEquip(Player player,JSONArray jsonArray,
		boolean justEquiped)
	{
		// ��player�л�ȡװ��ϵͳ
		EquipList equipList=player.getEquips();
		// ��װ��ϵͳ�л�ȡװ���뾭�����
		ArrayList pequips=equipList.getEquips();
		if(pequips.size()>0)
		{
			// ��ӡ
			for(int j=0;j<pequips.size();j++)
			{
				Equipment equip=(Equipment)pequips.get(j);
				if(justEquiped&&!equip.isEquiped()) continue;
				if(!equip.isUpgr()) continue;
				int nextLvExp=0;
				int currentLvExp=0;
				if(equip.getLevel()>1)
				{
					// ��������ǰ�ȼ����ܾ���
					int lastLvExp=equip.getLevelExps()[equip.getLevel()-2];
					// ��������һ�����ܾ����ȥ��ǰ�ȼ�������ܾ��鼴��ǰ�����������������
					// �����ǰ����δ�����
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
	/** ȫ��װ��ͳ�� */
	public int printEquipNum(Player player,JSONArray jsonArray)
	{
		// ��player�л�ȡװ��ϵͳ
		EquipList equipList=player.getEquips();
		// ��װ��ϵͳ�л�ȡװ���뾭�����
		ArrayList pequips=equipList.getEquips();
		// ��װ��ϵͳ�л�ȡ���ײ���
		IntKeyHashMap pstuffs=equipList.getQualityMap();
		// װ������
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
		// ��ӡ
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
