package foxu.cross.war;

import java.io.IOException;
import java.util.HashMap;

import mustang.event.ChangeListener;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.math.MathKit;
import mustang.set.ArrayList;
import mustang.set.SetKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cross.server.CrossAct;
import foxu.cross.server.CrossActManager;
import foxu.cross.server.CrossServer;
import foxu.cross.server.DataAction;
import foxu.fight.FightScene;
import foxu.sea.InterTransltor;
import foxu.sea.PublicConst;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.uid.UidKit;


/**
 * ��������ս������
 * @author yw
 *
 */
public class CrossWarManager implements TimerListener,DataAction,ChangeListener
{
	private static Logger log=LogFactory.getLogger(CrossWarManager.class);
	/* static fields */
	/** �������ͳ���   STEP ͬ������,REP Ԥ��ս�� ,SN nǿ,CUT ��ȡ�������,PROͬ��������Ա,FIN_REP����ս��,SEND_BETͬ��Ѻע��*/
	public static int STEP=0,REP=1,SN=2,CUT=3,PRO=4,FIN_REP=5,SEND_BET=6;
	/** �������ݳ��� JION �μ�,SUBMIT ���������ύ�������,GET_AWARD��ȡ���� ,GET_WAR��ȡ���п��ս,BETѺע,GET_SERVER��ȡ��������Ϣ*/
	public static int JION=0,SUBMIT=1,GET_AWARD=2,GET_WAR=3,BET=4,GET_SERVER=5;
	/** �������󷵻� JION_SUCCC�μӳɹ� JION_END������ֹ JION_SERVERδ�Ը÷����� JION_HAD�Ѳμ� */
	public static int JION_SUCC=0,JION_END=1,JION_SERVER=2,JION_HAD=3;
	/** ���ô��󷵻�  SET_SUCC���óɹ� SET_PLAEYRû�и����  SET_CANOT������ʱ��� */
	public static int SET_SUCC=0,SET_PLAEYR=1,SET_CANOT=2;
	/** ��ȡ���� ���󷵻� GET_SUCC��ȡ�ɹ� GET_HAD ����ȡ GET_NOPLAYER�޸���Ա GET_CANNOT���콱ʱ��*/
	public static int GET_SUCC=0,GET_HAD=1,GET_NOPLAYER=2,GET_CANNOT=3;
	/** ��ȡ���ս���� ���󷵻� */
	public static int GETACT_SUCC=0,GETACT_NOACT=1;
	/** Ѻע ���󷵻�  BET_CANNOT��ǰ������ע,BET_NOPLAYER�޸����,BET_NOACT�޿��ս�*/
	public static int BET_SUCC=0,BET_CANNOT=1,BET_NOPLAYER=2,BET_NOACT=3;
	/** ����ͷ��˴������� */
	String actionPort="11";
	/** ���ս���� */
	CrossWar warsave;
	/** ���������� */
	CrossActManager actmanager;
	/** ���ս��ҹ����� */
	CrossWarPlayerManager cWarPlayerManager;
	/** ���ս������������ */
	CrossWarServerManager cWarServerManager;
	
	/** ս�����ݿ� �������� */
	CrossWarSaveDBAccess saveDBAccess;
	
	/** ������Ψһid�ṩ�� */
	UidKit crossuid;
	/** ���ս��Ψһid�ṩ�� */
	UidKit repuid;
	
	/** ��ǰ������Ա */
	CrossWarPlayer[] ps;
	
	/** ս���Ƚ��� */
	CrossWarPFComparator cpfc=new CrossWarPFComparator();
	/** ���ֱȽ��� */
	CrossWarPGComparator cpgc=new CrossWarPGComparator();
	/** ��űȽ��� */
	CrossWarPNComparator cpnc=new CrossWarPNComparator();
	/** Ԥ��ս������ */
	ArrayList rep=new ArrayList();
	/** ����ս������ */
	ArrayList finalRep=new ArrayList();
	/** ��ǰս�����͵��ڼ��� */
	int crep;
	/** ��ǰÿ�η����� */
	int cnum;
	/** ����ս����ǰ�����ڼ��� */
	int frep;
	/** ����ս����ǰ�浽�ڼ��� */
	int fsave;
//	/** ˢ��Ѻע�Ƿ��ڼ���״̬ */
//	boolean flushBet;
	int openTime=0;
	
	boolean isclose;
	
	/** ��������ƶ��� */
	boolean push;
	
	Object pushlock=new Object();
	
	public void setPush(boolean push)
	{
		synchronized(pushlock)
		{
			this.push=push;
		}
	}
	
	public UidKit getRepuid()
	{
		return repuid;
	}
	
	public void setRepuid(UidKit repuid)
	{
		this.repuid=repuid;
	}

	public CrossWarPlayerManager getcWarPlayerManager()
	{
		return cWarPlayerManager;
	}
	
	public void setcWarPlayerManager(CrossWarPlayerManager cWarPlayerManager)
	{
		this.cWarPlayerManager=cWarPlayerManager;
	}
	
	public CrossWarServerManager getcWarServerManager()
	{
		return cWarServerManager;
	}
	
	public void setcWarServerManager(CrossWarServerManager cWarServerManager)
	{
		this.cWarServerManager=cWarServerManager;
	}
	
	public CrossActManager getActmanager()
	{
		return actmanager;
	}
	
	public void setActmanager(CrossActManager actmanager)
	{
		this.actmanager=actmanager;
	}
	
	public UidKit getCrossuid()
	{
		return crossuid;
	}
	
