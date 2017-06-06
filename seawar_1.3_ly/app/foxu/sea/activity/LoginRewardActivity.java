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
 * 登录有礼活动
 */
public class LoginRewardActivity extends Activity implements ActivitySave,
	ActivityCollate
{

	/** 领奖时间 秒 */
	int rewardTime;
	/** 用户累积登陆天数 key:id,value String: 登陆天数,上次做记录的时间(一年的第几天) */
	IntKeyHashMap playerLoginDays;
	/** 活动奖励 key:对应累积天数 value:奖品sid string 第一位是gems */
	String[] awardSids;
	/** 奖品 Award */
	Award[] awardArr;
	/** 玩家已领取的对应的奖品 value String: 领奖的int,时间找回次数 */
	IntKeyHashMap receivedAward;
	/** 时间找回宝石消耗数量 50 暂定 */
	private static final int TIME_FIND_BASE=50;
	private static final int TIME_FIND_COEF=50;

	/** 保存设置 */
	protected boolean isChange=false;
	protected final int INTERVALTIME=5000;
	protected long lastSaveTime=0;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
//		SessionMap smap=ActivityContainer.getInstance().dsManager
//			.getSessionMap();
//		// 活动激活后所有在线玩家登陆天数+1
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
		// 领奖时间
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
		// 已领取奖励
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
		// 领奖时间
		data.writeInt(rewardTime);
		// 奖品
		data.writeShort(awardSids.length);
		for(int i=0;i<awardSids.length;i++)
		{
			String str=awardSids[i];
			data.writeUTF(str);
		}
		// 登陆天数
		int[] playerArray=playerLoginDays.keyArray();
		data.writeShort(playerArray.length);
		for(int i=0;i<playerArray.length;i++)
		{
			String str="";
			str+=playerArray[i]+",";
			str+=playerLoginDays.get(playerArray[i]);
			data.writeUTF(str);
		}
		// 已领取奖励
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

	/** 前台序列化 */
	public void showByteWrite(ByteBuffer data,Player player)
	{
		int days=0;
		int[] receivedData=getReceivedData(player.getId());
		if(playerLoginDays.get(player.getId())==null)
		{
			days=1;
			//第一天 设置登陆
			setPlayerLoginDays(player.getId(),1,SeaBackKit.getDayOfYear());
		}
		else
		{
			days=getPlayerLoginDays(player.getId());
		}
		data.writeByte(awardArr.length); // 条目数
		for(int i=0;i<awardArr.length;i++)
		{
			// 奖品状态 0不可领取 1可领取 2时间找回 3已领取
			int status=getAwardStatus(player.getId(),i);
			Award award=awardArr[i];
			data.writeShort(i); // 奖品等级
			data.writeByte(status);
			award.viewAward(data,player);// 奖励长度// 奖励
			data.writeByte(days); // 玩家已登录天数
		}
		data.writeInt(TIME_FIND_BASE+TIME_FIND_COEF*receivedData[1]);// 时间找回需要宝石
		data.writeByte(receivedData[1]);// 玩家已经时间找回次数 50+50*count
		data.writeInt(endTime-rewardTime-TimeKit.getSecondTime()); // 领奖时间 秒
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
	}

	/** Awards 初始化 */
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
			// 第一位为宝石
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

	/** 获取用户累计登陆天数 */
	public int getPlayerLoginDays(int playerId)
	{
		if(playerLoginDays.get(playerId)==null) return 0;
		String value=(String)playerLoginDays.get(playerId);
		String[] valueArr=value.split(",");
		int days=Integer.parseInt(valueArr[0]);
		return days;
	}

	/** 获取用户上次记录时间 */
	public int getPlayerRecordDay(int playerId)
	{
		if(playerLoginDays.get(playerId)==null) return 0;
		String value=(String)playerLoginDays.get(playerId);
		String[] valueArr=value.split(",");
		int day=Integer.parseInt(valueArr[1]);
		return day;
	}

	/** 设置用户累计登陆天数 dayNum=增加的天数 day=一年中的第几天 */
	public void setPlayerLoginDays(int playerId,int dayNum,int day)
	{
		synchronized(playerLoginDays)
		{
			int now=TimeKit.getSecondTime();
			int actTime=endTime-rewardTime;
			if(now>actTime) return;
			// 如果同一天则返回
			if(day==getPlayerRecordDay(playerId)) return;
			int days=getPlayerLoginDays(playerId)+dayNum;
			playerLoginDays.put(playerId,(days+","+day));
		}
		setChanged();
	}

	/** 增加在线玩家登陆次数 */
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
			JBackKit.sendLoginReward(player); // 推送
		}
		setChanged();
	}

	/** 查看是否已领取过该奖励 */
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
	 * 领奖
	 * 
	 * @param awardLv 奖品等级
	 * @param isRecoverTime 是否时间找回
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
		data.writeInt(TIME_FIND_BASE+TIME_FIND_COEF*receivedRecover);// 时间找回需要宝石
		return true;
	}

	/** 获取玩家时间找回次数 */
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
	 * 检查奖品状态 奖品状态 0不可领取 1可领取 2时间找回 3已领取
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

	/** 获取玩家时间找回需要宝石 **/
	public int getTimeFindGems(int playerId)
	{
		return TIME_FIND_BASE+TIME_FIND_COEF*getReceivedData(playerId)[1];
	}

	/** 检查时间找回是否按次序找回 */
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
	 * 登陆有礼活动每日0点增加在线玩家登陆次数
	 */
	@Override
	public void activityCollate(int time,CreatObjectFactory factoty)
	{
		// 获取距当日凌晨的结束时间 秒
		int times=Math.abs(time-(SeaBackKit.getTimesnight()-24*3600));
		if(times<5)
		{
			DSManager dsm=ActivityContainer.getInstance().dsManager;
			addOnlinePlayerCount(dsm.getSessionMap());
		}
	}

}
