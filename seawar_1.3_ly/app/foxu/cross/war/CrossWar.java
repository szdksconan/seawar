package foxu.cross.war;

import java.util.Calendar;
import java.util.TimeZone;

import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.cross.server.CrossAct;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.award.Award;
import foxu.sea.equipment.Equipment;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerManager;
import foxu.sea.proplist.Prop;

/**
 * ���ս��Ϣ
 * 
 * @author yw
 * 
 */
 
public class CrossWar extends CrossAct
{

	/* static fields */
	//�ս���
	public static int EMPTY_SID=65051;
	/**
	 * ���³��� S0:���� S256:Ԥ�� S64:���� 64ǿ S32:���� 32ǿ S16:���� 16ǿ S8:���� 8ǿ S4:���� 4ǿ
	 * S2:���� 2ǿ S1��������
	 */
	public static int S0=0,S256=256,S64=64,S32=32,S16=16,S8=8,S4=4,S2=2,
					S1=1;
	/**
	 * ���½��ȳ��� T0 ����ʱ�� T1Ԥ�� T2�����ý��� T3 ���� 64ǿ 1/32 T4�����ý��� T5���� 32ǿ 1/16
	 * T6�����ý��� T7���� 16ǿ 1/8 T8�����ý��� T9���� 8ǿ 1/4 T10�����ý��� T11���� 4ǿ 1/2
	 * T12�����ý��� T13 ����2ǿ 1/1 T14 ������
	 */
	public static int T0=0,T1=1,T2=2,T3=3,T4=4,T5=5,T6=6,T7=7,T8=8,T9=9,
					T10=10,T11=11,T12=12,T13=13,T14=14;
	/** �����ý׶� */
	public static int[] CANSET={T0,T2,T4,T6,T8,T10,T12};
	/** �����׶� */
	public static int[] FINAL={T3,T5,T7,T9,T11,T13};
	/** ��ʱ�� ��ʼ �м����� ����ʱ�� */
	 public static String[][] TB={{"now","0","11:30:00"},
	 {"12:00:00","0","22:00:00"},{"22:00:00","1","10:30:00"},
	 {"11:00:00","0","11:30:00"},{"11:30:00","0","14:30:00"},
	 {"15:00:00","0","15:30:00"},{"15:30:00","0","19:30:00"},
	 {"20:00:00","0","20:30:00"},{"20:30:00","1","10:30:00"},
	 {"11:00:00","0","11:30:00"},{"11:30:00","0","14:30:00"},
	 {"15:00:00","0","15:30:00"},{"15:30:00","0","19:30:00"},
	 {"20:00:00","0","20:30:00"},{"20:30:00","2","23:59:59"}};
	// test
//	public static String[][] TB={{"now","0","13:100:00"},
//		{"13:100:00","0","13:105:00"},{"14:15:00","0","14:20:00"},
//		{"14:25:00","0","14:30:00"},{"14:30:00","0","14:35:00"},
//		{"14:40:00","0","14:45:00"},{"14:45:00","0","14:50:00"},
//		{"14:55:00","0","14:60:00"},{"14:60:00","0","14:65:00"},
//		{"14:70:00","0","14:75:00"},{"14:75:00","0","14:80:00"},
//		{"14:85:00","0","14:90:00"},{"14:90:00","0","14:95:00"},
//		{"14:100:00","0","14:105:00"},{"14:105:00","2","23:59:59"}};

	// Ѻע���� vip  bet,vip,bet,vip....
	int[] vipbet={100,0,300,0,500,0,1000,5,2000,7};
	/** ���ս���� */
	String award;
	//������Ϣ
	/** Ѻ������ */
	float odds=3.0f;
	/* fields */
	/** ˫����̭ ������ */
	int group=32;
	/** ǰn���� */
	int fn=8;
	/** ���ַ����� */
	int ggroup=32;
	/** ���� ǰn���� */
	int gfn=2;
	/** ���Ƶȼ� */
	int lvlimit=50;
	/** ��ǰ���� */
	int step;
	/** �Ƿ����� ��ǰ���� */
	boolean dostep;
	/** ��ǰ���� */
	int match;
	/** ��ǰ���ȿ�ʼʱ�� */
	int stepStime;
	/** ��ǰ���Ƚ���ʱ�� */
	int stepEtime;
	/** ��ֹ�������� */
	String endDay;
	/** �ھ����id */
	int cmid;