	public void setCrossuid(UidKit crossuid)
	{
		this.crossuid=crossuid;
	}
	/** ��ʼ�� */
	public void init()
	{
		if(!PublicConst.crossServer)return;
		warsave=(CrossWar)actmanager.getLastAct(CrossAct.WAR_SID,false);
		initPs();
		initFinalRep();
		TimerCenter.getMinuteTimer().add(new TimerEvent(this,"war",5*1000));// �ʱ��
		TimerCenter.getSecondTimer().add(new TimerEvent(this,"rep",5*1000));// ս��ʱ��
		TimerCenter.getSecondTimer().add(new TimerEvent(this,"bet",5*1000));// Ѻעʱ��
	}
	/** �ƶ�ս������ */
	public void pushReport()
	{
		if(rep.size()>crep)
		{
			if(cnum==0)
			{
				int now=TimeKit.getSecondTime();
				cnum=rep.size()/((warsave.getStepEtime()-now-300)/30)+1;// �׶ν���ǰ5���Ӳ������
				if(cnum>0&&cnum<100) cnum=100;
				if(cnum<=0) cnum=1000;// ʱ�䲻��ʱ Ĭ��30s�� 1000��
			}
			sendRep();
		}
	}
	/** ����Ԥ��ս�� */
	public void sendRep()
	{
		//System.out.println(crep+"-----rep.size()-----:"+rep.size());
		ArrayList slit=cWarServerManager.getServerlist();
		ByteBuffer data=new ByteBuffer();
		for(int i=0;i<slit.size();i++)
		{
			CrossServer cs=(CrossServer)slit.get(i);
			int count=0;
			data.clear();
			data.writeByte(REP);
			int top=data.top();
			data.writeShort(count);
			for(int k=crep;k<rep.size()&&k<crep+cnum;k++)
			{
				CrossWarRoundSave save=(CrossWarRoundSave)rep.get(k);
//				//System.out.println(cs.getIp()+":---------:"+save.getAttackip()+":-----:"+save.getDefenceip());
				if(cs.getIp().equals(save.getAttackip())
					||cs.getIp().equals(save.getDefenceip()))
				{
					save.showBytesWrite(data);
					count++;
				}
			}
			//System.out.println("------rep-count-------:"+count);
			if(count>0)
			{
				int nowtop=data.top();
				data.setTop(top);
				data.writeShort(count);
				data.setTop(nowtop);
				sendHttpData(data,cs.getIp(),cs.getPort());
			}
		}
		crep=rep.size()<crep+cnum?rep.size():crep+cnum;
		if(crep>=rep.size())
		{
			crep=rep.size();
			cnum=0;
		}
	}
	/** ��������ս��*/
	public void sendFinalRep()
	{
//		//System.out.println("-----finalRep.size()-----:"+finalRep.size());
		ByteBuffer data=new ByteBuffer();
		data.writeByte(FIN_REP);
		data.writeShort(finalRep.size()-frep);
		for(;frep<finalRep.size();frep++)
		{
			CrossWarRoundSave save=(CrossWarRoundSave)finalRep.get(frep);
			save.showBytesWrite(data);
		}
		broadCast(data);
	}
	/** �ƶ����ս */
	public void pushForwardWar()
	{
		if(isclose)return;
		if(!PublicConst.crossServer)return;
		if(warsave==null) return;
		int now=TimeKit.getSecondTime();
		if(warsave.isover())
		{
			finshWar();
			return;
		}
		if(push)return;
		setPush(true);
		// //System.out.println(SeaBackKit.formatDataTime(now)+":----now------StepE----:"+SeaBackKit.formatDataTime(warsave.getStepEtime()));
		if(now>=warsave.getStepStime()) 
		{
			doStep();
			warsave.setNeedsave(true);
		}
		if(now>=warsave.getStepEtime())
		{
			//System.out.println(warsave.hashCode()+"------nextStep-----");
			finishStep();
			nextStep();
			warsave.setNeedsave(true);
		}
		setPush(false);
	}
	/** ��ɿ��ս */
	public void finshWar()
	{
		//����
		//���
		resetClear();
	}
	/** ��ɵ�ǰ���� */
	public void finishStep()
	{
//		//System.out.println("-----warsave.getStep()--------");
		if(SeaBackKit.isContainValue(CrossWar.CANSET,warsave.getStep()))
		{
			// �ر�����
			// ��ȡ�������
			sendCut();
		}
		else if(warsave.getStep()==CrossWar.T1)
		{
			//����64ǿ����
			sendPromotion();
			sendSn(CrossWar.S64);
		}
		else if(SeaBackKit.isContainValue(CrossWar.FINAL,warsave.getStep()))
		{
			int n=warsave.getMatch()/2;
			sendPromotion();
			if(finalRep.size()>frep)
			{
				sendFinalRep();
			}
			if(n!=1)
			{
				// ��������nǿ����
				sendSn(n);
			}
			else
			{
				// �����ھ�
				sendChampion(n);
			}

		}
		else if(warsave.getStep()==CrossWar.T14)
		{
			//���� ����
		}
	}
	/** ��ȡ������� */
	public void sendCut()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeByte(CUT);
		broadCast(data);
	}
	/** ����nǿ */
	public void sendSn(int n)
	{
		ByteBuffer data=new ByteBuffer();
		for(int i=0;i<ps.length;i++)
		{
			data.clear();
			data.writeByte(SN);
			String[] ipp=cWarServerManager.getIpPort(ps[i]);
			if(ipp!=null)
			{
				data.writeUTF(ps[i].getSeverName());
				data.writeUTF(ps[i].getName());
				data.writeShort(n);
				sendHttpData(data,ipp[0],ipp[1]);
			}
		}
	}
	/** �����ھ� */
	public void sendChampion(int n)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeByte(SN);
		data.writeUTF(ps[0].getSeverName());
		data.writeUTF(ps[0].getName());
		data.writeShort(n);
		broadCast(data);
	}
	/** ͬ�� ���� */
	public void sendStep()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeByte(STEP);
		warsave.showBytesWrite(data);
		broadCast(data);
	}
	/** ͬ��������Ա */
	public void sendPromotion()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeByte(PRO);
		data.writeShort(ps.length);
