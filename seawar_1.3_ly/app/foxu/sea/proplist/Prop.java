package foxu.sea.proplist;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.builds.Produceable;
import foxu.sea.builds.Product;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

/**
 * 类说明：物品类
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */
public class Prop extends Sample implements Produceable
{
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	
	/** 物品类型：  VALID无效 PROP物品  EQUIP装备  SHIP船只 OFFICER军官*/
	public static int VALID=0,PROP=1,EQUIP=2,SHIP=3,OFFICER=4;
	
	/* static methods */
	/** 从字节数组中反序列化获得对象的域 */
	public static Prop bytesReadProp(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		if(sid==0) return null;
		Prop p=(Prop)factory.newSample(sid);
		if(p==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Prop.class.getName()
					+" bytesRead, invalid sid:"+sid);
		p.bytesRead(data);
		return p;
	}

	/* fields */
	/** 配置消耗 */
	int costResources[];
	/** 需要消耗的物品sid,num,sid,num */
	int costPropSid[];
	/** 每个物品建筑时间 */
	int buildTime;
	/** 建筑获取的经验 */
	int buildExperience;
	/** 名称 */
	String name="";
	/** 真名 */
	String pname="";
	/** 银票 */
	int price;
	/** 等级 */
	int level;
	// /** 物品单位 */
	String unit;
	/** 样式 */
	String style;
	/** 使用特效 */
	String effects;
	/** 描述 */
	String description;
	/** 临时性的描述信息(不需要通信) */
	String tempDescription=null;
	/** 显示对象 */
	Object show;
	/** 物品的排序ID */
	int sortId=0;
	/** 购买需要宝石数量 */
	int needGems;
	/** 使用等级限制 */
	int limitLevel;
	/** 联盟商店 购买所需个人积分 */
	int needAllianceScore;
	/** 每日最大兑换次数 */
	int maxExchangeNum;
	/**2级货币兑换价格**/
	int  coins;

	/* constructors */
	/** 保护样本的构造方法 */
	protected Prop()
	{
	}
	/* properties */
	/** 设置物品名字 */
	public void setName(String name)
	{
		this.name=name;
	}

	/** 获得名称 */
	public String getName()
	{
		return name;
	}
	/** 设置物品等级 */
	public void setLevel(int level)
	{
		this.level=level;
	}
	/** 获得等级 */
	public int getLevel()
	{
		return level;
	}
	/** 设置物品价格 */
	public void setPrice(int price)
	{
		this.price=price;
	}
	/** 获得价格 */
	public int getPrice()
	{
		return price;
	}
	// /** 设置物品单位 */
	// public void setUnit(String unit)
	// {
	// this.unit=unit;
	// }
	// /** 获得单位 */
	// public String getUnit()
	// {
	// return unit;
	// }
	/** 设置样式 */
	public void setStyle(String style)
	{
		this.style=style;
	}
	/** 获得样式 */
	public String getStyle()
	{
		return style;
	}
	/** 获得特效 */
	public String getEffects()
	{
		return effects;
	}
	/** 设置特效 */
	public void setEffects(String effects)
	{
		this.effects=effects;
	}
	/** 获得描述 */
	public String getDescription()
	{
		return description;
	}
	/** 获得临时描述信息 */
	public String getTempDescription()
	{
		return tempDescription;
	}
	/** 设置临时描述信息 */
	public void setTempDescription(String tempDescription)
	{
		this.tempDescription=tempDescription;
	}
	/** 获得显示对象 */
	public Object getShow()
	{
		return show;
	}
	/** 设置显示对象 */
	public void setShow(Object obj)
	{
		show=obj;
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
	}
	/* common methods */
	public String toString()
	{
		return "sid="+getSid()+",id="+getId()+",name="+name;
	}

	public int getSortId()
	{
		return sortId;
	}
	public void setSortId(int sortId)
	{
		this.sortId=sortId;
	}

	/**
	 * @return resources
	 */
	public int[] getCostResources()
	{
		return costResources;
	}

	/**
	 * @param resources 要设置的 resources
	 */
	public void setCostResources(int[] resources)
	{
		this.costResources=resources;
	}

	/**
	 * @return buildExperience
	 */
	public int getBuildExperience()
	{
		return buildExperience;
	}

	/**
	 * @param buildExperience 要设置的 buildExperience
	 */
	public void setBuildExperience(int buildExperience)
	{
		this.buildExperience=buildExperience;
	}

	/**
	 * @return buildTime
	 */
	public int getBuildTime()
	{
		return buildTime;
	}

	/**
	 * @param buildTime 要设置的 buildTime
	 */
	public void setBuildTime(int buildTime)
	{
		this.buildTime=buildTime;
	}

	/**
	 * @return costPropSid
	 */
	public int[] getCostPropSid()
	{
		return costPropSid;
	}

	/**
	 * @param costPropSid 要设置的 costPropSid
	 */
	public void setCostPropSid(int[] costPropSid)
	{
		this.costPropSid=costPropSid;
	}
	public void cancel(Player player,Product product)
	{
	}
	public void finish(Player player,Product product,CreatObjectFactory objectFactory)
	{
	}

	/**
	 * @return needGems
	 */
	public int getNeedGems()
	{
		return needGems;
	}

	/**
	 * @param needGems 要设置的 needGems
	 */
	public void setNeedGems(int needGems)
	{
		this.needGems=needGems;
	}
	public void cancelUp(Player player,Product product,CreatObjectFactory objectFactory)
	{
		// TODO 自动生成方法存根
		
	}
	
//	public String getPname()
//	{
//		return pname;
//	}
	
	public void setPname(String pname)
	{
		this.pname=pname;
	}
	
	public int getLimitLevel()
	{
		return limitLevel;
	}
	
	public void setLimitLevel(int limitLevel)
	{
		this.limitLevel=limitLevel;
	}
	
	public int getNeedAllianceScore()
	{
		return needAllianceScore;
	}
	
	public void setNeedAllianceScore(int needAllianceScore)
	{
		this.needAllianceScore=needAllianceScore;
	}
	
	public int getMaxExchangeNum()
	{
		return maxExchangeNum;
	}
	
	public void setMaxExchangeNum(int maxExchangeNum)
	{
		this.maxExchangeNum=maxExchangeNum;
	}
	
	public int getCoins()
	{
		return coins;
	}
	
	public void setCoins(int coins)
	{
		this.coins=coins;
	}

}