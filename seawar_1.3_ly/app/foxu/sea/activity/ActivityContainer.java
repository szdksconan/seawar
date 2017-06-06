package foxu.sea.activity;

import mustang.field.Fields;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.ObjectArray;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.Sample;
import mustang.util.TimeKit;
import shelby.ds.DSManager;
import foxu.dcaccess.ActivityDBAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.ActivityLogMemCache;
import foxu.sea.Player;
import foxu.sea.activity.WarManicActivity.RankerA;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

/**
 * 活动容器
 * 
 * @author yw
 */
public class ActivityContainer implements TimerListener
{

	/** 活动ID */
	public static final int VARIBLE_AWARD=0,EXP_ID=1,BOSS_ID=2,GMES_ID=3,
					LIMIT_ID=4,DISCOUNT_ID=5,AWARD_ID=6,ARM_ID=7,BUILD_ID=8,
					SCIENCE_ID=9,AWARD_CLASSIC_ID=10,DATE_OFF_ID=11,
					DOUBLE_GMES_ID=12,TOTALBUYGMES_ID=13,CONSUME_GEMS_ID=14,
					NIAN_SID=15,APP_GRADE_ID=16,NEW_PLAYER_UP_ID=17,APP_SHARE_ID=18,
					NEW_SERV_FIGHT=19,NEW_SERV_POINT=20,NEW_SERV_HONOR=21,
					JIGSAW_ID=22,WEB_SHOW_ID=23,LOGIN_REWARD=24,PAY_RELAY=25,
					WAR_MANIC_ID=26,QUESTIONNAIRE_ID=27,AWARD_SHIPPING_ID=28,
					LUCKY_EXPLORED_ID=29,AWARD_ROB_ID=30,PEACE_ACT=31,
					SELLING1=32,SELLING2=33,SELLING3=34;

	
	/**定制活动的sid**/
	public static final int ACTIVITY_SID=999;
	/**定制活动本身包含哪些sid**/
	public static int ACTIVITY_SIDS[]={SELLING1,SELLING2,SELLING3};
	/** 空奖励包 */
	public static final int EMPTY_SID=51004;

	/** 定时刷新间隔 */
	private static final int time=5000;

	private static ActivityContainer instace;

	ActivityDBAccess dbAccess;

	DSManager dsManager;
	ActivityComparator comparator;
	CreatObjectFactory objectFactory;

	/** 活动map表 id-activity */
	private IntKeyHashMap activityMap;

	/** 活动唯一 id */
	private int act_id;
	/** 活动总量 */
	private int act_count;
	/** 不再开放的活动 */
	private int[] disableActivity={DATE_OFF_ID};

	public ActivityContainer()
	{
		instace=this;
		activityMap=new IntKeyHashMap();
		comparator=new ActivityComparator();
		act_id=TimeKit.getSecondTime();
		Sample[] samples=Activity.factory.getSamples();
		for(int i=0;i<samples.length;i++)
		{
			if(samples[i]==null) break;
			act_count++;
		}
		// 天降好礼（特殊活动）
		Activity act=(Activity)Activity.factory.newSample(VARIBLE_AWARD);
		act.setId(act_id);
		act_id++;

		ObjectArray array=new ObjectArray();
		array.add(act);
		activityMap.put(act.getSid(),array);
	}

	public void setDsManager(DSManager dsManager)
	{
		this.dsManager=dsManager;
	}

	public void setDbAccess(ActivityDBAccess dbAccess)
	{
		this.dbAccess=dbAccess;
	}

	/** 启动定时器 */
	public void startTimer()
	{
		TimerCenter.getSecondTimer().add(new TimerEvent(this,"flush",time));
	}

	public static ActivityContainer getInstance()
	{
		return instace;
	}

	/** 加载已有活动 */
	public void initActivity()
	{
		Fields[] fields=dbAccess.initData();
		if(fields==null) return;
		for(int i=0;i<fields.length;i++)
		{
			Activity activity=dbAccess.mapping(fields[i],objectFactory);
			if(activity==null
				||SeaBackKit.isContainValue(disableActivity,
					activity.getSid())) continue;
			ObjectArray array=(ObjectArray)activityMap
				.get(activity.getSid());
			if(array==null)
			{
				array=new ObjectArray();
				array.setComparator(comparator);
				activityMap.put(activity.getSid(),array);
			}
			if(activity.getSid()==VARIBLE_AWARD) array.clear();// 天降好礼
			array.add(activity);
		}
	}