//		//System.out.println("-----ps.length-------:"+ps.length);
		for(int i=0;i<ps.length;i++)
		{
			ps[i].showBytesWrite(data);
		}
		broadCast(data);
	}
	/** ͬ��ĳ��������Ա */
	public void sendPromotionOne(int cid)
	{
		if(cid<=0)return;
		CrossWarPlayer cp=cWarPlayerManager.getPlayerById(cid);
		ByteBuffer data=new ByteBuffer();
		data.writeByte(PRO);
		data.writeShort(1);
		cp.showBytesWrite(data);
		broadCast(data);
	}
	/** �㲥 */
	public void broadCast(ByteBuffer data)
	{
		ArrayList slist=cWarServerManager.getServerlist();
		for(int i=0;i<slist.size();i++)
		{
			CrossServer s=(CrossServer)slist.get(i);
			sendHttpData(data,s.getIp(),s.getPort());
		}
	}
	/** ��һ���� */
	public void nextStep()
	{
		warsave.nextStep();
		sendStep();
	}
	
	/** ִ�е�ǰ���� */
	public void doStep()
	{
		if(warsave.dostep) return;
		int now=TimeKit.getSecondTime();
		if(now>=warsave.getStepStime()&&!warsave.dostep)
		{
			if(SeaBackKit.isContainValue(CrossWar.CANSET,warsave.getStep()))
			{
				// ��������
			}
			else if(warsave.getStep()==CrossWar.T1)//Ԥ��
			{
				// ��������  todo
				
				// ����256 ˫����̭
				ps=cWarPlayerManager.getEnterPlayers(warsave.getCanMatch());
				int needlen=CrossWar.S64-ps.length;
				//needlen=5000;
				if(needlen>0)
				{
//					//System.out.println("-----needlen--0----:"+needlen);
					createDefPlayer(needlen);
					ps=cWarPlayerManager.getEnterPlayers(warsave.getCanMatch());
//					//System.out.println(TimeKit.getSecondTime()+"---------ps------:"+ps.length);
				}
				ps=preDubEliMatch(ps);
				//System.out.println(TimeKit.getSecondTime()+"---------ps--1----:"+ps.length);
				// 256 ���� ���� 64ǿ
				ps=preMatch(ps);
				//System.out.println(TimeKit.getSecondTime()+"---------ps--2----:"+ps.length);
				//
				for(int i=0;i<ps.length;i++)
				{
					ps[i].reset();
					cWarPlayerManager.addPlayer(ps[i]);
				}
			}
			else if(SeaBackKit.isContainValue(CrossWar.FINAL,warsave.getStep()))//����
			{
				//System.out.println("------CrossWar.FINAL-----------");
				ps=finKnockoutMatch(ps);
				for(int i=0;i<ps.length;i++)
				{
					ps[i].reset();
					cWarPlayerManager.addPlayer(ps[i]);
				}
				
			}
			else if(warsave.getStep()==CrossWar.T14)//����
			{
				// �ƺ� ���ս
				// ������������
			}
			warsave.setDostep(true);
		}

	}
	/** ����Ĭ����� */
	public void createDefPlayer(int count)
	{
		if(count<=0) return;
		for(int i=0;i<count;i++)
		{
			CrossServer cs=(CrossServer)cWarServerManager.getServerlist()
				.get(
					MathKit.randomValue(0,cWarServerManager.getServerlist()
						.size()));

			int pid=cs.getPlatid();
			int serverid=cs.getSeverid();
			int areaid=cs.getAreaid();
			int platid=cs.getPlatid();
			int sid=1;
			int cid=crossuid.getPlusUid();
			String name=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"mystical fleet")
				+i;
			CrossWarPlayer player=new CrossWarPlayer(cid,pid,name,platid,
				areaid,serverid,sid,warsave.getId());
			player.setLevel(MathKit.randomValue(50,70));
			player.setFightscore(MathKit.randomValue(0,50));
			player.setSeverName(cs.getSeverName());
			player.setNational(cs.getNational());
			player.setDefFleet();
			// //System.out.println("-----player---rank--:"+player.getRank());
			cWarPlayerManager.addPlayer(player);
		}

	}
	
	/**
	 * Ԥ�� ˫����̭��
	 * @param ps ������Ա
	 * @param fn ǰn����
	 * @return ������Ա
	 */
	public CrossWarPlayer[] preDubEliMatch(CrossWarPlayer[] ps)
	{
		SetKit.sort(ps,cpfc);//ս������
		CrossWarPlayer[][] cps=cpuGroup(ps,warsave.getGroup());
		ArrayList pre=new ArrayList();
		for(int i=0;i<cps.length;i++)
		{
			CrossWarPlayer[] gps=dubEliMatch(cps[i],warsave.getFn());
			for(int k=0;k<gps.length;k++)
			{
//				if(gps[k]==null)//System.out.println("--------gps----null------");
				pre.add(gps[k]);
			}
		}
		CrossWarPlayer rps[]=new CrossWarPlayer[pre.size()];
		for(int i=0;i<rps.length;i++)
		{
			rps[i]=(CrossWarPlayer)pre.get(i);
		}
		return rps;
	}
	
	/**
	 * ˫����̭��
	 * @param ps ������Ա
	 * @param fn ǰn����
	 * @return ������Ա
	 */
	public CrossWarPlayer[] dubEliMatch(CrossWarPlayer[] ps,int fn)
	{
		if(ps.length<=fn) return ps;
		ArrayList win=new ArrayList(ps);
		CrossWarPlayer[] ops=new CrossWarPlayer[ps.length];
		System.arraycopy(ps,0,ops,0,ps.length);
		ArrayList org_ps=new ArrayList(ops);
		while(win.size()>=fn)// ˫�� ʤ����ѡ��
		{
			win=dubEliWinMatch(win);
		}
		CrossWarPlayer[] prom=new CrossWarPlayer[fn];
		for(int i=0;i<win.size();i++)
		{
			prom[i]=(CrossWarPlayer)win.get(i);
			org_ps.remove(prom[i]);
		}
		ArrayList lose=dubEliLoseMatch(org_ps,fn-win.size());// ˫�� ������ѡ��
		// //System.out.println("-----win-----:"+win.size());
		// //System.out.println("-----lose----:"+lose.size());
		// //System.out.println(fn+":-pre-now-:"+(win.size()+lose.size()));
		if(win.size()+lose.size()!=fn)
		{
			log.error("------dubEliMatch---error------");
			//System.out.println("------dubEliMatch---error------");
		}
		for(int i=0;i<lose.size();i++)
		{
			prom[i+win.size()]=(CrossWarPlayer)lose.get(i);
		}
		return prom;
	}
	/**
	 * ˫��  ʤ����ѡ��
	 * @param ps ������Ա
	 * @return ������Ա
	 */
	public ArrayList dubEliWinMatch(ArrayList ps)
	{
		ArrayList win=new ArrayList();
		while(ps.size()>0)
		{
			if(ps.size()==1)
			{
				win.add(ps.remove(0));
				break;
			}
//			//System.out.println(ps.size());
			int r1=MathKit.randomValue(0,ps.size());
			CrossWarPlayer p1=(CrossWarPlayer)ps.remove(r1);
			int r2=MathKit.randomValue(0,ps.size());
			CrossWarPlayer p2=(CrossWarPlayer)ps.remove(r2);
			if(roundFight(p1,p2,false,CrossWarRoundSave.PRE,false))
			{
				win.add(p1);
			}
			else
			{
				win.add(p2);
			}
		}
		return win;
	}
	/**
	 * ˫��  ������ѡ��
	 * @param ps ������Ա
	 * @param fn ��������
	 * @return ������Ա
	 */
	public ArrayList dubEliLoseMatch(ArrayList ps,int fn)
	{
		while(ps.size()>=fn*2)
		{
			ps=dubEliWinMatch(ps);
		}
		if(ps.size()==fn)return ps;
		ArrayList win=new ArrayList();//ps.size>fn
		ArrayList org_ps=(ArrayList)ps.clone();
		while(win.size()!=fn)
		{
			ps=dubEliWinMatch(ps);
			if(ps.size()+win.size()>fn) continue;
			for(int i=0;i<ps.size();i++)
			{
				Object obj=ps.get(i);
				win.add(obj);
				org_ps.remove(obj);
			}
			ps=org_ps;
		}
		return win;
	}
	
	
	/**
	 * Ԥ��������
	 * @param ps ������Ա
	 * @return ������Ա
	 */
	public CrossWarPlayer[] preMatch(CrossWarPlayer[] ps)
	{
		SetKit.sort(ps,cpfc);//ս������
		CrossWarPlayer[][] cps=cpuGroup(ps,warsave.getGgroup());
		for(int i=0;i<cps.length;i++)
		{
			preGroupMatch(cps[i],CrossWarRoundSave.PRE);
			SetKit.sort(cps[i],cpgc);
		}
		int fn=warsave.getGfn();
		ArrayList enterlist=new ArrayList();
		int sn=fn*warsave.getGgroup();
//		int  count=0;
		for(int i=0;i<cps.length;i++)
		{
			int len=fn<cps[i].length?fn:cps[i].length;
			for(int k=0;k<len;k++)
			{
//				//System.out.println("-----cps[i][k]------:"+cps[i][k].getGoal());
//				count++;
				cps[i][k].setRank(sn);//����nǿ
				enterlist.add(cps[i][k]);
			}
		}
		//System.out.println(sn+":-----256-------count--------:"+enterlist.size());
		int len=sn>enterlist.size()?enterlist.size():sn;
		CrossWarPlayer rps[]=new CrossWarPlayer[len];
		int i=0;
		for(;i<rps.length;i++)
		{
			rps[i]=(CrossWarPlayer)enterlist.get(i);
//			rps[i].setNum(i+1);
		}
		SetKit.sort(rps,cpfc);//ս������
//		//System.out.println("-----------------rps--k0---------------��"+rps.length);
//		for(int k=0;k<rps.length;k++)
//		{
//			//System.out.println(k+"---rps--k0-:"+rps[k].getFightscore());
//		}
		int mid=rps.length/2;
		ArrayList top=new ArrayList();
		ArrayList low=new ArrayList();
		for(int k=0;k<rps.length;k++)
		{
			if(k<mid)
				top.add(rps[k]);
			else
				low.add(rps[k]);
		}
		for(int k=0;k<mid;k++)
		{
			CrossWarPlayer cp=(CrossWarPlayer)top.remove(MathKit
				.randomValue(0,top.size()));
			cp.setNum(k*2+1);
			rps[k*2]=cp;
			cp=(CrossWarPlayer)low.remove(MathKit.randomValue(0,low.size()));
			cp.setNum(k*2+2);
			rps[k*2+1]=cp;
		}
//		//System.out.println("-----------------rps--k1---------------��"+rps.length);
//		for(int k=0;k<rps.length;k++)
//		{
//			//System.out.println(k+"---rps--k1-:"+rps[k].getFightscore());
//		}
		return rps;

	}
	
	/**
	 * Ԥ������
	 * @param ps  ������Ա
	 * @return ����
	 */
	public CrossWarPlayer[][] cpuGroup(CrossWarPlayer[] ps,int group)
	{
		int len=ps.length/group+(ps.length%group==0?0:1);
		CrossWarPlayer[][] cps=new CrossWarPlayer[group][len];

		int max0=group<ps.length?group:ps.length;
		for(int i=0;i<max0;i++)
		{
//			ps[i].setGroup(i+1);
			cps[i][0]=ps[i];
		}
		for(int i=max0,k=0,m=group;i<ps.length;i++,k++,m++)
		{
			if(m==group)
			{
				m=0;
				k=MathKit.randomValue(0,group);
			}
			k%=group;
//			ps[i].setGroup(k+1);
			cps[k][i/group]=ps[i];
		}
		// �Լ����  �ų���ֵ
		for(int i=0;i<cps.length;i++)
		{
			for(int k=cps[i].length-1;k>=0;k--)
			{
				if(cps[i][k]!=null) continue;
				CrossWarPlayer[] cs=new CrossWarPlayer[cps[i].length-1];
				System.arraycopy(cps[i],0,cs,0,cs.length);
				cps[i]=cs;
			}
		}
		return cps;
	}
	
	/** Ԥ������С���� */
	public void preGroupMatch(CrossWarPlayer[] ps,int matchtype)
	{
		for(int j=0,i=j+1;j<ps.length-1&&i<ps.length;i++)
		{
			roundFight(ps[j],ps[i],true,matchtype,false);
			if(i==ps.length-1)
			{
				j++;
				i=j;
			}
		}
	}
	
	/**
	 * ���� ��̭��
	 * @param ps ������Ա
	 * @return ������Ա
	 */
	public CrossWarPlayer[] finKnockoutMatch(CrossWarPlayer[] ps)
	{
		int sn=warsave.getMatch();
		//System.out.println("------------------:"+warsave.getMatch());
		CrossWarPlayer[] cps=new CrossWarPlayer[sn/2];
		//System.out.println((sn/2)+":------ps.length--------��"+ps.length);
		for(int i=0;i<ps.length;i+=2)
		{
			CrossWarPlayer p2=(i+1)<ps.length?ps[i+1]:null;
			boolean p1win=roundFight(ps[i],p2,false,getTypeByMatch(sn),true);
//			//System.out.println(ps[i].getNum()+":-------finKnockoutMatch-----------:"+ps[i+1].getNum());
			cps[i/2]=p1win?ps[i]:p2;
			cps[i/2].setRank(sn/2);
		}
		//���ùھ�
		if(sn/2==CrossWar.S1)
		{
			//System.out.println("---server---setCmid-------:"+cps[0].getCrossid());
			warsave.setCmid(cps[0].getCrossid());
		}
		return cps;
	}
	
	
	
	
	/** һ�ֶ�ս�����ظ�һ�Σ�*///�豣֤p1 != null
	public boolean roundFight(CrossWarPlayer p1,CrossWarPlayer p2,boolean isgoal,int matchtype,boolean isfinal)
	{
		CrossWarFightSave save1=fight(p1,p2);
		CrossWarFightSave save2=fight(p2,p1);
		CrossWarRoundSave save=new CrossWarRoundSave();
		save.setS1(save1);
		save.setS2(save2);
		boolean p1Win=false;
		if(save1.isAttackWin()&&!save2.isAttackWin())
		{
			p1Win=true;
		}
		else if((save1.isAttackWin()&&save2.isAttackWin())
			||(!save1.isAttackWin()&&!save2.isAttackWin()))
		{
			if(save.getLosePercent(true)<save.getLosePercent(false))
			{
				p1Win=true;
			}
		}
		if(isgoal)
		{
			if(p1Win)
			{
				p1.incrGoal();
			}
			else
			{
				p2.incrGoal();
			}
		}
		// ��ս�����뻺��
		int winid=p1.getCrossid();
		if(!p1Win) winid=p2.getCrossid();
		save.setCreatetime(TimeKit.getSecondTime());
		save.setId(repuid.getPlusUid());
		save.setWarid(warsave.getId());
		save.setType(matchtype);
		save.setAttackid(p1.getCrossid());
		save.setAttackpid(p1.getId());
		save.setAttackname(p1.getName());
		save.setAservername(cWarServerManager.getServerName(p1));
		save.setAttackip(cWarServerManager.getServerIp(p1));
		save.setAttacklv(p1.getLevel());
		save.setAnational(cWarServerManager.getServerNatoinal(p1));
		if(p2!=null)
		{
			save.setDefenceid(p2.getCrossid());
			save.setDefencepid(p2.getId());
			save.setDefencename(p2.getName());
			save.setDservername(cWarServerManager.getServerName(p2));
			save.setDefenceip(cWarServerManager.getServerIp(p2));
			save.setDefencelv(p2.getLevel());
			save.setDnational(cWarServerManager.getServerNatoinal(p2));
		}
		// //System.out.println(p1.getName()+":"+p1.getCrossid()+":---p1-----p2----:"+p2.getName()+":"+p2.getCrossid()+":---id------:"+save.getId()+":----matchtype----:"+matchtype);
		save.setWinid(winid);

//		//System.out.println("--------rep--------add--save--");
		if(!isfinal)
		{
			rep.add(save);
		}
		else
		{
			finalRep.add(save);
		}
		return p1Win;
	}
	
	/** ��ս */
	public CrossWarFightSave fight(CrossWarPlayer attack,CrossWarPlayer defence)
	{
		CrossWarFightSave fightSave=new CrossWarFightSave();
		if(attack!=null&&defence!=null)
		{
			FleetGroup attacker=attack.getFleetGroup(true);
			FleetGroup defencer=defence.getFleetGroup(false);
			FightScene scene=FightSceneFactory.factory.create(attacker,
				defencer);
			FightShowEventRecord r=FightSceneFactory.factory.fight(scene,
				null);
			int attackLose=attacker.getMaxNum()-attacker.nowTotalNum();
			int defenceLose=defencer.getMaxNum()-defencer.nowTotalNum();
			if(scene.getSuccessTeam()==0)
			{
				fightSave.setAttackWin(true);
			}
			fightSave.setAttackSum(attacker.getMaxNum());
			fightSave.setAttackLose(attackLose);
			fightSave.setDefenceSum(defencer.getMaxNum());
			fightSave.setDefenceLose(defenceLose);
			fightSave.setAlist(attacker.hurtList(FleetGroup.HURT_TROOPS));
			fightSave.setDlist(defencer.hurtList(FleetGroup.HURT_TROOPS));
			fightSave.setAttackOfficers(attacker.getOfficerFleetAttr()
				.getUsedOfficers());
			fightSave.setDefendOfficers(defencer.getOfficerFleetAttr()
				.getUsedOfficers());
			fightSave.setRecord(r.getRecord());
			attacker.resetBossShips();
			defencer.resetBossShips();
		}
		else if(defence==null)
		{
			fightSave.setAttackWin(true);
		}
		return fightSave;
	}
	@Override
	public void onTimer(TimerEvent e)
	{
		if(e.getParameter().equals("war"))
		{
			pushForwardWar();
			saveFinalRep(false);
		}
		else if(e.getParameter().equals("rep"))
		{
			pushReport();
		}
		else if(e.getParameter().equals("bet"))
		{
			flushBet();
		}

	}
	
	public ByteBuffer sendHttpData(ByteBuffer data,String ip,String port)
	{
		HttpRequester request=new HttpRequester();
		ByteBuffer tdata=(ByteBuffer)data.clone();
		String httpData=SeaBackKit.createBase64(tdata);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// ����port
		map.put("port",actionPort);
		HttpRespons re=null;
		try
		{
			re=request.send(
				"http://"+ip+":"+port+"/","POST",map,
				null);
		}
		catch(IOException e)
		{
			// TODO �Զ����� catch ��
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
	@Override
	public void readAction(ByteBuffer data)
	{
		int type=data.readUnsignedByte();
		//System.out.println("------readAction-----type------:"+type);
		if(type==JION)
		{
			jion(data);
		}
		else if(type==SUBMIT)
		{
			submitPlayer(data);
		}
//		else if(type==GET_AWARD)
//		{
//			getAward(data);
//		}
		else if(type==GET_WAR)
		{
			getWars(data);
		}
		else if(type==BET)
		{
			bet(data);
		}
		else if(type==GET_SERVER)
		{
			getLocalServer(data);
		}
		else 
		{
			data.clear();
			data.writeByte(127);// �޷����������Ϣ
		}

	}
	/** �μӱ��� */
	public void jion(ByteBuffer data)
	{
		int pid=data.readInt();
		int serverid=data.readUnsignedShort();
		int areaid=data.readUnsignedShort();
		int platid=data.readUnsignedShort();
		//System.out.println(serverid+":-----serverid-----areaid---:"+areaid+":---platid--:"+platid);
		CrossServer cs=cWarServerManager.getServerById(platid,areaid,
			serverid);
		if(cs==null)
		{
			data.clear();
			data.writeByte(JION_SERVER);
		}
		else if(cWarPlayerManager.isExist(platid,areaid,serverid,pid))
		{
			data.clear();
			data.writeByte(JION_HAD);
		}
		else if(warsave==null||warsave.joinEnd())
		{
			data.clear();
			data.writeByte(JION_END);
		}
		else
		{
			int sid=data.readUnsignedShort();
			String name=data.readUTF();
			String aname=data.readUTF();//��������
			int cid=crossuid.getPlusUid();
			CrossWarPlayer player=new CrossWarPlayer(cid,pid,name,platid,
				areaid,serverid,sid,warsave.getId());
			player.setAname(aname);
			player.setSeverName(cs.getSeverName());
			player.setNational(cs.getNational());
			cWarPlayerManager.addPlayer(player);
			setShipAtt(player,data);
			data.clear();
			data.writeByte(JION_SUCC);
			data.writeInt(cid);
			data.writeUTF(cs.getSeverName());
			data.writeUTF(cs.getNational());
		}

	}
	/** ���ý��Ӻ����� */
	public void setShipAtt(CrossWarPlayer player,ByteBuffer data)
	{
		if(player==null)
		{
			int cid=data.readInt();
			player=cWarPlayerManager.getPlayerById(cid);
		}
		if(player==null)
		{
			data.clear();
			data.writeByte(SET_PLAEYR);
		}
		if(warsave==null||!warsave.setCan()
			||player.getRank()!=warsave.getMatch())
		{
			data.clear();
			data.writeByte(SET_CANOT);
		}
		else
		{
			int lv=data.readUnsignedShort();
			int fightscore=data.readInt();
			player.setLevel(lv);
			player.setFightscore(fightscore);
			player.getAdjustment().crossBytesRead(data);
			player.bytesReadShiplevel(data);
			player.bytesReadAttacklist(data);
			player.bytesReadDefencelist(data);
			player.bytesReadOFS(data);
		}
		
	}
	/** ���շ������ύ�������  */
	public void submitPlayer(ByteBuffer data)
	{
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			int cid=data.readInt();
			String name=data.readUTF();
			CrossWarPlayer player=cWarPlayerManager.getPlayerById(cid);
			if(player==null)
			{
				log.error("-----receivePlayer------error------cid-----:"+cid);
				player=new CrossWarPlayer();
			}
			int lv=data.readUnsignedShort();
			int fightscore=data.readInt();
			player.setName(name);
			player.setLevel(lv);
			player.setFightscore(fightscore);
			player.getAdjustment().crossBytesRead(data);
			player.bytesReadShiplevel(data);
			player.bytesReadAttacklist(data);
			player.bytesReadDefencelist(data);
			player.bytesReadOFS(data);
			player.setSave(true);
		}
		//��ʽ��Ҫ��
		data.clear();
		data.writeByte(0);
	}
//	public void getAward(ByteBuffer data)
//	{
//		if(warsave==null||!warsave.isAward())
//		{
//			data.clear();
//			data.writeByte(GET_CANNOT);
//			return;
//		}
//		int crossid=data.readInt();
//		data.clear();
//		CrossWarPlayer player=cWarPlayerManager.getPlayerById(crossid);
//		if(player==null)
//		{
//			data.writeByte(GET_NOPLAYER);
//		}
//		else if(player.isGetAward())
//		{
//			data.writeByte(GET_HAD);
//		}
//		else
//		{
//			data.writeByte(GET_SUCC);
//			data.writeShort(warsave.getAwardSid(player.getRank()));
//		}
//	}
	/** ��ȡ���п��ս */
	public void getWars(ByteBuffer data)
	{
		data.clear();
		// ���л��
		bytesWriteWars(data);
		// ���л�64ǿ
		bytesWriteS64(data);
		// ���л�����ս��
		bytesWriteFinalRep(data);
	}
	
	/** ��ȡ���п��ս */
	public void getLocalServer(ByteBuffer data)
	{
		String ip=data.readUTF();
//		//System.out.println("--server---getLocalServer-----ip-----:"+ip);
		data.clear();
		CrossServer cs=cWarServerManager.getServerByIp(ip);
//		//System.out.println("--server---getLocalServer-----cs-----:"+cs);
		if(cs==null)
		{
			data.writeUTF(null);
			data.writeUTF(null);
		}
		else
		{
			data.writeUTF(cs.getSeverName());
			data.writeUTF(cs.getNational());
		}
	}
	
	/** ��ȡս������ ���ݱ������� */
	public int getTypeByMatch(int match)
	{
		int type=CrossWarRoundSave.PRE;
		if(match==CrossWar.S64)
		{
			type=CrossWarRoundSave.FIN64;
		}
		else if(match==CrossWar.S32)
		{
			type=CrossWarRoundSave.FIN32;
		}
		else if(match==CrossWar.S16)
		{
			type=CrossWarRoundSave.FIN16;
		}
		else if(match<CrossWar.S16 && match>=CrossWar.S2)
		{
			type=CrossWarRoundSave.FINAL;
		}
		return type;
	}
	/**
	 * Ѻע
	 * @param data
	 * @return ��Ѻע��ҿ��id
	 */
	public void bet(ByteBuffer data)
	{
		int serverid=data.readUnsignedShort();
		int areaid=data.readUnsignedShort();
		int platid=data.readUnsignedShort();
		int cid=data.readInt();
		int gems=data.readInt();
		data.clear();
		if(!cWarServerManager.isExist(platid,areaid,serverid)||warsave==null)
		{
			data.writeByte(BET_NOACT);
		}
		else if(!warsave.betCan())
		{
			data.writeByte(BET_CANNOT);
		}
		else
		{
			CrossWarPlayer cp=cWarPlayerManager.getPlayerById(cid);
			if(cp==null)
			{
				data.writeByte(BET_NOPLAYER);
			}
			else
			{
				cp.addBet(gems);
				cp.setSave(true);
				data.writeByte(BET_SUCC);
			}

		}
	}
	
	/** ˢ��Ѻע�� */
	public void flushBet()
	{
		if(ps==null||ps.length<CrossWar.S64) return;
		ByteBuffer data=new ByteBuffer();
		data.writeByte(SEND_BET);
		int top=data.top();
		int count=0;
		data.writeShort(count);
		for(int i=0;i<ps.length;i++)
		{
			if(!ps[i].isFlushBet()) continue;
			ps[i].showBytesWriteBet(data);
			count++;
		}
		if(count>0)
		{
			int nowtop=data.top();
			data.setTop(top);
			data.writeShort(count);
			data.setTop(nowtop);
			broadCast(data);
		}
	}
	
	

	@Override
	public void change(Object source,int type)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int v1,int v2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int v1,int v2,int v3)
	{
		// TODO Auto-generated method stub
		
	}
	
	/** �������ս��  */
	public int saveFinalRep(boolean closeserver)
	{
		if(closeserver) isclose=true;
//		if(isclose)
//		{
//			for(int i=0;i<ps.length;i++)
//			{
//				//System.out.println(i+":-------ps-----ps----:"+ps[i].getNum());
//			}
//		}
		//Thread.dumpStack();
		// test
		// if(true)return 0;
		int fail=0;
		for(;fsave<finalRep.size();fsave++)
		{
			fail+=saveDBAccess.save(finalRep.get(fsave))?0:1;
		}
		return fail;
	}
	/** ���ؾ���ս�� */
	public void initFinalRep()
	{
		if(warsave==null||warsave.isover()) return;
		String sql="select * from croundsave where warid="+warsave.getId();
		CrossWarRoundSave[] saves=saveDBAccess.loadBySql(sql);
		if(saves==null||saves.length<=0)return;
		finalRep.clear();
		for(int i=0;i<saves.length;i++)
		{
			finalRep.add(saves[i]);
//			//System.out.println(i+"--------saves[i]---------:"+saves[i].getId());
		}
		
	}
	/** ���ؽ�����Ա */
	public void initPs()
	{
		if(warsave==null) return;
		if(warsave.getStep()<CrossWar.T1) return;
//		int match=warsave.getMatch();
//		if(warsave.getStep()==CrossWar.T14) match=1;
		ps=cWarPlayerManager.getEnterPlayers(getMatch(warsave.getStep(),warsave.isDostep()));
		SetKit.sort(ps,cpnc);// �������
//		for(int i=0;i<ps.length;i++)
//		{
//			//System.out.println(i+":------ps----ps----------:"+ps[i].getNum());
//		}
	}
	public int getMatch(int step,boolean flag)
	{
		if(step==CrossWar.T1||step==CrossWar.T2)
		{
			if(CrossWar.T1==step && flag) return CrossWar.S64;
			return CrossWar.S0;
		}
		if(step==CrossWar.T3||step==CrossWar.T4)
		{
			if(CrossWar.T3==step && flag) return CrossWar.S32;
			return CrossWar.S64;
		}
		if(step==CrossWar.T5||step==CrossWar.T6)
		{
			if(CrossWar.T5==step && flag) return CrossWar.S16;
			return CrossWar.S32;
		}
		if(step==CrossWar.T7||step==CrossWar.T8)
		{
			if(CrossWar.T7==step && flag) return CrossWar.S8;
			return CrossWar.S16;
		}
		if(step==CrossWar.T9||step==CrossWar.T10)
		{
			if(CrossWar.T9==step && flag) return CrossWar.S4;
			return CrossWar.S8;
		}
		if(step==CrossWar.T11||step==CrossWar.T12)
		{
			if(CrossWar.T11==step && flag) return CrossWar.S2;
			return CrossWar.S4;
		}
		if(step==CrossWar.T13||step==CrossWar.T14) 
		{
			if(CrossWar.T13==step && flag) return CrossWar.S1;
			return CrossWar.S2;
		}
		return 0;
	}

	/** ���л�����ս�� */
	public void bytesWriteFinalRep(ByteBuffer data)
	{
		data.writeShort(finalRep.size());
		//System.out.println("-------bytesWriteFinalRep------------:"+finalRep.size());
		for(int i=0;i<finalRep.size();i++)
		{
			((CrossWarRoundSave)finalRep.get(i)).showBytesWrite(data);
		}

	}
	/** ���л�64ǿ */
	public void bytesWriteS64(ByteBuffer data)
	{
		CrossWarPlayer[] cps=cWarPlayerManager.getEnterPlayers(CrossWar.S64);
		data.writeShort(cps.length);
		//System.out.println("-----64---cps.length---:"+cps.length);
		for(int i=0;i<cps.length;i++)
		{
			//System.out.println("-----cps[i]-crossid--:"+cps[i].getCrossid());
			cps[i].showBytesWrite(data);
		}
	}
	/** ���л� ���п��ս */
	public void bytesWriteWars(ByteBuffer data)
	{
		ArrayList alist=actmanager.getActsBySid(CrossAct.WAR_SID);
		if(alist==null)
		{
			//System.out.println("-----alist==null--");
			data.writeShort(0);
		}
		else
		{
			//System.out.println("-----alist.size()----��"+alist.size());
			data.writeShort(alist.size());
			//System.out.println("-----alist.size()--11--��"+alist.size());
			for(int i=0;i<alist.size();i++)
			{
				//System.out.println(i+"-------alist.size()------:"+alist);
				try
				{
					CrossWar cw=(CrossWar)alist.get(i);
					//System.out.println("------cw--------:"+cw);
					cw.showBytesWrite(data);
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

	}
	
	/** ������ս */
	public void resetClear()
	{
		//�������ս��
		saveFinalRep(false);
		finalRep.clear();
		rep.clear();//����Ԥ��ս��
		ps=null;
		crep=0;
		cnum=0;
		frep=0;
		fsave=0;
		warsave=null;
	}

	@Override
	public void change(Object source,int type,Object value)
	{
		if(!(source instanceof CrossActManager)
			||!(value instanceof CrossWar)) return;
		if(type==CrossActManager.CREATE_ACT)
		{
			resetClear();// ����
			warsave=(CrossWar)value;
		}
//		else if(type==CrossActManager.MODY_ACT)
//		{
			sendStep();
//		}
		//System.out.println("------crosswarmanger--------type------:"+type);
//		else if(type==CrossActManager.MODY_ACT)
//		{
//			
//		}
//		else if(type==CrossActManager.OVER_ACT)
//		{
//
//		}
	}

	@Override
	public void change(Object source,int type,Object v1,Object v2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,Object v1,Object v2,Object v3)
	{
		// TODO Auto-generated method stub
		
	}

	
	public CrossWarSaveDBAccess getSaveDBAccess()
	{
		return saveDBAccess;
	}

	
	public void setSaveDBAccess(CrossWarSaveDBAccess saveDBAccess)
	{
		this.saveDBAccess=saveDBAccess;
	}

}
