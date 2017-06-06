package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Service;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 获取buff
 * 
 * @author lhj
 * 
 */
public class GetBuffInfo extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		/** 获取关卡buff */
		int buff[]=player.getPointBuff();
		/** 拥有的服务 */
		Object[] array=player.getService().getArray();
		if(buff==null && array.length==0)
			return GMConstant.ERR_BUFF_IS_NULL;
		if(buff!=null)
		{
			for(int i=0;i<buff.length;i++)
			{
				JSONObject jsonbuff=new JSONObject();
				try
				{
					jsonbuff.put(GMConstant.SID,"buff_"
						+PublicConst.SHOW_SIDS[i]);
					jsonbuff.put(GMConstant.LEVEL,buff[i]+"_lv");
					jsonArray.put(jsonbuff);
				}
				catch(JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
	
		for(int i=0;i<array.length;i++)
		{
			if(!((Service)array[i]).isOver(TimeKit.getSecondTime()))
			{
				JSONObject jsonbuff=new JSONObject();
				try
				{
					if(array[i]==null) continue;
					Service service=(Service)array[i];
					int endtime=service.getEndTime()-TimeKit.getSecondTime();
					String time=endtime/3600+"h"+endtime%3600/60+"m";
					jsonbuff.put(GMConstant.SID,"buff_"+service.getId());
					jsonbuff.put(GMConstant.LEVEL,time);
					jsonArray.put(jsonbuff);
				}
				catch(JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		return GMConstant.ERR_SUCCESS;
	}

}
