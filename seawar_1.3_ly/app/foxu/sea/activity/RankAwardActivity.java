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
 * ���������
 * 
 * @author Alan
 */
public abstract class RankAwardActivity extends Activity implements
	ActivitySave,ActivityCollate
{
	/** ������Ϣˢ��Ƶ�� */
	public static final int FLUSH_TIME=5*60;
	/** ���Ϣ�������� */
	public static final int SAVE_CIRCLE=15*60;
	/** �������¼ */
	ArrayList recordList=new ArrayList();
	/** ���������б� */
	ArrayList rankAwardList=new ArrayList();
	/** ������ȡ��¼ */
	IntList awardRecord=new IntList();
	/** ������ȡ����ʱ�� */
	int awardTime;
	/** ��һ�λ��Ϣ����ʱ�� */
	int lastSaveTime;
	/** �Ƿ��ѽ��� */
	boolean isSettleDown=false;
	/** ���ʾ���� */
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
	
	/** �Ƿ������ȡ����Ʒ */
	public boolean isAwardAvailable()
	{
		return isSettleDown;
	}
	
	/** �Ƿ��ڽ���ʱ�� */
	public boolean isTimeToSettleDown(int time)
	{
		if(awardTime>time) return false;
		return true;
	}
	/** �����ȡ��¼ */
	public void addAwardRecord(Player player)
	{
		awardRecord.add(player.getId());
	}
	
	/** �Ƿ��Ѿ���ȡ������ */
	public boolean isCompleteAward(Player player)
	{
		return awardRecord.contain(player.getId());
	}

	/** ��ȡ��ȡ����ʱ�� */
	public int getAwardTime()
	{
		return awardTime;
	}
	
	/** ��ȡ���ζ�Ӧ�Ľ��� */
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
	
	/** ��ȡ����λ��Ӧ�Ľ�����Ϣ */
	public RankAward[] getRankAwards()
	{
		RankAward[] awards=new RankAward[rankAwardList.size()];
		for(int i=0;i<awards.length;i++)
		{
			awards[i]=(RankAward)rankAwardList.get(i);
		}
		return awards;
	}
	/** ��ʼ������� */
	public abstract void initDataByString(String initData,
		CreatObjectFactory factoty);
	/** ������������������ */
	public abstract void settleDown(CreatObjectFactory factoty);
	
	/** ��ArrayList������µ�������Ϣ */
	public abstract void resetRankList(ArrayList list,int time,
		CreatObjectFactory factoty,int len);
	/** ��ȡ��Ҷ�Ӧ�Ľ��� */
	public abstract Award getPlayerRankAward(Player player);
	/** �µ�������Ϣ(�������������ٱ仯) */
	public abstract ArrayList getAwardRank();
	/** ��ǰ�����л������÷�ʱ�Ƿ�ʹ��Long���� */
	public abstract boolean isRankScoreLong();
	/** ��ȡ�Լ���������Ϣlong[0]=����,long[1]=�÷� */
	public abstract long[] getSelfRank(Player player);
	/** ��ȡ����� */
	public abstract String getActivityName(Player player);
	/** ����λ��Ӧ�Ľ�����Ϣ */
	public class RankAward
	{
		/** �������� */
		int rank;
		/** ����Ʒ */
		Award award;
		/** ����Ʒ���� */
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
