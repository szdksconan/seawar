package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.orm.ConnectionManager;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.AwardActivity;
import foxu.sea.activity.DiscountActivity;
import foxu.sea.activity.LimitSaleActivity;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 查询活动玩家统计
 * 
 * @author yw
 * 
 */
public class GetActPlayerdata extends GMOperator
{

	int p_max=100;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			// page 1-n
			int page=Integer.parseInt(params.get("page"));
			boolean next=Boolean.parseBoolean(params.get("next"));
//			System.out.println("--------next-------::"+next);
			if(next)
				page++;
			else
				page--;
			if(page<1) return GMConstant.ERR_ACTIVITY_MIN_PAGE;
//			System.out.println("--------page-------::"+page);
			int index=(page-1)*p_max;
			CreatObjectFactory objfactory=info.getObjectFactory();
			ConnectionManager cm=((SqlPersistence)objfactory
				.getActivityLogMemCache().getDbaccess().getGamePersistence())
				.getConnectionManager();
			int id=Integer.parseInt(params.get("id"));
			int sid=Integer.parseInt(params.get("sid"));
			String sql=null;
			Fields field=null;
			JSONObject json=new JSONObject();
			json.put("sid",sid);
			json.put("id",id);
			json.put("page",page);
			jsonArray.put(json);

			Activity act=ActivityContainer.getInstance().getDbAccess()
				.getActById(id,objfactory);
			if(act==null) return GMConstant.ERR_ACTIVITY_NOT_EXISTS;
			int[] sids=null;
			if(sid==ActivityContainer.LIMIT_ID)
			{
				sids=((LimitSaleActivity)act).getSid_num().keyArray();
			}
			else if(sid==ActivityContainer.DISCOUNT_ID)
			{
				sids=((DiscountActivity)act).getPropSids();
			}
			else if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
			{
				sids=((AwardActivity)act).getAwardPackage();
			}
			if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
			{
				for(int i=0;i<sids.length;i+=3)
				{
					json.put("sid"+i/3,sids[i]+"x"+sids[i+1]);
				}
			}
			else
			{
				for(int i=0;i<sids.length;i++)
				{
					json.put("sid"+i,sids[i]);
				}
			}

			sql="SELECT count(*) as count from (SELECT * from  activitylog where aid="
				+id+" GROUP BY pid) as t1";
			field=SqlKit.query(cm,sql);
			long count=((LongField)field.get("count")).value;
			long maxp=count%p_max==0?count/p_max:count/p_max+1;
			if(page>maxp) return GMConstant.ERR_ACTIVITY_MAX_PAGE;
			json.put("maxp",maxp);

			json.put("stime",SeaBackKit.formatDataTime(act.getStartTime()));
			json.put("etime",SeaBackKit.formatDataTime(act.getEndTime()));

			if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
			{
				sql="select pid,gem from (SELECT count(*) as gem,pid from  activitylog where aid="
					+id
					+" GROUP BY pid ORDER BY gem DESC) as t1 LIMIT "
					+index+","+p_max;
			}
			else
			{
				sql="select pid,gem from (SELECT SUM(gems) as gem,pid from  activitylog where aid="
					+id
					+" GROUP BY pid ORDER BY gem DESC) as t1 LIMIT "
					+index+","+p_max;
			}
			Fields[] fields=SqlKit.querys(cm,sql);
			if(fields==null)
			{
				json.put("num",0);
				return GMConstant.ERR_SUCCESS;
			}
			else
			{
				if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
				{
					json.put("num",sids.length/3);
				}
				else
				{
					json.put("num",sids.length);
				}
				for(int i=0;i<fields.length;i++)
				{
					json=new JSONObject();
					int pid=((IntField)fields[i].get("pid")).value;
					json.put("name",objfactory.getPlayerById(pid).getName());
					if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
					{
						json.put("gem",
							((LongField)fields[i].get("gem")).value);
					}
					else
					{
						json.put("gem",
							((IntField)fields[i].get("gem")).value);
					}
					if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
					{
						for(int k=0;k<sids.length;k+=3)
						{
							sql="select count(*) as count from activitylog where pid="
								+pid
								+" and aid="
								+id
								+" and sid='"
								+sids[k]
								+"x"
								+sids[k+1]+"'";
							field=SqlKit.query(cm,sql);
							json.put("sid"+k/3,
								((LongField)field.get("count")).value);
						}
					}
					else
					{
						for(int k=0;k<sids.length;k++)
						{
							sql="select count(*) as count from activitylog where pid="
								+pid+" and aid="+id+" and sid="+sids[k];
							field=SqlKit.query(cm,sql);
							json.put("sid"+k,
								((LongField)field.get("count")).value);
						}
					}
					jsonArray.put(json);
				}

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}

		return GMConstant.ERR_SUCCESS;
	}
}
