package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.Resources;
import foxu.sea.builds.produce.StandProduce;

/**
 * ��ҽ��� author:icetiger
 */
public class PlayerBuild extends Build
{
	/** ������������ʱ�� */
	public static final int BUILD_OUT_TIME=0;
	/** Serialization fileds */
	/** �������ʱ��ʱ�� */
	int buildCompleteTime;
	/** ��ǰ����Ľ���ʱ�� */
	int buildTotleTime;
	/** ����λ�� */
	int index;
	/** ������� */
	Produce produce;

	/** configure fileds */
	/** ����ǲֿ� ���ǲֿ����� �����������Ǳ�������� */
	int capacity[];
	/** ÿ���ȼ�������������Ʒsid,��λһ��(�ȼ�,sid) */
	int[] buildPropSids;
	/** ������Ʒ������ȼ����� ��λһ��(����ȼ�,sid) */
	int[] levelPropSids;
	/** ��Ǯ���� */
	int moneyCost[];
	/** �������� */
	int metalCost[];
	/** ʯ������ */
	int oilCost[];
	/** ������ */
	int siliconCost[];
	/** ������ */
	int uraniumCost[];
	/** �������� */
	int propSidCost[];
	/** ����ʱ�� */
	int buildTime[];
	/** �������õľ��� */
	int experience[];
	/** ��ʯ���� */
	int gemsCost[];
	/** ��������ķ���ֵ(ÿ��) */
	int giveProsperity;

	/** ��õ�ǰ���� */
	public int getNowCapacity()
	{
		if(capacity==null) return 0;
		if(buildLevel>=capacity.length) return capacity[capacity.length-1];
		if(buildLevel<=0) return 0;
		return capacity[buildLevel-1];
	}

	/**
	 * ���� checkTime�������ʱ��� ��һ�����ǵ�ǰʱ��
	 */
	public void produce(Player player,int checkTime,CreatObjectFactory objectFactory)
	{
		if(produce==null) return;
		produce.produce(player,buildLevel,checkTime,objectFactory);
	}

	/** ����Ƿ������һ���������� */
	public boolean checkProduce(Player player)
	{
		if(produce==null) return false;
		return produce.checkProduce(player);
	}

	/** ����ܷ�ȡ��index�±�Ķ��� */
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

	/** ���� */
	public void levelUp()
	{
		buildLevel++;
	}

	/** ȡ��������Ʒ */
	public void cancelProduce(Player player,int index,CreatObjectFactory objectFactory)
	{
		if(produce==null) return;
		if(produce instanceof StandProduce)
		{
			((StandProduce)produce).cancelProduce(player,index,objectFactory);
		}
	}

	/** ȡ�������������¼� */
	public void cancelBuilding(Player player)
	{
		float scroe=(float)(buildCompleteTime-TimeKit.getSecondTime())
			/(float)getLevelBuildTime(buildLevel);
		if(scroe<=0) return;
		/** ������Դ */
		Resources.addResources(player.getResources(),
			(int)(getLevelMetalCost(buildLevel)*scroe),
			(int)(getLevelOilCost(buildLevel)*scroe),
			(int)(getLevelSiliconCost(buildLevel)*scroe),
			(int)(getLevelUraniumCost(buildLevel)*scroe),
			(int)(getLevelMoneyCost(buildLevel)*scroe),player);
		buildCompleteTime = 0;
	}

	/** ��ǰ�Ƿ������ */
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

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(buildCompleteTime);
		data.writeInt(buildTotleTime);
		data.writeByte(index);
		if(produce!=null) produce.bytesWrite(data);
	}
	/** ������������л����ֽڻ����� */
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
	 * @param buildCompleteTime Ҫ���õ� buildCompleteTime
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
	 * @param index Ҫ���õ� index
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
	 * @param produce Ҫ���õ� produce
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
	 * @param propSidCost Ҫ���õ� propSidCost
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
	 * @param metalCost Ҫ���õ� metalCost
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
	 * @param moneyCost Ҫ���õ� moneyCost
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
	 * @param oilCost Ҫ���õ� oilCost
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
	 * @param siliconCost Ҫ���õ� siliconCost
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
	 * @param uraniumCost Ҫ���õ� uraniumCost
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
	 * @param buildTime Ҫ���õ� buildTime
	 */
	public void setBuildTime(int[] buildTime)
	{
		this.buildTime=buildTime;
	}

	/**
	 * @param buildShipSids Ҫ���õ� buildShipSids
	 */
	public void setBuildPropSids(int[] buildShipSids)
	{
		this.buildPropSids=buildShipSids;
	}
	/**
	 * ���ָ��sid��Ʒ�ڵ�ǰ�ȼ��ܷ�����
	 * 
	 * @param sid ָ��sid
	 * @return ����true��ʾ��������
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
	 * ���ָ��sid��Ʒ�ڵ�ǰ����ȼ��ܷ�����
	 * 
	 * @param sid ָ��sid
	 * @return ����true��ʾ��������
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
	 * @param gemsCost Ҫ���õ� gemsCost
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
	 * @param experience Ҫ���õ� experience
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
	 * @param buildTotleTime Ҫ���õ� buildTotleTime
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
