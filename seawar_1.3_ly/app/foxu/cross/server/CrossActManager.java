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
 * ����������Ϣ ������
 * 
 * @author yw
 * 
 */

public class CrossActManager extends ChangeListenerList implements TimerListener
{
	/** CREATE_ACT�����         OVER_ACT�����   MODY_ACT�޸Ļ*/
	public static int CREATE_ACT=1,OVER_ACT=2,MODY_ACT=3;

	int saveCD=60*15;
	
	/** �map */
	IntKeyHashMap actmap=new IntKeyHashMap();

	/** ����������Ϣ sql���� */
	CrossActDBAccess dbAccess;
	
	/** ����Ψһid�ṩ�� */
	UidKit actuid;

	CrossActComparator cac=new CrossActComparator();
	
	//test
	CrossWar act;

	/**
	 * ����һ���
	 * @param sid �sid
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
			//���ʱ��Ϸ���
			if(!act.checkDate(date))return 2;
			act.setId(actuid.getPlusUid());
			//����ʱ����
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
	
	/** �޸Ľ��� */
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
	 * ǿ�ƽ������һ���
	 * @param sid �sid
	 */
	public void forceOver(int sid)
	{
		CrossAct act=getLastAct(sid,false);
		if(act==null||act.getEtime()<TimeKit.getSecondTime()) return;
		act.setForceover(true);
		act.setNeedsave(true);
		change(this,OVER_ACT,act);
	}
	
	/** ��ʾ�ѿ�����б� */
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
	
	/** ͨ��sid��ȡ���һ��� */
	public CrossAct getLastAct(int sid,boolean create)
	{
		ArrayList alist=(ArrayList)actmap.get(sid);
		if(alist==null||alist.size()<=0) return null;
		return (CrossAct)alist.get(alist.size()-1);
	}
	/** ͨ��sid ��ȡһ�� */
	public ArrayList getActsBySid(int sid)
	{
		return (ArrayList)actmap.get(sid);
	}
	
	/** ���һ��� */
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

	/** ��ʼ�� */
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
		// ������ʱ��
		TimerCenter.getMinuteTimer().add(new TimerEvent(this,"",saveCD*1000));
	}
	/** �������л */
	@Override
	public void onTimer(TimerEvent e)
	{
		saveActs();
	}
	
	/** �������л */
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

	/** ��ȡ���Ϣ bySid */
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
