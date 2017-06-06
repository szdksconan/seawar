package foxu.sea.gm.operators;

import java.util.Map;
import java.util.regex.Pattern;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.proplist.PropList;

/****
 * ���Ӽ�����Ʒ
 * 
 * @author lhj
 * 
 */
public class AddPropInfo extends GMOperator
{

	final static int MAXNUM=65536;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		String sid=params.get("sid");
		String num=params.get("count");
		String more=params.get("more");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
		/** �����޸� */
		if(sid!=null)
		{
			int erro=addProp(player,num,sid);
			if(erro!=0) return erro;
		}
		/** ������� */
		if(more!=null)
		{
			if(!validePropNum(more)) return GMConstant.ERR_VLAUE_ERROR;
			String s[]=more.split(",");
			for(int i=0;i<s.length;i+=2)
			{
				addProp(player,s[i+1],s[i]);
			}
		}
		JBackKit.sendResetBunld(player);
		/** ������Ʒ��Ϣ */
		PropList propList=player.getBundle();
		Prop[] props=propList.getProps();
		for(int i=0;i<props.length;i++)
		{
			if(props[i]==null) continue;
			try
			{
				Prop prop=props[i];
				int count=1;
				if(prop instanceof NormalProp)
					count=((NormalProp)prop).getCount();
				JSONObject jo=new JSONObject();
				// ��Ʒsid
				jo.put(GMConstant.SID,prop.getSid());
				// ��Ʒ����
				jo.put(GMConstant.NAME,prop.getName());
				// ��Ʒ����
				jo.put(GMConstant.COUNT,count);
				jsonArray.put(jo);

			}
			catch(JSONException je)
			{
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
	/***
	 * ��֤����������Ƿ���ȷ
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str)
	{
		Pattern pattern=Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}
	/** ��֤��ǰ�Ķ��޸��Ƿ������������ȷ */
	public boolean validePropNum(String str)
	{
		String s[]=str.split(",");
		for(int i=0;i<s.length;i+=2)
		{
			if(!isInteger(s[i+1])||Integer.parseInt(s[i+1])<0
				||Integer.parseInt(s[i+1])>=MAXNUM) return false;
		}
		return true;
	}
	/** ��ӽ�Ʒ */
	public int addProp(Player player,String num,String sid)
	{
		Integer count=Integer.parseInt(num);
		int id=Integer.parseInt(sid);
		if(count<0||count>=MAXNUM) return GMConstant.ERR_VLAUE_ERROR;
		int nowCount=player.getBundle().getCountBySid(id);
		if(nowCount==count) return GMConstant.ERR_SUCCESS;
		if(nowCount>count)
			player.getBundle().decrProp(id,nowCount-count);
		else
		{
			Prop prop=(Prop)Prop.factory.newSample(Integer.parseInt(sid));
			if(prop instanceof NormalProp)
				((NormalProp)prop).setCount(count-nowCount);
			player.getBundle().incrProp(prop,true);
		}
		return GMConstant.ERR_SUCCESS;
	}
}
