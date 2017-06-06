package foxu.sea;

import mustang.io.ByteBuffer;


/** 游戏运营数据统计 */
public class GameData
{
	/** 自增ID */
	int id;
	/** 第几年 */
	int the_year;
	/** 第几个月 */
	int the_month;
	/** 第几天 */
	int the_day;
	
	/** 今日新增user */
	int new_user;
	/**今日新增udid*/
	int new_udid;
	/** dau */
	int dau;
	/** 充值额度  RMB */
	int charge_amount;
	/** 充值人数 */
	int charge_people;
	
	/**今天最高在线*/
	int max_online;
	
	//新增
	/** 付费数据 */
	int arpu1;
	int arpu3;
	int arpu7;
	int arpu14;
	int arpu30;
	int arpu60;
	
	/** 隔日重登率 */
	int last_day_rate;
	/** 3日重登率 */
	int three_day_rate;//新增加
	/** 周重登率 */
	int week_rate;
	/** 双周重登率 */
	int doublu_week_rate;
	/** 月重登率 */
	int month_rate;
	/** 双月重登率 */
	int double_month_rate;//新增加
	
	
	/** 用户总数 */
	int total_user;
	/** 充值用户总数 */
	int charge_total_user;
	/** 总充值率 扩大10000倍 4个有效数字 */
	int charge_rate;
	/** 累计充值额度RMB*/
	int total_charge;
	
	/**mau*/
	int mau;
	/**当前在线  */
	int online;
	/** 累计登陆 */
	int loginCount;
	/** 平均在线时长 */
	int onlineTime;
	
	//已有属性 计算获取
	/** arpdau 扩大10000倍 4个有效数字*/
	int arpu;
	/** arppdau 扩大10000倍 4个有效数字*/
	int arppu;
	/** 全用户arpu值 */
	float total_arpu;
	/** 充值用户ARPPU */
	float total_arppu;
	/** 累计存留  */
	int total_rate;//新增
	/** 活跃付费率 */
	int dau_charge_rate;//新增
	
	//临时变量 比例数据的分子与分母
	int arpu1_m;
	int arpu1_d;
	int arpu3_m;
	int arpu3_d;
	int arpu7_m;
	int arpu7_d;
	int arpu14_m;
	int arpu14_d;
	int arpu30_m;
	int arpu30_d;
	int arpu60_m;
	int arpu60_d;
	
	int rate1_m;
	int rate1_d;
	int rate3_m;
	int rate3_d;
	int rate7_m;
	int rate7_d;
	int rate14_m;
	int rate14_d;
	int rate30_m;
	int rate30_d;
	int rate60_m;
	int rate60_d;
	
	/** 平台 */
	String plat;
	
	public Object bytesRead(ByteBuffer data)
	{
		id=data.readInt();
		new_user=data.readInt();
		new_udid=data.readInt();
		dau=data.readInt();
		mau=data.readInt();
		max_online = data.readUnsignedShort();
		charge_amount=data.readInt();
		charge_people=data.readInt();
		arpu1=data.readUnsignedShort();
		arpu3=data.readUnsignedShort();
		arpu7=data.readUnsignedShort();
		arpu14=data.readUnsignedShort();
		arpu30=data.readUnsignedShort();
		arpu60=data.readUnsignedShort();
		last_day_rate=data.readUnsignedByte();
		three_day_rate=data.readUnsignedByte();
		week_rate=data.readUnsignedByte();
		doublu_week_rate=data.readUnsignedByte();
		month_rate=data.readUnsignedByte();
		double_month_rate=data.readUnsignedByte();
		arpu=data.readUnsignedShort();
		arppu=data.readUnsignedShort();
		total_user=data.readInt();
		charge_total_user=data.readInt();
		charge_rate=data.readUnsignedByte();
		total_charge=data.readInt();
		the_year=data.readUnsignedShort();
		the_month=data.readUnsignedByte();
		the_day=data.readUnsignedByte();
		plat=data.readUTF();
		loginCount=data.readInt();
		onlineTime=data.readInt();
		return this;
	}
	