	//��ʱ����
	/** ������ */
	Award[] awards;
	/** ��Ƿ�����ϴμ��ʱ�� */
	int checkTime=Integer.MAX_VALUE;
	/** �Ƿ��ѷ��� */
	boolean send=true;
	/** �Ƿ��ǿ���� */
	boolean open=false;
	/** �Ƿ���ǿ�Ʒ��� */
	boolean forceAward=true;
	/** �Ƿ�������콱״̬  */
	boolean awardSate;
	/** �Ƿ������Ԥ�����  */
	boolean preSate;
	/** �Ƿ���Ҫˢ�½��� */
	boolean flushAward;
	/** �Ƿ���Ҫˢǰ̨���� */
	boolean showAward;
	/** ��������ʱ��Ĳ�ֵ */
	int toCenterOffset;

	/* methods */
	public CrossWar()
	{
		setSid(CrossAct.WAR_SID);
	}
	//test
	public void creatSteps()
	{
		String[][] steps=new String[15][3];
		String date=SeaBackKit.formatDataTime(TimeKit.getSecondTime());
		String day=TextKit.split(date," ")[0];
		date=TextKit.split(date," ")[1];
		String[] strs=TextKit.split(date,":");
		int hour=TextKit.parseInt(strs[0]);
		int min=TextKit.parseInt(strs[1]);
		int second=TextKit.parseInt(strs[2]);
		for(int i=0;i<steps.length;i++)
		{
			if(i%2==1)
			{
				second+=10;
			}
			int rsecond=second%60;
			int rmin=min+second/60;
			steps[i][0]=hour+":"+rmin+":"+rsecond;
			steps[i][1]="0";
			second+=10;
			if(i==0||i==2)
			{
				second+=30;
			}
			if(i==14)
			{
				second+=1200;
			}
			rsecond=second%60;
			rmin=min+second/60;
			steps[i][2]=hour+":"+rmin+":"+rsecond;
		}
		TB=steps;
		init(day);
	}

	/**
	 * ��ʼ��
	 * 
	 * @param ������������
	 */
	public void init(String t0)
	{
		endDay=t0;
		// ���ʼ-����
		setStime(TimeKit.getSecondTime());
		setEtime(SeaBackKit.parseFormatTime(t0+" "+TB[TB.length-1][2])
			+getNeedDays()*PublicConst.DAY_SEC);

		// �׶ο�ʼ-����
		setStepStime(TimeKit.getSecondTime());
		t0+=" "+TB[step][2];
		setStepEtime(SeaBackKit.parseFormatTime(t0)
			+TextKit.parseInt(TB[step][1])*PublicConst.DAY_SEC);

	}
	
