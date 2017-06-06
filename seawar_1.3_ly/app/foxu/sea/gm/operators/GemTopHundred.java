package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.set.Comparator;
import mustang.set.IntKeyHashMap;
import mustang.set.SetKit;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Resources;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/****
 * 
 * 剩余宝石数量前一百名
 * 
 * @author lhj
 * 
 */
public class GemTopHundred extends GMOperator
{

	public static final int TOP_LENTH=100;

	Mycomparator camp=new Mycomparator();

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		IntKeyHashMap cacheMap=info.getObjectFactory().getPlayerCache()
			.getCacheMap();
		Object[] objs=cacheMap.valueArray();
		//SetKit.sort(objs,camp);
		SetKit.shellSort(objs,0,objs.length,camp,false);
		int length=objs.length;
		if(length>TOP_LENTH) length=TOP_LENTH;
		for(int i=0;i<length;i++)
		{
			// 科技名字替换
			PlayerSave player=(PlayerSave)objs[i];
			if(player==null) continue;
			if(player.getData()==null) continue;
			JSONObject jo=new JSONObject();
			try
			{
				jo.put(GMConstant.NAME,player.getData().getName());
				jo.put(GMConstant.GEMS,
					Resources.getGems(player.getData().getResources()));
				jsonArray.put(jo);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		return GMConstant.ERR_SUCCESS;

	}

	/** 实现Comparator接口,也就是定义排序规则 **/
	public class Mycomparator implements Comparator
	{

		public int compare(Object o1,Object o2)
		{
			if(o1==null) return Comparator.COMP_LESS;
			if(o2==null) return Comparator.COMP_GRTR;
			PlayerSave player1=(PlayerSave)o1;
			PlayerSave player2=(PlayerSave)o2;
			if(player1.getData()==null)
				return Comparator.COMP_LESS;
			if(player2.getData()==null)
				return Comparator.COMP_GRTR;
			if(Resources.getGems(player1.getData().getResources())<Resources
				.getGems(player2.getData().getResources()))
				return Comparator.COMP_GRTR;
			else
				return Comparator.COMP_LESS;
		}
	}
}