	/** 开启活动 */
	public synchronized String startActivity(int sid,String stime,
		String etime,String initData)
	{
		if(SeaBackKit.isContainValue(disableActivity,sid))
		{
			return erro("activity is disabled");
		}
		if(!checkActivityTime(sid,stime,etime,0))
		{
			return erro("time erro or duplicate");
		}
		Activity activity=null;
		if(sid==VARIBLE_AWARD)
			activity=getActivity(sid,0);
		else
			activity=(Activity)Activity.factory.newSample(sid);

		activity.setId(act_id);
		act_id++;
		String result=activity.startActivity(stime,etime,initData,objectFactory);

		ObjectArray array=(ObjectArray)activityMap.get(activity.getSid());
		if(array==null)
		{
			array=new ObjectArray();
			array.setComparator(comparator);
		}
		array.add(activity);
		activityMap.put(activity.getSid(),array);
		checkActivity(true);
		save(activity);
		result=TextKit.replaceAll(result,"\r\n","\\r\\n");
		return result;
	}
	/** 重设活动 */
	public String resetActivity(int sid,String stime,String etime,
		String initData,int id)
	{
		if(!checkActivityTime(sid,stime,etime,id))
			return erro("time erro or duplicate");
		Activity activity=getActivity(sid,id);
		if(activity==null) return erro("not open target activity");
		String result=activity.resetActivity(stime,etime,initData,objectFactory);
		ObjectArray array=(ObjectArray)activityMap.get(activity.getSid());
		array.sort();
		save(activity);
		checkActivity(true);
		result=TextKit.replaceAll(result,"\r\n","\\r\\n");
		return result;
	}
	/** 错误返回 */
	public String erro(String mes)
	{
		StringBuilder erro=new StringBuilder();
		erro.append("{\"erro\":\"").append(mes).append("\"}");
		return erro.toString();
	}
	/** 判断新开或修改时间合法性（不重叠） */
	public boolean checkActivityTime(int sid,String stime,String etime,int id)
	{
		int st=SeaBackKit.parseFormatTime(stime);
		int et=SeaBackKit.parseFormatTime(etime);
		if(st==0||et==0||et<=st) return false;
		ObjectArray array=(ObjectArray)activityMap.get(sid);
		if(array==null||array.size()<=0) return true;
		Object[] objs=array.getArray();
		for(int i=0;i<objs.length;i++)
		{
			Activity act=(Activity)objs[i];
			if(id==act.getId()) continue;
			if((st>=act.getStartTime()&&st<=act.getEndTime())
				||(et>=act.getStartTime()&&et<=act.getEndTime()))
				return false;
		}
		return true;
	}
	/** 判断某类活动是否在进行 */
	public boolean isOpen(int sid)
	{
		Activity acitvity=getActivity(sid,0);
		if(acitvity==null) return false;
		return acitvity.isOpen(TimeKit.getSecondTime());
	}

	/** 获取某类活动状态 */
	public String[] getActivityState(int sid)
	{
		Activity acitvity=getActivity(sid,0);
		String[] state=null;
		if(acitvity==null)
		{
			state=new String[1];
			state[0]=erro("not open this activity");
		}
		else
		{
			ObjectArray array=(ObjectArray)activityMap.get(sid);
			Object[] objs=array.getArray();
			state=new String[objs.length];
			for(int i=objs.length-1,j=0;i>=0;i--,j++)
			{
				state[j]=TextKit.replaceAll(
					((Activity)objs[i]).getActivityState(),"\r\n","\\r\\n");
			}
		}
		return state;
	}
	/** 获取某个活动 id为0 返回最先开启活动 */
	public Activity getActivity(int sid,int id)
	{
		ObjectArray array=(ObjectArray)activityMap.get(sid);
		if(array==null||array.size()<=0) return null;
		if(id==0) return (Activity)array.get();
		Object[] objs=array.getArray();
		for(int i=0;i<objs.length;i++)
		{
			Activity atc=(Activity)objs[i];
			if(atc.getId()==id) return atc;
		}
		return null;
	}
	/** 移除某个活动 */
	public void removeActivity(int sid,int id)
	{
		ObjectArray array=(ObjectArray)activityMap.get(sid);
		if(array==null||array.size()<=0) return;
		if(id==0)
		{
			array.remove();
		}
		else
		{
			Object[] objs=array.getArray();
			for(int i=0;i<objs.length;i++)
			{
				Activity act=(Activity)objs[i];
				if(act.getId()==id)
				{
					objs=ObjectArray.remove(objs,i);
					array=new ObjectArray(objs);
					activityMap.put(sid,array);
					break;
				}
			}
		}

	}