	/** ˢ�½��� */
	public void updateAward()
	{
		if(award==null||!isFlushAward()) return;
		ArrayList list=new ArrayList();
		String[] ads=TextKit.split(award,"|");
		for(int i=0;i<ads.length;i++)
		{
			Award ad=(Award)Award.factory.newSample(EMPTY_SID);
			int[] sids=TextKit.parseIntArray(TextKit.split(ads[i],","));
			ad.setGemsAward(sids[0]);
			ArrayList proplist=new ArrayList();
			ArrayList shiplist=new ArrayList();
			ArrayList equiplist=new ArrayList();
			ArrayList officerList=new ArrayList();
			for(int k=1;k<sids.length;k+=2)
			{
				int propType=SeaBackKit.getSidType(sids[k]);
				if(propType==Prop.PROP)
				{
					proplist.add(sids[k]);
					proplist.add(sids[k+1]);
				}
				else if(propType==Prop.SHIP)
				{
					shiplist.add(sids[k]);
					shiplist.add(sids[k+1]);
				}
				else if(propType==Prop.EQUIP)
				{
					equiplist.add(sids[k]);
					equiplist.add(sids[k+1]);
				}
				else if(propType==Prop.OFFICER)
				{
					officerList.add(sids[k]);
					officerList.add(sids[k+1]);
				}
			}
			if(proplist.size()>0)
			{
				int[] props=new int[proplist.size()];
				setInts(props,proplist);
				ad.setPropSid(props);
			}
			if(shiplist.size()>0)
			{
				int[] ships=new int[shiplist.size()];
				setInts(ships,shiplist);
				ad.setShipSids(ships);
			}
			if(equiplist.size()>0)
			{
				int[] equips=new int[equiplist.size()];
				setInts(equips,equiplist);
				ad.setEquipSids(equips);
			}
			if(officerList.size()>0)
			{
				int[] officers=new int[officerList.size()];
				setInts(officers,officerList);
				ad.setOfficerSids(officers);
			}
			list.add(ad);
		}
		awards=new Award[list.size()];
		for(int i=0;i<awards.length;i++)
		{
			awards[i]=(Award)list.get(i);
		}
		setFlushAward(false);
		setShowAward(true);
	}
	
	public void setInts(int[] props,ArrayList list)
	{
		if(props==null)return;
		if(list.size()<=0)return;
		for(int i=0;i<props.length;i++)
		{
			props[i]=(Integer)list.get(i);
		}
	}

	public int getCanMatch()
	{
		if(match==S0||match==S256) return 0;
		return match;
	}

	/** ������������ ���ӱ�����ֹ�ڶ������� */
	public int getNeedDays()
	{
		int days=0;
		for(int i=0;i<TB.length;i++)
		{
			days+=TextKit.parseInt(TB[i][1]);
		}
		return days;
	}
	/** ��һ�׶� */
	public void nextStep()
	{
		step++;
		String day=SeaBackKit.formatDataTime(TimeKit.getSecondTime());
		day=TextKit.split(day," ")[0];
		setStepStime(SeaBackKit.parseFormatTime(day+" "+TB[step][0]));
		setStepEtime(SeaBackKit.parseFormatTime(day+" "+TB[step][2])
			+TextKit.parseInt(TB[step][1])*PublicConst.DAY_SEC);
		if(step==T3||step==T2)
			setMatch(S64);
		else if(step==T5||step==T4)
			setMatch(S32);
		else if(step==T7||step==T6)
			setMatch(S16);
		else if(step==T9||step==T8)
			setMatch(S8);
		else if(step==T11||step==T10)
			setMatch(S4);
		else if(step==T13||step==T12) setMatch(S2);
		dostep=false;
	}

	/**
	 * ��ȡĳ����ǰ̨ ��ʾʱ��
	 * 
	 * @param step ����
	 * @param isstart �Ƿ��ǿ�ʼʱ��
	 * @return
	 */
	public int getShowStepTime(int step,boolean isstart)
	{
		if(step==0&&isstart) return getStime();
		if(step!=0&&step%2==0) step++;
		if(step>=TB.length) step=TB.length-1;
		if(isstart) step--;
		int days=0;
		for(int i=0;i<=step;i++)
		{
			days+=TextKit.parseInt(TB[i][1]);
		}
		return getLocalStandardTime(SeaBackKit.parseFormatTime(endDay+" "+TB[step][2])+days
			*PublicConst.DAY_SEC);
	}
	/** ��ȡ���� ��ʾʱ�� */
	public int getShowFinalTime(boolean isstart)
	{
		if(step<=T1) return 0;
		int nstep=step;
		if(nstep>=T14) nstep=T14-1;
		return getShowStepTime(nstep,isstart);
	}

