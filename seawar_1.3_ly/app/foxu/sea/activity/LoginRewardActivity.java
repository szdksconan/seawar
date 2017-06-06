package foxu.sea.activity;

import java.util.Arrays;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import shelby.ds.DSManager;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/**
 * ��¼����
 */
public class LoginRewardActivity extends Activity implements ActivitySave,
	ActivityCollate
{

	/** �콱ʱ�� �� */
	int rewardTime;
	/** �û��ۻ���½���� key:id,value String: ��½����,�ϴ�����¼��ʱ��(һ��ĵڼ���) */
	IntKeyHashMap playerLoginDays;
	/** ����� key:��Ӧ�ۻ����� value:��Ʒsid string ��һλ��gems */
	String[] awardSids;
	/** ��Ʒ Award */
	Award[] awardArr;
	/** �������ȡ�Ķ�Ӧ�Ľ�Ʒ value String: �콱��int,ʱ���һش��� */
	IntKeyHashMap receivedAward;
	/** ʱ���һر�ʯ�������� 50 �ݶ� */
	private static final int TIME_FIND_BASE=50;
	private static final int TIME_FIND_COEF=50;

	/** �������� */
	protected boolean isChange=false;
	protected final int INTERVALTIME=5000;
	protected long lastSaveTime=0;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
//		SessionMap smap=ActivityContainer.getInstance().dsManager
//			.getSessionMap();
//		// ����������������ҵ�½����+1
//		addOnlinePlayerCount(smap);
		return resetActivity(stime,etime,initData,factory);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		if(initData!=null)
		{
			// if(award==null) award= new int[10];
			if(playerLoginDays==null) playerLoginDays=new IntKeyHashMap();
			if(receivedAward==null) receivedAward=new IntKeyHashMap();
			String[] datas=initData.split("\\|");
			String awardsData=datas[0];
			rewardTime=Integer.parseInt(datas[1])*3600;
			awardSids=awardsData.split(";");
			awardArr=new Award[awardSids.length];
			awardsInit();
		}
		startTime=SeaBackKit.parseFormatTime(stime);
		endTime=SeaBackKit.parseFormatTime(etime)+rewardTime;
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
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
			.append(",\"others\":\"awards:")
			.append(Arrays.toString(awardSids));
		sb.append("\",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		// �콱ʱ��
		rewardTime=data.readInt();
		if(playerLoginDays==null) playerLoginDays=new IntKeyHashMap();
		if(receivedAward==null) receivedAward=new IntKeyHashMap();
		int awardLen=data.readUnsignedShort();
		if(awardArr==null) awardArr=new Award[awardLen];
		if(awardSids==null) awardSids=new String[awardLen];
		for(int i=0;i<awardLen;i++)
		{
			String str=data.readUTF();
			awardSids[i]=str;
		}
		int length=data.readUnsignedShort();
		for(int i=0;i<length;i++)
		{
			String playerInfo=data.readUTF();
			if(playerInfo==null||"".equals(playerInfo)) continue;
			String[] strArr=playerInfo.split(",");
			int playerId=Integer.parseInt(strArr[0]);
			int loginDays=Integer.parseInt(strArr[1]);
			int recordDay=Integer.parseInt(strArr[2]);
			String value=loginDays+","+recordDay;
			playerLoginDays.put(playerId,value);
		}
		length=data.readUnsignedShort();
		// ����ȡ����
		for(int i=0;i<length;i++)
		{
			String playerInfo=data.readUTF();
			if(playerInfo==null||"".equals(playerInfo)) continue;
			String[] strArr=playerInfo.split(",");
			int playerId=Integer.parseInt(strArr[0]);
			String receivedAwardStr=strArr[1]+","+strArr[2];
			receivedAward.put(playerId,receivedAwardStr);
		}
		awardsInit();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		// �콱ʱ��
		data.writeInt(rewardTime);
		// ��Ʒ
		data.writeShort(awardSids.length);
		for(int i=0;i<awardSids.length;i++)
		{
			String str=awardSids[i];
			data.writeUTF(str);
		}
		// ��½����
		int[] playerArray=playerLoginDays.keyArray();
		data.writeShort(playerArray.length);
		for(int i=0;i<playerArray.length;i++)
		{
			String str="";
			str+=playerArray[i]+",";
			str+=playerLoginDays.get(playerArray[i]);
			data.writeUTF(str);
		}
		// ����ȡ����
		playerArray=receivedAward.keyArray();
		data.writeShort(playerArray.length);
		for(int i=0;i<playerArray.length;i++)
		{
			String str=(String)receivedAward.get(playerArray[i]);
			data.writeUTF(playerArray[i]+","+str);
		}
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeByte(0);
		data.writeInt(endTime-TimeKit.getSecondTime());
		data.writeInt(endTime-rewardTime-TimeKit.getSecondTime());
	}

	/** ǰ̨���л� */
	public void showByteWrite(ByteBuffer data,Player player)
	{
		int days=0;
		int[] receivedData=getReceivedData(player.getId());
		if(playerLoginDays.get(player.getId())==null)
		{
			days=1;
			//��һ�� ���õ�½
			setPlayerLoginDays(player.getId(),1,SeaBackKit.getDayOfYear());
		}
		else
		{
			days=getPlayerLoginDays(player.getId());
		}
		data.writeByte(awardArr.length); // ��Ŀ��
		for(int i=0;i<awardArr.length;i++)
		{
			// ��Ʒ״̬ 0������ȡ 1����ȡ 2ʱ���һ� 3����ȡ
			int status=getAwardStatus(player.getId(),i);
			Award award=awardArr[i];
			data.writeShort(i); // ��Ʒ�ȼ�
			data.writeByte(status);
			award.viewAward(data,player);// ��������// ����
			data.writeByte(days); // ����ѵ�¼����
		}
		data.writeInt(TIME_FIND_BASE+TIME_FIND_COEF*receivedData[1]);// ʱ���һ���Ҫ��ʯ
		data.writeByte(receivedData[1]);// ����Ѿ�ʱ���һش��� 50+50*count
		data.writeInt(endTime-rewardTime-TimeKit.getSecondTime()); // �콱ʱ�� ��
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
	}

	/** Awards ��ʼ�� */
	public void awardsInit()
	{
		if(awardSids==null) return;
		for(int i=0;i<awardSids.length;i++)
		{
			Award a=(Award)Award.factory
				.newSample(ActivityContainer.EMPTY_SID);
			ArrayList propList=new ArrayList();
			ArrayList equipList=new ArrayList();
			ArrayList shipList=new ArrayList();
			ArrayList officerList=new ArrayList();
			String[] subAwardSids=awardSids[i].split(",");
			// ��һλΪ��ʯ
			int gems=Integer.parseInt(subAwardSids[0]);
			a.setGemsAward(gems);
			for(int j=1;j<subAwardSids.length;j+=2)
			{
				int sid=Integer.parseInt(subAwardSids[j]);
				int count=Integer.parseInt(subAwardSids[j+1]);
				if(SeaBackKit.getSidType(sid)==Prop.PROP)
				{
					propList.add(sid);
					propList.add(count);
				}
				if(SeaBackKit.getSidType(sid)==Prop.EQUIP)
				{
					equipList.add(sid);
					equipList.add(count);
				}
				if(SeaBackKit.getSidType(sid)==Prop.SHIP)
				{
					shipList.add(sid);
					shipList.add(count);
				}
				if(SeaBackKit.getSidType(sid)==Prop.OFFICER)
				{
					officerList.add(sid);
					officerList.add(count);
				}
			}
			int[] propArr=new int[propList.toArray().length];
			int[] equipArr=new int[equipList.toArray().length];
			int[] shipArr=new int[shipList.toArray().length];
			int[] officerArr=new int[officerList.toArray().length];
			if(propArr.length>0) for(int j=0;j<propList.size();j++)
				propArr[j]=(Integer)propList.get(j);
			if(equipArr.length>0) for(int j=0;j<equipList.size();j++)
				equipArr[j]=(Integer)equipList.get(j);
			if(shipArr.length>0) for(int j=0;j<shipList.size();j++)
				shipArr[j]=(Integer)shipList.get(j);
			if(officerArr.length>0) for(int j=0;j<officerList.size();j++)
				officerArr[j]=(Integer)officerList.get(j);
			a.setPropSid(propArr);
			a.setEquipSids(equipArr);
			a.setShipSids(shipArr);
			a.setOfficerSids(officerArr);
			awardArr[i]=a;
		}
	}

	/** ��ȡ�û��ۼƵ�½���� */
	public int getPlayerLoginDays(int playerId)
	{
		if(playerLoginDays.get(playerId)==null) return 0;
		String value=(String)playerLoginDays.get(playerId);
		String[] valueArr=value.split(",");
		int days=Integer.parseInt(valueArr[0]);
		return days;
	}

	/** ��ȡ�û��ϴμ�¼ʱ�� */
	public int getPlayerRecordDay(int playerId)
	{
		if(playerLoginDays.get(playerId)==null) return 0;
		String value=(String)playerLoginDays.get(playerId);
		String[] valueArr=value.split(",");
		int day=Integer.parseInt(valueArr[1]);
		return day;
	}

	/** �����û��ۼƵ�½���� dayNum=���ӵ����� day=һ���еĵڼ��� */
	public void setPlayerLoginDays(int playerId,int dayNum,int day)
	{
		synchronized(playerLoginDays)
		{
			int now=TimeKit.getSecondTime();
			int actTime=endTime-rewardTime;
			if(now>actTime) return;
			// ���ͬһ���򷵻�
			if(day==getPlayerRecordDay(playerId)) return;
			int days=getPlayerLoginDays(playerId)+dayNum;
			playerLoginDays.put(playerId,(days+","+day));
		}
		setChanged();
	}

	/** ����������ҵ�½���� */
	public void addOnlinePlayerCount(SessionMap smap)
	{
		Session[] sessions=smap.getSessions();
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			setPlayerLoginDays(player.getId(),1,SeaBackKit.getDayOfYear());
			JBackKit.sendLoginReward(player); // ����
		}
		setChanged();
	}

	/** �鿴�Ƿ�����ȡ���ý��� */
	public boolean checkReceivedAward(int playerId,int awardLv)
	{
		if(receivedAward.get(playerId)==null) return false;
		int[] receivedData=getReceivedData(playerId);
		int receivedLv=receivedData[0];
		if(receivedLv==0) return false;
		int num=(receivedLv&(0x1<<awardLv))>>awardLv;
		if(num==1) return true;
		return false;
	}

	/**
	 * �콱
	 * 
	 * @param awardLv ��Ʒ�ȼ�
	 * @param isRecoverTime �Ƿ�ʱ���һ�
	 * @return
	 */
	public boolean reward(Player player,int awardLv,ByteBuffer data,
		CreatObjectFactory objectFactory,boolean isRecoverTime)
	{
		if(data==null) data=new ByteBuffer();
		int playerId=player.getId();
		if(checkReceivedAward(playerId,awardLv)) return false;
		int[] receivedData=getReceivedData(playerId);
		int receivedLv=receivedData[0];
		int receivedRecover=receivedData[1];
		if(isRecoverTime) receivedRecover++;
		receivedLv+=(0x1<<awardLv);
		receivedAward.put(playerId,(receivedLv+","+receivedRecover));
		Award aw=awardArr[awardLv];
		aw.awardSelf(player,TimeKit.getSecondTime(),data,objectFactory,null,
			new int[]{EquipmentTrack.LOGIN_REWARD});
		data.clear();
		aw.viewAward(data,player);
		setChanged();
		data.writeInt(TIME_FIND_BASE+TIME_FIND_COEF*receivedRecover);// ʱ���һ���Ҫ��ʯ
		return true;
	}

	/** ��ȡ���ʱ���һش��� */
	private int[] getReceivedData(int playerId)
	{
		int[] receivedData=new int[2];
		if(receivedAward.get(playerId)==null)
		{
			receivedData[0]=0;
			receivedData[1]=0;
		}
		else
		{
			String receivedStr=(String)receivedAward.get(playerId);
			String[] arr=receivedStr.split(",");
			receivedData[0]=Integer.parseInt(arr[0]);
			receivedData[1]=Integer.parseInt(arr[1]);
		}
		return receivedData;
	}

	/**
	 * ��齱Ʒ״̬ ��Ʒ״̬ 0������ȡ 1����ȡ 2ʱ���һ� 3����ȡ
	 * 
	 * @param awardLv
	 * @return
	 */
	private int getAwardStatus(int playerId,int awardLv)
	{
		if(checkReceivedAward(playerId,awardLv)) return 3;
		boolean isRewardTime=TimeKit.getSecondTime()>(endTime-rewardTime);
		int loginDay=getPlayerLoginDays(playerId);
		if(loginDay>awardLv) return 1;
		if(loginDay<=awardLv&&isRewardTime) return 2;
		return 0;
	}

	/** ��ȡ���ʱ���һ���Ҫ��ʯ **/
	public int getTimeFindGems(int playerId)
	{
		return TIME_FIND_BASE+TIME_FIND_COEF*getReceivedData(playerId)[1];
	}

	/** ���ʱ���һ��Ƿ񰴴����һ� */
	public boolean checkTimeFind(int playerId,int awardLv)
	{
		int loginDay=getPlayerLoginDays(playerId);
		int num=getReceivedData(playerId)[1];
		if((loginDay+num)==awardLv) return true;
		return false;
	}

	public int getRewardTime()
	{
		return rewardTime;
	}

	public IntKeyHashMap getPlayerLoginDays()
	{
		return playerLoginDays;
	}

	public String[] getAwardSids()
	{
		return awardSids;
	}

	public Award[] getAwardArr()
	{
		return awardArr;
	}

	public IntKeyHashMap getReceivedAward()
	{
		return receivedAward;
	}

	public boolean isChange()
	{
		return isChange;
	}

	public void setChange(boolean isChange)
	{
		this.isChange=isChange;
	}

	@Override
	public boolean isSave()
	{
		if(TimeKit.getMillisTime()-lastSaveTime>INTERVALTIME&&isChange)
		{
			return true;
		}
		return false;
	}

	@Override
	public void setSave()
	{
		lastSaveTime=TimeKit.getMillisTime();
		isChange=false;
	}

	public void setChanged()
	{
		isChange=true;
	}

	/**
	 * ��½����ÿ��0������������ҵ�½����
	 */
	@Override
	public void activityCollate(int time,CreatObjectFactory factoty)
	{
		// ��ȡ�൱���賿�Ľ���ʱ�� ��
		int times=Math.abs(time-(SeaBackKit.getTimesnight()-24*3600));
		if(times<5)
		{
			DSManager dsm=ActivityContainer.getInstance().dsManager;
			addOnlinePlayerCount(dsm.getSessionMap());
		}
	}

}
