package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.field.StringField;
import mustang.orm.ConnectionManager;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.AwardActivity;
import foxu.sea.activity.DiscountActivity;
import foxu.sea.activity.DoubleGemsAcitivity;
import foxu.sea.activity.LimitSaleActivity;
import foxu.sea.activity.SellingPackageOne;
import foxu.sea.activity.SellingPackageThree;
import foxu.sea.activity.SellingPackageTwo;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/**
 * 玩家的活动的详细信息
 * 
 * @author lhj
 * 
 */
public class GetAcitivityPlayerInfo extends GMOperator
{

	int p_max=100;

	/** 充值宝石的数量 **/
	public int GEM_NUM[]={50,160,460,960,3420,8400};

	CreatObjectFactory objectFactory;

	@Override
	public int operate(String user,Map<String,String> params,
		javapns.json.JSONArray jsonArray,ServerInfo info)
	{
		try
		{
			int page=Integer.parseInt(params.get("page"));
			boolean next=Boolean.parseBoolean(params.get("next"));
			if(next)
				page++;
			else
				page--;
			if(page<1) return GMConstant.ERR_ACTIVITY_MIN_PAGE;
			int index=(page-1)*p_max;
			int id=Integer.parseInt(params.get("id"));
			int sid=Integer.parseInt(params.get("sid"));
			JSONObject json=new JSONObject();

			json.put("sid",sid);
			json.put("id",id);
			json.put("page",page);
			jsonArray.put(json);
//			String sql=null;
			objectFactory=info.getObjectFactory();
			ConnectionManager cm=((SqlPersistence)info.getObjectFactory()
							.getActivityLogMemCache().getDbaccess().getGamePersistence())
							.getConnectionManager();
			Activity act=ActivityContainer.getInstance().getDbAccess()
				.getActById(id,info.getObjectFactory());
			if(act==null) return GMConstant.ERR_ACTIVITY_NOT_EXISTS;
			int[] sids=new int[0];
			int[] spiecalG=null;
			if(sid==ActivityContainer.LIMIT_ID)
			{
				sids=((LimitSaleActivity)act).getSid_num().keyArray();
			}
			else if(sid==ActivityContainer.DISCOUNT_ID)
			{
				sids=((DiscountActivity)act).getPropSids();
			}
			else if(sid==ActivityContainer.AWARD_ID
				||sid==ActivityContainer.AWARD_CLASSIC_ID)
			{
				sids=((AwardActivity)act).getAwardPackage();
			}
			else if(sid==ActivityContainer.SELLING1 || sid==ActivityContainer.SELLING3 || sid==ActivityContainer.SELLING2)
			{
				if(sid==ActivityContainer.SELLING1)
				{
					sids=((SellingPackageOne)act).getAllProSids();
					spiecalG=((SellingPackageOne)act).getRandomPros();
				}
				else if(sid==ActivityContainer.SELLING3)
				{
					sids=((SellingPackageThree)act).getAllProSids();
					spiecalG=((SellingPackageThree)act).getRandomPros();
				}
				else if(sid==ActivityContainer.SELLING2)
				{
					sids=((SellingPackageTwo)act).getAllProSids();
					spiecalG=((SellingPackageTwo)act).getRandomPros();
				}
			}
			else if(sid==ActivityContainer.DATE_OFF_ID)
			{
				//获取每日折扣活动的sid
				sids=getDayActivityPro(cm,id);
				if(sids==null || sids.length==0)
					json.put("num",0);
				return GMConstant.ERR_SUCCESS;
			}
//			else if(sid==ActivityContainer.DOUBLE_GMES_ID)
//			{
//					JSONObject jo=new JSONObject();
//					//查询宝石记录
//					for(int i=0;i<GEM_NUM.length;i++)
//					{
//						jo.put("sid"+i,GEM_NUM[i]);
//					}
//					jsonArray.put(jo);
//					return GMConstant.ERR_SUCCESS;
//			}
			
		
			if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
			{
				for(int i=0;i<sids.length;i+=3)
				{
					json.put("sid"+i/3,sids[i]+"x"+sids[i+1]);
				}
			}
			else if(sid==ActivityContainer.SELLING1 || sid==ActivityContainer.SELLING2
							|| sid==ActivityContainer.SELLING3)
			{
				for(int i=0;i<sids.length;i+=3)
				{
					boolean flag=false;
					if(spiecalG!=null)
						flag=checkSpecial(spiecalG,sids[i],sids[i+1]);
					if(flag)
						json.put("sid"+i/3,sids[i]+"x"+sids[i+1]+"-S-");
					else 
						json.put("sid"+i/3,sids[i]+"x"+sids[i+1]);
				}
			}
			else if(sid==ActivityContainer.DOUBLE_GMES_ID)
			{
				for(int i=0;i<GEM_NUM.length;i++)
				{
					json.put("sid"+i,GEM_NUM[i]);
				}
			}
			else
			{	
				for(int i=0;i<sids.length;i++)
				{
					json.put("sid"+i,sids[i]);
				}
			}
			IntKeyHashMap map=new IntKeyHashMap();
			if(sid==ActivityContainer.DOUBLE_GMES_ID)
				 map=getChargeInfo((DoubleGemsAcitivity)act,cm);
			else
				map=getAllActivityInfo(String.valueOf(id),cm);
			long count=map.size();
			long maxp=count%p_max==0?count/p_max:count/p_max+1;
			if(page>maxp) return GMConstant.ERR_ACTIVITY_MAX_PAGE;
			json.put("maxp",maxp);
			json.put("stime",SeaBackKit.formatDataTime(act.getStartTime()));
			json.put("etime",SeaBackKit.formatDataTime(act.getEndTime()));
			String sql1=null;
			if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID)
			{
				sql1="select pid,gem from (SELECT count(*) as gem,pid from  activitylog where aid="
					+id
					+" GROUP BY pid ORDER BY gem DESC) as t1 LIMIT "
					+index+","+p_max;
			}
			else if(sid==ActivityContainer.SELLING1 || sid==ActivityContainer.SELLING2
							|| sid==ActivityContainer.SELLING3)
			{
				sql1="select pid,gem from (SELECT count(*) as gem,pid from  activitylog where aid="
								+id+" and activitylog.gems>0 "
								+" GROUP BY pid ORDER BY gem DESC) as t1 LIMIT "
								+index+","+p_max;
			}
			else if(sid==ActivityContainer.DOUBLE_GMES_ID)
			{
				if(map==null || map.size()==0) 
				{
					json.put("num",0);
					return GMConstant.ERR_SUCCESS;
				}
				json.put("num",GEM_NUM.length);
				int[] array=map.keyArray();
				for(int j=0;j<array.length;j++)
				{
					Player player=objectFactory.getPlayerById(array[j]);
					int value=(Integer)map.get(array[j]);
					jsonArray.put(beJsonString(player.getName(),value));
				}
				return GMConstant.ERR_SUCCESS;
			}
			else
			{
				sql1="select pid,gem from (SELECT SUM(gems) as gem,pid from  activitylog where aid="
					+id
					+" GROUP BY pid ORDER BY gem DESC) as t1 LIMIT "
					+index+","+p_max;
			}
			Fields[] fields=SqlKit.querys(cm,sql1);
			if(fields==null)
			{
				json.put("num",0);
				return GMConstant.ERR_SUCCESS;
			}
			else
			{
				if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID ||
								sid==ActivityContainer.SELLING1 || sid==ActivityContainer.SELLING2
								|| sid==ActivityContainer.SELLING3)
				{
					json.put("num",sids.length/3);
				}
				else
				{
					json.put("num",sids.length);
				}
				
				for(int i=0;i<fields.length;i++)
				{
					int pid=((IntField)fields[i].get("pid")).value;
					long gem=0;
					if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID ||
									sid==ActivityContainer.SELLING1 || sid==ActivityContainer.SELLING2
									|| sid==ActivityContainer.SELLING3)
					{
						gem=((LongField)fields[i].get("gem")).value;
					}
					else
					{
						gem=((IntField)fields[i].get("gem")).value;
					}
					jsonArray.put(getJsonString(map,sids,pid,gem,sid));
				}
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}

	/** 得到所有的活动信息 **/
	public IntKeyHashMap getAllActivityInfo(String aid,ConnectionManager cm)
	{
		IntKeyHashMap map=new IntKeyHashMap();
		String sql="select * from activitylog where aid='"+aid+"'";
		Fields[] fields=SqlKit.querys(cm,sql);
		if(fields==null || fields.length==0) return map;
		for(int i=0;i<fields.length;i++)
		{
			int pid=((IntField)fields[i].get("pid")).value;
			if(map==null||map.get(pid)==null)
			{
				String name=((StringField)fields[i].get("sid")).value;
				ActivityLog log=new ActivityLog();
				log.setName(name);
				log.setCount(1);
				ArrayList list=new ArrayList();
				list.add(log);
				map.put(pid,list);
			}
			else
			{
				String name=((StringField)fields[i].get("sid")).value;
				ArrayList list=(ArrayList)map.get(pid);
				boolean flag=true;
				for(int j=0;j<list.size();j++)
				{
					ActivityLog log=(ActivityLog)list.get(j);
					if(log==null) continue;
					if(log.getName().equals(name))
					{
						log.setCount(log.getCount()+1);
						flag=false;
						break;
					}
				}
				if(flag)
				{
					ActivityLog log=new ActivityLog();
					log.setName(name);
					log.setCount(1);
					list.add(log);
				}
			}
		}
		return map;
	}

	/**双倍充值活动的信息**/
	public IntKeyHashMap getChargeInfo(DoubleGemsAcitivity activity,ConnectionManager cm)
	{
		IntKeyHashMap map=new IntKeyHashMap();
		String sql="SELECT * from orders where create_at >"+activity.getStartTime()+" and create_at <"+activity.getEndTime()+" and sid>=1 and sid<=6";
		Fields[] fields=SqlKit.querys(cm,sql);
		if(fields==null || fields.length==0) return map;
		for(int i=0;i<fields.length;i++)
		{
			String name=((StringField)fields[i].get("user_name")).value;
			Player player =objectFactory.getPlayerByName(name,false);
			if(player==null) continue;
			int pid=player.getId();
			int gem=((IntField)fields[i].get("gems")).value;
			if(map==null||map.get(pid)==null)
			{
				map.put(pid,gem);
			}
		}
		return map;
	}
	/** 转化成json 格式 **/
	public JSONObject getJsonString(IntKeyHashMap map,int[] sids,int pid,long gem,int sid)
	{
		JSONObject json=new JSONObject();
		try
		{
			json.put("gem",gem);
			json.put("name",objectFactory.getPlayerById(pid).getName());
			ArrayList list=(ArrayList)map.get(pid);
			if(sid==ActivityContainer.AWARD_ID || sid==ActivityContainer.AWARD_CLASSIC_ID ||
							sid==ActivityContainer.SELLING1 || sid==ActivityContainer.SELLING2
							|| sid==ActivityContainer.SELLING3)
			{
				for(int j=0;j<sids.length;j+=3)
				{
					boolean bg=true;
					for(int j2=0;j2<list.size();j2++)
					{
						ActivityLog log=(ActivityLog)list.get(j2);
						if(log!=null
							&&log.getName().equals(+sids[j]+"x"+sids[j+1]))
						{
							json.put("sid"+j/3,log.getCount());
							bg=false;
						}
					}
					if(bg) json.put("sid"+j/3,0);
				}
			}
			else
			{
				for(int j=0;j<sids.length;j++)
				{
					boolean bg=true;
					for(int j2=0;j2<list.size();j2++)
					{
						ActivityLog log=(ActivityLog)list.get(j2);
						if(log!=null&&log.getName().equals(String.valueOf(sids[j])))
						{
							json.put("sid"+j,log.getCount());
							bg=false;
						}
					}
					if(bg) json.put("sid"+j,0);
				}
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return json;
	}
	
	/**双倍充值做修改**/
	public  JSONObject beJsonString(String name,int value)
	{
		JSONObject js=new JSONObject();
		try
		{
			js.put("name",name);
			for(int i=0;i<GEM_NUM.length;i++)
			{
				if(value==GEM_NUM[i])
					js.put("sid"+(i),1);
				else 
					js.put("sid"+(i),0);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return js;
	}
	
	/**获取每日折扣有哪些商品**/
	public int [] getDayActivityPro(ConnectionManager cm,int id)
	{	
		String sql="select DISTINCT sid from activitylog where aid="+id;
		Fields[] fields=SqlKit.querys(cm,sql);
		if(fields==null) return null;
		int sids[]=new int[fields.length];
		for(int i=0;i<fields.length;i++)
		{
			String pro=((StringField)fields[i].get("sid")).value;
			sids[i]=TextKit.parseInt(pro);
		}
		return sids;
	}
	
	/**檢查是否是特殊物品**/
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

/** 活动日志对象 **/
class ActivityLog
{

	String name;
	int count;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count=count;
	}
}