	/** �ж��Ƿ����콱��Χ */
	public boolean isAward()
	{
		return step==T14;
	}
	
//	/** �Ƿ��ѷ��� */
//	public boolean isSendAward()
//	{
//		return award;
//	}
	/** �޸Ľ��� */
	public boolean setAward(String award)
	{
		try
		{
			if(award==null) return false;
			String[] ads=TextKit.split(award,"|");
			if(ads.length!=8) return false;
			for(int i=0;i<ads.length;i++)
			{
				int[] sids=TextKit.parseIntArray(TextKit.split(ads[i],","));
				if(sids.length%2!=1) return false;
				if(sids[0]<0) return false;
				for(int k=1;k<sids.length;k+=2)
				{
					if(!checkSid(sids[k],sids[k+1])) return false;
				}
			}
		}
		catch(Exception e)
		{
			return false;
		}
		this.award=award;
		return true;
	}
	
	/** ���ʱ��Ϸ��� */
	public boolean checkDate(String date)
	{
		int dtime=SeaBackKit.parseFormatTime(date+" "+TB[0][2]);
		return TimeKit.getSecondTime()<dtime;
	}
	
	public boolean checkSid(int sid,int num)
	{
		if(num<=0) return false;
		Object obj=Prop.factory.getSample(sid);
		if(obj==null) obj=Ship.factory.getSample(sid);
		if(obj==null) obj=Equipment.factory.getSample(sid);
		if(obj==null)
		{
			for(int i=0;i<Equipment.QUALITY_STUFFS.length;i+=3)
			{
				if(sid==Equipment.QUALITY_STUFFS[i]) return true;
			}
		}
		if(obj==null) obj=OfficerManager.factory.getSample(sid);
		if(obj!=null) return true;
		return false;
	}

	/** �жϱ����Ƿ��ֹ */
	public boolean joinEnd()
	{
		if(step!=T0) return true;
		if(TimeKit.getSecondTime()>stepEtime) return true;
		return false;
	}
	/** �жϿɷ����� */
	public boolean setCan()
	{
		if(SeaBackKit.isContainValue(CANSET,step)
			&&TimeKit.getSecondTime()<=stepEtime)
		{
			return true;
		}
		return false;
	}
	/** �жϿɷ�Ѻע */
	public boolean betCan()
	{
		return step==T2&&getStepEtime()>TimeKit.getSecondTime();
	}

	/** �жϻ�Ƿ���� */
	public boolean isover()
	{
		return (step==T14&&TimeKit.getSecondTime()>=getEtime())
			||isForceover();
	}
	/** ��ȡ��ǰ�������� */
	public int getTransType()
	{
		int type=0;
		if(match==0)
		{
			type=0;
		}
		else if(match==64)
		{
			type=1;
		}
		else if(match==32)
		{
			type=2;
		}
		else if(match==16)
		{
			type=3;
		}
		else if(match==8)
		{
			type=4;
		}
		else if(match==4)
		{
			type=5;
		}
		else if(match==2)
		{
			type=6;
		}
		return type;
	}

	public void showBytesWrite(ByteBuffer data)
	{
		super.showBytesWrite(data);
		// ���׼ʱ��Ĳ�ֵ(����������ʱ���ڱ�׼ʱ��Ҳ���������ƫ����)
		data.writeInt(TimeZone.getDefault().getRawOffset()/1000);
		data.writeInt(TimeKit.getSecondTime());
		data.writeShort(lvlimit);
		data.writeByte(step);
		data.writeShort(match);
		data.writeInt(stepStime);
		data.writeInt(stepEtime);
		data.writeUTF(endDay);
		data.writeInt(cmid);
		data.writeUTF(award);
		data.writeBoolean(isForceover());
		data.writeShort(TB.length);
		for(int i=0;i<TB.length;i++)
		{
			data.writeUTF(TB[i][0]);
			data.writeUTF(TB[i][1]);
			data.writeUTF(TB[i][2]);
		}
	}
	
