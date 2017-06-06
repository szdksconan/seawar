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
 * ÿ���ۿۻ
 * 
 * @author Alan
 */
public class DatePriceOffActivity extends Activity implements ActivitySave
{

	/** ���Ϣ�������� */
	public static final int SAVE_CIRCLE=15*60;
//	/** ����ʱ�Σ��������� */
//	public static int[][] OPEN={{9*3600,13*3600},{14*3600,20*3600}};
	/** ����������� */
	public static int maxOnSale=3;
	/** �ֶ�����ۿ���Ʒ,��Ȼ���� */
	IntKeyHashMap manualMap;
	/** �����Ʒ */
	PropInfo[] randomProps=new PropInfo[0];
	/** ��ǰ������Ʒ */
	IntKeyHashMap currentPropMap;
	/** ���տ�ѡ�����Ʒ(��Ӧ�����Ʒ������) */
	int[] leftRandomProps;
	/** ��ҹ����¼,key:id,value:(��ƷsidΪkey,�������Ϊvalue��IntKeyHashMap) */
	IntKeyHashMap playerRecord;
	/** ��һ������ѡ���б��ʱ�� */
	int lastResetTime;
	/** ��һ�λ��Ϣ����ʱ�� */
	int lastSaveTime;
	/** �´μ��ʱ�� */
	volatile int nextTime;
	/** ��ʼ��Ȩ�� */
	int totalWeight;
	/** �Ƿ�ǿ������,����¿��ʱ,�ڵ�ǰ���ӽ������֮ǰ����������û���ҵ�����������Ч */
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

	/** �����Ƿ���Ҫˢ�µ�ǰ���� */
	public boolean addManualInfo(int sid,int num,int offPrice,int sTime,
		int eTime)
	{
		PropInfo prop=new PropInfo(sid,num,offPrice,sTime,eTime,0);
		manualMap.put(prop.sid,prop);
		// �����ǰ���������޸ĵ���Ʒ
		if(currentPropMap.get(sid)!=null)
		{
			int time=TimeKit.getSecondTime();
			// ����Ʒ���ڵ�ǰ����ʱ�Σ�������Ʒ��Ϣ
			if(prop.sTime>=time&&prop.eTime<time)
				currentPropMap.put(prop.sid,prop);
			// �����Ƴ���Ʒ������������Ʒ
			else
			{
				currentPropMap.remove(prop.sid);
				addCurrentPorp(time);
			}
			return true;
		}
		return false;
	}

	/** �Ƴ��ֶ�������������Ϣ,�����Ƿ���Ҫˢ�µ�ǰ���� */
	public boolean removeManualInfo()
	{
		boolean isFlush=false;

			if(manualMap.size()>0)
			{
				int[] sids=manualMap.keyArray();
				for(int i=0;i<sids.length;i++)
				{
					// �����ǰ�����д��ڣ��Ƴ���������������Ʒ
					if(currentPropMap.get(sids[i])!=null)
					{
						isFlush=true;
						currentPropMap.remove(sids[i]);
					}
				}
				// �Ƴ��ֶ�������������Ϣ
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
		// ��������
		readPropInfos(currentPropMap,data);
		// �ֶ������Ʒ
		readPropInfos(manualMap,data);
		// ���տ�ѡ
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
		// ��ҹ����¼
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
		// ��������
		writePropInfos(currentPropMap.valueArray(),data);
		// �ֶ������Ʒ
		writePropInfos(manualMap.valueArray(),data);
		// ���տ�ѡ
		data.writeByte(leftRandomProps.length);
		for(int i=0;i<leftRandomProps.length;i++)
		{
			data.writeByte(leftRandomProps[i]);
		}
		// ��ҹ����¼
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

	/** �Ƿ���Թ��� */
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
	/** ��ҹ����¼ */
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

	/** ��ȡ����ʱ */
	public int getTimeCount()
	{
		// ��ǰ���������У��´μ���ʱ�伴����ʱ��
		if(currentPropMap.size()>0)
		{
			return nextTime-TimeKit.getSecondTime();
		}
		return 0;
	}

	public void showByteWrite(ByteBuffer data,Player player)
	{
		checkAndSetTimer();
		// ����ʱ
		int timeCount=getTimeCount();
		data.writeInt(timeCount);
		// ��������
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
				// �ѹ�������
				data.writeByte(num);
				// ��������
				data.writeByte(prop.num);
				// �ۿۼ�
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
//			// ������¿������������豸����
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

	/** ���������б� */
	public void addCurrentPorp(int checkTime)
	{
		// ��ȡ�ֶ�������Ʒ
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
		// ����ֶ�������Ʒ����δ��,��������Ʒ
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
	/**��ȡ���������б�**/
	public IntKeyHashMap getPlayerRecord()
	{
		return playerRecord;
	}
	
	/**��������Ʒ**/
	public IntKeyHashMap getCurrentPropMap()
	{
		return currentPropMap;
	}
	/** ���ÿɹ�ѡ��������б� */
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

	/** ������ò������Ƿ���Ҫˢ������״̬ */
	public boolean checkAndSetTimer()
	{
		int checkTime=TimeKit.getSecondTime();
		// �����ǰʱ���δ����һ�μ��ʱ��������
		if(checkTime>=nextTime)
		{
			int dayStart=SeaBackKit.getSomedayBegin(0);
			// �������δ���ù���ѡ���б�
			if(lastResetTime<dayStart)
			{
				resetLeftRandom();
			}
			synchronized(currentPropMap)
			{
				float[] open=PublicConst.DATE_PRICE_OPEN;
				// ����ʼ����ʱ�䵹���ж�
				for(int i=open.length-1;i>0;i-=2)
				{
					// ��ʼ����ʱ��������գ���ֹ�����������
					currentPropMap.clear();
					playerRecord.clear();
					// ��ʱ�ν���
					if(checkTime>=dayStart+open[i]*3600)
					{
						nextTime=dayStart+(i+1)/open.length
							*PublicConst.DAY_SEC+(int)(open[(i+1)%open.length]*3600);
						break;
					}
					// ��ʱ�ο�ʼ
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

	/** �򿪻 */
	public void openActivity(SessionMap smap)
	{
		int timeNow=TimeKit.getSecondTime();
		setStartTime(timeNow);
		setEndTime(Integer.MAX_VALUE);
		lastSaveTime=timeNow;
		initRandomProps();
		resetLeftRandom();
		// �˴�����������,�����ʱ���δ��ӽ�����
		// �Ҳ�����ᵼ������ʧЧ
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
		/** ������ʼʱ�� */
		int sTime;
		/** ��������ʱ�� */
		int eTime;
		/** ���� */
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
