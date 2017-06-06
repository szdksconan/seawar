package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.ActivityLogMemCache;
import foxu.sea.ContextVarManager;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.operators.DayDiscountActivity;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;

/**
 * 每日折扣活动
 * 
 * @author Alan
 */
public class DatePriceOffActivity extends Activity implements ActivitySave
{

	/** 活动信息保存周期 */
	public static final int SAVE_CIRCLE=15*60;
//	/** 售卖时段，升序排列 */
//	public static int[][] OPEN={{9*3600,13*3600},{14*3600,20*3600}};
	/** 最大在售数量 */
	public static int maxOnSale=3;
	/** 手动添加折扣物品,必然售卖 */
	IntKeyHashMap manualMap;
	/** 随机物品 */
	PropInfo[] randomProps=new PropInfo[0];
	/** 当前售卖物品 */
	IntKeyHashMap currentPropMap;
	/** 当日可选随机物品(对应随机物品的索引) */
	int[] leftRandomProps;
	/** 玩家购买记录,key:id,value:(物品sid为key,购买次数为value的IntKeyHashMap) */
	IntKeyHashMap playerRecord;
	/** 上一次重置选择列表的时间 */
	int lastResetTime;
	/** 上一次活动信息保存时间 */
	int lastSaveTime;
	/** 下次检测时间 */
	volatile int nextTime;
	/** 初始总权重 */
	int totalWeight;
	/** 是否强制推送,解决新开活动时,在当前活动添加进活动容器之前的推送由于没有找到导致推送无效 */
	boolean isForce;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		if(initData!=null&&!initData.equals(""))
		{
			String[] data=initData.split(",");
			for(int i=0;i<data.length;i+=DayDiscountActivity.INITDATELENGTH)
			{
				int sid=TextKit.parseInt(data[i]);
				int num=TextKit.parseInt(data[i+1]);
				int price=TextKit.parseInt(data[i+2]);
				int sTime=SeaBackKit.parseFormatTime(data[i+3]);
				int eTime=SeaBackKit.parseFormatTime(data[i+4]);
				addManualInfo(sid,num,price,sTime,eTime);
			}
		}
		openActivity(factory.getDsmanager().getSessionMap());
		return getActivityState();
	}
	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		String[] data=initData.split(",");
		String type=data[data.length-1];
		if("1".equals(type))
		{
			closeActivity();
		}
		else if("2".equals(type))
		{
			boolean isFlush=false;
			for(int i=0;i<data.length-1;i+=DayDiscountActivity.INITDATELENGTH)
			{
				int sid=TextKit.parseInt(data[i]);
				int num=TextKit.parseInt(data[i+1]);
				int price=TextKit.parseInt(data[i+2]);
				int sTime=SeaBackKit.parseFormatTime(data[i+3]);
				int eTime=SeaBackKit.parseFormatTime(data[i+4]);
				if(addManualInfo(sid,num,price,sTime,eTime))
					isFlush=true;
			}
			if(isFlush)
				JBackKit.sendDateOffPropState(factory.getDsmanager().getSessionMap());
		}
		else if("3".equals(type))
		{
			if(removeManualInfo())
				JBackKit.sendDateOffPropState(factory.getDsmanager().getSessionMap());
		}
		return getActivityState();
	}

	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"others\":\"")
			.append("current:");
			int[] sids=currentPropMap.keyArray();
			for(int i=0;i<sids.length;i++)
			{
				PropInfo prop=(PropInfo)currentPropMap.get(sids[i]);
				sb.append(prop.sid+","+prop.offPrice+",");
			}
			sb.append("manual:");
			sids=manualMap.keyArray();
			for(int i=0;i<sids.length;i++)
			{
				PropInfo prop=(PropInfo)manualMap.get(sids[i]);
				sb.append(prop.sid+","+prop.offPrice);
				if(i<sids.length-1)
					sb.append(",");
			}
			float[] open=PublicConst.DATE_PRICE_OPEN;
			sb.append("time:");
			for(int i=0;i<open.length-1;i+=2)
			{
				int st=(int)open[i];
				int st_=(int)((open[i]-st)*60);
				int et=(int)open[i+1];
				int et_=(int)((open[i+1]-et)*60);
				sb.append(st+":"+st_+"-"+et+":"+et_+",");
			}
			sb.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	/** 返回是否需要刷新当前售卖 */
	public boolean addManualInfo(int sid,int num,int offPrice,int sTime,
		int eTime)
	{
		PropInfo prop=new PropInfo(sid,num,offPrice,sTime,eTime,0);
		manualMap.put(prop.sid,prop);
		// 如果当前售卖包含修改的物品
		if(currentPropMap.get(sid)!=null)
		{
			int time=TimeKit.getSecondTime();
			// 该物品处于当前售卖时段，更新物品信息
			if(prop.sTime>=time&&prop.eTime<time)
				currentPropMap.put(prop.sid,prop);
			// 否则移除物品重新添加随机物品
			else
			{
				currentPropMap.remove(prop.sid);
				addCurrentPorp(time);
			}
			return true;
		}
		return false;
	}

	/** 移除手动设置售卖的信息,返回是否需要刷新当前售卖 */
	public boolean removeManualInfo()
	{
		boolean isFlush=false;

			if(manualMap.size()>0)
			{
				int[] sids=manualMap.keyArray();
				for(int i=0;i<sids.length;i++)
				{
					// 如果当前售卖中存在，移除并重新添加随机物品
					if(currentPropMap.get(sids[i])!=null)
					{
						isFlush=true;
						currentPropMap.remove(sids[i]);
					}
				}
				// 移除手动设置售卖的信息
				manualMap.clear();
				if(isFlush)
					addCurrentPorp(TimeKit.getSecondTime());
			}

		return isFlush;
	}

	public void readPropInfos(IntKeyHashMap values,ByteBuffer data)
	{
		int length=data.readByte();
		for(int i=0;i<length;i++)
		{
			PropInfo prop=new PropInfo();
			prop.byteRead(data);
			values.put(prop.sid,prop);
		}
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		nextTime=data.readInt();
		lastResetTime=data.readInt();
		// 正在售卖
		readPropInfos(currentPropMap,data);
		// 手动添加物品
		readPropInfos(manualMap,data);
		// 当日可选
		int length=data.readByte();
		leftRandomProps=new int[length];
		for(int i=0;i<length;i++)
		{
			leftRandomProps[i]=data.readByte();
		}
		if(getEndTime()==Integer.MAX_VALUE)
		{
			initRandomProps();
		}
		// 玩家购买记录
		length=data.readUnsignedShort();
		for(int i=0;i<length;i++)
		{
			int pid=data.readInt();
			int len=data.readByte();
			IntKeyHashMap record=new IntKeyHashMap();
			for(int j=0;j<len;j++)
			{
				record.put((int)data.readShort(),(int)data.readByte());
			}
			playerRecord.put(pid,record);
		}
	}


	public void writePropInfos(Object[] values,ByteBuffer data)
	{
		data.writeByte(values.length);
		for(int i=0;i<values.length;i++)
		{
			PropInfo prop=(PropInfo)values[i];
			prop.byteWrite(data);
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeInt(nextTime);
		data.writeInt(lastResetTime);
		// 正在售卖
		writePropInfos(currentPropMap.valueArray(),data);
		// 手动添加物品
		writePropInfos(manualMap.valueArray(),data);
		// 当日可选
		data.writeByte(leftRandomProps.length);
		for(int i=0;i<leftRandomProps.length;i++)
		{
			data.writeByte(leftRandomProps[i]);
		}
		// 玩家购买记录
		int[] pids=playerRecord.keyArray();
		data.writeShort(pids.length);
		for(int i=0;i<pids.length;i++)
		{
			data.writeInt(pids[i]);
			IntKeyHashMap pRecord=(IntKeyHashMap)playerRecord.get(pids[i]);
			int[] records=pRecord.keyArray();
			data.writeByte(records.length);
			for(int j=0;j<records.length;j++)
			{
				data.writeShort(records[j]);
				data.writeByte((Integer)pRecord.get(records[j]));
			}
		}
		return data;
	}

	/** 是否可以购买 */
	public boolean isPurchasable(Player player,int propSid)
	{
		checkAndSetTimer();
		PropInfo prop=(PropInfo)currentPropMap.get(propSid);
		if(prop!=null)
		{
			int num=0;
			IntKeyHashMap record=(IntKeyHashMap)playerRecord.get(player
				.getId());
			if(record!=null&&record.get(propSid)!=null)
				num=(Integer)record.get(propSid);
			if(num<prop.num) return true;
		}
		return false;
	}
	/** 玩家购买记录 */
	public void addRecord(Player player,int propSid)
	{
		IntKeyHashMap record=(IntKeyHashMap)playerRecord.get(player.getId());
		if(record==null)
		{
			record=new IntKeyHashMap();
			playerRecord.put(player.getId(),record);
		}
		int num=0;
		if(record.get(propSid)!=null) num=(Integer)record.get(propSid);
		num++;
		record.put(propSid,num);
		ActivityLogMemCache.getInstance().collectAlog(getId(),
			propSid+"",player.getId(),((PropInfo)currentPropMap.get(propSid)).offPrice);
	}

	/** 获取倒计时 */
	public int getTimeCount()
	{
		// 当前正在售卖中，下次激活时间即结束时间
		if(currentPropMap.size()>0)
		{
			return nextTime-TimeKit.getSecondTime();
		}
		return 0;
	}

	public void showByteWrite(ByteBuffer data,Player player)
	{
		checkAndSetTimer();
		// 倒计时
		int timeCount=getTimeCount();
		data.writeInt(timeCount);
		// 售卖数量
		if(timeCount==0)
			data.writeByte(0);
		else
		{
			IntKeyHashMap record=(IntKeyHashMap)playerRecord.get(player
				.getId());
			int[] info=currentPropMap.keyArray();
			data.writeByte(info.length);
			for(int i=0;i<info.length;i++)
			{
				PropInfo prop=(PropInfo)currentPropMap.get(info[i]);
				int num=0;
				if(record!=null&&record.get(info[i])!=null)
					num=(Integer)record.get(info[i]);
				// sid
				data.writeShort(prop.sid);
				// 已购买数量
				data.writeByte(num);
				// 购买上限
				data.writeByte(prop.num);
				// 折扣价
				data.writeInt(prop.offPrice);
			}
		}
	}

	public static void showByteWriteClosed(ByteBuffer data)
	{
		data.writeInt(0);
		data.writeByte(0);
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{

	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		if(checkAndSetTimer()||isForce)
		{
			isForce=false;
//			// 如果是新开售卖，进行设备推送
//			if(currentPropMap.size()>0)
//			{
//				String push=InterTransltor.getInstance().getTransByKey(
//					PublicConst.SERVER_LOCALE,"date_price_off_start");
//				SeaBackKit.appPush(ActivityContainer.getInstance()
//					.getObjectFactory(),push,PublicConst.DATE_OFF_PUSH);
//			}
			JBackKit.sendDateOffPropState(smap);
		}
	}

	public void initRandomProps()
	{
		String[] awards=PublicConst.DATE_PRICE_OFF;
		randomProps=new PropInfo[awards.length];
		for(int i=0;i<awards.length;i++)
		{
			String[] info=TextKit.split(awards[i],":");
			PropInfo prop=new PropInfo(Integer.valueOf(info[0]),
				Integer.valueOf(info[1]),Integer.valueOf(info[2]),0,0,
				Integer.valueOf(info[3]));
			randomProps[i]=prop;
			totalWeight+=prop.ability;
		}
	}

	/** 生成售卖列表 */
	public void addCurrentPorp(int checkTime)
	{
		// 获取手动设置物品
		int[] manualKeys=manualMap.keyArray();
		for(int i=0;i<manualKeys.length;i++)
		{
			PropInfo prop=(PropInfo)manualMap.get(manualKeys[i]);
			if(prop==null||checkTime>=prop.eTime)
			{
				manualMap.remove(manualKeys[i]);
			}
			else if(checkTime>=prop.sTime&&checkTime<prop.eTime)
			{
				currentPropMap.put(manualKeys[i],
					manualMap.get(manualKeys[i]));
			}
			if(currentPropMap.size()>=maxOnSale) break;
		}
		// 如果手动设置物品数量未够,添加随机物品
		int leftLen=maxOnSale-currentPropMap.size();
		for(int j=0;j<leftLen;j++)
		{
			int random=MathKit.randomValue(0,totalWeight);
			int gv=0;
			for(int i=0;i<leftRandomProps.length;i++)
			{
				int index=leftRandomProps[i];
				if(index<0) continue;
				PropInfo prop=randomProps[i];
				gv+=prop.ability;
				if(gv>random)
				{
					totalWeight-=prop.ability;
					currentPropMap.put(prop.sid,prop);
					leftRandomProps[i]=-1;
					break;
				}
			}
		}
	}
	/**获取购买人物列表**/
	public IntKeyHashMap getPlayerRecord()
	{
		return playerRecord;
	}
	
	/**售卖的物品**/
	public IntKeyHashMap getCurrentPropMap()
	{
		return currentPropMap;
	}
	/** 重置可供选择的售卖列表 */
	public void resetLeftRandom()
	{
		leftRandomProps=new int[randomProps.length];
		lastResetTime=TimeKit.getSecondTime();
		totalWeight=0;
		for(int i=0;i<randomProps.length;i++)
		{
			PropInfo prop=randomProps[i];
			totalWeight+=prop.ability;
		}
	}

	/** 检测设置并返回是否需要刷新售卖状态 */
	public boolean checkAndSetTimer()
	{
		int checkTime=TimeKit.getSecondTime();
		// 如果当前时间点未到下一次检测时间则跳过
		if(checkTime>=nextTime)
		{
			int dayStart=SeaBackKit.getSomedayBegin(0);
			// 如果今日未重置过可选择列表
			if(lastResetTime<dayStart)
			{
				resetLeftRandom();
			}
			synchronized(currentPropMap)
			{
				float[] open=PublicConst.DATE_PRICE_OPEN;
				// 按开始结束时间倒序判定
				for(int i=open.length-1;i>0;i-=2)
				{
					// 开始结束时都进行清空，防止特殊情况出现
					currentPropMap.clear();
					playerRecord.clear();
					// 本时段结束
					if(checkTime>=dayStart+open[i]*3600)
					{
						nextTime=dayStart+(i+1)/open.length
							*PublicConst.DAY_SEC+(int)(open[(i+1)%open.length]*3600);
						break;
					}
					// 本时段开始
					else if(checkTime>=dayStart+open[i-1]*3600)
					{
						nextTime=dayStart+(int)(open[i]*3600);
						addCurrentPorp(checkTime);
						break;
					}
				}
			}
			isForce=true;
			return true;
		}
		return false;
	}

	/** 打开活动 */
	public void openActivity(SessionMap smap)
	{
		int timeNow=TimeKit.getSecondTime();
		setStartTime(timeNow);
		setEndTime(Integer.MAX_VALUE);
		lastSaveTime=timeNow;
		initRandomProps();
		resetLeftRandom();
		// 此处不再做推送,避免此时活动还未添加进容器
		// 找不到活动会导致推送失效
		checkAndSetTimer();
	}

	public boolean closeActivity()
	{
		ContextVarManager varmanager=ContextVarManager.getInstance();
		varmanager.putVar(ContextVarManager.DATE_OFF_STATE,0,null);
		setEndTime(TimeKit.getSecondTime());
		currentPropMap.clear();
		manualMap.clear();
		JBackKit.sendDateOffPropState(ActivityContainer.getInstance()
			.getObjectFactory().getDsmanager().getSessionMap());
		return false;
	}

	public boolean isAvaliableProp(int sid)
	{
		if(currentPropMap.get(sid)!=null) return true;
		return false;
	}

	public int getSidGems(int sid)
	{
		return ((PropInfo)currentPropMap.get(sid)).offPrice;
	}

	public class PropInfo
	{

		public PropInfo()
		{
		}
		public PropInfo(int sid,int num,int offPrice,int sTime,int eTime,
			int ability)
		{
			super();
			this.sid=sid;
			this.num=num;
			this.offPrice=offPrice;
			this.sTime=sTime;
			this.eTime=eTime;
			this.ability=ability;
		}

		int sid;
		int num;
		int offPrice;
		/** 售卖开始时段 */
		int sTime;
		/** 售卖结束时段 */
		int eTime;
		/** 概率 */
		int ability;

		public void byteRead(ByteBuffer data)
		{
			this.sid=data.readUnsignedShort();
			this.num=data.readInt();
			this.offPrice=data.readUnsignedShort();
			this.sTime=data.readInt();
			this.eTime=data.readInt();
			this.ability=data.readUnsignedShort();
		}

		public void byteWrite(ByteBuffer data)
		{
			data.writeShort(this.sid);
			data.writeInt(this.num);
			data.writeShort(this.offPrice);
			data.writeInt(this.sTime);
			data.writeInt(this.eTime);
			data.writeShort(this.ability);
		}
	}

	@Override
	public boolean isSave()
	{
		if(TimeKit.getSecondTime()>=lastSaveTime+SAVE_CIRCLE) return true;
		return false;
	}
	@Override
	public void setSave()
	{
		lastSaveTime=TimeKit.getSecondTime();
	}

	@Override
	public Object clone()
	{
		super.clone();
		nextTime=0;
		totalWeight=0;
		manualMap=new IntKeyHashMap();
		currentPropMap=new IntKeyHashMap();
		playerRecord=new IntKeyHashMap();
		return this;
	}

}
