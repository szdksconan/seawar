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
 * 跨服战信息
 * 
 * @author yw
 * 
 */
 
public class CrossWar extends CrossAct
{

	/* static fields */
	//空奖励
	public static int EMPTY_SID=65051;
	/**
	 * 赛事常量 S0:报名 S256:预赛 S64:决赛 64强 S32:决赛 32强 S16:决赛 16强 S8:决赛 8强 S4:决赛 4强
	 * S2:决赛 2强 S1比赛结束
	 */
	public static int S0=0,S256=256,S64=64,S32=32,S16=16,S8=8,S4=4,S2=2,
					S1=1;
	/**
	 * 赛事进度常量 T0 报名时段 T1预赛 T2可设置舰队 T3 决赛 64强 1/32 T4可设置舰队 T5决赛 32强 1/16
	 * T6可设置舰队 T7决赛 16强 1/8 T8可设置舰队 T9决赛 8强 1/4 T10可设置舰队 T11决赛 4强 1/2
	 * T12可设置舰队 T13 决赛2强 1/1 T14 赛后处理
	 */
	public static int T0=0,T1=1,T2=2,T3=3,T4=4,T5=5,T6=6,T7=7,T8=8,T9=9,
					T10=10,T11=11,T12=12,T13=13,T14=14;
	/** 可设置阶段 */
	public static int[] CANSET={T0,T2,T4,T6,T8,T10,T12};
	/** 决赛阶段 */
	public static int[] FINAL={T3,T5,T7,T9,T11,T13};
	/** 各时段 开始 中间天数 结束时刻 */
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

	// 押注量与 vip  bet,vip,bet,vip....
	int[] vipbet={100,0,300,0,500,0,1000,5,2000,7};
	/** 跨服战奖励 */
	String award;
	//奖励信息
	/** 押中赔率 */
	float odds=3.0f;
	/* fields */
	/** 双败淘汰 分组量 */
	int group=32;
	/** 前n晋级 */
	int fn=8;
	/** 积分分组量 */
	int ggroup=32;
	/** 积分 前n晋级 */
	int gfn=2;
	/** 限制等级 */
	int lvlimit=50;
	/** 当前进度 */
	int step;
	/** 是否动作完 当前进度 */
	boolean dostep;
	/** 当前赛事 */
	int match;
	/** 当前进度开始时刻 */
	int stepStime;
	/** 当前进度结束时刻 */
	int stepEtime;
	/** 截止报名日期 */
	String endDay;
	/** 冠军跨服id */
	int cmid;

	//临时变量
	/** 奖励表 */
	Award[] awards;
	/** 活动是否结束上次检测时间 */
	int checkTime=Integer.MAX_VALUE;
	/** 是否已发送 */
	boolean send=true;
	/** 是否是开启活动 */
	boolean open=false;
	/** 是否已强制发奖 */
	boolean forceAward=true;
	/** 是否可设置领奖状态  */
	boolean awardSate;
	/** 是否可设置预赛标记  */
	boolean preSate;
	/** 是否需要刷新奖励 */
	boolean flushAward;
	/** 是否需要刷前台奖励 */
	boolean showAward;
	/** 与跨服中心时间的差值 */
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
	 * 初始化
	 * 
	 * @param 报名结束日期
	 */
	public void init(String t0)
	{
		endDay=t0;
		// 活动开始-结束
		setStime(TimeKit.getSecondTime());
		setEtime(SeaBackKit.parseFormatTime(t0+" "+TB[TB.length-1][2])
			+getNeedDays()*PublicConst.DAY_SEC);

		// 阶段开始-结束
		setStepStime(TimeKit.getSecondTime());
		t0+=" "+TB[step][2];
		setStepEtime(SeaBackKit.parseFormatTime(t0)
			+TextKit.parseInt(TB[step][1])*PublicConst.DAY_SEC);

	}
	
	/** 刷新奖励 */
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

	/** 计算所需天数 （从报名截止第二天算起） */
	public int getNeedDays()
	{
		int days=0;
		for(int i=0;i<TB.length;i++)
		{
			days+=TextKit.parseInt(TB[i][1]);
		}
		return days;
	}
	/** 下一阶段 */
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
	 * 获取某赛段前台 显示时间
	 * 
	 * @param step 进度
	 * @param isstart 是否是开始时间
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
	/** 获取决赛 显示时间 */
	public int getShowFinalTime(boolean isstart)
	{
		if(step<=T1) return 0;
		int nstep=step;
		if(nstep>=T14) nstep=T14-1;
		return getShowStepTime(nstep,isstart);
	}

	/** 判断是否是领奖范围 */
	public boolean isAward()
	{
		return step==T14;
	}
	
//	/** 是否已发奖 */
//	public boolean isSendAward()
//	{
//		return award;
//	}
	/** 修改奖励 */
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
	
	/** 检测时间合法性 */
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

	/** 判断报名是否截止 */
	public boolean joinEnd()
	{
		if(step!=T0) return true;
		if(TimeKit.getSecondTime()>stepEtime) return true;
		return false;
	}
	/** 判断可否设置 */
	public boolean setCan()
	{
		if(SeaBackKit.isContainValue(CANSET,step)
			&&TimeKit.getSecondTime()<=stepEtime)
		{
			return true;
		}
		return false;
	}
	/** 判断可否押注 */
	public boolean betCan()
	{
		return step==T2&&getStepEtime()>TimeKit.getSecondTime();
	}

	/** 判断活动是否结束 */
	public boolean isover()
	{
		return (step==T14&&TimeKit.getSecondTime()>=getEtime())
			||isForceover();
	}
	/** 获取当前比赛类型 */
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
		// 与标准时间的差值(当地年月日时间在标准时区也出现所需的偏移量)
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
		collateTime(now,cnow);//修正时间
	}
	
	/**
	 *  修正时间参数
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
	/** 序列化给前台 */
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
		data.writeInt(getShowStepTime(CrossWar.T0,true));// 报名开始
		data.writeInt(getShowStepTime(CrossWar.T0,false));// 报名结束
		data.writeInt(getShowStepTime(CrossWar.T1,true));// 预赛开始时间
		data.writeInt(getShowStepTime(CrossWar.T1,false));// 预赛结束时间
		data.writeInt(getLvlimit());// 等级限制
		data.writeInt(getShowFinalTime(true));// 决赛开始时间
		data.writeInt(getShowFinalTime(false));// 决赛结束时间
		data.writeInt(getTransType());// 决赛类型
	}
	
	/** 写其他信息 */
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
	/** 读其他信息 */
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
	 * 将中心时刻转换成本地时刻的绝对时间
	 * 
	 * @param localTime 将中心时刻转换为绝对时间
	 */
	public int getLocalStandardTime(int centerTime)
	{
		// 与标准时间的差值(当地年月日时间在标准时区也出现所需的偏移量)
		int offset=TimeZone.getDefault().getRawOffset()/1000;
		// 与跨服中心时间的差值+本地时刻+夏令时
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
