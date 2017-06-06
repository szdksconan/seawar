package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/***
 * ���ð�״̬
 * 
 * @author lhj
 * 
 */
public class SetBindingState extends GMOperator
{

	// ��ַ
	public static int ADDRESS=1;
	// ���ð�״̬
	public static int BINGTYPE=0;
	/** ��ѯ״̬,�ر� **/
	public static int BINGD_SELECT=2,BINGD_CLO=3;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		/** ����ת���ǰ� **/
		String bindingState=params.get("state");
		String bindingStr=params.get("bindingStr");
		String bType=params.get("type");
		String platid=params.get("platid");
		String platids=params.get("platids");
		int type=TextKit.parseInt(bType);
		// ��ѯ
		if(type==BINGD_SELECT)
		{
			JSONObject jo=new JSONObject();
			try
			{
				//���ð󶨵�״̬
				jo.put("bingstate","state_"+PublicConst.GAME_BINDING);
				//ԭ��
				if(PublicConst.BINDING_REASON==null || PublicConst.BINDING_REASON.length()==0)
					PublicConst.BINDING_REASON="��";
				jo.put("bingreason",PublicConst.BINDING_REASON);
				jo.put("platIds",PublicConst.BINDING_PLATID);
				jsonArray.put(jo);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		// �ر�
		else if(type==BINGD_CLO)
		{
			PublicConst.BINDING_REASON=null;
			PublicConst.GAME_BINDING=0;
			PublicConst.BINDING_PLATID="";
		}
		// ����
		else
		{
			if(PublicConst.JUMP_ADDRESS!=0)
			{
				return GMConstant.ERR_BINDING_OPNE;
			}
			int state=TextKit.parseInt(bindingState);
			if(bindingStr==null||bindingStr.length()==0)
				return GMConstant.ERR_REASON_IS_NULL;
			PublicConst.BINDING_REASON=bindingStr;
			PublicConst.GAME_BINDING=state;
			if(state==PublicConst.ADVISE_BINDING)
			{
				if(SeaBackKit.isSameDay(PublicConst.START_BINDING_TIME,
					TimeKit.getSecondTime()))
					removeState(info.getObjectFactory());
				PublicConst.START_BINDING_TIME=TimeKit.getSecondTime();
			}
			/** ��ǰ�󶨵�ƽ̨ **/
			if(platid.equals("all"))
			{
				PublicConst.BINDING_PLATID=platids;
				return GMConstant.ERR_SUCCESS;
			}
			String binState=PublicConst.BINDING_PLATID;
			if(binState!=null&&binState.length()!=0)
			{
				if(isContainValue(binState.split(","),platid))
					return GMConstant.ERR_BINDING_OPEN;
			}
			if(binState==null)
				PublicConst.BINDING_PLATID=platid;
			else 
				PublicConst.BINDING_PLATID=binState+","+platid;
		}
		return GMConstant.ERR_SUCCESS;
	}

	/*** ȥ��������ϵ�״̬ **/
	public void removeState(CreatObjectFactory factory)
	{
		IntKeyHashMap cache=factory.getPlayerCache().getCacheMap();
		for(int i=0;i<cache.size();i++)
		{
			PlayerSave psave=(PlayerSave)cache.get(i);
			if(psave==null) continue;
			Player player=psave.getData();
			if(player==null) continue;
			player.setAttribute(PublicConst.PLAYER_BTIME,"");
		}
	}
	
	/** �ж�ĳ�������Ƿ����ĳ��ֵ */
	public static boolean isContainValue(String[] a,String value)
	{
		if(a==null) return false;
		for(int i=a.length-1;i>=0;i--)
		{
			if(a[i].equals(value)) return true;
		}
		return false;
	}
	
}
