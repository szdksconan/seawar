package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.field.ByteArrayField;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.io.ByteBuffer;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 查询活动信息
 * 
 * @author yw
 * 
 */
public class GetActInfo extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			String query_type=params.get("query_type");
			int type=Integer.parseInt(query_type);
			CreatObjectFactory objfactory=info.getObjectFactory();
			SqlPersistence sp=(SqlPersistence)objfactory
				.getActivityLogMemCache().getDbaccess().getGamePersistence();
			String sql=null;
			if(type==1)
			{
					sql="select * from activity where sid>0";
			}
			else if(type==2)
			{
				sql="select * from activity where sid="
					+ActivityContainer.GMES_ID+" order by stime";
			}
			else if(type==3)
			{
				sql="select distinct activity.*,COUNT(distinct activitylog.pid) as allplayers,SUM(activitylog.gems) as allsum,COUNT(activitylog.id)as allcount from activity,activitylog where activity.id=activitylog.aid and activity.sid="
					+ActivityContainer.LIMIT_ID+" GROUP BY activity.id";
			}
			else if(type==4)
			{
				sql="select * from activity where sid="
					+ActivityContainer.DISCOUNT_ID+" order by stime";
			}
			else if(type==5)
			{
				sql="select distinct activity.*,SUM(activitylog.gems) as allsum,COUNT(activitylog.id)as allcount from activity,activitylog where activity.id=activitylog.aid and activity.sid="
					+ActivityContainer.AWARD_ID+" GROUP BY activity.id";
			}
			else if(type==6)
			{
				sql="select distinct activity.*,SUM(activitylog.gems) as allsum,COUNT(activitylog.id)as allcount from activity,activitylog where activity.id=activitylog.aid and activity.sid="
					+ActivityContainer.AWARD_CLASSIC_ID
					+" GROUP BY activity.id";
			}
			//每日折扣
			else if(type==7)
			{
				sql="select * from activity where sid="
								+ActivityContainer.DATE_OFF_ID+" order by stime";
			}
			//手挡双倍充值
			else if(type==8)
			{
				sql="select * from activity where sid="
								+ActivityContainer.DOUBLE_GMES_ID+" order by stime";
			}
			//累计充值
			else if(type==9)
			{
				sql="select * from activity where sid="
								+ActivityContainer.TOTALBUYGMES_ID+" order by stime";
			}
			//累计消费
			else if(type==10)
			{
				sql="select * from activity where sid="
								+ActivityContainer.CONSUME_GEMS_ID+" order by stime";
			}
			//拼图活动
			else if(type==11)
			{
				sql="select * from activity where sid="
								+ActivityContainer.JIGSAW_ID+" order by stime";
			}
			//热销大礼包3
			else if(type==34)
			{
				sql="select * from activity where sid="
								+ActivityContainer.SELLING3+" order by stime";
			}
			//热销大礼包2
			else if(type==33)
			{
				sql="select * from activity where sid="
								+ActivityContainer.SELLING2+" order by stime";
			}
			//热销大礼包1
			else if(type==32)
			{
				sql="select * from activity where sid="
								+ActivityContainer.SELLING1+" order by stime";
			}
			//全民抢节
			else if(type==30)
			{
				sql="select * from activity where sid="
								+ActivityContainer.AWARD_ROB_ID+" order by stime";
			}
			//幸运探险
			else if(type==29)
			{
				sql="select * from activity where sid="
								+ActivityContainer.LUCKY_EXPLORED_ID+" order by stime";
			}
			//通商航运
			else if(type==28)
			{
				sql="select * from activity where sid="
								+ActivityContainer.AWARD_SHIPPING_ID+" order by stime";
			}
			//战争狂人
			else if(type==26)
			{
				sql="select * from activity where sid="
								+ActivityContainer.WAR_MANIC_ID+" order by stime";
			}
			//充值接力
			else if(type==25)
			{
				sql="select * from activity where sid="
								+ActivityContainer.PAY_RELAY+" order by stime";
			}
			//登录有礼
			else if(type==24)
			{
				sql="select * from activity where sid="
								+ActivityContainer.LOGIN_REWARD+" order by stime";
			}
			else
			{
				return GMConstant.ERR_PARAMATER_ERROR;
			}
			Fields[] fields=SqlKit.querys(sp.getConnectionManager(),sql);
			if(fields!=null)
				for(int i=0;i<fields.length;i++)
				{
					int id=((IntField)fields[i].get("id")).value;
					int sid=((IntField)fields[i].get("sid")).value;
					int stime=((IntField)fields[i].get("stime")).value;
					int etime=((IntField)fields[i].get("etime")).value;
					Activity activity=(Activity)Activity.factory
						.newSample(sid);
					byte[] array=((ByteArrayField)fields[i].get("initData")).value;
					if(array!=null&&array.length>0)
						activity.initData(new ByteBuffer(array),objfactory,false);
					JSONObject json=new JSONObject();
					json.put("id",id);
					json.put("sid",sid);
					json.put("stime",SeaBackKit.formatDataTime(stime));
					json.put("etime",SeaBackKit.formatDataTime(etime));
					json.put("initData",activity.getActivityState());
					if(type==3)
					{
						long allplayers=((LongField)fields[i]
							.get("allplayers")).value;
						json.put("allplayers",allplayers);

					}
					if(type==3||type==5||type==6)
					{
						int allsum=((IntField)fields[i].get("allsum")).value;
						long allcount=((LongField)fields[i].get("allcount")).value;
						json.put("allsum",allsum);
						json.put("allcount",allcount);
					}
					json.put("type",type);
					jsonArray.put(json);
				}

		}
		catch(Exception e)
		{
			return GMConstant.ERR_PARAMATER_ERROR;
		}
		return GMConstant.ERR_SUCCESS;
	}

}
