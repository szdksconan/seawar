package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;
import foxu.sea.worldboss.NianBoss;
import foxu.sea.worldboss.WorldBoss;


/**
 * 年兽活动 gm工具控制接口
 * @author yw
 *
 */
public class NianBossActivity extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		CreatObjectFactory factory=info.getObjectFactory();
		int type=TextKit.parseInt(params.get("op_type"));
		int sid=ActivityContainer.NIAN_SID;
		if(type==3)
		{
			String[] state=ActivityContainer.getInstance().getActivityState(sid);
			try
			{
				for(int i=0;i<state.length;i++)
				{
					JSONObject jo=new JSONObject(state[i]);
					jsonArray.put(jo);
				}
			}
			catch(Exception e)
			{
				return GMConstant.ERR_UNKNOWN;
			}
			return GMConstant.ERR_SUCCESS;
		}
		int id=TextKit.parseInt(params.get("id"));
		String stime=params.get("stime");
		String etime=params.get("etime");
		
		int bossSid=TextKit.parseInt(params.get("bossSid"));// boss sid
		Object obj=WorldBoss.factory.getSample(bossSid);
		if(obj==null||!(obj instanceof NianBoss))
			return GMConstant.ERR_PARAMATER_ERROR;
		int attackCD=TextKit.parseInt(params.get("attackCD"));// 攻击间隔
		if(attackCD<0) return GMConstant.ERR_PARAMATER_ERROR;
		String attackAward=params.get("attackAward");// 攻击奖励
		String killAward=params.get("killAward");// 击杀奖励
		String awardsA=params.get("awardsA");// 盟排名奖励
		String awardsP=params.get("awardsP");// 个人排名奖励
		if(!checkAward(attackAward,factory)||!checkAward(killAward,factory)
			||!checkRankAward(awardsA,factory)
			||!checkRankAward(awardsP,factory))
			return GMConstant.ERR_PARAMATER_ERROR;
		String initData=combinInitData(bossSid,attackCD,attackAward,
			killAward,awardsA,awardsP);
		String result=null;
		if(type==1)
		{
			result=ActivityContainer.getInstance().startActivity(sid,stime,
				etime,initData);
		}
		else if(type==2)
		{
			result=ActivityContainer.getInstance().resetActivity(sid,stime,
				etime,TextKit.split(initData,"#")[1],id);
		}
		if(result==null) return GMConstant.ERR_UNKNOWN;
		try
		{
				JSONObject jo=new JSONObject(result);
				jsonArray.put(jo);
		}
		catch(Exception e)
		{
			return GMConstant.ERR_PARAMATER_ERROR;
		}
		return GMConstant.ERR_SUCCESS;
	}
	
	public boolean checkAward(String str,CreatObjectFactory factory)
	{
		try
		{
			String[] sids=TextKit.split(str,",");
			if(sids.length<=0||sids.length%2!=0) return false;
			for(int i=0;i<sids.length;i+=2)
			{
				int sid=TextKit.parseInt(sids[i]);
				int num=TextKit.parseInt(sids[i+1]);
				//现阶段只保证配置的是物品
				if(SeaBackKit.getSidType(sid)==Prop.VALID||num<=0)
					return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
		return true;

	}
	public boolean checkRankAward(String str,CreatObjectFactory factory)
	{
		try
		{
			String[] ranks=TextKit.split(str,"|");
			if(ranks.length<=0) return false;
			for(int i=0;i<ranks.length;i++)
			{
				String[] rinfo=TextKit.split(ranks[i],":");
				if(rinfo.length!=2)return false;
				String[] ranges=TextKit.split(rinfo[0],"-");
				int r0=TextKit.parseInt(ranges[0]);
				int r1=TextKit.parseInt(ranges[1]);
				if(r0<=0||r1<=0||r0>r1)return false;
				if(!checkAward(rinfo[1],factory))return false;		
			}
			
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
	
	/** 组合初始化信息 */
	public String combinInitData(int sid,int cd,String attackAward,String killAward,String awardsA,String awardsP)
	{
		StringBuffer sub=new StringBuffer();
		sub.append(sid+"#");
		sub.append(cd+"&");
		sub.append(attackAward+"&");
		sub.append(killAward+"&");
		sub.append(awardsA+"&");
		sub.append(awardsP);
		return sub.toString();
	}

}