	public void showBytesRead(ByteBuffer data)
	{
		super.showBytesRead(data);
		toCenterOffset=data.readInt();
		int now=TimeKit.getSecondTime();
		int cnow=data.readInt();
		lvlimit=data.readUnsignedShort();
		step=data.readUnsignedByte();
		match=data.readUnsignedShort();
		stepStime=data.readInt();
		stepEtime=data.readInt();
		endDay=data.readUTF();
		cmid=data.readInt();
		String now_award=data.readUTF();
		setForceover(data.readBoolean());
		
		int len=data.readUnsignedShort();
		TB=new String[len][3];
		for(int i=0;i<len;i++)
		{
			TB[i][0]=data.readUTF();
			TB[i][1]=data.readUTF();
			TB[i][2]=data.readUTF();
		}
		
		if(now_award!=null&&(award==null||!now_award.equals(award)))
		{
			flushAward=true;
		}
		award=now_award;

		send=false;
		if(step==CrossWar.T2)
		{
			setPreSate(true);
		}
		if(cmid>0)
		{
			setForceAward(false);
			setAwardSate(true);
		}
		collateTime(now,cnow);//����ʱ��
	}
	
	/**
	 *  ����ʱ�����
	 */
	public void collateTime(int now,int cnow)
	{
		int dtime=now-cnow;
		stepStime+=dtime;
		stepEtime+=dtime;
		for(int i=0;i<TB.length;i++)
		{
			// {"now","0","11:30:00"}
			String tday=TB[i][2];
			String[] tdays=TextKit.split(tday,":");
			int second=TextKit.parseInt(tdays[2]);
			second+=dtime;
			TB[i][2]=tdays[0]+":"+tdays[1]+":"+second;

		}

	}
	/** ���л���ǰ̨ */
	public void showClientBytesWrite(ByteBuffer data)
	{
		data.writeBoolean(true);
		data.writeInt(getStime());
		if(isForceover())
		{
			data.writeInt(0);
		}
		else
		{
			data.writeInt(getEtime());
		}
		data.writeInt(getShowStepTime(CrossWar.T0,true));// ������ʼ
		data.writeInt(getShowStepTime(CrossWar.T0,false));// ��������
		data.writeInt(getShowStepTime(CrossWar.T1,true));// Ԥ����ʼʱ��
		data.writeInt(getShowStepTime(CrossWar.T1,false));// Ԥ������ʱ��
		data.writeInt(getLvlimit());// �ȼ�����
		data.writeInt(getShowFinalTime(true));// ������ʼʱ��
		data.writeInt(getShowFinalTime(false));// ��������ʱ��
		data.writeInt(getTransType());// ��������
	}
	
	/** д������Ϣ */
	@Override
	public void writeData(ByteBuffer data)
	{
		super.writeData(data);
		data.writeByte(step);
		data.writeBoolean(dostep);
		data.writeShort(match);
		data.writeInt(cmid);
		data.writeInt(stepStime);
		data.writeInt(stepEtime);
		data.writeUTF(endDay);
		data.writeUTF(award);
		data.writeBoolean(isForceover());
		data.writeShort(TB.length);
		for(int i=0;i<TB.length;i++)
		{
			data.writeUTF(TB[i][0]);
			data.writeUTF(TB[i][1]);
			data.writeUTF(TB[i][2]);
		}
	}
	/** ��������Ϣ */
	@Override
	public void readData(ByteBuffer data)
	{
		super.readData(data);
		step=data.readUnsignedByte();
		dostep=data.readBoolean();
		match=data.readUnsignedShort();
		cmid=data.readInt();
		stepStime=data.readInt();
		stepEtime=data.readInt();
		endDay=data.readUTF();
		award=data.readUTF();
		setForceover(data.readBoolean());
		int len=data.readUnsignedShort();
		TB=new String[len][3];
		for(int i=0;i<TB.length;i++)
		{
			TB[i][0]=data.readUTF();
			TB[i][1]=data.readUTF();
			TB[i][2]=data.readUTF();
		}
	}

