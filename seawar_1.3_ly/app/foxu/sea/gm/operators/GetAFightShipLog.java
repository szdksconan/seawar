package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.set.IntList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.OrderList;
import foxu.sea.Ship;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancefight.AfihgtShipData;
import foxu.sea.alliance.alliancefight.ShipRecord;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


public class GetAFightShipLog extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String aName=params.get("alliance_name");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Alliance alliace=objectFactory.getAllianceMemCache().loadByName(aName,false);
		if(alliace==null)
			return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
		// ��ȡ��־
		OrderList list=AfihgtShipData.instance().getShipRecords(alliace.getId());
		// ���û����־��ֱ�ӷ���
		if(list==null||list.size()<=0)
			return GMConstant.ERR_SUCCESS;
		for(int i=0;i<list.size();i++)
		{
			ShipRecord record=(ShipRecord)list.get(i);
			// һ����־
			JSONObject jo=new JSONObject();
			try
			{
				// ������Ϣ
				jo.put(GMConstant.EVENT_TYPE,record.getType());
				jo.put(GMConstant.TIME,record.getCreateTime());
				jo.put(GMConstant.EXTRA_INFO,record.getTarget());
				// �ۿڴ�ֻ
				JSONArray portShip=getShipInfo(record.getLeft());
				jo.put(GMConstant.PORT_SHIPS,portShip);
				// δ����Ĵ�ֻ
				JSONArray groundShip=getShipInfo(record.getBleft());
				jo.put(GMConstant.GROUND_SHIPS,groundShip);
				// ��ǰ�����¼�
				JSONArray changeInfo=getShipInfo(record.getList());
				jo.put(GMConstant.CHANGE_SHIPS,changeInfo);
				
				jsonArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return GMConstant.ERR_SUCCESS;
	}

	/**
	 * ������ֻ��Ϣ
	 * @param list sid,count��
	 * @return
	 */
	private JSONArray getShipInfo(IntList list)
	{
		JSONArray joArray= new JSONArray();
		if(list==null||list.size()<=0)return joArray;
		for(int i=0;i<list.size();i+=2)
		{
			Ship ship=(Ship)Ship.factory.getSample(list.get(i));
			try
			{
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.NAME,ship.getName());
				jo.put(GMConstant.COUNT,list.get(i+1));
				joArray.put(jo);
			}
			catch(JSONException e)
			{
			}
		}
		return joArray;
	}
	
}
