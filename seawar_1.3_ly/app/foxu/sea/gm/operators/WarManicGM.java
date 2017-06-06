package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;


/**
 * 战争狂人 gm接口
 * @author yw
 *
 */
public class WarManicGM extends GMOperator
{
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String idStr=params.get("id");
			String op_typeStr=params.get("op_type");// 开，关，修改
			String stime=params.get("stime");
			String etime=params.get("etime");
			// boss&elite&hostile&arms
			String socreA=params.get("socreA");// 积分奖励
			String rankp=params.get("rankp");
			String ranka=params.get("ranka");
			if(!checkScoreParams(socreA)||checkRankParams(rankp)
				||checkRankParams(ranka))
			{
				return GMConstant.ERR_PARAMATER_ERROR;
			}
			String initData=socreA+"&"+rankp+"&"+ranka;
			int type=TextKit.parseInt(op_typeStr);
			String ret=null;
			String[] state=null;
//			System.out.println("----------type------:"+type);
			if(type==1)
			{
				ret=ActivityContainer.getInstance().startActivity(
					ActivityContainer.WAR_MANIC_ID,stime,etime,initData);
			}
			else if(type==2)
			{
				int id=TextKit.parseInt(idStr);
				ret=ActivityContainer.getInstance().resetActivity(
					ActivityContainer.WAR_MANIC_ID,stime,etime,initData,id);
			}
			else
			{
				state=ActivityContainer.getInstance().getActivityState(
					ActivityContainer.WAR_MANIC_ID);
			}
			if(ret==null&&state==null) return GMConstant.ERR_UNKNOWN;
			if(ret!=null)
			{
				JSONObject jo=new JSONObject(ret);
				jsonArray.put(jo);
			}
			else
			{
				for(int i=0;i<state.length;i++)
				{
					JSONObject jo=new JSONObject(state[i]);
					jsonArray.put(jo);
				}
			}
			return GMConstant.ERR_SUCCESS;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return GMConstant.ERR_UNKNOWN;
	}
	
	/** 检测积分参数合法性 */
	public boolean checkScoreParams(String scoreA)
	{
		//100:0,3053,1|200:0,2022,5&100:50,41202,1|200:100,10003,1
		String[] s0=TextKit.split(scoreA,"&");
		if(s0.length!=4) return false;
		for(int i=0;i<s0.length;i++)
		{
			if("null".equals(s0[i])) continue;
			String[] s1=TextKit.split(s0[i],"|");
			for(int k=0;k<s1.length;k++)
			{
				int h=s1[k].indexOf(",");
				String[] hs=TextKit.split(s1[k].substring(0,h),":");
				int[] hs2=TextKit.parseIntArray(hs);
				if(hs2.length!=2||hs2[0]<0||hs2[1]<0)return false;
				String[] s2=TextKit.split(s1[k].substring(h+1),",");
				int[] ss2=TextKit.parseIntArray(s2);
				if(ss2.length<=0||ss2.length%2!=0)return false;
				for(int n=0;n<ss2.length;n+=2)
				{
					if(SeaBackKit.getSidType(ss2[n])==Prop.VALID)
						return false;
					if(ss2[n+1]<=0) return false;
				}
			}
		}
		return true;
	}
	
	/** 检测排名参数合法性 */
	public boolean checkRankParams(String rankA)
	{
		// 1-1:500,41201,6,902,50|2-2:400,41201,5,902,35
		String[] s1=TextKit.split(rankA,"|");
		for(int k=0;k<s1.length;k++)
		{
			int h=s1[k].indexOf(",");
			String[] hs=TextKit.split(s1[k].substring(0,h),":");
			if(hs.length!=2) return false;
			int gem=TextKit.parseInt(hs[1]);
			if(gem<0) return false;
			String[] hss=TextKit.split(hs[0],"-");
			int[] hss2=TextKit.parseIntArray(hss);
			if(hss2.length!=2||hss2[0]<1||hss2[0]>hss2[1]) return false;
			String[] s2=TextKit.split(s1[k].substring(h+1),",");
			int[] ss2=TextKit.parseIntArray(s2);
			if(ss2.length<=0||ss2.length%2!=0) return false;
			for(int n=2;n<ss2.length;n+=2)
			{
				if(SeaBackKit.getSidType(ss2[n])==Prop.VALID) return false;
				if(ss2[n+1]<=0) return false;
			}
		}
		return false;
	}

}
