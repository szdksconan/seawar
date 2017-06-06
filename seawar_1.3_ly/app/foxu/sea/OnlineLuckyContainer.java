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
 * 在线神秘抽奖容器
 * 
 * @author Alan
 */
public class OnlineLuckyContainer
{

	private static OnlineLuckyContainer instance=new OnlineLuckyContainer();
	/** 在线神秘奖励星级对应的时间 */
	public static int ONLINE_LUCKY_LIMIT[];
	/** 在线神秘奖励星级对应的权重 */
	public static int ONLINE_LUCKY_WEIGHT[];
	/** 奖励配置 */
	public ArrayList props=new ArrayList();
	/** 在线神秘奖励星级对应的权重 */
	int totalWeight;
	
	public static OnlineLuckyContainer getInstance()
	{
		return instance;
	}

	/** 重置在线神秘奖励信息：奖励品sid,数量,可领取时间。 */
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
			// 配置上已经保证所有玩家每一星级都会有满足条件的任务
			if(prop.limit!=available)	continue;
			// 玩家等级上限取值
			if(prop.playerMin!=0)
			{
				topLimit=prop.playerMax;
				if(topLimit==-1) topLimit=PublicConst.MAX_PLAYER_LEVEL;
				if(player.getLevel()<prop.playerMin
					||player.getLevel()>topLimit) continue;
			}
			// 建筑类型等级限制
			int buildType=prop.buildType;
			if(buildType!=0)
			{
				PlayerBuild build=player.getIsland().getBuildByType(
					buildType,null);
				if(build==null) continue;
				// 指定建筑等级上限取值
				topLimit=prop.buildMax;
				if(topLimit==-1) topLimit=PublicConst.MAX_BUILD_LEVEL;
				if(prop.buildMin>build.getBuildLevel()
					||build.getBuildLevel()>topLimit) continue;
			}
			// 满足条件,记录索引
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

	/** 获取在线神秘奖励信息：奖励品sid,数量,可领取时间。无信息则全为0 */
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

	/** 获取本次奖励倒计时 */
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

	/** 检查当前奖励是否可以领取,写入boolean:是否是新设置信息;short:sid;byte:num;int:倒计时 */
	public void checkAward(Player player,CreatObjectFactory objectFactory,
		ByteBuffer data)
	{
		int time=TimeKit.getSecondTime();
		int[] awardInfo=getPlayerAward(player);
		int countTime=awardInfo[2];
		// 是否已领取并返回新奖励状态
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
		// 如果已领取，则返回本次奖励品信息
		if(isAvailable)
		{
			data.writeShort(awardInfo[0]);
			data.writeByte(awardInfo[1]);
		}
		data.writeInt((countTime-time)<0?0:(countTime-time));// 前端倒计时
	}
	
	/** 重置星级总权重 */
	public void resetTotalWeight()
	{
		totalWeight=0;
		for(int i=0;i<ONLINE_LUCKY_WEIGHT.length;i++)
		{
			totalWeight+=ONLINE_LUCKY_WEIGHT[i];
		}
	}

	/** 获取本次随机奖励允许的星级 */
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

		/** 奖励品sid */
		int sid;
		/** 奖励品数量 */
		int num;
		/** 玩家等级下限，如不需要此条件填0 */
		int playerMin;
		/** 玩家等级上限，如使用当前最高级填-1 */
		int playerMax;
		/** 需检测的建筑类型，如不需要此条件填0 */
		int buildType;
		/** 建筑类型等级下限 */
		int buildMin;
		/** 建筑类型等级上限，如使用当前最高级填-1 */
		int buildMax;
		/** 奖励级别：1,2,3,4,5反应玩家等待时长 */
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
