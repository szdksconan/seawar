package foxu.sea.gm.operators;

import java.util.Map;

import mustang.set.IntList;
import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.DonateRank;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/***
 * 
 * @author ��ȡ���׵�
 * 
 */
public class GetAllianceValue extends GMOperator
{

	// PLAYER_NAME=1������� ALLIANCE_NAME=2 ��������
	public static final int PLAYER_NAME=1,ALLIANCE_NAME=2;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String name=params.get("serchname");
			String type=params.get("type");
			if(name==null||name.length()==0)
				return GMConstant.ERR_CONTENET_IS_NULL;
			CreatObjectFactory factory=info.getObjectFactory();
			// ��ҵĹ��׵�
			if(TextKit.parseInt(type)==1)
			{
				Player player=factory.getPlayerByName(name,false);
				if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
				if(player.getAttributes(PublicConst.ALLIANCE_ID)==null)
					return GMConstant.ERR_PLAYER_NO_ALLIANCE;
				Alliance alliance=info.getObjectFactory().getAlliance(TextKit.parseInt(player.getAttributes(PublicConst.ALLIANCE_ID)),false);
				if(alliance==null) 
					return GMConstant.ERR_PLAYER_NO_ALLIANCE;
				JSONObject object=new JSONObject();
				// ���õ�ǰΪ��һ�����
				object.put(GMConstant.PLAYER_NAME+"0",name);
				int value=0;
				DonateRank rank=(DonateRank)alliance.getGiveValue().get(player.getId());
				if(rank==null) value=0;
				else value=(int)rank.getTotleValue();
				object.put(GMConstant.ALLIANCE_VALUE,value);
				jsonArray.put(object);
			}
			// ���˵Ĺ��׵�
			else
			{
				Alliance alliance=factory.getAllianceMemCache().loadByName(
					name,false);
				if(alliance==null) return GMConstant.ERR_ALLIANCE_EXISTS;
				IntList list=alliance.getPlayerList();
				int count=0;
				for(int i=0;i<list.size();i++)
				{
					int playerId=list.get(i);
					Player player=factory.getPlayerById(playerId);
					if(player==null) continue;
					JSONObject object=new JSONObject();
					object
						.put(GMConstant.PLAYER_NAME+count,player.getName());
					int value=0;
					DonateRank rank=(DonateRank)alliance.getGiveValue().get(player.getId());
					if(rank==null) value=0;
					else value=(int)rank.getTotleValue();
					object.put(GMConstant.ALLIANCE_VALUE,value);
					count++;
					jsonArray.put(object);
				}
			}
		}
		catch(Exception e)
		{
		}
		return GMConstant.ERR_SUCCESS;
	}

}
