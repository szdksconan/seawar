package foxu.cross.server;

import javapns.json.JSONArray;
import mustang.event.ChangeListenerList;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.SetKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.cross.war.CrossWar;
import foxu.sea.PublicConst;
import foxu.sea.uid.UidKit;

/**
 * 跨服活动保存信息 管理器
 * 
 * @author yw
 * 
 */

public class CrossActManager extends ChangeListenerList implements TimerListener
{
	/** CREATE_ACT创建活动         OVER_ACT结束活动   MODY_ACT修改活动*/
	public static int CREATE_ACT=1,OVER_ACT=2,MODY_ACT=3;

	int saveCD=60*15;
	
	/** 活动map */
	IntKeyHashMap actmap=new IntKeyHashMap();

	/** 跨服活动保存信息 sql中心 */
	CrossActDBAccess dbAccess;
	
	/** 跨服活动唯一id提供器 */
	UidKit actuid;

	CrossActComparator cac=new CrossActComparator();
	
	//test
	CrossWar act;

	/**
	 * 创建一个活动
	 * @param sid 活动sid
	 * @return
	 */
	public int createAct(int sid,String date,String award)
	{
		CrossAct act=getLastAct(sid,false);
		if(act!=null&&!act.isover()) return 1;
		if(sid==CrossAct.WAR_SID)
		{
			act=new CrossWar();
			boolean succ=act.setAward(award);
			if(!succ) return 2;
			//检测时间合法性
			if(!act.checkDate(date))return 2;
			act.setId(actuid.getPlusUid());
			//测试时屏蔽
			((CrossWar)act).init(date);
			
//			{
//			//test
//				((CrossWar)act).creatSteps();
//			}
			
			act.setNeedsave(true);
			addAct(act);
		}
		if(act==null)
		{
			return 3;
		}
		else
		{
			change(this,CREATE_ACT,act);
			return 0;
		}
	}
	
	/** 修改奖励 */
	public int setAward(int sid,String award)
	{
		CrossAct act=getLastAct(sid,false);
		if(act!=null&&act.isover()) return 1;
		boolean succ=act.setAward(award);
		if(!succ)
		{
			return 2;
		}
		act.setNeedsave(true);
		change(this,MODY_ACT,act);
		return 0;
	}
	/**
	 * 强制结束最后一个活动
	 * @param sid 活动sid
	 */
	public void forceOver(int sid)
	{
		CrossAct act=getLastAct(sid,false);
		if(act==null||act.getEtime()<TimeKit.getSecondTime()) return;
		act.setForceover(true);
		act.setNeedsave(true);
		change(this,OVER_ACT,act);
	}
	
	/** 显示已开启活动列表 */
	public String showAct(int sid)
	{
		ArrayList alist=(ArrayList)actmap.get(sid);
		if(alist==null||alist.size()<=0) return "no this type acts";
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<alist.size();i++)
		{
			sb.append(((CrossAct)alist.get(i)).toString()+"\n");
		}
		return sb.toString();
	}
	
	/** 通过sid获取最后一个活动 */
	public CrossAct getLastAct(int sid,boolean create)
	{
		ArrayList alist=(ArrayList)actmap.get(sid);
		if(alist==null||alist.size()<=0) return null;
		return (CrossAct)alist.get(alist.size()-1);
	}
	/** 通过sid 获取一类活动 */
	public ArrayList getActsBySid(int sid)
	{
		return (ArrayList)actmap.get(sid);
	}
	
	/** 添加一个活动 */
	public void addAct(CrossAct act)
	{
		int sid=act.getSid();
		ArrayList alist=(ArrayList)actmap.get(sid);
		if(alist==null)
		{
			alist=new ArrayList();
			actmap.put(sid,alist);
		}
		alist.add(act);
		SetKit.sort(alist.getArray(),cac);
	}

	/** 初始化 */
	public void init()
	{
		if(!PublicConst.crossServer) return;
		String sql="select * from crossact";
		CrossAct[] acts=dbAccess.loadBySql(sql);
		if(acts!=null&&acts.length>0)
		{
			for(int i=0;i<acts.length;i++)
			{
				addAct(acts[i]);
			}
		}
		// 开启定时器
		TimerCenter.getMinuteTimer().add(new TimerEvent(this,"",saveCD*1000));
	}
	/** 保存现有活动 */
	@Override
	public void onTimer(TimerEvent e)
	{
		saveActs();
	}
	
	/** 保存所有活动 */
	public int saveActs()
	{
		int[] keys=actmap.keyArray();
		int fail=0;
		for(int i=0;i<keys.length;i++)
		{
			ArrayList alist=(ArrayList)actmap.get(keys[i]);
			if(alist==null||alist.size()<=0) continue;
			for(int k=0;k<alist.size();k++)
			{
				CrossAct act=(CrossAct)alist.get(k);
				if(!act.needsave) continue;
				if(dbAccess.save(alist.get(k)))
				{
					act.setNeedsave(false);
				}
				else
				{
					fail++;
				}
			}
		}
		return fail;
	}

	/** 获取活动信息 bySid */
	public void getActInfoBySid(int sid,JSONArray jsonArray)
	{
		ArrayList alist=getActsBySid(sid);
		if(alist==null||alist.size()<=0) return;
		for(int i=0;i<alist.size();i++)
		{
			jsonArray.put(((CrossAct)alist.get(i)).toJson());
		}
	}

	
	public CrossActDBAccess getDbAccess()
	{
		return dbAccess;
	}

	
	public void setDbAccess(CrossActDBAccess dbAccess)
	{
		this.dbAccess=dbAccess;
	}
	
	public UidKit getActuid()
	{
		return actuid;
	}
	
	public void setActuid(UidKit actuid)
	{
		this.actuid=actuid;
	}
	

}
