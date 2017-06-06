package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.ActivityLogMemCache;
import foxu.sea.Player;
import foxu.sea.Resources;
import foxu.sea.award.Award;
import foxu.sea.kit.SeaBackKit;

/**
 * 拼图活动
 * @author Alan
 *
 */
public class JigsawActivity extends Activity implements ActivitySave
{
	public static final int SAVE_CIRCLE=15*60;
	/** 碎片区域数量 */
	public static final int PARTS_NUM=9;
	/** 连抽次数 */
	public static int batchCount=10;
	/** 单次抽取价格 */
	int singlePrice;
	/** 连抽价格 */
	int batchPrice;
	/** 指定购买价格 */
	int salePrice;
	/** 奖品信息 */
	String[] awards;
	/** 活动记录 */
	IntKeyHashMap playerRecord=new IntKeyHashMap();
	/** 免费记录 */
	IntKeyHashMap freeRecord=new IntKeyHashMap();
	/** 奖品记录 */
	ArrayList awardList=new ArrayList();
	/** 限量奖励领取记录 */
	IntList[] limitAwardRecord;
	/** 各奖品当前累计(临时信息，用于存储和初始化活动) */
	int[] nowCount;
	/** 活动上一次保存时间 */
	int lastSaveTime;
	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		return resetActivity(stime,etime,initData,factoty);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		startTime=SeaBackKit.parseFormatTime(stime);
		endTime=SeaBackKit.parseFormatTime(etime);
		String[] infos=TextKit.split(initData,"-");
		int[] prices=TextKit.parseIntArray(TextKit.split(infos[0],","));
		singlePrice=prices[0];
		batchPrice=prices[1];
		salePrice=prices[2];
		awards=TextKit.split(infos[1],";");
		nowCount=new int[awards.length];
		limitAwardRecord=new IntList[awards.length];
		initAward();
		return getActivityState();
	}

	public void initAward()
	{
		awardList.clear();
		for(int i=0;i<awards.length;i++)
		{
			AwardInfo info=new AwardInfo();
			String[] infoByCount=TextKit.split(awards[i],"|");
			info.limit=TextKit.parseInt(infoByCount[1]);
			String[] infoByAbli=TextKit.split(infoByCount[0],":");
			info.abli=TextKit.parseInt(infoByAbli[1]);
			int[] infoAward=TextKit.parseIntArray(TextKit.split(infoByAbli[0],","));
			Award award=(Award)Award.factory.newSample(ActivityContainer.EMPTY_SID);
			info.award=award;
			info.nowCount=nowCount[i];
			SeaBackKit.resetAward(award,infoAward);
			awardList.add(info);
		}
	}

	@Override
	public String getActivityState()
	{

		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"")
			.append(id)
			.append('"')
			.append(",\"sid\":\"")
			.append(getSid())
			.append('"')
			.append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime))
			.append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime))
			.append('"')
			.append(",\"others\":\"")
			.append(
				"singlePrice:"+singlePrice+","+"batchPrice:"+batchPrice+","
					+"salePrice:"+salePrice+",playerCount:"
					+playerRecord.size()+",");
		for(int i=0;i<awards.length;i++)
		{
			sb.append(awards[i]).append(";");
		}
		sb.append("\",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append("}");
		return sb.toString();

	}

	/** 获取拥有当前碎片的数量 */
	public int getPartsCount(Player player,int parts)
	{
		if(parts>=PARTS_NUM) return 0;
		int[] playerParts=(int[])playerRecord.get(player.getId());
		if(playerParts!=null) return playerParts[parts-1];
		return 0;
	}

	/** 获取拥有各碎片的数量 */
	public int[] getPartsCount(Player player)
	{
		int[] playerParts=(int[])playerRecord.get(player.getId());
		if(playerParts==null) playerParts=new int[PARTS_NUM];
		return playerParts;
	}

	/** 今日免费次数是否可用 */
	public boolean isFreeTurn(Player player)
	{
		Integer freeTime=(Integer)freeRecord.get(player.getId());
		if(freeTime==null||freeTime<SeaBackKit.getSomedayBegin(0))
			return true;
		return false;
	}

	/** 单次抽取,返回宝石是否足够 */
	public boolean singleDraw(Player player)
	{
		boolean isFree=isFreeTurn(player);
		if(isFree)
		{
			freeRecord.put(player.getId(),TimeKit.getSecondTime());
		}
		else
		{
			if(!Resources.checkGems(singlePrice,player.getResources()))
				return false;
			Resources.reduceGems(singlePrice,player.getResources(),player);
			ActivityLogMemCache.getInstance().collectAlog(getId(),"",
				player.getId(),singlePrice);
		}
		drawAndCollateRecord(player);
		return true;
	}

	/** 批量抽取,返回宝石是否足够 */
	public boolean batchDraw(Player player)
	{
		if(!Resources.checkGems(batchPrice,player.getResources()))
			return false;
		Resources.reduceGems(batchPrice,player.getResources(),player);
		ActivityLogMemCache.getInstance().collectAlog(getId(),"",
			player.getId(),batchPrice);
		for(int i=0;i<batchCount;i++)
		{
			drawAndCollateRecord(player);
		}
		return true;
	}

	/** 抽取一次并且整理玩家记录 */
	public void drawAndCollateRecord(Player player)
	{
		int parts=MathKit.randomValue(0,PARTS_NUM);
		addAndCollateRecord(player,parts);
	}

	/** 购买指定碎片,返回宝石是否足够 */
	public boolean buyParts(Player player,int index)
	{
		if(!Resources.checkGems(salePrice,player.getResources()))
			return false;
		Resources.reduceGems(salePrice,player.getResources(),player);
		ActivityLogMemCache.getInstance().collectAlog(getId(),"",
			player.getId(),salePrice);
		addAndCollateRecord(player,index-1);
		return true;
	}
	
	public void addAndCollateRecord(Player player,int index)
	{
		int[] playerParts=getPartsCount(player);
		playerParts[index]+=1;
		playerRecord.put(player.getId(),playerParts);
	}
	
	/** 当前玩家是否可以领取奖励 */
	public boolean isAwardAvailable(Player player)
	{
		int[] record=(int[])playerRecord.get(player.getId());
		if(record!=null)
		{
			for(int i=0;i<PARTS_NUM;i++)
			{
				if(record[i]<1) return false;
			}
		}
		return true;
	}

	/** 领奖结算 */
	public void completeAward(Player player)
	{
		int[] record=(int[])playerRecord.get(player.getId());
		if(record!=null)
		{
			for(int i=0;i<PARTS_NUM;i++)
			{
				record[i]-=1;
			}
		}
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		singlePrice=data.readUnsignedShort();
		batchPrice=data.readUnsignedShort();
		salePrice=data.readUnsignedShort();
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			int pid=data.readInt();
			int[] record=new int[PARTS_NUM];
			for(int j=0;j<PARTS_NUM;j++)
			{
				record[j]=data.readUnsignedShort();
			}
			playerRecord.put(pid,record);
		}
		len=data.readUnsignedByte();
		awards=new String[len];
		for(int i=0;i<len;i++)
		{
			awards[i]=data.readUTF();
		}
		nowCount=new int[len];
		for(int i=0;i<len;i++)
		{
			nowCount[i]=data.readInt();
		}
		limitAwardRecord=new IntList[len];
		IntList record=null;
		for(int i=0;i<len;i++)
		{
			record=new IntList();
			int listLen=data.readInt();
			for(int j=0;j<listLen;j++)
			{
				record.add(data.readInt());
			}
		}
		initAward();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(singlePrice);
		data.writeShort(batchPrice);
		data.writeShort(salePrice);
		int[] records=playerRecord.keyArray();
		data.writeInt(records.length);
		for(int i=0;i<records.length;i++)
		{
			int[] record=(int[])playerRecord.get(records[i]);
			data.writeInt(records[i]);
			for(int j=0;j<PARTS_NUM;j++)
			{
				data.writeShort(record[j]);
			}
		}
		data.writeByte(awards.length);
		for(int i=0;i<awards.length;i++)
		{
			data.writeUTF(awards[i]);
		}
		for(int i=0;i<nowCount.length;i++)
		{
			AwardInfo info=(AwardInfo)awardList.get(i);
			data.writeInt(info.nowCount);
		}
		for(int i=0;i<limitAwardRecord.length;i++)
		{
			int listLen=0;
			if(limitAwardRecord[i]!=null)
				listLen=limitAwardRecord[i].size();
			data.writeInt(listLen);
			for(int j=0;j<listLen;j++)
			{
				data.writeInt(limitAwardRecord[i].get(j));
			}
		}
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeByte(0);
		data.writeInt(endTime-TimeKit.getSecondTime());
	}

	@Override
	public Object copy(Object obj)
	{
		JigsawActivity act=(JigsawActivity)obj;
		act.playerRecord=new IntKeyHashMap();
		act.freeRecord=new IntKeyHashMap();
		act.awardList=new ArrayList();
		return act;
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		// TODO Auto-generated method stub

	}

	public int getSinglePrice()
	{
		return singlePrice;
	}

	public int getBatchPrice()
	{
		return batchPrice;
	}

	public int getSalePrice()
	{
		return salePrice;
	}

	/** 当前位置的奖励对玩家是否可用 */
	public boolean isAwardAvailable(Player player,int awardKey)
	{
		AwardInfo info=(AwardInfo)awardList.get(awardKey);
		if(!info.isAwardAvailable())
			return false;
		IntList playerRecord=limitAwardRecord[awardKey];
		if(playerRecord!=null&&playerRecord.contain(player.getId()))
			return false;
		return true;
	}
	
	/** 随机获取玩家可用的奖励 */
	public Award getAward(Player player)
	{
		int totalWeight=0;
		AwardInfo info=null;
		synchronized(awardList)
		{
			// 先计算玩家可用的概率总计
			for(int i=0;i<awardList.size();i++)
			{
				if(isAwardAvailable(player,i))
				{
					info=(AwardInfo)awardList.get(i);
					totalWeight+=info.abli;
				}
			}
			// 随机数
			int random=MathKit.randomValue(0,totalWeight);
			// 开始的机率
			int startProbability=0;
			for(int i=0;i<awardList.size();i++)
			{
				info=(AwardInfo)awardList.get(i);
				if(!isAwardAvailable(player,i))
					continue;
				startProbability+=info.abli;
				// 如果随机数在这个范围内
				if(random<startProbability)
				{
					info.nowCount+=1;
					// 如果是限量奖励,进行记录
					if(info.isLimitAward())
					{
						IntList playerRecord=limitAwardRecord[i];
						if(playerRecord==null)
						{
							playerRecord=new IntList();
							limitAwardRecord[i]=playerRecord;
						}
						playerRecord.add(player.getId());
					}
					return info.award;
				}
			}
		}
		return (Award)Award.factory.getSample(ActivityContainer.EMPTY_SID);
	}
	
	/** 活动奖品信息 */
	class AwardInfo
	{
		Award award;
		/** 概率 */
		int abli;
		/** 次数上限,为0则表示无上限 */
		int limit;
		/** 当前总计 */
		int nowCount;
		
		/** 当前奖励品是否可用 */
		public boolean isAwardAvailable()
		{
			if(limit==0||nowCount<limit)
				return true;
			return false;
		}
		
		/** 是否是限量奖励 */
		public boolean isLimitAward()
		{
			return limit>0;
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
}
