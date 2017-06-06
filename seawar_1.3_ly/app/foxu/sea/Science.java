package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.Produceable;
import foxu.sea.builds.Product;
import foxu.sea.builds.produce.ScienceProduce;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.recruit.RecruitDayTask;
import foxu.sea.recruit.RecruitKit;
import foxu.sea.task.TaskEventExecute;

/**
 * 科技(经验,速度,侦查,伪装)
 * 
 * @author rockzyt
 */
public class Science extends Sample implements Produceable,
	AdjustmentsetUseable
{

	/* static fields */
	public static final SampleFactory factory=new SampleFactory();

	/* static methods */
	public static Science bytesReadSample(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Sample sample=factory.newSample(sid);
		if(sample==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Science.class
					.getName()
					+" bytesRead, invalid sid:"+sid);
		sample.bytesRead(data);
		return (Science)sample;
	}

	/* fileds */
	/** 名字 */
	String name;
	/** 科技等级 */
	int level;
	/** 改变属性类型 */
	int baseType;
	int attributeType;
	/** 数据 */
	int[] value;
	/** value类型,true为固定值,false为百分比 */
	boolean fix=false;
	/** 描述 */
	String description;
	/** 最大等级 */
	int maxLevel;

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
	/** 宝石消耗 */
	int gemsCost[];
	/** 建筑时间 */
	int buildTime[];
	/** 建筑后获得的经验 */
	int experience[];

	/* properties */
	/** 获得等级 */
	public int getLevel()
	{
		return level;
	}
	/** 获得数据 */
	public int getValue()
	{
		if(level<=0)level=1;
		return value[level-1];
	}

	/* methods */
	public boolean isFix()
	{
		return fix;
	}
	public void finish(Player player,Product product,CreatObjectFactory objectFactory)
	{
		PlayerBuild build=player.getIsland().getBuildByIndex(
			BuildInfo.INDEX_2,player.getIsland().getBuilds());
		ScienceProduce sp=(ScienceProduce)build.getProduce();
		Science s=sp.getScienceBySid(getSid());
		int lv=0;
		if(s==null)
		{
			level++;
			sp.addScience(this);
			lv=level;
		}
		else
		{
			s.level++;
			lv=s.level;
		}
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.SCIENCE_LEVEL_UP_EVENT,this,player,null);
		// 重置科技影响
		SeaBackKit.resetPlayerSkill(player,objectFactory);
		// 新兵福利
		RecruitKit.pushTask(RecruitDayTask.SCIENCE_LV,lv,player,true);
	}

	public void cancel(Player player,Product product)
	{
		int level=0;
		PlayerBuild build=player.getIsland().getBuildByIndex(
			BuildInfo.INDEX_2,player.getIsland().getBuilds());
		ScienceProduce sp=(ScienceProduce)build.getProduce();
		Science s=sp.getScienceBySid(getSid());
		if(s!=null) level=s.getLevel();
		// TODO 自动生成方法存根
		float scroe=(float)(product.getFinishTime()-TimeKit.getSecondTime())
			/(float)(buildTime[level]);
		if(product.getFinishTime()==0) scroe=1;
		if(scroe<0) return;
		if(scroe>1) scroe=1;
		int propCostSid=getLevelPropSidCost(level);
		// 返回道具
		if(propCostSid!=0)
		{
			NormalProp prop=(NormalProp)Prop.factory.newSample(propCostSid);
			player.getBundle().incrProp(prop,true);
		}
		/** 返回资源 */
		Resources.addResources(player.getResources(),
			(int)(getLevelMetalCost(level)*scroe),
			(int)(getLevelOilCost(level)*scroe),
			(int)(getLevelSiliconCost(level)*scroe),
			(int)(getLevelUraniumCost(level)*scroe),
			(int)(getLevelMoneyCost(level)*scroe),player);
	}
	/** 科技改变生活... 你妹 */
	public void setChangeValue(AttrAdjustment adjustment)
	{
		if(attributeType==0)
		{
			adjustment.add(baseType,getValue(),fix);
		}
		else
		{
			adjustment.add(baseType,attributeType,getValue(),fix);
		}
	}
	public void showBytesWrite(ByteBuffer data,int current)
	{
		super.bytesWrite(data);
		data.writeByte(level);
	}
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(level);

	}
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		level=data.readUnsignedByte();
		return this;
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
		if(buildLevel>=moneyCost.length) return moneyCost[moneyCost.length];
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
		if(buildLevel>=oilCost.length) return oilCost[oilCost.length];
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
			return siliconCost[siliconCost.length];
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
			return uraniumCost[uraniumCost.length];
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
		if(buildLevel>=buildTime.length) return buildTime[buildTime.length];
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
			return experience[experience.length];
		return experience[buildLevel];
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
			return propSidCost[propSidCost.length];
		return propSidCost[buildLevel];
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
		if(buildLevel>=metalCost.length) return metalCost[metalCost.length];
		return metalCost[buildLevel];
	}

	/**
	 * @param propSidCost 要设置的 propSidCost
	 */
	public void setPropSidCost(int[] propSidCost)
	{
		this.propSidCost=propSidCost;
	}
	
	public void setLevel(int level)
	{
		this.level=level;
	}
	public void cancelUp(Player player,Product product,CreatObjectFactory objectFactory)
	{
		// TODO 自动生成方法存根
		
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public int getMaxLevel()
	{
		return maxLevel;
	}
	
	
}