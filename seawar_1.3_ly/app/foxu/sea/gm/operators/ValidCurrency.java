package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * Ω˚”√≥‰÷µªı±“
 * 
 * @author yw
 * 
 */
public class ValidCurrency extends GMOperator
{

	static int ADD=0,DEL=1,GET=2;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			int type=TextKit.parseInt(params.get("optype"));
			if(type==GET)
			{
				getValid(jsonArray);
			}
			else
			{
				String valid=params.get("valid");
				String[] valids=TextKit.split(valid,",");
				if(valids==null||valids.length<=0)
					return GMConstant.ERR_PARAMATER_ERROR;
				if(type==ADD)
				{
					addValid(valids,jsonArray);
				}
				else if(type==DEL)
				{
					delValid(valids,jsonArray);
				}
			}
		}
		catch(Exception e)
		{
			return GMConstant.ERR_PARAMATER_ERROR;
		}
		return GMConstant.ERR_SUCCESS;
	}

	public void addValid(String[] valids,JSONArray jsonArray)
		throws JSONException
	{
		if(PublicConst.VALID_CURRENCY==null)
		{
			PublicConst.VALID_CURRENCY=new String[0];
		}
		for(int k=0;k<valids.length;k++)
		{
			if(valids[k].length()<3) continue;
			boolean ishave=false;
			for(int i=0;i<PublicConst.VALID_CURRENCY.length;i++)
			{
				if(valids[k].equalsIgnoreCase(PublicConst.VALID_CURRENCY[i]))
				{
					ishave=true;
					break;
				}
			}
			if(!ishave)
			{
				String[] temp=new String[PublicConst.VALID_CURRENCY.length+1];
				System.arraycopy(PublicConst.VALID_CURRENCY,0,temp,0,
					PublicConst.VALID_CURRENCY.length);
				temp[temp.length-1]=valids[k];
				PublicConst.VALID_CURRENCY=temp;
			}
		}

		getValid(jsonArray);
	}
	public void delValid(String[] valids,JSONArray jsonArray)
		throws JSONException
	{
		if(PublicConst.VALID_CURRENCY==null) return;
		int decr=0;
		for(int k=0;k<valids.length;k++)
		{
			if(valids[k].length()<3) continue;
			for(int i=0;i<PublicConst.VALID_CURRENCY.length;i++)
			{
				if(valids[k].equalsIgnoreCase(PublicConst.VALID_CURRENCY[i]))
				{
					PublicConst.VALID_CURRENCY[i]=null;
					decr++;
				}
			}
		}
		if(decr>0)
		{
			String[] temp=new String[PublicConst.VALID_CURRENCY.length-decr];
			for(int i=0,j=0;i<PublicConst.VALID_CURRENCY.length;i++)
			{
				if(PublicConst.VALID_CURRENCY[i]!=null)
				{
					temp[j]=PublicConst.VALID_CURRENCY[i];
					j++;
				}
			}
			PublicConst.VALID_CURRENCY=temp;
		}
		getValid(jsonArray);
	}
	public void getValid(JSONArray jsonArray) throws JSONException
	{
		if(PublicConst.VALID_CURRENCY==null
			||PublicConst.VALID_CURRENCY.length<=0) return;
		for(int i=0;i<PublicConst.VALID_CURRENCY.length;i++)
		{
			JSONObject json=new JSONObject();
			json.put("valid",PublicConst.VALID_CURRENCY[i]);
			jsonArray.put(json);
		}

	}

}
