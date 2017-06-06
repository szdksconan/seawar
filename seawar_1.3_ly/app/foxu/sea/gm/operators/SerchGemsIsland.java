package foxu.sea.gm.operators;

import java.util.Map;

import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.datasave.NpcIsLandSave;
import foxu.sea.GemsNpcIslandManager;
import foxu.sea.NpcIsland;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 查询宝石岛屿的坐标
 * 
 * @author lihon
 *
 */
public class SerchGemsIsland extends GMOperator
{
	/**SERCHE=1 查询宝石到状态  END=2 结束一个宝石岛   OPEN=3 开发宝石岛  CLOSE=4 关闭宝石岛  STATE=5 查询当前状态**/
	public static int SERCHE=1,END=2,OPEN=3,CLOSE=4,STATE=5;
	GemsNpcIslandManager manager;
	
	public static  int OPENS=1,CLOSES=2;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		int type=TextKit.parseInt(params.get("type"));
		if(type==SERCHE || type==END)
		{
			if(type==END)
			{
				String address=params.get("index");
				if(address==null||address.length()==0)
					return GMConstant.ERR_PARAMATER_ERROR;
				int id=TextKit.parseInt(address);
				IntKeyHashMap map=manager.getCahceMap();
				if(map==null||map.size()==0) return GMConstant.ERR_SUCCESS;
				Object[] objects=map.valueArray();
				for(int i=0;i<objects.length;i++)
				{
					if(objects[i]==null) continue;
					NpcIsLandSave islandSave=(NpcIsLandSave)objects[i];
					if(islandSave==null) continue;
					NpcIsland island=islandSave.getData();
					if(island==null) continue;
					if(island.getId()==id)
					{
						manager.removeGemsIsland(island.getIndex());
					}
				}

			}
			try
			{
				IntKeyHashMap map=manager.getCahceMap();
				if(map==null||map.size()==0) return GMConstant.ERR_SUCCESS;
				Object[] objects=map.valueArray();
				for(int i=0;i<objects.length;i++)
				{
					if(objects[i]==null) continue;
					NpcIsLandSave islandSave=(NpcIsLandSave)objects[i];
					if(islandSave==null) continue;
					NpcIsland island=islandSave.getData();
					if(island==null) continue;
					JSONObject json=new JSONObject();
					json.put("id",island.getId());
					json.put("islandType",island.getIslandType());
					json.put("flag",SERCHE);
					json.put("level",island.getIslandLevel());
					json.put("name",island.getName());
					int endtime=island.getEndTime()-TimeKit.getSecondTime();
					String time=endtime/3600+"h"+endtime%3600/60+"m";
					json.put("state",time);
					json.put(GMConstant.LOCATION,island.getIndex()>=0
						?SeaBackKit.getIslandLocation(island.getIndex())
						:"0,0");
					jsonArray.put(json);
				}
			}
			catch(Exception e)
			{
			}
		}
		else if(type==OPEN || type==CLOSE || type==STATE)
		{
			if(type==OPEN)
				PublicConst.GEMS_ISLAND_CLOSE=true;
			else if(type==CLOSE)
			{
				PublicConst.GEMS_ISLAND_CLOSE=false;
				PublicConst.GEMS_ISLAND_CLOSE=false;
				IntKeyHashMap map=manager.getCahceMap();
				if(map==null||map.size()==0) return GMConstant.ERR_SUCCESS;
				Object[] objects=map.valueArray();
				for(int i=0;i<objects.length;i++)
				{
					if(objects[i]==null) continue;
					NpcIsLandSave islandSave=(NpcIsLandSave)objects[i];
					if(islandSave==null) continue;
					NpcIsland island=islandSave.getData();
					if(island==null) continue;
					manager.removeGemsIsland(island.getIndex());
				}
			}
			try
			{
				JSONObject json=new JSONObject();
				int close=2;
				if(PublicConst.GEMS_ISLAND_CLOSE) close=OPENS;
				json.put("state",close);
				json.put("flag",STATE);
				jsonArray.put(json);
			}
			catch(Exception e)
			{
			}

		}
		return GMConstant.ERR_SUCCESS;
	}

	public void setManager(GemsNpcIslandManager manager)
	{
		this.manager=manager;
	}
}