	public Object bytesReadFromCenter(ByteBuffer data)
	{
		the_year=data.readUnsignedShort();
		the_month=data.readUnsignedByte();
		the_day=data.readUnsignedByte();
		
		new_user=data.readInt();
		new_udid=data.readInt();
		dau=data.readInt();
		charge_amount=data.readInt();
		charge_people=data.readInt();
		mau=data.readInt();
		max_online=data.readInt();
		
		last_day_rate=data.readInt();
		three_day_rate=data.readInt();
		week_rate=data.readInt();
		doublu_week_rate=data.readInt();
		month_rate=data.readInt();
		double_month_rate=data.readInt();
		
		arpu=data.readInt();
		arppu=data.readInt();
		
		total_user=data.readInt();
		charge_total_user=data.readInt();
		charge_rate=data.readInt();
		total_charge=data.readInt();
		
		total_arpu=data.readFloat();
		total_arppu=data.readFloat();
		
		arpu1=data.readInt();
		arpu3=data.readInt();
		arpu7=data.readInt();
		arpu14=data.readInt();
		arpu30=data.readInt();
		arpu60=data.readInt();
		
		dau_charge_rate=data.readInt();
		total_rate=data.readInt();
		plat=data.readUTF();
		if(plat.equals(""))plat=null;
		loginCount=data.readInt();
		onlineTime=data.readInt();
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(new_user);
		data.writeInt(new_udid);
		data.writeInt(dau);
		data.writeInt(mau);
		data.writeShort(max_online);
		data.writeInt(charge_amount);
		data.writeInt(charge_people);
		data.writeShort(arpu1);
		data.writeShort(arpu3);
		data.writeShort(arpu7);
		data.writeShort(arpu14);
		data.writeShort(arpu30);
		data.writeShort(arpu60);
		data.writeByte(last_day_rate);
		data.writeByte(three_day_rate);
		data.writeByte(week_rate);
		data.writeByte(doublu_week_rate);
		data.writeByte(month_rate);
		data.writeByte(double_month_rate);
		data.writeShort(arpu);
		data.writeShort(arppu);
		data.writeInt(total_user);
		data.writeInt(charge_total_user);
		data.writeByte(charge_rate);
		data.writeInt(total_charge);
		data.writeShort(the_year);
		data.writeByte(the_month);
		data.writeByte(the_day);
		data.writeUTF(plat);
		data.writeInt(loginCount);
		data.writeInt(onlineTime);
	}
	
	/** 将对象的域序列化到字节缓存中 */
	public void bytesWriteToCenter(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(new_user);
		data.writeInt(new_udid);
		data.writeInt(dau);
		data.writeInt(mau);
		data.writeShort(max_online);
		data.writeInt(charge_amount);
		data.writeInt(charge_people);
		data.writeInt(arpu1_m);
		data.writeInt(arpu1_d);
		data.writeInt(arpu3_m);
		data.writeInt(arpu3_d);
		data.writeInt(arpu7_m);
		data.writeInt(arpu7_d);
		data.writeInt(arpu14_m);
		data.writeInt(arpu14_d);
		data.writeInt(arpu30_m);
		data.writeInt(arpu30_d);
		data.writeInt(arpu60_m);
		data.writeInt(arpu60_d);
		data.writeInt(rate1_m);
		data.writeInt(rate1_d);
		data.writeInt(rate3_m);
		data.writeInt(rate3_d);
		data.writeInt(rate7_m);
		data.writeInt(rate7_d);
		data.writeInt(rate14_m);
		data.writeInt(rate14_d);
		data.writeInt(rate30_m);
		data.writeInt(rate30_d);
		data.writeInt(rate60_m);
		data.writeInt(rate60_d);
		data.writeInt(total_user);
		data.writeInt(charge_total_user);
		data.writeInt(total_charge);
		data.writeShort(the_year);
		data.writeByte(the_month);
		data.writeByte(the_day);
		data.writeUTF(plat);
		data.writeInt(loginCount);
		data.writeInt(onlineTime);
	}

	/**
	 * @return arpu
	 */
	public int getArpu()
	{
		return arpu;
	}

	/**
	 * @param arpu 要设置的 arpu
	 */
	public void setArpu(int arpu)
	{
		this.arpu=arpu;
	}

	/**
	 * @return charge_amount
	 */
	public int getCharge_amount()
	{
		return charge_amount;
	}

	/**
	 * @param charge_amount 要设置的 charge_amount
	 */
	public void setCharge_amount(int charge_amount)
	{
		this.charge_amount=charge_amount;
	}

	/**
	 * @return charge_people
	 */
	public int getCharge_people()
	{
		return charge_people;
	}

	/**
	 * @param charge_people 要设置的 charge_people
	 */
	public void setCharge_people(int charge_people)
	{
		this.charge_people=charge_people;
	}

