package foxu.sea.activity;

import java.util.Arrays;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/**
 * ��ֵ�����
 */
public class PayRelayActivity extends Activity implements ActivitySave,
	ActivityCollate
{

	/** �콱ʱ�� �� */
	int rewardTime;
	/** �û���ֵ���� value:��ֵ�����������ֵ��һ���еĵڼ��� */
	IntKeyHashMap playerPayDays;
	/** ����� key:��Ӧ�ۻ����� value:��Ʒsid string ��һλ��gems */
	String[] awardSids;
	/** ��Ʒ Award */
	Award[] awardArr;
	/** �������ȡ�Ķ�Ӧ�Ľ�Ʒ */
	IntKeyHashMap receivedAward;

	/** �������� */
	protected boolean isChange=false;
	protected final int INTERVALTIME=5000;
	protected long lastSaveTime=0;
	/** �Ƿ񲹷����� */
	private boolean isSendAward=false;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		return resetActivity(stime,etime,initData,factory);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		if(initData!=null)
		{
			if(playerPayDays==null) playerPayDays=new IntKeyHashMap();
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
		if(playerPayDays==null) playerPayDays=new IntKeyHashMap();
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
			String payInfo=strArr[1]+","+strArr[2];
			playerPayDays.put(playerId,payInfo);
		}
		length=data.readUnsignedShort();
		// ����ȡ����
		for(int i=0;i<length;i++)
		{
			String playerInfo=data.readUTF();
			if(playerInfo==null||"".equals(playerInfo)) continue;
			String[] strArr=playerInfo.split(",");
			int playerId=Integer.parseInt(strArr[0]);
			int receivedAwardStr=Integer.parseInt(strArr[1]);
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
		// ��ֵ����
		int[] playerArray=playerPayDays.keyArray();
		data.writeShort(playerArray.length);
		for(int i=0;i<playerArray.length;i++)
		{
			String str="";
			str+=playerArray[i]+",";
			str+=playerPayDays.get(playerArray[i]);
			data.writeUTF(str);
		}
		// ����ȡ����
		playerArray=receivedAward.keyArray();
		data.writeShort(playerArray.length);
		for(int i=0;i<playerArray.length;i++)
		{
			int str=(Integer)receivedAward.get(playerArray[i]);
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
		data.writeByte(awardArr.length); // ��Ŀ��
		String payInfo="";
		if(playerPayDays.get(player.getId())==null)
			payInfo="0,0";
		else
			payInfo=(String)playerPayDays.get(player.getId());
		String[] payInfoArr=payInfo.split(",");
		// int lastPayDay=Integer.parseInt(payInfoArr[1]);
		for(int i=0;i<awardArr.length;i++)
		{
			Award award=awardArr[i];
			data.writeShort(i); // ��Ʒ�ȼ�
			// ��Ʒ״̬ 0������ȡ 1����ȡ 2����ȡ 3�ɳ�ֵ
			data.writeByte(getAwardStatus(player.getId(),i));
			award.viewAward(data,player);// ��������// ����
		}
		data.writeByte(Integer.parseInt(payInfoArr[0]));// ����ѳ�ֵ����
		data.writeInt(endTime-rewardTime-TimeKit.getSecondTime()); // �콱ʱ�� ��
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
	}

	/** ��ȡ�û��۳�ֵ���� */
	public int getPlayerPayDays(int playerId)
	{
		if(playerPayDays.get(playerId)==null) return 0;
		String payInfo=(String)playerPayDays.get(playerId);
		String[] payInfoArr=payInfo.split(",");
		int days=Integer.parseInt(payInfoArr[0]);
		return days;
	}

	/** ��ȡ�û��ϴγ�ֵʱ�� */
	public int getPlayerPayLastDays(int playerId)
	{
		/** ������ */
		// if(playerPayDays.get(playerId)==null) return 0;
		// String payInfo=(String)playerPayDays.get(playerId);
		// String[] payInfoArr=payInfo.split(",");
		// int day=Integer.parseInt(payInfoArr[1]);
		// return day;
		/** ��ʽ�� */
		if(playerPayDays.get(playerId)==null) return 0;
		String payInfo=(String)playerPayDays.get(playerId);
		String[] payInfoArr=payInfo.split(",");
		int day=Integer.parseInt(payInfoArr[1]);
		return day;
	}

	/** �����û��ۼƳ�ֵ���� */
	public void setPlayerPayDays(Player player)
	{
		/** ������ */
		// int nowDay=TimeKit.getSecondTime();
		// int lastDay=getPlayerPayLastDays(player.getId());
		// if(nowDay<=(lastDay+120)) return;
		// int days=getPlayerPayDays(player.getId())+1;
		// lastDay=nowDay;
		// playerPayDays.put(player.getId(),(days+","+lastDay));
		// JBackKit.sendPayRelayActivity(player,days);
		/** ��ʽ�� */
		int nowDay=SeaBackKit.getDayOfYear();
		int lastDay=getPlayerPayLastDays(player.getId());
		if(nowDay==lastDay) return;
		int days=getPlayerPayDays(player.getId())+1;
		lastDay=nowDay;
		synchronized(playerPayDays)
		{
			playerPayDays.put(player.getId(),(days+","+lastDay));
		}
		JBackKit.sendPayRelayActivity(player,days);
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

	/** �鿴�Ƿ�����ȡ���ý��� */
	public boolean checkReceivedAward(int playerId,int awardLv)
	{
		if(receivedAward.get(playerId)==null) return false;
		int receivedLv=(Integer)receivedAward.get(playerId);
		if(receivedLv==0) return false;
		int num=(receivedLv&(0x1<<awardLv))>>awardLv;
		if(num==1) return true;
		return false;
	}

	/** ��������콱��¼ */
	public void setRewardRecord(int playerId,int awardLv)
	{
		int receivedLv;
		if(receivedAward.get(playerId)==null)
			receivedLv=0;
		else
			receivedLv=(Integer)receivedAward.get(playerId);
		receivedLv+=(0x1<<awardLv);
		synchronized(receivedAward)
		{
			receivedAward.put(playerId,receivedLv);
		}
	}

	/** �콱 */
	public boolean reward(Player player,int awardLv,ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		if(data==null) data=new ByteBuffer();
		int playerId=player.getId();
		if(checkReceivedAward(playerId,awardLv)) return false;
		// �����콱��¼
		setRewardRecord(playerId,awardLv);
		Award aw=awardArr[awardLv];
		aw.awardSelf(player,TimeKit.getSecondTime(),data,objectFactory,null,
			new int[]{EquipmentTrack.PAY_RELAY});
		data.clear();
		aw.viewAward(data,player);
		// //����Ѿ���ֵ����
		data.writeByte(getPlayerPayDays(playerId));
		return true;
	}

	/**
	 * ��齱Ʒ״̬ ��Ʒ״̬ 0������ȡ 1����ȡ 2����ȡ 3�ɳ�ֵ
	 * 
	 * @param awardLv
	 * @return
	 */
	private int getAwardStatus(int playerId,int awardLv)
	{
		if(checkReceivedAward(playerId,awardLv)) return 2;
		int payDay=getPlayerPayDays(playerId);
		if(payDay>awardLv) return 1;
		// /** ������ */
		// int nowDay=TimeKit.getSecondTime();
		// int lastDay=getPlayerPayLastDays(playerId);
		// if(nowDay>(lastDay+120))
		// /** ��ʽ�� */
		int lastDay=getPlayerPayLastDays(playerId);
		int nowDay=SeaBackKit.getDayOfYear();
		if(payDay==awardLv&&lastDay<nowDay) return 3;
		return 0;
	}

	/** ��������ѳ�ֵδ��ȡ������ҽ��в��� */
	private void sendAward(CreatObjectFactory objectFactory)
	{
		int[] players=playerPayDays.keyArray();
		for(int i=0;i<players.length;i++)
		{
			Player player=objectFactory.getPlayerCache().load(players[i]+"");
			if(player==null) continue;
			int payDays=getPlayerPayDays(players[i]);
			for(int j=0;j<payDays;j++)
			{
				if(!checkReceivedAward(players[i],j))
				{
					// ���Ž���
					ByteBuffer data=new ByteBuffer();
					Award aw=awardArr[j];
					aw.awardSelf(player,TimeKit.getSecondTime(),
						data,objectFactory,null,
						new int[]{EquipmentTrack.PAY_RELAY});
					// ����콱��¼
					setRewardRecord(players[i],j);
					// �����ʼ�
					sendSystemMail(player,objectFactory,j);
				}
			}
		}
	}
	
	private void sendSystemMail(Player player,CreatObjectFactory factoty,int awardLv)
	{
		String title=InterTransltor.getInstance().getTransByKey(player.getLocale(),
			"system_mail");
		String content=InterTransltor.getInstance()
			.getTransByKey(player.getLocale(),
				"PayRelayActivity_autoSendReward_mail");
		content=TextKit.replace(content,"%",Integer.toString(awardLv+1));
		content=TextKit.replace(content,"%",awardToString(player,awardLv));
		MessageKit.sendSystemMessages(player,factoty,content,
			title);
	}
	
	private String awardToString(Player player,int awardLv)
	{
		String awardStr=awardSids[awardLv];
		if(awardStr==null||awardStr.equals(""))
			return "";
		String[] props=awardStr.split(",");
		String gem=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"gems");
		StringBuilder sb=new StringBuilder();
		int gems=Integer.parseInt(props[0]);
		if(gems>0)
		{
			sb.append(gem+"*"+gems+" ");
		}
		if(props!=null)
		{
			for(int i=1;i<props.length;i+=2)
			{
				int sid=Integer.parseInt(props[i]);
 				int num=Integer.parseInt(props[i+1]);
				if(sid>0)
				{
					sb.append(InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"prop_sid_"+sid)
						+"*"+num+" ");
				}
			}
		}
		return sb.toString();
	}

	/** ��Ƿ���� �������콱ʱ�� */
	public boolean isEnd()
	{
		int time=TimeKit.getSecondTime();
		return time>(endTime-rewardTime);
	}

	public int getRewardTime()
	{
		return rewardTime;
	}

	public IntKeyHashMap getPlayerPayDays()
	{
		return playerPayDays;
	}

	public String[] getAwardSids()
	{
		return awardSids;
	}

	public Award[] getAwardArr()
	{
		return awardArr;
	}

	public IntKeyHashMap getAward()
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

	@Override
	public void activityCollate(int time,CreatObjectFactory factoty)
	{
		if(!isOpen(time)&&!isSendAward)
		{
			isSendAward=true;
			sendAward(factoty);
		}

	}

}
