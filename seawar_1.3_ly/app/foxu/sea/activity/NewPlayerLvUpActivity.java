package foxu.sea.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

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
import foxu.sea.builds.PlayerBuild;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/**
 * 新手冲级活动
 * </p>逻辑参照{@link TotalBuyActivity}累计充值活动
 * @author Alan
 */
public class NewPlayerLvUpActivity extends Activity implements ActivitySave
{

	/** key=礼包sid(int) value=礼包（TotalBuyBag） */
	protected IntKeyHashMap rewardMap=new IntKeyHashMap();

	/** key=玩家id（int） value=玩家领取过的礼包sid（String） */
	protected IntKeyHashMap getAwardRecordMap=new IntKeyHashMap();

	protected CreatObjectFactory creatObjectFactory;
	protected String initData="";
	protected boolean isChange=false;
	protected final int INTERVALTIME=5000;
	protected long lastSaveTime=0;
	protected InterTransltor interTransltor;
	int buildSid;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		// this.dsManager=dsManager;
		if(creatObjectFactory==null)
		{
			creatObjectFactory=factory;
		}
		interTransltor=InterTransltor.getInstance();
		return resetActivity(stime,etime,initData,factory);
	}

	@Override
	public boolean closeActivity()
	{
		return checkGetReward();
	}

	@Override
	public NewPlayerLvUpActivity copy(Object obj)
	{
		NewPlayerLvUpActivity activity=(NewPlayerLvUpActivity)super.copy(obj);
		activity.rewardMap=new IntKeyHashMap();
		activity.getAwardRecordMap=new IntKeyHashMap();
		return activity;
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		String err=transInitData(initData);
		if(err!=null)
		{
			return err;
		}
		this.initData=initData;
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime);
		return getActivityState();
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

	protected String transInitData(String initData)
	{
		String[] data=TextKit.split(initData,";");
		IntKeyHashMap map=new IntKeyHashMap();
		try
		{
			for(int i=0;i<data.length;i++)
			{
				String s=data[i];
				if(s.equals(""))
				{
					continue;
				}
				String[] rewards=TextKit.split(s,",");
				// i=礼包sid；rewards[0]达到的级数；rewards[1]奖励宝石；rewards[2]奖励物品
				Award award=(Award)Award.factory
					.newSample(ActivityContainer.EMPTY_SID);
				int[] awards=null;
				if(rewards.length>=3)
				{
					if(rewards[2]!=null&&rewards[2].length()>0)
					{
						awards=transInts(rewards[2]);
						if(awards!=null)
						{
							initAward(award,awards);
						}
						else
						{
							return "err:initData";
						}
					}
				}
				// award.setPropSid(props);
				int gems=TextKit.parseInt(rewards[1]);
				if(gems<0)
				{
					return "err:gems";
				}
				award.setGemsAward(gems);
				TotalBuyBag bag=new TotalBuyBag(i+1,
					TextKit.parseInt(rewards[0]),award,awards);
				map.put(bag.getSid(),bag);
			}
		}
		catch(Exception e)
		{
			return "err:initData";
		}
		rewardMap=map;
		return null;
	}

	public void initAward(Award award,int[] awards)
	{
		SeaBackKit.resetAward(award,awards);
	}

	public int[] transInts(String ints)
	{
		String[] strs=TextKit.split(ints,":");
		if(strs.length%2!=0||strs.length>8)
		{
			return null;
		}
		int[] props=new int[strs.length];
		for(int i=0;i<strs.length;i++)
		{
			props[i]=TextKit.parseInt(strs[i]);
			if(i%2==0&&SeaBackKit.getSidType(props[i])==Prop.VALID)
			{
				return null;
			}
			if(props[i]<=0)
			{
				return null;
			}
		}
		return props;
	}

	protected int getPlayerBuildLv(Player p)
	{
		PlayerBuild pb=null;
		int maxLv=0;
		Object[] builds=p.getIsland().getBuildArray();
		for(int i=0;i<builds.length;i++)
		{
			pb=(PlayerBuild)builds[i];
			if(pb==null)	continue;
			if(pb.getSid()==buildSid&&pb.getBuildLevel()>maxLv)
				maxLv=pb.getBuildLevel();
		}
		return maxLv;
	}
	/**
	 * 检查是否可获得的奖励
	 */
	protected boolean checkGetReward()
	{
		boolean isSave=false;
		int[] playerIds=creatObjectFactory.getPlayerCache().getCacheMap().keyArray();
		for(int playerId:playerIds)
		{
			String geted=(String)getAwardRecordMap.get(playerId);
			Player player=creatObjectFactory.getPlayerById(playerId);
			if(player==null)
			{
				continue;
			}
			ArrayList bags=getRewardList(player,geted);
			if(bags==null)
			{
				continue;
			}
			creatObjectFactory.getPlayerCache().load(
				String.valueOf(playerId));
			for(int i=0;i<bags.size();i++)
			{
				TotalBuyBag bag=(TotalBuyBag)bags.get(i);
				Award award=bag.getAward();
				award.awardSelf(player,TimeKit.getSecondTime(),null,
					creatObjectFactory,null,null);
				setGetRecord(player.getId(),bag.getSid(),geted);
				sendMail(player,bag);
			}
			isSave=true;
		}
		return isSave;
	}

	/**
	 * 发送邮件
	 * 
	 * @param player
	 * @param bag
	 */
	protected void sendMail(Player player,TotalBuyBag bag)
	{
		String title=interTransltor.getTransByKey(PublicConst.SERVER_LOCALE,
			"LvUpActivity_autoSendReward_mail_title");
		String content=interTransltor.getTransByKey(
			PublicConst.SERVER_LOCALE,"LvUpActivity_autoSendReward_mail");
		content=TextKit.replace(content,"%",formatDataTime(startTime));
		content=TextKit.replace(content,"%",formatDataTime(endTime));
		content=TextKit.replace(content,"%",interTransltor.getTransByKey(
			PublicConst.SERVER_LOCALE,"build_sid_7"));
		content=TextKit.replace(content,"%",
			String.valueOf(bag.getTargetLv()));
		content+=bag.toString();
		MessageKit.sendSystemMessages(player,creatObjectFactory,content,
			title);
	}

	/** 将当前毫秒数转化为时间格式 */
	public String formatDataTime(int creatTime)
	{
		long time=creatTime*1000l;
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
		return format.format(new Date(time));
	}

	/**
	 * 获得对应的奖励数组
	 * 
	 * @return
	 */
	protected ArrayList getRewardList(Player player,String geted)
	{
		ArrayList list=new ArrayList();
		Object[] bags=rewardMap.valueArray();
		for(int i=0;i<bags.length;i++)
		{
			TotalBuyBag bag=(TotalBuyBag)bags[i];
			if(geted!=null&&geted.contains(String.valueOf(bag.getSid())))// 获得玩家领取记录
			{
				int[] records=TextKit.parseIntArray(TextKit.split(geted,","));
				if(SeaBackKit.isContainValue(records,bag.getSid()));
					continue;
			}
			Integer maxLv=getPlayerBuildLv(player);// 获得玩家建筑对应级数

			if(maxLv==null||maxLv<bag.getTargetLv())
			{
				continue;
			}
			list.add(bag);
		}
		if(list.isEmpty())
		{
			return null;
		}
		return list;
	}


	/** 领取礼包 */
	public String getAward(Player player,int sid,ByteBuffer data)
	{
		String geted=(String)getAwardRecordMap.get(player.getId());
		if(geted!=null&&geted.contains(String.valueOf(sid)))// 获得玩家领取记录
		{
			int[] records=TextKit.parseIntArray(TextKit.split(geted,","));
			if(SeaBackKit.isContainValue(records,sid));
				return "have geted award";
		}
		int maxLv=getPlayerBuildLv(player);// 获得玩家建筑对应级数

		TotalBuyBag bag=(TotalBuyBag)rewardMap.get(sid);// 根据礼包sid获得礼包
		if(bag==null)
		{
			return "err:not award id";
		}
		if(maxLv<bag.getTargetLv())
		{
			return "err:Can't get";
		}
		setGetRecord(player.getId(),sid,geted);
		bag.getAward().awardLenth(data,player,creatObjectFactory,null,new int[]{EquipmentTrack.FROM_TOTAL_BUY});
		return null;
	}

	/**
	 * 设置玩家领取记录
	 */
	protected void setGetRecord(int playerId,int sid,String s)
	{
		if(s==null)
		{
			getAwardRecordMap.put(playerId,String.valueOf(sid));
		}
		else
		{
			getAwardRecordMap.put(playerId,s+","+sid);
		}
		isChange=true;
	}
	/**
	 * 玩家领取记录
	 * 
	 * @param playerId
	 * @param sid
	 * @return
	 */
	protected boolean getRecord(int playerId,int sid)
	{
		String geted=(String)getAwardRecordMap.get(playerId);
		if(geted!=null&&geted.contains(String.valueOf(sid)))
		{
			int[] records=TextKit.parseIntArray(TextKit.split(geted,","));
			if(SeaBackKit.isContainValue(records,sid));
				return false;
		}
		else
		{
			return true;
		}
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
			.append(",\"others\":\"").append("[initData]:"+initData+",")
			.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	// protected String getMapToString()
	// {
	// StringBuffer sb=new StringBuffer();
	// // IntKeyHashMap copyRewardMap=getCopyRewardMap();
	// Entry[] entrys=rewardMap.getArray();
	// for(int i=0;i<entrys.length;i++)
	// {
	// Entry entry=entrys[i];
	// if(entry!=null)
	// {
	// TotalBuyBag bag=(TotalBuyBag)entry.getValue();
	// sb.append("Info "+bag.toString()+";");
	// }
	// }
	// return sb.toString();
	// }

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeInt(endTime-TimeKit.getSecondTime());
	}

	public void showByteWrite(ByteBuffer data,Player player)
	{
		// IntKeyHashMap copyRewardMap=getCopyRewardMap();
		int[] keys=rewardMap.keyArray();
		int count=keys.length;
//		if(count>4)
//		{
//			count=4;
//		}
		data.writeByte(count);// 条目长度
//		data.writeInt(getPlayerBuildLv(player));// 当前建筑级数

		for(int i=0;i<count;i++)
		{
			int key=keys[i];
			data.writeShort(key);// 条目 id
			data.writeBoolean(getRecord(player.getId(),key));// 是否可以领取
			TotalBuyBag bag=(TotalBuyBag)rewardMap.get(key);
			Award award=bag.getAward();
			award.viewAward(data,player);// 奖励长度// 奖励
			data.writeByte(bag.getTargetLv());// 需要到达的级数

		}
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		if(creatObjectFactory==null)
		{
			creatObjectFactory=factory;
		}
		interTransltor=InterTransltor.getInstance();
		initData=data.readUTF();
		transInitData(initData);
		getAwardRecordMap.clear();
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			getAwardRecordMap.put(data.readInt(),data.readUTF());
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeUTF(initData);
		data.writeShort(getAwardRecordMap.size());
		int[] keys=getAwardRecordMap.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			data.writeInt(keys[i]);
			data.writeUTF((String)getAwardRecordMap.get(keys[i]));
		}

		return data;
	}
	@Override
	public void sendFlush(SessionMap smap)
	{
		
	}

	/**
	 * 新手冲级礼包
	 * 
	 * @author
	 */
	class TotalBuyBag
	{

		private int sid;
		private int targetLv;
		private Award award;
		private int[] props;

		public int getTargetLv()
		{
			return targetLv;
		}

		public int getSid()
		{
			return sid;
		}

		public Award getAward()
		{
			return award;
		}
		public TotalBuyBag()
		{
		}
		public TotalBuyBag(int sid,int targetLv,Award award,int[] props)
		{
			this.sid=sid;
			this.targetLv=targetLv;
			this.award=award;
			this.props=props;
		}

		public String toString()
		{
			String content=interTransltor.getTransByKey(
				PublicConst.SERVER_LOCALE,"gems");
			StringBuilder sb=new StringBuilder();
			int gems=award.getGemsAward(0);
			if(gems>0)
			{
				sb.append(gems+content+" ");
			}

			if(props!=null)
			{
				for(int i=0;i<props.length;i+=2)
				{
					if(props[i]>0)
					{
						sb.append(interTransltor.getTransByKey(
							PublicConst.SERVER_LOCALE,"prop_sid_"+props[i])
							+"*"+props[i+1]+" ");
					}
				}
			}
			return sb.toString();
		}
	}
}