	/**
	 * @return charge_rate
	 */
	public int getCharge_rate()
	{
		return charge_rate;
	}

	/**
	 * @param charge_rate 要设置的 charge_rate
	 */
	public void setCharge_rate(int charge_rate)
	{
		this.charge_rate=charge_rate;
	}

	/**
	 * @return charge_total_user
	 */
	public int getCharge_total_user()
	{
		return charge_total_user;
	}

	/**
	 * @param charge_total_user 要设置的 charge_total_user
	 */
	public void setCharge_total_user(int charge_total_user)
	{
		this.charge_total_user=charge_total_user;
	}

	/**
	 * @return dau
	 */
	public int getDau()
	{
		return dau;
	}

	/**
	 * @param dau 要设置的 dau
	 */
	public void setDau(int dau)
	{
		this.dau=dau;
	}

	/**
	 * @return doublu_week_rate
	 */
	public int getDoublu_week_rate()
	{
		return doublu_week_rate;
	}

	/**
	 * @param doublu_week_rate 要设置的 doublu_week_rate
	 */
	public void setDoublu_week_rate(int doublu_week_rate)
	{
		this.doublu_week_rate=doublu_week_rate;
	}

	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id 要设置的 id
	 */
	public void setId(int id)
	{
		this.id=id;
	}

	/**
	 * @return last_day_rate
	 */
	public int getLast_day_rate()
	{
		return last_day_rate;
	}

	/**
	 * @param last_day_rate 要设置的 last_day_rate
	 */
	public void setLast_day_rate(int last_day_rate)
	{
		this.last_day_rate=last_day_rate;
	}

	/**
	 * @return month_rate
	 */
	public int getMonth_rate()
	{
		return month_rate;
	}

	/**
	 * @param month_rate 要设置的 month_rate
	 */
	public void setMonth_rate(int month_rate)
	{
		this.month_rate=month_rate;
	}

	/**
	 * @return new_user
	 */
	public int getNew_user()
	{
		return new_user;
	}

	/**
	 * @param new_user 要设置的 new_user
	 */
	public void setNew_user(int new_user)
	{
		this.new_user=new_user;
	}

	/**
	 * @return total_charge
	 */
	public int getTotal_charge()
	{
		return total_charge;
	}

	/**
	 * @param total_charge 要设置的 total_charge
	 */
	public void setTotal_charge(int total_charge)
	{
		this.total_charge=total_charge;
	}

	/**
	 * @return total_user
	 */
	public int getTotal_user()
	{
		return total_user;
	}

	/**
	 * @param total_user 要设置的 total_user
	 */
	public void setTotal_user(int total_user)
	{
		this.total_user=total_user;
	}

	/**
	 * @return week_rate
	 */
	public int getWeek_rate()
	{
		return week_rate;
	}

	/**
	 * @param week_rate 要设置的 week_rate
	 */
	public void setWeek_rate(int week_rate)
	{
		this.week_rate=week_rate;
	}

	/**
	 * @return arppu
	 */
	public int getArppu()
	{
		return arppu;
	}

	/**
	 * @param arppu 要设置的 arppu
	 */
	public void setArppu(int arppu)
	{
		this.arppu=arppu;
	}

	/**
	 * @return the_day
	 */
	public int getThe_day()
	{
		return the_day;
	}

	/**
	 * @param the_day 要设置的 the_day
	 */
	public void setThe_day(int the_day)
	{
		this.the_day=the_day;
	}

	/**
	 * @return the_year
	 */
	public int getThe_year()
	{
		return the_year;
	}

	/**
	 * @param the_year 要设置的 the_year
	 */
	public void setThe_year(int the_year)
	{
		this.the_year=the_year;
	}

	/**
	 * @return the_month
	 */
	public int getThe_month()
	{
		return the_month;
	}

	/**
	 * @param the_month 要设置的 the_month
	 */
	public void setThe_month(int the_month)
	{
		this.the_month=the_month;
	}

//	/**
//	 * @return loss_rate_day
//	 */
//	public float getLoss_rate_day()
//	{
//		return loss_rate_day;
//	}
//
//	/**
//	 * @param loss_rate_day 要设置的 loss_rate_day
//	 */
//	public void setLoss_rate_day(float loss_rate_day)
//	{
//		this.loss_rate_day=loss_rate_day;
//	}
//
//	/**
//	 * @return loss_rate_week
//	 */
//	public int getLoss_rate_week()
//	{
//		return loss_rate_week;
//	}
//
//	/**
//	 * @param loss_rate_week 要设置的 loss_rate_week
//	 */
//	public void setLoss_rate_week(int loss_rate_week)
//	{
//		this.loss_rate_week=loss_rate_week;
//	}

