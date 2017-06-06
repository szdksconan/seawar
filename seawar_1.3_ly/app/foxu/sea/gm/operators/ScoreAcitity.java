package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.sea.Ship;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.equipment.Equipment;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.OfficerManager;
import foxu.sea.proplist.Prop;

/***
 *  评分活动
 * @author lhj
 *
 */
public class ScoreAcitity extends GMOperator
{
	
	//OPEN=1开启  CLOSE=2 关闭 ADD=3添加 UPDATE=4 修改 ELECT=5 查询
	public static final int OPEN=1,CLOSE=2,ADD=3,UPDATE=4,SELECT=5;
	public static final int POR_LENFTH=8;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String platids=params.get("platids");
		//原因
		String reason=params.get("reason");
		//sid
		String psid=params.get("sid");
		//活动id
		String aid=params.get("id");
		//奖励
		String award=params.get("award");
		//url 地址
		String url=params.get("url");
		if(url!=null)
			url=url.trim();
		//方式
		String op_type=params.get("op_type");
		//开始时间
		String starttime=params.get("stime");
		//结束时间
		String endtime=params.get("etime");
		
		int  type=TextKit.parseInt(op_type);
		 
		int stime=SeaBackKit.parseFormatTime(starttime);
		int etime=SeaBackKit.parseFormatTime(endtime);
		//平台
		String platid=params.get("platid");
		int sid=TextKit.parseInt(psid);
		String ret=null;
		String[] state=null;
		String initData="";
		int id=0;
		if(type==OPEN || type==UPDATE)
		{
			if(sid==ActivityContainer.APP_GRADE_ID
				&&(reason==null||reason.trim().length()==0))
				return GMConstant.ERR_SCORE_REASON_IS_NULL;
			if(award==null || award.trim().length()==0)
				return GMConstant.ERR_AWARD_ERRO;
			int result=validatPro(award);
			if(result!=GMConstant.ERR_SUCCESS)
				return result;
			if(type==UPDATE )
			{
				if(aid==null || aid.length()==0)
					return GMConstant.ERR_SCORE_ID_IS_NULL;
				 id=TextKit.parseInt(aid);
			}
			initData=type+";"+id+";"+platid+","+url+";"+platids+";"+award+";"+reason;
		}
		if(type==CLOSE || type==ADD)
		{
			if(aid==null || aid.length()==0)
				return GMConstant.ERR_SCORE_ID_IS_NULL;
			initData=type+";"+id+";"+platid+","+url+";"+platids;
		}
		if(type!=SELECT  && type!=CLOSE &&  type!=ADD  && type!=UPDATE)
		{
			if(stime>=etime || etime<TimeKit.getSecondTime())
				return GMConstant.ERR_TIME_ERRO;
		}
		if(type==SELECT)
			state=ActivityContainer.getInstance().getActivityState(sid);
		 if(type==OPEN)
			ret=ActivityContainer.getInstance().startActivity(sid,starttime,endtime,initData);
		if(type!=OPEN  && type!=SELECT)
			ret=ActivityContainer.getInstance().resetActivity(sid,starttime,endtime,initData,id);
		if(ret==null&&state==null) return GMConstant.ERR_UNKNOWN;
		try
		{
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return GMConstant.ERR_SUCCESS;
	}

	/**验证配置奖励的物品*/
	  public int validatPro(String award)
	  {
		  if(award==null ||award.length()==0)
			  return GMConstant.ERR_PROINFO_IS_NULL;
		  if(SeaBackKit.checkBlank(award))
				return GMConstant.ERR_PRO_IS_SPACE;
		  	String[] proList=award.split(",");
		  	if(proList.length%2!=0 || proList.length>POR_LENFTH)
			  	return GMConstant.ERR_PRO_ERRO_LENGTH;
		for(int i=0;i<proList.length;i+=2)
		{
			if(SeaBackKit.getSidType(TextKit.parseInt(proList[i]))==Prop.VALID)
			{
				return GMConstant.ERR_PRO_IS_NOT_PRO;
			}
		}
		  	return GMConstant.ERR_SUCCESS;
	  }
}
