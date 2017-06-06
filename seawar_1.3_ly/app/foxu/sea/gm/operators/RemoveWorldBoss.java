package foxu.sea.gm.operators;

import java.util.Map;

import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.WorldBossSave;
import foxu.sea.NpcIsland;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.FightKit;
import foxu.sea.worldboss.WorldBoss;

/***
 * ÒÆ³ýÁªÃËBOSS
 * 
 * @author lhj
 * 
 */
public class RemoveWorldBoss extends GMOperator
{

	public static int MAX_WITH=600,MAX_HIGHT=600;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{

		String address=params.get("address");
		if(address==null||address.length()==0)
			return GMConstant.ERR_CONTENT_NULL;
		String[] add=address.split(",");
		if(add.length<2) return GMConstant.ERR_ADDRESS_IS_ERROR;
		int x=TextKit.parseInt(add[0]);
		int y=TextKit.parseInt(add[1]);
		if(x>MAX_WITH||y>MAX_HIGHT) return GMConstant.ERR_ADDRESS_IS_ERROR;
		int index=600*(y-1)+(x-1);
		CreatObjectFactory factory=info.getObjectFactory();
		NpcIsland beIsland=factory.getIslandCache().load(index+"");
		if(beIsland!=null&&beIsland.getIslandType()==NpcIsland.WORLD_BOSS)
		{
			IntKeyHashMap bossMap=factory.getWorldBossCache().getCacheMap();
			Object[] bosssaves=bossMap.valueArray();
			for(int i=0;i<bosssaves.length;i++)
			{
				if(bosssaves[i]==null) continue;
				WorldBossSave bosssave=(WorldBossSave)bosssaves[i];
				if(bosssave==null) continue;
				WorldBoss boss=(WorldBoss)bosssave.getData();
				if(boss==null) continue;
				if(boss.getIndex()==index)
				{
					if(boss.getFleetNowNum()==0)
					{
						// ÒÆ³ýbossµºÓì
						beIsland.updateSid(FightKit.WATER_ISLAND_SID);
						// Ë¢ÐÂÇ°Ì¨
						foxu.sea.kit.JBackKit.flushIsland(
							factory.getDsmanager(),beIsland,factory);
						return GMConstant.ERR_SUCCESS;
					}
					return GMConstant.ERR_WORLD_HAVE_BLOOD;
				}
			}

		}
		return GMConstant.ERR_WORLD_ADDRESS_ERROR;
	}

}
