package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.Resources;
import foxu.sea.builds.produce.StandProduce;

/**
 * 玩家建筑 author:icetiger
 */
public class PlayerBuild extends Build
{
	/** 建筑允许的误差时间 */
	public static final int BUILD_OUT_TIME=0;
	/** Serialization fileds */
	/** 建筑完成时的时间 */
	int buildCompleteTime;
	/** 当前所需的建筑时间 */
	int buildTotleTime;
	/** 建筑位置 */
	int index;
	/** 生产相关 */
	Produce produce;

	/** configure fileds */
	/** 如果是仓库 就是仓库容量 其他建筑就是本身的容量 */
	int capacity[];
	/** 每个等级允许生产的物品sid,二位一组(等级,sid) */
	int[] buildPropSids;
	/** 生产物品的人物等级需求 二位一组(人物等级,sid) */
	int[] levelPropSids;
	/** 金钱消耗 */
	int moneyCost[];
	/** 金属消耗 */
	int metalCost[];
	/** 石油消耗 */
	int oilCost[];
	/** 硅消耗 */
	int siliconCost[];
	/** 铀消耗 */
	int uraniumCost[];
	/** 道具消耗 */
	int propSidCost[];
	/** 建筑时间 */
	int buildTime[];
	/** 建筑后获得的经验 */
	int experience[];
	/** 宝石消耗 */
	int gemsCost[];
	/** 建筑赋予的繁荣值(每级) */
	int giveProsperity;

	/** 获得当前容量 */
	public int getNowCapacity()
	{
		if(capacity==null) return 0;
		if(buildLevel>=capacity.length) return capacity[capacity.length-1];
		if(buildLevel<=0) return 0;
		return capacity[buildLevel-1];
	}

	/**
	 * 生产 checkTime传入检查的时间点 不一定会是当前时间
	 */
	public void produce(Player player,int checkTime,CreatObjectFactory objectFactory)
	{
		if(produce==null) return;
		produce.produce(player,buildLevel,checkTime,objectFactory);
	}

	/** 检查是否还能添加一个生产队列 */
	public boolean checkProduce(Player player)
	{
		if(produce==null) return false;
		return produce.checkProduce(player);
	}

	/** 检查能否取消index下标的队列 */
	public boolean checkCancelProduce(int index)
	{
		if(produce==null) return false;
		if(produce instanceof StandProduce)
		{
			return ((StandProduce)produce).checkCancelProduce(index);
		}
		return false;
	}
	/** produce init */
	public void init(int checkTime)
	{
		if(produce==null) return;
		produce.init(checkTime);
	}

	/** 升级 */
	public void levelUp()
	{
		buildLevel++;
	}

	/** 取消生产物品 */
	public void cancelProduce(Player player,int index,CreatObjectFactory objectFactory)
	{
		if(produce==null) return;
		if(produce instanceof StandProduce)
		{
			((StandProduce)produce).cancelProduce(player,index,objectFactory);
		}
	}

	/** 取消建筑或升级事件 */
	public void cancelBuilding(Player player)
	{
		float scroe=(float)(buildCompleteTime-TimeKit.getSecondTime())
			/(float)getLevelBuildTime(buildLevel);
		if(scroe<=0) return;
		/** 返回资源 */
		Resources.addResources(player.getResources(),
			(int)(getLevelMetalCost(buildLevel)*scroe),
			(int)(getLevelOilCost(buildLevel)*scroe),
			(int)(getLevelSiliconCost(buildLevel)*scroe),
			(int)(getLevelUraniumCost(buildLevel)*scroe),
			(int)(getLevelMoneyCost(buildLevel)*scroe),player);
		buildCompleteTime = 0;
	}