	/** 保存活动 */
	public void save(Activity activity)
	{
		dbAccess.save(activity);
	}

	/** 活动激活 */
	public boolean active(Activity activity)
	{
		int nowTime=TimeKit.getSecondTime();
		int lastTime=nowTime-time/1000;
		if(activity.isOpen(nowTime)&&!activity.isOpen(lastTime))
			return true;
		return false;
	}
	/** 活动关闭 */
	public Activity close(int sid)
	{
		ObjectArray array=(ObjectArray)activityMap.get(sid);
		if(array==null||array.size()<=0) return null;
		Object[] objs=array.getArray();
		Activity remove=null;
		for(int i=objs.length-1;i>=0;i--)
		{
			Activity act=(Activity)objs[i];
			if(act.getEndTime()<=TimeKit.getSecondTime())
			{
				remove=act;
				array.remove(act);
			}
			else
			{
				break;
			}
		}
		return remove;
	}

	/** 定时刷新活动 */
	@Override
	public void onTimer(TimerEvent e)
	{
		saveActivity(false);
		checkActivity(false);
	}
	/**
	 * 检测活动
	 * 
	 * @param isforce是否强刷新
	 */
	public void checkActivity(boolean isforce)
	{
		boolean isFlush=false;
		for(int i=0;i<act_count;i++)
		{
			if(i==VARIBLE_AWARD)// 天降好礼
			{
				VaribleAwardActivity act=(VaribleAwardActivity)getActivity(
					i,0);
				if(act.isFlush())
				{
					act.sendFlush(dsManager.getSessionMap());
					act.setFlush(false);
				}
			}
			else
			{
				Activity act=close(i);
				if(act!=null)
				{
					if(act instanceof ActivityCollate)
						((ActivityCollate)act).activityCollate(
							TimeKit.getSecondTime(),objectFactory);
					act.sendFlush(dsManager.getSessionMap());
					if(act.closeActivity())
					{
						save(act);
					}
					isFlush=true;
				}
				else
				{
					act=getActivity(i,0);
					if(act==null) continue;
					if(act instanceof ActivityCollate)
						((ActivityCollate)act).activityCollate(
							TimeKit.getSecondTime(),objectFactory);
					if(active(act))
					{
						act.sendFlush(dsManager.getSessionMap());
						isFlush=true;
					}
				}
			}
		}
		if(isFlush||isforce)
		{
			// 刷新前台
			ByteBuffer data=new ByteBuffer();
			showByteWrite(data);
			/**刷新新型活动**/
			JBackKit.sendActivityNewFlush(dsManager.getSessionMap());
			JBackKit.sendActivityFlush(dsManager.getSessionMap(),data);
		}
	}

