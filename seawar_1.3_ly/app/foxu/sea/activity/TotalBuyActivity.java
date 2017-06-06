package foxu.sea.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import mustang.back.BackKit;
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
 * �ۼƳ�ֵ�
 * 
 * @author zz
 */
public class TotalBuyActivity extends Activity implements ActivitySave
{

	/** key=���sid(int) value=�����TotalBuyBag�� */
	private IntKeyHashMap rewardMap=new IntKeyHashMap();

	/** key=���id��int�� value=����ۼƳ�ֵ����int�� */
	private IntKeyHashMap totalBuyMap=new IntKeyHashMap();

	/** key=���id��int�� value=�����ȡ�������sid��String�� */
	private IntKeyHashMap getAwardRecordMap=new IntKeyHashMap();

	// /** ֻ��¼�ﵽҪ��δ��ȡ������ key=���id��int�� value=���δ��ȡ�����sid��String) */
	// private IntKeyHashMap honoreesMap=new IntKeyHashMap();

	private CreatObjectFactory creatObjectFactory;
	private String initData="";
	private boolean isChange=false;
	private final int INTERVALTIME=5000;
	private long lastSaveTime=0;
	private InterTransltor interTransltor;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		// this.dsManager=dsManager;
		if(creatObjectFactory==null)
		{
			creatObjectFactory=(CreatObjectFactory)BackKit.getContext().get(
				"creatObjectFactory");
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
	public TotalBuyActivity copy(Object obj)
	{
		TotalBuyActivity activity=(TotalBuyActivity)super.copy(obj);
		activity.rewardMap=new IntKeyHashMap();
		activity.totalBuyMap=new IntKeyHashMap();
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

	private String transInitData(String initData)
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
				// i=���sid��rewards[0]�ﵽ���ۼƳ�ֵ����rewards[1]������ʯ�ٷֱȣ�rewards[2]������Ʒ
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
				int gems=TextKit.parseInt(rewards[0])
					*TextKit.parseInt(rewards[1])/100;
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

	/**
	 * �����ۼƳ�ֵ��
	 * 
	 * @param id
	 * @param gems
	 */
	public void updateTotalBuy(Player p,int gems)
	{
		int totalGems=getPlayerTotalBuy(p);
		if(totalGems<=0)
		{
			totalBuyMap.put(p.getId(),gems);
		}
		else
		{
			totalBuyMap.put(p.getId(),totalGems+gems);
		}
		isChange=true;
		JBackKit.sendPlayerTotalBuyAward(this,p,0);// ˢ�¸����ۼƳ�ֵ��Ʒ��Ϣ
	}

	private int getPlayerTotalBuy(Player p)
	{
		Object totalGems=totalBuyMap.get(p.getId());
		if(totalGems==null)
		{
			return 0;
		}
		else
		{
			return (Integer)totalGems;
		}
	}
	/**
	 * ����Ƿ�ɻ�õĽ���
	 */
	private boolean checkGetReward()
	{
		boolean isSave=false;
		int[] playerIds=totalBuyMap.keyArray();
		for(int playerId:playerIds)
		{
			String geted=(String)getAwardRecordMap.get(playerId);
			Player player=creatObjectFactory.getPlayerCache().load(
				String.valueOf(playerId));
			if(player==null)
			{
				continue;
			}
			ArrayList bags=getRewardList(player,geted);
			if(bags==null)
			{
				continue;
			}
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
	 * �����ʼ�
	 * 
	 * @param player
	 * @param bag
	 */
	private void sendMail(Player player,TotalBuyBag bag)
	{
		String title=interTransltor.getTransByKey(PublicConst.SERVER_LOCALE,
			"totalBuyActivity_autoSendReward_mail_title");
		String content=interTransltor
			.getTransByKey(PublicConst.SERVER_LOCALE,
				"totalBuyActivity_autoSendReward_mail");
		content=TextKit.replace(content,"%",formatDataTime(startTime));
		content=TextKit.replace(content,"%",formatDataTime(endTime));
		content=TextKit.replace(content,"%",
			String.valueOf(bag.getTargetGems()));
		content+=bag.toString();
		MessageKit.sendSystemMessages(player,creatObjectFactory,content,
			title);
	}

	/** ����ǰ������ת��Ϊʱ���ʽ */
	public String formatDataTime(int creatTime)
	{
		long time=creatTime*1000l;
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
		return format.format(new Date(time));
	}

	/**
	 * ��ö�Ӧ�Ľ�������
	 * 
	 * @return
	 */
	private ArrayList getRewardList(Player player,String geted)
	{
		ArrayList list=new ArrayList();
		Object[] bags=rewardMap.valueArray();
		for(int i=0;i<bags.length;i++)
		{
			TotalBuyBag bag=(TotalBuyBag)bags[i];
			if(geted!=null&&geted.contains(String.valueOf(bag.getSid())))// ��������ȡ��¼
			{
				int[] records=TextKit.parseIntArray(TextKit.split(geted,","));
				if(SeaBackKit.isContainValue(records,bag.getSid()));
					continue;
			}
			Integer totalBuy=(Integer)totalBuyMap.get(player.getId());// �������ۼƳ�ֵ��

			if(totalBuy==null||totalBuy<bag.getTargetGems())
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
	// todo
	// private IntKeyHashMap getCopyRewardMap()
	// {
	// IntKeyHashMap map=new IntKeyHashMap();
	// Entry[] entrys=rewardMap.getArray();
	// for(int i=0;i<entrys.length;i++)
	// {
	// Entry entry=entrys[i];
	// if(entry!=null)
	// {
	// map.put(entry.getKey(),entry.getValue());
	// }
	// }
	// return map;
	// }

	/** ��ȡ��� */
	public String getAward(Player player,int sid,ByteBuffer data)
	{
		String geted=(String)getAwardRecordMap.get(player.getId());
		if(geted!=null&&geted.contains(String.valueOf(sid)))// ��������ȡ��¼
		{
			int[] records=TextKit.parseIntArray(TextKit.split(geted,","));
			if(SeaBackKit.isContainValue(records,sid));
				return "have geted award";
		}
		Integer totalBuy=(Integer)totalBuyMap.get(player.getId());// �������ۼƳ�ֵ��
		if(totalBuy==null||totalBuy<=0)
		{
			return "player not buy";
		}

		TotalBuyBag bag=(TotalBuyBag)rewardMap.get(sid);// �������sid������
		if(bag==null)
		{
			return "err:not award id";
		}
		if(totalBuy<bag.getTargetGems())
		{
			return "err:Can't get";
		}
		data.clear();
		setGetRecord(player.getId(),sid,geted);
		bag.getAward().awardLenth(data,player,creatObjectFactory,null,new int[]{EquipmentTrack.FROM_TOTAL_BUY});
		return null;
	}

	/**
	 * ���������ȡ��¼
	 */
	private void setGetRecord(int playerId,int sid,String s)
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
	 * �����ȡ��¼
	 * 
	 * @param playerId
	 * @param sid
	 * @return
	 */
	private boolean getRecord(int playerId,int sid)
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

	// private String getMapToString()
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
		data.writeByte(100);
		data.writeInt(endTime-TimeKit.getSecondTime());
	}

	public void showShortByteWrite(ByteBuffer data,int awardSid,Player player)
	{
		data.writeInt(awardSid);// ��Ŀ id
		if(awardSid>0)
		{
			data.writeBoolean(getRecord(player.getId(),awardSid));// �Ƿ������ȡ

		}
		data.writeInt(getPlayerTotalBuy(player));// ��ǰ�ۼƱ�ʯ��
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
		data.writeByte(count);// ��Ŀ����
		data.writeInt(getPlayerTotalBuy(player));// ��ǰ�ۼƱ�ʯ��

		for(int i=0;i<count;i++)
		{
			int key=keys[i];
			data.writeInt(key);// ��Ŀ id
			data.writeBoolean(getRecord(player.getId(),key));// �Ƿ������ȡ
			TotalBuyBag bag=(TotalBuyBag)rewardMap.get(key);
			Award award=bag.getAward();
			award.viewAward(data,player);// ��������// ����
			data.writeInt(bag.getTargetGems());// �ܹ��ۼƱ�ʯ��

		}
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		if(creatObjectFactory==null)
		{
			creatObjectFactory=(CreatObjectFactory)BackKit.getContext().get(
				"creatObjectFactory");
		}
		interTransltor=InterTransltor.getInstance();
		initData=data.readUTF();
		transInitData(initData);
		totalBuyMap.clear();
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			totalBuyMap.put(data.readInt(),data.readInt());
		}
		getAwardRecordMap.clear();
		len=data.readUnsignedShort();
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
		data.writeShort(totalBuyMap.size());
		int[] keys=totalBuyMap.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			data.writeInt(keys[i]);
			data.writeInt((Integer)totalBuyMap.get(keys[i]));
		}

		data.writeShort(getAwardRecordMap.size());
		keys=getAwardRecordMap.keyArray();
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
		JBackKit.sendTotalBuyAwardActivty(smap,this);
	}

	/**
	 * �ۼƳ�ֵ���
	 * 
	 * @author
	 */
	class TotalBuyBag
	{

		private int sid;
		private int targetGems;
		private Award award;
		private int[] props;

		public int getTargetGems()
		{
			return targetGems;
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
		public TotalBuyBag(int sid,int targetGems,Award award,int[] props)
		{
			this.sid=sid;
			this.targetGems=targetGems;
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
