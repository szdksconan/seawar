package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Player;
import foxu.sea.Resources;
import foxu.sea.gems.GemsTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * ȫ������
 * @author comeback
 *
 */
public class CompensAll extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String addGems=params.get("add_gems");
		String code=params.get("code");
		String gemsStr=params.get("gems");
		String names=params.get("names");
		if(TextKit.valid(gemsStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		int gems=Integer.parseInt(gemsStr);
		CreatObjectFactory objectFactory=info.getObjectFactory();
		if(addGems.equals("true"))
		{
			if(code.equals("3125098"))
			{
				Object object[]=objectFactory.getPlayerCache()
					.getCacheMap().valueArray();
				int now=TimeKit.getSecondTime();
				for(int i=0;i<object.length;i++)
				{
					PlayerSave data=(PlayerSave)object[i];
					Player allplayer=data.getData();
					Resources.addGemsNomal(gems,
						allplayer.getResources(),allplayer);
					// ����֮�ڵ�¼������ң��ű�������
					if(now-allplayer.getUpdateTime()<3600*72)
					{
						objectFactory.createGemTrack(GemsTrack.SERVER_AWARD,allplayer.getId(),gems,
							0,Resources.getGems(allplayer.getResources()));
						objectFactory.getPlayerCache().getDbaccess().save(allplayer);
					}
				}
				return GMConstant.ERR_SUCCESS;
			}
			else if(code.equals("63023474"))
			{
				StringBuilder sb=new StringBuilder();
				String[] players=TextKit.split(names,",");
				int now=TimeKit.getSecondTime();
				for(int i=0;i<players.length;i++)
				{
					Player allplayer=objectFactory.getPlayerByName(players[i],true);
					if(allplayer!=null)
					{
						Resources.addGemsNomal(gems,
							allplayer.getResources(),allplayer);
						// ����֮�ڵ�¼������ң��ű�������
						if(now-allplayer.getUpdateTime()<3600*72)
						{
							objectFactory.createGemTrack(GemsTrack.SERVER_AWARD,allplayer.getId(),gems,
								0,Resources.getGems(allplayer.getResources()));
							objectFactory.getPlayerCache().getDbaccess().save(allplayer);
						}
					}
					else
					{
						// ��¼δ�����ɹ���
						sb.append(players[i]).append(",");
					}
				}
				
				try
				{
					String faileds=sb.toString();
					JSONObject jo=new JSONObject();
					jo.put(GMConstant.PLAYER_NAME,faileds);
					jsonArray.put(jo);
				}
				catch(JSONException e)
				{
				}
				return GMConstant.ERR_SUCCESS;
			}
		}
		return GMConstant.ERR_UNKNOWN;
	}

}