	/** 保存活动 */
	public int saveActivity(boolean force)
	{
		int failsave=0;
		for(int i=0;i<act_count;i++)
		{
			ObjectArray array=(ObjectArray)activityMap.get(i);
			if(array==null||array.size()<=0) continue;
			Object[] objs=array.toArray();
			for(int k=0;k<objs.length;k++)
			{
				if(!(objs[k] instanceof ActivitySave)) continue;
				ActivitySave actsave=(ActivitySave)objs[k];
				if(actsave.isSave()||force)
				{
					try
					{
						save((Activity)actsave);
						actsave.setSave();
					}
					catch(Exception e)
					{
						failsave++;
						SeaBackKit.log.error("save act:error sid:"
							+((Activity)actsave).getSid()+" e:"+e.toString());
					}
				}
			}
		}
		return failsave;
	}
	/** 序列或进行中的活动信息 */
	public void showByteWrite(ByteBuffer data)
	{
		data.clear();
		int len=0;
		int top=data.top();
		data.writeByte(len);
		Activity activity=null;
		for(int sid=1;sid<act_count;sid++)// 0活动（天降好礼）被排除
		{
			activity=getActivity(sid,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime())
				||!activity.isListShow()||activity.isIsnew()) continue;
			len++;
			activity.showByteWrite(data);
		}
		int nowTop=data.top();
		data.setTop(top);
		data.writeByte(len);
		data.setTop(nowTop);

	}
	/** 新型序列化 进行中的活动 */
	public void showBytesWriteNew(Player player,ByteBuffer data)
	{
		int len=0;
		int top=data.top();
		data.writeByte(len);
		Activity activity=null;
		for(int sid=1;sid<act_count;sid++)// 0活动（天降好礼）被排除
		{
			if(SeaBackKit.isContainValue(ACTIVITY_SIDS,sid)) continue;
			activity=getActivity(sid,0);
			if(activity==null||!activity.isIsnew()) continue;
			if(sid!=ActivityContainer.PEACE_ACT
				&&!activity.isOpen(TimeKit.getSecondTime())) continue;
			len++;
			activity.showByteWriteNew(data,player,objectFactory);
		}
		/** 热销大礼包序列化 **/
		len=SellingActivity(data,len);
		int nowTop=data.top();
		data.setTop(top);
		data.writeByte(len);
		data.setTop(nowTop);
	}
	
	/**获取新型活动的长度活动**/
	public int getActivityNewLength()
	{
		int length=0;
		for(int sid=1;sid<act_count;sid++)// 0活动（天降好礼）被排除
		{
			Activity activity=getActivity(sid,0);
			if(activity==null||!activity.isIsnew()) continue;
			if(!activity.isOpen(TimeKit.getSecondTime())) continue;
				length++;
		}
		return length;
	}
	
	/**
	 * （检测）打折活动影响
	 * 
	 * @param sid 物品sid
	 * @param gems 售价
	 * @return
	 */
	public int discountGems(int sid,int gems)
	{
		DiscountActivity activity=(DiscountActivity)getActivity(DISCOUNT_ID,
			0);
		if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			return gems;
		return activity.discountGems(sid,gems);

	}
	/**
	 * 打折活动影响
	 * 
	 * @param sid 物品sid
	 * @param gems 售价
	 * @return
	 */
	public int discountGems(int sid,int gems,int pid)
	{
		DiscountActivity activity=(DiscountActivity)getActivity(DISCOUNT_ID,
			0);
		if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			return gems;
		if(SeaBackKit.isContainValue(activity.getPropSids(),sid))
		{
			gems=activity.discountGems(sid,gems);
			ActivityLogMemCache.getInstance().collectAlog(activity.getId(),
				sid+"",pid,gems);
		}
		return gems;

	}

	/** 限时活动影响 */
	/**
	 * @param player 玩家
	 * @param sid 物品sid
	 */
	public void limitSaleRecord(Player player,int sid,int gems)
	{
		LimitSaleActivity activity=(LimitSaleActivity)getActivity(LIMIT_ID,0);
		if(activity==null||!activity.isOpen(TimeKit.getSecondTime())
			||!activity.isLimitProp(sid)) return;
		player.limitSaleRecord(activity.getId(),sid,gems);

	}

	/** 经验活动的影响 */
	public int resetActivityExp(int exp)
	{
		ExpActivity activity=(ExpActivity)getActivity(EXP_ID,0);
		if(activity!=null&&activity.isOpen(TimeKit.getSecondTime()))
		{
			exp=exp+exp*activity.getPercent()/100;
		}
		return exp;
	}
	/** BOSS加速活动影响 */
	public int resetActivityBossFlush(int lastTime,int flushTime)
	{
		// 如果上次刷新时间在活动时间段内，按设定的百分比重新计算经验值
		BossActivity activity=(BossActivity)getActivity(BOSS_ID,0);
		if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			return flushTime;
		if(lastTime<activity.getEndTime()&&lastTime>=activity.getStartTime())
		{
			flushTime=flushTime*activity.getPercent()/100;
		}
		else if(lastTime<activity.getStartTime()
			&&lastTime+flushTime<activity.getEndTime())
		{
			flushTime=(activity.getStartTime()-lastTime)
				+(lastTime+flushTime-activity.getStartTime())
				*activity.getPercent()/100;
		}
		return flushTime;
	}
	
	/** 产兵,建造,科技 加速的影响 
	 * ARM_ID,BUILD_ID,SCIENCE_ID 
	 * */
	public int getActivitySpeed(int sid)
	{
		ExpActivity activity=(ExpActivity)getActivity(sid,0);
		if(activity!=null&&activity.isOpen(TimeKit.getSecondTime()))
		{
			return activity.getPercent();
		}
		return 0;
	}

	public ActivityDBAccess getDbAccess()
	{
		return dbAccess;
	}

	
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
	/**热销大礼包活动系列化**/
	public int SellingActivity(ByteBuffer data,int le)
	{
		int stime=0;
		int etime=0;
		boolean flag=false;
		for(int i=0;i<ACTIVITY_SIDS.length;i++)
		{
			Activity activity=getActivity(ACTIVITY_SIDS[i],0);
			if(activity==null||!activity.isIsnew()) continue;
			if(!activity.isOpen(TimeKit.getSecondTime())) continue;
			stime=getActivityStime(stime,activity.getStartTime());
			etime=getActivityEtime(etime,activity.getEndTime());
			flag=true;
		}
		if(flag) 
		{
			le++;
			data.writeShort(ACTIVITY_SID);
			if(stime<TimeKit.getSecondTime())
				data.writeInt(TimeKit.getSecondTime());
			else 
				data.writeInt(stime);
			data.writeInt(etime);
		} 
		return le;
	}
	/**time  定义时间  time1 真正的活动时间**/
	public int getActivityStime(int time,int time1)
	{
		if(time==0 || time1<time)return time1;
			return time;
	}
	
	/**time  定义时间  time1 真正的活动时间**/
	public int getActivityEtime(int time,int time1)
	{
		if(time==0 || time1>time)return time1;
			return time;
	}
	
	/**序列化热销大礼包信息**/
	public  void showBytesWriteSelling(ByteBuffer data,Player player,CreatObjectFactory factory)
	{
		int length=0;
		int top=data.top();
		data.writeByte(length);
		for(int i=0;i<ACTIVITY_SIDS.length;i++)
		{
			Activity activity=getActivity(ACTIVITY_SIDS[i],0);
			if(activity==null||!activity.isIsnew()) continue;
			if(!activity.isOpen(TimeKit.getSecondTime())) continue;
			length++;
			activity.showByteWriteNew(data,player,factory);
		}
		if(length!=0)
		{
			int nowTop=data.top();
			data.setTop(top);
			data.writeByte(length);
			data.setTop(nowTop);
		}

	}
	
	/**热销大礼包活动系列化**/
	public void sendFlushActivity(Activity activity,ByteBuffer data)
	{
		int stime=0;
		int etime=0;
		for(int i=0;i<ACTIVITY_SIDS.length;i++)
		{
			Activity activity1=getActivity(ACTIVITY_SIDS[i],0);
			if(activity1==null||!activity1.isIsnew()) continue;
			if(!activity1.isOpen(TimeKit.getSecondTime())) continue;
			if(stime==0||stime>activity1.getStartTime())
				stime=activity1.getStartTime();
			if(etime==0||etime<activity1.getEndTime())
				stime=activity1.getEndTime();
		}
		if(SeaBackKit.isContainValue(ACTIVITY_SIDS,activity.getSid()))
			data.writeShort(ACTIVITY_SID);
		else
		{
			data.writeShort(0);
			return;
		}
		if(stime<TimeKit.getSecondTime())
			data.writeInt(TimeKit.getSecondTime());
		else
			data.writeInt(activity.getStartTime());
		data.writeInt(etime);
	}
	
	
	/**获取某种类型的活动**/
	public Object[] getActivityBySid(int sid)
	{
		Activity acitvity=getActivity(sid,0);
		if(acitvity==null) return null;
		ObjectArray array=(ObjectArray)activityMap.get(sid);
		return array.getArray();
	}
	/**获取所有的活动**/
	public Object[] getAllActivity()
	{
		if(activityMap==null||activityMap.size()==0) return null;
		return activityMap.valueArray();
	}
	
	/**清除活动中的联盟信息**/
	public void clearActivityInfo(int allianceId)
	{
		NianActivity acti=(NianActivity)ActivityContainer.getInstance()
			.getActivity(ActivityContainer.NIAN_SID,0);
		if(acti!=null&&acti.getBoss()!=null)
		{
			acti.getBoss().getHurtList_a().remove(allianceId);
			acti.getBoss().setNeedsave(true);
		}
		WarManicActivity act=(WarManicActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
		if(act!=null&&act.isOpen(TimeKit.getSecondTime()))
		{
			ObjectArray array=act.getRanka();
			Object[] objs=array.getArray();
			for(int i=0;i<objs.length;i++)
			{
				RankerA rank=(RankerA)objs[i];
				if(rank.getId()==allianceId)
				{
					array.remove(rank);
					act.getMapa().remove(allianceId);
					break;
				}
			}
		}

	}
	
}
