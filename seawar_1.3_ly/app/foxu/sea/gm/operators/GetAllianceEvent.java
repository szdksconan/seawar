package foxu.sea.gm.operators;

import java.util.ArrayList;
import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/***
 * 获取联盟事件
 * 
 * @author lihongji
 * 
 */
public class GetAllianceEvent extends GMOperator
{

	/** boss的id和战胜boss可以获得的升级联盟点数 */
	public static final int BOSS_SIDS[]={12001,1,12002,2,12003,3,12004,4,
		12005,5,12006,6,12007,7,12008,8,12009,9,12010,10,12011,11,12012,12,
		12013,13,12014,14,12015,15,12016,16,12017,17,12018,18,12019,19,
		12020,20};
	CreatObjectFactory objectFactory;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String e_type=params.get("type");
			String allianceName=params.get("name");
			objectFactory=info.getObjectFactory();
			Alliance alliance=objectFactory.getAllianceMemCache()
				.loadByName(allianceName,false);
			if(alliance==null) return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
			// 获取联盟事件
			ArrayList<AllianceEvent> events=alliance.getEventList();
			if(events==null||events.size()==0)
				return GMConstant.ERR_ALLIANCE_EVENT_NULL;
			int type=TextKit.parseInt(e_type);
			boolean flag=true;
			for(int i=0;i<events.size();i++)
			{
				AllianceEvent event=events.get(i);
				if(event==null) continue;
				if(type==0||event.getEventType()==type)
				{
					JSONObject jo=getEventInfo(event);
					if(jo==null) continue;
					jsonArray.put(jo);
					flag=false;
				}
			}
			if(flag) return GMConstant.ERR_ALLIANCE_EVENT_NULL;
		}
		catch(Exception e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}

	/** 获取数据 **/
	public JSONObject getEventInfo(AllianceEvent event)
	{
		JSONObject jo=new JSONObject();
		try
		{
			jo.put(GMConstant.E_PLAYERNAME,event.getPlayerName());
			jo.put(GMConstant.E_PASSIVENAME,event.getPassiveName());
			if(event.getEventType()==AllianceEvent.ALLIANCE_BOSS_FIGHT)
			{
				for(int j=0;j<BOSS_SIDS.length;j+=2)
				{
					if(BOSS_SIDS[j]==TextKit.parseInt(event.getPassiveName()))
					{
						jo.put(GMConstant.EXTRA_INFO,BOSS_SIDS[j+1]);
					}
				}
			}
			else
				jo.put(GMConstant.EXTRA_INFO,event.getExtraInfo());
			jo.put(GMConstant.TYPE,event.getEventType());
			jo.put(GMConstant.TIME,
				SeaBackKit.formatDataTime(event.getCreate_at()));
		}
		catch(Exception e)
		{
			return new JSONObject();
		}
		return jo;
	}

}
