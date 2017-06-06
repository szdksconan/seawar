package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * 建筑 升级相当于删除原来的 重新加上一个新的 方面配置调整 author:icetiger
 */
public class Build extends Sample
{

	/**
	 * 建筑物 BUILD_METAL=1 金属矿 BUILD_OIL=2石油矿，BUILD_SILION=3硅矿
	 * BUILD_URANIUM=4铀矿，BUILD_MONEY=5金币，BUILD_SHIP=6船厂，BUILD_DIRECTOR=7指挥中心
	 * BUILD_RESEARCH=8研究院，BUILD_SHOP=9制造车间，BUILD_STORE=10仓库
	 * BUILD_AIR=11空军基地,BUILD_MISSILE=12导弹基地,BUILD_ARTILLERY=13火炮阵地,BUILD_SHIP_UPDATE=14船厂升级
	 * STRENTGH_SHIP=15舰船强化
	 */
	public static final int BUILD_METAL=1,BUILD_OIL=2,BUILD_SILION=3,
					BUILD_URANIUM=4,BUILD_MONEY=5,BUILD_SHIP=6,
					BUILD_DIRECTOR=7,BUILD_RESEARCH=8,BUILD_SHOP=9,
					BUILD_STORE=10,BUILD_AIR=11,BUILD_MISSILE=12,
					BUILD_ARTILLERY=13,BUILD_SHIP_UPDATE=14,BUILD_STRENTGH_SHIP=15;

	/** 建筑最大等级 还跟岛屿等级有关 */
//	public static final int BUILD_MAX_LEVEL=60;
	/** 默认仓库容量 */
	public static final int DEFAULT_CAPACITY=5000;
	/** Serialization fileds */
	/** 建筑等级 */
	int buildLevel;

	/** configure fileds */
	/** 建筑名字 */
	String buildName;
	/** 建筑类型 */
	int buildType;
	/** 建筑介绍 */
	String description;
	/** 是否可以撤除 */
	boolean isRemove;
	/** 最大等级 */
	int maxLevel;

	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();

	/** 从字节数组中反序列化获得对象的域 */
	public static Build bytesReadBuild(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Build r=(Build)factory.newSample(sid);
		if(r==null)
		{
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Build.class.getName()
					+" bytesRead, invalid sid:"+sid);
		}
		r.bytesRead(data);
		return r;
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		buildLevel=data.readUnsignedByte();
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(buildLevel);
	}
	public void showBytesWrite(ByteBuffer data,int current)
	{
		super.bytesWrite(data);
		data.writeByte(buildLevel);
	}

	/**
	 * @return buildType
	 */
	public int getBuildType()
	{
		return buildType;
	}

	/**
	 * @param buildType 要设置的 buildType
	 */
	public void setBuildType(int buildType)
	{
		this.buildType=buildType;
	}

	/**
	 * @return buildLevel
	 */
	public int getBuildLevel()
	{
		return buildLevel;
	}

	/**
	 * @param buildLevel 要设置的 buildLevel
	 */
	public void setBuildLevel(int buildLevel)
	{
		this.buildLevel=buildLevel;
	}

	/**
	 * @return remove
	 */
	public boolean isRemove()
	{
		return isRemove;
	}

	/**
	 * @param remove 要设置的 remove
	 */
	public void setRemove(boolean remove)
	{
		this.isRemove=remove;
	}

	/**
	 * @return buildName
	 */
	public String getBuildName()
	{
		return buildName;
	}

	/**
	 * @param buildName 要设置的 buildName
	 */
	public void setBuildName(String buildName)
	{
		this.buildName=buildName;
	}

	/**
	 * @return maxLevel
	 */
	public int getMaxLevel()
	{
		return maxLevel;
	}

	/**
	 * @param maxLevel 要设置的 maxLevel
	 */
	public void setMaxLevel(int maxLevel)
	{
		this.maxLevel=maxLevel;
	}
}