	/**
	 * @return total_arppu
	 */
	public float getTotal_arppu()
	{
		return total_arppu;
	}

	/**
	 * @param total_arppu 要设置的 total_arppu
	 */
	public void setTotal_arppu(float total_arppu)
	{
		this.total_arppu=total_arppu;
	}

	/**
	 * @return total_arpu
	 */
	public float getTotal_arpu()
	{
		return total_arpu;
	}

	/**
	 * @param total_arpu 要设置的 total_arpu
	 */
	public void setTotal_arpu(float total_arpu)
	{
		this.total_arpu=total_arpu;
	}

	
	/**
	 * @return mau
	 */
	public int getMau()
	{
		return mau;
	}

	
	/**
	 * @param mau 要设置的 mau
	 */
	public void setMau(int mau)
	{
		this.mau=mau;
	}

	
	/**
	 * @return maxOnline
	 */
	public int getMaxOnline()
	{
		return max_online;
	}

	
	/**
	 * @param maxOnline 要设置的 maxOnline
	 */
	public void setMaxOnline(int maxOnline)
	{
		this.max_online=maxOnline;
	}

	
	/**
	 * @return online
	 */
	public int getOnline()
	{
		return online;
	}

	
	/**
	 * @param online 要设置的 online
	 */
	public void setOnline(int online)
	{
		this.online=online;
	}

	
//	public int getNew_inviter()
//	{
//		return new_inviter;
//	}
//
//	
//	public void setNew_inviter(int new_inviter)
//	{
//		this.new_inviter=new_inviter;
//	}

	
	public int getNew_udid()
	{
		return new_udid;
	}

	
	public void setNew_udid(int new_udid)
	{
		this.new_udid=new_udid;
	}

	
	public int getMax_online()
	{
		return max_online;
	}

	
	public void setMax_online(int max_online)
	{
		this.max_online=max_online;
	}

	
	public int getArpu1()
	{
		return arpu1;
	}

	
	public void setArpu1(int arpu1)
	{
		this.arpu1=arpu1;
	}
	
	
	public int getArpu3()
	{
		return arpu3;
	}

	
	public void setArpu3(int arpu3)
	{
		this.arpu3=arpu3;
	}