	/** 当前是否建筑完成 */
	public boolean checkBuildTime(int checkTime)
	{
		if(buildCompleteTime==0) return false;
		if((buildCompleteTime-BUILD_OUT_TIME)>checkTime) return false;
		return true;
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		buildCompleteTime=data.readInt();
		buildTotleTime=data.readInt();
		index=data.readUnsignedByte();
		if(produce!=null) 
		{
			produce.bytesRead(data);
			produce.setBuildIndex(index);
		}
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(buildCompleteTime);
		data.writeInt(buildTotleTime);
		data.writeByte(index);
		if(produce!=null) produce.bytesWrite(data);
	}
	/** 将对象的域序列化到字节缓存中 */
	public void showBytesWrite(ByteBuffer data,int current)
	{
		super.showBytesWrite(data,current);
		data.writeInt(buildCompleteTime-current>0?buildCompleteTime-current:0);
		data.writeInt(buildTotleTime);
		data.writeByte(index);
		if(produce!=null) produce.showBytesWrite(data,current);
	}
	/**
	 * @return buildCompleteTime
	 */
	public int getBuildCompleteTime()
	{
		return buildCompleteTime;
	}

	/**
	 * @param buildCompleteTime 要设置的 buildCompleteTime
	 */
	public void setBuildCompleteTime(int buildCompleteTime)
	{
		this.buildCompleteTime=buildCompleteTime;
	}

	/**
	 * @return index
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @param index 要设置的 index
	 */
	public void setIndex(int index)
	{
		this.index=index;
		if(produce!=null)
		produce.setBuildIndex(this.index);
	}

	/**
	 * @return produce
	 */
	public Produce getProduce()
	{
		return produce;
	}

	/**
	 * @param produce 要设置的 produce
	 */
	public void setProduce(Produce produce)
	{
		this.produce=produce;
	}

	/**
	 * @return propSidCost
	 */
	public int[] getPropSidCost()
	{
		return propSidCost;
	}

	public int getLevelPropSidCost(int buildLevel)
	{
		if(propSidCost==null) return 0;
		if(buildLevel>=propSidCost.length)
			return propSidCost[propSidCost.length-1];
		return propSidCost[buildLevel];
	}

	/**
	 * @param propSidCost 要设置的 propSidCost
	 */
	public void setPropSidCost(int[] propSidCost)
	{
		this.propSidCost=propSidCost;
	}

	/**
	 * @return metalCost
	 */
	public int[] getMetalCost()
	{
		return metalCost;
	}

	public int getLevelMetalCost(int buildLevel)
	{
		if(metalCost==null) return 0;
		if(buildLevel>=metalCost.length)
			return metalCost[metalCost.length-1];
		return metalCost[buildLevel];
	}

	/**
	 * @param metalCost 要设置的 metalCost
	 */
	public void setMetalCost(int[] metalCost)
	{
		this.metalCost=metalCost;
	}

	public int getLevelMoneyCost(int buildLevel)
	{
		if(moneyCost==null) return 0;
		if(buildLevel>=moneyCost.length)
			return moneyCost[moneyCost.length-1];
		return moneyCost[buildLevel];
	}

	/**
	 * @param moneyCost 要设置的 moneyCost
	 */
	public void setMoneyCost(int[] moneyCost)
	{
		this.moneyCost=moneyCost;
	}

	public int getLevelOilCost(int buildLevel)
	{
		if(oilCost==null) return 0;
		if(buildLevel>=oilCost.length) return oilCost[oilCost.length-1];
		return oilCost[buildLevel];
	}

	/**
	 * @param oilCost 要设置的 oilCost
	 */
	public void setOilCost(int[] oilCost)
	{
		this.oilCost=oilCost;
	}

	public int getLevelSiliconCost(int buildLevel)
	{
		if(siliconCost==null) return 0;
		if(buildLevel>=siliconCost.length)
			return siliconCost[siliconCost.length-1];
		return siliconCost[buildLevel];
	}

	/**
	 * @param siliconCost 要设置的 siliconCost
	 */
	public void setSiliconCost(int[] siliconCost)
	{
		this.siliconCost=siliconCost;
	}