	/**
	 * ������ʱ��ת���ɱ���ʱ�̵ľ���ʱ��
	 * 
	 * @param localTime ������ʱ��ת��Ϊ����ʱ��
	 */
	public int getLocalStandardTime(int centerTime)
	{
		// ���׼ʱ��Ĳ�ֵ(����������ʱ���ڱ�׼ʱ��Ҳ���������ƫ����)
		int offset=TimeZone.getDefault().getRawOffset()/1000;
		// ��������ʱ��Ĳ�ֵ+����ʱ��+����ʱ
		return centerTime+offset-toCenterOffset
			+Calendar.getInstance().get(Calendar.DST_OFFSET)/1000;
	}
	
	public int getStep()
	{
		return step;
	}

	public void setStep(int step)
	{
		this.step=step;
	}

	public int getStepStime()
	{
		return stepStime;
	}

	public void setStepStime(int stepStime)
	{
		this.stepStime=stepStime;
	}

	public int getMatch()
	{
		return match;
	}

	public void setMatch(int match)
	{
		this.match=match;
	}

	public int getStepEtime()
	{
		return stepEtime;
	}

	public void setStepEtime(int stepEtime)
	{
		this.stepEtime=stepEtime;
	}
	public int getGroup()
	{
		return group;
	}

	public void setGroup(int group)
	{
		this.group=group;
	}

	public int getFn()
	{
		return fn;
	}

	public void setFn(int fn)
	{
		this.fn=fn;
	}

	public int getGgroup()
	{
		return ggroup;
	}

	public void setGgroup(int ggroup)
	{
		this.ggroup=ggroup;
	}

	public int getGfn()
	{
		return gfn;
	}

	public void setGfn(int gfn)
	{
		this.gfn=gfn;
	}

	public boolean isDostep()
	{
		return dostep;
	}

	public void setDostep(boolean dostep)
	{
		this.dostep=dostep;
	}

	public int getLvlimit()
	{
		return lvlimit;
	}

	public void setLvlimit(int lvlimit)
	{
		this.lvlimit=lvlimit;
	}

	public boolean isSend()
	{
		return send;
	}

	public void setSend(boolean send)
	{
		this.send=send;
	}

	public int[] getVipbet()
	{
		return vipbet;
	}

	public void setVipbet(int[] vipbet)
	{
		this.vipbet=vipbet;
	}

	public float getOdds()
	{
		return odds;
	}

	public void setOdds(float odds)
	{
		this.odds=odds;
	}

	public int getCmid()
	{
		return cmid;
	}

	public void setCmid(int cmid)
	{
		this.cmid=cmid;
	}
	
	public boolean isOpen()
	{
		return open;
	}
	
	public void setOpen(boolean open)
	{
		this.open=open;
	}
	
	public String getAward()
	{
		return award;
	}
	public boolean isAwardSate()
	{
		return awardSate;
	}
	
	public void setAwardSate(boolean awardSate)
	{
		this.awardSate=awardSate;
	}
	
	public boolean isPreSate()
	{
		return preSate;
	}
	
	public void setPreSate(boolean preSate)
	{
		this.preSate=preSate;
	}
	
	public boolean isForceAward()
	{
		return forceAward;
	}
	
	public void setForceAward(boolean forceAward)
	{
		this.forceAward=forceAward;
	}
	
	public int getCheckTime()
	{
		return checkTime;
	}
	
	public void setCheckTime(int checkTime)
	{
		this.checkTime=checkTime;
	}
	
	public boolean isFlushAward()
	{
		return flushAward;
	}
	
	public void setFlushAward(boolean flushAward)
	{
		this.flushAward=flushAward;
	}
	
	public boolean isShowAward()
	{
		return showAward;
	}
	
	public void setShowAward(boolean showAward)
	{
		this.showAward=showAward;
	}
	public Award[] getAwards()
	{
		updateAward();
		return awards;
	}
	
	public void setAwards(Award[] awards)
	{
		this.awards=awards;
	}
	public JSONObject toJson()
	{
		JSONObject jsn=super.toJson();
		try
		{
			jsn.put("step",step);
			jsn.put("cmid",cmid);
			jsn.put("over",isover());
			jsn.put("award",award);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jsn;

	}
	
}
