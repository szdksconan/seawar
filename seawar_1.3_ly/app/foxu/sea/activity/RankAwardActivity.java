package foxu.sea.activity;

import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.award.Award;
import foxu.sea.kit.SeaBackKit;

/**
 * 排名奖励活动
 * 
 * @author Alan
 */
public abstract class RankAwardActivity extends Activity implements
	ActivitySave,ActivityCollate
{
	/** 名次信息刷新频率 */
	public static final int FLUSH_TIME=5*60;
	/** 活动信息保存周期 */
	public static final int SAVE_CIRCLE=15*60;
	/** 活动排名记录 */
	ArrayList recordList=new ArrayList();
	/** 排名奖励列表 */
	ArrayList rankAwardList=new ArrayList();
	/** 奖励领取记录 */
	IntList awardRecord=new IntList();
	/** 奖励领取开启时间 */
	int awardTime;
	/** 上一次活动信息保存时间 */
	int lastSaveTime;
	/** 是否已结算 */
	boolean isSettleDown=false;
	/** 活动显示排名 */
	int showRank=20;

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
		initDataByString(initData,factoty);
		resetRankList(recordList,TimeKit.getSecondTime(),factoty,showRank);
		return getActivityState();
	}

	@Override
	public Object copy(Object obj)
	{
		RankAwardActivity activity=(RankAwardActivity)obj;
		activity.rankAwardList=new ArrayList();
		activity.recordList=new ArrayList();
		activity.awardRecord=new IntList();
		return activity;
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
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			awardRecord.add(data.readInt());
		}
		awardTime=data.readInt();
		isSettleDown=data.readBoolean();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeInt(awardRecord.size());
		for(int i=0;i<awardRecord.size();i++)
		{
			data.writeInt(awardRecord.get(i));
		}
		data.writeInt(awardTime);
		data.writeBoolean(isSettleDown);
		return data;
	}
	
	/** 是否可以领取奖励品 */
	public boolean isAwardAvailable()
	{
		return isSettleDown;
	}
	
	/** 是否处于结算时间 */
	public boolean isTimeToSettleDown(int time)
	{
		if(awardTime>time) return false;
		return true;
	}
	/** 添加领取记录 */
	public void addAwardRecord(Player player)
	{
		awardRecord.add(player.getId());
	}
	
	/** 是否已经领取过奖励 */
	public boolean isCompleteAward(Player player)
	{
		return awardRecord.contain(player.getId());
	}

	/** 获取领取奖励时间 */
	public int getAwardTime()
	{
		return awardTime;
	}
	
	/** 获取名次对应的奖励 */
	public RankAward getAwardByRanking(int ranking)
	{
		RankAward ra=(RankAward)rankAwardList.get();
		if(ranking>ra.rank) return null;
		for(int j=0;j<rankAwardList.size();j++)
		{
			ra=(RankAward)rankAwardList.get(j);
			if(ranking<=ra.getRank()) return ra;
		}
		return null;
	}
	
	/** 获取各档位对应的奖励信息 */
	public RankAward[] getRankAwards()
	{
		RankAward[] awards=new RankAward[rankAwardList.size()];
		for(int i=0;i<awards.length;i++)
		{
			awards[i]=(RankAward)rankAwardList.get(i);
		}
		return awards;
	}
	/** 初始化活动数据 */
	public abstract void initDataByString(String initData,
		CreatObjectFactory factoty);
	/** 结束互动并结算活动数据 */
	public abstract void settleDown(CreatObjectFactory factoty);
	
	/** 向ArrayList中添加新的排名信息 */
	public abstract void resetRankList(ArrayList list,int time,
		CreatObjectFactory factoty,int len);
	/** 获取玩家对应的奖励 */
	public abstract Award getPlayerRankAward(Player player);
	/** 新的排名信息(活动结算后排名不再变化) */
	public abstract ArrayList getAwardRank();
	/** 向前端序列化排名得分时是否使用Long类型 */
	public abstract boolean isRankScoreLong();
	/** 获取自己的排名信息long[0]=排名,long[1]=得分 */
	public abstract long[] getSelfRank(Player player);
	/** 获取活动名称 */
	public abstract String getActivityName(Player player);
	/** 各档位对应的奖励信息 */
	public class RankAward
	{
		/** 名次上限 */
		int rank;
		/** 奖励品 */
		Award award;
		/** 奖励品数组 */
		int[] props;
		
		public int getRank()
		{
			return rank;
		}
		
		public Award getAward()
		{
			return award;
		}

		
		public int[] getProps()
		{
			return props;
		}
		
		
	}
}
