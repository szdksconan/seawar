package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.IntKeyHashMap;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.AwardActivity;
import foxu.sea.activity.ConsumeGemsActivity;
import foxu.sea.activity.DiscountActivity;
import foxu.sea.activity.LimitSaleActivity;
import foxu.sea.activity.SellingPackageOne;
import foxu.sea.activity.SellingPackageThree;
import foxu.sea.activity.SellingPackageTwo;
import foxu.sea.award.Award;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/**
 * 查询活动销售统计
 * 
 * @author yw
 * 
 */
public class GetActSelldata extends GMOperator
{

	/**充值宝石的数量**/
	public int GEM_NUM[]={50,160,460,960,3420,8400};
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
//		System.out.println("-------------11111---------------");
		try
		{
			CreatObjectFactory objfactory=info.getObjectFactory();
			SqlPersistence sp=(SqlPersistence)objfactory
				.getActivityLogMemCache().getDbaccess().getGamePersistence();
			int id=Integer.parseInt(params.get("id"));
			int sid=Integer.parseInt(params.get("sid"));
			String sql="select stime,etime from activity where id="+id;
			Fields field=SqlKit.query(sp.getConnectionManager(),sql);
//			System.out.println("-------------0---------------:"+sid);
			if(field==null) return GMConstant.ERR_ACTIVITY_NOT_EXISTS;
			int stime=((IntField)field.get("stime")).value;
			int etime=((IntField)field.get("etime")).value;
			JSONObject json=new JSONObject();
			json.put("type",sid);
			json.put("stime",SeaBackKit.formatDataTime(stime));
			json.put("etime",SeaBackKit.formatDataTime(etime));
			jsonArray.put(json);
			long charges=0;
			int allgem=0;
			int allrmb=0;
			//不是累计消费
			if(sid!=ActivityContainer.CONSUME_GEMS_ID && sid!= ActivityContainer.DATE_OFF_ID)
			{
				sql="SELECT SUM(gems) as gem,SUM(money) as rmb from orders where create_at>="
					+stime+" and create_at <="+etime;
				field=SqlKit.query(sp.getConnectionManager(),sql);
				if(field!=null)
				{
					allgem=((IntField)field.get("gem")).value;
					allrmb=((IntField)field.get("rmb")).value;
					sql="SELECT count(*) as count from (SELECT * from orders where create_at>="
						+stime
						+" and create_at <="
						+etime
						+" GROUP BY user_id) as t1";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					charges=((LongField)field.get("count")).value;
				}
			}
			if(sid==ActivityContainer.GMES_ID)
			{
				json=new JSONObject();
				json.put("charges",charges);
				json.put("allgem",allgem);
				json.put("allrmb",allrmb);
				jsonArray.put(json);
				for(int i=0;i<GEM_NUM.length;i++)
				{
					json=new JSONObject();
					json.put("allgem",GEM_NUM[i]);//宝石的数量
					sql="SELECT COUNT(gems) as num,SUM(money) as rmb from orders where   gems="+GEM_NUM[i]+"  and  create_at>="
									+stime+" and create_at <="+etime;
					field=SqlKit.query(sp.getConnectionManager(),sql);
					if(field!=null)
					{
						long num=((LongField)field.get("num")).value;
						long costm=((IntField)field.get("rmb")).value;
						json.put("charges",num);//人数
						json.put("allrmb",costm);//消费的人民币
						jsonArray.put(json);
					}
				}
			}
			else if(sid==ActivityContainer.LIMIT_ID)
			{
				// 物品sid,可购次数,单价(gem),售出总数(份),购买人数,全买人数,充值人数,销售总额(gem),充值总额(rmb)
				LimitSaleActivity act=(LimitSaleActivity)ActivityContainer
					.getInstance().getDbAccess().getActById(id,objfactory);
				IntKeyHashMap map=act.getSid_num();
				int[] keys=map.keyArray();
				for(int i=0;i<keys.length;i++)
				{
					json=new JSONObject();
					json.put("sid",keys[i]);
					int count=(Integer)map.get(keys[i]);
					json.put("count",count);
					int price=((Prop)Prop.factory.getSample(keys[i]))
						.getNeedGems();
					json.put("price",price);
					sql="select count(*) as amount from activitylog where aid="
						+id+" and sid="+"'"+keys[i]+"'";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					long amount=((LongField)field.get("amount")).value;
					json.put("amount",amount);
					sql="SELECT count(*) as buys from (SELECT * from activitylog where aid="
						+id
						+" and sid="
						+"'"
						+keys[i]
						+"'"
						+" GROUP BY pid) as t1";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					json.put("buys",((LongField)field.get("buys")).value);
					sql="SELECT count(*) as fullbuys from (SELECT count(*) as count from activitylog where aid="
						+id
						+" and sid="
						+"'"
						+keys[i]
						+"'"
						+" GROUP BY pid) as t1 where count>="+count;
					field=SqlKit.query(sp.getConnectionManager(),sql);
					json.put("fullbuys",
						((LongField)field.get("fullbuys")).value);
					json.put("allgem",amount*price);
					json.put("charges",charges);
					json.put("allrmb",allrmb);
					jsonArray.put(json);

				}

			}
			else if(sid==ActivityContainer.DISCOUNT_ID)
			{
				// 物品sid，折扣比例，单价(gem)，售出总数(份)，购买人数，充值人数，销售总额(gem)，充值总额(rmb)
				DiscountActivity act=(DiscountActivity)ActivityContainer
					.getInstance().getDbAccess().getActById(id,objfactory);
				int[] sids=act.getPropSids();
				for(int i=0;i<sids.length;i++)
				{
					json=new JSONObject();
					json.put("sid",sids[i]);
					json.put("dicount",act.getPercent());
					int price=act.discountGems(sids[i],((Prop)Prop.factory
						.getSample(sids[i])).getNeedGems());
					json.put("price",price);
					sql="select count(*) as amount from activitylog where aid="
						+id+" and sid='"+sids[i]+"'";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					long amount=((LongField)field.get("amount")).value;
					json.put("amount",amount);
					sql="SELECT count(*) as buys from (SELECT * from activitylog where aid="
						+id
						+" and sid="
						+"'"
						+sids[i]
						+"'"
						+" GROUP BY pid) as t1";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					json.put("buys",((LongField)field.get("buys")).value);
					json.put("allgem",price*amount);
					json.put("charges",charges);
					json.put("allrmb",allrmb);
					jsonArray.put(json);
				}

			}
			else if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
			{
//				System.out.println("--------AWARD_ID----------");
				// 物品sid，单价(gem)，出现几率，抽出份数，抽奖人数，总抽奖次数，充值人数，销售总额，充值总额
				AwardActivity act=(AwardActivity)ActivityContainer
					.getInstance().getDbAccess().getActById(id,objfactory);
				int[] siddata=act.getAwardPackage();
				sql="select count(*) as allcount from activitylog where aid="
					+id;
				field=SqlKit.query(sp.getConnectionManager(),sql);
				long allcount=((LongField)field.get("allcount")).value;
				for(int i=0;i<siddata.length;i+=3)
				{
					json=new JSONObject();
					String strsid=siddata[i]+"x"+siddata[i+1];
					json.put("sid",strsid);
					json.put("price",act.getGems());
					float chance=(i>0?siddata[i+2]-siddata[i-1]:siddata[i+2])
						/(float)Award.PROB_ABILITY*100;
					chance=(float)(Math.round(chance*100))/100;
					json.put("chance",chance);
					sql="select count(*) as amount from activitylog where aid="
						+id+" and sid='"+strsid+"'";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					long amount=((LongField)field.get("amount")).value;
					json.put("amount",amount);
					sql="SELECT count(*) as buys from (SELECT * from activitylog where aid="
						+id
						+" and sid="
						+"'"
						+strsid
						+"'"
						+" GROUP BY pid) as t1";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					json.put("buys",((LongField)field.get("buys")).value);
					json.put("allcount",allcount);
					json.put("allgem",act.getGems()*amount);
					json.put("charges",charges);
					json.put("allrmb",allrmb);
					jsonArray.put(json);

				}

			}
			//累计充值
			else if(sid==ActivityContainer.TOTALBUYGMES_ID)
			{
				json=new JSONObject();
				json.put("charges",charges);
				json.put("allgem",allgem);
				json.put("allrmb",allrmb);
				jsonArray.put(json);
				for(int i=0;i<GEM_NUM.length;i++)
				{
					json=new JSONObject();
					json.put("allgem",GEM_NUM[i]);//宝石的数量
					sql="SELECT COUNT(gems) as num,SUM(money) as rmb from orders where   gems="+GEM_NUM[i]+"  and  create_at>="
									+stime+" and create_at <="+etime;
					field=SqlKit.query(sp.getConnectionManager(),sql);
					if(field!=null)
					{
						long num=((LongField)field.get("num")).value;
						long costm=((IntField)field.get("rmb")).value;
						json.put("charges",num);//人数
						json.put("allrmb",costm);//消费的人民币
						jsonArray.put(json);
					}
				}
			}
			//累计消费
			else if(sid==ActivityContainer.CONSUME_GEMS_ID)
			{
				// 收入
			/*GEMS_PAY=7,GEMS_AWARD=8,GM_SEND=9,INVITE=11,
				GEMS_DAY_SEND=12,SERVER_AWARD=20,THIRD_GEMS_PAY=23,
				MOUTHCARD=24,MOUTHCARD_GET=25,
				// 不影响
				SUBMIT_ORDER=18,CANCEL_ORDER=19;*/
				sql="SELECT SUM(gems) as num  from gem_tracks where "
					+" createAt>="
					+stime
					+" and createAt <="
					+etime
					+" and type!=7  and type!=8 and type!=9 and type!=11 and type!=12 and type!=20 and type!=23 and type!=24  and type!=25"
					+" and type!=18 and type!=19";
				field=SqlKit.query(sp.getConnectionManager(),sql);
				long num=0;
				if(field!=null)
				{
					num=((IntField)field.get("num")).value;
				}
				ConsumeGemsActivity act=(ConsumeGemsActivity)ActivityContainer
								.getInstance().getDbAccess().getActById(id,info.getObjectFactory());
				if(act!=null) 
				{
					charges=act.getPlayers().toArray().length;
				}
				json=new JSONObject();
				json.put("charges",charges);
				json.put("allgem",num);
				json.put("allrmb",allrmb);
				jsonArray.put(json);
			}
			else if(sid ==ActivityContainer.DATE_OFF_ID)
			{
				sql ="select COUNT(pid) as num,SUM(gems) as gem from activitylog where activitylog.aid="+id;
				field=SqlKit.query(sp.getConnectionManager(),sql);
				//总计消费宝石
				long num=0;
				//参与人数
				long pnum=0;
				if(field!=null)
				{
					pnum=((LongField)field.get("num")).value;
					num=((IntField)field.get("gem")).value;
				}
				json.put("charges",pnum);
				json.put("allgem",num);				
			}
			else if(sid==ActivityContainer.DOUBLE_GMES_ID)
			{
				sql="select COUNT(DISTINCT o.user_name) num,SUM(o.gems) allgems,SUM(o.money) allmoney from (SELECT * from orders where sid=1 and create_at >"+stime+" and create_at <"+etime+"  GROUP BY user_id UNION ALL SELECT * from orders where sid=2 and create_at >"+stime+" and create_at <"+etime 

								+" GROUP BY user_id UNION ALL SELECT * from orders where sid=3 and create_at >"+stime+" and create_at <"+etime+" GROUP BY user_id UNION ALL SELECT * from orders where sid=4 and create_at >"+stime+" and create_at <"+etime+

								" GROUP BY user_id UNION ALL SELECT * from orders where sid=5 and create_at >"+stime+" and create_at <"+etime+" GROUP BY user_id UNION ALL SELECT * from orders where sid=6 "+

								"and create_at >"+stime+" and create_at <"+etime+" GROUP BY user_id) o" ;
				field=SqlKit.query(sp.getConnectionManager(),sql);
				//人数
				long num=0;
				//总计宝石数量
				int allgems=0;
				//总计钱的数量
				int allmoney=0;
				if(field!=null)
				{
					num=((LongField)field.get("num")).value;
					allgems=((IntField)field.get("allgems")).value;
					allmoney=((IntField)field.get("allmoney")).value;
				}
				json=new JSONObject();
				json.put("charges",num);
				json.put("allgem",allgems);
				json.put("allrmb",allmoney);
				jsonArray.put(json);
				//获取当前宝石（双倍）每份的记录
				for(int i=1;i<GEM_NUM.length+1;i++)
				{
					sql="SELECT COUNT(DISTINCT o.user_name) num,SUM(o.gems) allgems,SUM(o.money) allmoney from (select *  from orders where sid="+i+" and create_at >"+stime+" and create_at <"+etime+" GROUP BY user_id) o";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					num=0;
					allgems=0;
					allmoney=0;
					if(field!=null)
					{
						num=((LongField)field.get("num")).value;
						allgems=((IntField)field.get("allgems")).value;
						allmoney=((IntField)field.get("allmoney")).value;
					}
					json=new JSONObject();
					json.put("charges",num);
					json.put("allgem",GEM_NUM[i-1]+"("+allgems+")");
					json.put("allrmb",allmoney);
					jsonArray.put(json);
				}
			}
			else if(sid==ActivityContainer.SELLING1 || sid==ActivityContainer.SELLING2 || sid==ActivityContainer.SELLING3)
			{
				Activity act=(Activity)ActivityContainer
					.getInstance().getDbAccess().getActById(id,objfactory);
				int[] siddata=null;
				int[] specialSid=null;
				int price=0;
				if(sid==ActivityContainer.SELLING1)
				{
					SellingPackageOne aOne=(SellingPackageOne)act;
					siddata=aOne.getAllProSids();
					specialSid=aOne.getRandomPros();
					price=aOne.getPrice();
				}
				else if(sid==ActivityContainer.SELLING2)
				{
					SellingPackageTwo aTwo=(SellingPackageTwo)act;
					siddata=aTwo.getAllProSids();
					specialSid=aTwo.getRandomPros();
					price=aTwo.getPrice();
				}
				else if(sid==ActivityContainer.SELLING3)
				{
					SellingPackageThree aThree=(SellingPackageThree)act;
					siddata=aThree.getAllProSids();
					specialSid=aThree.getRandomPros();
					price=aThree.getPrice();
				}
				sql="select count(*) as allcount from activitylog where aid="
					+id;
				field=SqlKit.query(sp.getConnectionManager(),sql);
				long allcount=((LongField)field.get("allcount")).value;
				for(int i=0;i<siddata.length;i+=3)
				{
					json=new JSONObject();
					String strsid=siddata[i]+"x"+siddata[i+1];
					json.put("sid",strsid);
					int gems=price;
					if(checkSpecial(specialSid,siddata[i],siddata[i+1]))
						gems=0;
					json.put("price",gems);
					float chance=(i>0?siddata[i+2]-siddata[i-1]:siddata[i+2])
						/(float)Award.PROB_ABILITY*100;
					chance=(float)(Math.round(chance*100))/100;
					if(gems==0)
						chance=100+chance;
					json.put("chance",chance);
					sql="select count(*) as amount from activitylog where aid="
						+id+" and sid='"+strsid+"'";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					long amount=((LongField)field.get("amount")).value;
					json.put("amount",amount);
					sql="SELECT count(*) as buys from (SELECT * from activitylog where aid="
						+id
						+" and sid="
						+"'"
						+strsid
						+"'"
						+" GROUP BY pid) as t1";
					field=SqlKit.query(sp.getConnectionManager(),sql);
					json.put("buys",((LongField)field.get("buys")).value);
					json.put("allcount",allcount);
					json.put("allgem",gems*amount);
					json.put("charges",charges);
					json.put("allrmb",allrmb);
					jsonArray.put(json);
				}
			}	
		}
		catch(Exception e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}
	
	
	public boolean checkSpecial(int[] speical, int sid,int num)
	{
		if(speical==null || speical.length==0)
		return false;
		for(int i=0;i<speical.length;i+=3)
		{
			if(speical[i]==sid && speical[i+1]==num)
				return true;
		}
		return false;
	}
}
