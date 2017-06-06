package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.award.Award;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.equipment.EquipmentTrack;

/**
 * �������س齱����
 * 
 * @author Alan
 */
public class OnlineLuckyContainer
{

	private static OnlineLuckyContainer instance=new OnlineLuckyContainer();
	/** �������ؽ����Ǽ���Ӧ��ʱ�� */
	public static int ONLINE_LUCKY_LIMIT[];
	/** �������ؽ����Ǽ���Ӧ��Ȩ�� */
	public static int ONLINE_LUCKY_WEIGHT[];
	/** �������� */
	public ArrayList props=new ArrayList();
	/** �������ؽ����Ǽ���Ӧ��Ȩ�� */
	int totalWeight;
	
	public static OnlineLuckyContainer getInstance()
	{
		return instance;
	}

	/** �����������ؽ�����Ϣ������Ʒsid,����,����ȡʱ�䡣 */
	private int[] resetPlayerAward(Player player)
	{
		player.setAttribute(PublicConst.ONLINE_LUCKY_AWARD,null);
		IntList avaEntries=new IntList();
		PropInfo prop=null;
		int available=randomLimit();
		for(int i=0;i<props.size();i++)
		{
			int topLimit=0;
			prop=(PropInfo)props.get(i);
			// �������Ѿ���֤�������ÿһ�Ǽ���������������������
			if(prop.limit!=available)	continue;
			// ��ҵȼ�����ȡֵ
			if(prop.playerMin!=0)
			{
				topLimit=prop.playerMax;
				if(topLimit==-1) topLimit=PublicConst.MAX_PLAYER_LEVEL;
				if(player.getLevel()<prop.playerMin
					||player.getLevel()>topLimit) continue;
			}
			// �������͵ȼ�����
			int buildType=prop.buildType;
			if(buildType!=0)
			{
				PlayerBuild build=player.getIsland().getBuildByType(
					buildType,null);
				if(build==null) continue;
				// ָ�������ȼ�����ȡֵ
				topLimit=prop.buildMax;
				if(topLimit==-1) topLimit=PublicConst.MAX_BUILD_LEVEL;
				if(prop.buildMin>build.getBuildLevel()
					||build.getBuildLevel()>topLimit) continue;
			}
			// ��������,��¼����
			avaEntries.add(i);
		}
		int index=avaEntries.get(MathKit.randomValue(0,avaEntries.size()));
		prop=(PropInfo)props.get(index);
		int awardTime=TimeKit.getSecondTime()
			+ONLINE_LUCKY_LIMIT[prop.limit-1];
		player.setAttribute(PublicConst.ONLINE_LUCKY_AWARD,prop.sid+","
			+prop.num+","+awardTime);
		return getPlayerAward(player);
	}

	/** ��ȡ�������ؽ�����Ϣ������Ʒsid,����,����ȡʱ�䡣����Ϣ��ȫΪ0 */
	public int[] getPlayerAward(Player player)
	{
		int[] onlineLucky=new int[3];
		String str=player.getAttributes(PublicConst.ONLINE_LUCKY_AWARD);
		if(str!=null&&!"".equals(str))
		{
			String[] strs=str.split(",");
			onlineLucky[0]=TextKit.parseInt(strs[0]);
			onlineLucky[1]=TextKit.parseInt(strs[1]);
			onlineLucky[2]=TextKit.parseInt(strs[2]);
		}
		else
			onlineLucky=resetPlayerAward(player);
		return onlineLucky;
	}

	/** ��ȡ���ν�������ʱ */
	public int getCountTime(Player player)
	{
		int countTime=getPlayerAward(player)[2]-TimeKit.getSecondTime();
		return countTime<0?0:countTime;
	}

	public void addPropInfo(int sid,int num,int playerMin,int playerMax,
		int buildType,int buildMin,int buildMax,int limit)
	{
		PropInfo prop=new PropInfo(sid,num,playerMin,playerMax,buildType,
			buildMin,buildMax,limit);
		props.add(prop);
	}

	/** ��鵱ǰ�����Ƿ������ȡ,д��boolean:�Ƿ�����������Ϣ;short:sid;byte:num;int:����ʱ */
	public void checkAward(Player player,CreatObjectFactory objectFactory,
		ByteBuffer data)
	{
		int time=TimeKit.getSecondTime();
		int[] awardInfo=getPlayerAward(player);
		int countTime=awardInfo[2];
		// �Ƿ�����ȡ�������½���״̬
		boolean isAvailable=false;
		if(awardInfo[2]!=0&&time>=awardInfo[2])
		{
			Award award=(Award)Award.factory
				.newSample(ActivityContainer.EMPTY_SID);
			award.setRandomProps(new int[]{awardInfo[0],awardInfo[1],
				Award.PROB_ABILITY});
			award.awardSelf(player,TimeKit.getSecondTime(),null,
				objectFactory,null,new int[]{EquipmentTrack.FROM_ONLINE});
			countTime=resetPlayerAward(player)[2];
			isAvailable=true;
		}
		data.writeBoolean(isAvailable);
		// �������ȡ���򷵻ر��ν���Ʒ��Ϣ
		if(isAvailable)
		{
			data.writeShort(awardInfo[0]);
			data.writeByte(awardInfo[1]);
		}
		data.writeInt((countTime-time)<0?0:(countTime-time));// ǰ�˵���ʱ
	}
	
	/** �����Ǽ���Ȩ�� */
	public void resetTotalWeight()
	{
		totalWeight=0;
		for(int i=0;i<ONLINE_LUCKY_WEIGHT.length;i++)
		{
			totalWeight+=ONLINE_LUCKY_WEIGHT[i];
		}
	}

	/** ��ȡ�����������������Ǽ� */
	public int randomLimit()
	{
		int random=MathKit.randomValue(0,totalWeight);
		int limit=0;
		int gv=0;
		for(int i=0;i<ONLINE_LUCKY_WEIGHT.length;i++)
		{
			gv+=ONLINE_LUCKY_WEIGHT[i];
			if(gv>random)
			{
				limit=i+1;
				break;
			}
		}
		return limit;
	}
	
	public class PropInfo
	{

		/** ����Ʒsid */
		int sid;
		/** ����Ʒ���� */
		int num;
		/** ��ҵȼ����ޣ��粻��Ҫ��������0 */
		int playerMin;
		/** ��ҵȼ����ޣ���ʹ�õ�ǰ��߼���-1 */
		int playerMax;
		/** ����Ľ������ͣ��粻��Ҫ��������0 */
		int buildType;
		/** �������͵ȼ����� */
		int buildMin;
		/** �������͵ȼ����ޣ���ʹ�õ�ǰ��߼���-1 */
		int buildMax;
		/** ��������1,2,3,4,5��Ӧ��ҵȴ�ʱ�� */
		int limit;

		public PropInfo(int sid,int num,int playerMin,int playerMax,
			int buildType,int buildMin,int buildMax,int limit)
		{
			super();
			this.sid=sid;
			this.num=num;
			this.playerMin=playerMin;
			this.playerMax=playerMax;
			this.buildType=buildType;
			this.buildMin=buildMin;
			this.buildMax=buildMax;
			this.limit=limit;
		}

	}
}