	public int getLevelUraniumCost(int buildLevel)
	{
		if(uraniumCost==null) return 0;
		if(buildLevel>=uraniumCost.length)
			return uraniumCost[uraniumCost.length-1];
		return uraniumCost[buildLevel];
	}

	/**
	 * @param uraniumCost 要设置的 uraniumCost
	 */
	public void setUraniumCost(int[] uraniumCost)
	{
		this.uraniumCost=uraniumCost;
	}

	public int getLevelBuildTime(int buildLevel)
	{
		if(buildTime==null) return 0;
		if(buildLevel>=buildTime.length)
			return buildTime[buildTime.length-1];
		return buildTime[buildLevel];
	}

	/**
	 * @param buildTime 要设置的 buildTime
	 */
	public void setBuildTime(int[] buildTime)
	{
		this.buildTime=buildTime;
	}

	/**
	 * @param buildShipSids 要设置的 buildShipSids
	 */
	public void setBuildPropSids(int[] buildShipSids)
	{
		this.buildPropSids=buildShipSids;
	}
	/**
	 * 检查指定sid物品在当前等级能否生产
	 * 
	 * @param sid 指定sid
	 * @return 返回true表示可以生产
	 */
	public boolean checkLevelPropSid(int sid)
	{
		if(buildPropSids==null)return true;
		for(int i=0;i<buildPropSids.length;i+=2)
		{
			if(buildPropSids[i]<=buildLevel)
			{
				if(sid==buildPropSids[i+1]) return true;
			}
			else
			{
				return false;
			}
		}
		return false;
	}
	
	/**
	 * 检查指定sid物品在当前人物等级能否生产
	 * 
	 * @param sid 指定sid
	 * @return 返回true表示可以生产
	 */
	public boolean checkPlayerLevelPropSid(int sid,int level)
	{
		if(levelPropSids==null)return true;
		for(int i=0;i<levelPropSids.length;i+=2)
		{
			if(levelPropSids[i]<=level)
			{
				if(sid==levelPropSids[i+1]) return true;
			}
			else
			{
				return false;
			}
		}
		return false;
	}
	

	public Object copy(Object obj)
	{
		PlayerBuild p=(PlayerBuild)super.copy(obj);
		if(produce!=null) p.produce=(Produce)produce.clone();
		return p;
	}
	//
	// public String getLevelBuildPropSids(int buildLevel)
	// {
	// if(buildPropSids==null) return "0";
	// if(buildLevel>=buildPropSids.length)
	// return buildPropSids[buildPropSids.length];
	// return buildPropSids[buildLevel];
	// }

	/**
	 * @param gemsCost 要设置的 gemsCost
	 */
	public void setGemsCost(int[] gemsCost)
	{
		this.gemsCost=gemsCost;
	}

	public int getLevelGemsCost(int buildLevel)
	{
		if(gemsCost==null) return 0;
		if(buildLevel>=gemsCost.length)
			return gemsCost[gemsCost.length-1];
		return gemsCost[buildLevel];
	}

	/**
	 * @param experience 要设置的 experience
	 */
	public void setExperience(int[] experience)
	{
		this.experience=experience;
	}

	public int getLevelExperience(int buildLevel)
	{
		if(experience==null) return 0;
		if(buildLevel>=experience.length)
			return experience[experience.length-1];
		return experience[buildLevel];
	}
	
	/**
	 * @return buildTotleTime
	 */
	public int getBuildTotleTime()
	{
		return buildTotleTime;
	}

	/**
	 * @param buildTotleTime 要设置的 buildTotleTime
	 */
	public void setBuildTotleTime(int buildTotleTime)
	{
		this.buildTotleTime=buildTotleTime;
	}

	/**
	 * @return buildTime
	 */
	public int[] getBuildTime()
	{
		return buildTime;
	}

	
	public int getGiveProsperity()
	{
		return giveProsperity;
	}

	
	public void setGiveProsperity(int giveProsperity)
	{
		this.giveProsperity=giveProsperity;
	}
	
	
	
}