	public int getArpu7()
	{
		return arpu7;
	}

	
	public void setArpu7(int arpu7)
	{
		this.arpu7=arpu7;
	}

	
	public int getArpu14()
	{
		return arpu14;
	}

	
	public void setArpu14(int arpu14)
	{
		this.arpu14=arpu14;
	}

	
	public int getArpu30()
	{
		return arpu30;
	}

	
	public void setArpu30(int arpu30)
	{
		this.arpu30=arpu30;
	}

	
	public int getArpu60()
	{
		return arpu60;
	}

	
	public void setArpu60(int arpu60)
	{
		this.arpu60=arpu60;
	}

	
	public int getThree_day_rate()
	{
		return three_day_rate;
	}

	
	public void setThree_day_rate(int three_day_rate)
	{
		this.three_day_rate=three_day_rate;
	}

	
	public int getDouble_month_rate()
	{
		return double_month_rate;
	}

	
	public void setDouble_month_rate(int double_month_rate)
	{
		this.double_month_rate=double_month_rate;
	}

	
	public int getTotal_rate()
	{
		return total_rate;
	}

	
	public void setTotal_rate(int total_rate)
	{
		this.total_rate=total_rate;
	}

	
	public int getDau_charge_rate()
	{
		return dau_charge_rate;
	}

	
	public void setDau_charge_rate(int dau_charge_rate)
	{
		this.dau_charge_rate=dau_charge_rate;
	}

	
	public int getArpu1_m()
	{
		return arpu1_m;
	}

	
	public void setArpu1_m(int arpu1_m)
	{
		this.arpu1_m=arpu1_m;
	}

	
	public int getArpu1_d()
	{
		return arpu1_d;
	}

	
	public void setArpu1_d(int arpu1_d)
	{
		this.arpu1_d=arpu1_d;
	}

	
	public int getArpu3_m()
	{
		return arpu3_m;
	}

	
	public void setArpu3_m(int arpu3_m)
	{
		this.arpu3_m=arpu3_m;
	}

	
	public int getArpu3_d()
	{
		return arpu3_d;
	}

	
	public void setArpu3_d(int arpu3_d)
	{
		this.arpu3_d=arpu3_d;
	}

	
	public int getArpu7_m()
	{
		return arpu7_m;
	}

	
	public void setArpu7_m(int arpu7_m)
	{
		this.arpu7_m=arpu7_m;
	}

	
	public int getArpu7_d()
	{
		return arpu7_d;
	}

	
	public void setArpu7_d(int arpu7_d)
	{
		this.arpu7_d=arpu7_d;
	}

	
	public int getArpu14_m()
	{
		return arpu14_m;
	}

	
	public void setArpu14_m(int arpu14_m)
	{
		this.arpu14_m=arpu14_m;
	}

	
	public int getArpu14_d()
	{
		return arpu14_d;
	}

	
	public void setArpu14_d(int arpu14_d)
	{
		this.arpu14_d=arpu14_d;
	}

	
	public int getArpu30_m()
	{
		return arpu30_m;
	}

	
	public void setArpu30_m(int arpu30_m)
	{
		this.arpu30_m=arpu30_m;
	}

	
	public int getArpu30_d()
	{
		return arpu30_d;
	}

	
	public void setArpu30_d(int arpu30_d)
	{
		this.arpu30_d=arpu30_d;
	}

	
	public int getArpu60_m()
	{
		return arpu60_m;
	}

	
	public void setArpu60_m(int arpu60_m)
	{
		this.arpu60_m=arpu60_m;
	}

	
	public int getArpu60_d()
	{
		return arpu60_d;
	}

	
	public void setArpu60_d(int arpu60_d)
	{
		this.arpu60_d=arpu60_d;
	}

	
	public int getRate1_m()
	{
		return rate1_m;
	}

	
	public void setRate1_m(int rate1_m)
	{
		this.rate1_m=rate1_m;
	}

	
	public int getRate1_d()
	{
		return rate1_d;
	}

	
	public void setRate1_d(int rate1_d)
	{
		this.rate1_d=rate1_d;
	}

	
	public int getRate3_m()
	{
		return rate3_m;
	}

	
	public void setRate3_m(int rate3_m)
	{
		this.rate3_m=rate3_m;
	}

	
	public int getRate3_d()
	{
		return rate3_d;
	}

	
	public void setRate3_d(int rate3_d)
	{
		this.rate3_d=rate3_d;
	}

	
	public int getRate7_m()
	{
		return rate7_m;
	}

	
	public void setRate7_m(int rate7_m)
	{
		this.rate7_m=rate7_m;
	}

	
	public int getRate7_d()
	{
		return rate7_d;
	}

	
	public void setRate7_d(int rate7_d)
	{
		this.rate7_d=rate7_d;
	}

	
	public int getRate14_m()
	{
		return rate14_m;
	}

	
	public void setRate14_m(int rate14_m)
	{
		this.rate14_m=rate14_m;
	}

	
	public int getRate14_d()
	{
		return rate14_d;
	}

	
	public void setRate14_d(int rate14_d)
	{
		this.rate14_d=rate14_d;
	}

	
	public int getRate30_m()
	{
		return rate30_m;
	}

	
	public void setRate30_m(int rate30_m)
	{
		this.rate30_m=rate30_m;
	}

	
	public int getRate30_d()
	{
		return rate30_d;
	}

	
	public void setRate30_d(int rate30_d)
	{
		this.rate30_d=rate30_d;
	}

	
	public int getRate60_m()
	{
		return rate60_m;
	}

	
	public void setRate60_m(int rate60_m)
	{
		this.rate60_m=rate60_m;
	}

	
	public int getRate60_d()
	{
		return rate60_d;
	}

	
	public void setRate60_d(int rate60_d)
	{
		this.rate60_d=rate60_d;
	}

	
	public String getPlat()
	{
		return plat;
	}

	
	public void setPlat(String plat)
	{
		this.plat=plat;
	}

	
	public int getLoginCount()
	{
		return loginCount;
	}

	
	public void setLoginCount(int loginCount)
	{
		this.loginCount=loginCount;
	}

	
	public int getOnlineTime()
	{
		return onlineTime;
	}

	
	public void setOnlineTime(int onlineTime)
	{
		this.onlineTime=onlineTime;
	}
	
	
}
